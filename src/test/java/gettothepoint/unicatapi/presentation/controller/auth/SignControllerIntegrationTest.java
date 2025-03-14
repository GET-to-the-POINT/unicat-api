package gettothepoint.unicatapi.presentation.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.config.DummyTextToSpeechConfiguration;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(DummyTextToSpeechConfiguration.class)
class SignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 - 유효한 데이터로 회원가입 성공")
    void signUpWithValidData() throws Exception {
        SignUpDto signUpDto = SignUpDto.builder()
                .email("integration@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 - 중복 이메일로 회원가입 실패")
    void signUpWithDuplicateEmail() throws Exception {
        // 첫 번째 회원가입 - 성공
        SignUpDto signUpDto = SignUpDto.builder()
                .email("duplicate@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated());

        // 동일 이메일로 다시 회원가입 시도 - 중복으로 인해 실패(BadRequest)
        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 - 유효한 데이터로 로그인 성공")
    void signInWithValidData() throws Exception {
        // 먼저 회원가입을 진행해야 로그인 테스트가 가능함
        SignUpDto signUpDto = SignUpDto.builder()
                .email("login@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated());

        // 회원가입한 사용자로 로그인 테스트
        SignInDto signInDto = SignInDto.builder()
                .email("login@example.com")
                .password("Password1@")  // 실제 로그인 시에도 동일한 비밀번호 사용
                .build();

        mockMvc.perform(post("/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 - 잘못된 자격 증명으로 로그인 실패")
    void signInWithInvalidCredentials() throws Exception {
        // 회원가입을 먼저 진행하여 테스트 환경을 구성
        SignUpDto signUpDto = SignUpDto.builder()
                .email("invalid@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated());

        // 올바르지 않은 비밀번호로 로그인 시도
        SignInDto signInDto = SignInDto.builder()
                .email("invalid@example.com")
                .password("WrongPassword1@")
                .build();

        mockMvc.perform(post("/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInDto)))
                .andExpect(status().isUnauthorized());
    }
}