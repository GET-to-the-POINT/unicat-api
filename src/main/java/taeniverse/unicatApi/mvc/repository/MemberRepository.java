package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);
}
