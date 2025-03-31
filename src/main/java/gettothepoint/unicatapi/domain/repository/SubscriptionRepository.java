package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
}
