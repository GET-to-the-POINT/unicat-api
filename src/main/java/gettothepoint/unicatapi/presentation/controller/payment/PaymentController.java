package gettothepoint.unicatapi.presentation.controller.payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentRequest;
import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.dto.payment.TossPaymentResponse;
import gettothepoint.unicatapi.application.service.payment.PaymentCancelService;
import gettothepoint.unicatapi.application.service.payment.PaymentService;

import java.util.List;

@Tag(name = "Payment API", description = "결제 관련 API")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentCancelService paymentCancelService;

    @Operation(
        summary = "구매 이력 조회",
        description = "인증된 사용자의 구매 이력을 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "구매 이력 조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentHistoryDto.class))),
            @ApiResponse(responseCode = "401", description = "권한 없음")
        }
    )
    @GetMapping
    public List<PaymentHistoryDto> paymentsHistory(@AuthenticationPrincipal Jwt jwt) {
        // TODO: 구매 이력 조회 로직 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Operation(
        summary = "결제 확인",
        description = "주어진 주문 ID, 금액, 결제 키를 사용하여 결제를 확인하고, 최종 결제 결과를 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "결제 확인 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TossPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "권한 없음")
        }
    )
    @GetMapping("/confirm")
    public TossPaymentResponse confirmPayment(@RequestParam String orderId,
                                              @RequestParam Long amount,
                                              @RequestParam String paymentKey) {
        return paymentService.confirmAndFinalizePayment(orderId, amount, paymentKey);
    }

    @Operation(
        summary = "결제 취소",
        description = "결제를 취소하고, 취소 결과를 반환합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "결제 취소 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CancelPaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "권한 없음")
        }
    )
    @PostMapping("/cancel")
    public CancelPaymentResponse cancelPayment(@RequestBody @Valid CancelPaymentRequest cancelRequest) {
        Long paymentId = cancelRequest.getPaymentId();

        return paymentCancelService.cancelPayment(paymentId, cancelRequest);
    }

    public static class PaymentHistoryDto {
        // TODO: 구매 이력 DTO 필드 정의
    }
}