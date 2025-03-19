package gettothepoint.unicatapi.presentation.controller.payment;
import gettothepoint.unicatapi.domain.dto.payment.*;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.repository.BillingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.application.service.payment.PaymentCancelService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Tag(name = "Payment API", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingRepository billingRepository;

    @Operation(
            summary = "구매 이력 조회",
            description = "인증된 사용자의 구매 이력을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "구매 이력 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentHistoryResponse.class))),
                    @ApiResponse(responseCode = "401", description = "권한 없음")
            }
    )

    @GetMapping("/history")
    public List<PaymentHistoryResponse> paymentsHistory(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaim("email");
        return paymentService.findByMemberEmail(email);
    }

    @PostMapping("/approve")  //결제 승인
    public ResponseEntity<Map<String, Object>> approveAutoPayment(
            @RequestParam String billingKey,
            @RequestBody PaymentApprovalRequest approvalRequest
    ) {
        Map<String, Object> tossResponse = paymentService.approveAutoPayment(billingKey, approvalRequest);
        return ResponseEntity.ok(tossResponse);
    }


    @PutMapping("/{billingId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long billingId) {
        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing not found"));

        billing.cancelSubscription();
        billingRepository.save(billing);

        return ResponseEntity.noContent().build();
    }
}

