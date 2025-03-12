package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentHistoryDto {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private Long amount;
    private String status;
    private LocalDateTime approvedAt;

    public static PaymentHistoryDto fromEntity(Payment payment) {
        return PaymentHistoryDto.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrder().getId())
                .orderName(payment.getOrder().getOrderName())
                .amount(payment.getAmount())
                .status(payment.getTossPaymentStatus().toString())
                .approvedAt(payment.getApprovedAt())
                .build();
    }
}
