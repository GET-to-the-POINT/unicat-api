package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;

public interface OAuth2Repository extends JpaRepository<OAuth2, Long> {
    OAuth2 findByUsername(String username);
}
