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

@Tag(name = "Subscription", description = "구독 API")
@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final BillingService billingService;
    private final OrderService orderService;
    private final PlanService planService;

    @Operation(
            summary = "주문 생성",
            description = "사용자 이메일과 'PREMIUM' 구독 플랜을 기준으로 주문을 생성하는 API입니다. (BASIC, PREMIUM, VIP 중 선택 가능)"
    )
    @PostMapping
    public void create(
            @AuthenticationPrincipal Jwt jwt){
        Long memberId = Long.parseLong(jwt.getSubject());
        Plan premiumPlan = planService.getPlanByName("PREMIUM");
        orderService.create(memberId, premiumPlan);
    }

    @Operation(
            summary = "구독 취소",
            description = "현재 사용 중인 구독을 취소하며 다음 달 정기 결제를 중지시키는 API입니다."
    )
    @DeleteMapping
    public void delete(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        billingService.cancelRecurringByMember(memberId);
    }
}
