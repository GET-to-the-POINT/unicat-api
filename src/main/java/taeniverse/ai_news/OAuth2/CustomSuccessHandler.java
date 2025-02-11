package taeniverse.ai_news.OAuth2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import taeniverse.ai_news.config.JWTUtil;
import taeniverse.ai_news.mvc.model.dto.PrincipalDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Handles successful OAuth2 authentication, creates JWT, and redirects the user.
 */
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final String protocol;
    private final String domain;
    private final String port;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomSuccessHandler(
            JWTUtil jwtUtil,
            @Value("${ai-news.frontend.protocol}") String protocol,
            @Value("${ai-news.frontend.domain}") String domain,
            @Value("${ai-news.frontend.port}") String port
    ) {
        this.jwtUtil = jwtUtil;
        this.protocol = protocol;
        this.domain = domain;
        this.port = port;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String encodedState = request.getParameter("state");
        String finalRedirectUrl = extractRedirectUrlOrDefault(encodedState);

        // JWT 생성 및 쿠키(혹은 헤더) 설정
        PrincipalDetails userDetails = (PrincipalDetails) authentication.getPrincipal();
        GrantedAuthority authority = userDetails.getAuthorities().stream().findFirst().orElse(null);
        String role = (authority != null) ? authority.getAuthority() : "";
        jwtUtil.setJwtResponse(response, userDetails.getUsername(), userDetails.getUserId(), role);

        // 리디렉션
        response.sendRedirect(finalRedirectUrl);
    }

    /**
     * state 파라미터를 JSON으로 디코딩하여 "redirect_location" 값을 추출.
     * 추출 실패 시 기본 리디렉션 URL 반환.
     */
    private String extractRedirectUrlOrDefault(String encodedState) {
        if (encodedState == null || encodedState.isEmpty()) {
            return buildDefaultRedirectUrl();
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedState);
            String stateJson = new String(decodedBytes, StandardCharsets.UTF_8);
            Map<String, Object> stateMap = objectMapper.readValue(stateJson, new TypeReference<>() {});
            String redirectLocation = (String) stateMap.get("redirect_location");

            return (redirectLocation == null || redirectLocation.isEmpty())
                    ? buildDefaultRedirectUrl()
                    : redirectLocation;
        } catch (Exception e) {
            e.printStackTrace();
            return buildDefaultRedirectUrl();
        }
    }

    /**
     * 기본 리디렉션 URL (프로퍼티로 설정된 front-end 주소)
     */
    private String buildDefaultRedirectUrl() {
        return String.join("", protocol, domain, ":", port, "/");
    }
}
