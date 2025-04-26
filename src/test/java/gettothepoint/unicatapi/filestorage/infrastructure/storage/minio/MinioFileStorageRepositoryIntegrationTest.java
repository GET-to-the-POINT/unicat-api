package gettothepoint.unicatapi.filestorage.infrastructure.storage.minio;

import gettothepoint.unicatapi.filestorage.config.MinioTestConfig;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.FileStorageRepositoryIntegrationTestBase;
import gettothepoint.unicatapi.filestorage.infrastructure.storage.config.MinioFileStorageConfig;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.TEST_CONTENT;
import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.TEST_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MinioFileStorageConfig.class, MinioTestConfig.class})
@DisplayName("Minio 파일 저장소 테스트")
@ActiveProfiles("dev")
@Testcontainers
class MinioFileStorageRepositoryIntegrationTest extends FileStorageRepositoryIntegrationTestBase {

    private static final String TEST_BUCKET = "test-bucket-" + UUID.randomUUID();

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @Autowired
    private FileStorageRepository repository;
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${app.minio.bucket}")
    private String bucketName;

    @DynamicPropertySource
    static void configureMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("app.minio.bucket", () -> TEST_BUCKET);
        registry.add("app.minio.endpoint", minio::getS3URL);
        registry.add("app.minio.accessKeyId", minio::getUserName);
        registry.add("app.minio.secretAccessKey", minio::getPassword);
    }
    
    @Override
    protected String getExpectedUrlProtocol() {
        return "http";
    }
    
    @Override
    protected String getProtocolAssertionMessage() {
        return "Minio 저장소는 http 프로토콜을 사용해야 함";
    }
    
    @Override
    protected FileStorageRepository getRepository() {
        return repository;
    }

    @Nested
    @DisplayName("Minio 특화 테스트")
    class MinioSpecificTests {
        
        @Test
        @DisplayName("파일 메타데이터 검증")
        void fileMetadataShouldBeCorrect() {
            // Given
            String filename = "metadata-test-" + UUID.randomUUID() + ".txt";
            FileStorageCommand command = createTestFileCommand(filename, TEST_CONTENT);
            String key = repository.store(command);
            
            // When/Then - Minio API를 직접 사용하여 메타데이터 검증
            try {
                // 객체 정보 조회
                var statObjectResponse = minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(key)
                                .build()
                );
                
                // 메타데이터 검증
                assertThat(statObjectResponse.contentType()).isEqualTo(TEST_CONTENT_TYPE);
                assertThat(statObjectResponse.size()).isEqualTo(TEST_CONTENT.getBytes(StandardCharsets.UTF_8).length);
            } catch (Exception e) {
                fail("Minio 객체 정보 조회 실패: " + e.getMessage(), e);
            }
        }
        
        @Test
        @DisplayName("S3 API로 직접 파일 내용 확인")
        void contentReadDirectlyWithS3Api() {
            // Given
            String filename = "direct-access-test-" + UUID.randomUUID() + ".txt";
            String content = "직접 S3 API로 확인하는 콘텐츠";
            FileStorageCommand command = createTestFileCommand(filename, content);
            String key = repository.store(command);
            
            // When - Minio API를 직접 사용하여 파일 로드
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            )) {
                // Then
                String loadedContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(loadedContent).isEqualTo(content);
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                fail("Minio에서 직접 파일 읽기 실패: " + e.getMessage(), e);
            }
        }
    }
}
