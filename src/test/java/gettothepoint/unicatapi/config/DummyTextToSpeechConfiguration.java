package gettothepoint.unicatapi.config;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DummyTextToSpeechConfiguration {

    @Bean
    @Primary
    public TextToSpeechService textToSpeechService() {
        return Mockito.mock(TextToSpeechService.class);
    }
}