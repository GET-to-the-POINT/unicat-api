package getToThePoint.unicatApi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import getToThePoint.unicatApi.domain.constant.payment.PayType;
import getToThePoint.unicatApi.domain.constant.payment.TossPaymentStatus;

import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "purchase")
public class Order {
    @Id
    @Column(updatable = false, nullable = false)
    private final String Id = UUID.randomUUID().toString();

    @Column
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
    private TossPaymentStatus status; // 주문 상태

    @OneToOne
    private Subscription subscription; // 주문이 특정 구독에 연결

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
}
