package taeniverse.unicatApi.mvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.service.OrderService;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessController {

    private final OrderService orderService;

    @PostMapping("/success")
    public ResponseEntity<?> handlePaymentSuccess(
            @RequestBody Map<String, Object> responseBody,
            @RequestHeader(value = "X-User-Id", required = false) Long memberId) {
        try {
            log.info("Complete Toss API Response: {}", responseBody);
            log.info("Received memberId: {}", memberId);

            // Map에서 orderId와 method 값을 추출
            String orderId = (String) responseBody.get("orderId");
            String methodValue = (String) responseBody.get("method");
            log.info("Extracted orderId: {}", orderId);
            log.info("Extracted payment method: '{}'", methodValue);

            // method 값을 PayType enum으로 변환 (값이 없으면 예외 발생)
            PayType payType;
            if (methodValue == null || methodValue.trim().isEmpty()) {
                throw new IllegalArgumentException("Payment method is empty");
            } else {
                payType = PayType.fromString(methodValue);
            }

            // 결제 성공 후 기존 가주문 업데이트: 상태를 SUCCESS로, 결제 방식(payType) 설정
            Order updatedOrder = orderService.finalizeOrder(orderId, TossPaymentStatus.SUCCESS, payType);
            log.info("Payment success processed for orderId: {}", orderId);

            // 최종 결과를 JSON 객체로 반환
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("paymentKey", responseBody.get("paymentKey"));
            response.put("amount", responseBody.get("amount"));
            response.put("finalStatus", TossPaymentStatus.SUCCESS);
            response.put("payType", payType.name());
            response.put("tossResponse", responseBody);
            response.put("memberId", memberId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error finalizing order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error finalizing order: " + e.getMessage());
        }
    }
}
