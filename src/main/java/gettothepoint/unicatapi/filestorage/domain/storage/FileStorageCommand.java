package gettothepoint.unicatapi.filestorage.domain.storage;

import jakarta.validation.constraints.NotNull;

import java.io.InputStream;

/**
 * @param filename    저장할 이름
 * @param content     파일 데이터
 * @param size        바이트 단위 크기
 * @param contentType MIME 타입
 */
public record FileStorageCommand(@NotNull String filename, @NotNull InputStream content, @NotNull long size, @NotNull String contentType) {
    public FileStorageCommand {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        if (contentType == null) {
            throw new IllegalArgumentException("ContentType cannot be null");
        }
    }
}
