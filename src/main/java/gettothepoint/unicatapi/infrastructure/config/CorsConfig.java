package gettothepoint.unicatapi.infrastructure.config;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        AppProperties.Cors cors = appProperties.cors();
        registry.addMapping("/**")
                .allowedOrigins(cors.allowedOrigins())
                .allowedMethods(cors.allowedMethods())
                .allowedHeaders(cors.allowedHeaders())
                .allowCredentials(cors.allowCredentials())
                .maxAge(cors.maxAge());
    }
}