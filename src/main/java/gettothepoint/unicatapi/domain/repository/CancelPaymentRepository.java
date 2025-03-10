package gettothepoint.unicatapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import gettothepoint.unicatapi.domain.entity.CancelPayment;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

}
