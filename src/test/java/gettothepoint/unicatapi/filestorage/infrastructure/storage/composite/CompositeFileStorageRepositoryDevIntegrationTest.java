package gettothepoint.unicatapi.filestorage.infrastructure.storage.composite;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.MinioFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Path;
import java.util.UUID;


@SpringJUnitConfig(classes = {
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
