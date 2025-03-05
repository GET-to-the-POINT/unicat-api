package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.CancelPayment;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

}
