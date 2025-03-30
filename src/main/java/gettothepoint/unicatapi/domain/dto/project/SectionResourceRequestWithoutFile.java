package gettothepoint.unicatapi.domain.dto.project;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 파일 업로드 없는 섹션 리소스 요청 DTO입니다.
 */
@Schema(description = "파일 업로드 없는 섹션 리소스 요청 DTO입니다.")
public record SectionResourceRequestWithoutFile(
    @Schema(description = "음성 모델 식별자입니다.", example = "model1") String voiceModel,
    @Schema(description = "대체 텍스트입니다.", example = "Image alt text") String alt,
    @Schema(description = "스크립트 텍스트입니다.", example = "Script for the section") String script,
    @Schema(description = "전환 이름입니다.", example = "fade") String transitionName
) {
}