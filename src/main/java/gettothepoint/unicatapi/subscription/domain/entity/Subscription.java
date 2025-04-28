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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private SubscriptionPeriod period;

    @Builder
    public Subscription(Member member, Plan plan) {
        this.member = member;
        this.plan = plan;
        this.period = plan.isBasicPlan() 
            ? SubscriptionPeriod.startNowIndefinitely() 
            : SubscriptionPeriod.forOneMonthFromNow();
    }

    public void changePlan(Plan newPlan) {
        this.plan = newPlan;
        
        // 베이직 플랜이 아닌 경우 구독 기간 갱신
        if (!newPlan.isBasicPlan()) {
            this.period = SubscriptionPeriod.forOneMonthFromNow();
        }
    }

    public void expireToBasicPlan(Plan basicPlan) {
        if (!basicPlan.isBasicPlan()) {
            throw new IllegalArgumentException("기본 플랜이 아닙니다");
        }
        this.plan = basicPlan;
        this.period = SubscriptionPeriod.startNowIndefinitely();
    }

    public boolean isExpired() {
        return this.period.isExpired();
    }
}
