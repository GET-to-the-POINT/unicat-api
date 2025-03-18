package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.entity.payment.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentHistoryResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long amount;
    private String status;
    private LocalDateTime approvedAt;

    public static PaymentHistoryResponse fromEntity(Payment payment) {
        return PaymentHistoryResponse.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrder().getId())
                .orderName(payment.getOrder().getOrderName())
                .amount(payment.getAmount())
                .status(payment.getTossPaymentStatus().toString())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
