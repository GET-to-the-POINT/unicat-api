package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.*;
import taeniverse.unicatApi.mvc.model.dto.PaymentResponse;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PayType payType;

    @Column(nullable = false, updatable = false)
    private String paymentKey;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "order_name")
    private String orderName;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "member_id")
    private Member member;


    @Column(name = "created_at", updatable = false )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        // 엔티티가 처음 저장되기 전에 createdAt 값을 현재 시간으로 설정
        this.createdAt = LocalDateTime.now();
    }


    public PaymentResponse toDto() {
        return PaymentResponse.builder()
                .payType(payType.name())
                .paymentKey(paymentKey)
                .orderId(order != null ? order.getOrderId() : null)
                .orderName(order != null ? order.getOrderName() : null)
                .amount(amount)
                .tossPaymentStatus(tossPaymentStatus)
                .memberId(member != null ? member.getId() : null)
                .createdAt(createdAt != null ? createdAt.toString() : null)
                .build();
    }
}
