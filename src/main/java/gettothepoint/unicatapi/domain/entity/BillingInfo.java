package gettothepoint.unicatapi.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table
public class BillingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String billingKey;

    @Column
    private String cardType;

    @Column
    private String cardLastFourDigits; // 카드 마지막 4자리

    public BillingInfo(String billingKey, String cardType, String cardLastFourDigits) {
        this.billingKey = billingKey;
        this.cardType = cardType;
        this.cardLastFourDigits = cardLastFourDigits;
    }
}