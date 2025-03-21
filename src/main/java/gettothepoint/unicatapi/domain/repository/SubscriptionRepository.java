package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
    Optional<Subscription> findTopByMemberOrderByCreatedAtDesc(Member member);
}
