package gettothepoint.unicatapi.domain.entity.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.subscription.domain.entity.Plan;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    private String status;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private Member member;

    @OneToOne(mappedBy = "order")
    @JsonIgnore
    private Payment payment;

    @Builder
    public Order(String orderName, Long amount, Member member, String status, Plan plan) {
        this.orderName = orderName;
        this.amount = amount;
        this.member = member;
        this.status = status;
        this.plan = plan;
    }

    public void markDone() {
        this.status = "DONE";
    }

    public boolean isPending() {
        return Objects.equals(this.status, "PENDING");
    }

    @Override
    public int compareTo(Order other) {
        return other.getCreatedAt().compareTo(this.getCreatedAt());
    }
}
