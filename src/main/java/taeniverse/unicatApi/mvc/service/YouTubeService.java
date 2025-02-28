package taeniverse.unicatApi.mvc.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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
        // 받은 엑세스 토큰을 사용하여 Credential 생성
        return new GoogleCredential().setAccessToken(accessToken);
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     * @param accessToken 유효한 엑세스 토큰
     * @return YouTube 서비스 객체
     */
    private YouTube getYouTubeService(String accessToken) throws Exception {
        Credential credential = authorizeWithAccessToken(accessToken);  // 인증된 자격증명
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 동영상을 YouTube에 업로드하는 메서드
     * @param videoFilePath 업로드할 비디오 파일 경로
     * @param videoFileName 업로드할 비디오 파일 이름
     * @param accessToken 유효한 엑세스 토큰
     */
    public void uploadVideo(String videoFilePath, String videoFileName, String accessToken) throws Exception {
        YouTube youtubeService = getYouTubeService(accessToken);  // 엑세스 토큰을 사용하여 YouTube 서비스 객체 가져오기

        // 업로드할 동영상 파일 설정
        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new Exception("Video file does not exist at the specified path: " + videoFilePath);
        }

        // Video 객체 생성 (메타데이터 설정)
        Video video = new Video();

        // 비공개 업로드 설정 (여기선 기본적으로 비공개로 설정)
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private");  // "private", "public", "unlisted"
        video.setStatus(status);

        // 업로드할 파일 설정
        FileContent mediaContent = new FileContent("video/*", videoFile);

        // 동영상 업로드 요청
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("status"), video, mediaContent);

        // 업로드 실행
        Video response = request.execute();
        System.out.println("Video uploaded! Video ID: " + response.getId());
    }
}
