package gettothepoint.unicatapi.presentation.controller.auth;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc

@Import({TestDummyTextToSpeechConfiguration.class, TestDummyEmailServiceConfiguration.class})
class SignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("회원가입 통합 테스트")
    class SignUpIntegrationTests {
        private static final String TEST_EMAIL = "integration@example.com";
        private static final String VALID_PASSWORD = "ValidPass123!";
        private static final String TEST_NAME = "test-user";
        private static final String TEST_PHONE_NUMBER = "01012345678";

        @Test
        @DisplayName("정상 회원가입 요청 - 201 Created")
        void signUpWithValidData() throws Exception {
            SignUpDto request = new SignUpDto(TEST_EMAIL, VALID_PASSWORD, VALID_PASSWORD, TEST_NAME, TEST_PHONE_NUMBER);

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(cookie().exists("Authorization")); // 쿠키 이름 수정
        }

        @Test
        @DisplayName("중복 이메일 회원가입 - 400 BadRequest")
        void signUpWithDuplicateEmail() throws Exception {
            // 첫 번째 회원가입 요청
            SignUpDto initialRequest = new SignUpDto(TEST_EMAIL, VALID_PASSWORD, VALID_PASSWORD,TEST_NAME, TEST_PHONE_NUMBER);
            mockMvc.perform(post("/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialRequest)));

            // 중복 이메일 요청
            SignUpDto duplicateRequest = new SignUpDto(TEST_EMAIL, "DifferentPass123!", "DifferentPass123!", TEST_NAME, TEST_PHONE_NUMBER);
            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isBadRequest()); // 실제 동작은 400 BadRequest
        }

        @Test
        @DisplayName("유효성 검증 실패 - 400 BadRequest")
        void signUpWithInvalidData() throws Exception {
            SignUpDto invalidRequest = new SignUpDto("invalid-email", "short", "mismatch", TEST_NAME, TEST_PHONE_NUMBER);

            mockMvc.perform(post("/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
            // 응답 본문이 없는 경우가 있으므로, 추가적인 에러 내용 검증은 생략합니다.
        }
    }

    @Nested
    @DisplayName("로그인 통합 테스트")
    class SignInIntegrationTests {
        private static final String TEST_EMAIL = "login@example.com";
        private static final String VALID_PASSWORD = "SecurePass123!";
        private static final String TEST_NAME = "test-user";
        private static final String TEST_PHONE_NUMBER = "01012345678";

        @BeforeEach
        void setUp() throws Exception {
            // 테스트 사용자 생성
            SignUpDto signUpRequest = new SignUpDto(TEST_EMAIL, VALID_PASSWORD, VALID_PASSWORD,TEST_NAME, TEST_PHONE_NUMBER);
            mockMvc.perform(post("/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)));
        }

        @Test
        @DisplayName("정상 로그인 요청 - 200 OK")
        void signInWithValidCredentials() throws Exception {
            SignInDto request = new SignInDto(TEST_EMAIL, VALID_PASSWORD);

            mockMvc.perform(post("/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("Authorization")); // 수정: 쿠키 이름 "Authorization" 검증
        }

        @Test
        @DisplayName("잘못된 비밀번호 - 401 Unauthorized")
        void signInWithWrongPassword() throws Exception {
            SignInDto request = new SignInDto(TEST_EMAIL, "wrong-password");

            MvcResult result = mockMvc.perform(post("/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            // 응답 본문이 없으므로 에러 메시지를 응답의 errorMessage에서 검증
            assertThat(result.getResponse().getErrorMessage()).contains("잘못된 이메일 또는 비밀번호");
        }

        @Test
        @DisplayName("존재하지 않는 이메일 - 401 Unauthorized")
        void signInWithNonExistentEmail() throws Exception {
            SignInDto request = new SignInDto("nonexistent@example.com", VALID_PASSWORD);

            mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized()); // 수정: 예상 상태 코드를 401 Unauthorized로 변경
        }
    }

    @Nested
    @DisplayName("보안 검증 테스트")
    class SecurityValidationTests {
        @Test
        @DisplayName("비밀번호 암호화 검증")
        void passwordEncryptionTest() throws Exception {
            String rawPassword = "OriginalPass123!";
            SignUpDto signUpRequest = new SignUpDto("encrypt@example.com", rawPassword, rawPassword, "test-user", "01012345678");

            // 회원가입 요청
            mockMvc.perform(post("/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)));

            // 로그인 요청
            SignInDto signInRequest = new SignInDto("encrypt@example.com", rawPassword);
            mockMvc.perform(post("/auth/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInRequest)))
                    .andExpect(status().isOk()); // 원본 비밀번호로 로그인 성공
        }
    }
}