package gettothepoint.unicatapi.payment.domain.dto;

import lombok.Builder;

/**
 * 토스용 결제 승인 요청 DTO
 * @param amount 결제 금액
 * @param customerKey 고객 키(이 시스템에서는 이메일)
 * @param orderId 주문 ID(UUID)
 * @param orderName 주문 이름
 */
@Builder
public record TossApprovalRequest(
         Long amount,
         String customerKey,
         String orderId,
         String orderName
) {}