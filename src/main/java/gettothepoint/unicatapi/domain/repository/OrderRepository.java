package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.payment.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
