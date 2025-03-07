package gettothepoint.unicatapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.OAuthLink;

@Repository
public interface OAuthLinkRepository extends JpaRepository<OAuthLink, Long> {

}
