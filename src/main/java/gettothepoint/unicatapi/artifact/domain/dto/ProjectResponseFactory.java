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
                .description(project.getDescription())
                .scriptTone(project.getScriptTone())
                .imageStyle(project.getImageStyle())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
