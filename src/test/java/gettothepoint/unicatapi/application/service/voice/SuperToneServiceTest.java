package gettothepoint.unicatapi.application.service.voice;

import gettothepoint.unicatapi.common.properties.SupertoneProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SuperToneServiceTest.TestConfig.class, SuperToneService.class})
class SuperToneServiceTest {

    @Autowired
    SuperToneService superToneService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SupertoneProperties supertoneProperties(@Value("${app.supertone.api-key:default-api-key}") String apiKey,
                                                       @Value("${app.supertone.default-voice-id:d9Hi4iF7HEXpGWo6cC5YbZ}") String voiceId) {
            return new SupertoneProperties(apiKey, voiceId);
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Test
    void testCreateIntegration() {

        String script = "안녕하세요, 테스트입니다.";
        File resultFile = superToneService.create(script, null);

        assertNotNull(resultFile, "생성된 파일이 null이어서는 안 됩니다.");
        assertTrue(resultFile.exists(), "파일이 실제로 존재해야 합니다.");
        assertTrue(resultFile.length() > 0, "다운로드된 파일의 크기는 0보다 커야 합니다.");

        if (!resultFile.delete()) {
            System.err.println("테스트 후 파일 삭제 실패: " + resultFile.getAbsolutePath());
        }
    }
}