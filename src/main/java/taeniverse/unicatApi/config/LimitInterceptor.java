package taeniverse.unicatApi.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import taeniverse.unicatApi.mvc.model.entity.PostRequest;
import taeniverse.unicatApi.mvc.repository.PostRequestRepository;

import java.time.LocalDate;

@Component
public class LimitInterceptor implements HandlerInterceptor {

    private final JWTUtil jwtUtil;

    private final PostRequestRepository postRequestRepository;

    public LimitInterceptor(JWTUtil jwtUtil, PostRequestRepository postRequestRepository) {
        this.jwtUtil = jwtUtil;
        this.postRequestRepository = postRequestRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //쿠키찾기
        String token = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    token = cookie.getValue();
                }
            }
        }

        Long userId = jwtUtil.getUserId(token);

        //3번 이상 요청시 거절
        if (postRequestRepository.findByUserIdAndDate(userId, LocalDate.now()).size() > 2) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }

        PostRequest postRequest = new PostRequest();
        postRequest.setUserId(userId);
        postRequest.setDate(LocalDate.now());
        postRequestRepository.save(postRequest);

        return true;
    }
}
