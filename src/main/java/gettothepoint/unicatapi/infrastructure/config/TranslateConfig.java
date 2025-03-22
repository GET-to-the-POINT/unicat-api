package gettothepoint.unicatapi.infrastructure.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslateConfig {
    @Bean
    public Translate translateClient() {
        return TranslateOptions.getDefaultInstance().getService();
    }
}
