package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.storage.StoredFile;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileUploadUseCase {

    private static final Path EMPTY_PATH = Path.of("");

    private final FileStorageRepository fileStorageRepository;

    public String uploadFile(@NotNull File file) {
        return uploadFile(file, EMPTY_PATH);
    }

    private String uploadFile(@NotNull File file, @NotNull Path path) {
        if (!file.exists()) {
            throw new IllegalArgumentException("업로드할 파일이 존재하지 않습니다.");
        }

        if (file.length() == 0) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        if (!file.canRead()) {
            throw new IllegalArgumentException("업로드할 파일을 읽을 수 없습니다.");
        }

        try {
            Tika tika = new Tika();
            String contentType = tika.detect(file);
            if (contentType == null || contentType.isEmpty()) {
                throw new IllegalArgumentException("파일의 MIME 타입을 감지할 수 없습니다.");
            }
            StoredFile storedFile = StoredFile.fromFile(file, contentType, path);
            return fileStorageRepository.store(storedFile.toCommand());
        } catch (Exception e) {
            throw new IllegalArgumentException("파일의 MIME 타입을 감지하는 중 오류가 발생했습니다.", e);
        }

    }

    public String uploadFile(@NotNull MultipartFile file) {
        return uploadFile(file, EMPTY_PATH);
    }

    /**
     * 파일을 저장하고 파일 키를 반환합니다.
     *
     * @param file 업로드할 파일
     * @param path 버킷 내 디렉토리 경로 (예: "images/", "docs/")
     * @return 저장된 파일의 키
     */
    public String uploadFile(@NotNull MultipartFile file, @NotNull Path path) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        if (file.getSize() == 0) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        String fileContentType = file.getContentType();

        if (fileContentType == null || fileContentType.isEmpty()) {
            throw new IllegalArgumentException("파일의 MIME 타입을 감지할 수 없습니다.");
        }

        try {
            Tika tika = new Tika();
            String contentType = tika.detect(file.getInputStream());
            if (contentType == null || contentType.isEmpty()) {
                throw new IllegalArgumentException("파일의 MIME 타입을 감지할 수 없습니다.");
            }

            if (!contentType.equals(fileContentType)) {
                throw new IllegalArgumentException("파일의 MIME 타입이 일치하지 않습니다.");
            }

            StoredFile storedFile = StoredFile.fromMultipart(file, path);
            return fileStorageRepository.store(storedFile.toCommand());
        } catch (IOException e) {
            throw new IllegalArgumentException("파일의 MIME 타입을 감지하는 중 오류가 발생했습니다.", e);
        }
    }

}
