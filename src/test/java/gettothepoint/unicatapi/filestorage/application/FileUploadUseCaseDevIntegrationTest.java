package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.application.port.in.FileUploadUseCase;
import gettothepoint.unicatapi.filestorage.infrastructure.config.CompositeFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.LocalFileStorageConfig;
import gettothepoint.unicatapi.filestorage.infrastructure.config.MinioFileStorageConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        FileUploadUseCase.class,
        CompositeFileStorageConfig.class,
        LocalFileStorageConfig.class,
        MinioFileStorageConfig.class,
})
@DisplayName("파일 업로드 유스케이스 Dev 환경 통합 테스트")
@Testcontainers
@ActiveProfiles("dev")
class FileUploadUseCaseDevIntegrationTest extends FileUploadUseCaseTestBase {

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest");

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // 로컬 저장소 설정
        registry.add("app.filestorage.local-root", tempDir::toAbsolutePath);

        // Minio 설정
        minio.start();
        registry.add("app.minio.bucket", () -> "test-bucket");
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }
}
