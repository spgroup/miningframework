package org.springframework.boot.autoconfigure.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;

final class CacheConfigurations {

    private static final Map<CacheType, Class<?>> MAPPINGS;

    static {
        Map<CacheType, Class<?>> mappings = new HashMap<CacheType, Class<?>>();
        mappings.put(CacheType.GENERIC, GenericCacheConfiguration.class);
        mappings.put(CacheType.EHCACHE, EhCacheCacheConfiguration.class);
        mappings.put(CacheType.HAZELCAST, HazelcastCacheConfiguration.class);
        mappings.put(CacheType.INFINISPAN, InfinispanCacheConfiguration.class);
        mappings.put(CacheType.JCACHE, JCacheCacheConfiguration.class);
        mappings.put(CacheType.COUCHBASE, CouchbaseCacheConfiguration.class);
        mappings.put(CacheType.REDIS, RedisCacheConfiguration.class);
        mappings.put(CacheType.CAFFEINE, CaffeineCacheConfiguration.class);
<<<<<<< MINE
=======
        addGuavaMapping(mappings);
>>>>>>> YOURS
        mappings.put(CacheType.SIMPLE, SimpleCacheConfiguration.class);
        mappings.put(CacheType.NONE, NoOpCacheConfiguration.class);
        MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    @Deprecated
    private static void addGuavaMapping(Map<CacheType, Class<?>> mappings) {
        mappings.put(CacheType.GUAVA, GuavaCacheConfiguration.class);
    }

    private CacheConfigurations() {
    }

    public static String getConfigurationClass(CacheType cacheType) {
        Class<?> configurationClass = MAPPINGS.get(cacheType);
        Assert.state(configurationClass != null, "Unknown cache type " + cacheType);
        return configurationClass.getName();
    }

    public static CacheType getType(String configurationClassName) {
        for (Map.Entry<CacheType, Class<?>> entry : MAPPINGS.entrySet()) {
            if (entry.getValue().getName().equals(configurationClassName)) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Unknown configuration class " + configurationClassName);
    }
}
