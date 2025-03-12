package gettothepoint.unicatapi.domain.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CancelPaymentRequest {

    @NotNull
    private Long paymentId;

    @NotBlank(message = "취소 사유는 필수 입력값입니다.")
    private String cancelReason;  // 필수: 취소 사유
}