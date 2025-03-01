package taeniverse.unicatApi.mvc.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.component.propertie.AppProperties;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
@Tag(name = "JWK API", description = "JSON Web Key Set 제공 API")
public class JwkSetController {

    private final AppProperties appProperties;

    private final RSAPublicKey publicKey;

    @GetMapping("/jwks.json")
    @Operation(
            summary = "JWK Set 조회",
            description = "현재 RSA 공개키를 기반으로 생성된 JWK Set 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "JWK Set 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "JWK Set",
                                                    summary = "예시 JWK Set",
                                                    value = """
{
  "keys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "rsa-key-id",
      "alg": "RS256",
      "n": "1n4iFl-u0JR0Fdm62KFkaRZv-i6o3fBGyPpwpuoNwP84hEDgwfuDlgN9S6Hqaz_GNDxZqRlAdzOFz4DRJQOo_fPh..."
    }
  ]
}
"""
                                            )
                                    }
                            )
                    )
            }
    )
    public Map<String, Object> getJwks() {
        JWK jwk = new RSAKey.Builder(publicKey)
                .keyID(appProperties.jwt().keyId())
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}