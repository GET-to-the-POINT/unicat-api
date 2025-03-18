package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Getter;

@Getter
public class PaymentApprovalRequest {
    private Long amount;
    private String customerKey;
    private String orderId;
    private String orderName;

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }
}
