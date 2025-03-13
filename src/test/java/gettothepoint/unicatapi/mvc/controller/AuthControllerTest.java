package gettothepoint.unicatapi.mvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.presentation.controller.auth.AuthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import gettothepoint.unicatapi.application.service.AuthService;
import gettothepoint.unicatapi.domain.dto.oauth.OAuthLinkDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }
    }

    @Nested
    @DisplayName("OAuth Link Retrieval Test Cases")
    class OAuthLinkRetrievalTest {
        @Test
        @DisplayName("Valid OAuth Link Retrieval")
        void testValidOAuthLinkRetrieval() throws Exception {
            List<OAuthLinkDto> oAuthLinkDtos = List.of(
                    OAuthLinkDto.builder().provider("Google").link("https://example.com/oauth2/authorization/google").build(),
                    OAuthLinkDto.builder().provider("Facebook").link("https://example.com/oauth2/authorization/facebook").build()
            );
            doNothing().when(authService).getOAuthLinks();
            mockMvc.perform(get("/oauth-links")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(oAuthLinkDtos)))
                    .andExpect(status().isOk());
        }
        @Test
        @DisplayName("Invalid OAuth Link Retrieval")
        void testInvalidOAuthLinkRetrieval() throws Exception {
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OAuth provider"))
                    .when(authService).getOAuthLinks();
            mockMvc.perform(get("/oauth-links")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("JWK Set Retrieval Test Cases")
    class JWKSetRetrievalTest {
        @Test
        @DisplayName("Valid JWK Set Retrieval")
        void testValidJWKSetRetrieval() throws Exception {
            String jwkSet = """
            {
              "keys": [
                {
                  "kty": "RSA",
                  "e": "AQAB",
                  "use": "sig",
                  "kid": "rsa-key-id",
                  "alg": "RS256",
                  "n": "1n4iFl-u0JR0Fdm62KFkaRZv-i6o3fBGyPpwpuoNwP84hEDgwfuDlgN9S6Hqaz_GNDxZqRlAdzOFz4DRJQOo_fPh..."
                }
              ]
            }
            """;
            doNothing().when(authService).getJwks();
            mockMvc.perform(get("/.well-known/jwks.json")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jwkSet))
                    .andExpect(status().isOk());
        }
        @Test
        @DisplayName("Invalid JWK Set Retrieval")
        void testInvalidJWKSetRetrieval() throws Exception {
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JWK Set"))
                    .when(authService).getJwks();
            mockMvc.perform(get("/.well-known/jwks.json")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
