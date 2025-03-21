package gettothepoint.unicatapi.presentation.controller.payment;

import gettothepoint.unicatapi.application.service.payment.BillingService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;
import gettothepoint.unicatapi.domain.dto.payment.PaymentHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Payment API", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingService billingService;

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
        return paymentService.findPaymentHistoryByMember(email);
    }

    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveAutoPayment(
            @AuthenticationPrincipal Jwt jwt) {
        String memberEmail = jwt.getClaim("email");
        return ResponseEntity.ok(paymentService.approveAutoPayment(memberEmail));
    }

    @PatchMapping("/{billingId}/cancel") //자동 결제 취소
    public void cancelSubscription(@PathVariable Long billingId) {
        billingService.cancelSubscription(billingId);
    }
}

