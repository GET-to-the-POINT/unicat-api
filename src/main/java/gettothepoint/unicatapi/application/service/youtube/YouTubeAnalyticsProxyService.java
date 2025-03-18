package gettothepoint.unicatapi.application.service.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YouTubeAnalyticsProxyService {

    private final AppProperties appProperties;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private YouTubeAnalytics initializeYouTubeAnalyticsService(OAuth2Token accessToken)
            throws GeneralSecurityException, IOException {

        GoogleCredentials credentials = GoogleCredentials.create(
                new com.google.auth.oauth2.AccessToken(accessToken.getTokenValue(),
                        accessToken.getExpiresAt() != null ? java.util.Date.from(accessToken.getExpiresAt()) : null));

        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(appProperties.name()).build();
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
                    case "dimensions" -> query.setDimensions(value);
                    case "filters" -> query.setFilters(value);
                    case "sort" -> query.setSort(value);
                    case "maxResults" -> query.setMaxResults(Integer.parseInt(value));
                    case "startIndex" -> query.setStartIndex(Integer.parseInt(value));
                }
            });

            return query.execute();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}