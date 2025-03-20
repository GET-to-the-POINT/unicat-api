package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
}
