package gettothepoint.unicatapi.filestorage.infrastructure.config;

import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.infrastructure.exception.MinioFileStorageException;
import gettothepoint.unicatapi.filestorage.infrastructure.persistence.minio.MinioFileStorageRepository;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.MakeBucketArgs;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties(MinioFileStorageProperties.class)
@Profile({"dev", "prod"})
public class MinioFileStorageConfig {

    @Bean
    public MinioClient minioClient(MinioFileStorageProperties props) {
        MinioClient client = MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(props.accessKeyId(), props.secretAccessKey())
                .build();
        try {
            boolean found = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(props.bucket())
                            .build()
            );
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).build());
            }
        } catch (Exception e) {
            throw MinioFileStorageException.connectionError(e);
        }
        return client;
    }

    @Bean
    public FileStorageRepository minioFileStorageRepository(MinioClient minioClient, MinioFileStorageProperties props) {
        return new MinioFileStorageRepository(minioClient, props.bucket());
    }
}
