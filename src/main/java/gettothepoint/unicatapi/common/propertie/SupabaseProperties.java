package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase")
public record SupabaseProperties(String url, String key, Storage storage, S3 s3) {
    public record Storage(String bucket) {
    }
    public record S3(String endpoint, String region, String accessKeyId, String secretAccessKey) {
    }
}