package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalStorageService extends AbstractStorageService {

    @Override
    protected File realDownload(String fileUrl) {
        File cacheFile = FileUtil.getFilePath(fileUrl);
        if (!cacheFile.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found for hash: " + fileUrl
            );
        }

        return cacheFile;
    }

    @Override
    public String upload(MultipartFile file) {
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
        } catch (IOException e) {
            throw new UncheckedIOException("임시 파일 생성 실패", e);
        }
        return tmpFile.getAbsolutePath();
    }

    @Override
    public String upload(File file) {
        File foundFile = FileUtil.getFile(file.getName());
        if (foundFile.exists()) {
            return foundFile.getAbsolutePath();
        }

        String tempPath = System.getProperty("java.io.tmpdir");
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        File tmpFile = FileUtil.createTempFile(tempPath, extension);
        return tmpFile.getAbsolutePath();
    }
}
