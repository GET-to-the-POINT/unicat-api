package taeniverse.ai_news.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key Serializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // Value Serializer
        Jackson2JsonRedisSerializer<Claims> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Claims.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule()
                .addAbstractTypeMapping(Claims.class, DefaultClaims.class)); // Claims → DefaultClaims 매핑
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL); // 타입 정보 포함

        ///  TODO: 대체 메서드가?
        jacksonSerializer.setObjectMapper(objectMapper);

        redisTemplate.setValueSerializer(jacksonSerializer);

        return redisTemplate;
    }
}
