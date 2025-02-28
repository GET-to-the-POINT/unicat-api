package taeniverse.unicatApi.mvc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taeniverse.unicatApi.payment.TossPaymentStatus;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String payType;
    private Long amount;
    private String orderId;
    private Long memberId;
    private String SuccessUrl;
    private String FailUrl;
    private String createdAt;
    private TossPaymentStatus tossPaymentStatus;
    private String paymentKey;
    private String tossOrderId;
}
