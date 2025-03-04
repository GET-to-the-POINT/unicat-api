package taeniverse.unicatApi.mvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Payment;
import taeniverse.unicatApi.mvc.repository.PaymentRepository;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;


    //Toss 결제 API 호출 공통 로직

    public Map<String, Object> confirmPayment(RestTemplate restTemplate, String secretKey, String paymentKey, String orderId, Long amount) {
        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> tossRequest = new HashMap<>();
        tossRequest.put("paymentKey", paymentKey);
        tossRequest.put("orderId", orderId);
        tossRequest.put("amount", amount);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(tossRequest, headers);
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        orderService.finalizeOrder(orderId, TossPaymentStatus.SUCCESS, PayType.CARD);

        log.info("Toss API response: {}", response.getBody());
        return response.getBody();
    }

    //결제 정보 저장 로직
    public Payment savePayment(Order order, String paymentKey, long amount, TossPaymentStatus status, PayType payType) {
        Payment payment = Payment.builder()
                .order(order)
                .orderName(order.getOrderName())
                .paymentKey(paymentKey)
                .amount(amount)
                .tossPaymentStatus(status)
                .payType(payType)
                .member(order.getMember())
                .createdAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(payment);
    }
}
