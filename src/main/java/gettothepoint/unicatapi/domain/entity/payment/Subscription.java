package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan subscriptionPlan;

    private Boolean active = false;

    @OneToOne
    private Member member;

    @OneToMany
    private List<Order> order;

    @Builder
    public Subscription(Member member, SubscriptionPlan subscriptionPlan) {
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
        this.member = member;
        this.subscriptionPlan = subscriptionPlan;
        this.active = true;
    }

    public void setActive() {
        this.active = true;
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
    }

    public void setInactive() {
        this.active = false;
    }
}