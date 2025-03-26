package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PlanService;
import gettothepoint.unicatapi.domain.entity.payment.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscription - Subscription", description = "구독 API")
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final BillingService billingService;
    private final OrderService orderService;
    private final PlanService planService;

    @Operation(
            summary = "주문 생성",
            description = "사용자의 이메일과 구독 플랜을 기반으로 주문을 생성합니다. " +
                    "가능한 구독 플랜은 BASIC, PREMIUM, VIP 입니다."
    )
    @PostMapping
    public void create(
            @AuthenticationPrincipal Jwt jwt){
        String email = jwt.getClaimAsString("email");
        Plan premiumPlan = planService.getPlanByName("PREMIUM");
        orderService.create(email, premiumPlan);
    }

    @Operation(
            summary = "구독 취소",
            description = "구독 취소하면 다음달 사용자의 정기결제가 이루어지지 않습니다. "
    )
    @DeleteMapping
    public void delete(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        billingService.cancelRecurringByMember(memberId);
    }
}
