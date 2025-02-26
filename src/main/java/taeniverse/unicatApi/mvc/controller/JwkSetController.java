package taeniverse.unicatApi.mvc.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwkSetController {

    @Value("${app.jwt.key-id}")
    private String keyId;

    private final RSAPublicKey publicKey;

    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        JWK jwk = new RSAKey.Builder(publicKey)
                .keyID(keyId)  // 키 ID
                .algorithm(JWSAlgorithm.RS256)  // 알고리즘 (RS256)
                .keyUse(KeyUse.SIGNATURE)  // 서명(Signing) 용도
                .build();

        return new JWKSet(jwk).toJSONObject();
    }
}