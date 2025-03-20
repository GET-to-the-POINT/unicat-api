package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.dto.storage.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface StorageService {
    UploadResult upload(MultipartFile file, String fileMimeType);
    UploadResult upload(File file, String fileMimeType);
    UploadResult upload(InputStream inputStream, String fileMimeType);

    List<InputStream> downloads(List<String> fileHashes);
    InputStream download(String fileHash);
    File downloadFile(String fileHash);
}