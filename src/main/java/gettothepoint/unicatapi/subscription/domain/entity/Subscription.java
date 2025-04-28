package gettothepoint.unicatapi.subscription.domain.entity;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.vo.SubscriptionPeriod;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    private SubscriptionPeriod period;

    @ManyToOne
    private Plan plan;

    @OneToOne
    private Member member;

    @Builder
    public Subscription(Member member,Plan plan) {
        this.member = member;
        this.plan = (plan != null) ? plan : getDefaultPlan();
        this.period = SubscriptionPeriod.startNowIndefinitely();
    }

    private Plan getDefaultPlan() {
        return Plan.builder()
                .name("BASIC")
                .description("기본 플랜")
                .price(0L)
                .build();
    }

    public void changePlan(Plan newPlan) {
        if (newPlan == null) {
            throw new IllegalArgumentException("새로운 플랜은 null일 수 없습니다.");
        }
        if (newPlan.equals(this.plan)) {
            throw new IllegalStateException("동일한 플랜으로는 변경할 수 없습니다.");
        }
        this.plan = newPlan;
        this.period = SubscriptionPeriod.forOneMonthFromNow();
    }
    public boolean isExpired() {
        return period.isExpired();
    }

    public void expireToBasicPlan(Plan basicPlan) {
        this.plan = basicPlan;
        this.period = SubscriptionPeriod.startNowIndefinitely();
    }
}