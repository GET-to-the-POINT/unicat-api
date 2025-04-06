package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Component("localRepository")
@RequiredArgsConstructor
public class LocalRepository implements FileRepository {

    @Override
    public Optional<File> findFileByKey(Path relativePath) {
        Path absolutePath = FileUtil.getAbsolutePath(relativePath);
        File file = absolutePath.toFile();
        if (file.exists()) {
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<URI> findUriByKey(Path relativePath) {
        return Optional.empty();
    }

    public Path save(MultipartFile file) {
        String contentType = file.getContentType(); // "image/png"
        String extension = "";
        if (contentType != null && contentType.contains("/")) {
            extension = contentType.substring(contentType.indexOf("/") + 1); // → "png"
        }
        Path filePath = FileUtil.getUniqueFilePath(extension);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        return FileUtil.getRelativePath(filePath);
    }

    @Override
    public Path save(File file) {
        String originalFilename = file.getName();
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        Path filePath = FileUtil.getUniqueFilePath(extension);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        return FileUtil.getRelativePath(filePath);
    }
}