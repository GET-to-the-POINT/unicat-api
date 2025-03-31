package gettothepoint.unicatapi.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository("localFileStorageRepository")
@RequiredArgsConstructor
public class LocalFileStorageRepository implements FileStorageRepository{

    @Override
    public String save(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            throw new RuntimeException("저장된 파일을 찾을 수 없습니다: " + filepath);
        }
        return file.getName();
    }
}
