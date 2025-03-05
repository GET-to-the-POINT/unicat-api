package taeniverse.unicatApi.component.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(defaultResolver.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return customize(defaultResolver.resolve(request, clientRegistrationId), request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        if (authorizationRequest == null) {
            return null;
        }

        String redirect = request.getParameter("redirect");
        if (!StringUtils.hasText(redirect)) redirect = request.getHeader("Referer");
        if (!StringUtils.hasText(redirect)) redirect = request.getRequestURL().toString();

        if (redirect.endsWith("/login")) {
            String contextPath = request.getContextPath();
            redirect = (StringUtils.hasText(contextPath) ? contextPath : "") + "/";
        }

        String encodedRedirectUri = URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        String modifiedState = authorizationRequest.getState() + "|" + encodedRedirectUri;

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        return OAuth2AuthorizationRequest.from(authorizationRequest).state(modifiedState).additionalParameters(additionalParameters).build();
    }
}