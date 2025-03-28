package gettothepoint.unicatapi.infrastructure.security.oauth2.resourceserver;

import gettothepoint.unicatapi.common.propertie.JwtProperties;
import gettothepoint.unicatapi.common.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageSource messageSource;
    private final JwtProperties jwtProperties;
    private final CookieUtil cookieUtil;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        Cookie cookie = WebUtils.getCookie(request, jwtProperties.cookie().name());
        if (cookie != null) {
            cookieUtil.zeroAge(cookie);
            response.addCookie(cookie);
        }

        String errorMessage = messageSource.getMessage("error.jwt.invalid", null, "", request.getLocale());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
    }
}