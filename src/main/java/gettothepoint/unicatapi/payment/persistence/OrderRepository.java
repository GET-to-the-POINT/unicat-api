package gettothepoint.unicatapi.payment.persistence;

import gettothepoint.unicatapi.payment.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
