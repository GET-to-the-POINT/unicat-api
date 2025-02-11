package taeniverse.ai_news.cache.claims.impl;

import io.jsonwebtoken.Claims;
import taeniverse.ai_news.cache.claims.ClaimsCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LocalClaimsCache implements ClaimsCache {
    private final Map<String, Claims> cache = new ConcurrentHashMap<>();

    @Override
    public Claims get(String token) {
        return cache.get(token);
    }

    @Override
    public void put(String token, Claims claims) {
        cache.put(token, claims);
    }

    @Override
    public Claims computeIfAbsent(String token, Function<String, Claims> mappingFunction) {
        return cache.computeIfAbsent(token, mappingFunction);
    }
}
