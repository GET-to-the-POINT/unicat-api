package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final BillingService billingService;

    // TODO: 이거 메서드 이름 뭘로하죠?
    @DeleteMapping
    public void cancelMySubscription(@AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.valueOf(jwt.getSubject());
        billingService.cancelRecurringByMember(memberId);
    }
}
