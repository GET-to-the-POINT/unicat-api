package getToThePoint.unicatApi.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPaymentRequest {

    @NotBlank(message = "결제 키(paymentKey)는 필수 입력값입니다.")
    private String paymentKey;  // 필수: 결제 취소할 paymentKey

    @NotBlank(message = "취소 사유는 필수 입력값입니다.")
    private String cancelReason;  // 필수: 취소 사유

    @Positive(message = "취소 금액은 0보다 커야 합니다.")
    private Long cancelAmount;  // 선택: 부분 취소할 금액 (없으면 전액 취소)
}