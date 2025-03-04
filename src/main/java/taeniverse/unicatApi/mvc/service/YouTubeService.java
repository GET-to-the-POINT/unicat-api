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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.List;

import taeniverse.unicatApi.mvc.model.entity.UploadVideo;
import taeniverse.unicatApi.mvc.repository.YouTubeRepository;

@Service
public class YouTubeService {

    private static final String APPLICATION_NAME = "YouTube Uploader";

    @Autowired
    private YouTubeRepository youTubeRepository;

    /**
     * Google API 인증을 위한 Credential 객체 생성
     * @param accessToken 유효한 액세스 토큰
     * @return Google API 인증을 위한 Credential 객체
     */
    private Credential authorizeWithAccessToken(String accessToken) {
        return new GoogleCredential().setAccessToken(accessToken);
    }

    /**
     * YouTube API 서비스 객체를 생성하는 메서드
     * @param accessToken 유효한 액세스 토큰
     * @return YouTube 서비스 객체
     */
    private YouTube getYouTubeService(String accessToken) throws Exception {
        Credential credential = authorizeWithAccessToken(accessToken);
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String uploadVideo(String videoId, String accessToken, String title, String description) throws Exception {
        // 🔹 DB에서 videoId로 파일 경로 조회
        UploadVideo uploadVideo = youTubeRepository.findByVideoId(videoId);
        if (uploadVideo == null) {
            throw new Exception("동영상 정보를 찾을 수 없습니다. videoId: " + videoId);
        }

        String filePath = uploadVideo.getFilePath();
        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            throw new Exception("파일이 존재하지 않습니다. 경로: " + filePath);
        }

        // 🔹 YouTube API 호출을 위한 YouTube 서비스 객체 생성
        YouTube youtubeService = getYouTubeService(accessToken);

        // 🔹 YouTube 업로드 메타데이터 설정
        Video video = new Video();
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        video.setSnippet(snippet);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus("public"); // "private", "unlisted" 도 가능
        video.setStatus(status);

        // 🔹 YouTube API로 동영상 업로드
        FileContent mediaContent = new FileContent("video/*", videoFile);
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(List.of("snippet", "status"), video, mediaContent);

        Video response = request.execute();
        System.out.println("Video uploaded! Video ID: " + response.getId());

        return response.getId(); // 업로드된 동영상 ID 반환
    }

}






































//package taeniverse.unicatApi.mvc.service;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.FileContent;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.youtube.YouTube;
//import com.google.api.services.youtube.YouTubeScopes;
//import com.google.api.services.youtube.model.Video;
//import com.google.api.services.youtube.model.VideoSnippet;
//import com.google.api.services.youtube.model.VideoStatus;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.io.File;
//import java.util.Collections;
//import java.util.List;
//
//import taeniverse.unicatApi.mvc.model.entity.UploadVideo;
//import taeniverse.unicatApi.mvc.repository.YouTubeRepository;
//
//@Service
//public class YouTubeService {
//
//    private static final String APPLICATION_NAME = "YouTube Uploader";
//    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);
//
//    @Autowired
//    private YouTubeRepository youTubeRepository;
//
//
//    /**
//     * Google API 인증을 위한 Credential 객체 생성
//     * @param accessToken 유효한 엑세스 토큰
//     * @return Google API 인증을 위한 Credential 객체
//     */
//    private Credential authorizeWithAccessToken(String accessToken) throws Exception {
//        return new GoogleCredential().setAccessToken(accessToken);
//    }
//
//    /**
//     * YouTube API 서비스 객체를 생성하는 메서드
//     * @param accessToken 유효한 엑세스 토큰
//     * @return YouTube 서비스 객체
//     */
//    private YouTube getYouTubeService(String accessToken) throws Exception {
//        Credential credential = authorizeWithAccessToken(accessToken);
//        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    public void uploadVideo(String videoId, String accessToken, String title, String description) throws Exception {
//
//        UploadVideo uploadVideo = youTubeRepository.findByVideoId(videoId);
//
//        if (uploadVideo == null) {
//            throw new Exception("동영상 정보를 찾을 수 없습니다.");
//        }
//
//        // 비디오 경로 확인
//        String filePath = uploadVideo.getFilePath();
//        File videoFile = new File(filePath);
//        if (!videoFile.exists()) {
//            throw new Exception("파일이 존재하지 않습니다. 경로: " + filePath);
//        }
//
//        YouTube youtubeService = getYouTubeService(accessToken);
//
//        // 비디오 객체 생성 (메타데이터 포함된 그 자체)
//        Video video = new Video();
//
//        // VideoSnippet 객체 (제목, 설명 등 설정)
//        VideoSnippet snippet = new VideoSnippet();
//        snippet.setTitle(title);
//        snippet.setDescription(description);
//        video.setSnippet(snippet);
//
//        // 업로드 설정 (일단 공개로 설정,,)
//        VideoStatus status = new VideoStatus();
//        status.setPrivacyStatus("public");  // "private", "public", "unlisted"
//        video.setStatus(status);
//
//        // 업로드할 파일 설정
//        FileContent mediaContent = new FileContent("video/*", videoFile);
//
//        // 동영상 업로드 요청
//        YouTube.Videos.Insert request = youtubeService.videos()
//                .insert(List.of("snippet", "status"), video, mediaContent);
//
//        // 업로드 실행
//        Video response = request.execute();
//        System.out.println("Video uploaded! Video ID: " + response.getId());
//    }
//
//}
