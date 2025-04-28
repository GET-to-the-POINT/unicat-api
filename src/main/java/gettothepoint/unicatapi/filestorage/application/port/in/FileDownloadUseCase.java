package gettothepoint.unicatapi.filestorage.application.port.in;

import gettothepoint.unicatapi.filestorage.application.exception.FileDownloadErrorCode;
import gettothepoint.unicatapi.filestorage.application.exception.FileDownloadException;
import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
     * @throws FileDownloadException 파일 키가 유효하지 않은 경우
     */
    public Optional<UrlResource> downloadFile(@NonNull String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new FileDownloadException(FileDownloadErrorCode.INVALID_DOWNLOAD_KEY);
        }

        return fileStorageRepository.load(fileKey);
    }
}