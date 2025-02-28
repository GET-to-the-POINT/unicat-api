package taeniverse.unicatApi.mvc.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.payment.TossPaymentStatus;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") String orderId);


    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    Optional<Order> findByOrderId(String orderId);
}
