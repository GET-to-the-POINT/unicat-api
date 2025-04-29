package gettothepoint.unicatapi.artifact.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 프로젝트 응답 DTO
 * 이 DTO는 프로젝트 정보를 클라이언트에 전달하기 위한 객체입니다.
 */
@Builder(access = AccessLevel.PACKAGE)
@Schema(description = "프로젝트 응답 DTO")
public record ProjectResponse(
    @Schema(description = "프로젝트 고유 ID", example = "1") Long id,
    @Schema(description = "프로젝트 제목", example = "백설공주") String title,
    @Schema(description = "프로젝트 부제목", example = "마녀의 이야기") String subtitle,
    @Schema(description = "프로젝트 설명", example = "이 프로젝트는 동화에 관한 동영상입니다.") String description,
    @Schema(description = "스크립트 톤", example = "Friendly") String scriptTone,
    @Schema(description = "이미지 스타일", example = "DigitalArt") String imageStyle,
    @Schema(description = "썸네일 URL", example = "https://unicat.com/thumbnail.png") String thumbnailUrl,
    @Schema(description = "산출물 URL", example = "https://unicat.com/artifact.zip") String artifactUrl,
    @Schema(description = "제목 URL", example = "https://unicat.com/title.png") String titleUrl,
    @Schema(description = "생성일시", example = "2025-03-31T12:00:00") LocalDateTime createdAt
) {
}
