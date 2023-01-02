package sun.net.www.protocol.http.ntlm;

import com.sun.security.ntlm.Client;
import com.sun.security.ntlm.NTLMException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.AuthenticationInfo;
import sun.net.www.protocol.http.AuthScheme;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.security.action.GetPropertyAction;

public class NTLMAuthentication extends AuthenticationInfo {

    private static final long serialVersionUID = 170L;

    private static final NTLMAuthenticationCallback NTLMAuthCallback = NTLMAuthenticationCallback.getNTLMAuthenticationCallback();

    private String hostname;

    private static final String defaultDomain;

    private static final boolean ntlmCache;

    static {
        Properties props = GetPropertyAction.privilegedGetProperties();
        defaultDomain = props.getProperty("http.auth.ntlm.domain", "");
        String ntlmCacheProp = props.getProperty("jdk.ntlm.cache", "true");
        ntlmCache = Boolean.parseBoolean(ntlmCacheProp);
    }

    public static boolean supportsTransparentAuth() {
        return false;
    }

    public static boolean isTrustedSite(URL url) {
        return NTLMAuthCallback.isTrustedSite(url);
    }

    private void init0() {
        hostname = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public String run() {
                String localhost;
                try {
                    localhost = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    localhost = "localhost";
                }
                return localhost;
            }
        });
    }

    PasswordAuthentication pw;

    Client client;

    public NTLMAuthentication(boolean isProxy, URL url, PasswordAuthentication pw, String authenticatorKey) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION, AuthScheme.NTLM, url, "", Objects.requireNonNull(authenticatorKey));
        init(pw);
    }

    private void init(PasswordAuthentication pw) {
        String username;
        String ntdomain;
        char[] password;
        this.pw = pw;
        String s = pw.getUserName();
        int i = s.indexOf('\\');
        if (i == -1) {
            username = s;
            ntdomain = defaultDomain;
        } else {
            ntdomain = s.substring(0, i).toUpperCase();
            username = s.substring(i + 1);
        }
        password = pw.getPassword();
        init0();
        try {
            String version = GetPropertyAction.privilegedGetProperty("ntlm.version");
            client = new Client(version, hostname, username, ntdomain, password);
        } catch (NTLMException ne) {
            try {
                client = new Client(null, hostname, username, ntdomain, password);
            } catch (NTLMException ne2) {
                throw new AssertionError("Really?");
            }
        }
    }

    public NTLMAuthentication(boolean isProxy, String host, int port, PasswordAuthentication pw, String authenticatorKey) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION, AuthScheme.NTLM, host, port, "", Objects.requireNonNull(authenticatorKey));
        init(pw);
    }

    @Override
    protected boolean useAuthCache() {
        return ntlmCache && super.useAuthCache();
    }

    @Override
    public boolean supportsPreemptiveAuthorization() {
        return false;
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
            if (raw.length() < 6) {
                response = buildType1Msg();
            } else {
                String msg = raw.substring(5);
                response = buildType3Msg(msg);
            }
            conn.setAuthenticationProperty(getHeaderName(), response);
            return true;
        } catch (IOException e) {
            return false;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private String buildType1Msg() {
        byte[] msg = client.type1();
        String result = "NTLM " + Base64.getEncoder().encodeToString(msg);
        return result;
    }

    private String buildType3Msg(String challenge) throws GeneralSecurityException, IOException {
        byte[] type2 = Base64.getDecoder().decode(challenge);
        byte[] nonce = new byte[8];
        new java.util.Random().nextBytes(nonce);
        byte[] msg = client.type3(type2, nonce);
        String result = "NTLM " + Base64.getEncoder().encodeToString(msg);
        return result;
    }
}