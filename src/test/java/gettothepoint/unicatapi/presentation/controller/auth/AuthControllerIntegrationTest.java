package gettothepoint.unicatapi.presentation.controller.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import gettothepoint.unicatapi.common.util.JwtUtil;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestDummyTextToSpeechConfiguration.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("JWK Set 조회 - 200 OK")
    void getJwks_returnsValidJwksSet() throws Exception {
         MvcResult result = mockMvc.perform(get("/auth/.well-known/jwks.json"))
                         .andExpect(status().isOk())
                         .andReturn();
         String responseBody = result.getResponse().getContentAsString();
         assertThat(responseBody).contains("keys");
    }

    @Test
    @DisplayName("OAuth 링크 조회 - 200 OK")
    void getOAuthHrefLinks_returnsListOfOAuthLinks() throws Exception {
         MvcResult result = mockMvc.perform(get("/auth/oauth-links"))
                         .andExpect(status().isOk())
                         .andReturn();
         String responseBody = result.getResponse().getContentAsString();
         assertThat(responseBody).contains("provider", "link");
    }

    @Test
    @DisplayName("토큰 리프레시 - 204 No Content")
    void refreshToken_returnsNoContent() throws Exception {
         String token = jwtUtil.generateJwtToken(1L, "test@example.com");
         mockMvc.perform(post("/auth/token/refresh")
                .cookie(new Cookie("Authorization", token)))
                .andExpect(status().isNoContent());
    }
}