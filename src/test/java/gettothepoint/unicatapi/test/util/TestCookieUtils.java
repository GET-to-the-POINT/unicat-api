package gettothepoint.unicatapi.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.auth.presentation.SignInRequest;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestCookieUtils {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 주어진 이메일과 비밀번호로 로그인을 시도하고, JWT 쿠키를 반환합니다.
     *
     * @param email    로그인할 이메일
     * @param password 로그인할 비밀번호
     * @return JWT 쿠키
     * @throws Exception 로그인 요청 실패 시
     */
    public Cookie getJwtCookie(String email, String password) throws Exception {
        SignInRequest signInRequest = new SignInRequest(email, password);

        MockHttpServletRequestBuilder request = post("/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest));

        MockHttpServletResponse response = mockMvc.perform(request)
                .andReturn()
                .getResponse();

        return response.getCookie("JWT"); // JWT 쿠키 반환
    }

    /**
     * 주어진 이메일과 비밀번호로 로그인을 시도하고, JWT 쿠키를 문자열로 반환합니다.
     *
     * @param email    로그인할 이메일
     * @param password 로그인할 비밀번호
     * @return JWT 쿠키 값 (문자열)
     * @throws Exception 로그인 요청 실패 시
     */
    public String getJwtCookieValue(String email, String password) throws Exception {
        Cookie jwtCookie = getJwtCookie(email, password);
        return jwtCookie != null ? jwtCookie.getValue() : null;
    }

    /**
     * 주어진 이메일과 비밀번호로 로그인을 시도하고, JWT 쿠키가 유효한지 검증합니다.
     *
     * @param email    로그인할 이메일
     * @param password 로그인할 비밀번호
     * @return JWT 쿠키가 유효하면 true, 그렇지 않으면 false
     * @throws Exception 로그인 요청 실패 시
     */
    public boolean isJwtCookieValid(String email, String password) throws Exception {
        Cookie jwtCookie = getJwtCookie(email, password);
        return jwtCookie != null && !jwtCookie.getValue().isEmpty();
    }
}