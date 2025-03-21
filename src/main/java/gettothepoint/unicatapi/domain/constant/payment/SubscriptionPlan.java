package gettothepoint.unicatapi.domain.constant.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {
    BASIC("Basic Subscription", "BASIC", 0L),
    PREMIUM("Premium Subscription", "PREMIUM", 2_000L),
    VIP("VIP Subscription", "VIP", 3_000L);

    private final String displayName;
    private final String koreanName;
    private final Long price;

    public String getAutoOrderName() {
        return String.format("%s 플랜 자동 결제", koreanName);
    }
}