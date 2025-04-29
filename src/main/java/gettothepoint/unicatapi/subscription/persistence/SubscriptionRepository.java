package gettothepoint.unicatapi.subscription.persistence;

import gettothepoint.unicatapi.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 구독 정보 관련 데이터 액세스를 위한 리포지토리
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
