package gettothepoint.unicatapi.presentation.controller.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentRequest;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.dto.payment.TossPaymentResponse;
import gettothepoint.unicatapi.application.service.payment.PaymentCancelService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentCancelService paymentCancelService;

    @GetMapping("/confirm")
    public TossPaymentResponse confirmPayment(@RequestParam String orderId,
                                              @RequestParam Long amount,
                                              @RequestParam String paymentKey) {
        return paymentService.confirmAndFinalizePayment(orderId, amount, paymentKey);
    }

    @PostMapping("/cancel")
    public CancelPaymentResponse cancelPayment(@RequestBody @Valid CancelPaymentRequest cancelRequest) {
        Long paymentId = cancelRequest.getPaymentId();

        return paymentCancelService.cancelPayment(paymentId, cancelRequest);
    }
}