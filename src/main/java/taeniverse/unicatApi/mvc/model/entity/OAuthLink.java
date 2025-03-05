package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class OAuthLink {

    @Id
    private Long id;

    private String email;

    private String provider;

    @ManyToOne
    private Member member;

    @Builder
    public OAuthLink(String email, String provider, Member member) {
        this.email = email;
        this.provider = provider;
        this.member = member;
    }
}
