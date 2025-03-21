package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SubscriptionRequest {

    @Schema(description = "구독 티어 선택", example = "BASIC")
    @NotNull
    private SubscriptionPlan tier;
}