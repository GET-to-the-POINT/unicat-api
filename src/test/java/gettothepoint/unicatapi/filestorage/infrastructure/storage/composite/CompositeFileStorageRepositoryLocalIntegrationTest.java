package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Path;

/**
 * <p>통합 저장소(Composite) – <b>Local Profile</b> 환경 통합 테스트.</p>
 *
 * <ul>
 *   <li>활성화 모듈 : Local File Storage 만</li>
 *   <li>예상 프로토콜 : {@code file}</li>
 *   <li>공통 테스트 로직 : {@link FileStorageRepositoryIntegrationTestBase}</li>
 * </ul>
 *
 * <p>※ 공통 테스트 톤 &amp; 형식을 {@link FileStorageRepositoryIntegrationTestBase}와 동일하게 맞춘다.</p>
 */
@SpringJUnitConfig(classes = {
        CompositeFileStorageConfig.class,
        LocalFileStorageConfig.class
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

    // ======= FileStorageRepositoryIntegrationTestBase 구현 =======

    private static final String EXPECTED_PROTOCOL = "file";
    private static final String PROTOCOL_MESSAGE =
            "Local 환경에서는 로컬 파일 저장소만 활성화되므로 file 프로토콜이어야 함";

    @Override protected String getExpectedUrlProtocol()   { return EXPECTED_PROTOCOL; }
    @Override protected String getProtocolAssertionMessage() { return PROTOCOL_MESSAGE; }
    @Override protected FileStorageRepository getRepository() { return repository; }
}
