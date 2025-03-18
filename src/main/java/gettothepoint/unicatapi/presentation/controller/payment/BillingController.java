package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.domain.dto.payment.OrderRequest;
import gettothepoint.unicatapi.domain.dto.payment.PaymentApprovalRequest;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final PaymentService paymentService;

    @GetMapping("/issue")
    public ResponseEntity<Map<String, String>> issueBillingKey(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String authKey,
            @RequestParam String customerKey
    ) {
        String email = jwt.getClaim("email");

        String billingKey = billingService.SaveBillingKey(authKey, customerKey, email);
        return ResponseEntity.ok(Map.of(
                "billingKey", billingKey,
                "message", "✅ 빌링키가 발급되고 저장되었습니다."
        ));
    }

    @PostMapping("/approve")
    public ResponseEntity<Map<String, String>> approveAutoPayment(
            @RequestParam String billingKey,
            @RequestBody PaymentApprovalRequest approvalRequest
    ) {
        paymentService.approveAutoPayment(billingKey, approvalRequest);
        return ResponseEntity.ok(Map.of("message", "✅ 자동 결제가 승인되었습니다."));
    }
}