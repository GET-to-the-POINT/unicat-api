package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import gettothepoint.unicatapi.common.properties.OpenAIProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(
        classes = {OpenAiChatAutoConfiguration.class, OpenAIServiceSpringAI.class})
@EnableConfigurationProperties(OpenAIProperties.class)
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test",
        "app.openai.script.prompt=test-prompt",
        "app.openai.script.model=gpt-4o-mini",
        "app.openai.script.temperature=0.7",
        "app.openai.image.prompt=다음 스크립트를 읽고, ‘%s’스타일로 아름답고 퀄리티 좋은 정밀한 이미지를 만들어줘. 부족한 부분이 있다면 알아서 채워줘. %n%n스크립트:%n%s",
        "app.openai.image.model=dall-e-3",
        "app.openai.image.quality=hd",
        "app.openai.auto.prompt=다음의 주제를 보고 %s에 맞춰서 5개의 섹션으로 나누어서 한 섹션마다 1~2문장의 스크립트를 만들어줘. 그걸 모두 합쳐서 짧은 영상으로 만들거야. %n%n주제:%s"
})
class OpenOpenAIServiceTest {

    @Autowired
    private OpenAIServiceSpringAI openAiService;

    @Test
    void testOpenAiService() {
        PromptRequest promptRequest = new PromptRequest("비행기가 추락하면서 발생하는 이야기", "비둘기 조종사가 곤란해 하는 톤");
        AIGenerate result = openAiService.create(promptRequest);
    }
}