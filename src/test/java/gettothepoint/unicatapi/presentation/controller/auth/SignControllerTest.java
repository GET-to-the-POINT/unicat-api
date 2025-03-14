package gettothepoint.unicatapi.presentation.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.application.service.AuthService;
import gettothepoint.unicatapi.application.service.MemberService;
import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
import gettothepoint.unicatapi.domain.dto.sign.SignUpDto;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignController.class)
@Import(SignControllerTest.TestConfig.class)
class SignControllerTest {

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
        public MemberService memberService() {
            return Mockito.mock(MemberService.class);
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }
    }

    @Nested
    @DisplayName("회원가입 테스트 케이스")
    class SignUpTest {
        @Test
        @DisplayName("유효한 회원가입")
        void testSignUpWithValidData() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().email("test@example.com").password("Password1@").confirmPassword("Password1@").build();
            doNothing().when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isCreated());
        }
        @Test
        @DisplayName("빈 데이터 회원가입")
        void testSignUpWithEmptyData() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required"))
                    .when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        @DisplayName("이메일 누락 회원가입")
        void testSignUpWithMissingEmail() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().password("password").confirmPassword("password").build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"))
                    .when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        @DisplayName("비밀번호 누락 회원가입")
        void testSignUpWithMissingPassword() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().email("test@example.com").confirmPassword("password").build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required"))
                    .when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        @DisplayName("중복 이메일 회원가입")
        void testSignUpWithDuplicateEmail() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().email("duplicate@example.com").password("Password1@").confirmPassword("Password1@").build();
            doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use"))
                    .when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isConflict());
        }
        @Test
        @DisplayName("비밀번호 불일치 회원가입")
        void testSignUpWithPasswordMismatch() throws Exception {
            SignUpDto signUpDto = SignUpDto.builder().email("test@example.com").password("password").confirmPassword("differentPassword").build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match"))
                    .when(authService).signUp(any(SignUpDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signUpDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그인 테스트 케이스")
    class SignInTest {
        @Test
        @DisplayName("유효한 로그인")
        void testSignInWithValidData() throws Exception {
            SignInDto signInDto = SignInDto.builder().email("test@example.com").password("password").build();
            doNothing().when(authService).signIn(any(SignInDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInDto)))
                    .andExpect(status().isOk());
        }
        @Test
        @DisplayName("이메일 누락 로그인")
        void testSignInWithMissingEmail() throws Exception {
            SignInDto signInDto = SignInDto.builder().password("password").build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"))
                    .when(authService).signIn(any(SignInDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInDto)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        @DisplayName("비밀번호 누락 로그인")
        void testSignInWithMissingPassword() throws Exception {
            SignInDto signInDto = SignInDto.builder().email("test@example.com").build();
            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required"))
                    .when(authService).signIn(any(SignInDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInDto)))
                    .andExpect(status().isBadRequest());
        }
        @Test
        @DisplayName("잘못된 자격 증명 로그인")
        void testSignInWithInvalidCredentials() throws Exception {
            SignInDto signInDto = SignInDto.builder().email("test@example.com").password("wrongpassword").build();
            doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"))
                    .when(authService).signIn(any(SignInDto.class), any(HttpServletResponse.class));
            mockMvc.perform(post("/sign-in")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signInDto)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
