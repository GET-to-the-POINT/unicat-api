package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;
import gettothepoint.unicatapi.domain.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findFirstByMember_EmailAndStatusOrderByCreatedAtDesc(String email, TossPaymentStatus status);
}
