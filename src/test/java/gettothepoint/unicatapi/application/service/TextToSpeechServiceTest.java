package gettothepoint.unicatapi.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TextToSpeechServiceTest {

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Test
    void testCreateTextToSpeech() {

        String inputText = "안녕하세요, 테스트 문구입니다.";

        byte[] audioData = textToSpeechService.createTextToSpeech(inputText, "ko-KR-Wavenet-A");

        assertNotNull(audioData, "생성된 오디오 데이터가 null이면 안 됩니다.");
        assertTrue(audioData.length > 0, "생성된 오디오 데이터의 길이는 0보다 커야 합니다.");
    }
}
