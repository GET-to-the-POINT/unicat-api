package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.domain.entity.payment.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    Optional<Billing> findByMember(Member member);

    List<Billing> findAllByLastPaymentDateBeforeAndRecurring(LocalDate date, Boolean recurring);

    @Query("""
            SELECT b FROM Billing b
            JOIN FETCH b.member m
            JOIN FETCH m.subscription s
            WHERE b.recurring = false
            AND FUNCTION('DATE', s.endDate) = :expiredDate
            """)
    List<Billing> findNonRecurringMembersWithExpiredSubscription(@Param("expiredDate") LocalDate expiredDate);
}
