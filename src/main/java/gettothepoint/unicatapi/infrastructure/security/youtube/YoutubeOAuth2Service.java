package gettothepoint.unicatapi.infrastructure.security.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class YoutubeOAuth2Service {

    private final AppProperties appProperties;

    /**
      Google API 인증을 위한 Credential 객체 생성
     */
    private HttpRequestInitializer authorizeWithAccessToken(OAuth2AccessToken accessToken) {
        String tokenValue = accessToken.getTokenValue();
        Date expirationTime = accessToken.getExpiresAt() != null ? Date.from(accessToken.getExpiresAt()) : null;

        com.google.auth.oauth2.AccessToken googleAccessToken =
                new com.google.auth.oauth2.AccessToken(tokenValue, expirationTime);
        GoogleCredentials credentials = GoogleCredentials.create(googleAccessToken);
        return new HttpCredentialsAdapter(credentials);
    }

    /**
     YouTube API 서비스 객체를 생성하는 메서드
    */
    public YouTube getYouTubeService(OAuth2AccessToken accessToken) {
        try {
            HttpRequestInitializer requestInitializer = authorizeWithAccessToken(accessToken);
            return new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    requestInitializer)
                    .setApplicationName(appProperties.name())
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "YouTube API 서비스 생성에 실패했습니다.", e);
        }
    }
}