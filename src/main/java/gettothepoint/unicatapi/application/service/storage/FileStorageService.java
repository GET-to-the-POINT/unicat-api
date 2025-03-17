package gettothepoint.unicatapi.application.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file);
}
