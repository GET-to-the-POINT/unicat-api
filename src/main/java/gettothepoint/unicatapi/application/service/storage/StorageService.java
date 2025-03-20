package gettothepoint.unicatapi.application.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface StorageService {
    String upload(MultipartFile file);

    Integer upload(File inputStream);
    Integer upload(InputStream inputStream);

    File downloadFile(Integer fileHash);
    InputStream download(Integer fileHash);
    List<InputStream> downloads(List<Integer> fileHashes);
}