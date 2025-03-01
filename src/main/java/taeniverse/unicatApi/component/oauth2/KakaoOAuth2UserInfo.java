package taeniverse.unicatApi.component.oauth2;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 카카오의 경우 고유 식별자는 최상위 키 "id"에 있습니다.
    @Override
    public String getId() {
        // 숫자형일 수 있으므로 String.valueOf를 사용해 문자열로 변환합니다.
        return String.valueOf(attributes.get("id"));
    }

    // 카카오 계정 내에서 이메일 정보는 "kakao_account" 하위 객체에 있습니다.
    @Override
    public String getEmail() {
        // "kakao_account" 객체를 가져옵니다.
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        // kakaoAccount가 null이 아니면 "email" 키의 값을 반환합니다.
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    // 카카오에서 이름은 보통 "kakao_account" 내 "profile" 객체의 "nickname" 속성에 있습니다.
    @Override
    public String getName() {
        // "kakao_account" 객체를 가져옵니다.
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            // "profile" 객체를 kakaoAccount에서 추출합니다.
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            // profile이 null이 아니라면 "nickname" 값을 반환합니다.
            return profile != null ? (String) profile.get("nickname") : null;
        }
        return null;
    }
}