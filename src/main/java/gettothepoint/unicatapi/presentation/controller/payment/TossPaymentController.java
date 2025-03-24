package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Subscription - Toss", description = "Toss Payments API")
@RestController
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final AppProperties appProperties;

    @GetMapping("/approve")
    public void approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            HttpServletResponse response) throws IOException {
        String email = jwt.getClaim("email");
        orderService.create(email, SubscriptionPlan.PREMIUM);//todo 가주문 나중에 subscription컨트롤러 옮길수도 있음
        billingService.saveBillingKey(authKey, email);
        paymentService.approveAutoPayment(email);
        response.sendRedirect(appProperties.frontend().url());
    }

}
