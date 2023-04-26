package com.lambdaworks.redis;

import static com.google.common.base.Preconditions.*;
import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.pubsub.PubSubCommandHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public abstract class AbstractRedisClient {

    protected static final InternalLogger logger = InternalLoggerFactory.getInstance(RedisClient.class);

    private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 4));
        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.eventLoopThreads: {}", DEFAULT_EVENT_LOOP_THREADS);
        }
    }

    @Deprecated
    protected EventLoopGroup eventLoopGroup;

    protected final Map<Class<? extends EventLoopGroup>, EventLoopGroup> eventLoopGroups;

    protected final HashedWheelTimer timer;

    protected final ChannelGroup channels;

    protected long timeout = 60;

    protected TimeUnit unit;

    protected ConnectionEvents connectionEvents = new ConnectionEvents();

    protected Set<Closeable> closeableResources = Sets.newConcurrentHashSet();

    protected ClientOptions clientOptions = new ClientOptions.Builder().build();

    protected AbstractRedisClient() {
        timer = new HashedWheelTimer();
        eventLoopGroups = new ConcurrentHashMap<Class<? extends EventLoopGroup>, EventLoopGroup>();
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        timer.start();
        unit = TimeUnit.SECONDS;
    }

    public void setDefaultTimeout(long timeout, TimeUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    @SuppressWarnings("unchecked")
    protected <K, V, T extends RedisAsyncConnectionImpl<K, V>> T connectAsyncImpl(final CommandHandler<K, V> handler, final T connection, final Supplier<SocketAddress> socketAddressSupplier) {
        ConnectionBuilder connectionBuilder = ConnectionBuilder.connectionBuilder();
        connectionBuilder.clientOptions(clientOptions);
        connectionBuilder(handler, connection, socketAddressSupplier, connectionBuilder, null);
        channelType(connectionBuilder, null);
        return (T) initializeChannel(connectionBuilder);
    }

    protected void connectionBuilder(CommandHandler<?, ?> handler, RedisChannelHandler<?, ?> connection, Supplier<SocketAddress> socketAddressSupplier, ConnectionBuilder connectionBuilder, RedisURI redisURI) {
        Bootstrap redisBootstrap = new Bootstrap();
        if (redisURI == null) {
            redisBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) unit.toMillis(timeout));
            connectionBuilder.timeout(timeout, unit);
        } else {
            redisBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) redisURI.getUnit().toMillis(redisURI.getTimeout()));
            connectionBuilder.timeout(redisURI.getTimeout(), redisURI.getUnit());
        }
        connectionBuilder.bootstrap(redisBootstrap);
        connectionBuilder.channelGroup(channels).connectionEvents(connectionEvents).timer(timer);
        connectionBuilder.commandHandler(handler).socketAddressSupplier(socketAddressSupplier).connection(connection);
    }

    protected void channelType(ConnectionBuilder connectionBuilder, ConnectionPoint connectionPoint) {
        connectionBuilder.bootstrap().group(getEventLoopGroup(connectionPoint));
        if (connectionPoint != null && connectionPoint.getSocket() != null) {
            checkForEpollLibrary();
            connectionBuilder.bootstrap().channel(EpollProvider.epollDomainSocketChannelClass);
        } else {
            connectionBuilder.bootstrap().channel(NioSocketChannel.class);
        }
    }

    private synchronized EventLoopGroup getEventLoopGroup(ConnectionPoint connectionPoint) {
        if ((connectionPoint == null || connectionPoint.getSocket() == null) && !eventLoopGroups.containsKey(NioEventLoopGroup.class)) {
            if (eventLoopGroup == null) {
                eventLoopGroup = new NioEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS);
            }
            eventLoopGroups.put(NioEventLoopGroup.class, eventLoopGroup);
        }
        if (connectionPoint != null && connectionPoint.getSocket() != null) {
            checkForEpollLibrary();
            if (!eventLoopGroups.containsKey(EpollProvider.epollEventLoopGroupClass)) {
                EventLoopGroup epl = EpollProvider.newEventLoopGroup(DEFAULT_EVENT_LOOP_THREADS);
                eventLoopGroups.put(EpollProvider.epollEventLoopGroupClass, epl);
            }
        }
        if (connectionPoint == null || connectionPoint.getSocket() == null) {
            return eventLoopGroups.get(NioEventLoopGroup.class);
        }
        if (connectionPoint != null && connectionPoint.getSocket() != null) {
            checkForEpollLibrary();
            return eventLoopGroups.get(EpollProvider.epollEventLoopGroupClass);
        }
        throw new IllegalStateException("This should not have happened in a binary decision. Please file a bug.");
    }

    private void checkForEpollLibrary() {
        EpollProvider.checkForEpollLibrary();
    }

    @SuppressWarnings("unchecked")
    protected <K, V, T extends RedisChannelHandler<K, V>> T initializeChannel(ConnectionBuilder connectionBuilder) {
        RedisChannelHandler<?, ?> connection = connectionBuilder.connection();
        SocketAddress redisAddress = connectionBuilder.socketAddress();
        try {
            logger.debug("Connecting to Redis, address: " + redisAddress);
            Bootstrap redisBootstrap = connectionBuilder.bootstrap();
            RedisChannelInitializer initializer = connectionBuilder.build();
            redisBootstrap.handler(initializer);
            ChannelFuture connectFuture = redisBootstrap.connect(redisAddress);
            connectFuture.await();
            if (!connectFuture.isSuccess()) {
                if (connectFuture.cause() instanceof Exception) {
                    throw (Exception) connectFuture.cause();
                }
                connectFuture.get();
            }
            try {
                initializer.channelInitialized().get(connectionBuilder.getTimeout(), connectionBuilder.getTimeUnit());
            } catch (TimeoutException e) {
                throw new RedisConnectionException("Could not initialize channel within " + connectionBuilder.getTimeout() + " " + connectionBuilder.getTimeUnit());
            }
            connection.registerCloseables(closeableResources, connection, connectionBuilder.commandHandler());
            return (T) connection;
        } catch (RedisException e) {
            connection.close();
            throw e;
        } catch (Exception e) {
            connection.close();
            throw new RedisConnectionException("Unable to connect to " + redisAddress, e);
        }
    }

    public void shutdown() {
        shutdown(2, 15, TimeUnit.SECONDS);
    }

    public void shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {
        while (!closeableResources.isEmpty()) {
            Closeable closeableResource = closeableResources.iterator().next();
            try {
                closeableResource.close();
            } catch (Exception e) {
                logger.debug("Exception on Close: " + e.getMessage(), e);
            }
            closeableResources.remove(closeableResource);
        }
        for (Channel c : channels) {
            ChannelPipeline pipeline = c.pipeline();
            CommandHandler<?, ?> commandHandler = pipeline.get(CommandHandler.class);
            if (commandHandler != null && !commandHandler.isClosed()) {
                commandHandler.close();
            }
            PubSubCommandHandler<?, ?> psCommandHandler = pipeline.get(PubSubCommandHandler.class);
            if (psCommandHandler != null && !psCommandHandler.isClosed()) {
                psCommandHandler.close();
            }
        }
        List<Future<?>> closeFutures = Lists.newArrayList();
        ChannelGroupFuture closeFuture = channels.close();
        closeFutures.add(closeFuture);
        for (EventLoopGroup eventExecutors : eventLoopGroups.values()) {
            Future<?> groupCloseFuture = eventExecutors.shutdownGracefully(quietPeriod, timeout, timeUnit);
            closeFutures.add(groupCloseFuture);
        }
        for (Future<?> future : closeFutures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RedisException(e);
            }
        }
        timer.stop();
    }

    protected int getResourceCount() {
        return closeableResources.size();
    }

    protected int getChannelCount() {
        return channels.size();
    }

    protected static <K, V> Object syncHandler(RedisChannelHandler<K, V> connection, Class<?>... interfaceClasses) {
        FutureSyncInvocationHandler<K, V> h = new FutureSyncInvocationHandler<K, V>(connection);
        return Proxy.newProxyInstance(AbstractRedisClient.class.getClassLoader(), interfaceClasses, h);
    }

    public void addListener(RedisConnectionStateListener listener) {
        checkArgument(listener != null, "RedisConnectionStateListener must not be null");
        connectionEvents.addListener(listener);
    }

    public void removeListener(RedisConnectionStateListener listener) {
        checkArgument(listener != null, "RedisConnectionStateListener must not be null");
        connectionEvents.removeListener(listener);
    }

    public ClientOptions getOptions() {
        return clientOptions;
    }

    public void setOptions(ClientOptions clientOptions) {
        checkArgument(clientOptions != null, "clientOptions must not be null");
        this.clientOptions = clientOptions;
    }
}
