package gettothepoint.unicatapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import gettothepoint.unicatapi.domain.entity.payment.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
