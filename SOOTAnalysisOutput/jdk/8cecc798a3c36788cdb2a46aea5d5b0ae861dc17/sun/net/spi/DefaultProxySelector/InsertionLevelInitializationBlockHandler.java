package sun.net.spi;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import sun.net.NetProperties;
import sun.net.SocksProxy;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class DefaultProxySelector extends ProxySelector {

    static final String[][] props = { { "http", "http.proxy", "proxy", "socksProxy" }, { "https", "https.proxy", "proxy", "socksProxy" }, { "ftp", "ftp.proxy", "ftpProxy", "proxy", "socksProxy" }, { "socket", "socksProxy" } };

    private static final String SOCKS_PROXY_VERSION = "socksProxyVersion";

    private static boolean hasSystemProxies = false;

    private static final List<Proxy> NO_PROXY_LIST = List.of(Proxy.NO_PROXY);

    static {
        final String key = "java.net.useSystemProxies";
        Boolean b = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            public Boolean run() {
                return NetProperties.getBoolean(key);
            }
        });
        if (b != null && b.booleanValue()) {
            jdk.internal.loader.BootLoader.loadLibrary("net");
            hasSystemProxies = init();
        }
    }

    public static int socksProxyVersion() {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>() {

            @Override
            public Integer run() {
                return NetProperties.getInteger(SOCKS_PROXY_VERSION, 5);
            }
        });
    }

    static class NonProxyInfo {

        static final String defStringVal = "localhost|127.*|[::1]|0.0.0.0|[::0]";

        String hostsSource;

        Pattern pattern;

        final String property;

        final String defaultVal;

        static NonProxyInfo ftpNonProxyInfo = new NonProxyInfo("ftp.nonProxyHosts", null, null, defStringVal);

        static NonProxyInfo httpNonProxyInfo = new NonProxyInfo("http.nonProxyHosts", null, null, defStringVal);

        static NonProxyInfo socksNonProxyInfo = new NonProxyInfo("socksNonProxyHosts", null, null, defStringVal);

        NonProxyInfo(String p, String s, Pattern pattern, String d) {
            property = p;
            hostsSource = s;
            this.pattern = pattern;
            defaultVal = d;
        }
    }

    public java.util.List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        String protocol = uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            String auth = uri.getAuthority();
            if (auth != null) {
                int i;
                i = auth.indexOf('@');
                if (i >= 0) {
                    auth = auth.substring(i + 1);
                }
                i = auth.lastIndexOf(':');
                if (i >= 0) {
                    auth = auth.substring(0, i);
                }
                host = auth;
            }
        }
        if (protocol == null || host == null) {
            throw new IllegalArgumentException("protocol = " + protocol + " host = " + host);
        }
        NonProxyInfo pinfo = null;
        if ("http".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("https".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.ftpNonProxyInfo;
        } else if ("socket".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.socksNonProxyInfo;
        }
        final String proto = protocol;
        final NonProxyInfo nprop = pinfo;
        final String urlhost = host.toLowerCase();
        Proxy[] proxyArray = AccessController.doPrivileged(new PrivilegedAction<Proxy[]>() {

            public Proxy[] run() {
                int i, j;
                String phost = null;
                int pport = 0;
                String nphosts = null;
                InetSocketAddress saddr = null;
                for (i = 0; i < props.length; i++) {
                    if (props[i][0].equalsIgnoreCase(proto)) {
                        for (j = 1; j < props[i].length; j++) {
                            phost = NetProperties.get(props[i][j] + "Host");
                            if (phost != null && phost.length() != 0)
                                break;
                        }
                        if (phost == null || phost.isEmpty()) {
                            if (hasSystemProxies) {
                                String sproto;
                                if (proto.equalsIgnoreCase("socket"))
                                    sproto = "socks";
                                else
                                    sproto = proto;
                                return getSystemProxies(sproto, urlhost);
                            }
                            return null;
                        }
                        if (nprop != null) {
                            nphosts = NetProperties.get(nprop.property);
                            synchronized (nprop) {
                                if (nphosts == null) {
                                    if (nprop.defaultVal != null) {
                                        nphosts = nprop.defaultVal;
                                    } else {
                                        nprop.hostsSource = null;
                                        nprop.pattern = null;
                                    }
                                } else if (!nphosts.isEmpty()) {
                                    nphosts += "|" + NonProxyInfo.defStringVal;
                                }
                                if (nphosts != null) {
                                    if (!nphosts.equals(nprop.hostsSource)) {
                                        nprop.pattern = toPattern(nphosts);
                                        nprop.hostsSource = nphosts;
                                    }
                                }
                                if (shouldNotUseProxyFor(nprop.pattern, urlhost)) {
                                    return null;
                                }
                            }
                        }
                        pport = NetProperties.getInteger(props[i][j] + "Port", 0).intValue();
                        if (pport == 0 && j < (props[i].length - 1)) {
                            for (int k = 1; k < (props[i].length - 1); k++) {
                                if ((k != j) && (pport == 0))
                                    pport = NetProperties.getInteger(props[i][k] + "Port", 0).intValue();
                            }
                        }
                        if (pport == 0) {
                            if (j == (props[i].length - 1))
                                pport = defaultPort("socket");
                            else
                                pport = defaultPort(proto);
                        }
                        saddr = InetSocketAddress.createUnresolved(phost, pport);
                        if (j == (props[i].length - 1)) {
                            return new Proxy[] { SocksProxy.create(saddr, socksProxyVersion()) };
                        }
                        return new Proxy[] { new Proxy(Proxy.Type.HTTP, saddr) };
                    }
                }
                return null;
            }
        });
        if (proxyArray != null) {
            return Stream.of(proxyArray).distinct().collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
        return NO_PROXY_LIST;
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
    }

    private int defaultPort(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        } else if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            return 80;
        } else if ("socket".equalsIgnoreCase(protocol)) {
            return 1080;
        } else {
            return -1;
        }
    }

    private static native boolean init();

    private synchronized native Proxy[] getSystemProxies(String protocol, String host);

    static boolean shouldNotUseProxyFor(Pattern pattern, String urlhost) {
        if (pattern == null || urlhost.isEmpty())
            return false;
        boolean matches = pattern.matcher(urlhost).matches();
        return matches;
    }

    static Pattern toPattern(String mask) {
        boolean disjunctionEmpty = true;
        StringJoiner joiner = new StringJoiner("|");
        for (String disjunct : mask.split("\\|")) {
            if (disjunct.isEmpty())
                continue;
            disjunctionEmpty = false;
            String regex = disjunctToRegex(disjunct.toLowerCase());
            joiner.add(regex);
        }
        return disjunctionEmpty ? null : Pattern.compile(joiner.toString());
    }

    static String disjunctToRegex(String disjunct) {
        String regex;
        if (disjunct.startsWith("*") && disjunct.endsWith("*")) {
            regex = ".*" + quote(disjunct.substring(1, disjunct.length() - 1)) + ".*";
        } else if (disjunct.startsWith("*")) {
            regex = ".*" + quote(disjunct.substring(1));
        } else if (disjunct.endsWith("*")) {
            regex = quote(disjunct.substring(0, disjunct.length() - 1)) + ".*";
        } else {
            regex = quote(disjunct);
        }
        return regex;
    }
}