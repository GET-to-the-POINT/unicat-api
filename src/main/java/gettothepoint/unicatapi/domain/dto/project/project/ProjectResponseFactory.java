package gettothepoint.unicatapi.domain.dto.project.project;

import gettothepoint.unicatapi.domain.entity.project.Project;
import gettothepoint.unicatapi.filestorage.application.FileDownloadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResponseFactory {

    private final FileDownloadUseCase fileDownloadUseCase;

    public ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .subtitle(project.getSubtitle())
                .description(project.getDescription())
                .thumbnailUrl(project.getThumbnailUrl() != null ? fileDownloadUseCase.downloadFile(project.getThumbnailUrl()).toString() : null)
                .titleUrl(project.getTitleImageKey() != null ? fileDownloadUseCase.downloadFile(project.getTitleImageKey()).toString() : null)
                .artifactUrl(project.getArtifactKey() != null ? fileDownloadUseCase.downloadFile(project.getArtifactKey()).toString() : null)
                .scriptTone(project.getScriptTone())
                .imageStyle(project.getImageStyle())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
