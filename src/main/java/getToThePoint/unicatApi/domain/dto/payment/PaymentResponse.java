package getToThePoint.unicatApi.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import getToThePoint.unicatApi.domain.constant.payment.TossPaymentStatus;


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
    private String SuccessUrl;
    private String FailUrl;
    private String createdAt;
    private TossPaymentStatus tossPaymentStatus;
    private String paymentKey;
    private String tossOrderId;
}
