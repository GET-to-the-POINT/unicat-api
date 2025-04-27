package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommandValidator;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageErrorCode;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class DefaultFileStorageCommandValidator implements FileStorageCommandValidator {

    private static final Tika tika = new Tika();
    private static final Map<String, String> ALLOWED_MIME_TYPES = Map.of(
            ".jpg",  "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png",  "image/png",
            ".gif",  "image/gif",
            ".txt",  "text/plain"
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|[\\\\/])\\.\\.($|[\\\\/])");
    private static final Pattern LEADING_DOT_PATTERN = Pattern.compile("^\\.(?![\\\\/])");
    private static final Pattern MULTIPLE_DOTS_PATTERN = Pattern.compile("\\.\\.");
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[:*?\"<>|]");

    @Override
    public void validate(String filename, InputStream content, long size, String contentType) {
        validateFilename(filename);
        validatePositiveSize(size);
        validateContent(content, filename, size, contentType);
    }

    private void validateFilename(String filename) {
        if (filename.isBlank()) {
            throw new FileStorageException(FileStorageErrorCode.EMPTY_FILENAME);
        }
        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            throw new FileStorageException(FileStorageErrorCode.PATH_TRAVERSAL_DETECTED, filename);
        }
        if (filename.startsWith("/") || filename.startsWith("\\") || filename.matches("^[a-zA-Z]:[\\\\/].*")) {
            throw new FileStorageException(FileStorageErrorCode.ABSOLUTE_PATH_DETECTED, filename);
        }
        if (LEADING_DOT_PATTERN.matcher(filename).find()) {
            throw new FileStorageException(FileStorageErrorCode.LEADING_DOT_FILENAME, filename);
        }
        if (MULTIPLE_DOTS_PATTERN.matcher(filename).find()) {
            throw new FileStorageException(FileStorageErrorCode.MULTIPLE_DOTS_DETECTED, filename);
        }
        if (FORBIDDEN_CHARS_PATTERN.matcher(filename).find()) {
            throw new FileStorageException(FileStorageErrorCode.FORBIDDEN_CHARACTERS, filename);
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (filename.endsWith(".") || filename.endsWith(" ")) {
                throw new FileStorageException(FileStorageErrorCode.WINDOWS_SPECIAL_RULE_VIOLATION, filename);
            }
        }
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
