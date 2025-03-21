package gettothepoint.unicatapi.domain.dto.storage;

import java.io.File;

public record UploadRequestFile(Integer fileHashCode, File file, String mimeType) {
}
