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
    public Optional<File> findFileByRelativePath(Path relativePath) {
        Path absolutePath = FileUtil.getAbsolutePath(relativePath);
        File file = absolutePath.toFile();
        if (file.exists()) {
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<URI> findUriByRelativePath(Path relativePath) {
        return Optional.of(FileUtil.getAbsolutePath(relativePath).toUri());
    }

    public Path save(MultipartFile file) {
        Path hashedAbsolutePath = FileUtil.getAbsoluteHashedPath(file);
        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(hashedAbsolutePath.getParent());
            Files.copy(inputStream, hashedAbsolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        return FileUtil.getRelativePath(hashedAbsolutePath);
    }

    @Override
    public Path save(File file) {
        Path filePath = FileUtil.getAbsoluteHashedPath(file);

        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        return FileUtil.getRelativePath(filePath);
    }
}