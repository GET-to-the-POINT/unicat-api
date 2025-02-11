package taeniverse.ai_news.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import taeniverse.ai_news.mvc.model.dto.OAuthDTO;
import taeniverse.ai_news.mvc.model.dto.PrincipalDetails;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 특정 경로는 필터를 건너뜀
        if (requestUri.matches("^\\/login(?:\\/.*)?$") || requestUri.matches("^\\/oauth2(?:\\/.*)?$")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더 또는 쿠키에서 토큰 추출
        String token = getTokenFromHeaderOrCookie(request);
        if (token == null || jwtUtil.isExpired(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 사용자 정보 설정
        OAuthDTO user = jwtUtil.parseTokenToOAuthDTO(token);
        PrincipalDetails userDetails = new PrincipalDetails(user);
        Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 응답에 토큰 갱신
        jwtUtil.setJwtResponse(response, user.getUsername(), user.getUserId(), user.getRole());
        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeaderOrCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .orElseGet(() -> {
                    Cookie[] cookies = request.getCookies();
                    return cookies == null ? null :
                            Arrays.stream(cookies)
                                    .filter(cookie -> "Authorization".equals(cookie.getName()))
                                    .map(Cookie::getValue)
                                    .findFirst()
                                    .orElse(null);
                });
    }

}
