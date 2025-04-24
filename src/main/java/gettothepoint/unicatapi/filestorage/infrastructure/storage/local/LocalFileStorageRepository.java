package gettothepoint.unicatapi.filestorage.infrastructure.storage.local;

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
        Path p = root.resolve(c.filename()).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("잘못된 경로: " + c.filename());
        }
        try (InputStream is = c.content(); OutputStream os = Files.newOutputStream(p)) {
            is.transferTo(os);
        } catch (IOException e) {
            throw new RuntimeException("저장 실패", e);
        }
        return c.filename();
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