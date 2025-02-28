package taeniverse.unicatApi.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentResponse {
    private String orderId;        // 주문 ID (프론트에서 전달받음)
    private String orderName;      // 주문명
    private Long totalAmount;      // 총 결제 금액

    @JsonProperty("method")
    private String method;         // 결제 수단 (카드, 간편결제 등)

    private Long memberId;           // 유저 ID (
    private Long subscriptionId;   // 구독 ID

    // 기본 생성자
    public TossPaymentResponse() {
    }

    // 모든 필드를 포함한 생성자
    public TossPaymentResponse(String orderId, String orderName, Long totalAmount, String method, Long memberId) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.totalAmount = totalAmount;
        this.method = method;
        this.memberId = memberId;
    }
}