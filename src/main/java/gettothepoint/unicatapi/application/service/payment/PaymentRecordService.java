package gettothepoint.unicatapi.application.service.payment;

import gettothepoint.unicatapi.order.entity.Order;
import gettothepoint.unicatapi.domain.entity.payment.Payment;
import gettothepoint.unicatapi.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentRecordService {

    private final PaymentRepository paymentRepository;


    public void save(Order order, Map<String, Object> response) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentKey((String) response.get("paymentKey"))
                .method((String) response.get("method"))
                .amount(((Number) response.get("totalAmount")).longValue())
                .productName((String) response.get("orderName"))
                .approvedAt(OffsetDateTime.parse((String) response.get("approvedAt")).toLocalDateTime())
                .build();

        paymentRepository.save(payment);
    }
}
