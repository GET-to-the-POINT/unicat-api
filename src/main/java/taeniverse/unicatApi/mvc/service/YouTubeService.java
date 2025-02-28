package taeniverse.unicatApi.mvc.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.List;

@Service
public class YouTubeService {

    private static final String CLIENT_SECRETS_FILE_PATH = "client_secret.json";
    private static final String APPLICATION_NAME = "YouTube Uploader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);

    /**
     * OAuth 2.0 인증을 수행하는 메서드
     */
    private Credential authorize() throws Exception {
        InputStream in = new FileInputStream(CLIENT_SECRETS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();

        return flow.loadCredential("user");
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     */
    private YouTube getYouTubeService() throws Exception {
        Credential credential = authorize();
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 동영상을 YouTube에 업로드하는 메서드
     */
    public void uploadVideo(String videoFilePath, String videoFileName) throws Exception {
        YouTube youtubeService = getYouTubeService();

        // 업로드할 동영상 파일 설정
        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new Exception("Video file does not exist at the specified path.");
        }

        // Video 객체 생성 (메타데이터 없이)
        Video video = new Video();

        // 비공개 업로드 설정
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private"); // "private", "public", "unlisted"
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
