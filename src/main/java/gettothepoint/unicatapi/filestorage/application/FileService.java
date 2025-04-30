package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.FileResource;
import gettothepoint.unicatapi.filestorage.persistence.FileStorageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Path EMPTY_PATH = Path.of("");

    private final FileStorageRepository fileStorageRepository;

    /**
     * 파일 키를 이용하여 파일 리소스를 조회합니다.
     *
     * @param fileKey 파일 키
     * @return 파일 리소스 (Optional)
     * @throws IllegalArgumentException 파일 키가 유효하지 않은 경우
     */
    public Optional<UrlResource> load(@NonNull String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            throw new IllegalArgumentException("유효하지 않은 다운로드 키입니다");
        }

        return fileStorageRepository.load(fileKey);
    }

    /**
     * 기본 경로에 MultipartFile을 업로드합니다.
     * 
     * @param file 업로드할 파일
     * @return 저장된 파일 식별자
     * @throws IllegalArgumentException 파일이 비어있거나 크기가 0인 경우
     */
    public String store(MultipartFile file) {
        return store(file, EMPTY_PATH);
    }

    public String store(String finename, byte[] content) {
        FileResource fileResource = new FileResource(finename, content);
        return fileStorageRepository.store(fileResource);
    }

    /**
     * 지정된 경로에 MultipartFile을 업로드합니다.
     * 
     * @param file 업로드할 파일
     * @param path 저장 경로
     * @return 저장된 파일 식별자
     * @throws IllegalArgumentException 파일이 비어있거나 크기가 0인 경우
     */
    public String store(@NonNull MultipartFile file, @NonNull Path path) {
        if (file.isEmpty() || file.getSize() == 0) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다");
        }

        FileResource fileResource = new FileResource(file, path);
        return fileStorageRepository.store(fileResource);
    }

    /**
     * 기본 경로에 File을 업로드합니다.
     * 
     * @param file 업로드할 파일
     * @return 저장된 파일 식별자
     * @throws IllegalArgumentException 파일이 존재하지 않거나, 읽을 수 없거나, 비어있는 경우
     */
    public String store(File file) {
        return store(file, EMPTY_PATH);
    }

    private String store(@NonNull File file, @NonNull Path path) {
        if (!file.exists() || file.length() == 0 || !file.canRead()) {
            throw new IllegalArgumentException("유효하지 않은 파일입니다");
        }

        FileResource fileResource = new FileResource(file, path);
        return fileStorageRepository.store(fileResource);
    }
}