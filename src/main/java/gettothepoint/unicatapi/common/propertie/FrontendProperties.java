package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String url) {
}
