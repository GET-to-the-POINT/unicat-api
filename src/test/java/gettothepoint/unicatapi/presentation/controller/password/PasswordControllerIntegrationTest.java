package gettothepoint.unicatapi.presentation.controller.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.domain.dto.password.AnonymousChangePasswordRequest;
import gettothepoint.unicatapi.domain.dto.password.AuthorizedChangePasswordRequest;
import gettothepoint.unicatapi.domain.dto.password.PasswordResetEmailRequest;
import gettothepoint.unicatapi.domain.dto.sign.SignUpRequest;
import gettothepoint.unicatapi.test.config.TestDummyEmailServiceConfiguration;
import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestDummyTextToSpeechConfiguration.class, TestDummyEmailServiceConfiguration.class})
@DisplayName("패스워드 컨트롤러 통합 테스트")
class PasswordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private final  String testEmail = "test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = signUp();
        assertThat(jwtToken).isNotNull();
    }

    private String signUp() throws Exception {
        String testPassword = "ValidPass123!";
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .confirmPassword(testPassword)
                .build();

        MvcResult result = mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("Authorization"))
                .andReturn();
        return Objects.requireNonNull(result.getResponse().getCookie("Authorization")).getValue();
    }

    @Nested
    @DisplayName("로그인한 사용자 (인증 헤더 기반) 비밀번호 재설정")
    class AuthorizedUserPasswordResetTests {

        @Test
        @DisplayName("정상 재설정 요청 - 200 OK")
        void resetPasswordForLoggedInUser() throws Exception {
            AuthorizedChangePasswordRequest request = AuthorizedChangePasswordRequest.builder()
                    .currentPassword("ValidPass123!")
                    .newPassword("NewSecur123!")
                    .confirmNewPassword("NewSecur123!")
                    .build();

            mockMvc.perform(put("/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("현재 비밀번호 불일치로 인한 재설정 실패 - 400 BadRequest")
        void resetPasswordForLoggedInUser_InvalidCurrentPassword() throws Exception {
            AuthorizedChangePasswordRequest request = AuthorizedChangePasswordRequest.builder()
                    .currentPassword("InvalidPass123!")
                    .newPassword("NewSecur123!")
                    .confirmNewPassword("NewSecur123!")
                    .build();

            mockMvc.perform(put("/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("비밀번호 확인 불일치로 인한 재설정 실패 - 400 BadRequest")
        void resetPasswordForLoggedInUser_PasswordMismatch() throws Exception {
            AuthorizedChangePasswordRequest request = AuthorizedChangePasswordRequest.builder()
                    .currentPassword("ValidPass123!")
                    .newPassword("NewPass123!")
                    .confirmNewPassword("Mismatch123!")
                    .build();

            mockMvc.perform(put("/members/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + jwtToken)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("비로그인 사용자 (토큰 기반) 비밀번호 재설정")
    class AnonymousUserPasswordResetTests {

        @Test
        @DisplayName("정상 재설정 요청 - 200 OK")
        void resetPasswordForNonLoggedInUser() throws Exception {
            AnonymousChangePasswordRequest request = AnonymousChangePasswordRequest.builder()
                    .token(jwtToken)
                    .newPassword("NewSecur123!")
                    .confirmNewPassword("NewSecur123!")
                    .build();

            // 토큰을 쿼리 파라미터로도 전달하는 시나리오
            mockMvc.perform(put("/members/anonymous/password")
                            .param("token", jwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 토큰으로 인한 재설정 실패 - 401 Unauthorized")
        void resetPasswordForNonLoggedInUser_InvalidToken() throws Exception {
            AnonymousChangePasswordRequest request = AnonymousChangePasswordRequest.builder()
                    .token("invalid-token")
                    .newPassword("NewPass123!")
                    .confirmNewPassword("NewPass123!")
                    .build();

            mockMvc.perform(put("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("비밀번호 확인 불일치로 인한 재설정 실패 - 400 BadRequest")
        void resetPasswordForNonLoggedInUser_PasswordMismatch() throws Exception {
            AnonymousChangePasswordRequest request = AnonymousChangePasswordRequest.builder()
                    .token(jwtToken)
                    .newPassword("NewPass123!")
                    .confirmNewPassword("Mismatch123!")
                    .build();

            mockMvc.perform(put("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("비밀번호 초기화 메일 발송")
    class AnonymousPasswordResetSendEmailTests {

        @Test
        @DisplayName("정상 재설정 요청 - 200 OK")
        void resetSendEmail() throws Exception {
            PasswordResetEmailRequest request = PasswordResetEmailRequest.builder()
                    .email(testEmail)
                    .url("https://www.naver.com")
                    .build();

            mockMvc.perform(post("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("이메일 검증 실패 - 400 bad request")
        void resetSendEmailError() throws Exception {
            PasswordResetEmailRequest request = PasswordResetEmailRequest.builder()
                    .email("fasdfasfd")
                    .url("https://www.naver.com")
                    .build();

            mockMvc.perform(post("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Url 검증 실패 - 400 bad request")
        void resetSendUrlError() throws Exception {
            PasswordResetEmailRequest request = PasswordResetEmailRequest.builder()
                    .email(testEmail)
                    .url("test")
                    .build();

            mockMvc.perform(post("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("email + Url 검증 실패 - 400 bad request")
        void resetSendUrlAndEmailError() throws Exception {
            PasswordResetEmailRequest request = PasswordResetEmailRequest.builder()
                    .email("so728")
                    .url("test")
                    .build();

            mockMvc.perform(post("/members/anonymous/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
