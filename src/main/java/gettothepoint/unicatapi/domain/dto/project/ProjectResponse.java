package gettothepoint.unicatapi.domain.dto.project;

import gettothepoint.unicatapi.domain.entity.dashboard.Project;

import java.time.LocalDateTime;

public record ProjectResponse(Long id, String title, String subtitle, String thumbnailUrl, String artifactUrl,
                              String scriptTone, String imageStyle, LocalDateTime createdAt) {
    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(project.getId(), project.getTitle(), project.getSubtitle(), project.getThumbnailUrl(), project.getArtifactUrl(), project.getScriptTone(), project.getImageStyle(), project.getCreatedAt());
    }
}
