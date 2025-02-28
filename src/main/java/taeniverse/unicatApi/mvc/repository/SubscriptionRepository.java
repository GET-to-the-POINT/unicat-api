package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Subscription;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findByMember(Member member);
}
