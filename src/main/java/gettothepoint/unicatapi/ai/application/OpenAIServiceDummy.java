package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;
import org.springframework.stereotype.Service;

@Service
public class OpenAIServiceDummy implements OpenAIService {

    @Override
    public AIGenerate create(PromptRequest promptRequest) {
        return null;
    }
}
