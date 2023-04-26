package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.PlatformSocketImpl;
import sun.net.ResourceManager;
import sun.net.ext.ExtendedSocketOptions;
import sun.net.util.IPAddressUtil;
import sun.net.util.SocketExceptions;

abstract class AbstractPlainSocketImpl extends SocketImpl implements PlatformSocketImpl {

    int timeout;

    private int trafficClass;

    private boolean shut_rd = false;

    private boolean shut_wr = false;

    private SocketInputStream socketInputStream = null;

    private SocketOutputStream socketOutputStream = null;

    protected int fdUseCount = 0;

    protected final Object fdLock = new Object();

    protected boolean closePending = false;

    private volatile boolean connectionReset;

    boolean isBound;

    volatile boolean isConnected;

    protected boolean stream;

    final boolean isServer;

    static {
        jdk.internal.loader.BootLoader.loadLibrary("net");
    }

    private static volatile boolean checkedReusePort;

    private static volatile boolean isReusePortAvailable;

    static boolean isReusePortAvailable() {
        if (!checkedReusePort) {
            isReusePortAvailable = isReusePortAvailable0();
            checkedReusePort = true;
        }
        return isReusePortAvailable;
    }

    AbstractPlainSocketImpl(boolean isServer) {
        this.isServer = isServer;
    }

    protected synchronized void create(boolean stream) throws IOException {
        this.stream = stream;
        if (!stream) {
            ResourceManager.beforeUdpCreate();
            fd = new FileDescriptor();
            try {
                socketCreate(false);
                SocketCleanable.register(fd);
            } catch (IOException ioe) {
                ResourceManager.afterUdpClose();
                fd = null;
                throw ioe;
            }
        } else {
            fd = new FileDescriptor();
            socketCreate(true);
            SocketCleanable.register(fd);
        }
    }

