package gettothepoint.unicatapi.domain.dto.payment;

import lombok.Builder;

@Builder
public record PaymentApprovalRequest(
         Long amount,
         String customerKey,
         String orderId,
         String orderName
) {}