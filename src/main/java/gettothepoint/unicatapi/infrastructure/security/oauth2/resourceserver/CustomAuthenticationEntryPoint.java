package gettothepoint.unicatapi.infrastructure.security.oauth2.resourceserver;

import gettothepoint.unicatapi.common.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        Cookie jwtCookie = jwtUtil.removeJwtCookie();
        response.addCookie(jwtCookie);
        String errorMessage = messageSource.getMessage("error.jwt.invalid", null, "", request.getLocale());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
    }
}