package com.popush.triela.common.cache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.NonNull;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig extends CachingConfigurerSupport {

    private static final int DEFAULT_DURATION = 1500000;
    private static final String CACHE_PREFIX = "TRIELA";

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    @NonNull
    @Bean
    @Primary
    public ExpiringSupportRedisCacheManager cacheManager() {
        CacheKeyPrefix prefix = name -> String.format("%s:%s:", CACHE_PREFIX, name);
        RedisSerializer<Object> redisSerializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper());
        RedisSerializationContext.SerializationPair<Object> serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer);
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
                redisConnectionFactory);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                                                .computePrefixWith(prefix)
                                                                .serializeValuesWith(serializationPair);
        return new ExpiringSupportRedisCacheManager(redisCacheWriter, config);
    }

    @Bean
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(),
                                     ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }

    @Override
    @NonNull
    @Bean
    @Primary
    public CacheResolver cacheResolver() {
        return new ExpiringSupportRedisCacheResolver(cacheManager(),
                                                     Duration.of(DEFAULT_DURATION, ChronoUnit.MILLIS));
    }
}
