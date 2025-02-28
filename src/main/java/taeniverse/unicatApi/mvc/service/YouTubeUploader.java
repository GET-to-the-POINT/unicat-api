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

import java.io.*;
import java.util.Collections;
import java.util.List;

public class YouTubeUploader {
    private static final String CLIENT_SECRETS_FILE_PATH = "/Users/yurim/Desktop/unicat.day/client_secret.json"; // OAuth 2.0 JSON 파일
    private static final String APPLICATION_NAME = "YouTube Uploader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);

    /**
     * OAuth 2.0 인증을 수행하는 메서드
     */
    private static Credential authorize() throws Exception {
        // JSON에서 클라이언트 비밀키 로드
        InputStream in = new FileInputStream(CLIENT_SECRETS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // 인증 흐름 설정
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();

        // 사용자 인증
        return flow.loadCredential("user");
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     */
    public static YouTube getYouTubeService() throws Exception {
        Credential credential = authorize();
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 동영상을 YouTube에 업로드하는 메서드
     */
    public static void uploadVideo(String videoFilePath, String title, String description) throws Exception {
        YouTube youtubeService = getYouTubeService();

        // 동영상 메타데이터 설정
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        snippet.setTags(Collections.singletonList("API Upload"));

        // 비공개 업로드 설정
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("private"); // "public", "private", "unlisted"

        // Video 객체 생성
        Video video = new Video();
        video.setSnippet(snippet);
        video.setStatus(status);

        // 업로드할 동영상 파일 설정
        File videoFile = new File(videoFilePath);
        FileContent mediaContent = new FileContent("video/*", videoFile);

        // 동영상 업로드 요청
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("snippet,status"), video, mediaContent);

        // 업로드 실행
        Video response = request.execute();
        System.out.println("업로드 완료! 비디오 ID: " + response.getId());
    }

    public static void main(String[] args) throws Exception {

        String filePath = "/Users/yurim/Desktop/unicat.day/client_secret.json";
        File file = new File(filePath);

        if (file.exists()) {
            System.out.println("파일이 존재합니다: " + file.getAbsolutePath());
        } else {
            System.out.println("파일이 존재하지 않습니다: " + filePath);
        }

        String videoFilePath = "/Users/yurim/Desktop/unicat.day/유민.mp4"; // 업로드할 동영상 파일 경로
        String title = "테스트 동영상 업로드";
        String description = "이것은 YouTube API를 사용한 자동 업로드 테스트입니다.";

        uploadVideo(videoFilePath, title, description);
    }
}
