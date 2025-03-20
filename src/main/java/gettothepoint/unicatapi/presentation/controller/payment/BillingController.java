package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.domain.dto.payment.BillingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/issue")
    public void issueBillingKey(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            @RequestParam String customerKey
    ) {
        String email = jwt.getClaim("email");
        billingService.saveBillingKey(authKey, customerKey, email);
    }

    @GetMapping("/list")
    public List<BillingResponse> getBillingList() {
        return billingService.getAllBillings();
    }
}