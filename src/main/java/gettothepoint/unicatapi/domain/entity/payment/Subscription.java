package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private SubscriptionStatus status; // 구독 상태 (pending, active, cancel, expired)

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToOne
    private Order order;

    @Builder
    public Subscription(Member member, Order order) {
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
        this.status = SubscriptionStatus.ACTIVE;
        this.member = member;
        this.order = order;
    }
}