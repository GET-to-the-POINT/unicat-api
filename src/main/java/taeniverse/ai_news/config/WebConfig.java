package taeniverse.ai_news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LimitInterceptor limitInterceptor;

    @Value("${ai-news.frontend.protocol}")
    private String frontProtocol;
    @Value("${ai-news.frontend.domain}")
    private String frontDomain;
    @Value("${ai-news.frontend.port}")
    private String frontPort;

    public WebConfig(LimitInterceptor limitInterceptor) {
        this.limitInterceptor = limitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limitInterceptor)
                .addPathPatterns("/api/ai-news");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins(frontProtocol + frontDomain + ":" + frontPort)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
