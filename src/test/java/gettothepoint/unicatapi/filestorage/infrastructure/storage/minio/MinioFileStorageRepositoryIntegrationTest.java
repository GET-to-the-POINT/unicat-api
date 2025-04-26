package gettothepoint.unicatapi.filestorage.infrastructure.storage.minio;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.MinioFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.UUID;

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
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MinioFileStorageConfig.class})
@ActiveProfiles("dev")
@DisplayName("Minio 파일 저장소 통합 테스트 - Dev 프로파일")
class MinioFileStorageRepositoryIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @Autowired
    private FileStorageRepository repository;

    @DynamicPropertySource
    static void configureMinioProperties(DynamicPropertyRegistry registry) {
        minio.start();
        registry.add("app.minio.bucket", () -> "test-bucket-" + UUID.randomUUID());
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }

    // ======= FileStorageRepositoryIntegrationTestBase 구현 =======

    private static final String EXPECTED_PROTOCOL = "http";
    private static final String PROTOCOL_MESSAGE =
            "Dev 환경에서는 Minio 저장소가 우선순위를 가지므로 http 프로토콜이어야 함";

    @Override protected String getExpectedUrlProtocol()   { return EXPECTED_PROTOCOL; }
    @Override protected String getProtocolAssertionMessage() { return PROTOCOL_MESSAGE; }
    @Override protected FileStorageRepository getRepository() { return repository; }
}
