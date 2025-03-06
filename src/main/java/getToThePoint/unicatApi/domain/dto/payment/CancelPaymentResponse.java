package getToThePoint.unicatApi.domain.dto.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import getToThePoint.unicatApi.domain.constant.payment.PayType;
import getToThePoint.unicatApi.domain.constant.payment.TossPaymentStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CancelPaymentResponse {
    private String orderId;        // 주문 ID
    private String orderName;      // 주문명
    private String paymentKey;     // 결제 키
    private OffsetDateTime requestedAt; // 요청 시간
    private OffsetDateTime approvedAt;  // 승인 시간
    private String cardCompany;    // 카드사 정보
    private String cardNumber;     // 카드 번호
    private String receiptUrl;     // 영수증 URL
    private Long cancelAmount;     // 취소 금액
    private LocalDateTime cancelDate; // 취소 완료 시간
    private String cancelReason;   // 취소 사유

    @JsonProperty("status")
    private String status; // Toss API 응답의 status 값 (READY, IN_PROGRESS, DONE, CANCELED 등)

    @JsonProperty("method")
    private String method;

    @JsonCreator
    public CancelPaymentResponse(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderName") String orderName,
            @JsonProperty("paymentKey") String paymentKey,
            @JsonProperty("requestedAt") OffsetDateTime requestedAt,
            @JsonProperty("approvedAt") OffsetDateTime approvedAt,
            @JsonProperty("cancelAmount") Long cancelAmount,
            @JsonProperty("cancels") List<CancelInfo> cancels,
            @JsonProperty("cancelReason") String cancelReason,
            @JsonProperty("status") String status // Toss API 응답에서 status 값 매핑
    ) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.paymentKey = paymentKey;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.cancelAmount = cancelAmount;
        if (cancels != null && !cancels.isEmpty() && cancels.getFirst().getCanceledAt() != null) {
            this.cancelDate = cancels.getFirst().getCanceledAt().toLocalDateTime();
        }
        this.cancelReason = cancelReason;
        this.status = status;
    }

    // Toss API의 status 값을 TossPaymentStatus Enum으로 변환하는 메서드
    public TossPaymentStatus getTossPaymentStatus() {
        return TossPaymentStatus.fromTossStatus(this.status);
    }
    public PayType getPayType() {
        return PayType.fromKoreanName(this.method);
    }
}


