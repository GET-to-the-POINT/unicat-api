package gettothepoint.unicatapi.domain.constant.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {
    BASIC("BASIC", 0L),
    PREMIUM("PREMIUM", 2_000L),
    VIP("VIP", 3_000L);

    private final String displayName;
    private final Long price;

    public String getAutoOrderName() {
        return String.format("%s 플랜 자동 결제", displayName);
    }
}