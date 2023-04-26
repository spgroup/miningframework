package jdk.incubator.http.internal.websocket;

import jdk.incubator.http.internal.common.MinimalFuture;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Version;
import jdk.incubator.http.HttpHeaders;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import jdk.incubator.http.WebSocketHandshakeException;
import jdk.incubator.http.internal.common.Pair;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import static java.lang.String.format;
import static jdk.incubator.http.internal.common.Utils.isValidName;
import static jdk.incubator.http.internal.common.Utils.stringOf;

final class OpeningHandshake {

    private static final String HEADER_CONNECTION = "Connection";

    private static final String HEADER_UPGRADE = "Upgrade";

    private static final String HEADER_ACCEPT = "Sec-WebSocket-Accept";

    private static final String HEADER_EXTENSIONS = "Sec-WebSocket-Extensions";

    private static final String HEADER_KEY = "Sec-WebSocket-Key";

    private static final String HEADER_PROTOCOL = "Sec-WebSocket-Protocol";

    private static final String HEADER_VERSION = "Sec-WebSocket-Version";

    private static final Set<String> FORBIDDEN_HEADERS;

    static {
        FORBIDDEN_HEADERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        FORBIDDEN_HEADERS.addAll(List.of(HEADER_ACCEPT, HEADER_EXTENSIONS, HEADER_KEY, HEADER_PROTOCOL, HEADER_VERSION));
    }

    private static final SecureRandom srandom = new SecureRandom();

    private final MessageDigest sha1;

    private final HttpClient client;

