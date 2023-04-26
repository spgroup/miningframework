package jdk.internal.net.http.common;

import sun.net.NetProperties;
import sun.net.util.IPAddressUtil;
import sun.net.www.HeaderParser;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.System.Logger.Level;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLPermission;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import jdk.internal.net.http.HttpRequestImpl;

public final class Utils {

    public static final boolean ASSERTIONSENABLED;

    static {
        boolean enabled = false;
        assert enabled = true;
        ASSERTIONSENABLED = enabled;
    }

    public static final boolean DEBUG = getBooleanProperty(DebugLogger.HTTP_NAME, false);

    public static final boolean DEBUG_WS = getBooleanProperty(DebugLogger.WS_NAME, false);

    public static final boolean DEBUG_HPACK = getBooleanProperty(DebugLogger.HPACK_NAME, false);

    public static final boolean TESTING = DEBUG;

    public static final boolean isHostnameVerificationDisabled = hostnameVerificationDisabledValue();

    private static boolean hostnameVerificationDisabledValue() {
        String prop = getProperty("jdk.internal.httpclient.disableHostnameVerification");
        if (prop == null)
            return false;
        return prop.isEmpty() ? true : Boolean.parseBoolean(prop);
    }

    private static final int DEFAULT_BUFSIZE = 16 * 1024;

    public static final int BUFSIZE = getIntegerNetProperty("jdk.httpclient.bufsize", DEFAULT_BUFSIZE);

    public static final BiPredicate<String, String> ACCEPT_ALL = (x, y) -> true;

    private static final Set<String> DISALLOWED_HEADERS_SET = getDisallowedHeaders();

    private static Set<String> getDisallowedHeaders() {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        headers.addAll(Set.of("connection", "content-length", "expect", "host", "upgrade"));
        String v = getNetProperty("jdk.httpclient.allowRestrictedHeaders");
        if (v != null) {
            String[] tokens = v.trim().split(",");
            for (String token : tokens) {
                headers.remove(token);
            }
            return Collections.unmodifiableSet(headers);
        } else {
            return Collections.unmodifiableSet(headers);
        }
    }

    public static final BiPredicate<String, String> ALLOWED_HEADERS = (header, unused) -> !DISALLOWED_HEADERS_SET.contains(header);

    public static final BiPredicate<String, String> VALIDATE_USER_HEADER = (name, value) -> {
        assert name != null : "null header name";
        assert value != null : "null header value";
        if (!isValidName(name)) {
            throw newIAE("invalid header name: \"%s\"", name);
        }
        if (!Utils.ALLOWED_HEADERS.test(name, null)) {
            throw newIAE("restricted header name: \"%s\"", name);
        }
        if (!isValidValue(value)) {
            throw newIAE("invalid header value for %s: \"%s\"", name, value);
        }
        return true;
    };

    public static final BiPredicate<String, String> CONTEXT_RESTRICTED(HttpClient client) {
        return (k, v) -> client.authenticator() == null || !(k.equalsIgnoreCase("Authorization") && k.equalsIgnoreCase("Proxy-Authorization"));
    }

    private static final BiPredicate<String, String> HOST_RESTRICTED = (k, v) -> !"host".equalsIgnoreCase(k);

    public static final BiPredicate<String, String> PROXY_TUNNEL_RESTRICTED(HttpClient client) {
        return CONTEXT_RESTRICTED(client).and(HOST_RESTRICTED);
    }

    private static final Predicate<String> IS_HOST = "host"::equalsIgnoreCase;

    private static final Predicate<String> IS_PROXY_HEADER = (k) -> k != null && k.length() > 6 && "proxy-".equalsIgnoreCase(k.substring(0, 6));

    private static final Predicate<String> NO_PROXY_HEADER = IS_PROXY_HEADER.negate();

    private static final Predicate<String> ALL_HEADERS = (s) -> true;

    private static final Set<String> PROXY_AUTH_DISABLED_SCHEMES;

    private static final Set<String> PROXY_AUTH_TUNNEL_DISABLED_SCHEMES;

    static {
        String proxyAuthDisabled = getNetProperty("jdk.http.auth.proxying.disabledSchemes");
        String proxyAuthTunnelDisabled = getNetProperty("jdk.http.auth.tunneling.disabledSchemes");
        PROXY_AUTH_DISABLED_SCHEMES = proxyAuthDisabled == null ? Set.of() : Stream.of(proxyAuthDisabled.split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toUnmodifiableSet());
        PROXY_AUTH_TUNNEL_DISABLED_SCHEMES = proxyAuthTunnelDisabled == null ? Set.of() : Stream.of(proxyAuthTunnelDisabled.split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toUnmodifiableSet());
    }

