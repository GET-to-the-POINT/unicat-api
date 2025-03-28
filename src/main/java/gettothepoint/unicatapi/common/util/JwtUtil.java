package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.JwtProperties;
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

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public String generateJwtToken(Long memberId, String email, String plan) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(memberId.toString())
                .claim("email", email)
                .claim("plan", plan)
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.cookie().maxAge(), ChronoUnit.SECONDS))
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256")
                        .keyId(jwtProperties.keyId())
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