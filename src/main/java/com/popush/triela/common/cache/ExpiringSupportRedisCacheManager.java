package com.popush.triela.common.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class ExpiringSupportRedisCacheManager extends RedisCacheManager {
    private final RedisCacheConfiguration defaultConfig;
    private final Map<String, CacheMetaDataEntry> cacheMetaData;

    public ExpiringSupportRedisCacheManager(RedisCacheWriter cacheWriter,
                                            RedisCacheConfiguration configuration) {
        super(cacheWriter, configuration);
        defaultConfig = configuration;
        cacheMetaData = new ConcurrentHashMap<>(16);
    }

    public Cache getCache(String name, Duration ttl) {
        String cacheContainerName = String.format("%s(%s)", name, ttl);
        cacheMetaData.putIfAbsent(cacheContainerName, new CacheMetaDataEntry(name, ttl));
        return getCache(cacheContainerName);
    }

    @Override
    protected RedisCache getMissingCache(String cacheContainerName) {
        CacheMetaDataEntry defaultEntry = new CacheMetaDataEntry(cacheContainerName,
                                                                 defaultConfig.getTtl());
        CacheMetaDataEntry entry = cacheMetaData.getOrDefault(cacheContainerName, defaultEntry);
        return createRedisCache(entry.cacheName, defaultConfig.entryTtl(entry.ttl));
    }

    @Value
    private static class CacheMetaDataEntry {
        private String cacheName;
        private Duration ttl;
    }
}
