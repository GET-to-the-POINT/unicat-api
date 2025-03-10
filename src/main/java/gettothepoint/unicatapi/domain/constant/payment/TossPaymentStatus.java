package gettothepoint.unicatapi.domain.constant.payment;

public enum TossPaymentStatus {
    PENDING,          // 가주문 상태 (내부 로직에서 설정)
    READY,            // Toss API 결제 생성됨
    IN_PROGRESS,      // 인증 완료됨
    WAITING_FOR_DEPOSIT, // 가상계좌 입금 대기 중
    DONE,          // 결제 승인 완료 (Toss API의 DONE)
    CANCELED,         // 결제 취소됨
    PARTIAL_CANCELED, // 부분 취소됨
    FAILED,           // 승인 실패 (Toss API의 ABORTED)
    EXPIRED;          // 유효시간 초과로 취소됨

    public static TossPaymentStatus fromTossStatus(String tossStatus) {
        return switch (tossStatus) {
            case "READY" -> READY;
            case "IN_PROGRESS" -> IN_PROGRESS;
            case "WAITING_FOR_DEPOSIT" -> WAITING_FOR_DEPOSIT;
            case "DONE" -> DONE;
            case "CANCELED" -> CANCELED;
            case "PARTIAL_CANCELED" -> PARTIAL_CANCELED;
            case "ABORTED" -> FAILED;
            case "EXPIRED" -> EXPIRED;
            default ->
                    throw new IllegalArgumentException("Unknown Toss Payment Status: " + tossStatus);
        };
    }
}
