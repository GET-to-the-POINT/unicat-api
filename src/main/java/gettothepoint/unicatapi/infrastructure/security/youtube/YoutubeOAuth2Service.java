package gettothepoint.unicatapi.infrastructure.security.youtube;

import gettothepoint.unicatapi.domain.repository.MemberRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

@Service
public class YoutubeOAuth2Service {

    private static final String APPLICATION_NAME = "YouTube API Client";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MemberRepository memberRepository;

    public YoutubeOAuth2Service(OAuth2AuthorizedClientService authorizedClientService, MemberRepository memberRepository) {
        this.authorizedClientService = authorizedClientService;
        this.memberRepository = memberRepository;
    }

    /**
     * Google API 인증을 위한 Credential 객체 생성
     *
     * @param accessToken 유효한 액세스 토큰
     * @return Google API 인증을 위한 Credential 객체
     */
    public HttpRequestInitializer authorizeWithAccessToken(OAuth2AccessToken accessToken) {
        String tokenValue = accessToken.getTokenValue();
        Date expirationTime = accessToken.getExpiresAt() != null ? Date.from(accessToken.getExpiresAt()) : null;

        com.google.auth.oauth2.AccessToken googleAccessToken =
                new com.google.auth.oauth2.AccessToken(tokenValue, expirationTime);
        GoogleCredentials credentials = GoogleCredentials.create(googleAccessToken);
        return new HttpCredentialsAdapter(credentials);
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     *
     * @param accessToken 유효한 액세스 토큰
     * @return YouTube 서비스 객체
     */
    public YouTube getYouTubeService(OAuth2AccessToken accessToken) throws GeneralSecurityException, IOException {
        HttpRequestInitializer requestInitializer = authorizeWithAccessToken(accessToken);
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public OAuth2AccessToken getAccessTokenFromAuthorizedClient(String memberId) {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient("google", memberId); // "youtube"는 클라이언트 ID로 설정된 값이어야 합니다.

        if (authorizedClient == null) {
            throw new IllegalStateException("Access token not found for member " + memberId);
        }

        return authorizedClient.getAccessToken();
    }
}