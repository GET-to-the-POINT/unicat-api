package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

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

    @OneToOne
    @JoinColumn
    private Order order;

    @ManyToOne
    @JoinColumn
    private Member member;

    @Builder
    public Payment(String paymentKey, String productName, long amount, PayType payType, TossPaymentStatus tossPaymentStatus, Order order, Member member) {
        this.paymentKey = paymentKey;
        this.productName = productName;
        this.amount = amount;
        this.payType = payType;
        this.tossPaymentStatus = tossPaymentStatus;
        this.order = order;
        this.member = member;
    }
}
