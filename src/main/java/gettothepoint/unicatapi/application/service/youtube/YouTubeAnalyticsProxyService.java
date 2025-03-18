package gettothepoint.unicatapi.application.service.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YouTubeAnalyticsProxyService {

    private static final String APPLICATION_NAME = "YourAppName";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private YouTubeAnalytics initializeYouTubeAnalyticsService(OAuth2Token accessToken)
            throws GeneralSecurityException, IOException {

        String tokenValue = accessToken.getTokenValue();
        Date expirationTime = accessToken.getExpiresAt() != null ? Date.from(accessToken.getExpiresAt()) : null;
        AccessToken googleAccessToken = new AccessToken(tokenValue, expirationTime);

        GoogleCredentials credentials = GoogleCredentials.create(googleAccessToken);

        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    public QueryResponse getYouTubeAnalyticsData(OAuth2Token accessToken, Map<String, String> params) {
        try {
            YouTubeAnalytics analytics = initializeYouTubeAnalyticsService(accessToken);

            YouTubeAnalytics.Reports.Query query = analytics.reports().query()
                    .setIds(params.getOrDefault("ids", "channel==MINE")) // 기본값: 현재 사용자의 채널
                    .setStartDate(params.get("startDate"))
                    .setEndDate(params.get("endDate"))
                    .setMetrics(params.get("metrics"));

            params.forEach((key, value) -> {
                switch (key) {
                    case "dimensions":
                        query.setDimensions(value);
                        break;
                    case "filters":
                        query.setFilters(value);
                        break;
                    case "sort":
                        query.setSort(value);
                        break;
                    case "maxResults":
                        query.setMaxResults(Integer.parseInt(value));
                        break;
                    case "startIndex":
                        query.setStartIndex(Integer.parseInt(value));
                        break;
                }
            });

            return query.execute();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}