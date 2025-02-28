package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;

import java.util.Optional;

public interface OAuth2Repository extends JpaRepository<OAuth2, Long> {
    Optional<OAuth2> findByUsername(String username);

    Optional<OAuth2> findByProviderAndEmail(String provider, String email);
}
