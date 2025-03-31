package gettothepoint.unicatapi.domain.repository.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileRepository {
    File findByKey(String relativePath);
    String save(MultipartFile file);
    String save(File file);
}