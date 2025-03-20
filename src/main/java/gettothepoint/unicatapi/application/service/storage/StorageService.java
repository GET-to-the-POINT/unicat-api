package gettothepoint.unicatapi.application.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface StorageService {
    String upload(MultipartFile file);

    Integer upload(File inputStream);
    Integer upload(InputStream inputStream);

    List<InputStream> downloads(List<Integer> fileHashes);
    InputStream download(Integer fileHash);
    File downloadFile(Integer fileHash);
}