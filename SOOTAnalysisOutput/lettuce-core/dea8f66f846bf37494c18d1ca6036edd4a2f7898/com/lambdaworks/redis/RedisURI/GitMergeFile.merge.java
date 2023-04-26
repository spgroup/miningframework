package com.lambdaworks.redis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.lambdaworks.redis.LettuceStrings.isEmpty;
import static com.lambdaworks.redis.LettuceStrings.isNotEmpty;
<<<<<<< MINE
=======
import static com.google.common.base.Preconditions.*;
import static com.lambdaworks.redis.LettuceStrings.*;
>>>>>>> YOURS
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.lambdaworks.redis.protocol.LettuceCharsets;

@SuppressWarnings("serial")
public class RedisURI implements Serializable, ConnectionPoint {

    public static final String URI_SCHEME_REDIS_SENTINEL = "redis-sentinel";

    public static final String URI_SCHEME_REDIS = "redis";

    public static final String URI_SCHEME_REDIS_SECURE = "rediss";

    public static final String URI_SCHEME_REDIS_SECURE_ALT = "redis+ssl";

    public static final String URI_SCHEME_REDIS_TLS_ALT = "redis+tls";

    public static final String URI_SCHEME_REDIS_SOCKET = "redis-socket";

    public static final String URI_SCHEME_REDIS_SOCKET_ALT = "redis+socket";

    public static final String PARAMETER_NAME_TIMEOUT = "timeout";

    public static final String PARAMETER_NAME_DATABASE = "database";

    public static final String PARAMETER_NAME_DATABASE_ALT = "db";

    public static final String PARAMETER_NAME_SENTINEL_MASTER_ID = "sentinelMasterId";

    public static final Map<String, TimeUnit> TIME_UNIT_MAP;

    static {
        Map<String, TimeUnit> unitMap = new HashMap<String, TimeUnit>();
        unitMap.put("ns", TimeUnit.NANOSECONDS);
        unitMap.put("us", TimeUnit.MICROSECONDS);
        unitMap.put("ms", TimeUnit.MILLISECONDS);
        unitMap.put("s", TimeUnit.SECONDS);
        unitMap.put("m", TimeUnit.MINUTES);
        unitMap.put("h", TimeUnit.HOURS);
        unitMap.put("d", TimeUnit.DAYS);
        TIME_UNIT_MAP = Collections.unmodifiableMap(unitMap);
    }

    public static final int DEFAULT_SENTINEL_PORT = 26379;

    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final long DEFAULT_TIMEOUT = 60;

    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private String host;

    private String socket;

    private String sentinelMasterId;

    private int port;

    private int database;

    private char[] password;

    private boolean ssl = false;

    private boolean verifyPeer = true;

    private boolean startTls = false;

    private long timeout = 60;

    private TimeUnit unit = TimeUnit.SECONDS;

    private final List<RedisURI> sentinels = new ArrayList<RedisURI>();

    public RedisURI() {
    }

