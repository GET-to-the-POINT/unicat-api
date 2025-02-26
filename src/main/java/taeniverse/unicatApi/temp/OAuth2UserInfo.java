package taeniverse.unicatApi.temp;

import java.util.Map;

// OAuth2 로그인 사용자 정보를 추상화하기 위한 인터페이스입니다.
public interface OAuth2UserInfo {
    // OAuth2 제공자에서 제공하는 고유 사용자 식별자
    String getId();
    // 사용자 이메일 반환
    String getEmail();
    // 사용자 이름 또는 별칭 반환
    String getName();
    // 원본 속성 전체를 반환 (필요 시 사용)
    Map<String, Object> getAttributes();
}