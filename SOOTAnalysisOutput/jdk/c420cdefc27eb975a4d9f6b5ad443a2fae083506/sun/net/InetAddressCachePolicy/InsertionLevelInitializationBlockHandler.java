package sun.net;

import java.security.PrivilegedAction;
import java.security.Security;

public final class InetAddressCachePolicy {

    private static final String cachePolicyProp = "networkaddress.cache.ttl";

    private static final String cachePolicyPropFallback = "sun.net.inetaddr.ttl";

    private static final String negativeCachePolicyProp = "networkaddress.cache.negative.ttl";

    private static final String negativeCachePolicyPropFallback = "sun.net.inetaddr.negative.ttl";

    public static final int FOREVER = -1;

    public static final int NEVER = 0;

    public static final int DEFAULT_POSITIVE = 30;

    private static int cachePolicy = FOREVER;

    private static int negativeCachePolicy = NEVER;

    private static boolean propertySet;

    private static boolean propertyNegativeSet;

    static {
        Integer tmp = null;
        try {
            tmp = new Integer(java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {

                public String run() {
                    return Security.getProperty(cachePolicyProp);
                }
            }));
        } catch (NumberFormatException e) {
        }
        if (tmp != null) {
            cachePolicy = tmp.intValue();
            if (cachePolicy < 0) {
                cachePolicy = FOREVER;
            }
            propertySet = true;
        } else {
            tmp = java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction(cachePolicyPropFallback));
            if (tmp != null) {
                cachePolicy = tmp.intValue();
                if (cachePolicy < 0) {
                    cachePolicy = FOREVER;
                }
                propertySet = true;
            } else {
                if (System.getSecurityManager() == null) {
                    cachePolicy = DEFAULT_POSITIVE;
                }
            }
        }
        try {
            tmp = new Integer(java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {

                public String run() {
                    return Security.getProperty(negativeCachePolicyProp);
                }
            }));
        } catch (NumberFormatException e) {
        }
        if (tmp != null) {
            negativeCachePolicy = tmp.intValue();
            if (negativeCachePolicy < 0) {
                negativeCachePolicy = FOREVER;
            }
            propertyNegativeSet = true;
        } else {
            tmp = java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction(negativeCachePolicyPropFallback));
            if (tmp != null) {
                negativeCachePolicy = tmp.intValue();
                if (negativeCachePolicy < 0) {
                    negativeCachePolicy = FOREVER;
                }
                propertyNegativeSet = true;
            }
        }
    }

    public static synchronized int get() {
        return cachePolicy;
    }

    public static synchronized int getNegative() {
        return negativeCachePolicy;
    }

    public static synchronized void setIfNotSet(int newPolicy) {
        if (!propertySet) {
            checkValue(newPolicy, cachePolicy);
            cachePolicy = newPolicy;
        }
    }

    public static synchronized void setNegativeIfNotSet(int newPolicy) {
        if (!propertyNegativeSet) {
            negativeCachePolicy = newPolicy;
        }
    }

    private static void checkValue(int newPolicy, int oldPolicy) {
        if (newPolicy == FOREVER)
            return;
        if ((oldPolicy == FOREVER) || (newPolicy < oldPolicy) || (newPolicy < FOREVER)) {
            throw new SecurityException("can't make InetAddress cache more lax");
        }
    }
}