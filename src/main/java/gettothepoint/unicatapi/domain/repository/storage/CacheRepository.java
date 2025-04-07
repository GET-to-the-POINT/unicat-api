package gettothepoint.unicatapi.domain.repository.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

@Primary
@Component
public class CacheRepository implements FileRepository {

    private final FileRepository primary;
    private final FileRepository secondary;

    public CacheRepository(@Qualifier("localRepository") FileRepository primary,
                           @Qualifier("s3Repository") FileRepository secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public Optional<File> findFileByRelativePath(Path relativePath) {
        return primary.findFileByRelativePath(relativePath)
                .or(() -> secondary.findFileByRelativePath(relativePath));
    }

    @Override
    public Optional<URI> findUriByRelativePath(Path relativePath) {
        String activeProfile = System.getProperty("spring.profiles.active", "");
        if (activeProfile.isEmpty() || activeProfile.contains("local")) {
            return primary.findUriByRelativePath(relativePath);
        }
        return secondary.findUriByRelativePath(relativePath)
                .or(() -> primary.findUriByRelativePath(relativePath));
    }

    @Override
    public Path save(MultipartFile file) {
        Path key = primary.save(file);
        Path key2 = secondary.save(file);
        if (!key.equals(key2)) {
            throw new RuntimeException("파일 저장 실패: 키가 일치하지 않음");
        }
        return key;
    }

    @Override
    public Path save(File file) {
        Path key = primary.save(file);
        secondary.save(file);
        return key;
    }
}