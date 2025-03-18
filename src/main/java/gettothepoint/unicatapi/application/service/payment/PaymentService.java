package gettothepoint.unicatapi.application.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.CharacterUtil;
import gettothepoint.unicatapi.common.util.PaymentUtil;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentHistoryResponse;
import gettothepoint.unicatapi.domain.dto.payment.TossPaymentResponse;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Payment;
import gettothepoint.unicatapi.domain.repository.MemberRepository;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final AppProperties appProperties;
    private final PaymentUtil paymentUtil;
    private final MemberRepository memberRepository;


    public TossPaymentResponse confirmAndFinalizePayment(String orderId, Long amount, String paymentKey) {
        TossPaymentResponse tossResponse = confirmPaymentExternal(paymentKey, orderId, amount);
        processPayment(orderId, paymentKey, amount, tossResponse);
        processSubscription(orderId);
        processOrder(orderId, tossResponse);
        return tossResponse;
    }

    private void processSubscription(String orderId) {
        Order order = orderService.findById(orderId);
        Member member = order.getMember();
        subscriptionService.createSubscription(member, order);
    }

    private void processOrder(String orderId, TossPaymentResponse tossResponse) {
        TossPaymentStatus status = TossPaymentStatus.valueOf(tossResponse.getStatus());
        orderService.updateOrder(orderId, status);
    }

    private void processPayment(String orderId, String paymentKey, Long amount, TossPaymentResponse tossResponse) {
        Order order = orderService.findById(orderId);
        TossPaymentStatus status = TossPaymentStatus.valueOf(tossResponse.getStatus());
        String method = CharacterUtil.convertToUTF8(tossResponse.getMethod());
        String orderName = CharacterUtil.convertToUTF8(tossResponse.getOrderName());
        PayType payType = PayType.fromKoreanName(method);
        String approvedAt = tossResponse.getApprovedAt();
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(approvedAt);
        LocalDateTime approvedAtLocal = offsetDateTime.toLocalDateTime();
        savePayment(order, paymentKey, amount, status, payType, approvedAtLocal);
        tossResponse.setMethod(method);
        tossResponse.setOrderName(orderName);
    }

    private TossPaymentResponse confirmPaymentExternal(String paymentKey, String orderId, Long amount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", paymentUtil.createAuthorizationHeader());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "paymentKey", paymentKey,
                    "orderId", orderId,
                    "amount", amount
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    appProperties.toss().confirmUrl(), HttpMethod.POST, requestEntity, String.class
            );
            String responseBody = responseEntity.getBody();
            return objectMapper.readValue(responseBody, TossPaymentResponse.class);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while calling Toss API: ", e);
        }
    }

    public void savePayment(Order order, String paymentKey, Long amount, TossPaymentStatus status,
                            PayType payType, LocalDateTime approvedAt) {
        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(amount)
                .payType(payType)
                .tossPaymentStatus(status)
                .order(order)
                .productName(order.getOrderName())
                .approvedAt(approvedAt)
                .build();
        paymentRepository.save(payment);
    }

    public Payment findById(Long id) {
        return paymentRepository.findByid(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paymentId not found"));
    }

    public List<PaymentHistoryResponse> findByMemberEmail(String email) {
        List<Payment> payments = paymentRepository.findByOrder_Member_Email(email);
        return payments.stream()
                .map(PaymentHistoryResponse::fromEntity)
                .toList();
    }

    public void approveAutoPayment(String billingKey, PaymentApprovalRequest approvalRequest) {
        String url = "https://api.tosspayments.com/v1/billing/" + billingKey;

        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((appProperties.toss().secretKey() + ":").getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PaymentApprovalRequest> requestEntity = new HttpEntity<>(approvalRequest, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        Map<String, Object> responseBody = Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "자동 결제 승인이 실패했습니다."));

        String paymentKey = (String) responseBody.get("paymentKey");
        String statusStr = (String) responseBody.get("status");

        // 상태 문자열을 TossPaymentStatus 열거형으로 변환 (메서드는 직접 구현)
        TossPaymentStatus tossStatus = TossPaymentStatus.fromTossStatus(statusStr);


        LocalDateTime approvedAt = LocalDateTime.now();

        Order order = orderService.findById(approvalRequest.getOrderId());


        PayType payType = PayType.CARD; // 실제 결제 방식에 맞게 설정
        String productName = approvalRequest.getOrderName(); // 또는 다른 값을 사용

        // Payment 엔티티 생성 후 저장
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(paymentKey)
                .amount(approvalRequest.getAmount())
                .tossPaymentStatus(tossStatus)
                .payType(payType)
                .productName(productName)
                .approvedAt(approvedAt)
                .build();
        paymentRepository.save(payment);
    }
}