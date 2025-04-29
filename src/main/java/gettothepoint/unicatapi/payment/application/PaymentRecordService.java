package gettothepoint.unicatapi.payment.application;

import gettothepoint.unicatapi.payment.domain.Order;
import gettothepoint.unicatapi.payment.domain.Payment;
import gettothepoint.unicatapi.payment.persistence.PaymentRepository;
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
