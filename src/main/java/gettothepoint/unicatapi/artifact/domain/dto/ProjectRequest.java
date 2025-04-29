package gettothepoint.unicatapi.artifact.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(access = lombok.AccessLevel.PRIVATE)
public record ProjectRequest(
        @Schema(description = "스크립트 말투 (예: Friendly, Formal)", example = "Friendly", defaultValue = "Friendly", nullable = true)
        String scriptTone,

        @Schema(description = "이미지 스타일 (예: Anime, Digital Art)", example = "Illustration", defaultValue = "Photo", nullable = true)
        String imageStyle,

        @Schema(description = "프로젝트 설명", example = "이 프로젝트는 동화에 관한 동영상입니다.", nullable = true)
        String description,

        @Schema(description = "프로젝트 제목", example = "백설 공주", nullable = true)
        String title
) {
    public static ProjectRequest fromProjectRequestWithoutFile(ProjectRequestWithoutFile request) {
        return ProjectRequest.builder()
                .scriptTone(request.scriptTone())
                .imageStyle(request.imageStyle())
                .description(request.description())
                .title(request.title())
                .build();
    }
}