package getToThePoint.unicatApi.domain.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentResponse {
    private String orderId;
    private String orderName;
    private Long totalAmount;
    private String paymentKey;
    private String method;
    private String status;
}