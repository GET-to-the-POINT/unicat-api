package gettothepoint.unicatapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.payment.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByid(Long id);
    Optional<Payment> findByOrder_Id(String orderId);
    List<Payment> findByOrder_Member_Email(String email);
}