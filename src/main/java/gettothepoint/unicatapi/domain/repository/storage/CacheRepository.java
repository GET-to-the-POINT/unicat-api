package gettothepoint.unicatapi.domain.repository.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static gettothepoint.unicatapi.common.util.FileUtil.getTempPath;

@RequiredArgsConstructor
public class CacheRepository implements FileRepository {

    private final FileRepository primary;   // 로컬 저장소 (캐시)
    private final FileRepository secondary; // 대체 저장소 (예: S3)

    @Override
    public File findByKey(String relativePath) {
        try {
            // 먼저 로컬 저장소(캐시)에서 조회
            return primary.findByKey(relativePath);
        } catch (RuntimeException e) {
            // 로컬에 파일이 없으면 대체 저장소에서 조회
            File file = secondary.findByKey(relativePath);
            // 조회된 파일을 로컬 캐시로 복사
            cacheFile(relativePath, file);
            return file;
        }
    }

    private void cacheFile(String relativePath, File sourceFile) {
        Path localFilePath = getTempPath().resolve(relativePath);
        try {
            Files.createDirectories(localFilePath.getParent());
            Files.copy(sourceFile.toPath(), localFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 캐싱 실패: " + localFilePath, e);
        }
    }

    @Override
    public String save(MultipartFile file) {
        String key = primary.save(file);
        secondary.save(file);
        return key;
    }

    @Override
    public String save(File file) {
        String key = primary.save(file);
        secondary.save(file);
        return key;
    }

    public List<File> findAll(List<String> relativePaths) {
        return relativePaths.stream()
                .map(this::findByKey)
                .toList();
    }
}