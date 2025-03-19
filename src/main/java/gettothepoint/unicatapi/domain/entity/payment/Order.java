package gettothepoint.unicatapi.domain.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gettothepoint.unicatapi.domain.constant.payment.PayType;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "purchase")
public class Order extends BaseEntity {
    @Id
    @Column(updatable = false, nullable = false)
    private  String id = UUID.randomUUID().toString();

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
    private TossPaymentStatus status;

    @OneToOne
    private Subscription subscription;

    @OneToOne(mappedBy = "order")
    @JsonIgnore
    private Payment payment;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Billing> billingList = new ArrayList<>();

    @Builder
    public Order(String id,String orderName, Long amount, Member member, PayType payMethod, TossPaymentStatus status, Subscription subscription) {
        this.id = id;
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

    public void markDone() {
        if (this.status != TossPaymentStatus.PENDING) {
            throw new IllegalStateException("이미 완료된 주문입니다.");
        }
        this.status = TossPaymentStatus.DONE;
    }
}
