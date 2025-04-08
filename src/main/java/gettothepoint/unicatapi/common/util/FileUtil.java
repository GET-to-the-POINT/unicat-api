package gettothepoint.unicatapi.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Function;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_BUCKET = "unicat";

    private static Path getTempPath() {
        return Paths.get(TEMP_DIR);
    }

    public static Path getRelativePath(Path absolutePath) {
        return getTempPath().relativize(absolutePath);
    }

    public static Path getAbsolutePath(Path relativePath) {
        return getTempPath().resolve(relativePath);
    }

    private static Path getAbsoluteUniquePath(String extension) {
        if (extension.indexOf('.') != 0) {
            extension = "." + extension;
        }
        String fileName = UUID.randomUUID().toString();
        String fileNameWithExtension = fileName + extension;
        return getTempPath().resolve(DEFAULT_BUCKET).resolve(fileNameWithExtension);
    }

    public static Path getAbsoluteHashedPath(File file) {
        String extension = guessContentTypeFromKey(file.toPath());
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return getHashedFilePath(inputStream, extension);
        } catch (IOException e) {
            log.error("Failed to hash file stream", e);
            throw new RuntimeException("Could not create hashed path from file", e);
        }
    }

    public static Path getAbsoluteHashedPath(MultipartFile file) {
        String extension = guessContentTypeFromKey(Path.of(file.getOriginalFilename()));
        try (InputStream inputStream = file.getInputStream()) {
            return getHashedFilePath(inputStream, extension);
        } catch (IOException e) {
            log.error("Failed to hash file stream", e);
            throw new RuntimeException("Could not create hashed path from stream", e);
        }
    }

    public static Path getHashedFilePath(InputStream inputStream, String extension) {
        if (extension.indexOf('.') != 0) {
            extension = "." + extension;
        }
        try {
            String hashedFileName = DigestUtils.sha256Hex(inputStream) + extension;
            return getTempPath().resolve(DEFAULT_BUCKET).resolve(hashedFileName);
        } catch (IOException e) {
            log.error("Failed to hash file stream", e);
            throw new RuntimeException("Could not create hashed path from stream", e);
        }
    }

    public static String guessContentTypeFromKey(Path key) {
        try {
            return Files.probeContentType(key);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public static File createFileWithHashedName(String extension, Function<Path, File> writeLogic) {
        Path absoluteUniquePath = getAbsoluteUniquePath(extension);
        File tempFile = writeLogic.apply(absoluteUniquePath);
        Path absoluteHashedPath = getAbsoluteHashedPath(tempFile);
        File hashedFile = absoluteHashedPath.toFile();

        if (!tempFile.renameTo(hashedFile)) {
            try {
                Files.copy(tempFile.toPath(), absoluteHashedPath);
                if (!tempFile.delete()) {
                    log.warn("임시 파일 삭제 실패: " + tempFile.getAbsolutePath());
                }
            } catch (IOException e) {
                log.error("파일을 해시 경로로 이동하는 중 오류 발생", e);
                throw new RuntimeException("파일 이동 실패", e);
            }
        }

        return hashedFile;
    }
}