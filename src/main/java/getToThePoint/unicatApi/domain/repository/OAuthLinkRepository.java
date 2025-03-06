package getToThePoint.unicatApi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import getToThePoint.unicatApi.domain.entity.OAuthLink;

@Repository
public interface OAuthLinkRepository extends JpaRepository<OAuthLink, Long> {

}
