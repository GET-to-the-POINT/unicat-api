package getToThePoint.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import getToThePoint.unicatApi.domain.entity.CancelPayment;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

}
