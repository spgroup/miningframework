package sun.net;

import java.net.InetAddress;
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

    private static int cachePolicy;

    private static int negativeCachePolicy;

    private static boolean set = false;

    private static boolean negativeSet = false;

    static {
        set = false;
        negativeSet = false;
        cachePolicy = FOREVER;
        negativeCachePolicy = 0;
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
            set = true;
        } else {
            tmp = java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction(cachePolicyPropFallback));
            if (tmp != null) {
                cachePolicy = tmp.intValue();
                if (cachePolicy < 0) {
                    cachePolicy = FOREVER;
                }
                set = true;
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
            negativeSet = true;
        } else {
            tmp = java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction(negativeCachePolicyPropFallback));
            if (tmp != null) {
                negativeCachePolicy = tmp.intValue();
                if (negativeCachePolicy < 0) {
                    negativeCachePolicy = FOREVER;
                }
                negativeSet = true;
            }
        }
    }

    public static synchronized int get() {
        if (!set && System.getSecurityManager() == null) {
            return DEFAULT_POSITIVE;
        } else {
            return cachePolicy;
        }
    }

    public static synchronized int getNegative() {
        return negativeCachePolicy;
    }

    public static synchronized void setIfNotSet(int newPolicy) {
        if (!set) {
            checkValue(newPolicy, cachePolicy);
            cachePolicy = newPolicy;
        }
    }

    public static synchronized void setNegativeIfNotSet(int newPolicy) {
        if (!negativeSet) {
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
