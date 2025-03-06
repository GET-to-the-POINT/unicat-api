package getToThePoint.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import getToThePoint.unicatApi.domain.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