    public RedisURI(String host, int port, long timeout, TimeUnit unit) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.unit = unit;
    }

    public static RedisURI create(String host, int port) {
        return new Builder().redis(host, port).build();
    }

    public static RedisURI create(String uri) {
        return create(URI.create(uri));
    }

    public static RedisURI create(URI uri) {
        RedisURI.Builder builder;
        if (uri.getScheme().equals(URI_SCHEME_REDIS_SENTINEL)) {
            builder = configureSentinel(uri);
        } else {
            builder = configureStandalone(uri);
        }
        String userInfo = uri.getUserInfo();
        if (isEmpty(userInfo) && isNotEmpty(uri.getAuthority()) && uri.getAuthority().indexOf('@') > 0) {
            userInfo = uri.getAuthority().substring(0, uri.getAuthority().indexOf('@'));
        }
        if (isNotEmpty(userInfo)) {
            String password = userInfo;
            if (password.startsWith(":")) {
                password = password.substring(1);
            } else {
                int index = password.indexOf(':');
                if (index > 0) {
                    password = password.substring(index + 1);
                }
            }
            if (password != null && !password.equals("")) {
                builder.withPassword(password);
            }
        }
        if (isNotEmpty(uri.getPath()) && builder.socket == null) {
            String pathSuffix = uri.getPath().substring(1);
            if (isNotEmpty(pathSuffix)) {
                builder.withDatabase(Integer.parseInt(pathSuffix));
            }
        }
        if (isNotEmpty(uri.getQuery())) {
            StringTokenizer st = new StringTokenizer(uri.getQuery(), "&;");
            while (st.hasMoreTokens()) {
                String queryParam = st.nextToken();
                String forStartWith = queryParam.toLowerCase();
                if (forStartWith.startsWith(PARAMETER_NAME_TIMEOUT + "=")) {
                    parseTimeout(builder, queryParam.toLowerCase());
                }
                if (forStartWith.startsWith(PARAMETER_NAME_DATABASE + "=") || queryParam.startsWith(PARAMETER_NAME_DATABASE_ALT + "=")) {
                    parseDatabase(builder, queryParam);
                }
                if (forStartWith.startsWith(PARAMETER_NAME_SENTINEL_MASTER_ID.toLowerCase() + "=")) {
                    parseSentinelMasterId(builder, queryParam);
                }
            }
        }
        if (uri.getScheme().equals(URI_SCHEME_REDIS_SENTINEL)) {
            checkArgument(isNotEmpty(builder.sentinelMasterId), "URI must contain the sentinelMasterId");
        }
        return builder.build();
    }

    private static void parseTimeout(Builder builder, String queryParam) {
        int index = queryParam.indexOf('=');
        if (index < 0) {
            return;
        }
        String timeoutString = queryParam.substring(index + 1);
        int numbersEnd = 0;
        while (numbersEnd < timeoutString.length() && Character.isDigit(timeoutString.charAt(numbersEnd))) {
            numbersEnd++;
        }
        if (numbersEnd == 0) {
            if (timeoutString.startsWith("-")) {
                builder.withTimeout(0, TimeUnit.MILLISECONDS);
            } else {
            }
        } else {
            String timeoutValueString = timeoutString.substring(0, numbersEnd);
            long timeoutValue = Long.parseLong(timeoutValueString);
            builder.withTimeout(timeoutValue, TimeUnit.MILLISECONDS);
            String suffix = timeoutString.substring(numbersEnd);
            TimeUnit timeoutUnit = TIME_UNIT_MAP.get(suffix);
            if (timeoutUnit == null) {
                timeoutUnit = TimeUnit.MILLISECONDS;
            }
            builder.withTimeout(timeoutValue, timeoutUnit);
        }
    }

    private static void parseDatabase(Builder builder, String queryParam) {
        int index = queryParam.indexOf('=');
        if (index < 0) {
            return;
        }
        String databaseString = queryParam.substring(index + 1);
        int numbersEnd = 0;
        while (numbersEnd < databaseString.length() && Character.isDigit(databaseString.charAt(numbersEnd))) {
            numbersEnd++;
        }
        if (numbersEnd != 0) {
            String databaseValueString = databaseString.substring(0, numbersEnd);
            int value = Integer.parseInt(databaseValueString);
            builder.withDatabase(value);
        }
    }

    private static void parseSentinelMasterId(Builder builder, String queryParam) {
        int index = queryParam.indexOf('=');
        if (index < 0) {
            return;
        }
        String masterIdString = queryParam.substring(index + 1);
        if (isNotEmpty(masterIdString)) {
            builder.withSentinelMasterId(masterIdString);
        }
    }

    private static Builder configureStandalone(URI uri) {
        Builder builder;
        Set<String> allowedSchemes = ImmutableSet.of(URI_SCHEME_REDIS, URI_SCHEME_REDIS_SECURE, URI_SCHEME_REDIS_SOCKET, URI_SCHEME_REDIS_SOCKET_ALT, URI_SCHEME_REDIS_SECURE_ALT, URI_SCHEME_REDIS_TLS_ALT);
        if (!allowedSchemes.contains(uri.getScheme())) {
            throw new IllegalArgumentException("Scheme " + uri.getScheme() + " not supported");
        }
        if (URI_SCHEME_REDIS_SOCKET.equals(uri.getScheme()) || URI_SCHEME_REDIS_SOCKET_ALT.equals(uri.getScheme())) {
            builder = Builder.socket(uri.getPath());
        } else {
            if (uri.getPort() > 0) {
                builder = Builder.redis(uri.getHost(), uri.getPort());
            } else {
                builder = Builder.redis(uri.getHost());
            }
        }
        if (URI_SCHEME_REDIS_SECURE.equals(uri.getScheme()) || URI_SCHEME_REDIS_SECURE_ALT.equals(uri.getScheme())) {
<<<<<<< MINE
=======
            builder.withSsl(true);
        }
        if (URI_SCHEME_REDIS_TLS_ALT.equals(uri.getScheme())) {
>>>>>>> YOURS
            builder.withSsl(true);
            builder.withStartTls(true);
        }
        if (URI_SCHEME_REDIS_TLS_ALT.equals(uri.getScheme())) {
            builder.withSsl(true);
            builder.withStartTls(true);
        }
        return builder;
    }

    private static RedisURI.Builder configureSentinel(URI uri) {
        String masterId = uri.getFragment();
        RedisURI.Builder builder = null;
        if (isNotEmpty(uri.getHost())) {
            if (uri.getPort() != -1) {
                builder = RedisURI.Builder.sentinel(uri.getHost(), uri.getPort(), masterId);
            } else {
                builder = RedisURI.Builder.sentinel(uri.getHost(), masterId);
            }
        }
        if (builder == null && isNotEmpty(uri.getAuthority())) {
            String authority = uri.getAuthority();
            if (authority.indexOf('@') > -1) {
                authority = authority.substring(authority.indexOf('@') + 1);
            }
            String[] hosts = authority.split("\\,");
            for (String host : hosts) {
                HostAndPort hostAndPort = HostAndPort.fromString(host);
                if (builder == null) {
                    if (hostAndPort.hasPort()) {
                        builder = RedisURI.Builder.sentinel(hostAndPort.getHostText(), hostAndPort.getPort(), masterId);
                    } else {
                        builder = RedisURI.Builder.sentinel(hostAndPort.getHostText(), masterId);
                    }
                } else {
                    if (hostAndPort.hasPort()) {
                        builder.withSentinel(hostAndPort.getHostText(), hostAndPort.getPort());
                    } else {
                        builder.withSentinel(hostAndPort.getHostText());
                    }
                }
            }
        }
        checkArgument(builder != null, "Invalid URI, cannot get host part");
        return builder;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSentinelMasterId() {
        return sentinelMasterId;
    }

    public void setSentinelMasterId(String sentinelMasterId) {
        this.sentinelMasterId = sentinelMasterId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isVerifyPeer() {
        return verifyPeer;
    }

    public void setVerifyPeer(boolean verifyPeer) {
        this.verifyPeer = verifyPeer;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public List<RedisURI> getSentinels() {
        return sentinels;
    }

    public URI toURI() {
        String scheme = getScheme();
        String authority = getAuthority(scheme);
        String queryString = getQueryString();
        String uri = scheme + "://" + authority;
        if (!queryString.isEmpty()) {
            uri += "?" + queryString;
        }
        return URI.create(uri);
    }

    private String getAuthority(final String scheme) {
        String authority = null;
        if (host != null) {
            authority = urlEncode(host) + getPortPart(port, scheme);
        }
        if (sentinels.size() != 0) {
            List<String> strings = Lists.transform(sentinels, new Function<RedisURI, String>() {

                @Nullable
                @Override
                public String apply(RedisURI input) {
                    return urlEncode(input.getHost()) + getPortPart(input.getPort(), scheme);
                }
            });
            authority = Joiner.on(',').join(strings);
        }
        if (socket != null) {
            authority = urlEncode(socket);
        }
        if (password != null && password.length != 0) {
            authority = urlEncode(new String(password)) + "@" + authority;
        }
        return authority;
    }

    private String getQueryString() {
        List<String> queryPairs = Lists.newArrayList();
        if (database != 0) {
            queryPairs.add(PARAMETER_NAME_DATABASE + "=" + database);
<<<<<<< MINE
        }
        if (sentinelMasterId != null) {
            queryPairs.add(PARAMETER_NAME_SENTINEL_MASTER_ID + "=" + urlEncode(sentinelMasterId));
        }
        if (timeout != 0 && unit != null && (timeout != DEFAULT_TIMEOUT && !unit.equals(DEFAULT_TIMEOUT_UNIT))) {
            queryPairs.add(PARAMETER_NAME_TIMEOUT + "=" + timeout + toQueryParamUnit(unit));
        }
        return Joiner.on('&').join(queryPairs);
    }

    private String getPortPart(int port, String scheme) {
        if (URI_SCHEME_REDIS_SENTINEL.equals(scheme) && port == DEFAULT_SENTINEL_PORT) {
            return "";
        }
        if (URI_SCHEME_REDIS.equals(scheme) && port == DEFAULT_REDIS_PORT) {
            return "";
        }
        return ":" + port;
    }

    private String getScheme() {
        String scheme = URI_SCHEME_REDIS;
        if (isSsl()) {
            if (isStartTls()) {
                scheme = URI_SCHEME_REDIS_TLS_ALT;
            } else {
                scheme = URI_SCHEME_REDIS_SECURE;
            }
        }
        if (socket != null) {
            scheme = URI_SCHEME_REDIS_SOCKET;
        }
        if (host == null && !sentinels.isEmpty()) {
            scheme = URI_SCHEME_REDIS_SENTINEL;
        }
        return scheme;
    }

=======
        }
        if (sentinelMasterId != null) {
            queryPairs.add(PARAMETER_NAME_SENTINEL_MASTER_ID + "=" + urlEncode(sentinelMasterId));
        }
        if (timeout != 0 && unit != null && (timeout != DEFAULT_TIMEOUT && !unit.equals(DEFAULT_TIMEOUT_UNIT))) {
            queryPairs.add(PARAMETER_NAME_TIMEOUT + "=" + timeout + toQueryParamUnit(unit));
        }
        return Joiner.on('&').join(queryPairs);
    }

    private String getPortPart(int port, String scheme) {
        if (URI_SCHEME_REDIS_SENTINEL.equals(scheme) && port == DEFAULT_SENTINEL_PORT) {
            return "";
        }
        if (URI_SCHEME_REDIS.equals(scheme) && port == DEFAULT_REDIS_PORT) {
            return "";
        }
        return ":" + port;
    }

    private String getScheme() {
        String scheme = URI_SCHEME_REDIS;
        if (isSsl()) {
            if (isStartTls()) {
                scheme = URI_SCHEME_REDIS_TLS_ALT;
            } else {
                scheme = URI_SCHEME_REDIS_SECURE;
            }
        }
        if (socket != null) {
            scheme = URI_SCHEME_REDIS_SOCKET;
        }
        if (host == null && !sentinels.isEmpty()) {
            scheme = URI_SCHEME_REDIS_SENTINEL;
        }
        return scheme;
    }

>>>>>>> YOURS
    private String toQueryParamUnit(TimeUnit unit) {
        for (Map.Entry<String, TimeUnit> entry : TIME_UNIT_MAP.entrySet()) {
            if (entry.getValue().equals(unit)) {
                return entry.getKey();
            }
        }
        return "";
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, LettuceCharsets.UTF8.name()).replaceAll("%2F", "/");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public SocketAddress getResolvedAddress() {
        if (getSocket() != null) {
            return EpollProvider.newSocketAddress(getSocket());
        }
        return new InetSocketAddress(host, port);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        if (host != null) {
            sb.append("host='").append(host).append('\'');
            sb.append(", port=").append(port);
        }
        if (socket != null) {
            sb.append("socket='").append(socket).append('\'');
        }
        if (sentinelMasterId != null) {
            sb.append("sentinels=").append(getSentinels());
            sb.append(", sentinelMasterId=").append(sentinelMasterId);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RedisURI))
            return false;
        RedisURI redisURI = (RedisURI) o;
        if (port != redisURI.port)
            return false;
        if (database != redisURI.database)
            return false;
        if (host != null ? !host.equals(redisURI.host) : redisURI.host != null)
            return false;
        if (socket != null ? !socket.equals(redisURI.socket) : redisURI.socket != null)
            return false;
        if (sentinelMasterId != null ? !sentinelMasterId.equals(redisURI.sentinelMasterId) : redisURI.sentinelMasterId != null)
            return false;
        return !(sentinels != null ? !sentinels.equals(redisURI.sentinels) : redisURI.sentinels != null);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (socket != null ? socket.hashCode() : 0);
        result = 31 * result + (sentinelMasterId != null ? sentinelMasterId.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + database;
        result = 31 * result + (sentinels != null ? sentinels.hashCode() : 0);
        return result;
    }

    public static class Builder {

        private String host;

        private String socket;

        private String sentinelMasterId;

        private int port;

        private int database;

        private char[] password;

        private boolean ssl = false;

        private boolean verifyPeer = true;

        private boolean startTls = false;

        private long timeout = 60;

        private TimeUnit unit = TimeUnit.SECONDS;

        private final List<HostAndPort> sentinels = new ArrayList<HostAndPort>();

        public static Builder socket(String socket) {
            checkNotNull(socket, "Socket must not be null");
            Builder builder = new Builder();
            builder.socket = socket;
            return builder;
        }

        public static Builder redis(String host) {
            return redis(host, DEFAULT_REDIS_PORT);
        }

        public static Builder redis(String host, int port) {
            checkNotNull(host, "Host must not be null");
            Builder builder = new Builder();
            builder.host = host;
            builder.port = port;
            return builder;
        }

        public static Builder sentinel(String host) {
            return sentinel(host, DEFAULT_SENTINEL_PORT, null);
        }

        public static Builder sentinel(String host, int port) {
            return sentinel(host, port, null);
        }

        public static Builder sentinel(String host, String masterId) {
            return sentinel(host, DEFAULT_SENTINEL_PORT, masterId);
        }

        public static Builder sentinel(String host, int port, String masterId) {
            checkNotNull(host, "Host must not be null");
            Builder builder = new Builder();
            builder.sentinelMasterId = masterId;
            builder.withSentinel(host, port);
            return builder;
        }

        public Builder withSentinel(String host) {
            return withSentinel(host, DEFAULT_SENTINEL_PORT);
        }

        public Builder withSentinel(String host, int port) {
            checkState(this.host == null, "Cannot use with Redis mode.");
            checkNotNull(host, "Host must not be null");
            sentinels.add(HostAndPort.fromParts(host, port));
            return this;
        }

        public Builder withPort(int port) {
            checkState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            this.port = port;
            return this;
        }

        public Builder withSsl(boolean ssl) {
            checkState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            this.ssl = ssl;
            return this;
        }

        public Builder withStartTls(boolean startTls) {
            checkState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            this.startTls = startTls;
            return this;
        }

        public Builder withVerifyPeer(boolean verifyPeer) {
            checkState(this.host != null, "Host is null. Cannot use in Sentinel mode.");
            this.verifyPeer = verifyPeer;
            return this;
        }

        public Builder withDatabase(int database) {
            checkArgument(database >= 0 && database <= 15, "Invalid database number: " + database);
            this.database = database;
            return this;
        }

        public Builder withPassword(String password) {
            checkNotNull(password, "Password must not be null");
            this.password = password.toCharArray();
            return this;
        }

        public Builder withTimeout(long timeout, TimeUnit unit) {
            checkNotNull(unit, "TimeUnit must not be null");
            checkArgument(timeout >= 0, "Timeout must be greater or equal 0");
            this.timeout = timeout;
            this.unit = unit;
            return this;
        }

        public Builder withSentinelMasterId(String sentinelMasterId) {
            checkNotNull(sentinelMasterId, "Sentinel master id must not ne null");
            this.sentinelMasterId = sentinelMasterId;
            return this;
        }

        public RedisURI build() {
            RedisURI redisURI = new RedisURI();
            redisURI.setHost(host);
            redisURI.setPort(port);
            redisURI.password = password;
            redisURI.setDatabase(database);
            redisURI.setSentinelMasterId(sentinelMasterId);
            for (HostAndPort sentinel : sentinels) {
                redisURI.getSentinels().add(new RedisURI(sentinel.getHostText(), sentinel.getPort(), timeout, unit));
            }
            redisURI.setSocket(socket);
            redisURI.setSsl(ssl);
            redisURI.setStartTls(startTls);
            redisURI.setVerifyPeer(verifyPeer);
            redisURI.setTimeout(timeout);
            redisURI.setUnit(unit);
            return redisURI;
        }
    }
}
