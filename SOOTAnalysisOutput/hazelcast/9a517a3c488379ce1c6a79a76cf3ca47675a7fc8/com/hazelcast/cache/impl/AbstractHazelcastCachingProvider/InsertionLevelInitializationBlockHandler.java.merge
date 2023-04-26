package com.hazelcast.cache.impl;

import com.hazelcast.cache.HazelcastCachingProvider;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.util.ExceptionUtil;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class AbstractHazelcastCachingProvider implements CachingProvider {

    protected static final ILogger LOGGER = Logger.getLogger(HazelcastCachingProvider.class);

    protected static final String INVALID_HZ_INSTANCE_SPECIFICATION_MESSAGE = "Not available Hazelcast instance. " + "Please specify your Hazelcast configuration file path via " + "\"HazelcastCachingProvider.HAZELCAST_CONFIG_LOCATION\" property or " + "specify Hazelcast instance name via " + "\"HazelcastCachingProvider.HAZELCAST_INSTANCE_NAME\" property " + "in \"properties\" parameter.";

    protected static final Set<String> SUPPORTED_SCHEMES;

    static {
        Set<String> supportedSchemes = new HashSet<String>();
        supportedSchemes.add("classpath");
        supportedSchemes.add("file");
        supportedSchemes.add("http");
        supportedSchemes.add("https");
        SUPPORTED_SCHEMES = supportedSchemes;
    }

    protected volatile HazelcastInstance hazelcastInstance;

    protected final ClassLoader defaultClassLoader;

    protected final URI defaultURI;

    private final Map<ClassLoader, Map<URI, AbstractHazelcastCacheManager>> cacheManagers;

    public AbstractHazelcastCachingProvider() {
        this.cacheManagers = new WeakHashMap<ClassLoader, Map<URI, AbstractHazelcastCacheManager>>();
        this.defaultClassLoader = getClass().getClassLoader();
        try {
            defaultURI = new URI("hazelcast");
        } catch (URISyntaxException e) {
            throw new CacheException("Cannot create Default URI", e);
        }
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader, Properties properties) {
        final URI managerURI = getManagerUri(uri);
        final ClassLoader managerClassLoader = getManagerClassLoader(classLoader);
        final Properties managerProperties = properties == null ? new Properties() : properties;
        synchronized (cacheManagers) {
            Map<URI, AbstractHazelcastCacheManager> cacheManagersByURI = cacheManagers.get(managerClassLoader);
            if (cacheManagersByURI == null) {
                cacheManagersByURI = new HashMap<URI, AbstractHazelcastCacheManager>();
                cacheManagers.put(managerClassLoader, cacheManagersByURI);
            }
            AbstractHazelcastCacheManager cacheManager = cacheManagersByURI.get(managerURI);
            if (cacheManager == null || cacheManager.isClosed()) {
                try {
                    cacheManager = createHazelcastCacheManager(uri, classLoader, managerProperties);
                    cacheManagersByURI.put(managerURI, cacheManager);
                } catch (Exception e) {
                    throw new CacheException("Error opening URI [" + managerURI.toString() + ']', e);
                }
            }
            return cacheManager;
        }
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return defaultClassLoader;
    }

    @Override
    public URI getDefaultURI() {
        return defaultURI;
    }

    @Override
    public Properties getDefaultProperties() {
        return null;
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
        return getCacheManager(uri, classLoader, null);
    }

    @Override
    public CacheManager getCacheManager() {
        return getCacheManager(null, null, null);
    }

    @Override
    public void close() {
        synchronized (cacheManagers) {
            for (Map<URI, AbstractHazelcastCacheManager> cacheManagersByURI : cacheManagers.values()) {
                for (AbstractHazelcastCacheManager cacheManager : cacheManagersByURI.values()) {
                    if (cacheManager.isDefaultClassLoader) {
                        cacheManager.close();
                    } else {
                        cacheManager.destroy();
                    }
                }
            }
        }
        this.cacheManagers.clear();
        shutdownHazelcastInstance();
    }

    protected void shutdownHazelcastInstance() {
        final HazelcastInstance localInstanceRef = hazelcastInstance;
        if (localInstanceRef != null) {
            localInstanceRef.shutdown();
        }
        hazelcastInstance = null;
    }

    @Override
    public void close(ClassLoader classLoader) {
        final ClassLoader managerClassLoader = getManagerClassLoader(classLoader);
        synchronized (cacheManagers) {
            final Map<URI, AbstractHazelcastCacheManager> cacheManagersByURI = this.cacheManagers.get(managerClassLoader);
            if (cacheManagersByURI != null) {
                for (CacheManager cacheManager : cacheManagersByURI.values()) {
                    cacheManager.close();
                }
            }
        }
    }

    @Override
    public void close(URI uri, ClassLoader classLoader) {
        final URI managerURI = getManagerUri(uri);
        final ClassLoader managerClassLoader = getManagerClassLoader(classLoader);
        synchronized (cacheManagers) {
            final Map<URI, AbstractHazelcastCacheManager> cacheManagersByURI = this.cacheManagers.get(managerClassLoader);
            if (cacheManagersByURI != null) {
                final CacheManager cacheManager = cacheManagersByURI.remove(managerURI);
                if (cacheManager != null) {
                    cacheManager.close();
                }
                if (cacheManagersByURI.isEmpty()) {
                    cacheManagers.remove(classLoader);
                }
            }
        }
    }

    @Override
    public boolean isSupported(OptionalFeature optionalFeature) {
        switch(optionalFeature) {
            case STORE_BY_REFERENCE:
                return false;
            default:
                return false;
        }
    }

    protected URI getManagerUri(URI uri) {
        return uri == null ? defaultURI : uri;
    }

    protected ClassLoader getManagerClassLoader(ClassLoader classLoader) {
        return classLoader == null ? defaultClassLoader : classLoader;
    }

    protected <T extends AbstractHazelcastCacheManager> T createHazelcastCacheManager(URI uri, ClassLoader classLoader, Properties managerProperties) {
        final HazelcastInstance instance;
        try {
            instance = getOrCreateInstance(uri, classLoader, managerProperties);
            if (instance == null) {
                throw new IllegalArgumentException(INVALID_HZ_INSTANCE_SPECIFICATION_MESSAGE);
            }
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
        return createCacheManager(instance, uri, classLoader, managerProperties);
    }

    protected abstract HazelcastInstance getOrCreateInstance(URI uri, ClassLoader classLoader, Properties properties) throws URISyntaxException, IOException;

    protected abstract <T extends AbstractHazelcastCacheManager> T createCacheManager(HazelcastInstance instance, URI uri, ClassLoader classLoader, Properties properties);

    protected boolean isConfigLocation(URI location) {
        String scheme = location.getScheme();
        if (scheme == null) {
            try {
                String resolvedPlaceholder = System.getProperty(location.getRawSchemeSpecificPart());
                if (resolvedPlaceholder == null) {
                    return false;
                }
                location = new URI(resolvedPlaceholder);
                scheme = location.getScheme();
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return (scheme != null && SUPPORTED_SCHEMES.contains(scheme.toLowerCase()));
    }
}