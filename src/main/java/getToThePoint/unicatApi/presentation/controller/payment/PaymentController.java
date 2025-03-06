package getToThePoint.unicatApi.presentation.controller.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import getToThePoint.unicatApi.domain.dto.payment.CancelPaymentRequest;
import getToThePoint.unicatApi.domain.dto.payment.CancelPaymentResponse;
import getToThePoint.unicatApi.domain.dto.payment.TossPaymentResponse;
import getToThePoint.unicatApi.application.service.payment.PaymentCancelService;
import getToThePoint.unicatApi.application.service.payment.PaymentService;

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
        String paymentKey = cancelRequest.getPaymentKey();

        return paymentCancelService.cancelPayment(paymentKey, cancelRequest);
    }
}