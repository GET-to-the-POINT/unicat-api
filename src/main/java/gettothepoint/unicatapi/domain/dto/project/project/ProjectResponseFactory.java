package gettothepoint.unicatapi.domain.dto.project.project;

import gettothepoint.unicatapi.application.service.storage.StorageService;
import gettothepoint.unicatapi.domain.entity.project.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResponseFactory {

    private final StorageService storageService;

    public ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .subtitle(project.getSubtitle())
                .description(project.getDescription())
                .thumbnailUrl(project.getThumbnailUrl() != null ? storageService.getUri(project.getThumbnailUrl()).toString() : null)
                .titleUrl(project.getTitleImageKey() != null ? storageService.getUri(project.getTitleImageKey()).toString() : null)
                .artifactUrl(project.getArtifactKey() != null ? storageService.getUri(project.getArtifactKey()).toString() : null)
                .scriptTone(project.getScriptTone())
                .imageStyle(project.getImageStyle())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
