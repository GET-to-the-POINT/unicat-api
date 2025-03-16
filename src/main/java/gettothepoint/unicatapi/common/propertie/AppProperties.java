package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String name, Jwt jwt, Toss toss, Api api, Youtube youtube, Supabase supabase, Cors cors, Email email, TTS tts, OpenAI openAI) {

    public record Jwt(Resource privateKey, Resource publicKey, String keyId, Cookie cookie) {
        public record Cookie(String name, String domain, String path, boolean secure, boolean httpOnly, String sameSite, int maxAge) {
        }
    }

    public record Toss(String clientKey, String secretKey,String confirmUrl,String cancelUrl) {
    }

    public record Email(String from, String fromName) {
    }

    public record Youtube(String apiKey) {
    }

    public record Api(String protocol, String domain, int port) {
    }

    public record Supabase(String url, String key, Storage storage) {
        public record Storage(String bucket) {
        }
    }

    public record OpenAI(String prompt, String model, double temperature) {
    }

    public record Cors(String[] allowedOrigins, String[] allowedMethods, String[] allowedHeaders, boolean allowCredentials, long maxAge) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cors(
                    String[] origins, String[] methods, String[] headers, boolean credentials, long age
            ))) return false;
            return allowCredentials == credentials &&
                    maxAge == age &&
                    java.util.Arrays.equals(allowedOrigins, origins) &&
                    java.util.Arrays.equals(allowedMethods, methods) &&
                    java.util.Arrays.equals(allowedHeaders, headers);
        }

        @Override
        public int hashCode() {
            int result = java.util.Arrays.hashCode(allowedOrigins);
            result = 31 * result + java.util.Arrays.hashCode(allowedMethods);
            result = 31 * result + java.util.Arrays.hashCode(allowedHeaders);
            result = 31 * result + Boolean.hashCode(allowCredentials);
            result = 31 * result + Long.hashCode(maxAge);
            return result;
        }

        @Override
        public String toString() {
            return "Cors{" +
                    "allowedOrigins=" + java.util.Arrays.toString(allowedOrigins) +
                    ", allowedMethods=" + java.util.Arrays.toString(allowedMethods) +
                    ", allowedHeaders=" + java.util.Arrays.toString(allowedHeaders) +
                    ", allowCredentials=" + allowCredentials +
                    ", maxAge=" + maxAge +
                    '}';
        }
    }

    public record TTS(String filePath, String fileExtension) {}
}
