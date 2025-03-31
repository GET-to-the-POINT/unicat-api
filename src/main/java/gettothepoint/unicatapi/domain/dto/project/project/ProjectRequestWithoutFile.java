package gettothepoint.unicatapi.domain.dto.project.project;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectRequestWithoutFile(
        @Schema(description = "스크립트 말투 (예: friendly, FORMAL)", example = "friendly", defaultValue = "friendly", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String scriptTone,

        @Schema(description = "이미지 스타일 (예: Anime, Digital Art)", example = "Illustration", defaultValue = "Photo", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String imageStyle,

        @Schema(description = "/assets?type=template 요청시 나오는 name값을 넣어주세요", example = "back2.mp4", defaultValue = "back2.mp4", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String templateName,

        @Schema(description = "프로젝트 설명", example = "이 프로젝트는 동화에 관한 동영상입니다.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description,

        @Schema(description = "프로젝트 제목", example = "백설 공주", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String title,

        @Schema(description = "프로젝트 부제목", example = "마녀의 이야기", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String subtitle
) {
    public static ProjectRequestWithoutFile basic() {
        return new ProjectRequestWithoutFile(
                null,
                null,
                "back2.mp4",
                null,
                null,
                null
        );
    }
}