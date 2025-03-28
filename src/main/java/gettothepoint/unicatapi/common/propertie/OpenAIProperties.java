package gettothepoint.unicatapi.common.propertie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openai")
public record OpenAIProperties(OpenAIScript openAIScript, OpenAIImage openAIImage, OpenAIAuto openAIAuto) {

    public record OpenAIScript(String prompt, String model, double temperature) {
    }

    public record OpenAIImage(String prompt, String model, String quality) {}

    public record OpenAIAuto(String prompt) {}

}
