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
    public Optional<File> findFileByKey(Path keyPath) {
        return primary.findFileByKey(keyPath)
                .or(() -> secondary.findFileByKey(keyPath));
    }

    @Override
    public Optional<URI> findUriByKey(Path keyPath) {
        return secondary.findUriByKey(keyPath);
    }

    @Override
    public Path save(MultipartFile file) {
        Path keyPath = primary.save(file);
        Path keyPath2 = secondary.save(file);
        if (!keyPath.equals(keyPath2)) {
            throw new RuntimeException("파일 저장 실패: 키가 일치하지 않음");
        }
        return keyPath;
    }

    @Override
    public Path save(File file) {
        Path keyPath = primary.save(file);
        Path keyPath2 = secondary.save(file);
        if (!keyPath.equals(keyPath2 )) {
            throw new RuntimeException("파일 저장 실패: 키가 일치하지 않음");
        }
        return keyPath;
    }
}