package gettothepoint.unicatapi.ai.application;

import org.springframework.stereotype.Service;

@Service
public class TTSServiceDummy implements TTSService {
    @Override
    public String create(String script, String voiceModel) {
        return null;
    }
}
