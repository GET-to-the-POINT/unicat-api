package taeniverse.unicatApi.mvc.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import taeniverse.unicatApi.mvc.model.dto.SignDto;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.repository.MemberRepository;
import taeniverse.unicatApi.util.JwtUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public ResponseEntity<String> signUp(SignDto signDto, HttpServletResponse response) {
        if (memberRepository.existsByEmail(signDto.getEmail())) {
            return ResponseEntity.badRequest().body("Member already exists");
        }

        Member member = Member.builder()
                .email(signDto.getEmail())
                .password(passwordEncoder.encode(signDto.getPassword()))
                .build();

        memberRepository.save(member);

        String token = generateJwtToken(member.getEmail());
        jwtUtil.addJwtCookie(response, token);

        return ResponseEntity.ok("User registered successfully");
    }

    public ResponseEntity<String> signIn(SignDto signDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(signDto.getEmail())
                .orElse(null);
        if (member == null || !passwordEncoder.matches(signDto.getPassword(), member.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String token = generateJwtToken(member.getEmail());
        jwtUtil.addJwtCookie(response, token);

        return ResponseEntity.ok("User signed in successfully");
    }

    private String generateJwtToken(String email) {
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