package gettothepoint.unicatapi.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Member 엔티티의 기본키 자동 생성
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String customerKey;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL )
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private List<OAuthLink> oAuthLinks = new ArrayList<>();

    private Instant createAt;
    private Instant updateAt;

    @Builder
    public Member(String email, String password) {
        this.email = email;
        this.password = password;
        this.customerKey = UUID.randomUUID().toString();
    }
    @PrePersist
    protected void onCreate() {
        this.createAt = Instant.now();
        this.updateAt = Instant.now();
        this.customerKey = UUID.randomUUID().toString();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updateAt = Instant.now();
    }
}