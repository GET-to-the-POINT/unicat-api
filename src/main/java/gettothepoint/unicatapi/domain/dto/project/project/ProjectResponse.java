package gettothepoint.unicatapi.domain.dto.project.project;

import gettothepoint.unicatapi.domain.entity.project.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 프로젝트 응답 DTO
 * 이 DTO는 프로젝트 정보를 클라이언트에 전달하기 위한 객체입니다.
 */
@Builder
@Schema(description = "프로젝트 응답 DTO")
public record ProjectResponse(
    @Schema(description = "프로젝트 고유 ID", example = "1") Long id,
    @Schema(description = "프로젝트 제목", example = "백설공주") String title,
    @Schema(description = "프로젝트 부제목", example = "마녀의 이야기") String subtitle,
    @Schema(description = "프로젝트 설명", example = "이 프로젝트는 동화에 관한 동영상입니다.") String description,
    @Schema(description = "스크립트 톤", example = "Friendly") String scriptTone,
    @Schema(description = "이미지 스타일", example = "DigitalArt") String imageStyle,
    @Schema(description = "썸네일 URL", example = "http://example.com/thumbnail.png") String thumbnailUrl,
    @Schema(description = "산출물 URL", example = "http://example.com/artifact.zip") String artifactUrl,
    @Schema(description = "제목 URL", example = "http://example.com/title.png") String titleUrl,
    @Schema(description = "생성일시", example = "2025-03-31T12:00:00") LocalDateTime createdAt
) {
    /**
     * Project 엔티티를 ProjectResponse DTO로 변환합니다.
     *
     * @param project 변환할 Project 엔티티
     * @return 변환된 ProjectResponse DTO
     */
    public static ProjectResponse fromEntity(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .subtitle(project.getSubtitle())
                .description(project.getDescription())
                .thumbnailUrl(project.getThumbnailUrl())
                .titleUrl(project.getTitleUrl())
                .artifactUrl(project.getArtifactUrl())
                .scriptTone(project.getScriptTone())
                .imageStyle(project.getImageStyle())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
