package gettothepoint.unicatapi.domain.repository.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class CacheRepository implements FileRepository {

    private final FileRepository primary;
    private final FileRepository secondary;

    public CacheRepository(@Qualifier("localRepository") FileRepository primary,
                           @Qualifier("s3Repository") FileRepository secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public List<Optional<File>> findFileAll(List<Path> relativePaths) {
        return relativePaths.stream()
                .map(this::findFileByKey)
                .toList();
    }

    @Override
    public Optional<File> findFileByKey(Path relativePath) {
        return primary.findFileByKey(relativePath)
                .or(() -> secondary.findFileByKey(relativePath));
    }

    @Override
    public Optional<URI> findUriByKey(Path relativePath) {
        return primary.findUriByKey(relativePath)
                .or(() -> secondary.findUriByKey(relativePath));
    }

    @Override
    public Path save(MultipartFile file) {
        Path key = primary.save(file);
        secondary.save(file);
        return key;
    }

    @Override
    public Path save(File file) {
        Path key = primary.save(file);
        secondary.save(file);
        return key;
    }
}