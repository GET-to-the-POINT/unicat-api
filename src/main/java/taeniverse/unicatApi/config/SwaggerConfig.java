package taeniverse.unicatApi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Value("${app.api.domain}")
    private String PROD_HOST;

    @Value("${app.api.port}")
    private String PROD_PORT;

    @Value("${app.api.protocol}")
    private String PROD_PROTOCOL;

    @Value("${spring.security.oauth2.client.registration.google.client-id:12312312}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:123123}")
    private String clientSecret;

    @Bean
    public OpenAPI customOpenAPI(Environment environment) {
        String jwtSchemeName = "bearerAuth";
        String oauth2SchemeName = "oauth2";

        SecurityScheme oauth2Scheme = new SecurityScheme()
                .name(oauth2SchemeName)
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("/oauth2/authorization/google")
                                .tokenUrl("/login/oauth2/code/google")
                                .scopes(new Scopes()
                                        .addString("profile", "프로필 정보")
                                        .addString("email", "이메일 정보"))));

        // 개발 모드에서만 클라이언트 정보 추가
        if (environment.matchesProfiles("dev")) {
            Map<String, Object> extensions = new HashMap<>();
            extensions.put("x-client-id", clientId);
            extensions.put("x-client-secret", clientSecret);
            oauth2Scheme.setExtensions(extensions);
        }

        OpenAPI openAPI = new OpenAPI()
                .info(new Info().title("unicat-api"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(jwtSchemeName)
                        .addList(oauth2SchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName,
                                new SecurityScheme()
                                        .name(jwtSchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSecuritySchemes(oauth2SchemeName, oauth2Scheme));
        return openAPI;
    }
}