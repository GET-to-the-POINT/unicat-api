package gettothepoint.unicatapi.test.config;

import gettothepoint.unicatapi.application.service.voice.GoogleTextToSpeechService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDummyTextToSpeechConfiguration {

    @Bean
    @Primary
    public GoogleTextToSpeechService textToSpeechService() {
        return Mockito.mock(GoogleTextToSpeechService.class);
    }
}