package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final AppProperties appProperties;

    public Cookie createJwtCookie(String jwtToken) {
        String cookieName = appProperties.jwt().cookie().name();
        int maxAge = appProperties.jwt().cookie().maxAge();

        return this.create(cookieName, jwtToken, maxAge);
    }

    public Cookie create(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(appProperties.jwt().cookie().path());
        cookie.setSecure(appProperties.jwt().cookie().secure());
        cookie.setHttpOnly(appProperties.jwt().cookie().httpOnly());
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public void zeroAge(Cookie cookie) {
        cookie.setMaxAge(0);
    }
}