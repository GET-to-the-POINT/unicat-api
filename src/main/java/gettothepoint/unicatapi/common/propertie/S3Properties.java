package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(String bucket, String endpoint, String region, String accessKeyId, String secretAccessKey) {
}
