package gettothepoint.unicatapi.application.service.video;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.entity.video.Video;
import gettothepoint.unicatapi.domain.repository.video.VideosRepository;
import gettothepoint.unicatapi.domain.repository.video.YouTubeUploadRepository;
import gettothepoint.unicatapi.infrastructure.security.youtube.YoutubeOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUploadService {

    private final VideosRepository videosRepository;
    private final YouTubeUploadRepository youTubeUploadRepository;
    private final YoutubeOAuth2Service youtubeoAuth2Service;

    /**
     * 동기적으로 비디오 존재 여부와 파일 검증을 수행한 후,
     * 유튜브 업로드 작업은 비동기로 처리합니다.
     */
    public void uploadVideo(String videoId, OAuth2AccessToken accessToken, String title, String description, String visibility) {
        log.info("업로드 요청 수신 - videoId: {}", videoId);

        // 동기적 검증: 비디오 존재 여부 및 파일 검증
        Video video = videosRepository.findByVideoId(Long.valueOf(videoId))
                .orElseThrow(() -> {
                    log.error("Video not found for ID: {}", videoId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found for ID: " + videoId);
                });

        File videoFile = new File(video.getFilePath());
        if (!videoFile.exists() || !videoFile.isFile()) {
            log.error("Video file not found at path: {}", video.getFilePath());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file not found at path: " + video.getFilePath());
        }

        // 비디오 검증이 완료되었으므로 업로드 단계는 비동기로 실행
        uploadVideoToYoutubeAsync(video, accessToken, title, description, visibility);
    }

    /**
     * @Async 애너테이션이 적용된 메서드는 유튜브 업로드 작업을 백그라운드에서 수행합니다.
     */
    @Async
    public CompletableFuture<Void> uploadVideoToYoutubeAsync(Video video, OAuth2AccessToken accessToken, String title, String description, String visibility) {
        return CompletableFuture.runAsync(() -> {
            try {
                YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);

                com.google.api.services.youtube.model.Video youtubeVideo = new com.google.api.services.youtube.model.Video();
                VideoSnippet snippet = new VideoSnippet();
                snippet.setTitle(title);
                snippet.setDescription(description);
                youtubeVideo.setSnippet(snippet);

                VideoStatus status = new VideoStatus();
                status.setPrivacyStatus(visibility);
                youtubeVideo.setStatus(status);

                FileContent mediaContent = new FileContent("video/*", new File(video.getFilePath()));
                YouTube.Videos.Insert request = youtubeService.videos()
                        .insert(List.of("snippet", "status"), youtubeVideo, mediaContent);

                com.google.api.services.youtube.model.Video response = request.execute();

                UploadVideo uploadVideo = UploadVideo.builder()
                        .video(video)
                        .timestamp(LocalDateTime.now())
                        .youtubeVideoId(response.getId())
                        .memberId(video.getMember().getId())
                        .build();

                youTubeUploadRepository.save(uploadVideo);
                log.info("업로드 완료 - YouTube Video ID: {}", response.getId());

            } catch (IOException | GeneralSecurityException e) {
                log.error("YouTube 업로드 실패 - videoId: {}, error: {}", video.getVideoId(), e.getMessage(), e);
                // 업로드 단계에서 예외 발생 시 추가 처리(알림 등)를 할 수 있습니다.
            }
        });
    }
}