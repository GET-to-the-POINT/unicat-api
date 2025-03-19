package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.ProjectRepository;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ArtifactService {

    private final ProjectRepository projectRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final MergeService mergeService;
    private final SectionRepository sectionRepository;
    private final SupabaseStorageService supabaseStorageService;

    private Project build(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다: " + projectId));
        if (project.getArtifactUrl() != null) {
            return project;
        }

        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("해당 프로젝트에 포함된 섹션이 없습니다: " + projectId);
        }
        List<String> videoPaths = sections.stream()
                .map(Section::getVideoUrl)
                .toList();  // Java 16+

        String outputFile = mergeService.videos(videoPaths);

        File mergedFile = new File(outputFile);

        MultipartFile multipartFile = new MultipartFileUtil(mergedFile, "artifact_video", "video/mp4");

        String supabaseUrl;
        try {
            supabaseUrl = supabaseStorageService.uploadFile(multipartFile);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Supabase에 비디오 업로드 실패: " + e.getMessage(), e);
        }

        project.setArtifactUrl(supabaseUrl);
        projectRepository.save(project);
        FileUtil.deleteLocalFile(outputFile);

        return project;
    }

    public void create(Long projectId) {
        this.create(projectId, "artifact", null);
    }

    public void create(Long projectId, String type, OAuth2AccessToken accessToken) {
        Project project = build(projectId);

        if ("youtube".equalsIgnoreCase(type)) {
            youtubeUploadService.uploadVideoToYoutube(project, accessToken);
        }
    }
}