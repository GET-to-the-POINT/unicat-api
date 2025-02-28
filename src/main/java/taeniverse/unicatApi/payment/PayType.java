package taeniverse.unicatApi.payment;

public enum PayType {
    CARD("카드"),
    EASY_PAY("간편결제"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰"),
    TRANSFER("계좌이체"),
    CULTURE_GIFT_CERTIFICATE("문화상품권"),
    BOOK_GIFT_CERTIFICATE("도서문화상품권"),
    GAME_GIFT_CERTIFICATE("게임문화상품권");

    private final String korName;

    PayType(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }

    public static PayType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid payType: " + value);
        }
        String trimmed = value.trim();
        for (PayType type : PayType.values()) {
            if (type.korName.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid payType: " + value);
    }
}