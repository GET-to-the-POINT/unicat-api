package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.constant.payment.SubscriptionStatus;
import gettothepoint.unicatapi.domain.entity.member.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    Optional<Billing> findByMember(Member member);
    Optional<Billing> findByBillingKey(String billingKey);
    List<Billing> findAllByLastPaymentDateBeforeAndSubscriptionStatus(LocalDate date, SubscriptionStatus status);
    //구매 날짜와 구독상태로 찾기
}