package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final BillingService billingService;
    private final PaymentService paymentService;

    @GetMapping("/approve")
    public Map<String, Object> approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt, @RequestParam String authKey) {
        String email = jwt.getClaim("email");
        billingService.saveBillingKey(authKey, email);
        return paymentService.approveAutoPayment(email);
    }

}
