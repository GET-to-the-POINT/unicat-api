package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.domain.FileResource;
import lombok.NonNull;
import org.springframework.core.io.UrlResource;

import java.util.Optional;

/**
 * 여러 저장소(S3, 로컬 등)에 대해 동일한 API로
 * - 저장(store)         : 파일을 기록하고 key 반환
 * - 조회(load)          : key로 파일(Resource) 가져오기
 * <p>
 * 도메인 계층은 저장소 종류를 모르고, 구현체만 교체 / 조합하면 된다.
 */
public interface FileStorageRepository {

    /**
     * 파일 저장 ―> 저장소 내부 식별자(key) 반환
     */
    String store(FileResource file);
    
    /**
     * key로 파일을 읽어 Resource 로 반환
     */
    Optional<UrlResource> load(@NonNull String key);
}