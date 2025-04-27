package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CompositeFileStorageRepository implements FileStorageRepository {

    private final List<FileStorageRepository> delegates;

    @Override
    public String store(FileStorageCommand c) {
        delegates.forEach(delegate -> delegate.store(c));
        return c.getFilename();
    }

    @Override
    public Optional<UrlResource> load(String key) {
        for (FileStorageRepository delegate : delegates) {
            Optional<UrlResource> resource = delegate.load(key);
            if (resource.isPresent()) {
                return resource;
            }
        }
        return Optional.empty();
    }
}