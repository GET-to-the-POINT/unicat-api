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

    private final ProjectRepository projectRepository;
    private final UploadVideoRepository uploadVideoRepository;
    private final YoutubeOAuth2Service youtubeoAuth2Service;
    private final UploadProgressService uploadProgressService;
    private final SupabaseStorageService supabaseStorageService;

    @Async
    public void uploadVideoToYoutube(Project project, OAuth2AccessToken accessToken) {
//        try {
//            File videoFile = supabaseStorageService.downloadFile(project.getArtifactUrl());
//            YouTube youtubeService = youtubeoAuth2Service.getYouTubeService(accessToken);
//            Video youtubeVideo = new Video();
//
//            VideoSnippet snippet = new VideoSnippet();
//            snippet.setTitle("프로젝트 " + project.getId() + "의 영상");
//            snippet.setDescription("생성되었습니다.");
//            youtubeVideo.setSnippet(snippet);
//
//            VideoStatus status = new VideoStatus();
//            status.setPrivacyStatus("public");
//            youtubeVideo.setStatus(status);
//
//            FileContent mediaContent = new FileContent("video/*", videoFile);
//            YouTube.Videos.Insert request = youtubeService.videos()
//                    .insert(List.of("snippet", "status"), youtubeVideo, mediaContent);
//
//            request.getMediaHttpUploader().setProgressListener(uploaderProgress -> {
//                double progress = uploaderProgress.getProgress() * 100;
//                uploadProgressService.updateProgress(project.getId(), progress);
//            });
//
//            Video uploadedVideo = request.execute();
//            String youtubeUrl = "https://www.youtube.com/watch?v=" + uploadedVideo.getId();
//
//            projectRepository.save(project);
//
//            uploadVideoRepository.save(UploadVideo.builder()
//                    .linkId(uploadedVideo.getId())
//                    .project(project)
//                    .build()
//            );
//
//            project.assignUploadVideo(uploadVideo);
//            projectRepository.save(project);
//
//            CompletableFuture.completedFuture(youtubeUrl);
//        } catch (Exception e) {
//            log.error(" 유튜브 업로드 실패 - 프로젝트 ID: {}, 에러: {}", project.getId(), e.getMessage(), e);
//            uploadProgressService.markFailed(project.getId());
//            CompletableFuture.failedFuture(e);
//        }
    }
}