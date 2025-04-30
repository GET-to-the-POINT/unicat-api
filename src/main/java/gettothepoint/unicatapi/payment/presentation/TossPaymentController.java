package gettothepoint.unicatapi.payment.presentation;

import gettothepoint.unicatapi.member.application.MemberService;
import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.application.BillingService;
import gettothepoint.unicatapi.payment.application.OrderService;
import gettothepoint.unicatapi.payment.application.PaymentService;
import gettothepoint.unicatapi.common.properties.FrontendProperties;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.subscription.application.PlanService;
import gettothepoint.unicatapi.subscription.domain.Plan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "Payment", description = "Toss Payments API")
@RestController
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final FrontendProperties frontendProperties;
    private final MemberService memberService;
    private final OrderService orderService;
    private final PlanService planService;


    @Operation(
            summary = "정기 결제 승인",
            description = "[Toss 결제](https://api.unicat.day/toss) - 토스 위젯을 위한 엔드포인트 입니다."
    )
    @GetMapping("/approve")
    public void approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            HttpServletResponse response) throws IOException {

        UUID memberId = UUID.fromString(jwt.getSubject());
        Member member = memberService.getOrElseThrow(memberId);
        Plan plan = planService.getOrElseThrow("PREMIUM");

        Order order = orderService.create(member, plan);

        billingService.saveBillingKey(authKey, member.getId());

        paymentService.approveAutoPayment(member.getId());

        response.sendRedirect(frontendProperties.url());
    }

    @Operation(
            summary = "정기 결제 취소",
            description = "현재 등록된 정기 결제를 취소합니다. 구독 자체는 BASIC으로 남습니다."
    )
    @DeleteMapping
    public void cancelRecurringPayment(@AuthenticationPrincipal Jwt jwt) {
        UUID memberId = UUID.fromString(jwt.getSubject());
        billingService.cancelRecurringByMember(memberId);
    }
}
