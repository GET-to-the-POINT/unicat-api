package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.entity.payment.Payment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String approvedAt;
    private Long totalAmount;
    private String receiptUrl;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentKey(payment.getPaymentKey())
                .orderId(payment.getOrder().getId()) // Order 엔티티가 있다면 해당 값을 사용합니다.
                .orderName(payment.getOrder().getOrderName())
                .status(payment.getTossPaymentStatus().toString())
                .approvedAt(payment.getApprovedAt() != null ? payment.getApprovedAt().toString() : null)
                .totalAmount(payment.getAmount())
                .build();
    }
}
