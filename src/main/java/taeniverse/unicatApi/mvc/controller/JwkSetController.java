package taeniverse.unicatApi.mvc.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
@Tag(name = "JWK API", description = "JSON Web Key Set 제공 API")
public class JwkSetController {

    @Value("${app.jwt.key-id}")
    private String keyId;

    private final RSAPublicKey publicKey;

    @GetMapping("/jwks.json")
    @Operation(
            summary = "JWK Set 조회",
            description = "현재 RSA 공개키를 기반으로 생성된 JWK Set 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JWK Set 조회 성공")
            }
    )
    public Map<String, Object> getJwks() {
        JWK jwk = new RSAKey.Builder(publicKey)
                .keyID(keyId)
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}