package taeniverse.ai_news.cache.claims;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

public interface ClaimsCache {
    Claims get(String token);

    void put(String token, Claims claims);

    Claims computeIfAbsent(String token, Function<String, Claims> mappingFunction);
}
