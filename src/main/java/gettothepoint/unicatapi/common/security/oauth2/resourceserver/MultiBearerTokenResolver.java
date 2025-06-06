package gettothepoint.unicatapi.common.security.oauth2.resourceserver;

import gettothepoint.unicatapi.common.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MultiBearerTokenResolver implements BearerTokenResolver {

    private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
    private final JwtProperties jwtProperties;

    @Override
    public String resolve(HttpServletRequest request) {
        String token = defaultResolver.resolve(request);
        if (token != null) {
            return token;
        }

        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> jwtProperties.cookie().name().equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        return null;
    }
}