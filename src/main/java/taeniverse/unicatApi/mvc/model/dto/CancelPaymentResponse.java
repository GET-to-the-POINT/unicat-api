package taeniverse.unicatApi.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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

    @JsonCreator
    public CancelPaymentResponse(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("orderName") String orderName,
            @JsonProperty("paymentKey") String paymentKey,
            @JsonProperty("requestedAt") OffsetDateTime requestedAt,
            @JsonProperty("approvedAt") OffsetDateTime approvedAt,
            @JsonProperty("cancelAmount") Long cancelAmount,
            @JsonProperty("cancels") List<CancelInfo> cancels,
            @JsonProperty("cancelReason") String cancelReason
    ) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.paymentKey = paymentKey;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.cancelAmount = cancelAmount;
        if (cancels != null && !cancels.isEmpty() && cancels.get(0).getCanceledAt() != null) {
            this.cancelDate = cancels.get(0).getCanceledAt().toLocalDateTime();
        }
        this.cancelReason = cancelReason;
    }
}

