package gettothepoint.unicatapi.filestorage.infrastructure.config;

import gettothepoint.unicatapi.filestorage.domain.policy.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.policy.StoredFileValidator;
import gettothepoint.unicatapi.filestorage.infrastructure.command.StoredFileFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 파일 저장 커맨드 관련 설정 클래스입니다.
 * 필요한 의존성을 주입하고 초기화합니다.
 */
@Configuration
@RequiredArgsConstructor
public class StoredFileConfig {

    private final StoredFileValidator storedFileValidator;
    private final FileNameTransformer fileNameTransformer;

    @PostConstruct
    public void init() {
        StoredFileFactory.configure(storedFileValidator, fileNameTransformer);
    }

}