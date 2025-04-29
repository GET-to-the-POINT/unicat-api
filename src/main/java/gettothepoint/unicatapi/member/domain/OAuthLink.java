package gettothepoint.unicatapi.member.domain;

import gettothepoint.unicatapi.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class OAuthLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String email;
    private String provider;

    @ManyToOne
    @JoinColumn
    private Member member;

    @Builder
    public OAuthLink(String email, String provider, Member member) {
        this.email = email;
        this.provider = provider;
        this.member = member;
    }
}
