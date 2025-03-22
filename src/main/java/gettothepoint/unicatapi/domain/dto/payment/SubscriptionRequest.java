package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequest {

    @Schema(
            description = "구독 plan (가능한 값: basic, premium, vip)",
            example = "basic",
            allowableValues = {"basic", "premium", "vip"}
    )
    @NotNull
    private SubscriptionPlan plan;
}