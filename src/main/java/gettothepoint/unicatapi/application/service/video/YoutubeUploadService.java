package gettothepoint.unicatapi.application.service.video;

import com.google.api.client.http.FileContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.video.UploadVideo;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.video.UploadVideoRepository;
import gettothepoint.unicatapi.infrastructure.security.youtube.YoutubeOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUploadService {

    private final ProjectRepository projectRepository;
    private final UploadVideoRepository uploadVideoRepository;
    private final YoutubeOAuth2Service youtubeoAuth2Service;
    private final UploadProgressService uploadProgressService;
    private final SupabaseStorageService supabaseStorageService;

    public void uploadVideo(Long projectId, String channelId, OAuth2AccessToken accessToken, String title, String description, String visibility) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found for ID: " + projectId));

        File videoFile = new File(project.getArtifactUrl());
        if (!videoFile.exists() || !videoFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video file not found at path: " + project.getArtifactUrl());
        }

        uploadProgressService.updateProgress(projectId, 0.0);
        uploadVideoToYoutubeAsync(project, channelId, accessToken, title, description, visibility);
    }

    @Async
    public void uploadVideoToYoutubeAsync(Project project, String channelId, OAuth2AccessToken accessToken, String title, String description, String visibility) {
        CompletableFuture.runAsync(() -> {
            try {
                YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);

                Video youtubeVideo = getYoutubeVideo(title, description);

                VideoStatus status = new VideoStatus();
                status.setPrivacyStatus(visibility);
                youtubeVideo.setStatus(status);

                FileContent mediaContent = new FileContent("video/*", new File(project.getArtifactUrl()));
                YouTube.Videos.Insert request = youtubeService.videos()
                        .insert(List.of("snippet", "status"), youtubeVideo, mediaContent);

                request.getMediaHttpUploader().setProgressListener(uploaderProgress -> {
                    double progress = uploaderProgress.getProgress() * 100;
                    uploadProgressService.updateProgress(project.getId(), progress);
                });

                Video youtubeResponse = request.execute();

                UploadVideo uploadVideo = UploadVideo.builder()
                        .linkId(youtubeResponse.getId())
                        .channelId(channelId)
                        .project(project)
                        .build();

                saveUploadVideoAndUpdateProject(uploadVideo, project);

            } catch (IOException e) {
                log.error("YouTube 업로드 실패 - videoId: {}, error: {}", project.getId(), e.getMessage(), e);
                uploadProgressService.markFailed(project.getId());
            }
        });
    }

    private static Video getYoutubeVideo(String title, String description) {
        Video youtubeVideo = new Video();
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        youtubeVideo.setSnippet(snippet);
        return youtubeVideo;
    }

    @Transactional
    public void saveUploadVideoAndUpdateProject(UploadVideo uploadVideo, Project project) {
        uploadVideoRepository.save(uploadVideo);
        project.assignUploadVideo(uploadVideo);
        projectRepository.save(project);
        uploadProgressService.markCompleted(project.getId());
    }


    @Async
    public CompletableFuture<String> uploadVideoToYoutube(String artifactUrl, Long projectId, OAuth2AccessToken accessToken) {
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

            File videoFile = supabaseStorageService.downloadFile(artifactUrl);

            if (!videoFile.exists() || !videoFile.isFile()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "다운로드한 비디오 파일이 존재하지 않습니다: " + artifactUrl);
            }

            YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);
            Video youtubeVideo = new Video();

            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle("프로젝트 " + projectId + "의 영상");
            snippet.setDescription("생성되었습니다.");
            youtubeVideo.setSnippet(snippet);

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");
            youtubeVideo.setStatus(status);

            FileContent mediaContent = new FileContent("video/*", videoFile);
            YouTube.Videos.Insert request = youtubeService.videos()
                    .insert(List.of("snippet", "status"), youtubeVideo, mediaContent);

            request.getMediaHttpUploader().setProgressListener(uploaderProgress -> {
                double progress = uploaderProgress.getProgress() * 100;
                uploadProgressService.updateProgress(projectId, progress);
            });

            Video uploadedVideo = request.execute();
            String youtubeUrl = "https://www.youtube.com/watch?v=" + uploadedVideo.getId();

            projectRepository.save(project);

            UploadVideo uploadVideo = UploadVideo.builder()
                    .linkId(uploadedVideo.getId())
                    .project(project)
                    .build();

            uploadVideoRepository.save(uploadVideo);

            return CompletableFuture.completedFuture(youtubeUrl);
        } catch (Exception e) {
            log.error(" 유튜브 업로드 실패 - 프로젝트 ID: {}, 에러: {}", projectId, e.getMessage(), e);
            uploadProgressService.markFailed(projectId);
            return CompletableFuture.failedFuture(e);
        }
    }
}