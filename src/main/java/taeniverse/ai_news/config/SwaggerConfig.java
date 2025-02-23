package taeniverse.ai_news.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SwaggerConfig {

    private static final String PROD_SERVER_URL = "https://api.ai-news.xiyo.dev";

    @Bean
    public OpenAPI customOpenAPI(Environment environment) {
        String securitySchemeName = "bearerAuth";

            OpenAPI openAPI = new OpenAPI()
                    .info(new Info()
                            .title("AI-NEWS-API")
                    )
                    .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                    .components(new Components().addSecuritySchemes(securitySchemeName,
                            new SecurityScheme()
                                    .name(securitySchemeName)
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));

            if (environment.matchesProfiles("prod")) {
                openAPI.addServersItem(new Server().url(PROD_SERVER_URL));
            }

            return openAPI;
    }
}
