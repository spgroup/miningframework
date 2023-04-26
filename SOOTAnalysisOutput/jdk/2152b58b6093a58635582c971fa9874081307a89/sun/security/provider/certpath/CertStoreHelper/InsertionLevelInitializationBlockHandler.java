package sun.security.provider.certpath;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.CertStore;
import java.security.cert.X509CertSelector;
import java.security.cert.X509CRLSelector;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import sun.security.util.Cache;

public abstract class CertStoreHelper {

    private static final int NUM_TYPES = 2;

    private final static Map<String, String> classMap = new HashMap<>(NUM_TYPES);

    static {
        classMap.put("LDAP", "sun.security.provider.certpath.ldap.LDAPCertStoreHelper");
        classMap.put("SSLServer", "sun.security.provider.certpath.ssl.SSLServerCertStoreHelper");
    }

    private static Cache<String, CertStoreHelper> cache = Cache.newSoftMemoryCache(NUM_TYPES);

    public static CertStoreHelper getInstance(final String type) throws NoSuchAlgorithmException {
        CertStoreHelper helper = cache.get(type);
        if (helper != null) {
            return helper;
        }
        final String cl = classMap.get(type);
        if (cl == null) {
            throw new NoSuchAlgorithmException(type + " not available");
        }
        try {
            helper = AccessController.doPrivileged(new PrivilegedExceptionAction<CertStoreHelper>() {

                public CertStoreHelper run() throws ClassNotFoundException {
                    try {
                        Class<?> c = Class.forName(cl, true, null);
                        CertStoreHelper csh = (CertStoreHelper) c.newInstance();
                        cache.put(type, csh);
                        return csh;
                    } catch (InstantiationException e) {
                        throw new AssertionError(e);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            });
            return helper;
        } catch (PrivilegedActionException e) {
            throw new NoSuchAlgorithmException(type + " not available", e.getException());
        }
    }

    public abstract CertStore getCertStore(URI uri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    public abstract X509CertSelector wrap(X509CertSelector selector, X500Principal certSubject, String dn) throws IOException;

    public abstract X509CRLSelector wrap(X509CRLSelector selector, Collection<X500Principal> certIssuers, String dn) throws IOException;
}