package gettothepoint.unicatapi.common.security.oauth2.client.user;

import java.util.Map;
import java.util.UUID;

public interface OAuth2UserInfo {
    String getId();
    String getEmail();
    String getPicture();
    String getName();
    UUID getMemberId();
    void setMemberId(UUID memberId);

    Map<String, Object> getAttributes();

    static OAuth2UserInfo from(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}