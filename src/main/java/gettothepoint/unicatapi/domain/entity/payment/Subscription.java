package gettothepoint.unicatapi.domain.entity.payment;

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

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne
    private Plan plan;

    @OneToOne
    private Member member;

    @Builder
    public Subscription(Member member,Plan plan) {
        this.member = member;
        this.plan = (plan != null) ? plan : getDefaultPlan();
        this.startDate = LocalDateTime.now();
        this.endDate = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    }

    private Plan getDefaultPlan() {
        return Plan.builder()
                .name("BASIC")
                .description("기본 플랜")
                .price(0L)
                .build();
    }

    public void changePlan(Plan newPlan) {
        this.plan = newPlan;
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
    }
}