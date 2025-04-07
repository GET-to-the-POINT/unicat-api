package gettothepoint.unicatapi.domain.repository.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public interface FileRepository {
    Optional<File> findFileByRelativePath(Path relativePath);
    Optional<URI> findUriByRelativePath(Path relativePath);
    Path save(MultipartFile file);
    Path save(File file);
}