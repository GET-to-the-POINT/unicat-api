package gettothepoint.unicatapi.application.service.video;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.entity.video.Videos;
import gettothepoint.unicatapi.domain.repository.video.VideosRepository;
import gettothepoint.unicatapi.domain.repository.video.YouTubeUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class YouTubeUploadService {

    private static final String APPLICATION_NAME = "YouTube Uploader";

    private final VideosRepository videosRepository;
    private final YouTubeUploadRepository youTubeUploadRepository;

    /**
     * Google API 인증을 위한 Credential 객체 생성
     * @param accessToken 유효한 액세스 토큰
     * @return Google API 인증을 위한 Credential 객체
     */
    private HttpRequestInitializer authorizeWithAccessToken(OAuth2AccessToken accessToken) {
        // Spring OAuth2AccessToken에서 토큰 값과 만료 시간을 가져옵니다.
        String tokenValue = accessToken.getTokenValue();
        // getExpiresAt()는 Instant를 반환하므로, Date로 변환합니다.
        Date expirationTime = accessToken.getExpiresAt() != null ? Date.from(accessToken.getExpiresAt()) : null;

        // Google의 AccessToken으로 변환
        com.google.auth.oauth2.AccessToken googleAccessToken = new com.google.auth.oauth2.AccessToken(tokenValue, expirationTime);
        GoogleCredentials credentials = GoogleCredentials.create(googleAccessToken);
        return new HttpCredentialsAdapter(credentials);
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     * @param accessToken 유효한 액세스 토큰
     * @return YouTube 서비스 객체
     */
    private YouTube getYouTubeService(OAuth2AccessToken accessToken) throws GeneralSecurityException, IOException {
        HttpRequestInitializer requestInitializer = authorizeWithAccessToken(accessToken);
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

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
            YouTube youtubeService = getYouTubeService(accessToken);

            // YouTube 업로드 메타데이터 설정
            Video video = createVideoMetadata(title, description);

            // YouTube API로 동영상 업로드
            Video response = uploadToYouTube(youtubeService, video, videoFile);

            saveUploadVideo(videos, response);

            return response.getId();

        } catch (IOException | GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload video", e);
        }
    }

    private Video createVideoMetadata(String title, String description) {
        Video video = new Video();
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        video.setSnippet(snippet);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public"); // "private", "unlisted" 도 가능
        video.setStatus(status);

        return video;
    }

    private Video uploadToYouTube(YouTube youtubeService, Video video, File videoFile) throws IOException {
        FileContent mediaContent = new FileContent("video/*", videoFile);
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("snippet", "status"), video, mediaContent);

        return request.execute();
    }

    private void saveUploadVideo(Videos videos, Video response) {
        UploadVideo uploadVideo = UploadVideo.builder()
                .video(videos)
                .updateScheduleDate(LocalDate.now())
                .youtubeVideoId(response.getId())
                .build();
        youTubeUploadRepository.save(uploadVideo);
    }
}
