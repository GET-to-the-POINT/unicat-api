package gettothepoint.unicatapi.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import gettothepoint.unicatapi.domain.constant.payment.TossPaymentStatus;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String payType;
    private Long amount;
    private String orderId;
    private String orderName;
    private Long memberId;
    private String successUrl;
    private String failUrl;
    private String createdAt;
    private TossPaymentStatus tossPaymentStatus;
    private String paymentKey;
    private String tossOrderId;
}
