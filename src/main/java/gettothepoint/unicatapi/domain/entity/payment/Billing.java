package gettothepoint.unicatapi.domain.entity.payment;

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

    @Column(nullable = false)
    private Boolean recurring = false;

    private LocalDate lastPaymentDate;

    @OneToOne
    private Member member;

    @Builder
    public Billing(Member member, String billingKey) {
        this.member = member;
        this.billingKey = billingKey;
    }

    @PrePersist
    public void prePersist() {
        this.lastPaymentDate = LocalDate.now();
    }

    public void cancelRecurring() {
        this.recurring = false;
    }

    public void recurring() {
        this.recurring = true;
        this.lastPaymentDate = LocalDate.now();
    }
}