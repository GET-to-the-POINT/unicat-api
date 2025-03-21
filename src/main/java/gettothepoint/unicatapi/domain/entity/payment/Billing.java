package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.constant.payment.MembershipTier;
import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
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

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Enumerated(EnumType.STRING)
    private MembershipTier membershipTier;

    private LocalDate lastPaymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @PrePersist
    public void prePersist() {
        if (subscriptionStatus == null) {
            subscriptionStatus = SubscriptionStatus.PENDING;
        }
    }

    @Builder
    public Billing(Member member, String billingKey, String cardCompany, String cardNumber,
                   String method, LocalDate lastPaymentDate,MembershipTier membershipTier) {
        this.member = member;
        this.billingKey = billingKey;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.method = method;
        this.lastPaymentDate = lastPaymentDate;
        this.membershipTier = MembershipTier.BASIC;
    }

    public void updateLastPaymentDate(LocalDate newDate) {
        this.lastPaymentDate = newDate;
    }

    public void cancelSubscription() {
        this.subscriptionStatus = SubscriptionStatus.CANCELLED;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}