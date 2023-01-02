package sun.net.www.protocol.http;

import java.net.URL;
import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.util.Base64;
import java.util.HashMap;
import sun.net.www.HeaderParser;
import sun.util.logging.PlatformLogger;
import static sun.net.www.protocol.http.AuthScheme.NEGOTIATE;
import static sun.net.www.protocol.http.AuthScheme.KERBEROS;

class NegotiateAuthentication extends AuthenticationInfo {

    private static final long serialVersionUID = 100L;

    private static final PlatformLogger logger = HttpURLConnection.getHttpLogger();

    private final HttpCallerInfo hci;

    static HashMap<String, Boolean> supported = null;

    static HashMap<String, Negotiator> cache = null;

    private Negotiator negotiator = null;

    public NegotiateAuthentication(HttpCallerInfo hci) {
        super(RequestorType.PROXY == hci.authType ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION, hci.scheme.equalsIgnoreCase("Negotiate") ? NEGOTIATE : KERBEROS, hci.url, "", AuthenticatorKeys.getKey(hci.authenticator));
        this.hci = hci;
    }

    @Override
    public boolean supportsPreemptiveAuthorization() {
        return false;
    }

    public static boolean isSupported(HttpCallerInfo hci) {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException se) {
            if (logger.isLoggable(PlatformLogger.Level.FINER)) {
                logger.finer("NegotiateAuthentication: " + "Attempt to get the context class loader failed - " + se);
            }
        }
        if (loader != null) {
            synchronized (loader) {
                return isSupportedImpl(hci);
            }
        }
        return isSupportedImpl(hci);
    }

    private static synchronized boolean isSupportedImpl(HttpCallerInfo hci) {
        if (supported == null) {
            supported = new HashMap<String, Boolean>();
            cache = new HashMap<String, Negotiator>();
        }
        String hostname = hci.host;
        hostname = hostname.toLowerCase();
        if (supported.containsKey(hostname)) {
            return supported.get(hostname);
        }
        Negotiator neg = Negotiator.getNegotiator(hci);
        if (neg != null) {
            supported.put(hostname, true);
            cache.put(hostname, neg);
            return true;
        } else {
            supported.put(hostname, false);
            return false;
        }
    }

    @Override
    public String getHeaderValue(URL url, String method) {
        throw new RuntimeException("getHeaderValue not supported");
    }

    @Override
    public boolean isAuthorizationStale(String header) {
        return false;
    }

    @Override
    public synchronized boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        try {
            String response;
            byte[] incoming = null;
            String[] parts = raw.split("\\s+");
            if (parts.length > 1) {
                incoming = Base64.getDecoder().decode(parts[1]);
            }
            response = hci.scheme + " " + Base64.getEncoder().encodeToString(incoming == null ? firstToken() : nextToken(incoming));
            conn.setAuthenticationProperty(getHeaderName(), response);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private byte[] firstToken() throws IOException {
        negotiator = null;
        if (cache != null) {
            synchronized (cache) {
                negotiator = cache.get(getHost());
                if (negotiator != null) {
                    cache.remove(getHost());
                }
            }
        }
        if (negotiator == null) {
            negotiator = Negotiator.getNegotiator(hci);
            if (negotiator == null) {
                IOException ioe = new IOException("Cannot initialize Negotiator");
                throw ioe;
            }
        }
        return negotiator.firstToken();
    }

    private byte[] nextToken(byte[] token) throws IOException {
        return negotiator.nextToken(token);
    }
}
