package taeniverse.unicatApi.temp;

import java.util.Map;

// OAuth2 제공자별 OAuth2UserInfo 구현체를 생성하는 팩토리 클래스입니다.
public class OAuth2UserInfoFactory {

    // 제공자와 원본 속성을 받아서 적절한 OAuth2UserInfo 객체를 반환합니다.
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        // registrationId를 소문자로 통일하여 비교합니다.
        registrationId = registrationId.toLowerCase();

        // 구글인 경우
        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);

            // 카카오인 경우 (구현 예시)
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);

            // 네이버인 경우 (구현 예시)
//            case "naver" -> new NaverOAuth2UserInfo((Map<String, Object>) attributes.get("response"));

            // 깃허브인 경우 (구현 예시)
//            case "github" -> new GithubOAuth2UserInfo(attributes);

            // 지원하지 않는 제공자인 경우 예외 발생
            default ->
                    throw new IllegalArgumentException("Sorry! Login with " + registrationId + " is not supported yet.");
        };
    }
}