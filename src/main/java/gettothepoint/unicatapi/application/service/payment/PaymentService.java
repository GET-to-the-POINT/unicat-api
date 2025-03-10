package gettothepoint.unicatapi.application.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.common.util.CharacterUtil;
import gettothepoint.unicatapi.domain.dto.payment.TossPaymentResponse;
import gettothepoint.unicatapi.domain.entity.Member;
import gettothepoint.unicatapi.domain.entity.Order;
import gettothepoint.unicatapi.domain.entity.Payment;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final AppProperties appProperties;


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
        Payment payment = findByOrderId(orderId);
        subscriptionService.createSubscription(member, order, payment);
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
        savePayment(order, paymentKey, amount, status, payType,approvedAtLocal);
        tossResponse.setMethod(method);
        tossResponse.setOrderName(orderName);
    }

    private TossPaymentResponse confirmPaymentExternal(String paymentKey, String orderId, Long amount) {


        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", createAuthorizationHeader());
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
            int statusCode = responseEntity.getStatusCode().value();
            String responseBody = responseEntity.getBody();
            System.out.printf("Confirm API response status: %d, body: %s%n", statusCode, responseBody);

            return objectMapper.readValue(responseBody, TossPaymentResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Toss API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String createAuthorizationHeader() {
        String authString = appProperties.toss().secretKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
        return "Basic " + encodedAuth;
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
                .member(order.getMember())
                .approvedAt(approvedAt)
                .build();
        paymentRepository.save(payment);
    }

    public Payment findById(Long id) {
        return paymentRepository.findByid(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paymentId not found"));
    }

    public Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "orderId not found"));
    }

    public List<Payment> findByMemberEmail(String email) {
        return paymentRepository.findByMember_Email(email);
    }
}
