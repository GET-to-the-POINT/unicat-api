package taeniverse.unicatApi.mvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import taeniverse.unicatApi.component.propertie.AppProperties;
import taeniverse.unicatApi.component.util.CharacterUtil;
import taeniverse.unicatApi.mvc.model.dto.TossPaymentResponse;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Payment;
import taeniverse.unicatApi.mvc.repository.PaymentRepository;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.io.IOException;
import java.util.Base64;
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

    /**
     * 외부 Toss API를 호출하고, 주문 및 결제 상태를 최종 처리한 후 Toss API 응답을 반환합니다.
     *
     * @param orderId    주문 ID
     * @param amount     결제 금액
     * @param paymentKey 결제 키
     * @return Toss API 응답을 Map으로 파싱한 결과
     */
    public TossPaymentResponse confirmAndFinalizePayment(String orderId, Long amount, String paymentKey) {
        TossPaymentResponse tossResponse = confirmPaymentExternal(paymentKey, orderId, amount);
        // 구독
        Order order = orderService.findById(orderId);
        Member member = order.getMember();
        subscriptionService.createSubscription(member, order);
        // 주문
        TossPaymentStatus status = TossPaymentStatus.valueOf(tossResponse.getStatus());
        orderService.updateOrder(orderId, status);
        // 결제
        String method = CharacterUtil.convertToUTF8(tossResponse.getMethod());
        String orderName = CharacterUtil.convertToUTF8(tossResponse.getOrderName());
        PayType payType = PayType.fromKoreanName(method);
        savePayment(order, paymentKey, amount, status, payType);

        tossResponse.setMethod(method);
        tossResponse.setOrderName(orderName);
        return tossResponse;
    }

    private TossPaymentResponse confirmPaymentExternal(String paymentKey, String orderId, Long amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
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
                    url, HttpMethod.POST, requestEntity, String.class
            );
            int statusCode = responseEntity.getStatusCode().value();//나중에 삭제
            String responseBody = responseEntity.getBody();
            System.out.printf("Confirm API response status: %d, body: %s%n", statusCode, responseBody);//응답값 확인용 나중에 삭제

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
    public void savePayment(Order order, String paymentKey, Long amount, TossPaymentStatus status, PayType payType) {
        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(amount)
                .payType(payType)
                .tossPaymentStatus(status)
                .order(order)
                .productName(order.getOrderName())
                .member(order.getMember())
                .build();
        paymentRepository.save(payment);
    }
    public Payment findByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + paymentKey));
    }
}