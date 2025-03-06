package getToThePoint.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import getToThePoint.unicatApi.domain.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
}
