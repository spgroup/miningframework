package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.MembershipKey;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import jdk.internal.ref.CleanerFactory;
import sun.net.ResourceManager;
import sun.net.ext.ExtendedSocketOptions;
import sun.net.util.IPAddressUtil;

class DatagramChannelImpl extends DatagramChannel implements SelChImpl {

    private static final NativeDispatcher nd = new DatagramDispatcher();

    private final ProtocolFamily family;

    private final FileDescriptor fd;

    private final int fdVal;

    private final NativeSocketAddress socketAddress;

    private final Cleanable cleaner;

    private final ReentrantLock readLock = new ReentrantLock();

    private final ReentrantLock writeLock = new ReentrantLock();

    private final Object stateLock = new Object();

    private static final int ST_UNCONNECTED = 0;

    private static final int ST_CONNECTED = 1;

    private static final int ST_CLOSING = 2;

    private static final int ST_CLOSED = 3;

    private int state;

    private long readerThread;

    private long writerThread;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    private InetSocketAddress initialLocalAddress;

    private static final VarHandle SOCKET;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            SOCKET = l.findVarHandle(DatagramChannelImpl.class, "socket", DatagramSocket.class);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    private volatile DatagramSocket socket;

    private MembershipRegistry registry;

    private boolean reuseAddressEmulated;

    private boolean isReuseAddress;

