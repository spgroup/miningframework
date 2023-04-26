package com.lambdaworks.redis;

import static com.google.common.base.Preconditions.*;
import static com.lambdaworks.redis.LettuceStrings.*;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;

@SuppressWarnings("serial")
public class RedisURI implements Serializable, ConnectionPoint {

    public static final String URI_SCHEME_REDIS_SENTINEL = "redis-sentinel";

    public static final String URI_SCHEME_REDIS = "redis";

    public static final String URI_SCHEME_REDIS_SECURE = "rediss";

    public static final String URI_SCHEME_REDIS_SOCKET = "redis-socket";

    public static final int DEFAULT_SENTINEL_PORT = 26379;

    public static final int DEFAULT_REDIS_PORT = 6379;

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

    private transient SocketAddress resolvedAddress;

    public RedisURI() {
    }

    public RedisURI(String host, int port, long timeout, TimeUnit unit) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.unit = unit;
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
            }
            builder.withPassword(password);
        }
        if (isNotEmpty(uri.getPath()) && builder.redisURI.getSocket() == null) {
            String pathSuffix = uri.getPath().substring(1);
            if (isNotEmpty(pathSuffix)) {
                builder.withDatabase(Integer.parseInt(pathSuffix));
            }
        }
        return builder.build();
    }

    private static Builder configureStandalone(URI uri) {
        Builder builder;
        Set<String> allowedSchemes = ImmutableSet.of(URI_SCHEME_REDIS, URI_SCHEME_REDIS_SECURE, URI_SCHEME_REDIS_SOCKET);
        if (!allowedSchemes.contains(uri.getScheme())) {
            throw new IllegalArgumentException("Scheme " + uri.getScheme() + " not supported");
        }
        if (URI_SCHEME_REDIS_SOCKET.equals(uri.getScheme())) {
            builder = Builder.socket(uri.getPath());
        } else {
            if (uri.getPort() > 0) {
                builder = Builder.redis(uri.getHost(), uri.getPort());
            } else {
                builder = Builder.redis(uri.getHost());
            }
        }
        if (URI_SCHEME_REDIS_SECURE.equals(uri.getScheme())) {
            builder.withSsl(true);
        }
        return builder;
    }

    private static RedisURI.Builder configureSentinel(URI uri) {
        checkArgument(isNotEmpty(uri.getFragment()), "URI Fragment must contain the sentinelMasterId");
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

    public SocketAddress getResolvedAddress() {
        if (resolvedAddress == null) {
            resolveAddress();
        }
        return resolvedAddress;
    }

    private void resolveAddress() {
        if (getSocket() != null) {
            resolvedAddress = EpollProvider.newSocketAddress(getSocket());
        } else {
            resolvedAddress = new InetSocketAddress(host, port);
        }
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
            sb.append(", sentinelMasterId=").append(sentinelMasterId);
        }
        sb.append(']');
        return sb.toString();
    }

    public static class Builder {

        private final RedisURI redisURI = new RedisURI();

        public static Builder socket(String socket) {
            checkNotNull(socket, "Socket must not be null");
            Builder builder = new Builder();
            builder.redisURI.setSocket(socket);
            return builder;
        }

        public static Builder redis(String host) {
            return redis(host, DEFAULT_REDIS_PORT);
        }

        public static Builder redis(String host, int port) {
            checkNotNull(host, "Host must not be null");
            Builder builder = new Builder();
            builder.redisURI.setHost(host);
            builder.redisURI.setPort(port);
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
            builder.redisURI.setSentinelMasterId(masterId);
            builder.redisURI.sentinels.add(new RedisURI(host, port, 1, TimeUnit.SECONDS));
            return builder;
        }

        public Builder withSentinel(String host) {
            return withSentinel(host, DEFAULT_SENTINEL_PORT);
        }

        public Builder withSentinel(String host, int port) {
            checkState(redisURI.host == null, "Cannot use with Redis mode.");
            checkNotNull(host, "Host must not be null");
            redisURI.sentinels.add(new RedisURI(host, port, 1, TimeUnit.SECONDS));
            return this;
        }

        public Builder withPort(int port) {
            checkState(redisURI.host != null, "Host is null. Cannot use in Sentinel mode.");
            redisURI.setPort(port);
            return this;
        }

        public Builder withSsl(boolean ssl) {
            checkState(redisURI.host != null, "Host is null. Cannot use in Sentinel mode.");
            redisURI.setSsl(ssl);
            return this;
        }

        public Builder withStartTls(boolean startTls) {
            checkState(redisURI.host != null, "Host is null. Cannot use in Sentinel mode.");
            redisURI.setStartTls(startTls);
            return this;
        }

        public Builder withVerifyPeer(boolean verifyPeer) {
            checkState(redisURI.host != null, "Host is null. Cannot use in Sentinel mode.");
            redisURI.setVerifyPeer(verifyPeer);
            return this;
        }

        public Builder withDatabase(int database) {
            redisURI.setDatabase(database);
            return this;
        }

        public Builder withPassword(String password) {
            checkNotNull(password, "Password must not be null");
            redisURI.setPassword(password);
            return this;
        }

        public Builder withTimeout(long timeout, TimeUnit unit) {
            checkNotNull(unit, "TimeUnit must not be null");
            checkArgument(timeout >= 0, "Timeout must be greater or equal 0");
            redisURI.setTimeout(timeout);
            redisURI.setUnit(unit);
            return this;
        }

        public RedisURI build() {
            return redisURI;
        }
    }
}
