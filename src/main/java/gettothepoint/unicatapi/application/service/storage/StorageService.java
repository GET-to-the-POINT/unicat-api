package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface StorageService {
    UploadResult upload(MultipartFile file);

    UploadResult upload(File file);
    UploadResult upload(InputStream inputStream);

    List<InputStream> downloads(List<Integer> fileHashes);
    InputStream download(Integer fileHash);
    File downloadFile(Integer fileHash);
}