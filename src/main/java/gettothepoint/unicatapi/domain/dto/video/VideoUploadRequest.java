package gettothepoint.unicatapi.domain.dto.video;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "동영상 업로드 요청을 위한 DTO")
public class VideoUploadRequest {

    @NotBlank
    @Schema(description = "업로드할 동영상의 제목", example = "My Awesome Video")
    private String title;

    @Schema(description = "업로드할 동영상의 설명", example = "이것은 정말 멋진 비디오입니다.")
    private String description;

    @Pattern(regexp = "^(youtube|vimeo)$")
    @Schema(description = "동영상을 업로드할 플랫폼 (youtube 또는 vimeo)", example = "youtube")
    private String platform = "youtube";

    @Pattern(regexp = "^(public|private)$")
    @Schema(description = "동영상의 공개 상태 (public 또는 private)", example = "public")
    private String visibility = "public";
}