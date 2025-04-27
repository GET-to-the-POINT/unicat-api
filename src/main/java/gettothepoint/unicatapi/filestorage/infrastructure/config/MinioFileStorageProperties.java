package gettothepoint.unicatapi.filestorage.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minio")
public record MinioFileStorageProperties(String bucket, String endpoint, String accessKeyId, String secretAccessKey) {
}
