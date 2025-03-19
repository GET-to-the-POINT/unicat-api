package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/issue")
    public ResponseEntity<Map<String, String>> issueBillingKey(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            @RequestParam String customerKey
    ) {
        String email = jwt.getClaim("email");

        String billingKey = billingService.saveBillingKey(authKey, customerKey, email);
        return ResponseEntity.ok(Map.of(
                "billingKey", billingKey,
                "message", "✅ 빌링키가 발급되고 저장되었습니다."
        ));
    }
}