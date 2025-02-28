package taeniverse.unicatApi.mvc.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.entity.YoutubeVideo;
import taeniverse.unicatApi.mvc.repository.YoutubeDataRepository;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
@Service
public class YoutubeDataService {


    private String API_KEY="AIzaSyD98eK0PRdCvE9hr_7qLIwhriuxdM5mUZc";

    private final YoutubeDataRepository youtubeDataRepository;

    // YouTube 서비스 생성
    private YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        //싱글톤
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        //HTTP 트랜스포터,유튜브 서버와 HTTP 요청을 주고받기 위한 트랜스포트 설정
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                jsonFactory,
                //추가 요청
                request -> {

                })
                .setApplicationName("My First Project")
                .build();
    }

    // YouTube 동영상 조회수, 좋아요 등을 가져오는 메서드
    public String getVideoData(String videoId) throws GeneralSecurityException, IOException {

        YouTube youtubeService = getYouTubeService();
        YouTube.Videos.List request = youtubeService.videos().list(Arrays.asList("statistics"));
        request.setId(Arrays.asList(videoId));
        request.setKey(API_KEY);

        VideoListResponse response = request.execute();

        // 동영상 정보 추출
        if (response.getItems() != null && !response.getItems().isEmpty()) {
            Video video = response.getItems().get(0);
            BigInteger viewCount = video.getStatistics().getViewCount();
            BigInteger likeCount = video.getStatistics().getLikeCount();
            BigInteger commentCount = video.getStatistics().getCommentCount();

            boolean exists = youtubeDataRepository.existsByVideoId(videoId);

            if (!exists) {
                YoutubeVideo videoEntity = new YoutubeVideo();
                videoEntity.setVideoId(videoId);
                videoEntity.setViewCount(viewCount);
                videoEntity.setLikeCount(likeCount);
                videoEntity.setCommentCount(commentCount);

                LocalDateTime now = LocalDateTime.now(); // 현재 날짜와 시간 (LocalDateTime)
                videoEntity.setDate(now);

                youtubeDataRepository.save(videoEntity);
            } else {
                System.out.println("이미 존재하는 데이터: " + videoId + ", 저장하지 않음.");
            }
            return String.format("조회수: %d, 좋아요 수: %d, 댓글 수: %d",
                    viewCount, likeCount, commentCount);
        } else {
            return "동영상 정보를 찾을 수 없습니다.";
        }
    }

    // YouTube 동영상 조회수, 좋아요 등을 가져오는 메서드 (여러 동영상)
    public String getVideosData(String[] videoIds) throws GeneralSecurityException, IOException {

        YouTube youtubeService = getYouTubeService();

        // 여러 개의 동영상 ID를 한 번에 전달
        YouTube.Videos.List request = youtubeService.videos().list(Arrays.asList("statistics"));
        request.setId(Arrays.asList(videoIds));  // 여러 동영상 ID를 리스트로 전달
        request.setKey(API_KEY);

        VideoListResponse response = request.execute();

        // 여러 동영상의 정보를 추출
        StringBuilder result = new StringBuilder();
        if (response.getItems() != null && !response.getItems().isEmpty()) {
            for (Video video : response.getItems()) {
                BigInteger viewCount = video.getStatistics().getViewCount();
                BigInteger likeCount = video.getStatistics().getLikeCount();
                BigInteger commentCount = video.getStatistics().getCommentCount();

                boolean exists = youtubeDataRepository.existsByVideoId(video.getId());

                if (!exists) {
                    YoutubeVideo videoEntity = new YoutubeVideo();
                    videoEntity.setVideoId(video.getId());
                    videoEntity.setViewCount(viewCount);
                    videoEntity.setLikeCount(likeCount);
                    videoEntity.setCommentCount(commentCount);

                    LocalDateTime now = LocalDateTime.now(); // 현재 날짜와 시간 (LocalDateTime)
                    videoEntity.setDate(now);

                    youtubeDataRepository.save(videoEntity);
                } else {
                    System.out.println("이미 존재하는 데이터: " + video.getId() + ", 저장하지 않음.");
                }

                result.append(String.format("동영상 ID: %s\n조회수: %d\n좋아요 수: %d\n댓글 수: %d\n\n",
                        video.getId(), viewCount, likeCount, commentCount));
            }
        } else {
            return "동영상 정보를 찾을 수 없습니다.";
        }

        return result.toString();
    }

}
