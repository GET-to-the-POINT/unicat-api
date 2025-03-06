package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class CancelPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    private String orderName;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentStatus status;

    private Long cancelAmount;

    @Column(nullable = false)
    private LocalDateTime canceledAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayType method;

    @Builder
    public CancelPayment(String orderId, String orderName, String paymentKey, String cancelReason, TossPaymentStatus status, Long cancelAmount, LocalDateTime canceledAt, Payment payment, PayType method) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.paymentKey = paymentKey;
        this.cancelReason = cancelReason;
        this.status = status;
        this.cancelAmount = cancelAmount;
        this.canceledAt = canceledAt;
        this.payment = payment;
        this.method = method;
    }
}