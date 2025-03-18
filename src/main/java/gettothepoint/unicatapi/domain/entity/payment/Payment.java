package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String paymentKey;
    private String productName;

    @Column(nullable = false)
    private Long amount;

    @Setter
    @Enumerated(EnumType.STRING)
    private PayType payType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @OneToOne
    @JoinColumn
    private Order order;

    @ManyToOne
    @JoinColumn
    private Billing billing;

    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;
    private String cancelReason;

    @Builder
    public Payment(Order order, String paymentKey, Long amount, TossPaymentStatus tossPaymentStatus,
                   PayType payType, String productName, LocalDateTime approvedAt) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.tossPaymentStatus = tossPaymentStatus;
        this.payType = payType;
        this.productName = productName;
        this.approvedAt = approvedAt;
    }

    public void setCancel(CancelPaymentResponse cancelPaymentResponse) {
        this.tossPaymentStatus = cancelPaymentResponse.getTossPaymentStatus();
        this.canceledAt = cancelPaymentResponse.getCancelDate();
        this.cancelReason = cancelPaymentResponse.getCancelReason();
    }
}
