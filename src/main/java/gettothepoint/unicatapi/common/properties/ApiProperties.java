package gettothepoint.unicatapi.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api")
public record ApiProperties(String protocol, String domain, int port) {
}
