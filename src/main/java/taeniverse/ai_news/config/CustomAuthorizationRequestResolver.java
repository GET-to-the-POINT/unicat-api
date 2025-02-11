package taeniverse.ai_news.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Authorization Request Resolver that allows us to embed a custom state (e.g. redirect_location).
 */
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // 기본 요청 URL "/oauth2/authorization"을 기반으로 DefaultResolver 구성
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest defaultRequest = defaultResolver.resolve(request);
        return createCustomAuthorizationRequest(request, defaultRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest defaultRequest = defaultResolver.resolve(request, clientRegistrationId);
        return createCustomAuthorizationRequest(request, defaultRequest);
    }

    /**
     * Default 요청에 custom state를 추가(또는 교체)한 OAuth2AuthorizationRequest를 생성한다.
     */
    private OAuth2AuthorizationRequest createCustomAuthorizationRequest(
            HttpServletRequest request,
            OAuth2AuthorizationRequest defaultRequest
    ) {
        if (defaultRequest == null) {
            return null;
        }

        String redirectLocation = request.getParameter("redirect_location");
        if (redirectLocation == null || redirectLocation.isEmpty()) {
            // 커스텀 state 정보가 없으면 그대로 반환
            return defaultRequest;
        }

        // 기존 state와 redirect_location을 JSON으로 묶어서 Base64 인코딩
        Map<String, Object> customState = new HashMap<>();
        customState.put("redirect_location", redirectLocation);
        customState.put("original_state", defaultRequest.getState());

        try {
            String stateJson = objectMapper.writeValueAsString(customState);
            String encodedState = Base64.getUrlEncoder()
                    .encodeToString(stateJson.getBytes(StandardCharsets.UTF_8));

            // 기존 요청정보 + 새로운 state를 반영한 빌더
            return OAuth2AuthorizationRequest.from(defaultRequest)
                    .state(encodedState)
                    .build();
        } catch (Exception e) {
            // 로깅 후 fallback으로 defaultRequest 반환
            e.printStackTrace();
            return defaultRequest;
        }
    }

}
