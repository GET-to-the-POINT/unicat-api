package gettothepoint.unicatapi.domain.entity.member;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Column
    private String name;

    @Setter
    @Column
    private String phoneNumber;

    @Column(nullable = false)
    @Setter
    private String password;

    @Column(nullable = false)
    private boolean verified = false;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Order> orders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Subscription subscription;

    @OneToOne
    @Setter private Billing billing;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private final List<OAuthLink> oAuthLinks = new ArrayList<>();

    @Builder
    public Member(String email, String password ,String name, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.subscription = Subscription.builder().member(this).build();
    }

    public void verified() {
        this.verified = true;
    }
}

