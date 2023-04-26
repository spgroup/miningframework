package sun.net.www.protocol.http.ntlm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Objects;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.AuthenticationInfo;
import sun.net.www.protocol.http.AuthScheme;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;

public class NTLMAuthentication extends AuthenticationInfo {

    private static final long serialVersionUID = 100L;

    private static final NTLMAuthenticationCallback NTLMAuthCallback = NTLMAuthenticationCallback.getNTLMAuthenticationCallback();

    private String hostname;

    private static String defaultDomain;

    static {
        defaultDomain = GetPropertyAction.privilegedGetProperty("http.auth.ntlm.domain", "domain");
    }

    private void init0() {
        hostname = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {

            public String run() {
                String localhost;
                try {
                    localhost = InetAddress.getLocalHost().getHostName().toUpperCase();
                } catch (UnknownHostException e) {
                    localhost = "localhost";
                }
                return localhost;
            }
        });
        int x = hostname.indexOf('.');
        if (x != -1) {
            hostname = hostname.substring(0, x);
        }
    }

    String username;

    String ntdomain;

    String password;

    public NTLMAuthentication(boolean isProxy, URL url, PasswordAuthentication pw, String authenticatorKey) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION, AuthScheme.NTLM, url, "", Objects.requireNonNull(authenticatorKey));
        init(pw);
    }

    private void init(PasswordAuthentication pw) {
        this.pw = pw;
        if (pw != null) {
            String s = pw.getUserName();
            int i = s.indexOf('\\');
            if (i == -1) {
                username = s;
                ntdomain = defaultDomain;
            } else {
                ntdomain = s.substring(0, i).toUpperCase();
                username = s.substring(i + 1);
            }
            password = new String(pw.getPassword());
        } else {
            username = null;
            ntdomain = null;
            password = null;
        }
        init0();
    }

    public NTLMAuthentication(boolean isProxy, String host, int port, PasswordAuthentication pw, String authenticatorKey) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION, AuthScheme.NTLM, host, port, "", Objects.requireNonNull(authenticatorKey));
        init(pw);
    }

    @Override
    public boolean supportsPreemptiveAuthorization() {
        return false;
    }

    public static boolean supportsTransparentAuth() {
        return true;
    }

    public static boolean isTrustedSite(URL url) {
        return NTLMAuthCallback.isTrustedSite(url);
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
            NTLMAuthSequence seq = (NTLMAuthSequence) conn.authObj();
            if (seq == null) {
                seq = new NTLMAuthSequence(username, password, ntdomain);
                conn.authObj(seq);
            }
            String response = "NTLM " + seq.getAuthHeader(raw.length() > 6 ? raw.substring(5) : null);
            conn.setAuthenticationProperty(getHeaderName(), response);
            if (seq.isComplete()) {
                conn.authObj(null);
            }
            return true;
        } catch (IOException e) {
            conn.authObj(null);
            return false;
        }
    }
}
