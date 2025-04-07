package gettothepoint.unicatapi.domain.dto.project.section;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 파일 업로드 없는 섹션 리소스 요청 DTO입니다.
 */
@Schema(description = "파일 업로드가 포함되지 않은 섹션 리소스 요청입니다.")
public record SectionResourceRequestWithoutFile(
    @Schema(description = "스크립트에 적용할 음성 톤을 지정합니다.", example = "kbwmGExKNkzJPdZeyZVATm") String voiceModel,
    @Schema(description = "이미지의 대체 설명입니다.", example = "Image alt text") String alt,
    @Schema(description = "스크립트 텍스트입니다.", example = "Script for the section") String script,
    @Schema(description = "섹션 전환 효과 사운드를 지정합니다.", example = "unicat/assets/transition/happy.mp3") String transitionKey
) {
}