package javax.net.ssl;

import java.net.URL;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;

abstract public class HttpsURLConnection extends HttpURLConnection {

    protected HttpsURLConnection(URL url) {
        super(url);
    }

    public abstract String getCipherSuite();

    public abstract java.security.cert.Certificate[] getLocalCertificates();

    public abstract java.security.cert.Certificate[] getServerCertificates() throws SSLPeerUnverifiedException;

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        java.security.cert.Certificate[] certs = getServerCertificates();
        return ((X500Principal) ((X509Certificate) certs[0]).getSubjectX500Principal());
    }

    public Principal getLocalPrincipal() {
        java.security.cert.Certificate[] certs = getLocalCertificates();
        if (certs != null) {
            return ((X500Principal) ((X509Certificate) certs[0]).getSubjectX500Principal());
        } else {
            return null;
        }
    }

    private static HostnameVerifier defaultHostnameVerifier;

    static {
        try {
            defaultHostnameVerifier = new sun.net.www.protocol.https.DefaultHostnameVerifier();
        } catch (NoClassDefFoundError e) {
            defaultHostnameVerifier = new DefaultHostnameVerifier();
        }
    }

    private static class DefaultHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return false;
        }
    }

    protected HostnameVerifier hostnameVerifier = defaultHostnameVerifier;

    public static void setDefaultHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException("no default HostnameVerifier specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("setHostnameVerifier"));
        }
        defaultHostnameVerifier = v;
    }

    public static HostnameVerifier getDefaultHostnameVerifier() {
        return defaultHostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException("no HostnameVerifier specified");
        }
        hostnameVerifier = v;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    private static SSLSocketFactory defaultSSLSocketFactory = null;

    private SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();

    public static void setDefaultSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException("no default SSLSocketFactory specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        defaultSSLSocketFactory = sf;
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSSLSocketFactory == null) {
            defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }
        return defaultSSLSocketFactory;
    }

    public void setSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException("no SSLSocketFactory specified");
        }
        sslSocketFactory = sf;
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }
}
