package gettothepoint.unicatapi.domain.dto.project;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.multipart.MultipartFile;

@Schema(description = "파일 업로드가 포함된 섹션 리소스 요청입니다.")
public record SectionResourceRequest(
    @Schema(description = "멀티파트 파일 업로드입니다.") MultipartFile multipartFile,
    @Schema(description = "음성 모델 식별자입니다.", example = "model1") String voiceModel,
    @Schema(description = "대체 텍스트입니다.", example = "Image alt text") String alt,
    @Schema(description = "스크립트 텍스트입니다.", example = "Script for the section") String script,
    @Schema(description = "전환 이름입니다.", example = "fade") String transitionName
) {
}
