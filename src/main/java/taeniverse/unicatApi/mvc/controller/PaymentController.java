package taeniverse.unicatApi.mvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentRequest;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentResponse;
import taeniverse.unicatApi.mvc.model.dto.TossPaymentResponse;
import taeniverse.unicatApi.mvc.service.PaymentCancelService;
import taeniverse.unicatApi.mvc.service.PaymentService;

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