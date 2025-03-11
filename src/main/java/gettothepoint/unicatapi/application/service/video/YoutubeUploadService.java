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
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class YoutubeUploadService {

    private static final String APPLICATION_NAME = "YouTube Uploader";

    private final VideosRepository videosRepository;
    private final YouTubeUploadRepository youTubeUploadRepository;
    private final YoutubeOAuth2Service youtubeoAuth2Service;



    public String uploadVideo(String videoId, OAuth2AccessToken accessToken, String title, String description) {
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

//            Long memberId = youtubeoAuth2Service.getMemberIdFromAccessToken(accessToken);

            UploadVideo uploadVideo = UploadVideo.builder()
                    .video(videos)
                    .timestamp(LocalDateTime.now())
                    .youtubeVideoId(response.getId())
                    .memberId(videos.getVideoId())
                    .build();

            youTubeUploadRepository.save(uploadVideo);

            return response.getId();

        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();  // 더 자세한 스택 트레이스를 기록
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload video", e);
        }

    }
}