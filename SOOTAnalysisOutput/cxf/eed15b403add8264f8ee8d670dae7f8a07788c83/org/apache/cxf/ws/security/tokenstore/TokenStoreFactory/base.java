package org.apache.cxf.ws.security.tokenstore;

import java.io.IOException;
import java.net.URL;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.cache.ReplayCacheFactory;

public abstract class TokenStoreFactory {

    private static boolean ehCacheInstalled;

    static {
        try {
            Class<?> cacheManagerClass = ClassLoaderUtils.loadClass("net.sf.ehcache.CacheManager", TokenStoreFactory.class);
            if (cacheManagerClass != null) {
                ehCacheInstalled = true;
            }
        } catch (Exception e) {
        }
    }

    protected static synchronized boolean isEhCacheInstalled() {
        return ehCacheInstalled;
    }

    public static TokenStoreFactory newInstance() {
        if (isEhCacheInstalled()) {
            return new EHCacheTokenStoreFactory();
        }
        return new MemoryTokenStoreFactory();
    }

    public abstract TokenStore newTokenStore(String key, Message message);

    protected URL getConfigFileURL(Message message) {
        Object o = message.getContextualProperty(SecurityConstants.CACHE_CONFIG_FILE);
        if (o instanceof String) {
            URL url = null;
            ResourceManager rm = message.getExchange().getBus().getExtension(ResourceManager.class);
            url = rm.resolveResource((String) o, URL.class);
            try {
                if (url == null) {
                    url = ClassLoaderUtils.getResource((String) o, ReplayCacheFactory.class);
                }
                if (url == null) {
                    url = new URL((String) o);
                }
                return url;
            } catch (IOException e) {
            }
        } else if (o instanceof URL) {
            return (URL) o;
        }
        return null;
    }
}
