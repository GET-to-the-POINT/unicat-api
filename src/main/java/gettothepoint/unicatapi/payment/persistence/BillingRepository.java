package gettothepoint.unicatapi.payment.persistence;

import gettothepoint.unicatapi.member.domain.Member;
import gettothepoint.unicatapi.payment.domain.Billing;
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
            AND s.endDate BETWEEN :startOfDay AND :endOfDay
            """)
    List<Billing> findNonRecurringMembersWithExpiredSubscription(
            @Param("startOfDay") java.time.LocalDateTime startOfDay,
            @Param("endOfDay") java.time.LocalDateTime endOfDay
    );
}
