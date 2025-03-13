package gettothepoint.unicatapi.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.web.util.WebUtils;

import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CookieUtil {

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        return Optional.ofNullable(WebUtils.getCookie(request, name));
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie != null) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name).map(Cookie::getValue);
    }
}