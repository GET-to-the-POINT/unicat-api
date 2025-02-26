package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class OAuth2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String provider;

    @Column(unique = true, nullable = false)
    private String username;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Setter
    private String email;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Builder
    public OAuth2(String provider, String username, Member member, String email) {
        this.provider = provider;
        this.username = username;
        this.member = member;
        this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}