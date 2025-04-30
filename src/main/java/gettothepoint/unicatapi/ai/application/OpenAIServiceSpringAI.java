package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import gettothepoint.unicatapi.common.properties.OpenAIProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
@Primary
public class OpenAIServiceSpringAI implements OpenAIService {

    private final OpenAIProperties openAIProperties;
    private final OpenAiChatModel openAiChatModel;

    @Override
    public AIGenerate create(PromptRequest promptRequest) {
        String promptText = String.format(
                openAIProperties.auto().prompt(),
                promptRequest.tone(),
                promptRequest.prompt()
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openAIProperties.script().model())
                .temperature(openAIProperties.script().temperature())
                .build();

        Prompt prompt = new Prompt(promptText, options);

        return ChatClient.create(openAiChatModel).prompt()
                .user(prompt.getContents())
                .call()
                .entity(AIGenerate.class);
    }

}