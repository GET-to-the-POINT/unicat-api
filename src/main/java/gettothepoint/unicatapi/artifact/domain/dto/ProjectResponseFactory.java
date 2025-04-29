package gettothepoint.unicatapi.artifact.domain.dto;

import gettothepoint.unicatapi.artifact.domain.Project;
import gettothepoint.unicatapi.filestorage.application.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResponseFactory {

    private final FileService fileService;

    public ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .subtitle(project.getSubtitle())
                .description(project.getDescription())
                .thumbnailUrl(project.getThumbnailUrl() != null ? fileService.downloadFile(project.getThumbnailUrl()).toString() : null)
                .titleUrl(project.getTitleImageKey() != null ? fileService.downloadFile(project.getTitleImageKey()).toString() : null)
                .artifactUrl(project.getArtifactKey() != null ? fileService.downloadFile(project.getArtifactKey()).toString() : null)
                .scriptTone(project.getScriptTone())
                .imageStyle(project.getImageStyle())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
