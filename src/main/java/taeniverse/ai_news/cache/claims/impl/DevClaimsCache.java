package taeniverse.ai_news.cache.claims.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import taeniverse.ai_news.cache.claims.ClaimsCache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * custom.redis.ttl 설정 값은 dev 프로퍼티에만 있습니다.
 */
@RequiredArgsConstructor
public class DevClaimsCache implements ClaimsCache {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${custom.redis.ttl}")
    private long ttlMillis;

    @Override
    public Claims get(String token) {
        return (Claims) redisTemplate.opsForValue().get(token);
    }

    @Override
    public void put(String token, Claims claims) {
        redisTemplate.opsForValue().set(token, claims, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Claims computeIfAbsent(String token, Function<String, Claims> mappingFunction) {
        synchronized (this) { // 동기화 블록 추가
            Claims claims = get(token);
            if (claims == null) {
                claims = mappingFunction.apply(token);
                put(token, claims);
            }
            return claims;
        }
    }

}
