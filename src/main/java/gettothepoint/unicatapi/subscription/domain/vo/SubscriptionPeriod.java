package gettothepoint.unicatapi.subscription.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Embeddable
public class SubscriptionPeriod {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private SubscriptionPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작일과 종료일은 null일 수 없습니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static SubscriptionPeriod startNowIndefinitely() {
        return new SubscriptionPeriod(LocalDateTime.now(), LocalDateTime.of(9999, 12, 31, 23, 59, 59));
    }

    public static SubscriptionPeriod forOneMonthFromNow() {
        LocalDateTime now = LocalDateTime.now();
        return new SubscriptionPeriod(now, now.plusMonths(1));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
}