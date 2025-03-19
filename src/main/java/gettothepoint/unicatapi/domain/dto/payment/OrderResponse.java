package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
    private Long amount;
    private String customerKey;
    private String orderId;
    private String orderName;
}