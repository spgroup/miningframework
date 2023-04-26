package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import sun.net.ResourceManager;
import sun.net.ext.ExtendedSocketOptions;
import sun.net.util.IPAddressUtil;
import sun.security.action.GetPropertyAction;

abstract class AbstractPlainDatagramSocketImpl extends DatagramSocketImpl {

    int timeout = 0;

    boolean connected = false;

    private int trafficClass = 0;

    protected InetAddress connectedAddress = null;

    private int connectedPort = -1;

    private static final String os = GetPropertyAction.privilegedGetProperty("os.name");

    private static final boolean connectDisabled = os.contains("OS X");

    static {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public Void run() {
                System.loadLibrary("net");
                return null;
            }
        });
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

    protected synchronized void create() throws SocketException {
        ResourceManager.beforeUdpCreate();
        fd = new FileDescriptor();
        try {
            datagramSocketCreate();
            SocketCleanable.register(fd);
        } catch (SocketException ioe) {
            ResourceManager.afterUdpClose();
            fd = null;
            throw ioe;
        }
    }

    protected synchronized void bind(int lport, InetAddress laddr) throws SocketException {
        if (laddr.isLinkLocalAddress()) {
            laddr = IPAddressUtil.toScopedAddress(laddr);
        }
        bind0(lport, laddr);
    }

    protected abstract void bind0(int lport, InetAddress laddr) throws SocketException;

    protected void send(DatagramPacket p) throws IOException {
        InetAddress orig = p.getAddress();
        if (orig.isLinkLocalAddress()) {
            InetAddress scoped = IPAddressUtil.toScopedAddress(orig);
            if (orig != scoped) {
                p = new DatagramPacket(p.getData(), p.getOffset(), p.getLength(), scoped, p.getPort());
            }
        }
        send0(p);
    }

    protected abstract void send0(DatagramPacket p) throws IOException;

    protected void connect(InetAddress address, int port) throws SocketException {
        if (address.isLinkLocalAddress()) {
            address = IPAddressUtil.toScopedAddress(address);
        }
        connect0(address, port);
        connectedAddress = address;
        connectedPort = port;
        connected = true;
    }

    protected void disconnect() {
        disconnect0(connectedAddress.holder().getFamily());
        connected = false;
        connectedAddress = null;
        connectedPort = -1;
    }

    protected abstract int peek(InetAddress i) throws IOException;

    protected abstract int peekData(DatagramPacket p) throws IOException;

    protected synchronized void receive(DatagramPacket p) throws IOException {
        receive0(p);
    }

    protected abstract void receive0(DatagramPacket p) throws IOException;

    protected abstract void setTimeToLive(int ttl) throws IOException;

    protected abstract int getTimeToLive() throws IOException;

    @Deprecated
    protected abstract void setTTL(byte ttl) throws IOException;

    @Deprecated
    protected abstract byte getTTL() throws IOException;

    protected void join(InetAddress inetaddr) throws IOException {
        join(inetaddr, null);
    }

    protected void leave(InetAddress inetaddr) throws IOException {
        leave(inetaddr, null);
    }

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        join(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected abstract void join(InetAddress inetaddr, NetworkInterface netIf) throws IOException;

    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        leave(((InetSocketAddress) mcastaddr).getAddress(), netIf);
    }

    protected abstract void leave(InetAddress inetaddr, NetworkInterface netIf) throws IOException;

    protected void close() {
        if (fd != null) {
            SocketCleanable.unregister(fd);
            datagramSocketClose();
            ResourceManager.afterUdpClose();
            fd = null;
        }
    }

    protected boolean isClosed() {
        return (fd == null) ? true : false;
    }

    public void setOption(int optID, Object o) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        switch(optID) {
            case SO_TIMEOUT:
                if (o == null || !(o instanceof Integer)) {
                    throw new SocketException("bad argument for SO_TIMEOUT");
                }
                int tmp = ((Integer) o).intValue();
                if (tmp < 0)
                    throw new IllegalArgumentException("timeout < 0");
                timeout = tmp;
                return;
            case IP_TOS:
                if (o == null || !(o instanceof Integer)) {
                    throw new SocketException("bad argument for IP_TOS");
                }
                trafficClass = ((Integer) o).intValue();
                break;
            case SO_REUSEADDR:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for SO_REUSEADDR");
                }
                break;
            case SO_BROADCAST:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for SO_BROADCAST");
                }
                break;
            case SO_BINDADDR:
                throw new SocketException("Cannot re-bind Socket");
            case SO_RCVBUF:
            case SO_SNDBUF:
                if (o == null || !(o instanceof Integer) || ((Integer) o).intValue() < 0) {
                    throw new SocketException("bad argument for SO_SNDBUF or " + "SO_RCVBUF");
                }
                break;
            case IP_MULTICAST_IF:
                if (o == null || !(o instanceof InetAddress))
                    throw new SocketException("bad argument for IP_MULTICAST_IF");
                break;
            case IP_MULTICAST_IF2:
                if (o == null || !(o instanceof NetworkInterface))
                    throw new SocketException("bad argument for IP_MULTICAST_IF2");
                break;
            case IP_MULTICAST_LOOP:
                if (o == null || !(o instanceof Boolean))
                    throw new SocketException("bad argument for IP_MULTICAST_LOOP");
                break;
            case SO_REUSEPORT:
                if (o == null || !(o instanceof Boolean)) {
                    throw new SocketException("bad argument for SO_REUSEPORT");
                }
                if (!supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT)) {
                    throw new UnsupportedOperationException("unsupported option");
                }
                break;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        socketSetOption(optID, o);
    }

    public Object getOption(int optID) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket Closed");
        }
        Object result;
        switch(optID) {
            case SO_TIMEOUT:
                result = timeout;
                break;
            case IP_TOS:
                result = socketGetOption(optID);
                if (((Integer) result).intValue() == -1) {
                    result = trafficClass;
                }
                break;
            case SO_BINDADDR:
            case IP_MULTICAST_IF:
            case IP_MULTICAST_IF2:
            case SO_RCVBUF:
            case SO_SNDBUF:
            case IP_MULTICAST_LOOP:
            case SO_REUSEADDR:
            case SO_BROADCAST:
                result = socketGetOption(optID);
                break;
            case SO_REUSEPORT:
                if (!supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT)) {
                    throw new UnsupportedOperationException("unsupported option");
                }
                result = socketGetOption(optID);
                break;
            default:
                throw new SocketException("invalid option: " + optID);
        }
        return result;
    }

    static final ExtendedSocketOptions extendedOptions = ExtendedSocketOptions.getInstance();

    private static final Set<SocketOption<?>> datagramSocketOptions = datagramSocketOptions();

    private static final Set<SocketOption<?>> multicastSocketOptions = multicastSocketOptions();

    private static Set<SocketOption<?>> datagramSocketOptions() {
        HashSet<SocketOption<?>> options = new HashSet<>();
        options.add(StandardSocketOptions.SO_SNDBUF);
        options.add(StandardSocketOptions.SO_RCVBUF);
        options.add(StandardSocketOptions.SO_REUSEADDR);
        options.add(StandardSocketOptions.IP_TOS);
        if (isReusePortAvailable())
            options.add(StandardSocketOptions.SO_REUSEPORT);
        options.addAll(ExtendedSocketOptions.datagramSocketOptions());
        return Collections.unmodifiableSet(options);
    }

    private static Set<SocketOption<?>> multicastSocketOptions() {
        HashSet<SocketOption<?>> options = new HashSet<>();
        options.add(StandardSocketOptions.SO_SNDBUF);
        options.add(StandardSocketOptions.SO_RCVBUF);
        options.add(StandardSocketOptions.SO_REUSEADDR);
        options.add(StandardSocketOptions.IP_TOS);
        options.add(StandardSocketOptions.IP_MULTICAST_IF);
        options.add(StandardSocketOptions.IP_MULTICAST_TTL);
        options.add(StandardSocketOptions.IP_MULTICAST_LOOP);
        if (isReusePortAvailable())
            options.add(StandardSocketOptions.SO_REUSEPORT);
        options.addAll(ExtendedSocketOptions.datagramSocketOptions());
        return Collections.unmodifiableSet(options);
    }

    @Override
    protected Set<SocketOption<?>> supportedOptions() {
        if (getDatagramSocket() instanceof MulticastSocket)
            return multicastSocketOptions;
        else
            return datagramSocketOptions;
    }

    @Override
    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        if (!name.type().isInstance(value))
            throw new IllegalArgumentException("Invalid value '" + value + "'");
        if (isClosed())
            throw new SocketException("Socket closed");
        if (name == StandardSocketOptions.SO_SNDBUF) {
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
        } else if (name == StandardSocketOptions.IP_TOS) {
            int i = ((Integer) value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid IP_TOS value: " + value);
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
            setOption(SocketOptions.IP_MULTICAST_IF2, value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
            int i = ((Integer) value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid TTL/hop value: " + value);
            setTimeToLive((Integer) value);
        } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP) {
            setOption(SocketOptions.IP_MULTICAST_LOOP, value);
        } else if (extendedOptions.isOptionSupported(name)) {
            extendedOptions.setOption(fd, name, value);
        } else {
            throw new AssertionError("unknown option :" + name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        if (isClosed())
            throw new SocketException("Socket closed");
        if (name == StandardSocketOptions.SO_SNDBUF) {
            return (T) getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T) getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T) getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_REUSEPORT) {
            return (T) getOption(SocketOptions.SO_REUSEPORT);
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T) getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.IP_MULTICAST_IF) {
            return (T) getOption(SocketOptions.IP_MULTICAST_IF2);
        } else if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
            return (T) ((Integer) getTimeToLive());
        } else if (name == StandardSocketOptions.IP_MULTICAST_LOOP) {
            return (T) getOption(SocketOptions.IP_MULTICAST_LOOP);
        } else if (extendedOptions.isOptionSupported(name)) {
            return (T) extendedOptions.getOption(fd, name);
        } else {
            throw new AssertionError("unknown option: " + name);
        }
    }

    protected abstract void datagramSocketCreate() throws SocketException;

    protected abstract void datagramSocketClose();

    protected abstract void socketSetOption(int opt, Object val) throws SocketException;

    protected abstract Object socketGetOption(int opt) throws SocketException;

    protected abstract void connect0(InetAddress address, int port) throws SocketException;

    protected abstract void disconnect0(int family);

    protected boolean nativeConnectDisabled() {
        return connectDisabled;
    }

    abstract int dataAvailable();

    private static native boolean isReusePortAvailable0();
}
