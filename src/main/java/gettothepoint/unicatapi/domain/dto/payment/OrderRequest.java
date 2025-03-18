package gettothepoint.unicatapi.domain.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import gettothepoint.unicatapi.domain.constant.payment.PayType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    // 구독 플랜 이름, 일반 프로
    private String orderName;

    // 결제 금액
    private Long amount;

    // 결제 상태
    private String status;

    private Long memberId;

    private String subscriptionId;

    private PayType payMethod;

    public OrderRequest(String orderName, Long amount) {
        this.orderName = orderName;
        this.amount = amount;
    }
}
