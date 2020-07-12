package com.popush.triela.common.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ExpiringSupportRedisCacheResolver extends SimpleCacheResolver {

    private final Duration defaultDuration;

    public ExpiringSupportRedisCacheResolver(ExpiringSupportRedisCacheManager cacheManager,
                                             Duration defaultDuration) {
        super(cacheManager);
        this.defaultDuration = defaultDuration;
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        Assert.isInstanceOf(ExpiringSupportRedisCacheManager.class, cacheManager);
        super.setCacheManager(cacheManager);
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Collection<String> cacheNames = getCacheNames(context);
        if (CollectionUtils.isEmpty(cacheNames)) {
            return Collections.emptyList();
        }
        Duration ttl = defaultDuration;
        CacheExpiring cacheExpiring = context.getMethod().getAnnotation(CacheExpiring.class);
        if (cacheExpiring != null) {
            ttl = Duration.of(cacheExpiring.value(), cacheExpiring.unit());
        }
        Collection<Cache> result = new ArrayList<>(cacheNames.size());
        for (String cacheName : cacheNames) {
            Cache cache = ((ExpiringSupportRedisCacheManager) getCacheManager()).getCache(cacheName, ttl);
            if (cache == null) {
                throw new IllegalArgumentException(
                        String.format("Cannot find cache named '%s' for %s", cacheName,
                                      context.getOperation()));
            }
            result.add(cache);
        }
        return result;
    }
}
