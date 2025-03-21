package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}