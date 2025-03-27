package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Payment", description = "Toss Payments API")
@RestController
@RequestMapping("/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final BillingService billingService;
    private final PaymentService paymentService;
    private final AppProperties appProperties;

    @Operation(
            summary = "정기 결제 승인",
            description = "[Toss 결제](https://api.unicat.day/toss) - 토스 위젯을 위한 엔드포인트 입니다."
    )
    @GetMapping("/approve")
    public void approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            HttpServletResponse response) throws IOException {
        Long memberId = Long.parseLong(jwt.getSubject());
        billingService.saveBillingKey(authKey, memberId);
        paymentService.approveAutoPayment(memberId);
        response.sendRedirect(appProperties.frontend().url());
    }

    @Operation(
            summary = "정기 결제 취소",
            description = "현재 등록된 정기 결제를 취소합니다. 구독 자체는 BASIC으로 남습니다."
    )
    @DeleteMapping
    public void cancelRecurringPayment(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        billingService.cancelRecurringByMember(memberId);
    }
}
