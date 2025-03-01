package taeniverse.unicatApi.component.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.component.propertie.CookieProperties;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MultiBearerTokenResolver implements BearerTokenResolver {

    private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
    private final CookieProperties cookieProperties;

    @Override
    public String resolve(HttpServletRequest request) {String token = defaultResolver.resolve(request);
        if (token != null) {
            return token;
        }

        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieProperties.getName().equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        return null;
    }
}