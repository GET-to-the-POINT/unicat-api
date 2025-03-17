package gettothepoint.unicatapi.domain.dto.project;

import org.springframework.web.multipart.MultipartFile;

public record UploadImageRequest(MultipartFile image, String alt) {
}
