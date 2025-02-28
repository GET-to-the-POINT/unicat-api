package taeniverse.unicatApi.mvc.model.dto;

import lombok.Getter;
import lombok.Setter;
import taeniverse.unicatApi.payment.PayType;

@Getter
@Setter
public class OrderRequest {
    private String orderName;
    private Long amount;
    private String status;
    private String subscriptionId;
    private PayType payMethod;
}
