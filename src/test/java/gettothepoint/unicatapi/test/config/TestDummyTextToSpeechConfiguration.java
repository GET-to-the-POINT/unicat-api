package gettothepoint.unicatapi.test.config;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDummyTextToSpeechConfiguration {

    @Bean
    @Primary
    public TextToSpeechService textToSpeechService() {
        return Mockito.mock(TextToSpeechService.class);
    }
}