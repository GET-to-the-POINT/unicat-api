package taeniverse.unicatApi.temp;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    // 원본 속성을 저장하는 맵
    private final Map<String, Object> attributes;

    // 생성자: 전달받은 attributes를 초기화합니다.
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 구글에서 제공하는 고유 식별자 추출
    @Override
    public String getId() {
        // 구글의 경우 "sub" 속성이 고유 식별자입니다.
        return (String) attributes.get("sub");
    }

    // 구글에서 제공하는 이메일 추출
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    // 구글에서 제공하는 이름 추출
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}