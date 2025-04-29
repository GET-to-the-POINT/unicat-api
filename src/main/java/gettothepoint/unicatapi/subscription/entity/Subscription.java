package gettothepoint.unicatapi.subscription.entity;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder
    public Subscription(Member member, Plan plan) {
        this.member = member;
        this.plan = plan;

        LocalDateTime now = LocalDateTime.now();
        this.startDate = now;
        this.endDate = plan.isBasicPlan()
                ? LocalDateTime.of(9999, 12, 31, 23, 59, 59)
                : now.plusMonths(1);
    }

    public void changePlan(Plan newPlan) {
        this.plan = newPlan;

        this.startDate = LocalDateTime.now();
        this.endDate = newPlan.isBasicPlan()
                ? LocalDateTime.of(9999, 12, 31, 23, 59, 59)
                : LocalDateTime.now().plusMonths(1);
    }

    public boolean isExpired() {
        return this.endDate.isBefore(LocalDateTime.now());
    }
}
