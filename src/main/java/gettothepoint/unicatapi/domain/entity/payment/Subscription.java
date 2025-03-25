package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @OneToOne
    private Member member;

    @Builder
    public Subscription(Member member) {
        this.member = member;
        this.subscriptionPlan = SubscriptionPlan.BASIC;
        this.startDate = LocalDateTime.now();
        this.endDate = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    }

    public void changePlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
    }
}