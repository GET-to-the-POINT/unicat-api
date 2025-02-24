package taeniverse.unicatApi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LimitInterceptor limitInterceptor;

    @Value("${app.ui.protocol}")
    private String uiProtocol;
    @Value("${app.ui.domain}")
    private String uiDomain;
    @Value("${app.ui.port}")
    private String uiPort;

    public WebConfig(LimitInterceptor limitInterceptor) {
        this.limitInterceptor = limitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limitInterceptor)
                .addPathPatterns("/api/unicat");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins(uiProtocol + "://" + uiDomain + ":" + uiPort)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}
