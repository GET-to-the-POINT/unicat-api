package gettothepoint.unicatapi.domain.dto.project;

import org.springframework.web.multipart.MultipartFile;

public record SectionResourceRequest(MultipartFile multipartFile, String alt, String script) {
}
