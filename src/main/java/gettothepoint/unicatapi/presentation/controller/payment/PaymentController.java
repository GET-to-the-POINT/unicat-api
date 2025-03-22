package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Payment API", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/approve")
    public Map<String, Object> approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt) {
        String memberEmail = jwt.getClaim("email");
        return paymentService.approveAutoPayment(memberEmail);
    }

}

