package taeniverse.unicatApi.mvc.model.entity;

import jakarta.persistence.*;
import lombok.*;
import taeniverse.unicatApi.mvc.model.dto.CancelPaymentResponse;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "cancel_payment")
public class CancelPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    private String orderName;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false, length = 200)
    private String cancelReason;

    private Long cancelAmount;

    @Column(name = "cancel_date", nullable = false)
    private LocalDateTime cancelDate;
    // Payment 엔티티와 다대일 관계 설정 (Payment 엔티티 내에서 취소 내역 배열로 관리할 수 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    public CancelPaymentResponse toDto() {
        return CancelPaymentResponse.builder()
                .orderId(orderId)
                .orderName(orderName)
                .paymentKey(paymentKey)
                .cancelReason(cancelReason)
                .cancelAmount(cancelAmount)
                .cancelDate(cancelDate)
                .build();
    }

    public static CancelPayment fromDto(CancelPaymentResponse dto) {
        return CancelPayment.builder()
                .orderId(dto.getOrderId())
                .orderName(dto.getOrderName())
                .paymentKey(dto.getPaymentKey())
                .cancelReason(dto.getCancelReason())
                .cancelAmount(dto.getCancelAmount())
                .cancelDate(dto.getCancelDate())
                .build();
    }
}