package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.repository.storage.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final FileRepository fileRepository;

    public File getFile(String key) {
        return fileRepository.findFileByKey(Path.of(key))
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + key));
    }

    public URI getUri(String key) {
        return fileRepository.findUriByKey(Path.of(key))
                .orElseThrow(() -> new IllegalArgumentException("URI not found: " + key));
    }

    public String save(MultipartFile file) {
        return fileRepository.save(file).toString();
    }

    public String save(File file) {
        return fileRepository.save(file).toString();
    }
}