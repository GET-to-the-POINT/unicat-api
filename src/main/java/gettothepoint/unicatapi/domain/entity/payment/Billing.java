package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionPlan;
import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String billingKey;

    private String cardCompany;
    private String cardNumber;
    private String method;

    @Column(nullable = false)
    private Boolean recurring = false;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;

    private LocalDate lastPaymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Billing(Member member, String billingKey, String cardCompany, String cardNumber,
                   String method) {
        this.member = member;
        this.billingKey = billingKey;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.method = method;
        this.subscriptionPlan = SubscriptionPlan.BASIC;
    }

    @PrePersist
    public void prePersist() {
        this.lastPaymentDate = LocalDate.now();
    }

    public void updateLastPaymentDate() {
        this.lastPaymentDate = LocalDate.now();
    }

    public void cancelRecurring() {
        this.recurring = false;
    }

    public void recurring() {
        this.recurring = true;
    }
}