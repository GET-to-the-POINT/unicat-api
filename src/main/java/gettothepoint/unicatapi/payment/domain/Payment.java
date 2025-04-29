package gettothepoint.unicatapi.payment.domain;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String method;

    @OneToOne
    @JoinColumn
    private Order order;

    private LocalDateTime approvedAt;

    @Builder
    public Payment(Order order, String paymentKey, Long amount,
                   String method, String productName, LocalDateTime approvedAt) {
        this.order = order;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.method = method;
        this.productName = productName;
        this.approvedAt = approvedAt;
    }
}
