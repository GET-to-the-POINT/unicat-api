package gettothepoint.unicatapi.artifact.domain.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.web.multipart.MultipartFile;

@Schema(description = "파일 업로드가 포함된 섹션 리소스 요청입니다.")
public record SectionResourceRequest(
    @Schema(description = "멀티파트 파일 업로드입니다.", nullable = true) MultipartFile multipartFile,
    @Schema(description = "스크립트 텍스트입니다.", example = "Script for the section", nullable = true) String script,
    @Schema(description = "스크립트에 적용할 음성 톤을 지정합니다.", example = "kbwmGExKNkzJPdZeyZVATm", nullable = true) String voiceModel,
    @Schema(description = "섹션 전환 효과 사운드를 지정합니다.", example = "unicat/assets/transition/happy.mp3", nullable = true) String transitionKey
) { }
