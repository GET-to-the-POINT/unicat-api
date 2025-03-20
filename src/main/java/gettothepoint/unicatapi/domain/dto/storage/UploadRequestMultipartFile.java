package gettothepoint.unicatapi.domain.dto.storage;

import org.springframework.web.multipart.MultipartFile;

public record UploadRequestMultipartFile(Integer fileHashCode, MultipartFile multipartFile, String mimeType) {
}