    public static <T> CompletableFuture<T> wrapForDebug(Logger logger, String name, CompletableFuture<T> cf) {
        if (logger.on()) {
            return cf.handle((r, t) -> {
                logger.log("%s completed %s", name, t == null ? "successfully" : t);
                return cf;
            }).thenCompose(Function.identity());
        } else {
            return cf;
        }
    }

    private static final String WSPACES = " \t\r\n";

    private static final boolean isAllowedForProxy(String name, String value, Set<String> disabledSchemes, Predicate<String> allowedKeys) {
        if (!allowedKeys.test(name))
            return false;
        if (disabledSchemes.isEmpty())
            return true;
        if (name.equalsIgnoreCase("proxy-authorization")) {
            if (value.isEmpty())
                return false;
            for (String scheme : disabledSchemes) {
                int slen = scheme.length();
                int vlen = value.length();
                if (vlen == slen) {
                    if (value.equalsIgnoreCase(scheme)) {
                        return false;
                    }
                } else if (vlen > slen) {
                    if (value.substring(0, slen).equalsIgnoreCase(scheme)) {
                        int c = value.codePointAt(slen);
                        if (WSPACES.indexOf(c) > -1 || Character.isSpaceChar(c) || Character.isWhitespace(c)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static final BiPredicate<String, String> PROXY_TUNNEL_FILTER = (s, v) -> isAllowedForProxy(s, v, PROXY_AUTH_TUNNEL_DISABLED_SCHEMES, IS_PROXY_HEADER.or(IS_HOST));

    public static final BiPredicate<String, String> PROXY_FILTER = (s, v) -> isAllowedForProxy(s, v, PROXY_AUTH_DISABLED_SCHEMES, ALL_HEADERS);

    public static final BiPredicate<String, String> NO_PROXY_HEADERS_FILTER = (n, v) -> Utils.NO_PROXY_HEADER.test(n);

    public static boolean proxyHasDisabledSchemes(boolean tunnel) {
        return tunnel ? !PROXY_AUTH_TUNNEL_DISABLED_SCHEMES.isEmpty() : !PROXY_AUTH_DISABLED_SCHEMES.isEmpty();
    }

    private static final String HEADER_CONNECTION = "Connection";

    private static final String HEADER_UPGRADE = "Upgrade";

    public static final void setWebSocketUpgradeHeaders(HttpRequestImpl request) {
        request.setSystemHeader(HEADER_UPGRADE, "websocket");
        request.setSystemHeader(HEADER_CONNECTION, "Upgrade");
    }

    public static IllegalArgumentException newIAE(String message, Object... args) {
        return new IllegalArgumentException(format(message, args));
    }

    public static ByteBuffer getBuffer() {
        return ByteBuffer.allocate(BUFSIZE);
    }

    public static Throwable getCompletionCause(Throwable x) {
        if (!(x instanceof CompletionException) && !(x instanceof ExecutionException))
            return x;
        final Throwable cause = x.getCause();
        if (cause == null) {
            throw new InternalError("Unexpected null cause", x);
        }
        return cause;
    }

    public static IOException getIOException(Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            return getIOException(cause);
        }
        return new IOException(t);
    }

    public static Throwable wrapWithExtraDetail(Throwable t, Supplier<String> messageSupplier) {
        if (!(t instanceof IOException))
            return t;
        if (t instanceof SSLHandshakeException)
            return t;
        String msg = messageSupplier.get();
        if (msg == null)
            return t;
        if (t instanceof ConnectionExpiredException) {
            if (t.getCause() instanceof SSLHandshakeException)
                return t;
            IOException ioe = new IOException(msg, t.getCause());
            t = new ConnectionExpiredException(ioe);
        } else {
            IOException ioe = new IOException(msg, t);
            t = ioe;
        }
        return t;
    }

    private Utils() {
    }

    public static URLPermission permissionForProxy(InetSocketAddress proxyAddress) {
        if (proxyAddress == null)
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append("socket://").append(proxyAddress.getHostString()).append(":").append(proxyAddress.getPort());
        String urlString = sb.toString();
        return new URLPermission(urlString, "CONNECT");
    }

    public static URLPermission permissionForServer(URI uri, String method, Stream<String> headers) {
        String urlString = new StringBuilder().append(uri.getScheme()).append("://").append(uri.getRawAuthority()).append(uri.getRawPath()).toString();
        StringBuilder actionStringBuilder = new StringBuilder(method);
        String collected = headers.collect(joining(","));
        if (!collected.isEmpty()) {
            actionStringBuilder.append(":").append(collected);
        }
        return new URLPermission(urlString, actionStringBuilder.toString());
    }

    private static final boolean[] tchar = new boolean[256];

    private static final boolean[] fieldvchar = new boolean[256];

    static {
        char[] allowedTokenChars = ("!#$%&'*+-.^_`|~0123456789" + "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        for (char c : allowedTokenChars) {
            tchar[c] = true;
        }
        for (char c = 0x21; c < 0xFF; c++) {
            fieldvchar[c] = true;
        }
        fieldvchar[0x7F] = false;
    }

    public static boolean isValidName(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255 || !tchar[c]) {
                return false;
            }
        }
        return !token.isEmpty();
    }

    public static class ServerName {

        ServerName(String name, boolean isLiteral) {
            this.name = name;
            this.isLiteral = isLiteral;
        }

        final String name;

        final boolean isLiteral;

        public String getName() {
            return name;
        }

        public boolean isLiteral() {
            return isLiteral;
        }
    }

    public static ServerName getServerName(InetSocketAddress addr) {
        String host = addr.getHostString();
        byte[] literal = IPAddressUtil.textToNumericFormatV4(host);
        if (literal == null) {
            literal = IPAddressUtil.textToNumericFormatV6(host);
            return new ServerName(host, literal != null);
        } else {
            return new ServerName(host, true);
        }
    }

    private static boolean isLoopbackLiteral(byte[] bytes) {
        if (bytes.length == 4) {
            return bytes[0] == 127;
        } else if (bytes.length == 16) {
            for (int i = 0; i < 14; i++) if (bytes[i] != 0)
                return false;
            if (bytes[15] != 1)
                return false;
            return true;
        } else
            throw new InternalError();
    }

    public static boolean isValidValue(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255) {
                return false;
            }
            if (c == ' ' || c == '\t') {
                continue;
            } else if (!fieldvchar[c]) {
                return false;
            }
        }
        return true;
    }

    public static int getIntegerNetProperty(String name, int defaultValue) {
        return AccessController.doPrivileged((PrivilegedAction<Integer>) () -> NetProperties.getInteger(name, defaultValue));
    }

    public static String getNetProperty(String name) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> NetProperties.get(name));
    }

    public static boolean getBooleanProperty(String name, boolean def) {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.parseBoolean(System.getProperty(name, String.valueOf(def))));
    }

    public static String getProperty(String name) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(name));
    }

    public static int getIntegerProperty(String name, int defaultValue) {
        return AccessController.doPrivileged((PrivilegedAction<Integer>) () -> Integer.parseInt(System.getProperty(name, String.valueOf(defaultValue))));
    }

    public static SSLParameters copySSLParameters(SSLParameters p) {
        SSLParameters p1 = new SSLParameters();
        p1.setAlgorithmConstraints(p.getAlgorithmConstraints());
        p1.setCipherSuites(p.getCipherSuites());
        p1.setEnableRetransmissions(p.getEnableRetransmissions());
        p1.setMaximumPacketSize(p.getMaximumPacketSize());
        p1.setEndpointIdentificationAlgorithm(p.getEndpointIdentificationAlgorithm());
        p1.setNeedClientAuth(p.getNeedClientAuth());
        String[] protocols = p.getProtocols();
        if (protocols != null) {
            p1.setProtocols(protocols.clone());
        }
        p1.setSNIMatchers(p.getSNIMatchers());
        p1.setServerNames(p.getServerNames());
        p1.setUseCipherSuitesOrder(p.getUseCipherSuitesOrder());
        p1.setWantClientAuth(p.getWantClientAuth());
        return p1;
    }

    public static void flipToMark(ByteBuffer buffer, int mark) {
        buffer.limit(buffer.position());
        buffer.position(mark);
    }

    public static String stackTrace(Throwable t) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String s = null;
        try {
            PrintStream p = new PrintStream(bos, true, "US-ASCII");
            t.printStackTrace(p);
            s = bos.toString("US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
        return s;
    }

    public static int copy(ByteBuffer src, ByteBuffer dst) {
        int srcLen = src.remaining();
        int dstLen = dst.remaining();
        if (srcLen > dstLen) {
            int diff = srcLen - dstLen;
            int limit = src.limit();
            src.limit(limit - diff);
            dst.put(src);
            src.limit(limit);
        } else {
            dst.put(src);
        }
        return srcLen - src.remaining();
    }

    private static final int COPY_THRESHOLD = 8192;

    public static long accumulateBuffers(List<ByteBuffer> currentList, List<ByteBuffer> buffersToAdd) {
        long accumulatedBytes = 0;
        for (ByteBuffer bufferToAdd : buffersToAdd) {
            int remaining = bufferToAdd.remaining();
            if (remaining <= 0)
                continue;
            int listSize = currentList.size();
            if (listSize == 0) {
                currentList.add(bufferToAdd);
                accumulatedBytes = remaining;
                continue;
            }
            ByteBuffer lastBuffer = currentList.get(listSize - 1);
            int freeSpace = lastBuffer.capacity() - lastBuffer.limit();
            if (remaining <= COPY_THRESHOLD && freeSpace >= remaining) {
                int position = lastBuffer.position();
                int limit = lastBuffer.limit();
                lastBuffer.position(limit);
                lastBuffer.limit(limit + remaining);
                lastBuffer.put(bufferToAdd);
                lastBuffer.position(position);
            } else {
                currentList.add(bufferToAdd);
            }
            accumulatedBytes += remaining;
        }
        return accumulatedBytes;
    }

    public static ByteBuffer copy(ByteBuffer src) {
        ByteBuffer dst = ByteBuffer.allocate(src.remaining());
        dst.put(src);
        dst.flip();
        return dst;
    }

    public static ByteBuffer copyAligned(ByteBuffer src) {
        int len = src.remaining();
        int size = ((len + 7) >> 3) << 3;
        assert size >= len;
        ByteBuffer dst = ByteBuffer.allocate(size);
        dst.put(src);
        dst.flip();
        return dst;
    }

    public static String dump(Object... objects) {
        return Arrays.toString(objects);
    }

    public static String stringOf(Collection<?> source) {
        return Arrays.toString(source.toArray());
    }

    public static long remaining(ByteBuffer[] bufs) {
        long remain = 0;
        for (ByteBuffer buf : bufs) {
            remain += buf.remaining();
        }
        return remain;
    }

    public static boolean hasRemaining(List<ByteBuffer> bufs) {
        synchronized (bufs) {
            for (ByteBuffer buf : bufs) {
                if (buf.hasRemaining())
                    return true;
            }
        }
        return false;
    }

    public static long remaining(List<ByteBuffer> bufs) {
        long remain = 0;
        synchronized (bufs) {
            for (ByteBuffer buf : bufs) {
                remain += buf.remaining();
            }
        }
        return remain;
    }

    public static int remaining(List<ByteBuffer> bufs, int max) {
        long remain = 0;
        synchronized (bufs) {
            for (ByteBuffer buf : bufs) {
                remain += buf.remaining();
                if (remain > max) {
                    throw new IllegalArgumentException("too many bytes");
                }
            }
        }
        return (int) remain;
    }

    public static int remaining(ByteBuffer[] refs, int max) {
        long remain = 0;
        for (ByteBuffer b : refs) {
            remain += b.remaining();
            if (remain > max) {
                throw new IllegalArgumentException("too many bytes");
            }
        }
        return (int) remain;
    }

    public static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(0);

    public static final ByteBuffer[] EMPTY_BB_ARRAY = new ByteBuffer[0];

    public static final List<ByteBuffer> EMPTY_BB_LIST = List.of();

    public static ByteBuffer sliceWithLimitedCapacity(ByteBuffer buffer, int amount) {
        final int index = buffer.position() + amount;
        final int limit = buffer.limit();
        if (index != limit) {
            buffer.limit(index);
        } else {
            buffer.limit(buffer.capacity());
        }
        ByteBuffer newb = buffer.slice();
        buffer.position(index);
        buffer.limit(limit);
        newb.limit(amount);
        return newb;
    }

    public static Charset charsetFrom(HttpHeaders headers) {
        String type = headers.firstValue("Content-type").orElse("text/html; charset=utf-8");
        int i = type.indexOf(";");
        if (i >= 0)
            type = type.substring(i + 1);
        try {
            HeaderParser parser = new HeaderParser(type);
            String value = parser.findValue("charset");
            if (value == null)
                return StandardCharsets.UTF_8;
            return Charset.forName(value);
        } catch (Throwable x) {
            Log.logTrace("Can't find charset in \"{0}\" ({1})", type, x);
            return StandardCharsets.UTF_8;
        }
    }

    public static UncheckedIOException unchecked(IOException e) {
        return new UncheckedIOException(e);
    }

    public static Logger getDebugLogger(Supplier<String> dbgTag) {
        return getDebugLogger(dbgTag, DEBUG);
    }

    static Logger getDebugLogger(Supplier<String> dbgTag, Level errLevel) {
        return DebugLogger.createHttpLogger(dbgTag, Level.OFF, errLevel);
    }

    public static Logger getDebugLogger(Supplier<String> dbgTag, boolean on) {
        Level errLevel = on ? Level.ALL : Level.OFF;
        return getDebugLogger(dbgTag, errLevel);
    }

    public static String hostString(HttpRequestImpl request) {
        URI uri = request.uri();
        int port = uri.getPort();
        String host = uri.getHost();
        boolean defaultPort;
        if (port == -1) {
            defaultPort = true;
        } else if (uri.getScheme().equalsIgnoreCase("https")) {
            defaultPort = port == 443;
        } else {
            defaultPort = port == 80;
        }
        if (defaultPort) {
            return host;
        } else {
            return host + ":" + Integer.toString(port);
        }
    }

    public static Logger getHpackLogger(Supplier<String> dbgTag, Level errLevel) {
        Level outLevel = Level.OFF;
        return DebugLogger.createHpackLogger(dbgTag, outLevel, errLevel);
    }

    public static Logger getHpackLogger(Supplier<String> dbgTag, boolean on) {
        Level errLevel = on ? Level.ALL : Level.OFF;
        return getHpackLogger(dbgTag, errLevel);
    }

    public static Logger getWebSocketLogger(Supplier<String> dbgTag, Level errLevel) {
        Level outLevel = Level.OFF;
        return DebugLogger.createWebSocketLogger(dbgTag, outLevel, errLevel);
    }

    public static Logger getWebSocketLogger(Supplier<String> dbgTag, boolean on) {
        Level errLevel = on ? Level.ALL : Level.OFF;
        return getWebSocketLogger(dbgTag, errLevel);
    }

    public static SSLSession immutableSession(SSLSession session) {
        if (session instanceof ExtendedSSLSession)
            return new ImmutableExtendedSSLSession((ExtendedSSLSession) session);
        else
            return new ImmutableSSLSession(session);
    }

    public static boolean isHostnameVerificationDisabled() {
        return isHostnameVerificationDisabled;
    }

    public static InetSocketAddress resolveAddress(InetSocketAddress address) {
        if (address != null && address.isUnresolved()) {
            address = new InetSocketAddress(address.getHostString(), address.getPort());
        }
        return address;
    }

    public static Throwable toConnectException(Throwable e) {
        if (e == null)
            return null;
        e = getCompletionCause(e);
        if (e instanceof ConnectException)
            return e;
        if (e instanceof SecurityException)
            return e;
        if (e instanceof SSLException)
            return e;
        if (e instanceof Error)
            return e;
        if (e instanceof HttpTimeoutException)
            return e;
        Throwable cause = e;
        e = new ConnectException(e.getMessage());
        e.initCause(cause);
        return e;
    }

    public static int pow2Size(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        } else if (n == 0) {
            return 1;
        } else if (n >= (1 << 30)) {
            return 1 << 30;
        } else {
            return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
        }
    }

    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static void appendEscape(StringBuilder sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }

    public static String encode(String s) {
        int n = s.length();
        if (n == 0)
            return s;
        for (int i = 0; ; ) {
            if (s.charAt(i) >= '\u0080')
                break;
            if (++i >= n)
                return s;
        }
        String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
        ByteBuffer bb = null;
        try {
            bb = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(ns));
        } catch (CharacterCodingException x) {
            assert false : x;
        }
        StringBuilder sb = new StringBuilder();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (b >= 0x80)
                appendEscape(sb, (byte) b);
            else
                sb.append((char) b);
        }
        return sb.toString();
    }
}
