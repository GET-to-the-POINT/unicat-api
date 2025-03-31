package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.repository.storage.LocalRepository;
import gettothepoint.unicatapi.domain.repository.storage.S3Repository;
import gettothepoint.unicatapi.domain.repository.storage.CacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
public class StorageService {
    private final CacheRepository cachedRepository;

    public StorageService(LocalRepository localRepository, S3Repository s3Repository) {
        this.cachedRepository = new CacheRepository(localRepository, s3Repository);
    }

    public List<File> getAll(List<String> relativePaths) {
        return cachedRepository.findAll(relativePaths);
    }

    public File get(String relativePath) {
        return cachedRepository.findByKey(relativePath);
    }

    public String save(MultipartFile file) {
        return cachedRepository.save(file);
    }

    public String save(File file) {
        return cachedRepository.save(file);
    }
}