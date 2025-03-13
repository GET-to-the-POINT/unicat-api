package gettothepoint.unicatapi.domain.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "purchase")
public class Order extends BaseEntity {
    @Id
    @Column(updatable = false, nullable = false)
    private final String id = UUID.randomUUID().toString();

    private String orderName;
    private Long amount;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private Member member;

    @Enumerated(EnumType.STRING)
    private PayType payMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private TossPaymentStatus status;

    @OneToOne
    private Subscription subscription;

    @OneToOne(mappedBy = "order")
    @JsonIgnore
    private Payment payment;

    @Builder
    public Order(String orderName, Long amount, Member member, PayType payMethod, TossPaymentStatus status, Subscription subscription) {
        this.orderName = orderName;
        this.amount = amount;
        this.member = member;
        this.payMethod = payMethod;
        this.status = status;
        this.subscription = subscription;
    }

    public void cancelOrder() {
        this.status = TossPaymentStatus.CANCELED;
    }
}
