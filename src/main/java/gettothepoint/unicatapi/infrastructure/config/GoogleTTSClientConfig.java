package gettothepoint.unicatapi.infrastructure.config;

import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleTTSClientConfig {

    @Bean
    public TextToSpeechClient googleTTSClient() throws IOException {
        return TextToSpeechClient.create();
    }
}
