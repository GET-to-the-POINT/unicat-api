package gettothepoint.unicatapi.common.util;

import jakarta.servlet.http.Cookie;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CookieUtil {

    public static Cookie create(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public static void fire(Cookie redirectCookie) {
        redirectCookie.setMaxAge(0);
    }
}