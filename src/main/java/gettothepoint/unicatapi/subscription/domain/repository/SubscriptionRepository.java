package gettothepoint.unicatapi.subscription.domain.repository;

import gettothepoint.unicatapi.subscription.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
}
