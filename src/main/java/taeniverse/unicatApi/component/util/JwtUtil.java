package taeniverse.unicatApi.component.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.component.propertie.AppProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AppProperties appProperties;
    private final JwtEncoder jwtEncoder;

    public void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = this.createJwtCookie(token);
        response.addCookie(jwtCookie);
    }

    private Cookie createJwtCookie(String token) {
        Cookie jwtCookie = new Cookie(appProperties.jwt().cookie().name(), token);
        jwtCookie.setDomain(appProperties.jwt().cookie().domain());
        jwtCookie.setPath(appProperties.jwt().cookie().path());
        jwtCookie.setSecure(appProperties.jwt().cookie().secure());
        jwtCookie.setHttpOnly(appProperties.jwt().cookie().httpOnly());
        jwtCookie.setMaxAge(appProperties.jwt().cookie().maxAge());

        return jwtCookie;
    }

    public void removeJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = this.createJwtCookie("");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }

    public String generateJwtToken(String email) {
        // 현재 시각을 기준으로 JWT의 발행 및 만료 시간을 설정합니다.
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)                     // JWT의 subject에 사용자 이메일을 설정합니다.
                .issuedAt(now)                      // 발행 시간을 현재 시간으로 설정합니다.
                .expiresAt(now.plus(1, ChronoUnit.DAYS)) // 만료 시간을 1일 후로 설정합니다.
                .build();

        // RS256 알고리즘과 RSA 키 ID("rsa-key-id")를 사용하여 JWT 헤더를 생성합니다.
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256")       // RS256 알고리즘 사용: 기존 HS256 대신 변경합니다.
                        .keyId("rsa-key-id")        // JWT 설정 시 등록한 RSA 키의 식별자와 일치시킵니다.
                        .build(),
                claims
        );

        // JwtEncoder를 사용하여 JWT를 인코딩(서명 포함)하고, 최종 토큰 값을 반환합니다.
        return jwtEncoder.encode(parameters).getTokenValue();
    }
}