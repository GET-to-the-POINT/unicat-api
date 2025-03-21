package gettothepoint.unicatapi.domain.repository;

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
    List<Billing> findAllByLastPaymentDateBeforeAndRecurring(LocalDate date, Boolean recurring);

}