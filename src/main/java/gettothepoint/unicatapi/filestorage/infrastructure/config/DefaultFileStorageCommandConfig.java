package gettothepoint.unicatapi.filestorage.infrastructure.config;

import gettothepoint.unicatapi.filestorage.domain.storage.FileNameTransformer;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommandValidator;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.DefaultFileStorageCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 파일 저장 커맨드 관련 설정 클래스입니다.
 * 필요한 의존성을 주입하고 초기화합니다.
 */
@Configuration
@RequiredArgsConstructor
public class DefaultFileStorageCommandConfig {

    private final FileStorageCommandValidator fileStorageCommandValidator;
    private final FileNameTransformer fileNameTransformer;

    @PostConstruct
    public void init() {
        DefaultFileStorageCommand.configure(fileStorageCommandValidator, fileNameTransformer);
    }

}