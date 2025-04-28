package gettothepoint.unicatapi.filestorage.application.port.in;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.application.exception.FileUploadErrorCode;
import gettothepoint.unicatapi.filestorage.application.exception.FileUploadException;
import gettothepoint.unicatapi.filestorage.domain.model.FileResource;
import gettothepoint.unicatapi.filestorage.domain.service.FileResourceFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileUploadUseCase {
    private static final Path EMPTY_PATH = Path.of("");

    private final FileStorageRepository fileStorageRepository;
    private final FileResourceFactory fileResourceFactory;

    public String uploadFile(@NonNull MultipartFile file) {
        return uploadFile(file, EMPTY_PATH);
    }

    public String uploadFile(@NonNull MultipartFile file, @NonNull Path path) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw new FileUploadException(FileUploadErrorCode.EMPTY_FILE);
        }

        // 간소화된 StoredFile 생성 및 저장
        FileResource fileResource = fileResourceFactory.fromMultipartFile(file, path);
        return fileStorageRepository.store(fileResource);
    }

    public String uploadFile(@NonNull File file) {
        return uploadFile(file, EMPTY_PATH);
    }

    private String uploadFile(@NonNull File file, @NonNull Path path) {
        if (!file.exists() || file.length() == 0 || !file.canRead()) {
            throw new FileUploadException(FileUploadErrorCode.INVALID_FILE);
        }

        // 인스턴스 메서드 호출로 변경
        FileResource fileResource = fileResourceFactory.fromFile(file, path);
        return fileStorageRepository.store(fileResource);
    }
}