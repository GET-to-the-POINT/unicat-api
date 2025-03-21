package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Payment API", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingService billingService;

    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt) {
        String memberEmail = jwt.getClaim("email");
        return ResponseEntity.ok(paymentService.approveAutoPayment(memberEmail));
    }

    @PatchMapping("/{billingId}/cancel") //자동 결제 취소
    public void cancelSubscription(@PathVariable Long billingId) {
        billingService.cancelRecurring(billingId);
    }
}

