package gettothepoint.unicatapi.application.service.video;


import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.entity.video.Videos;
import gettothepoint.unicatapi.domain.repository.video.VideosRepository;
import gettothepoint.unicatapi.domain.repository.video.YouTubeUploadRepository;
import gettothepoint.unicatapi.infrastructure.security.youtube.YoutubeOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@EnableAsync
public class YoutubeUploadService {

    private static final String APPLICATION_NAME = "YouTube Uploader";

    private final VideosRepository videosRepository;
    private final YouTubeUploadRepository youTubeUploadRepository;
    private final YoutubeOAuth2Service youtubeoAuth2Service;

    @Async
    public CompletableFuture<String> uploadVideo(String videoId, OAuth2AccessToken accessToken, String title, String description) {
        System.out.println("비동기 업로드 시자아아악");
        // 필수 파라미터 검증
        Objects.requireNonNull(videoId, "videoId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");

        // DB에서 videoId로 파일 경로 조회
        Videos videos = videosRepository.findByVideoId(Long.valueOf(videoId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found for ID: " + videoId));

        String filePath = videos.getFilePath();
        File videoFile = new File(filePath);
        if (!videoFile.exists() || !videoFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file not found at path: " + filePath);
        }

        try {
            // YouTube API 호출을 위한 YouTube 서비스 객체 생성
            YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);

            // YouTube 업로드 메타데이터 설정
            Video video = new Video();
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description);
            video.setSnippet(snippet);

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public"); // "private", "unlisted" 도 가능
            video.setStatus(status);

            // YouTube API로 동영상 업로드
            FileContent mediaContent = new FileContent("video/*", videoFile);
            YouTube.Videos.Insert request = youtubeService.videos()
                    .insert(List.of("snippet", "status"), video, mediaContent);

            Video response = request.execute();

            // 비디오 업로드 후 후속 작업 처리
            return CompletableFuture.completedFuture(response.getId())
                    .thenApply(youtubeVideoId -> {
                        // 후속 작업: DB에 업로드 완료 기록 저장
                        UploadVideo uploadVideo = UploadVideo.builder()
                                .video(videos)
                                .timestamp(LocalDateTime.now())
                                .youtubeVideoId(youtubeVideoId)
                                .memberId(videos.getVideoId())
                                .build();

                        youTubeUploadRepository.save(uploadVideo);
                        System.out.println("업로드 완료");
                        // 추가 후속 작업 예: 알림 처리, 상태 업데이트 등
                        notifyUser(youtubeVideoId);  // 후속 작업 예시: 사용자 알림

                        return youtubeVideoId;
                    });

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload video", e);
        }
    }

    // 후속 작업 예시: 사용자에게 비디오 업로드 완료 알림을 보내는 메서드
    private void notifyUser(String youtubeVideoId) {
        // 실제 알림 처리 로직 추가
        System.out.println("User has been notified about video with ID: " + youtubeVideoId);
    }
}