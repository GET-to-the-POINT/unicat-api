//package gettothepoint.unicatapi.presentation.controller.auth;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import gettothepoint.unicatapi.auth.application.AuthService;
//import gettothepoint.unicatapi.domain.dto.sign.SignInDto;
//import gettothepoint.unicatapi.auth.presentation.SignUpRequest;
//import gettothepoint.unicatapi.test.config.TestDummyTextToSpeechConfiguration;
//import gettothepoint.unicatapi.test.config.TestSecurityConfig;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.server.ResponseStatusException;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(SignController.class)
//@Import({TestSecurityConfig.class, TestDummyTextToSpeechConfiguration.class})
//class SignControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private AuthService authService;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Nested
//    @DisplayName("회원가입 테스트 케이스")
//    class SignUpTest {
//        @Test
//        @DisplayName("유효한 회원가입")
//        void testSignUpWithValidData() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().email("test@example.com").password("Password1@").confirmPassword("Password1@").build();
//            when(authService.signUp(any(SignUpRequest.class))).thenReturn("dummy-token");
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isCreated());
//        }
//        @Test
//        @DisplayName("빈 데이터 회원가입")
//        void testSignUpWithEmptyData() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required"))
//                    .when(authService).signUp(any(SignUpRequest.class));
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isBadRequest());
//        }
//        @Test
//        @DisplayName("이메일 누락 회원가입")
//        void testSignUpWithMissingEmail() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().password("password").confirmPassword("password").build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"))
//                    .when(authService).signUp(any(SignUpRequest.class));
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isBadRequest());
//        }
//        @Test
//        @DisplayName("비밀번호 누락 회원가입")
//        void testSignUpWithMissingPassword() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().email("test@example.com").confirmPassword("password").build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required"))
//                    .when(authService).signUp(any(SignUpRequest.class));
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isBadRequest());
//        }
//        @Test
//        @DisplayName("중복 이메일 회원가입")
//        void testSignUpWithDuplicateEmail() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().email("duplicate@example.com").password("Password1@").confirmPassword("Password1@").build();
//            doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use"))
//                    .when(authService).signUp(any(SignUpRequest.class));
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isConflict());
//        }
//        @Test
//        @DisplayName("비밀번호 불일치 회원가입")
//        void testSignUpWithPasswordMismatch() throws Exception {
//            SignUpRequest signUpRequest = SignUpRequest.builder().email("test@example.com").password("password").confirmPassword("differentPassword").build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match"))
//                    .when(authService).signUp(any(SignUpRequest.class));
//            mockMvc.perform(post("/auth/sign-up")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signUpRequest)))
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("로그인 테스트 케이스")
//    class SignInTest {
//        @Test
//        @DisplayName("유효한 로그인")
//        void testSignInWithValidData() throws Exception {
//            SignInDto signInDto = SignInDto.builder().email("test@example.com").password("password").build();
//            doNothing().when(authService).signIn(any(SignInDto.class));
//            mockMvc.perform(post("/auth/sign-in")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signInDto)))
//                    .andExpect(status().isOk());
//        }
//        @Test
//        @DisplayName("이메일 누락 로그인")
//        void testSignInWithMissingEmail() throws Exception {
//            SignInDto signInDto = SignInDto.builder().password("password").build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required"))
//                    .when(authService).signIn(any(SignInDto.class));
//            mockMvc.perform(post("/auth/sign-in")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signInDto)))
//                    .andExpect(status().isBadRequest());
//        }
//        @Test
//        @DisplayName("비밀번호 누락 로그인")
//        void testSignInWithMissingPassword() throws Exception {
//            SignInDto signInDto = SignInDto.builder().email("test@example.com").build();
//            doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required"))
//                    .when(authService).signIn(any(SignInDto.class));
//            mockMvc.perform(post("/auth/sign-in")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signInDto)))
//                    .andExpect(status().isBadRequest());
//        }
//        @Test
//        @DisplayName("잘못된 자격 증명 로그인")
//        void testSignInWithInvalidCredentials() throws Exception {
//            SignInDto signInDto = SignInDto.builder().email("test@example.com").password("wrongpassword").build();
//            doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"))
//                    .when(authService).signIn(any(SignInDto.class));
//            mockMvc.perform(post("/auth/sign-in")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(signInDto)))
//                    .andExpect(status().isUnauthorized());
//        }
//    }
//}
