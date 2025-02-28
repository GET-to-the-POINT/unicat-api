package taeniverse.unicatApi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

        private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

        public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
            this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
            return customize(request, authorizationRequest);
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
            return customize(request, authorizationRequest);
        }

        private OAuth2AuthorizationRequest customize(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
            String requestUri = request.getRequestURI();
            String baseUri = "/oauth2/authorization/";
            String registrationId = requestUri.startsWith(baseUri) ? requestUri.substring(baseUri.length()) : null;

            if (authorizationRequest != null && "google".equals(registrationId)) {
                Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
                additionalParameters.put("access_type", "offline");
                additionalParameters.put("prompt", "consent");

                return OAuth2AuthorizationRequest.from(authorizationRequest)
                        .additionalParameters(additionalParameters)
                        .build();
            }
            return authorizationRequest;
        }
    }

