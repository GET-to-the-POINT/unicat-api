package gettothepoint.unicatapi.domain.repository.storage;

import gettothepoint.unicatapi.common.util.FileUtil;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Repository("localRepository")
public class LocalRepository implements FileRepository {

    /**
     * 상대 경로(예: "bucketName/subdir/filename.ext")를 받아서,
     * FileUtil.getTempPath() 아래에 저장된 파일을 반환합니다.
     */
    public File findByKey(String relativePath) {
        Path localFilePath = FileUtil.getTempPath().resolve(relativePath);
        if (!Files.exists(localFilePath)) {
            throw new RuntimeException("파일이 존재하지 않습니다: " + localFilePath);
        }
        return localFilePath.toFile();
    }

    /**
     * MultipartFile을 받아서, 원본 파일명을 상대 경로로 사용해
     * FileUtil.getTempPath() 아래에 저장합니다.
     */
    public String save(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }
        // 여기서 원본 파일명을 그대로 상대 경로로 사용합니다.
        Path localFilePath = FileUtil.getTempPath().resolve(originalFilename);
        try {
            // 파일이 저장될 상위 디렉토리가 없으면 생성
            Files.createDirectories(localFilePath.getParent());
            // 파일 저장 (기존 파일이 있으면 덮어쓰기)
            Files.copy(file.getInputStream(), localFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        // 저장한 파일의 상대 경로(또는 원본 파일명)를 반환할 수 있습니다.
        return originalFilename;
    }

    @Override
    public String save(File file) {
        String originalFilename = file.getName();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }
        // 여기서 원본 파일명을 그대로 상대 경로로 사용합니다.
        Path localFilePath = FileUtil.getTempPath().resolve(originalFilename);
        try {
            // 파일이 저장될 상위 디렉토리가 없으면 생성
            Files.createDirectories(localFilePath.getParent());
            // 파일 저장 (기존 파일이 있으면 덮어쓰기)
            Files.copy(file.toPath(), localFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
        // 저장한 파일의 상대 경로(또는 원본 파일명)를 반환할 수 있습니다.
        return originalFilename;
    }
}