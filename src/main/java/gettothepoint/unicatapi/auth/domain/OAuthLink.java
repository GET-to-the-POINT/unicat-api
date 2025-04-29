package gettothepoint.unicatapi.auth.domain;

import gettothepoint.unicatapi.common.domain.BaseEntity;
import gettothepoint.unicatapi.member.domain.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class OAuthLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
