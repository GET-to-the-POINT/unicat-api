package taeniverse.unicatApi.mvc.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.Order;
import taeniverse.unicatApi.mvc.model.entity.Payment;
import taeniverse.unicatApi.payment.PayType;
import taeniverse.unicatApi.payment.TossPaymentStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class PaymentRequest {

    @NotNull(message = "payType은 필수 값입니다.")
    private PayType payType; //지불 방법
    private Long amount; //지불 금액
    private String username; //구매자 이름
    private String orderName; //주문명 (구독명)
    private String orderId;
    private String paymentKey;



    public Payment toEntity(Member member, Order order) {
        return Payment.builder()
                .order(order)
                .payType(payType)
                .amount(amount)
                .member(member)
                .tossPaymentStatus(TossPaymentStatus.SUCCESS)
                .build();
    }
}
