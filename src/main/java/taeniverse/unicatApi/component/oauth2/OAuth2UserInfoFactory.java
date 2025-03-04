package taeniverse.unicatApi.component.oauth2;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        registrationId = registrationId.toLowerCase();

        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);

            case "kakao" -> new KakaoOAuth2UserInfo(attributes);

//            case "naver" -> new NaverOAuth2UserInfo((Map<String, Object>) attributes.get("response"));

//            case "gitHub" -> new GithubOAuth2UserInfo(attributes);

            default ->
                    throw new IllegalArgumentException("Sorry! Login with " + registrationId + " is not supported yet.");
        };
    }
}