package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {
    // 구독 플랜 이름, 일반 프로
    private String orderName;

    // 결제 금액
    private Long amount;

    public OrderRequest(String orderName, Long amount) {
        this.orderName = orderName;
        this.amount = amount;
    }
}
