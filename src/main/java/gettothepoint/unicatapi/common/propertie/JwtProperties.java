package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(Resource privateKey, Resource publicKey, String keyId, Cookie cookie) {
    public record Cookie(String name, String domain, String path, boolean secure, boolean httpOnly, String sameSite, int maxAge) {}
}