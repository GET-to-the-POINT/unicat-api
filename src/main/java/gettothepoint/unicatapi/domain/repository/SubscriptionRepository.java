package gettothepoint.unicatapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
}
