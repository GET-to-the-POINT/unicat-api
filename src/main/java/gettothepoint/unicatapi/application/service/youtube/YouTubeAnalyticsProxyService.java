package gettothepoint.unicatapi.application.service.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeAnalyticsProxyService {

    private final AppProperties appProperties;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final ProjectRepository projectRepository;

    private YouTubeAnalytics initializeYouTubeAnalyticsService(OAuth2Token accessToken) {

        GoogleCredentials credentials = GoogleCredentials.create(
                new com.google.auth.oauth2.AccessToken(accessToken.getTokenValue(),
                        accessToken.getExpiresAt() != null ? java.util.Date.from(accessToken.getExpiresAt()) : null));

        try {
            return new YouTubeAnalytics.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(appProperties.name()).build();
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize YouTube Analytics service", e);
        }
    }

    public QueryResponse getYouTubeAnalyticsData(OAuth2Token accessToken, Map<String, String> params, Long memberId) {
        try {
            YouTubeAnalytics analytics = initializeYouTubeAnalyticsService(accessToken);
            YouTubeAnalytics.Reports.Query query = analytics.reports().query()
                    .setIds(params.getOrDefault("ids", "channel==MINE")) // 기본값: 현재 사용자의 채널
                    .setStartDate(params.get("startDate"))
                    .setEndDate(params.get("endDate"))
                    .setMetrics(params.get("metrics"));

            List<Project> projectList = projectRepository.findProjectsWithUploadVideoByMemberId(memberId);

            // TODO, 데미 데이터를 너힉 전까지 프로덕트 반영 금지
//            if (projectList.isEmpty()) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploaded video is not found. Please upload a video first.");
//            }

            String projectIdFilter = projectList.stream()
                    .map(project -> project.getUploadVideo().getId().toString())
                    .collect(Collectors.joining(","));

            params.put("filters", projectIdFilter);

            params.forEach((key, value) -> {
                if ("dimensions".equals(key)) {
                    query.setDimensions(value);
                } else if ("filters".equals(key)) {
                    query.setFilters(value);
                } else if ("sort".equals(key)) {
                    query.setSort(value);
                } else if ("maxResults".equals(key)) {
                    query.setMaxResults(Integer.parseInt(value));
                } else if ("startIndex".equals(key)) {
                    query.setStartIndex(Integer.parseInt(value));
                }
            });

            return query.execute();
        } catch (IOException e) {
            QueryResponse errorResponse = new QueryResponse();
            errorResponse.set("error", e.getMessage());
            return errorResponse;
        }
    }
}