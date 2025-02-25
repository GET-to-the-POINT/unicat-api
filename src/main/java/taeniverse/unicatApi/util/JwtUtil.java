package taeniverse.unicatApi.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.config.CookieProperties;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final CookieProperties jwtProperties;

    public void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = this.createJwtCookie(token);
        response.addCookie(jwtCookie);
    }

    private Cookie createJwtCookie(String token) {
        Cookie jwtCookie = new Cookie(jwtProperties.getName(), token);
        jwtCookie.setDomain(jwtProperties.getDomain());
        jwtCookie.setPath(jwtProperties.getPath());
        jwtCookie.setSecure(jwtProperties.isSecure());
        jwtCookie.setHttpOnly(jwtProperties.isHttpOnly());
        jwtCookie.setMaxAge(jwtProperties.getMaxAge());

        return jwtCookie;
    }
}