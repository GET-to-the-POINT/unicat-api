package gettothepoint.unicatapi.payment.persistence;

import gettothepoint.unicatapi.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}