package taeniverse.unicatApi.component.oauth2;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    // 원본 속성을 저장하는 맵
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        // 구글의 경우 "sub" 속성이 고유 식별자입니다.
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}