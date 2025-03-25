package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final AppProperties appProperties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public Cookie createJwtCookie(String jwtToken) {
        Cookie jwtCookie = new Cookie(appProperties.jwt().cookie().name(), jwtToken);
        jwtCookie.setDomain(appProperties.jwt().cookie().domain());
        jwtCookie.setPath(appProperties.jwt().cookie().path());
        jwtCookie.setSecure(appProperties.jwt().cookie().secure());
        jwtCookie.setHttpOnly(appProperties.jwt().cookie().httpOnly());
        jwtCookie.setMaxAge(appProperties.jwt().cookie().maxAge());

        return jwtCookie;
    }

    public Cookie removeJwtCookie() {
        Cookie jwtCookie = this.createJwtCookie("");
        jwtCookie.setMaxAge(0);
        return jwtCookie;
    }

    public String generateJwtToken(Long memberId, String email, String plan) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(memberId.toString())
                .claim("email", email)
                .claim("plan", plan)
                .issuedAt(now)
                .expiresAt(now.plus(appProperties.jwt().cookie().maxAge(), ChronoUnit.SECONDS))
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256")
                        .keyId(appProperties.jwt().keyId())
                        .build(),
                claims
        );

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    public Long getMemberId(String jwtToken) {
        try {
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            return Long.parseLong(decodedJwt.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    public String getEmail(String jwtToken) {
        try {
            Jwt decodedJwt = jwtDecoder.decode(jwtToken);
            return decodedJwt.getClaim("email");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

}