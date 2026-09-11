package org.example.librarymanagementsystem.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching  // ← enables caching — the only place this should be declared
public class  RedisConfig {

    // ─── ObjectMapper for Redis ─────────────────────
    // No activateDefaultTyping() — GenericJackson2JsonRedisSerializer already
    // embeds type info on its own. Adding default typing with a permissive
    // validator (e.g. LaissezFaireSubTypeValidator) opens a known Jackson
    // polymorphic-deserialization risk and isn't needed here.
    private ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // ─── Redis Template ────────────────────────────
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    // ─── Cache Manager ─────────────────────────────
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .prefixCacheNameWith("library:")
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        // books
        configs.put("books", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configs.put("book", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // authors
        configs.put("authors", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configs.put("author", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // categories — rarely change
        configs.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));

        // publishers — rarely change
        configs.put("publishers", defaultConfig.entryTtl(Duration.ofHours(1)));

        // members — short TTL: status (ACTIVE/SUSPENDED) gates borrow authorization,
        // so staleness here has real consequences, not just an outdated display value
        configs.put("members", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("member", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}