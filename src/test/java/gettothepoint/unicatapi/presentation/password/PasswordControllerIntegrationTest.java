package gettothepoint.unicatapi.presentation.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpDto;
import gettothepoint.unicatapi.test.config.TestDummyEmailServiceConfiguration;
import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import({TestDummyTextToSpeechConfiguration.class, TestDummyEmailServiceConfiguration.class})
class PasswordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("비밀번호 검증 기능")
    class PasswordVerificationIntegrationTests {

        private static final String TEST_EMAIL = "verify@example.com";
        private static final String VALID_PASSWORD = "ValidPass123!";

        private String jwtToken;

        @BeforeEach
        void setUp() throws Exception {
            signUp();
            jwtToken = signInAndExtractJwt(TEST_EMAIL, VALID_PASSWORD);
            assertThat(jwtToken).isNotNull();
        }

        private void signUp() throws Exception {
            SignUpDto signUpRequest = new SignUpDto(PasswordVerificationIntegrationTests.TEST_EMAIL, PasswordVerificationIntegrationTests.VALID_PASSWORD, PasswordVerificationIntegrationTests.VALID_PASSWORD);
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isCreated());
        }

        private String signInAndExtractJwt(String email, String password) throws Exception {
            SignInDto signInRequest = new SignInDto(email, password);
            MvcResult result = mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInRequest)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("Authorization"))
                    .andReturn();
            return result.getResponse().getCookie("Authorization").getValue();
        }

        @Test
        @DisplayName("정상 비밀번호 검증 - 200 No Content")
        void verifyCorrectPassword() throws Exception {
            mockMvc.perform(post("/members/me/password/verify")
                            .header("Authorization", "Bearer " + jwtToken)
                            .param("currentPassword", VALID_PASSWORD))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 비밀번호 검증 - 401 Unauthorized")
        void verifyIncorrectPassword() throws Exception {
            // 잘못된 비밀번호 전송 시, 401 Unauthorized 상태와 에러 메시지 검증
            MvcResult result = mockMvc.perform(post("/members/me/password/verify")
                            .header("Authorization", "Bearer " + jwtToken)
                            .param("currentPassword", "WrongPass"))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            String errorMessage = result.getResponse().getErrorMessage();
            assertThat(errorMessage).contains("비밀번호가 일치하지 않습니다");
        }

        @Test
        @DisplayName("비밀번호 미입력 - 400 Bad Request")
        void verifyMissingPassword() throws Exception {
            // currentPassword 파라미터가 빈 값인 경우 400 Bad Request 상태 확인
            mockMvc.perform(post("/members/me/password/verify")
                            .header("Authorization", "Bearer " + jwtToken)
                            .param("currentPassword", ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("JWT 미포함 - 401 Unauthorized 또는 403 Forbidden")
        void verifyWithoutJwt() throws Exception {
            // JWT가 없는 경우, 인증 실패 상태(401 또는 403)가 반환되어야 함
            mockMvc.perform(post("/members/me/password/verify")
                            .param("currentPassword", VALID_PASSWORD))
                    .andExpect(status().is4xxClientError());
        }
    }
}
