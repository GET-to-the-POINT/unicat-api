package gettothepoint.unicatapi.filestorage.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class MinioTestConfig {

    @Container
    static final MinIOContainer minio = new MinIOContainer("minio/minio:latest");
    private static final String BUCKET = "test-bucket";

    @Bean
    @Primary
    public MinioFileStorageProperties minioFileStorageProperties() {
        minio.start();
        return new MinioFileStorageProperties(BUCKET, minio.getS3URL(), minio.getUserName(), minio.getPassword());
    }
}