package gettothepoint.unicatapi.application.service.video;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Videos.Insert;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.infrastructure.security.youtube.YoutubeOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUploadService {

    private final YoutubeOAuth2Service youtubeoAuth2Service;
    private final StorageService storageService;

    @Async
    public CompletableFuture<Void> uploadToYoutube(Project project, OAuth2AccessToken accessToken) {
        try {
            File video = storageService.download(project.getArtifactUrl());
            YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);
            Video youtubeVideo = new Video();

            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(project.getTitle());
            snippet.setDescription(project.getDescription());
            youtubeVideo.setSnippet(snippet);

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");
            youtubeVideo.setStatus(status);

            FileContent mediaContent = new FileContent("video/*", video);
            Insert request = youtubeService.videos().insert(List.of("snippet", "status"), youtubeVideo, mediaContent);
            request.execute();

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}