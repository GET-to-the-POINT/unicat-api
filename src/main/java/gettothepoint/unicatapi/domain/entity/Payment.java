package gettothepoint.unicatapi.domain.entity;

import gettothepoint.unicatapi.domain.dto.payment.CancelPaymentResponse;
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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String paymentKey;

    @Column
    private String productName;

    @Column(nullable = false)
    private long amount;

    @Setter
    @Enumerated(EnumType.STRING)
    private PayType payType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @ManyToOne
    @JoinColumn
    private Order order;

    @ManyToOne
    @JoinColumn
    private Member member;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private LocalDateTime canceledAt;

    @Column
    private String cancelReason;

    @Builder
    public Payment(String paymentKey, String productName, long amount, PayType payType,
                   TossPaymentStatus tossPaymentStatus, Order order, Member member,
                   LocalDateTime approvedAt,LocalDateTime canceledAt, String cancelReason) {
        this.paymentKey = paymentKey;
        this.productName = productName;
        this.amount = amount;
        this.payType = payType;
        this.tossPaymentStatus = tossPaymentStatus;
        this.order = order;
        this.member = member;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
        this.cancelReason = cancelReason;
    }

    public void setCancel(CancelPaymentResponse cancelPaymentResponse) {
        this.tossPaymentStatus = cancelPaymentResponse.getTossPaymentStatus();
        this.canceledAt = cancelPaymentResponse.getCancelDate();
        this.cancelReason = cancelPaymentResponse.getCancelReason();
    }
}