    protected void connect(String host, int port) throws UnknownHostException, IOException {
        boolean connected = false;
        try {
            InetAddress address = InetAddress.getByName(host);
            this.address = address;
            this.port = port;
            if (address.isLinkLocalAddress()) {
                address = IPAddressUtil.toScopedAddress(address);
            }
            connectToAddress(address, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                }
            }
            isConnected = connected;
        }
    }

    protected void connect(InetAddress address, int port) throws IOException {
        this.address = address;
        this.port = port;
        if (address.isLinkLocalAddress()) {
            address = IPAddressUtil.toScopedAddress(address);
        }
        try {
            connectToAddress(address, port, timeout);
            isConnected = true;
            return;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    protected void connect(SocketAddress address, int timeout) throws IOException {
        boolean connected = false;
        try {
            if (address == null || !(address instanceof InetSocketAddress))
                throw new IllegalArgumentException("unsupported address type");
            InetSocketAddress addr = (InetSocketAddress) address;
            if (addr.isUnresolved())
                throw new UnknownHostException(addr.getHostName());
            InetAddress ia = addr.getAddress();
            this.address = ia;
            this.port = addr.getPort();
            if (ia.isLinkLocalAddress()) {
                ia = IPAddressUtil.toScopedAddress(ia);
            }
            connectToAddress(ia, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                }
            }
            isConnected = connected;
        }
    }

    private void connectToAddress(InetAddress address, int port, int timeout) throws IOException {
        if (address.isAnyLocalAddress()) {
            doConnect(InetAddress.getLocalHost(), port, timeout);
        } else {
            doConnect(address, port, timeout);
        }
    }

    public void setOption(int opt, Object val) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        boolean on = true;
        switch(opt) {
            case SO_LINGER:
                if (val == null || (!(val instanceof Integer) && !(val instanceof Boolean)))
                    throw new SocketException("Bad parameter for option");
                if (val instanceof Boolean) {
                    on = false;
                }
                break;
            case SO_TIMEOUT:
                if (val == null || (!(val instanceof Integer)))
                    throw new SocketException("Bad parameter for SO_TIMEOUT");
                int tmp = ((Integer) val).intValue();
                if (tmp < 0)
                    throw new IllegalArgumentException("timeout < 0");
                timeout = tmp;
                break;
            case IP_TOS:
                if (val == null || !(val instanceof Integer)) {
                    throw new SocketException("bad argument for IP_TOS");
                }
                trafficClass = ((Integer) val).intValue();
                break;
            case SO_BINDADDR:
                throw new SocketException("Cannot re-bind socket");
            case TCP_NODELAY:
                if (val == null || !(val instanceof Boolean))
                    throw new SocketException("bad parameter for TCP_NODELAY");
                on = ((Boolean) val).booleanValue();
                break;
            case SO_SNDBUF:
            case SO_RCVBUF:
                if (val == null || !(val instanceof Integer) || !(((Integer) val).intValue() > 0)) {
                    throw new SocketException("bad parameter for SO_SNDBUF " + "or SO_RCVBUF");
                }
                break;
            case SO_KEEPALIVE:
                if (val == null || !(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_KEEPALIVE");
                on = ((Boolean) val).booleanValue();
                break;
            case SO_OOBINLINE:
                if (val == null || !(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_OOBINLINE");
                on = ((Boolean) val).booleanValue();
                break;
            case SO_REUSEADDR:
                if (val == null || !(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_REUSEADDR");
                on = ((Boolean) val).booleanValue();
                break;
            case SO_REUSEPORT:
                if (val == null || !(val instanceof Boolean))
                    throw new SocketException("bad parameter for SO_REUSEPORT");
                if (!supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT))
                    throw new UnsupportedOperationException("unsupported option");
                on = ((Boolean) val).booleanValue();
                break;
            default:
                throw new SocketException("unrecognized TCP option: " + opt);
        }
        socketSetOption(opt, on, val);
    }

    public Object getOption(int opt) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        if (opt == SO_TIMEOUT) {
            return timeout;
        }
        int ret = 0;
        switch(opt) {
            case TCP_NODELAY:
                ret = socketGetOption(opt, null);
                return Boolean.valueOf(ret != -1);
            case SO_OOBINLINE:
                ret = socketGetOption(opt, null);
                return Boolean.valueOf(ret != -1);
            case SO_LINGER:
                ret = socketGetOption(opt, null);
                return (ret == -1) ? Boolean.FALSE : (Object) (ret);
            case SO_REUSEADDR:
                ret = socketGetOption(opt, null);
                return Boolean.valueOf(ret != -1);
            case SO_BINDADDR:
                InetAddressContainer in = new InetAddressContainer();
                ret = socketGetOption(opt, in);
                return in.addr;
            case SO_SNDBUF:
            case SO_RCVBUF:
                ret = socketGetOption(opt, null);
                return ret;
            case IP_TOS:
                try {
                    ret = socketGetOption(opt, null);
                    if (ret == -1) {
                        return trafficClass;
                    } else {
                        return ret;
                    }
                } catch (SocketException se) {
                    return trafficClass;
                }
            case SO_KEEPALIVE:
                ret = socketGetOption(opt, null);
                return Boolean.valueOf(ret != -1);
            case SO_REUSEPORT:
                if (!supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT)) {
                    throw new UnsupportedOperationException("unsupported option");
                }
                ret = socketGetOption(opt, null);
                return Boolean.valueOf(ret != -1);
            default:
                return null;
        }
    }

    static final ExtendedSocketOptions extendedOptions = ExtendedSocketOptions.getInstance();

    private static final Set<SocketOption<?>> clientSocketOptions = clientSocketOptions();

    private static final Set<SocketOption<?>> serverSocketOptions = serverSocketOptions();

    private static Set<SocketOption<?>> clientSocketOptions() {
        HashSet<SocketOption<?>> options = new HashSet<>();
        options.add(StandardSocketOptions.SO_KEEPALIVE);
        options.add(StandardSocketOptions.SO_SNDBUF);
        options.add(StandardSocketOptions.SO_RCVBUF);
        options.add(StandardSocketOptions.SO_REUSEADDR);
        options.add(StandardSocketOptions.SO_LINGER);
        options.add(StandardSocketOptions.IP_TOS);
        options.add(StandardSocketOptions.TCP_NODELAY);
        if (isReusePortAvailable())
            options.add(StandardSocketOptions.SO_REUSEPORT);
        options.addAll(ExtendedSocketOptions.clientSocketOptions());
        return Collections.unmodifiableSet(options);
    }

    private static Set<SocketOption<?>> serverSocketOptions() {
        HashSet<SocketOption<?>> options = new HashSet<>();
        options.add(StandardSocketOptions.SO_RCVBUF);
        options.add(StandardSocketOptions.SO_REUSEADDR);
        options.add(StandardSocketOptions.IP_TOS);
        if (isReusePortAvailable())
            options.add(StandardSocketOptions.SO_REUSEPORT);
        options.addAll(ExtendedSocketOptions.serverSocketOptions());
        return Collections.unmodifiableSet(options);
    }

    @Override
    protected Set<SocketOption<?>> supportedOptions() {
        if (isServer)
            return serverSocketOptions;
        else
            return clientSocketOptions;
    }

    @Override
    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        if (!name.type().isInstance(value))
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        if (isClosedOrPending())
            throw new SocketException("Socket closed");
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            setOption(SocketOptions.SO_KEEPALIVE, value);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            if (((Integer) value).intValue() < 0)
                throw new IllegalArgumentException("Invalid send buffer size:" + value);
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            if (((Integer) value).intValue() < 0)
                throw new IllegalArgumentException("Invalid recv buffer size:" + value);
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(SocketOptions.SO_REUSEADDR, value);
        } else if (name == StandardSocketOptions.SO_REUSEPORT) {
            setOption(SocketOptions.SO_REUSEPORT, value);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            if (((Integer) value).intValue() < 0)
                setOption(SocketOptions.SO_LINGER, false);
            else
                setOption(SocketOptions.SO_LINGER, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            int i = ((Integer) value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid IP_TOS value: " + value);
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            setOption(SocketOptions.TCP_NODELAY, value);
        } else if (extendedOptions.isOptionSupported(name)) {
            extendedOptions.setOption(fd, name, value);
        } else {
            throw new AssertionError("unknown option: " + name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        if (isClosedOrPending())
            throw new SocketException("Socket closed");
        if (name == StandardSocketOptions.SO_KEEPALIVE) {
            return (T) getOption(SocketOptions.SO_KEEPALIVE);
        } else if (name == StandardSocketOptions.SO_SNDBUF) {
            return (T) getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T) getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T) getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_REUSEPORT) {
            return (T) getOption(SocketOptions.SO_REUSEPORT);
        } else if (name == StandardSocketOptions.SO_LINGER) {
            Object value = getOption(SocketOptions.SO_LINGER);
            if (value instanceof Boolean) {
                assert ((Boolean) value).booleanValue() == false;
                value = -1;
            }
            return (T) value;
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T) getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.TCP_NODELAY) {
            return (T) getOption(SocketOptions.TCP_NODELAY);
        } else if (extendedOptions.isOptionSupported(name)) {
            return (T) extendedOptions.getOption(fd, name);
        } else {
            throw new AssertionError("unknown option: " + name);
        }
    }

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        synchronized (fdLock) {
            if (!closePending && !isBound) {
                NetHooks.beforeTcpConnect(fd, address, port);
            }
        }
        try {
            acquireFD();
            try {
                socketConnect(address, port, timeout);
                synchronized (fdLock) {
                    if (closePending) {
                        throw new SocketException("Socket closed");
                    }
                }
            } finally {
                releaseFD();
            }
        } catch (IOException e) {
            close();
            throw SocketExceptions.of(e, new InetSocketAddress(address, port));
        }
    }

    protected synchronized void bind(InetAddress address, int lport) throws IOException {
        synchronized (fdLock) {
            if (!closePending && !isBound) {
                NetHooks.beforeTcpBind(fd, address, lport);
            }
        }
        if (address.isLinkLocalAddress()) {
            address = IPAddressUtil.toScopedAddress(address);
        }
        socketBind(address, lport);
        isBound = true;
    }

    protected synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

    protected void accept(SocketImpl si) throws IOException {
        si.fd = new FileDescriptor();
        acquireFD();
        try {
            socketAccept(si);
        } finally {
            releaseFD();
        }
        SocketCleanable.register(si.fd);
    }

    protected synchronized InputStream getInputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_rd)
                throw new IOException("Socket input is shutdown");
            if (socketInputStream == null) {
                PrivilegedExceptionAction<SocketInputStream> pa = () -> new SocketInputStream(this);
                try {
                    socketInputStream = AccessController.doPrivileged(pa);
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getCause();
                }
            }
        }
        return socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        socketInputStream = in;
    }

    protected synchronized OutputStream getOutputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_wr)
                throw new IOException("Socket output is shutdown");
            if (socketOutputStream == null) {
                PrivilegedExceptionAction<SocketOutputStream> pa = () -> new SocketOutputStream(this);
                try {
                    socketOutputStream = AccessController.doPrivileged(pa);
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getCause();
                }
            }
        }
        return socketOutputStream;
    }

    void setFileDescriptor(FileDescriptor fd) {
        this.fd = fd;
    }

    void setAddress(InetAddress address) {
        this.address = address;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setLocalPort(int localport) {
        this.localport = localport;
    }

    protected synchronized int available() throws IOException {
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        }
        if (isConnectionReset() || shut_rd) {
            return 0;
        }
        int n = 0;
        try {
            n = socketAvailable();
        } catch (ConnectionResetException exc1) {
            setConnectionReset();
        }
        return n;
    }

    protected void close() throws IOException {
        synchronized (fdLock) {
            if (fd != null) {
                if (!stream) {
                    ResourceManager.afterUdpClose();
                }
                if (fdUseCount == 0) {
                    if (closePending) {
                        return;
                    }
                    closePending = true;
                    try {
                        socketPreClose();
                    } finally {
                        socketClose();
                    }
                    fd = null;
                    return;
                } else {
                    if (!closePending) {
                        closePending = true;
                        fdUseCount--;
                        socketPreClose();
                    }
                }
            }
        }
    }

    void reset() {
        throw new InternalError("should not get here");
    }

    protected void shutdownInput() throws IOException {
        if (fd != null) {
            socketShutdown(SHUT_RD);
            if (socketInputStream != null) {
                socketInputStream.setEOF(true);
            }
            shut_rd = true;
        }
    }

    protected void shutdownOutput() throws IOException {
        if (fd != null) {
            socketShutdown(SHUT_WR);
            shut_wr = true;
        }
    }

    protected boolean supportsUrgentData() {
        return true;
    }

    protected void sendUrgentData(int data) throws IOException {
        if (fd == null) {
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData(data);
    }

    FileDescriptor acquireFD() {
        synchronized (fdLock) {
            fdUseCount++;
            return fd;
        }
    }

    void releaseFD() {
        synchronized (fdLock) {
            fdUseCount--;
            if (fdUseCount == -1) {
                if (fd != null) {
                    try {
                        socketClose();
                    } catch (IOException e) {
                    } finally {
                        fd = null;
                    }
                }
            }
        }
    }

    boolean isConnectionReset() {
        return connectionReset;
    }

    void setConnectionReset() {
        connectionReset = true;
    }

    public boolean isClosedOrPending() {
        synchronized (fdLock) {
            if (closePending || (fd == null)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public int getTimeout() {
        return timeout;
    }

    private void socketPreClose() throws IOException {
        socketClose0(true);
    }

    protected void socketClose() throws IOException {
        SocketCleanable.unregister(fd);
        socketClose0(false);
    }

    abstract void socketCreate(boolean stream) throws IOException;

    abstract void socketConnect(InetAddress address, int port, int timeout) throws IOException;

    abstract void socketBind(InetAddress address, int port) throws IOException;

    abstract void socketListen(int count) throws IOException;

    abstract void socketAccept(SocketImpl s) throws IOException;

    abstract int socketAvailable() throws IOException;

    abstract void socketClose0(boolean useDeferredClose) throws IOException;

    abstract void socketShutdown(int howto) throws IOException;

    abstract void socketSetOption(int cmd, boolean on, Object value) throws SocketException;

    abstract int socketGetOption(int opt, Object iaContainerObj) throws SocketException;

    abstract void socketSendUrgentData(int data) throws IOException;

    public static final int SHUT_RD = 0;

    public static final int SHUT_WR = 1;

    private static native boolean isReusePortAvailable0();
}
