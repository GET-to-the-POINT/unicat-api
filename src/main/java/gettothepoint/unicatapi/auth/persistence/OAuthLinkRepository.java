package gettothepoint.unicatapi.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.auth.domain.OAuthLink;

@Repository
public interface OAuthLinkRepository extends JpaRepository<OAuthLink, Long> {

}
