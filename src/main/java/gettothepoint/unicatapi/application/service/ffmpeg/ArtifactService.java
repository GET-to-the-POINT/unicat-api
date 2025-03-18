package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class ArtifactService {

    private final ProjectRepository projectRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final MergeService mergeService;

    public String handleArtifactRequest(Long projectId, String type, OAuth2AccessToken accessToken) throws IOException, InterruptedException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        if (project.getArtifactUrl() != null && !project.getArtifactUrl().isEmpty()) {
            if ("youtube".equalsIgnoreCase(type)) {

                return "기존 아티팩트가 유튜브에 업로드되었습니다: " +
                        youtubeUploadService.uploadVideoToYoutube(project.getArtifactUrl(), projectId, accessToken).join();
            }
            return "기존 아티팩트가 이미 존재합니다: " + project.getArtifactUrl();
        }

        String artifactUrl = mergeService.createArtifactVideo(projectId);
        project.setArtifactUrl(artifactUrl);
        projectRepository.save(project);

        if ("youtube".equalsIgnoreCase(type)) {
            return "아티팩트가 생성되고 유튜브에 업로드되었습니다: " +
                    youtubeUploadService.uploadVideoToYoutube(artifactUrl, projectId, accessToken).join();
        }
        return "아티팩트가 성공적으로 생성되었습니다: " + artifactUrl;
    }
}