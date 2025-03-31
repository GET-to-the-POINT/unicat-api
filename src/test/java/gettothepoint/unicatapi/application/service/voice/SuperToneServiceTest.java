package gettothepoint.unicatapi.application.service.voice;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperToneServiceTest {

    private final String apiKey = System.getenv("SPRING_SUPERTONE_API_KEY");
    private final String defaultVoiceId = System.getenv("SPRING_SUPERTONE_DEFAULT_VOICE");

    private final String ttsFilePath = System.getProperty("user.dir") + "/build/tmp/test-tts/";

    private final RestTemplate restTemplate = new RestTemplate();
    /**
     * 실제 Supertone API를 호출하여 음성 파일을 생성하는 통합 테스트.
     * 테스트 환경에 맞게 아래 값들을 실제 값으로 수정하세요.
     */
    @Test
    void testCreateIntegration() {

        SuperToneService service = new SuperToneService(apiKey, defaultVoiceId, ttsFilePath, restTemplate);
        String script = "안녕하세요, 테스트입니다.";
        File resultFile = service.create(script, null);

        assertNotNull(resultFile, "생성된 파일이 null이어서는 안 됩니다.");
        assertTrue(resultFile.exists(), "파일이 실제로 존재해야 합니다.");
        assertTrue(resultFile.length() > 0, "다운로드된 파일의 크기는 0보다 커야 합니다.");

        if (!resultFile.delete()) {
            System.err.println("테스트 후 파일 삭제 실패: " + resultFile.getAbsolutePath());
        }
    }
}