package taeniverse.unicatApi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SwaggerConfig {

    @Value("${app.api.domain}")
    private String PROD_HOST;

    @Value("${app.api.port}")
    private String PROD_PORT;

    @Value("${app.api.protocol}")
    private String PROD_PROTOCOL;

    @Bean
    public OpenAPI customOpenAPI(Environment environment) {
        String securitySchemeName = "bearerAuth";

            OpenAPI openAPI = new OpenAPI()
                    .info(new Info()
                            .title("unicat-api")
                    )
                    .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                    .components(new Components().addSecuritySchemes(securitySchemeName,
                            new SecurityScheme()
                                    .name(securitySchemeName)
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));

            if (environment.matchesProfiles("prod")) {
                openAPI.addServersItem(
                        new Server().url(PROD_PROTOCOL + "://" + PROD_HOST + ":" + PROD_PORT)
                );
            }

            return openAPI;
    }
}
