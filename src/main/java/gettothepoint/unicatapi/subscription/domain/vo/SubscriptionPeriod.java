package gettothepoint.unicatapi.subscription.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 기간을 나타내는 값 객체(Value Object)
 * 구독의 시작일과 종료일을 관리합니다.
 */
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

    /**
     * 현재 시점부터 무기한(사실상 영구)으로 지속되는 구독 기간 생성
     * 
     * @return 현재부터 무기한 지속되는 구독 기간
     */
    public static SubscriptionPeriod startNowIndefinitely() {
        return new SubscriptionPeriod(LocalDateTime.now(), LocalDateTime.of(9999, 12, 31, 23, 59, 59));
    }

    /**
     * 현재 시점부터 1개월 동안 지속되는 구독 기간 생성
     * 
     * @return 현재부터 1개월 동안 지속되는 구독 기간
     */
    public static SubscriptionPeriod forOneMonthFromNow() {
        LocalDateTime now = LocalDateTime.now();
        return new SubscriptionPeriod(now, now.plusMonths(1));
    }

    /**
     * 구독 기간이 만료되었는지 확인
     * 
     * @return 만료 여부 (true: 만료됨, false: 유효함)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
}
