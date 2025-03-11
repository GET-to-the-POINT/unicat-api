package gettothepoint.unicatapi.common.util;

import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.entity.Member;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

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

    public String generateAndAddJwtToken(HttpServletResponse response, Member member) {
        String token = generateJwtToken(member.getId(), member.getEmail());
        addJwtCookie(response, token);
        return token;
    }
}
