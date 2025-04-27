package gettothepoint.unicatapi.filestorage.infrastructure.storage;

import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageErrorCode;
import gettothepoint.unicatapi.filestorage.domain.exception.FileStorageException;
import gettothepoint.unicatapi.filestorage.domain.storage.FileNameValidator;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 파일명 유효성 검증을 위한 기본 구현체입니다.
 * 경로 조작 방지, 금지된 문자 검사 등 파일명 관련 보안 검증을 수행합니다.
 */
@Component
public class DefaultFileNameValidator implements FileNameValidator {

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile("(^|[\\\\/])\\.\\.($|[\\\\/])");
    private static final Pattern LEADING_DOT_PATTERN = Pattern.compile("^\\.(?![\\\\/])");
    private static final Pattern MULTIPLE_DOTS_PATTERN = Pattern.compile("\\.\\.");
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[:*?\"<>|]");

    @Override
    public void validate(String filename) {
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
}
