package gettothepoint.unicatapi.domain.entity;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private SubscriptionStatus status; // 구독 상태 (pending,active, cancel, expired)

    @ManyToOne
    @JoinColumn
    private Member member;

    @OneToOne
    private Order order; // 구독은 하나의 주문과 연결

    @OneToOne
    @JoinColumn
    private Payment payment;

    @Builder
    public Subscription(Member member, Order order, Payment payment) {
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
        this.status = SubscriptionStatus.ACTIVE;
        this.member = member;
        this.order = order;
        this.payment = payment;
    }
}