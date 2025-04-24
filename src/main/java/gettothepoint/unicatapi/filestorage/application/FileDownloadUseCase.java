package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileDownloadUseCase {

    private final FileStorageRepository fileStorageRepository;

    /**
     * 파일 키를 이용하여 파일 리소스를 조회합니다.
     * 
     * @param fileKey 파일 키
     * @return 파일 리소스 (Optional)
     */
    public Optional<UrlResource> downloadFile(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            throw new IllegalArgumentException("파일 키가 유효하지 않습니다.");
        }
        
        return fileStorageRepository.load(fileKey);
    }
}
