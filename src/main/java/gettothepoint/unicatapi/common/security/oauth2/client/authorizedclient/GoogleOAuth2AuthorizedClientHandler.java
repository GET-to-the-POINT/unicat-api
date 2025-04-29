package gettothepoint.unicatapi.common.security.oauth2.client.authorizedclient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class GoogleOAuth2AuthorizedClientHandler implements VendorOAuth2AuthorizedClientHandler {
    @Override
    public OAuth2AuthorizedClient handle(OAuth2AuthorizedClient newClient, OAuth2AuthorizedClient existingClient) {
        if (existingClient != null
                && newClient.getRefreshToken() == null
                && existingClient.getRefreshToken() != null) {
            // 기존의 refresh token(발급 시간 등 메타데이터 포함)을 그대로 사용
            return new OAuth2AuthorizedClient(
                    newClient.getClientRegistration(),
                    newClient.getPrincipalName(),
                    newClient.getAccessToken(),
                    existingClient.getRefreshToken()
            );
        }
        return newClient;
    }
}