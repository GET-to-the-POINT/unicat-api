package taeniverse.unicatApi.mvc.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class YouTubeService {

    private static final String APPLICATION_NAME = "YouTube Uploader";
    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);

    /**
     * Google API 인증을 위한 Credential 객체 생성
     * @param accessToken 유효한 엑세스 토큰
     * @return Google API 인증을 위한 Credential 객체
     */
    private Credential authorizeWithAccessToken(String accessToken) throws Exception {
        return new GoogleCredential().setAccessToken(accessToken);
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     * @param accessToken 유효한 엑세스 토큰
     * @return YouTube 서비스 객체
     */
    private YouTube getYouTubeService(String accessToken) throws Exception {
        Credential credential = authorizeWithAccessToken(accessToken);
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void uploadVideo(MultipartFile file, String accessToken, String title, String description) throws Exception {
        YouTube youtubeService = getYouTubeService(accessToken);  // 엑세스 토큰을 사용하여 YouTube 서비스 객체 가져오기

        // 업로드할 동영상 파일 설정
        File videoFile = convertMultiPartToFile(file);
        if (!videoFile.exists()) {
            throw new Exception("Video file does not exist.");
        }

        // 비디오 객체 생성 (메타데이터 포함된 그 자체)
        Video video = new Video();

        // VideoSnippet 객체 (제목, 설명 등 설정)
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        video.setSnippet(snippet);

        // 업로드 설정 (일단 공개로 설정,,)
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public");  // "private", "public", "unlisted"
        video.setStatus(status);

        // 업로드할 파일 설정
        FileContent mediaContent = new FileContent("video/*", videoFile);

        // 동영상 업로드 요청
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("snippet", "status"), video, mediaContent);

        // 업로드 실행
        Video response = request.execute();
        System.out.println("Video uploaded! Video ID: " + response.getId());
    }

    private File convertMultiPartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        return file;
    }
}
