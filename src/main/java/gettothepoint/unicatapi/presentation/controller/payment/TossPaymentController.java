package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.application.service.payment.PlanService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.entity.payment.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final PlanService planService;

    @Operation(
            summary = "정기 결제 승인",
            description = "사용자의 빌링키를 저장하고 자동 결제 승인이 이루어집니다."
    )
    @GetMapping("/approve")
    public void approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            HttpServletResponse response) throws IOException {
        String email = jwt.getClaim("email");
        Plan premiumPlan = planService.getPlanByName("PREMIUM");
        orderService.create(email, premiumPlan);//todo 가주문 나중에 subscription컨트롤러 옮길수도 있음
        billingService.saveBillingKey(authKey, email);
        paymentService.approveAutoPayment(email);
        response.sendRedirect(appProperties.frontend().url());
    }

}
