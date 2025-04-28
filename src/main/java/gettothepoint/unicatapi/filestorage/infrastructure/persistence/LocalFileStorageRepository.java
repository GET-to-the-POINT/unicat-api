package gettothepoint.unicatapi.filestorage.infrastructure.persistence;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.infrastructure.exception.LocalFileStorageException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class LocalFileStorageRepository implements FileStorageRepository {

    private final Path root;

    public LocalFileStorageRepository(Path root) {
        this.root = root;
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw LocalFileStorageException.directoryCreationFailed(root.toString(), e);
        }
    }

    @Override
    public String store(FileResource file) {
        try {
            Path destination = root.resolve(file.filename());
            
            // 부모 디렉토리가 존재하지 않을 경우 생성
            Path parent = destination.getParent();
            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    throw gettothepoint.unicatapi.filestorage.infrastructure.exception.LocalFileStorageException.directoryCreationFailed(parent.toString(), e);
                }
            }

            try (InputStream inputStream = file.content()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return file.filename();
        } catch (IOException e) {
            throw gettothepoint.unicatapi.filestorage.infrastructure.exception.LocalFileStorageException.fileIOError(file.filename(), e);
        }
    }

    @Override
    public Optional<UrlResource> load(@NotNull String key) {
        try {
            Path filePath = root.resolve(key);
            UrlResource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return Optional.of(resource);
            } else {
                return Optional.empty();
            }
        } catch (MalformedURLException e) {
            // 로깅만 하고 빈 Optional 반환
            // logger.warn("URL 생성 실패: {}", key, e);
            return Optional.empty();
        }
    }
}