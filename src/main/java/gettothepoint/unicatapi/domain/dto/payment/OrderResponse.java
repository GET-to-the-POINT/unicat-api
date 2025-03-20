package gettothepoint.unicatapi.domain.dto.payment;

import gettothepoint.unicatapi.domain.entity.payment.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
    private Long amount;
    private String customerKey;
    private String orderId;
    private String orderName;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .amount(order.getAmount())
                .customerKey(order.getMember().getCustomerKey())
                .orderId(order.getId())
                .orderName(order.getOrderName())
                .build();
    }
}