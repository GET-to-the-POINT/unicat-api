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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
    public Subscription(LocalDateTime endDate, Member member, Order order, Payment payment) {
        this.endDate = endDate;
        this.status =  SubscriptionStatus.ACTIVE;
        this.member = member;
        this.order = order;
        this.payment = payment;
        this.endDate = LocalDateTime.now().plusMonths(1);
    }
}