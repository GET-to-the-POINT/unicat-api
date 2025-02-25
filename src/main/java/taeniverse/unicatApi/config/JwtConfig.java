package taeniverse.unicatApi.config;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration // 스프링 구성 클래스임을 명시
public class JwtConfig {

    // 파일에서 읽은 PEM 문자열을 저장할 변수입니다.
    private String privateKeyPEM;
    private String publicKeyPEM;

    // 애플리케이션 초기화 시 PEM 파일을 리소스에서 읽어옵니다.
    @PostConstruct
    public void init() throws Exception {
        // src/main/resources/keys/ 폴더 안에 파일을 배치했다고 가정합니다.
        privateKeyPEM = readKeyFromResource("keys/private_key.pem"); // 개인키 파일 경로
        publicKeyPEM = readKeyFromResource("keys/public_key.pem");    // 공개키 파일 경로 (또는 별도의 PEM 파일)
    }

    // 클래스패스 리소스에서 키 파일을 읽어 문자열로 반환하는 메서드입니다.
    private String readKeyFromResource(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            // 파일 내용을 UTF-8로 읽어 문자열로 변환합니다.
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    // PEM 형식의 개인키 문자열을 RSAPrivateKey 객체로 변환하는 메서드입니다.
    private RSAPrivateKey getPrivateKey() throws Exception {
        String privateKeyContent = privateKeyPEM
                .replaceAll("-----.*?-----", "") // 헤더/푸터 제거
                .replaceAll("\\s+", "");                    // 공백 제거
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // RSA 알고리즘의 KeyFactory 생성
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent)); // Base64 디코딩 후 KeySpec 생성
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec); // RSAPrivateKey 객체로 변환
    }

    // PEM 형식의 공개키 문자열을 RSAPublicKey 객체로 변환하는 메서드입니다.
    private RSAPublicKey getPublicKey() throws Exception {
        String publicKeyContent = publicKeyPEM
                .replaceAll("-----.*?-----", "") // 헤더/푸터 제거
                .replaceAll("\\s+", "");                // 공백 제거
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // RSA 알고리즘의 KeyFactory 생성
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent)); // Base64 디코딩 후 KeySpec 생성
        return (RSAPublicKey) keyFactory.generatePublic(keySpec); // RSAPublicKey 객체로 변환
    }

    // JWT 인코더 Bean을 생성합니다. JWT 생성 시 개인키를 이용하여 서명합니다.
    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        // RSAKey 객체를 생성하여 공개키와 개인키를 모두 포함시킵니다.
        RSAKey rsaKey = new RSAKey.Builder(getPublicKey())
                .privateKey(getPrivateKey())
                .keyID("rsa-key-id") // 키 식별자 설정 (원하는 값으로 변경 가능)
                .build();
        // ImmutableJWKSet을 생성하여 JWT 서명에 사용될 키 소스를 구성합니다.
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        // NimbusJwtEncoder를 사용하여 JWT 인코더를 반환합니다.
        return new NimbusJwtEncoder(jwkSource);
    }

    // JWT 디코더 Bean을 생성합니다. JWT 검증 시 공개키를 사용합니다.
    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        // 공개키를 사용하여 NimbusJwtDecoder를 생성 (JWT의 서명 검증에 활용)
        return NimbusJwtDecoder.withPublicKey(getPublicKey()).build();
    }
}