package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Project;

import java.util.List;

public record ProjectDto(
        Long id,
        String title,
        String subtitle,
        String thumbnailUrl,
        String artifactUrl,
        String scriptTone,
        String imageStyle,
        List<SectionResponse> sections
) {
    public static ProjectDto fromEntity(Project project, List<SectionResponse> sections) {
        return new ProjectDto(
                project.getId(),
                project.getTitle(),
                project.getSubtitle(),
                project.getThumbnailUrl(),
                project.getArtifactUrl(),
                project.getScriptTone(),
                project.getImageStyle(),
                sections
        );
    }
}