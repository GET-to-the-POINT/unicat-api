//package taeniverse.unicatApi.mvc.service;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
//import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
//import org.springframework.stereotype.Service;
//import taeniverse.unicatApi.mvc.repository.YouTubeAnalyticsRepository;
//
//import java.util.List;
//
//@Service
//public class YouTubeAnalyticsService {
//
//    private static final String ACCESS_TOKEN = "ya29.a0AeXRPp6GI2lqwoEOWP4JSt1TGBwugHZq82x6OZIRZdg9ELoDvFlMDdZTY2IJK2R5frzyhS7hqIOddE6k0gyq8eMbACzNBUGOiTyS4P7YrJ_Nggt87kEWenth1yQcNFhZvxM9dZekdfAeJt3x87X0XNvFmFOiA4HxAEVbuAE0QAaCgYKAZ8SARISFQHGX2MiTpRl16bNLSYe0yYkSnyAbA0177"; // 여기에 실제 액세스 토큰을 넣으세요.
//    private static final String CHANNEL_ID = "UCto8MzAV2-kh43gqs6PqPmg"; // 여기에 실제 채널 ID를 넣으세요.
//
//    private final YouTubeAnalyticsRepository youTubeAnalyticsRepository;
//
//    // 생성자 주입을 통해 YouTubeAnalyticsRepository 주입
//    public YouTubeAnalyticsService(YouTubeAnalyticsRepository youTubeAnalyticsRepository) {
//        this.youTubeAnalyticsRepository = youTubeAnalyticsRepository;
//    }
//
//    // DB에서 동영상 ID를 가져온 후 YouTube API로 메타데이터 조회
//    public String getAdvancedVideoAnalytics(String startDate, String endDate) {
//        List<Long> videoIds = youTubeAnalyticsRepository.findAllIds();  // DB에서 동영상 ID 목록을 가져옵니다.
//
//        // 조회 결과를 저장할 StringBuilder
//        StringBuilder result = new StringBuilder();
//
//        for (Long videoId : videoIds) {
//            try {
//                // 자격 증명 설정
//                Credential credential = new GoogleCredential().setAccessToken(ACCESS_TOKEN);
//                HttpTransport transport = new NetHttpTransport();
//                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//                // YouTube Analytics 서비스 객체 생성
//                YouTubeAnalytics youtubeAnalytics = new YouTubeAnalytics.Builder(transport, jsonFactory, credential)
//                        .setApplicationName("YouTube Analytics Example")
//                        .build();
//
//                // Analytics 데이터 요청
//                YouTubeAnalytics.Reports.Query query = youtubeAnalytics.reports().query()
//                        .setIds("channel==" + CHANNEL_ID)
//                        .setStartDate("2025-01-01")  // 예시 날짜 설정
//                        .setEndDate("2025-02-01")    // 예시 날짜 설정
//                        .setMetrics("views,likes,comments,dislikes,estimatedMinutesWatched,estimatedRevenue,shares,subscribersGained,subscribersLost")  // 여러 메트릭 요청
//                        .setDimensions("day")
//                        .setFilters("video==" + videoId);  // DB에서 가져온 videoId에 대한 메트릭만 요청
//
//                // 응답 결과를 받아 처리
//                QueryResponse response = query.execute();
//
//                if (response != null && response.getRows() != null) {
//                    for (var row : response.getRows()) {
//                        result.append("Video ID: ").append(videoId)
//                                .append(", Date: ").append(row.get(0))
//                                .append(", Views: ").append(row.get(1))
//                                .append(", Likes: ").append(row.get(2))
//                                .append(", Comments: ").append(row.get(3))
//                                .append(", Dislikes: ").append(row.get(4))
//                                .append(", Estimated Minutes Watched: ").append(row.get(5))
//                                .append(", Estimated Revenue: ").append(row.get(6))
//                                .append(", Shares: ").append(row.get(7))
//                                .append(", Subscribers Gained: ").append(row.get(8))
//                                .append(", Subscribers Lost: ").append(row.get(9))
//                                .append("\n");
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                result.append("Error retrieving data for Video ID: ").append(videoId).append("\n");
//            }
//        }
//
//        return result.toString();  // 클라이언트에게 결과 반환
//    }
//}
