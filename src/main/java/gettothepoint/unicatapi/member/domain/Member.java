package gettothepoint.unicatapi.member.domain;

import gettothepoint.unicatapi.auth.domain.OAuthLink;
import gettothepoint.unicatapi.common.domain.BaseEntity;
import gettothepoint.unicatapi.payment.domain.Billing;
import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.subscription.domain.Subscription;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Setter
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
    }

    public void verified() {
        this.verified = true;
    }
}

