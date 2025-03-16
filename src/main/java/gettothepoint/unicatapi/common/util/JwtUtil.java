package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

    public String generateJwtToken(Long memberId, String email) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(memberId.toString())
                .claim("email", email)
                .claim("scope", "all")
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

    public String getEmailFromToken(String token) {
        try {
            Jwt decodedJwt = jwtDecoder.decode(token);
            return decodedJwt.getClaim("email");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

}