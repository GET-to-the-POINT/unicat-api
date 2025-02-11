package taeniverse.ai_news.mvc.model.dto;

import java.util.Map;

public class KakkoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public KakkoResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {

        Map<String, Object> kakaoAccount = (Map<String, Object>)attribute.get("kakao_account");

        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getName() {

        Map<String, Object> kakaoAccount = (Map<String, Object>)attribute.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>)kakaoAccount.get("profile");

        return kakaoProfile.get("nickname").toString();
    }
}
