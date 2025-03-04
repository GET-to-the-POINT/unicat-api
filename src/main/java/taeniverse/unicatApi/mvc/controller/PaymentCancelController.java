package taeniverse.unicatApi.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentRequest;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentResponse;
import taeniverse.unicatApi.mvc.model.entity.CancelPayment;
import taeniverse.unicatApi.mvc.service.PaymentCancelService;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelController {


    private final PaymentCancelService paymentCancelService;

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(@RequestBody @Valid CancelPaymentRequest cancelRequest) {
        try {
            // ✅ paymentKey를 가져와서 서비스에 전달
            String paymentKey = cancelRequest.getPaymentKey();
            if (paymentKey == null || paymentKey.isEmpty()) {
                return ResponseEntity.badRequest().body("결제 취소를 위해 paymentKey가 필요합니다.");
            }

            // 서비스 레이어 호출: CancelPayment 엔티티 반환
            CancelPayment cancelPayment = paymentCancelService.cancelPayment(paymentKey, cancelRequest);
            // 엔티티를 DTO로 변환
            CancelPaymentResponse response = cancelPayment.toDto();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error while canceling payment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("결제 취소 중 오류가 발생했습니다.");
        }
    }
}