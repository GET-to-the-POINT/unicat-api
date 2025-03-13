package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String name, Jwt jwt, Toss toss, Api api, Youtube youtube, Supabase supabase, Cors cors,Email email) {

    public record Jwt(Resource privateKey, Resource publicKey, String keyId, Cookie cookie) {
        public record Cookie(String name, String domain, String path, boolean secure, boolean httpOnly, String sameSite, int maxAge) {
        }
    }

    public record Toss(String clientKey, String secretKey,String confirmUrl,String cancelUrl) {
    }

    public record Email(String replyTo) { }

    public record Youtube(String apiKey) {
    }

    public record Api(String protocol, String domain, int port) {
    }

    public record Supabase(String url, String key, Storage storage) {
        public record Storage(String bucket) {
        }
    }
    public record Cors(String[] allowedOrigins, String[] allowedMethods, String[] allowedHeaders, boolean allowCredentials, long maxAge) {
    }
}
