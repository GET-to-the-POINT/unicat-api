package taeniverse.unicatApi.component.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.component.util.JwtUtil;
import taeniverse.unicatApi.mvc.service.MemberDetailsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final JwtUtil jwtUtil;
    private final MemberDetailsService memberDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        assert email != null;

        List<String> roles = memberDetailsService.loadUserByUsername(email)
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
        String token = jwt.getTokenValue();

        jwtUtil.addJwtCookie(response, token);

        String state = request.getParameter("state");
        String redirect = "/";
        if (state != null && state.contains("|")) {
            String[] parts = state.split("\\|");
            if (parts.length == 2) {
                redirect = java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        response.sendRedirect(redirect);
    }
}