package taeniverse.ai_news.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.ai_news.mvc.model.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
