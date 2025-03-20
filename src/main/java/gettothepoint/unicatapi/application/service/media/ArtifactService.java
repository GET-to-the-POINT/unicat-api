package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
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
    private final MediaService mediaService;
    private final SupabaseStorageService supabaseStorageService;
    private final ProjectService projectService;
    private final SectionService sectionService;

    private Project build(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        List<Section> sections = sectionService.getAllSortedBySortOrderIfEmptyThrow(projectId);
        List<String> videoPaths = sections.stream()
                .map(Section::getVideoUrl)
                .toList();  // Java 16
        String outputFile = mediaService.videos(videoPaths);

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