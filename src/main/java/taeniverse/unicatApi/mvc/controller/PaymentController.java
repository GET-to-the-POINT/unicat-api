package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Payment;
import taeniverse.unicatApi.mvc.repository.OrderRepository;
import taeniverse.unicatApi.mvc.service.PaymentService;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

import static javax.crypto.Cipher.SECRET_KEY;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Value("${toss.secret-key}")
    private String tossSecretKey;


    @GetMapping(value = "/confirm")
    @Transactional
    public ResponseEntity<?> confirmPayment(@RequestParam String orderId,
                                            @RequestParam Long amount,
                                            @RequestParam String paymentKey) {
        try {
            log.info("Received confirm request: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

            // Toss API 호출하여 전체 응답을 Map으로 받아옵니다.
            Map<String, Object> tossResponse = paymentService.confirmPayment(restTemplate,tossSecretKey, paymentKey, orderId, amount);

            // 주문 엔티티 조회
            Order order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

            // Toss API 응답에서 "method" 값을 추출하여 PayType으로 변환
            String methodValue = tossResponse.get("method").toString();
            PayType payType = PayType.fromString(methodValue);

            // PaymentEntity 저장: 최종 상태 SUCCESS로 저장
            Payment payment = paymentService.savePayment(order, paymentKey, amount, TossPaymentStatus.SUCCESS, payType);
            log.info("Payment saved successfully, paymentKey: {}", paymentKey);

            // 최종 결과를 JSON 객체로 구성
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("paymentKey", paymentKey);
            response.put("amount", amount);
            response.put("finalStatus", TossPaymentStatus.SUCCESS);
            response.put("payType", payType.name());
            response.put("tossResponse", tossResponse);

            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            log.error("Toss API Error: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException | IllegalArgumentException e) {
            log.error("결제 요청 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("결제 요청 중 오류 발생: " + e.getMessage());
        }
    }
}
