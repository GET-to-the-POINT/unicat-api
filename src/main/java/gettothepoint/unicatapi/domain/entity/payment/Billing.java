package gettothepoint.unicatapi.domain.entity.payment;

import gettothepoint.unicatapi.domain.entity.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, unique = true)
    private String billingKey;

    private String cardCompany;


    private String cardNumber;

    private String method;

    private LocalDateTime lastPaymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; //멤버와 연관 관계 (N:1)

    @Builder
    public Billing(Member member, String billingKey, String cardCompany, String cardNumber
            , String method,Long amount,LocalDateTime lastPaymentDate) {
        this.member = member;
        this.billingKey = billingKey;
        this.cardCompany = cardCompany;
        this.cardNumber = cardNumber;
        this.method = method;
        this.amount = amount;
        this.lastPaymentDate = lastPaymentDate;
    }
    public void updateLastPaymentDate(LocalDateTime lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
    public void updateAmount(Long newAmount) {
        this.amount = newAmount;
    }
}
