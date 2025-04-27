package gettothepoint.unicatapi.filestorage.infrastructure.persistence.composite;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CompositeFileStorageRepository implements FileStorageRepository {

    private final List<FileStorageRepository> delegates;

    @Override
    public String store(StoredFile c) {
        delegates.forEach(delegate -> delegate.store(c));
        return c.filename();
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