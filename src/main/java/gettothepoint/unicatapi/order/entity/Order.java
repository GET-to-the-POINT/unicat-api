package gettothepoint.unicatapi.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gettothepoint.unicatapi.common.domain.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Payment;
import gettothepoint.unicatapi.subscription.entity.Plan;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "purchase")
public class Order extends BaseEntity implements Comparable<Order> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orderName;
    private Long amount;

    @ManyToOne
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private Member member;

    @OneToOne(mappedBy = "order")
    @JsonIgnore
    private Payment payment;

    public static Order createOrder(String orderName, Long amount, Member member, Plan plan) {
        return Order.builder()
                .orderName(orderName)
                .amount(amount)
                .member(member)
                .status(OrderStatus.PENDING)
                .plan(plan)
                .build();
    }

    @Builder
    private Order(String orderName, Long amount, Member member, OrderStatus status, Plan plan) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
        if (member == null) {
            throw new IllegalArgumentException("구매자 정보는 필수입니다");
        }

        this.orderName = orderName;
        this.amount = amount;
        this.member = member;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.plan = plan;
    }

    public void markDone() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 주문만 완료로 변경할 수 있습니다");
        }
        this.status = OrderStatus.DONE;
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    @Override
    public int compareTo(Order other) {
        return other.getCreatedAt().compareTo(this.getCreatedAt());
    }
}