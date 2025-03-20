package gettothepoint.unicatapi.application.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface StorageService {
    String upload(MultipartFile file);
    String upload(File file);

    File download(String fileUrl);
    List<File> downloads(List<String> fileUrl);
}