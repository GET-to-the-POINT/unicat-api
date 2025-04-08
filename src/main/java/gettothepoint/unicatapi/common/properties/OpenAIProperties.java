package gettothepoint.unicatapi.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAIProperties(Script script, Image image, Auto auto) {

    public record Script(String prompt, String model, double temperature) { }

    public record Image(String prompt, String model, String quality) {}

    public record Auto(String prompt) {}

}
