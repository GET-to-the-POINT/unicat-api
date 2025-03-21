package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {

    private String orderName;

    private Long amount;

    public OrderRequest(String orderName, Long amount) {
        this.orderName = orderName;
        this.amount = amount;
    }
}