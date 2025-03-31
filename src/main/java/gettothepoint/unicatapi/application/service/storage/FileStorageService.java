package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.repository.FileStorageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final FileStorageRepository fileStorageRepository;

    public FileStorageService(@Qualifier("supabaseS3FileStorageRepository") FileStorageRepository fileStorageRepository) {
        this.fileStorageRepository = fileStorageRepository;
    }

    public String storeMultipartFile(MultipartFile file) {
            Path baseDir = Paths.get(System.getProperty("java.io.tmpdir"), "unicat_uploads");
            try {
                Files.createDirectories(baseDir);
            } catch (IOException e) {
                throw new UncheckedIOException("임시 업로드 디렉토리 생성 실패", e);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
                throw new IllegalArgumentException("유효한 파일 이름(확장자 포함)을 제공해주세요.");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            File tmpFile;
            try {
                tmpFile = Files.createTempFile(baseDir, "upload-", extension).toFile();
                file.transferTo(tmpFile);
                return fileStorageRepository.save(tmpFile.getAbsolutePath());
            } catch (IOException e) {
                throw new UncheckedIOException("임시 파일 생성 실패", e);
            }
        }
}
