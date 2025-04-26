package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.config.LocalTestConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CompositeFileStorageConfig.class,
        LocalFileStorageConfig.class,
        LocalTestConfig.class
})
@DisplayName("컴포지트 파일 저장소 Local 환경 통합 테스트")
class CompositeFileStorageRepositoryLocalIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }

    @Autowired
    private FileStorageRepository repository;

    @Override
    protected String getExpectedUrlProtocol() {
        return "file";
    }
    
    @Override
    protected String getProtocolAssertionMessage() {
        return "Local 환경에서는 로컬 파일 저장소만 활성화되므로 file 프로토콜이어야 함";
    }
    
    @Override
    protected FileStorageRepository getRepository() {
        return repository;
    }
}
