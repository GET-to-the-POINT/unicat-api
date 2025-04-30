package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIServiceDummy implements OpenAIService {

    @Override
    public AIGenerate create(PromptRequest promptRequest) {
        List<String> scripts = List.of(
                "This is a dummy response for the prompt: " + promptRequest.prompt()
        );
        return new AIGenerate(scripts);
    }
}
