package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase")
public record SupabaseProperties(String url, String key, Storage storage) {
    public record Storage(String bucket) {
    }
}