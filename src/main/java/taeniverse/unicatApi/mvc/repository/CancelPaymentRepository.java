package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.CancelPayment;

import java.util.Optional;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

}
