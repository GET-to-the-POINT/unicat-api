package gettothepoint.unicatapi.domain.dto.project;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadResourceRequest(@NotNull MultipartFile image, String alt) {
}
