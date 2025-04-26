package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.MinioFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Path;
import java.util.UUID;

/**
 * <p>통합 저장소(Composite) – <b>Dev Profile</b> 환경 통합 테스트.</p>
 *
 * <ul>
 *   <li>활성화 모듈 : Minio + Local File Storage</li>
 *   <li>우선순위   : Minio (S3 HTTP)</li>
 *   <li>예상 프로토콜 : {@code http}</li>
 *   <li>공통 테스트 로직 : {@link FileStorageRepositoryIntegrationTestBase}</li>
 * </ul>
 *
 * <p>※ 주석과 어조는 공통 베이스 테스트와 일치하게 유지.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CompositeFileStorageConfig.class,
        MinioFileStorageConfig.class,
        LocalFileStorageConfig.class
})
@DisplayName("컴포지트 파일 저장소 Dev 환경 통합 테스트")
@ActiveProfiles("dev")
class CompositeFileStorageRepositoryDevIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest");

    @TempDir
    private static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);

        minio.start();
        registry.add("app.minio.bucket", () -> "test-bucket-" + UUID.randomUUID());
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }

    @Autowired
    private FileStorageRepository repository;

    // ======= FileStorageRepositoryIntegrationTestBase 구현 =======

    private static final String EXPECTED_PROTOCOL = "http";
    private static final String PROTOCOL_MESSAGE =
            "Dev 환경에서는 Minio 저장소가 우선순위를 가지므로 http 프로토콜이어야 함";

    @Override protected String getExpectedUrlProtocol()   { return EXPECTED_PROTOCOL; }
    @Override protected String getProtocolAssertionMessage() { return PROTOCOL_MESSAGE; }
    @Override protected FileStorageRepository getRepository() { return repository; }
}
