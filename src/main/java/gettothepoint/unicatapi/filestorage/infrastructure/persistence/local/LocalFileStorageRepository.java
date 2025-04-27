package gettothepoint.unicatapi.filestorage.infrastructure.persistence.local;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
            throw new UncheckedIOException("로컬 저장소 디렉토리 생성 실패", e);
        }
    }

    @Override
    public String store(StoredFile file) {
        try {
            Path destination = root.resolve(file.filename());
            Files.createDirectories(destination.getParent());

            try (InputStream inputStream = file.content()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return file.filename();
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 중 오류 발생", e);
        }
    }

    @Override
    public Optional<UrlResource> load(String key) {
        try {
            Path file = root.resolve(key);
            UrlResource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return Optional.of(resource);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}