package gettothepoint.unicatapi.filestorage.infrastructure.persistence.local;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * 2) 로컬 파일 저장소
 */
@RequiredArgsConstructor
public class LocalFileStorageRepository implements FileStorageRepository {

    private final Path root;

    @Override
    public String store(FileStorageCommand c) {
        Path target = root.resolve(c.getFilename()).normalize();
        try {
            Files.createDirectories(target.getParent());

            try (InputStream is = c.getContent(); OutputStream os = Files.newOutputStream(target)) {
                is.transferTo(os);
            }
        } catch (IOException e) {
            throw new RuntimeException("저장 실패", e);
        }
        return c.getFilename();
    }

    @Override
    public Optional<UrlResource> load(String key) {
        try {
            Path target = root.resolve(key).normalize();
            if (!Files.exists(target) || !target.startsWith(root)) {
                return Optional.empty();
            }
            return Optional.of(new UrlResource(target.toUri().toURL()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}