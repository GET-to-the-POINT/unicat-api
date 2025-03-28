package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.JwtProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;

    public Cookie createJwtCookie(String jwtToken) {
        String cookieName = jwtProperties.cookie().name();
        int maxAge = jwtProperties.cookie().maxAge();

        return this.create(cookieName, jwtToken, maxAge);
    }

    public Cookie create(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(jwtProperties.cookie().path());
        cookie.setSecure(jwtProperties.cookie().secure());
        cookie.setHttpOnly(jwtProperties.cookie().httpOnly());
        cookie.setDomain(jwtProperties.cookie().domain());
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public void zeroAge(Cookie cookie) {
        cookie.setMaxAge(0);
    }
}