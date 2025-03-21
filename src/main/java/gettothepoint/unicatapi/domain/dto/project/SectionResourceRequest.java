package gettothepoint.unicatapi.domain.dto.project;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record SectionResourceRequest(@NotNull MultipartFile multipartFile, String alt, String script) {
}
