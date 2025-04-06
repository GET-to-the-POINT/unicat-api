package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supertone")
public record SupertoneProperties(String apiKey, String defaultVoiceId) {
}
