package gettothepoint.unicatapi.test.config;

import gettothepoint.unicatapi.ai.application.TTSServiceGoogle;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDummyTextToSpeechConfiguration {

    @Bean
    @Primary
    public TTSServiceGoogle textToSpeechService() {
        return Mockito.mock(TTSServiceGoogle.class);
    }
}