    public DatagramChannelImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.socketAddress = new NativeSocketAddress();
        ResourceManager.beforeUdpCreate();
        try {
            this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
            this.fd = Net.socket(family, false);
            this.fdVal = IOUtil.fdVal(fd);
        } catch (IOException ioe) {
            ResourceManager.afterUdpClose();
            socketAddress.free();
            throw ioe;
        }
        Runnable releaser = releaserFor(fd, socketAddress);
        this.cleaner = CleanerFactory.cleaner().register(this, releaser);
    }

    public DatagramChannelImpl(SelectorProvider sp, ProtocolFamily family) throws IOException {
        super(sp);
        Objects.requireNonNull(family, "'family' is null");
        if ((family != StandardProtocolFamily.INET) && (family != StandardProtocolFamily.INET6)) {
            throw new UnsupportedOperationException("Protocol family not supported");
        }
        if (family == StandardProtocolFamily.INET6) {
            if (!Net.isIPv6Available()) {
                throw new UnsupportedOperationException("IPv6 not available");
            }
        }
        this.socketAddress = new NativeSocketAddress();
        ResourceManager.beforeUdpCreate();
        try {
            this.family = family;
            this.fd = Net.socket(family, false);
            this.fdVal = IOUtil.fdVal(fd);
        } catch (IOException ioe) {
            ResourceManager.afterUdpClose();
            socketAddress.free();
            throw ioe;
        }
        Runnable releaser = releaserFor(fd, socketAddress);
        this.cleaner = CleanerFactory.cleaner().register(this, releaser);
    }

    public DatagramChannelImpl(SelectorProvider sp, FileDescriptor fd) throws IOException {
        super(sp);
        try {
            this.socketAddress = new NativeSocketAddress();
        } catch (OutOfMemoryError e) {
            nd.close(fd);
            throw e;
        }
        ResourceManager.beforeUdpCreate();
        this.family = Net.isIPv6Available() ? StandardProtocolFamily.INET6 : StandardProtocolFamily.INET;
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        Runnable releaser = releaserFor(fd, socketAddress);
        this.cleaner = CleanerFactory.cleaner().register(this, releaser);
        synchronized (stateLock) {
            this.localAddress = Net.localAddress(fd);
        }
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!isOpen())
            throw new ClosedChannelException();
    }

    @Override
    public DatagramSocket socket() {
        DatagramSocket socket = this.socket;
        if (socket == null) {
            socket = DatagramSocketAdaptor.create(this);
            if (!SOCKET.compareAndSet(this, null, socket)) {
                socket = this.socket;
            }
        }
        return socket;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        synchronized (stateLock) {
            ensureOpen();
            return Net.getRevealedLocalAddress(localAddress);
        }
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        synchronized (stateLock) {
            ensureOpen();
            return remoteAddress;
        }
    }

    private ProtocolFamily familyFor(SocketOption<?> name) {
        assert Thread.holdsLock(stateLock);
        if (SocketOptionRegistry.findOption(name, Net.UNSPEC) != null)
            return Net.UNSPEC;
        if (family == StandardProtocolFamily.INET)
            return StandardProtocolFamily.INET;
        if (localAddress == null)
            return StandardProtocolFamily.INET6;
        InetAddress address = localAddress.getAddress();
        if (address.isAnyLocalAddress() || (address instanceof Inet6Address))
            return StandardProtocolFamily.INET6;
        if (Net.canUseIPv6OptionsWithIPv4LocalAddress()) {
            return StandardProtocolFamily.INET6;
        } else {
            return StandardProtocolFamily.INET;
        }
    }

    @Override
    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        if (!name.type().isInstance(value))
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        synchronized (stateLock) {
            ensureOpen();
            ProtocolFamily family = familyFor(name);
            if (name == StandardSocketOptions.IP_MULTICAST_IF) {
                NetworkInterface interf = (NetworkInterface) value;
                if (family == StandardProtocolFamily.INET6) {
                    int index = interf.getIndex();
                    if (index == -1)
                        throw new IOException("Network interface cannot be identified");
                    Net.setInterface6(fd, index);
                } else {
                    Inet4Address target = Net.anyInet4Address(interf);
                    if (target == null)
                        throw new IOException("Network interface not configured for IPv4");
                    int targetAddress = Net.inet4AsInt(target);
                    Net.setInterface4(fd, targetAddress);
                }
                return this;
            }
            if (name == StandardSocketOptions.SO_REUSEADDR && Net.useExclusiveBind() && localAddress != null) {
                reuseAddressEmulated = true;
                this.isReuseAddress = (Boolean) value;
            }
            Net.setSocketOption(fd, family, name, value);
            return this;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            ensureOpen();
            ProtocolFamily family = familyFor(name);
            if (name == StandardSocketOptions.IP_MULTICAST_IF) {
                if (family == StandardProtocolFamily.INET) {
                    int address = Net.getInterface4(fd);
                    if (address == 0)
                        return null;
                    InetAddress ia = Net.inet4FromInt(address);
                    NetworkInterface ni = NetworkInterface.getByInetAddress(ia);
                    if (ni == null)
                        throw new IOException("Unable to map address to interface");
                    return (T) ni;
                } else {
                    int index = Net.getInterface6(fd);
                    if (index == 0)
                        return null;
                    NetworkInterface ni = NetworkInterface.getByIndex(index);
                    if (ni == null)
                        throw new IOException("Unable to map index to interface");
                    return (T) ni;
                }
            }
            if (name == StandardSocketOptions.SO_REUSEADDR && reuseAddressEmulated) {
                return (T) Boolean.valueOf(isReuseAddress);
            }
            return (T) Net.getSocketOption(fd, family, name);
        }
    }

    private static class DefaultOptionsHolder {

        static final Set<SocketOption<?>> defaultOptions = defaultOptions();

        private static Set<SocketOption<?>> defaultOptions() {
            HashSet<SocketOption<?>> set = new HashSet<>();
            set.add(StandardSocketOptions.SO_SNDBUF);
            set.add(StandardSocketOptions.SO_RCVBUF);
            set.add(StandardSocketOptions.SO_REUSEADDR);
            if (Net.isReusePortAvailable()) {
                set.add(StandardSocketOptions.SO_REUSEPORT);
            }
            set.add(StandardSocketOptions.SO_BROADCAST);
            set.add(StandardSocketOptions.IP_TOS);
            set.add(StandardSocketOptions.IP_MULTICAST_IF);
            set.add(StandardSocketOptions.IP_MULTICAST_TTL);
            set.add(StandardSocketOptions.IP_MULTICAST_LOOP);
            set.addAll(ExtendedSocketOptions.datagramSocketOptions());
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    private SocketAddress beginRead(boolean blocking, boolean mustBeConnected) throws IOException {
        if (blocking) {
            begin();
        }
        SocketAddress remote;
        synchronized (stateLock) {
            ensureOpen();
            remote = remoteAddress;
            if ((remote == null) && mustBeConnected)
                throw new NotYetConnectedException();
            if (localAddress == null)
                bindInternal(null);
            if (blocking)
                readerThread = NativeThread.current();
        }
        return remote;
    }

    private void endRead(boolean blocking, boolean completed) throws AsynchronousCloseException {
        if (blocking) {
            synchronized (stateLock) {
                readerThread = 0;
                if (state == ST_CLOSING) {
                    tryFinishClose();
                }
            }
            end(completed);
        }
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        if (dst.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        readLock.lock();
        try {
            boolean blocking = isBlocking();
            SocketAddress sender = null;
            try {
                SocketAddress remote = beginRead(blocking, false);
                boolean connected = (remote != null);
                SecurityManager sm = System.getSecurityManager();
                if (connected || (sm == null)) {
                    int n = receive(dst, connected);
                    if (blocking) {
                        while (IOStatus.okayToRetry(n) && isOpen()) {
                            park(Net.POLLIN);
                            n = receive(dst, connected);
                        }
                    }
                    if (n >= 0) {
                        sender = socketAddress.toInetSocketAddress();
                    }
                } else {
                    sender = untrustedReceive(dst);
                }
                return sender;
            } finally {
                endRead(blocking, (sender != null));
            }
        } finally {
            readLock.unlock();
        }
    }

    private SocketAddress untrustedReceive(ByteBuffer dst) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        assert readLock.isHeldByCurrentThread() && sm != null && remoteAddress == null;
        ByteBuffer bb = Util.getTemporaryDirectBuffer(dst.remaining());
        try {
            boolean blocking = isBlocking();
            for (; ; ) {
                int n = receive(bb, false);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && isOpen()) {
                        park(Net.POLLIN);
                        n = receive(bb, false);
                    }
                }
                if (n >= 0) {
                    InetSocketAddress isa = socketAddress.toInetSocketAddress();
                    try {
                        sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                        bb.flip();
                        dst.put(bb);
                        return isa;
                    } catch (SecurityException se) {
                        bb.clear();
                    }
                } else {
                    return null;
                }
            }
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    SocketAddress blockingReceive(ByteBuffer dst, long nanos) throws IOException {
        readLock.lock();
        try {
            ensureOpen();
            if (!isBlocking())
                throw new IllegalBlockingModeException();
            SecurityManager sm = System.getSecurityManager();
            boolean connected = isConnected();
            SocketAddress sender;
            do {
                if (nanos > 0) {
                    sender = trustedBlockingReceive(dst, nanos);
                } else {
                    sender = trustedBlockingReceive(dst);
                }
                if (sm != null && !connected) {
                    InetSocketAddress isa = (InetSocketAddress) sender;
                    try {
                        sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                    } catch (SecurityException e) {
                        sender = null;
                    }
                }
            } while (sender == null);
            return sender;
        } finally {
            readLock.unlock();
        }
    }

    private SocketAddress trustedBlockingReceive(ByteBuffer dst) throws IOException {
        assert readLock.isHeldByCurrentThread() && isBlocking();
        SocketAddress sender = null;
        try {
            SocketAddress remote = beginRead(true, false);
            boolean connected = (remote != null);
            int n = receive(dst, connected);
            while (IOStatus.okayToRetry(n) && isOpen()) {
                park(Net.POLLIN);
                n = receive(dst, connected);
            }
            if (n >= 0) {
                sender = socketAddress.toInetSocketAddress();
            }
            return sender;
        } finally {
            endRead(true, (sender != null));
        }
    }

    private SocketAddress trustedBlockingReceive(ByteBuffer dst, long nanos) throws IOException {
        assert readLock.isHeldByCurrentThread() && isBlocking();
        SocketAddress sender = null;
        try {
            SocketAddress remote = beginRead(true, false);
            boolean connected = (remote != null);
            lockedConfigureBlocking(false);
            try {
                long startNanos = System.nanoTime();
                int n = receive(dst, connected);
                while (n == IOStatus.UNAVAILABLE && isOpen()) {
                    long remainingNanos = nanos - (System.nanoTime() - startNanos);
                    if (remainingNanos <= 0) {
                        throw new SocketTimeoutException("Receive timed out");
                    }
                    park(Net.POLLIN, remainingNanos);
                    n = receive(dst, connected);
                }
                if (n >= 0) {
                    sender = socketAddress.toInetSocketAddress();
                }
                return sender;
            } finally {
                tryLockedConfigureBlocking(true);
            }
        } finally {
            endRead(true, (sender != null));
        }
    }

    private int receive(ByteBuffer dst, boolean connected) throws IOException {
        int pos = dst.position();
        int lim = dst.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (dst instanceof DirectBuffer && rem > 0)
            return receiveIntoNativeBuffer(dst, rem, pos, connected);
        int newSize = Math.max(rem, 1);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            int n = receiveIntoNativeBuffer(bb, newSize, 0, connected);
            bb.flip();
            if (n > 0 && rem > 0)
                dst.put(bb);
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int receiveIntoNativeBuffer(ByteBuffer bb, int rem, int pos, boolean connected) throws IOException {
        int n = receive0(fd, ((DirectBuffer) bb).address() + pos, rem, socketAddress.address(), connected);
        if (n > 0)
            bb.position(pos + n);
        return n;
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        Objects.requireNonNull(src);
        InetSocketAddress isa = Net.checkAddress(target, family);
        writeLock.lock();
        try {
            boolean blocking = isBlocking();
            int n = 0;
            try {
                SocketAddress remote = beginWrite(blocking, false);
                if (remote != null) {
                    if (!target.equals(remote)) {
                        throw new AlreadyConnectedException();
                    }
                    n = IOUtil.write(fd, src, -1, nd);
                    if (blocking) {
                        while (IOStatus.okayToRetry(n) && isOpen()) {
                            park(Net.POLLOUT);
                            n = IOUtil.write(fd, src, -1, nd);
                        }
                    }
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    InetAddress ia = isa.getAddress();
                    if (sm != null) {
                        if (ia.isMulticastAddress()) {
                            sm.checkMulticast(ia);
                        } else {
                            sm.checkConnect(ia.getHostAddress(), isa.getPort());
                        }
                    }
                    if (ia.isLinkLocalAddress())
                        isa = IPAddressUtil.toScopedAddress(isa);
                    n = send(fd, src, isa);
                    if (blocking) {
                        while (IOStatus.okayToRetry(n) && isOpen()) {
                            park(Net.POLLOUT);
                            n = send(fd, src, isa);
                        }
                    }
                }
            } finally {
                endWrite(blocking, n > 0);
                assert IOStatus.check(n);
            }
            return IOStatus.normalize(n);
        } finally {
            writeLock.unlock();
        }
    }

    void blockingSend(ByteBuffer src, SocketAddress target) throws IOException {
        writeLock.lock();
        try {
            ensureOpen();
            if (!isBlocking())
                throw new IllegalBlockingModeException();
            send(src, target);
        } finally {
            writeLock.unlock();
        }
    }

    private int send(FileDescriptor fd, ByteBuffer src, InetSocketAddress target) throws IOException {
        if (src instanceof DirectBuffer)
            return sendFromNativeBuffer(fd, src, target);
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd, bb, target);
            if (n > 0) {
                src.position(pos + n);
            }
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int sendFromNativeBuffer(FileDescriptor fd, ByteBuffer bb, InetSocketAddress target) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        boolean preferIPv6 = (family != StandardProtocolFamily.INET);
        int written;
        try {
            written = send0(preferIPv6, fd, ((DirectBuffer) bb).address() + pos, rem, target.getAddress(), target.getPort());
        } catch (PortUnreachableException pue) {
            if (isConnected())
                throw pue;
            written = rem;
        }
        if (written > 0)
            bb.position(pos + written);
        return written;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        Objects.requireNonNull(buf);
        readLock.lock();
        try {
            boolean blocking = isBlocking();
            int n = 0;
            try {
                beginRead(blocking, true);
                n = IOUtil.read(fd, buf, -1, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && isOpen()) {
                        park(Net.POLLIN);
                        n = IOUtil.read(fd, buf, -1, nd);
                    }
                }
            } finally {
                endRead(blocking, n > 0);
                assert IOStatus.check(n);
            }
            return IOStatus.normalize(n);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, dsts.length);
        readLock.lock();
        try {
            boolean blocking = isBlocking();
            long n = 0;
            try {
                beginRead(blocking, true);
                n = IOUtil.read(fd, dsts, offset, length, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && isOpen()) {
                        park(Net.POLLIN);
                        n = IOUtil.read(fd, dsts, offset, length, nd);
                    }
                }
            } finally {
                endRead(blocking, n > 0);
                assert IOStatus.check(n);
            }
            return IOStatus.normalize(n);
        } finally {
            readLock.unlock();
        }
    }

    private SocketAddress beginWrite(boolean blocking, boolean mustBeConnected) throws IOException {
        if (blocking) {
            begin();
        }
        SocketAddress remote;
        synchronized (stateLock) {
            ensureOpen();
            remote = remoteAddress;
            if ((remote == null) && mustBeConnected)
                throw new NotYetConnectedException();
            if (localAddress == null)
                bindInternal(null);
            if (blocking)
                writerThread = NativeThread.current();
        }
        return remote;
    }

    private void endWrite(boolean blocking, boolean completed) throws AsynchronousCloseException {
        if (blocking) {
            synchronized (stateLock) {
                writerThread = 0;
                if (state == ST_CLOSING) {
                    tryFinishClose();
                }
            }
            end(completed);
        }
    }

    @Override
    public int write(ByteBuffer buf) throws IOException {
        Objects.requireNonNull(buf);
        writeLock.lock();
        try {
            boolean blocking = isBlocking();
            int n = 0;
            try {
                beginWrite(blocking, true);
                n = IOUtil.write(fd, buf, -1, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && isOpen()) {
                        park(Net.POLLOUT);
                        n = IOUtil.write(fd, buf, -1, nd);
                    }
                }
            } finally {
                endWrite(blocking, n > 0);
                assert IOStatus.check(n);
            }
            return IOStatus.normalize(n);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, srcs.length);
        writeLock.lock();
        try {
            boolean blocking = isBlocking();
            long n = 0;
            try {
                beginWrite(blocking, true);
                n = IOUtil.write(fd, srcs, offset, length, nd);
                if (blocking) {
                    while (IOStatus.okayToRetry(n) && isOpen()) {
                        park(Net.POLLOUT);
                        n = IOUtil.write(fd, srcs, offset, length, nd);
                    }
                }
            } finally {
                endWrite(blocking, n > 0);
                assert IOStatus.check(n);
            }
            return IOStatus.normalize(n);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        readLock.lock();
        try {
            writeLock.lock();
            try {
                lockedConfigureBlocking(block);
            } finally {
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
    }

    private void lockedConfigureBlocking(boolean block) throws IOException {
        assert readLock.isHeldByCurrentThread() || writeLock.isHeldByCurrentThread();
        synchronized (stateLock) {
            ensureOpen();
            IOUtil.configureBlocking(fd, block);
        }
    }

    private boolean tryLockedConfigureBlocking(boolean block) throws IOException {
        assert readLock.isHeldByCurrentThread() || writeLock.isHeldByCurrentThread();
        synchronized (stateLock) {
            if (isOpen()) {
                IOUtil.configureBlocking(fd, block);
                return true;
            } else {
                return false;
            }
        }
    }

    InetSocketAddress localAddress() {
        synchronized (stateLock) {
            return localAddress;
        }
    }

    InetSocketAddress remoteAddress() {
        synchronized (stateLock) {
            return remoteAddress;
        }
    }

    @Override
    public DatagramChannel bind(SocketAddress local) throws IOException {
        readLock.lock();
        try {
            writeLock.lock();
            try {
                synchronized (stateLock) {
                    ensureOpen();
                    if (localAddress != null)
                        throw new AlreadyBoundException();
                    bindInternal(local);
                }
            } finally {
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
        return this;
    }

    private void bindInternal(SocketAddress local) throws IOException {
        assert Thread.holdsLock(stateLock) && (localAddress == null);
        InetSocketAddress isa;
        if (local == null) {
            if (family == StandardProtocolFamily.INET) {
                isa = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
            } else {
                isa = new InetSocketAddress(0);
            }
        } else {
            isa = Net.checkAddress(local, family);
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkListen(isa.getPort());
        Net.bind(family, fd, isa.getAddress(), isa.getPort());
        localAddress = Net.localAddress(fd);
    }

    @Override
    public boolean isConnected() {
        synchronized (stateLock) {
            return (state == ST_CONNECTED);
        }
    }

    @Override
    public DatagramChannel connect(SocketAddress sa) throws IOException {
        return connect(sa, true);
    }

    DatagramChannel connect(SocketAddress sa, boolean check) throws IOException {
        InetSocketAddress isa = Net.checkAddress(sa, family);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            InetAddress ia = isa.getAddress();
            if (ia.isMulticastAddress()) {
                sm.checkMulticast(ia);
            } else {
                sm.checkConnect(ia.getHostAddress(), isa.getPort());
                sm.checkAccept(ia.getHostAddress(), isa.getPort());
            }
        }
        readLock.lock();
        try {
            writeLock.lock();
            try {
                synchronized (stateLock) {
                    ensureOpen();
                    if (check && state == ST_CONNECTED)
                        throw new AlreadyConnectedException();
                    if (localAddress == null) {
                        bindInternal(null);
                    }
                    initialLocalAddress = localAddress;
                    int n = Net.connect(family, fd, isa.getAddress(), isa.getPort());
                    if (n <= 0)
                        throw new Error();
                    remoteAddress = isa;
                    state = ST_CONNECTED;
                    localAddress = Net.localAddress(fd);
                    boolean blocking = isBlocking();
                    if (blocking) {
                        IOUtil.configureBlocking(fd, false);
                    }
                    try {
                        ByteBuffer buf = ByteBuffer.allocate(100);
                        while (receive(buf, false) >= 0) {
                            buf.clear();
                        }
                    } finally {
                        if (blocking) {
                            IOUtil.configureBlocking(fd, true);
                        }
                    }
                }
            } finally {
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
        return this;
    }

    @Override
    public DatagramChannel disconnect() throws IOException {
        readLock.lock();
        try {
            writeLock.lock();
            try {
                synchronized (stateLock) {
                    if (!isOpen() || (state != ST_CONNECTED))
                        return this;
                    boolean isIPv6 = (family == StandardProtocolFamily.INET6);
                    disconnect0(fd, isIPv6);
                    remoteAddress = null;
                    state = ST_UNCONNECTED;
                    localAddress = Net.localAddress(fd);
                    try {
                        if (!localAddress.equals(initialLocalAddress)) {
                            repairSocket(initialLocalAddress);
                            assert (localAddress != null) && localAddress.equals(Net.localAddress(fd)) && localAddress.equals(initialLocalAddress);
                        }
                    } finally {
                        initialLocalAddress = null;
                    }
                }
            } finally {
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
        return this;
    }

    private void repairSocket(InetSocketAddress target) throws IOException {
        assert Thread.holdsLock(stateLock);
        if (localAddress.getPort() == 0) {
            assert localAddress.getAddress().equals(target.getAddress());
            Net.bind(family, fd, target.getAddress(), target.getPort());
            localAddress = Net.localAddress(fd);
            return;
        }
        Map<SocketOption<?>, Object> map = new HashMap<>();
        for (SocketOption<?> option : supportedOptions()) {
            Object value = getOption(option);
            if (value != null) {
                map.put(option, value);
            }
        }
        FileDescriptor newfd = Net.socket(family, false);
        try {
            for (Map.Entry<SocketOption<?>, Object> e : map.entrySet()) {
                SocketOption<?> option = e.getKey();
                if (SocketOptionRegistry.findOption(option, Net.UNSPEC) != null) {
                    Object value = e.getValue();
                    try {
                        Net.setSocketOption(newfd, Net.UNSPEC, option, value);
                    } catch (IOException ignore) {
                    }
                }
            }
            if (!isBlocking()) {
                IOUtil.configureBlocking(newfd, false);
            }
            nd.dup(newfd, fd);
        } finally {
            nd.close(newfd);
        }
        try {
            Net.bind(family, fd, target.getAddress(), target.getPort());
        } catch (IOException ioe) {
            localAddress = null;
            throw ioe;
        }
        localAddress = Net.localAddress(fd);
        for (Map.Entry<SocketOption<?>, Object> e : map.entrySet()) {
            @SuppressWarnings("unchecked")
            SocketOption<Object> option = (SocketOption<Object>) e.getKey();
            Object value = e.getValue();
            try {
                setOption(option, value);
            } catch (IOException ignore) {
            }
        }
        MembershipRegistry registry = this.registry;
        if (registry != null) {
            registry.forEach(k -> {
                if (k instanceof MembershipKeyImpl.Type6) {
                    MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) k;
                    Net.join6(fd, key6.groupAddress(), key6.index(), key6.source());
                } else {
                    MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) k;
                    Net.join4(fd, key4.groupAddress(), key4.interfaceAddress(), key4.source());
                }
            });
        }
        AbstractSelectableChannels.forEach(this, SelectionKeyImpl::reset);
    }

    private static class AbstractSelectableChannels {

        private static final Method FOREACH;

        static {
            try {
                PrivilegedExceptionAction<Method> pae = () -> {
                    Method m = AbstractSelectableChannel.class.getDeclaredMethod("forEach", Consumer.class);
                    m.setAccessible(true);
                    return m;
                };
                FOREACH = AccessController.doPrivileged(pae);
            } catch (Exception e) {
                throw new InternalError(e);
            }
        }

        static void forEach(AbstractSelectableChannel ch, Consumer<SelectionKeyImpl> action) {
            try {
                FOREACH.invoke(ch, action);
            } catch (Exception e) {
                throw new InternalError(e);
            }
        }
    }

    private MembershipKey innerJoin(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
        if (!group.isMulticastAddress())
            throw new IllegalArgumentException("Group not a multicast address");
        if (group instanceof Inet4Address) {
            if (family == StandardProtocolFamily.INET6 && !Net.canIPv6SocketJoinIPv4Group())
                throw new IllegalArgumentException("IPv6 socket cannot join IPv4 multicast group");
        } else if (group instanceof Inet6Address) {
            if (family != StandardProtocolFamily.INET6)
                throw new IllegalArgumentException("Only IPv6 sockets can join IPv6 multicast group");
        } else {
            throw new IllegalArgumentException("Address type not supported");
        }
        if (source != null) {
            if (source.isAnyLocalAddress())
                throw new IllegalArgumentException("Source address is a wildcard address");
            if (source.isMulticastAddress())
                throw new IllegalArgumentException("Source address is multicast address");
            if (source.getClass() != group.getClass())
                throw new IllegalArgumentException("Source address is different type to group");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkMulticast(group);
        synchronized (stateLock) {
            ensureOpen();
            if (registry == null) {
                registry = new MembershipRegistry();
            } else {
                MembershipKey key = registry.checkMembership(group, interf, source);
                if (key != null)
                    return key;
            }
            MembershipKeyImpl key;
            if ((family == StandardProtocolFamily.INET6) && ((group instanceof Inet6Address) || Net.canJoin6WithIPv4Group())) {
                int index = interf.getIndex();
                if (index == -1)
                    throw new IOException("Network interface cannot be identified");
                byte[] groupAddress = Net.inet6AsByteArray(group);
                byte[] sourceAddress = (source == null) ? null : Net.inet6AsByteArray(source);
                int n = Net.join6(fd, groupAddress, index, sourceAddress);
                if (n == IOStatus.UNAVAILABLE)
                    throw new UnsupportedOperationException();
                key = new MembershipKeyImpl.Type6(this, group, interf, source, groupAddress, index, sourceAddress);
            } else {
                Inet4Address target = Net.anyInet4Address(interf);
                if (target == null)
                    throw new IOException("Network interface not configured for IPv4");
                int groupAddress = Net.inet4AsInt(group);
                int targetAddress = Net.inet4AsInt(target);
                int sourceAddress = (source == null) ? 0 : Net.inet4AsInt(source);
                int n = Net.join4(fd, groupAddress, targetAddress, sourceAddress);
                if (n == IOStatus.UNAVAILABLE)
                    throw new UnsupportedOperationException();
                key = new MembershipKeyImpl.Type4(this, group, interf, source, groupAddress, targetAddress, sourceAddress);
            }
            registry.add(key);
            return key;
        }
    }

    @Override
    public MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException {
        return innerJoin(group, interf, null);
    }

    @Override
    public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
        Objects.requireNonNull(source);
        return innerJoin(group, interf, source);
    }

    void drop(MembershipKeyImpl key) {
        assert key.channel() == this;
        synchronized (stateLock) {
            if (!key.isValid())
                return;
            try {
                if (key instanceof MembershipKeyImpl.Type6) {
                    MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                    Net.drop6(fd, key6.groupAddress(), key6.index(), key6.source());
                } else {
                    MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                    Net.drop4(fd, key4.groupAddress(), key4.interfaceAddress(), key4.source());
                }
            } catch (IOException ioe) {
                throw new AssertionError(ioe);
            }
            key.invalidate();
            registry.remove(key);
        }
    }

    void block(MembershipKeyImpl key, InetAddress source) throws IOException {
        assert key.channel() == this;
        assert key.sourceAddress() == null;
        synchronized (stateLock) {
            if (!key.isValid())
                throw new IllegalStateException("key is no longer valid");
            if (source.isAnyLocalAddress())
                throw new IllegalArgumentException("Source address is a wildcard address");
            if (source.isMulticastAddress())
                throw new IllegalArgumentException("Source address is multicast address");
            if (source.getClass() != key.group().getClass())
                throw new IllegalArgumentException("Source address is different type to group");
            int n;
            if (key instanceof MembershipKeyImpl.Type6) {
                MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                n = Net.block6(fd, key6.groupAddress(), key6.index(), Net.inet6AsByteArray(source));
            } else {
                MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                n = Net.block4(fd, key4.groupAddress(), key4.interfaceAddress(), Net.inet4AsInt(source));
            }
            if (n == IOStatus.UNAVAILABLE) {
                throw new UnsupportedOperationException();
            }
        }
    }

    void unblock(MembershipKeyImpl key, InetAddress source) {
        assert key.channel() == this;
        assert key.sourceAddress() == null;
        synchronized (stateLock) {
            if (!key.isValid())
                throw new IllegalStateException("key is no longer valid");
            try {
                if (key instanceof MembershipKeyImpl.Type6) {
                    MembershipKeyImpl.Type6 key6 = (MembershipKeyImpl.Type6) key;
                    Net.unblock6(fd, key6.groupAddress(), key6.index(), Net.inet6AsByteArray(source));
                } else {
                    MembershipKeyImpl.Type4 key4 = (MembershipKeyImpl.Type4) key;
                    Net.unblock4(fd, key4.groupAddress(), key4.interfaceAddress(), Net.inet4AsInt(source));
                }
            } catch (IOException ioe) {
                throw new AssertionError(ioe);
            }
        }
    }

    private boolean tryClose() throws IOException {
        assert Thread.holdsLock(stateLock) && state == ST_CLOSING;
        if ((readerThread == 0) && (writerThread == 0) && !isRegistered()) {
            state = ST_CLOSED;
            try {
                cleaner.clean();
            } catch (UncheckedIOException ioe) {
                throw ioe.getCause();
            }
            return true;
        } else {
            return false;
        }
    }

    private void tryFinishClose() {
        try {
            tryClose();
        } catch (IOException ignore) {
        }
    }

    private void implCloseBlockingMode() throws IOException {
        synchronized (stateLock) {
            assert state < ST_CLOSING;
            state = ST_CLOSING;
            if (registry != null)
                registry.invalidateAll();
            if (!tryClose()) {
                long reader = readerThread;
                long writer = writerThread;
                if (reader != 0 || writer != 0) {
                    nd.preClose(fd);
                    if (reader != 0)
                        NativeThread.signal(reader);
                    if (writer != 0)
                        NativeThread.signal(writer);
                }
            }
        }
    }

    private void implCloseNonBlockingMode() throws IOException {
        synchronized (stateLock) {
            assert state < ST_CLOSING;
            state = ST_CLOSING;
            if (registry != null)
                registry.invalidateAll();
        }
        readLock.lock();
        readLock.unlock();
        writeLock.lock();
        writeLock.unlock();
        synchronized (stateLock) {
            if (state == ST_CLOSING) {
                tryClose();
            }
        }
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        assert !isOpen();
        if (isBlocking()) {
            implCloseBlockingMode();
        } else {
            implCloseNonBlockingMode();
        }
    }

    @Override
    public void kill() {
        synchronized (stateLock) {
            if (state == ST_CLOSING) {
                tryFinishClose();
            }
        }
    }

    public boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl ski) {
        int intOps = ski.nioInterestOps();
        int oldOps = ski.nioReadyOps();
        int newOps = initialOps;
        if ((ops & Net.POLLNVAL) != 0) {
            return false;
        }
        if ((ops & (Net.POLLERR | Net.POLLHUP)) != 0) {
            newOps = intOps;
            ski.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }
        if (((ops & Net.POLLIN) != 0) && ((intOps & SelectionKey.OP_READ) != 0))
            newOps |= SelectionKey.OP_READ;
        if (((ops & Net.POLLOUT) != 0) && ((intOps & SelectionKey.OP_WRITE) != 0))
            newOps |= SelectionKey.OP_WRITE;
        ski.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl ski) {
        return translateReadyOps(ops, ski.nioReadyOps(), ski);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl ski) {
        return translateReadyOps(ops, 0, ski);
    }

    public int translateInterestOps(int ops) {
        int newOps = 0;
        if ((ops & SelectionKey.OP_READ) != 0)
            newOps |= Net.POLLIN;
        if ((ops & SelectionKey.OP_WRITE) != 0)
            newOps |= Net.POLLOUT;
        if ((ops & SelectionKey.OP_CONNECT) != 0)
            newOps |= Net.POLLIN;
        return newOps;
    }

    public FileDescriptor getFD() {
        return fd;
    }

    public int getFDVal() {
        return fdVal;
    }

    private static Runnable releaserFor(FileDescriptor fd, NativeSocketAddress sa) {
        return () -> {
            try {
                nd.close(fd);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            } finally {
                ResourceManager.afterUdpClose();
                sa.free();
            }
        };
    }

    private static native void disconnect0(FileDescriptor fd, boolean isIPv6) throws IOException;

    private static native int receive0(FileDescriptor fd, long address, int len, long senderAddress, boolean connected) throws IOException;

    private static native int send0(boolean preferIPv6, FileDescriptor fd, long address, int len, InetAddress addr, int port) throws IOException;

    static {
        IOUtil.load();
    }
}