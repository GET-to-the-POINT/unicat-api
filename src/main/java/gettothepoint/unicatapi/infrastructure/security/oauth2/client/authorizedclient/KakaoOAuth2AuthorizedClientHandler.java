package gettothepoint.unicatapi.infrastructure.security.oauth2.client.authorizedclient;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

public class KakaoOAuth2AuthorizedClientHandler implements VendorOAuth2AuthorizedClientHandler {
    @Override
    public OAuth2AuthorizedClient handle(OAuth2AuthorizedClient newClient, OAuth2AuthorizedClient existingClient) {
        return newClient;
    }
}