package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.application.service.project.ProjectService;
import gettothepoint.unicatapi.application.service.project.SectionService;
import gettothepoint.unicatapi.application.service.storage.AbstractStorageService;
import gettothepoint.unicatapi.application.service.video.YoutubeUploadService;
import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ArtifactService {

    private final YoutubeUploadService youtubeUploadService;
    private final MediaService mediaService;
    private final ProjectService projectService;
    private final SectionService sectionService;
    private final AbstractStorageService abstractStorageService;

    public void create(Long projectId) {
        this.create(projectId, "artifact", null);
    }

    // TODO: type을 String 으로 받으면 오타 나기 쉬움 밸리데이션 추가 필요, 또는 이넘으로 관리 필요
    public void create(Long projectId, String type, OAuth2AccessToken accessToken) {
        Project project = makeProcess(projectId);
        try {
            if ("youtube".equalsIgnoreCase(type)) youtubeUploadService.uploadVideoToYoutube(project, accessToken);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Project makeProcess(Long projectId) {
        Project project = projectService.getOrElseThrow(projectId);
        List<Section> sections = sectionService.getAllSortedBySortOrderOrElseThrow(projectId);
        List<Integer> videoHashes = sections.stream().map(Section::getVideoHashCode).toList();
        List<InputStream> videoStreams = abstractStorageService.downloads(videoHashes);
        InputStream artifactStream = mediaService.mergeVideosAndExtractVFRFromInputStream(videoStreams);
        Integer artifactHashCode = abstractStorageService.upload(artifactStream);

        project.setArtifactHashCode(artifactHashCode);
        project.setArtifactMimeType("video/mp4"); // TODO: mime type을 어떻게 가져올지 고민 필요
        projectService.update(project);

        return project;
    }

}