    {
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("Minimum requirements", e);
        }
    }

    private final HttpRequest request;

    private final Collection<String> subprotocols;

    private final String nonce;

    OpeningHandshake(BuilderImpl b) {
        this.client = b.getClient();
        URI httpURI = createRequestURI(b.getUri());
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(httpURI);
        Duration connectTimeout = b.getConnectTimeout();
        if (connectTimeout != null) {
            requestBuilder.timeout(connectTimeout);
        }
        for (Pair<String, String> p : b.getHeaders()) {
            if (FORBIDDEN_HEADERS.contains(p.first)) {
                throw illegal("Illegal header: " + p.first);
            }
            requestBuilder.header(p.first, p.second);
        }
        this.subprotocols = createRequestSubprotocols(b.getSubprotocols());
        if (!this.subprotocols.isEmpty()) {
            String p = this.subprotocols.stream().collect(Collectors.joining(", "));
            requestBuilder.header(HEADER_PROTOCOL, p);
        }
        requestBuilder.header(HEADER_VERSION, "13");
        this.nonce = createNonce();
        requestBuilder.header(HEADER_KEY, this.nonce);
        this.request = requestBuilder.version(Version.HTTP_1_1).GET().build();
        WebSocketRequest r = (WebSocketRequest) this.request;
        r.isWebSocket(true);
        r.setSystemHeader(HEADER_UPGRADE, "websocket");
        r.setSystemHeader(HEADER_CONNECTION, "Upgrade");
    }

    private static Collection<String> createRequestSubprotocols(Collection<String> subprotocols) {
        LinkedHashSet<String> sp = new LinkedHashSet<>(subprotocols.size(), 1);
        for (String s : subprotocols) {
            if (s.trim().isEmpty() || !isValidName(s)) {
                throw illegal("Bad subprotocol syntax: " + s);
            }
            if (!sp.add(s)) {
                throw illegal("Duplicating subprotocol: " + s);
            }
        }
        return Collections.unmodifiableCollection(sp);
    }

    private static URI createRequestURI(URI uri) {
        String s = uri.getScheme();
        if (!("ws".equalsIgnoreCase(s) || "wss".equalsIgnoreCase(s)) || uri.getFragment() != null) {
            throw illegal("Bad URI: " + uri);
        }
        String scheme = "ws".equalsIgnoreCase(s) ? "http" : "https";
        try {
            return new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException e) {
            throw new InternalError(e);
        }
    }

    CompletableFuture<Result> send() {
        return client.sendAsync(this.request, BodyHandler.<Void>discard(null)).thenCompose(this::resultFrom);
    }

    static final class Result {

        final String subprotocol;

        final RawChannel channel;

        private Result(String subprotocol, RawChannel channel) {
            this.subprotocol = subprotocol;
            this.channel = channel;
        }
    }

    private CompletableFuture<Result> resultFrom(HttpResponse<?> response) {
        Result result = null;
        Exception exception = null;
        try {
            result = handleResponse(response);
        } catch (IOException e) {
            exception = e;
        } catch (Exception e) {
            exception = new WebSocketHandshakeException(response).initCause(e);
        }
        if (exception == null) {
            return MinimalFuture.completedFuture(result);
        }
        try {
            ((RawChannel.Provider) response).rawChannel().close();
        } catch (IOException e) {
            exception.addSuppressed(e);
        }
        return MinimalFuture.failedFuture(exception);
    }

    private Result handleResponse(HttpResponse<?> response) throws IOException {
        int c = response.statusCode();
        if (c != 101) {
            throw checkFailed("Unexpected HTTP response status code " + c);
        }
        HttpHeaders headers = response.headers();
        String upgrade = requireSingle(headers, HEADER_UPGRADE);
        if (!upgrade.equalsIgnoreCase("websocket")) {
            throw checkFailed("Bad response field: " + HEADER_UPGRADE);
        }
        String connection = requireSingle(headers, HEADER_CONNECTION);
        if (!connection.equalsIgnoreCase("Upgrade")) {
            throw checkFailed("Bad response field: " + HEADER_CONNECTION);
        }
        requireAbsent(headers, HEADER_VERSION);
        requireAbsent(headers, HEADER_EXTENSIONS);
        String x = this.nonce + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        this.sha1.update(x.getBytes(StandardCharsets.ISO_8859_1));
        String expected = Base64.getEncoder().encodeToString(this.sha1.digest());
        String actual = requireSingle(headers, HEADER_ACCEPT);
        if (!actual.trim().equals(expected)) {
            throw checkFailed("Bad " + HEADER_ACCEPT);
        }
        String subprotocol = checkAndReturnSubprotocol(headers);
        RawChannel channel = ((RawChannel.Provider) response).rawChannel();
        return new Result(subprotocol, channel);
    }

    private String checkAndReturnSubprotocol(HttpHeaders responseHeaders) throws CheckFailedException {
        Optional<String> opt = responseHeaders.firstValue(HEADER_PROTOCOL);
        if (!opt.isPresent()) {
            return "";
        }
        String s = requireSingle(responseHeaders, HEADER_PROTOCOL);
        if (this.subprotocols.contains(s)) {
            return s;
        } else {
            throw checkFailed("Unexpected subprotocol: " + s);
        }
    }

    private static void requireAbsent(HttpHeaders responseHeaders, String headerName) {
        List<String> values = responseHeaders.allValues(headerName);
        if (!values.isEmpty()) {
            throw checkFailed(format("Response field '%s' present: %s", headerName, stringOf(values)));
        }
    }

    private static String requireSingle(HttpHeaders responseHeaders, String headerName) {
        List<String> values = responseHeaders.allValues(headerName);
        if (values.isEmpty()) {
            throw checkFailed("Response field missing: " + headerName);
        } else if (values.size() > 1) {
            throw checkFailed(format("Response field '%s' multivalued: %s", headerName, stringOf(values)));
        }
        return values.get(0);
    }

    private static String createNonce() {
        byte[] bytes = new byte[16];
        OpeningHandshake.srandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static IllegalArgumentException illegal(String message) {
        return new IllegalArgumentException(message);
    }

    private static CheckFailedException checkFailed(String message) {
        throw new CheckFailedException(message);
    }
}