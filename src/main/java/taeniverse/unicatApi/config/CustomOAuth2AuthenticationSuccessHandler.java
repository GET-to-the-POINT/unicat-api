package taeniverse.unicatApi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;
import taeniverse.unicatApi.mvc.repository.MemberRepository;
import taeniverse.unicatApi.mvc.repository.OAuth2Repository;
import taeniverse.unicatApi.temp.CustomOAuth2User;
import taeniverse.unicatApi.util.JwtUtil;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final JwtUtil jwtUtil;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuth2Repository oauth2Repository;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        // 1. 현재 인증 객체를 OAuth2AuthenticationToken으로 캐스팅
        OAuth2AuthenticationToken oauth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        Member member = memberRepository.findByEmail(email).orElse(null);
        String registrationId = oauth2AuthenticationToken.getAuthorizedClientRegistrationId(); // 예: "google"

        // 2. authorizedClientService를 통해 OAuth2AuthorizedClient를 조회
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, email);

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

            OAuth2 oauth2 = oauth2Repository.findByProviderAndEmail(registrationId, email)
                    .orElse(OAuth2.builder()
                            .provider(registrationId)
                            .username(customUser.getName())
                            .member(member)
                            .email(email)
                            .build());

            oauth2.setAccessToken(accessToken.getTokenValue());
            oauth2.setAccessTokenExpiresAt(accessToken.getExpiresAt());
            if (refreshToken != null) {
                oauth2.setRefreshToken(refreshToken.getTokenValue());
            }
            oauth2Repository.save(oauth2);
        }

            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .subject(email)
                    .issuedAt(now)
                    .expiresAt(now.plus(1, ChronoUnit.DAYS))
                    .build();

            Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
            String token = jwt.getTokenValue();

            jwtUtil.addJwtCookie(response, token);
            response.sendRedirect("/");

        }
    }