package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.ai.domain.dto.AIGenerate;
import gettothepoint.unicatapi.ai.domain.dto.PromptRequest;

public interface OpenAIService {

    AIGenerate create(PromptRequest promptRequest);
}