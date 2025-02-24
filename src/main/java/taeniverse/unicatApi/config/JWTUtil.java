package taeniverse.unicatApi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import taeniverse.unicatApi.cache.claims.ClaimsCache;
import taeniverse.unicatApi.mvc.model.dto.OAuthDTO;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    private final ClaimsCache claimsCache;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiredTime;

    @Value("${app.jwt.cookie.maxAge}")
    private int maxAge;

    @Value("${app.jwt.cookie.sameSite}")
    private String sameSite;

    @Value("${app.jwt.cookie.httpOnly}")
    private boolean httpOnly;

    @Value("${app.jwt.cookie.secure}")
    private boolean secure;

    @Value("${app.jwt.cookie.domain}")
    private String domain;

    @Value("${app.jwt.cookie.path}")
    private String path;

    @Value("${app.jwt.cookie.name}")
    private String cookieName;

    public JWTUtil(
            ClaimsCache claimsCache,
            @Value("${app.jwt.secret}") String secret
                   ) {
        this.claimsCache = claimsCache;
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    private Claims getClaims(String token) {
        return claimsCache.computeIfAbsent(token, t -> Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(t)
                .getPayload());
    }

    //JWT 검증
    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public OAuthDTO parseTokenToOAuthDTO(String token) {
        Claims claims = getClaims(token);
        return OAuthDTO.builder()
                .username(claims.get("username", String.class))
                .userId(claims.get("userId", Long.class))
                .role(claims.get("role", String.class))
                .build();
    }

    public Boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public void setJwtResponse(HttpServletResponse response, String username, Long userId, String role) {
        String token = createToken(username, userId, role);
        String rawJwtCookie = this.createRawJwtCookie(token).toString();
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        response.addHeader(HttpHeaders.SET_COOKIE, rawJwtCookie);
    }

    public void deleteJwtResponse(HttpServletResponse response) {
        String expiredRawJwtCookie = this.createExpriedRawJwtCookie().toString();
        response.setHeader(HttpHeaders.SET_COOKIE, expiredRawJwtCookie);
    }

    private String createToken(String username, Long userId, String role) {
        return Jwts.builder()
                .claim("username", username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + this.jwtExpiredTime))
                .signWith(secretKey)
                .compact();
    }

    private ResponseCookie createRawJwtCookie(String token) {
        return this.createRawJwtCookie(token, this.maxAge);
    }

    private ResponseCookie createExpriedRawJwtCookie() {
        return this.createRawJwtCookie("", 0);
    }

    private ResponseCookie createRawJwtCookie(String token, int maxAge) {
        return ResponseCookie.from(this.cookieName, token)
                .httpOnly(this.httpOnly)
                .secure(this.secure)
                .path(this.path)
                .maxAge(maxAge)
                .domain(this.domain)
                .sameSite(this.sameSite)
                .build();
    }

}
