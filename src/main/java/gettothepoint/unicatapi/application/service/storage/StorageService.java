package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.repository.storage.CacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final CacheRepository cachedRepository;

    public File getFile(String relativePath) {
        return cachedRepository.findFileByKey(Path.of(relativePath))
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + relativePath));
    }

    public File getFile(Path relativePath) {
        return cachedRepository.findFileByKey(relativePath)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + relativePath));
    }

    public List<File> getAll(List<String> sectionKeys) {
        return cachedRepository.findFileAll(sectionKeys.stream()
                .map(Path::of)
                .toList()).stream()
                .map(file -> file.orElseThrow(() -> new IllegalArgumentException("File not found: " + file)))
                .toList();
    }

    public String save(MultipartFile file) {
        return cachedRepository.save(file).toString();
    }

    public String save(File file) {
        return cachedRepository.save(file).toString();
    }
}