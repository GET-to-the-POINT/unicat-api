package gettothepoint.unicatapi.domain.constant.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipTier {
    BASIC("Basic Subscription", "BASIC", 1_000L),
    PREMIUM("Premium Subscription", "PREMIUM", 2_000L),
    VIP("VIP Subscription", "VIP", 3_000L);

    private final String displayName;
    private final String koreanName;
    private final Long price;

    public String getAutoOrderName() {
        return String.format("자동결제 %s 결제", koreanName);
    }
}