package gettothepoint.unicatapi.filestorage.domain.service;

import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

public interface FileResourceFactory {
    FileResource fromMultipartFile(MultipartFile file, Path directory);

    FileResource fromFile(File file, Path directory);
}
