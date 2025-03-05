package taeniverse.unicatApi.payment;

public enum PayType {
    CARD("CARD", "카드"),
    EASY_PAY("EASY_PAY", "간편결제"),
    VIRTUAL_ACCOUNT("VIRTUAL_ACCOUNT", "가상계좌"),
    MOBILE_PHONE("MOBILE_PHONE", "휴대폰"),
    TRANSFER("TRANSFER", "계좌이체"),
    CULTURE_GIFT_CERTIFICATE("CULTURE_GIFT_CERTIFICATE", "문화상품권"),
    BOOK_GIFT_CERTIFICATE("BOOK_GIFT_CERTIFICATE", "도서문화상품권"),
    GAME_GIFT_CERTIFICATE("GAME_GIFT_CERTIFICATE", "게임문화상품권");

    private final String code;
    private final String koreanName;

    PayType(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }

    public String getKorName() {
        return koreanName;
    }

    public static PayType fromKoreanName(String koreanName) {
        for (PayType type : values()) {
            if (type.koreanName.equals(koreanName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for koreanName: " + koreanName);
    }
}
