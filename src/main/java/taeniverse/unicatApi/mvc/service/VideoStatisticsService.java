package taeniverse.unicatApi.mvc.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;
import taeniverse.unicatApi.mvc.repository.VideoStatisticsRepository;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;


@Service
public class VideoStatisticsService {

    //@Value("${youtube.api.key}")
    private String API_KEY = "AIzaSyD98eK0PRdCvE9hr_7qLIwhriuxdM5mUZc";
    private static final String VIDEO_ID = "4PQs57cq84U";  // 동영상 ID

    @Autowired
    private VideoStatisticsRepository videoStatisticsRepository;

    // YouTube 서비스 생성
    private YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        //싱글톤
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        //HTTP 트랜스포터, http 요청 보내는거
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                //추가 요청
                request -> {

                })
                .setApplicationName("My First Project")
                .build();
    }

    // YouTube 동영상 조회수, 좋아요 등을 가져오는 메서드
    public String getVideoStatistics() throws GeneralSecurityException, IOException {
        YouTube youtubeService = getYouTubeService();
        YouTube.Videos.List request = youtubeService.videos().list(Arrays.asList("statistics"));
        request.setId(Arrays.asList(VIDEO_ID));
        request.setKey(API_KEY);

        VideoListResponse response = request.execute();

        // 동영상 정보 추출
        if (response.getItems() != null && !response.getItems().isEmpty()) {
            Video video = response.getItems().get(0);
            BigInteger viewCount = video.getStatistics().getViewCount();
            BigInteger likeCount = video.getStatistics().getLikeCount();
            BigInteger commentCount = video.getStatistics().getCommentCount();

            // Video 객체 생성
            VideoStatistics videoStatistics = new VideoStatistics();// 엔티티 Video 사용
            videoStatistics.setVideoId(VIDEO_ID);  // 필드 수정
            videoStatistics.setViewCount(viewCount);
            videoStatistics.setLikeCount(likeCount);
            videoStatistics.setCommentCount(commentCount);

            // DB에 저장
            videoStatisticsRepository.save(videoStatistics);  // 레파지토리 이름 수정

            return String.format("조회수: %d, 좋아요 수: %d, 댓글 수: %d",
                    viewCount, likeCount, commentCount);
        } else {
            return "동영상 정보를 찾을 수 없습니다.";
        }
    }
}