package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.FileResource;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
            throw new IllegalStateException("로컬 저장소 디렉터리 생성 실패: " + root, e);
        }
    }

    @Override
    public String store(FileResource file) {
        try {
            Path destination = root.resolve(file.getFilename());
            
            // 부모 디렉토리가 존재하지 않을 경우 생성
            Path parent = destination.getParent();
            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    throw new UncheckedIOException("디렉터리 생성 실패: " + parent, e);
                }
            }

            try (InputStream inputStream = file.getContent()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            return file.getFilename();
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장 실패: " + file.getFilename(), e);
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