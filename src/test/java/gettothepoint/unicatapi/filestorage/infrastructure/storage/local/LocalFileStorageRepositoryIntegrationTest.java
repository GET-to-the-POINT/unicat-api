package gettothepoint.unicatapi.filestorage.infrastructure.storage.local;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
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
        LocalFileStorageConfig.class
})
@DisplayName("Local 파일 저장소 통합 테스트")
class LocalFileStorageRepositoryIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    @Autowired
    private FileStorageRepository repository;

    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);
    }

    // ======= FileStorageRepositoryIntegrationTestBase 구현 =======

    private static final String EXPECTED_PROTOCOL = "file";
    private static final String PROTOCOL_MESSAGE =
            "Local 환경에서는 로컬 파일 저장소만 활성화되므로 file 프로토콜이어야 함";

    @Override protected String getExpectedUrlProtocol()   { return EXPECTED_PROTOCOL; }
    @Override protected String getProtocolAssertionMessage() { return PROTOCOL_MESSAGE; }
    @Override protected FileStorageRepository getRepository() { return repository; }
}
