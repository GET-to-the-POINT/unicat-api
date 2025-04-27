package gettothepoint.unicatapi.filestorage.infrastructure.policy;

import gettothepoint.unicatapi.filestorage.domain.policy.FileNameValidator;
import gettothepoint.unicatapi.filestorage.domain.policy.StoredFileValidator;
import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageErrorCode;
import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 파일 저장 커맨드의 유효성을 검증하는 기본 구현체입니다.
 * 파일명, 파일 크기, 콘텐츠 타입 등 다양한 측면의 검증을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class DefaultStoredFileValidator implements StoredFileValidator {

    private static final Tika tika = new Tika();
    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
            ".jpg",  "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png",  "image/png",
            ".gif",  "image/gif",
            ".txt",  "text/plain"
    );

    private final FileNameValidator fileNameValidator;

    @Override
    public void validate(String filename, InputStream content, long size, String contentType) {
        fileNameValidator.validate(filename);
        validatePositiveSize(size);
        validateContent(content, filename, size, contentType);
    }

    private void validatePositiveSize(long size) {
        if (size <= 0) {
            throw new FileStorageException(FileStorageErrorCode.NON_POSITIVE_SIZE);
        }
    }

    private void validateContent(InputStream content, String filename, long expectedSize, String providedContentType) {
        try {
            if (!content.markSupported()) {
                throw new FileStorageException(FileStorageErrorCode.UNSUPPORTED_INPUTSTREAM);
            }
            content.mark(Integer.MAX_VALUE);

            long available = content.available();
            if (available != expectedSize) {
                throw new FileStorageException(FileStorageErrorCode.SIZE_MISMATCH, expectedSize, available);
            }

            String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : "";
            String expectedMimeType = ALLOWED_MIME_TYPES.get(ext);
            if (expectedMimeType == null) {
                throw new FileStorageException(FileStorageErrorCode.UNSUPPORTED_EXTENSION, ext);
            }

            String detectedMimeType = tika.detect(content, filename).split(";")[0].trim();

            if (!expectedMimeType.equals(detectedMimeType)) {
                throw new FileStorageException(FileStorageErrorCode.EXTENSION_MIMETYPE_MISMATCH, 
                        expectedMimeType, detectedMimeType);
            }
            if (!providedContentType.equals(detectedMimeType)) {
                throw new FileStorageException(FileStorageErrorCode.CONTENT_TYPE_MISMATCH, 
                        providedContentType, detectedMimeType);
            }

            content.reset();
        } catch (IOException e) {
            throw new FileStorageException(FileStorageErrorCode.IO_ERROR, e);
        }
    }
}
