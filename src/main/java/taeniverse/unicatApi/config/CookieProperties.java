package taeniverse.unicatApi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.jwt.cookie")
public class CookieProperties {
    private String name;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;
    private int maxAge;
}