package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.OrderService;
import gettothepoint.unicatapi.application.service.payment.PlanService;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Subscription", description = "구독 API")
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final OrderService orderService;
    private final PlanService planService;

    @Operation(
            summary = "구독 변경",
            description = "기본 구독( BASIC )을 다른 플랜으로 변경합니다. 사용 가능한 플랜은 BASIC, PREMIUM, VIP입니다. " +
                    "구독 변경 [Toss 결제](https://api.unicat.day/toss)로 가서 결제가 필요합니다."
    )
    @PatchMapping
    public void updatePlan(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "선택할 구독 플랜 이름 (BASIC, PREMIUM, VIP 중 하나)", example = "PREMIUM")
            String planName
            ) {
        Long memberId = Long.parseLong(jwt.getSubject());
        Plan premiumPlan = planService.getPlanByName(planName);
        orderService.create(memberId, premiumPlan);
    }

}
