package com.drawpin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * Redis configuration for caching refresh token validity and rate-limit counters.
 *
 * <p>Redis is used as an optional fast-path cache on top of PostgreSQL:
 * <ul>
 *   <li>Refresh token hash → valid/invalid (avoids a DB hit on every /auth/refresh call)</li>
 *   <li>Login attempt counters per IP / email (for rate limiting)</li>
 *   <li>Forgot-password rate-limit markers</li>
 * </ul>
 *
 * <p>If Redis is unavailable, the application degrades gracefully — auth operations
 * fall through to the PostgreSQL source of truth.
 *
 * <p><b>Serialisation:</b>
 * Keys are plain {@link String}. Values are JSON-serialised via Jackson with
 * Java 8 time support enabled, so {@link java.time.Instant} fields serialise correctly.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * Creates a Lettuce-based Redis connection factory.
     * Configures the password only when one is provided (omit in local dev).
     *
     * <p>The factory is set to {@code lazyStart = true} so Redis is NOT contacted
     * at application startup. If Redis is unavailable, the first Redis operation
     * will fail gracefully rather than preventing the app from booting. This allows
     * the application to run in local dev with PostgreSQL only.
     *
     * @return a configured {@link LettuceConnectionFactory}
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setEagerInitialization(false); // Do not connect at startup
        return factory;
    }

    /**
     * Creates the primary {@link RedisTemplate} used across the application.
     *
     * <p>Key serialiser: {@link StringRedisSerializer} (plain UTF-8 strings).
     * Value serialiser: {@link GenericJackson2JsonRedisSerializer} (JSON with type info).
     *
     * @param connectionFactory the Redis connection factory
     * @return a configured {@link RedisTemplate}
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }
}
