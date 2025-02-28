package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.*;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Builder
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;

    @Version
    private Long version;

    @Column(name = "order_name")
    private String orderName;

    private Long amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "total_price")
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private PayType payMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TossPaymentStatus status; // 주문 상태

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false,unique = true)
    private Subscription subscription; // 주문이 특정 구독에 연결

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @PrePersist
    private void prePersist() {
        if (this.orderId == null) {
            this.orderId = UUID.randomUUID().toString();
        }
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
