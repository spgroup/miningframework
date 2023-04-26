package sun.nio.ch;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

public class Net {

    private Net() {
    }

    static final ProtocolFamily UNSPEC = new ProtocolFamily() {

        public String name() {
            return "UNSPEC";
        }
    };

    private static final boolean exclusiveBind;

    static {
        int availLevel = isExclusiveBindAvailable();
        if (availLevel >= 0) {
            String exclBindProp = java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {

                @Override
                public String run() {
                    return System.getProperty("sun.net.useExclusiveBind");
                }
            });
            if (exclBindProp != null) {
                exclusiveBind = exclBindProp.length() == 0 ? true : Boolean.parseBoolean(exclBindProp);
            } else if (availLevel == 1) {
                exclusiveBind = true;
            } else {
                exclusiveBind = false;
            }
        } else {
            exclusiveBind = false;
        }
    }

    private static volatile boolean checkedIPv6 = false;

    private static volatile boolean isIPv6Available;

    static boolean isIPv6Available() {
        if (!checkedIPv6) {
            isIPv6Available = isIPv6Available0();
            checkedIPv6 = true;
        }
        return isIPv6Available;
    }

    static boolean useExclusiveBind() {
        return exclusiveBind;
    }

    static boolean canIPv6SocketJoinIPv4Group() {
        return canIPv6SocketJoinIPv4Group0();
    }

    static boolean canJoin6WithIPv4Group() {
        return canJoin6WithIPv4Group0();
    }

    public static InetSocketAddress checkAddress(SocketAddress sa) {
        if (sa == null)
            throw new NullPointerException();
        if (!(sa instanceof InetSocketAddress))
            throw new UnsupportedAddressTypeException();
        InetSocketAddress isa = (InetSocketAddress) sa;
        if (isa.isUnresolved())
            throw new UnresolvedAddressException();
        InetAddress addr = isa.getAddress();
        if (!(addr instanceof Inet4Address || addr instanceof Inet6Address))
            throw new IllegalArgumentException("Invalid address type");
        return isa;
    }

    static InetSocketAddress asInetSocketAddress(SocketAddress sa) {
        if (!(sa instanceof InetSocketAddress))
            throw new UnsupportedAddressTypeException();
        return (InetSocketAddress) sa;
    }

    static void translateToSocketException(Exception x) throws SocketException {
        if (x instanceof SocketException)
            throw (SocketException) x;
        Exception nx = x;
        if (x instanceof ClosedChannelException)
            nx = new SocketException("Socket is closed");
        else if (x instanceof NotYetConnectedException)
            nx = new SocketException("Socket is not connected");
        else if (x instanceof AlreadyBoundException)
            nx = new SocketException("Already bound");
        else if (x instanceof NotYetBoundException)
            nx = new SocketException("Socket is not bound yet");
        else if (x instanceof UnsupportedAddressTypeException)
            nx = new SocketException("Unsupported address type");
        else if (x instanceof UnresolvedAddressException) {
            nx = new SocketException("Unresolved address");
        }
        if (nx != x)
            nx.initCause(x);
        if (nx instanceof SocketException)
            throw (SocketException) nx;
        else if (nx instanceof RuntimeException)
            throw (RuntimeException) nx;
        else
            throw new Error("Untranslated exception", nx);
    }

    static void translateException(Exception x, boolean unknownHostForUnresolved) throws IOException {
        if (x instanceof IOException)
            throw (IOException) x;
        if (unknownHostForUnresolved && (x instanceof UnresolvedAddressException)) {
            throw new UnknownHostException();
        }
        translateToSocketException(x);
    }

    static void translateException(Exception x) throws IOException {
        translateException(x, false);
    }

    static InetSocketAddress getRevealedLocalAddress(InetSocketAddress addr) {
        SecurityManager sm = System.getSecurityManager();
        if (addr == null || sm == null)
            return addr;
        try {
            sm.checkConnect(addr.getAddress().getHostAddress(), -1);
        } catch (SecurityException e) {
            addr = getLoopbackAddress(addr.getPort());
        }
        return addr;
    }

    static String getRevealedLocalAddressAsString(InetSocketAddress addr) {
        return System.getSecurityManager() == null ? addr.toString() : getLoopbackAddress(addr.getPort()).toString();
    }

    private static InetSocketAddress getLoopbackAddress(int port) {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    static Inet4Address anyInet4Address(final NetworkInterface interf) {
        return AccessController.doPrivileged(new PrivilegedAction<Inet4Address>() {

            public Inet4Address run() {
                Enumeration<InetAddress> addrs = interf.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address) {
                        return (Inet4Address) addr;
                    }
                }
                return null;
            }
        });
    }

    static int inet4AsInt(InetAddress ia) {
        if (ia instanceof Inet4Address) {
            byte[] addr = ia.getAddress();
            int address = addr[3] & 0xFF;
            address |= ((addr[2] << 8) & 0xFF00);
            address |= ((addr[1] << 16) & 0xFF0000);
            address |= ((addr[0] << 24) & 0xFF000000);
            return address;
        }
        throw new AssertionError("Should not reach here");
    }

    static InetAddress inet4FromInt(int address) {
        byte[] addr = new byte[4];
        addr[0] = (byte) ((address >>> 24) & 0xFF);
        addr[1] = (byte) ((address >>> 16) & 0xFF);
        addr[2] = (byte) ((address >>> 8) & 0xFF);
        addr[3] = (byte) (address & 0xFF);
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException uhe) {
            throw new AssertionError("Should not reach here");
        }
    }

    static byte[] inet6AsByteArray(InetAddress ia) {
        if (ia instanceof Inet6Address) {
            return ia.getAddress();
        }
        if (ia instanceof Inet4Address) {
            byte[] ip4address = ia.getAddress();
            byte[] address = new byte[16];
            address[10] = (byte) 0xff;
            address[11] = (byte) 0xff;
            address[12] = ip4address[0];
            address[13] = ip4address[1];
            address[14] = ip4address[2];
            address[15] = ip4address[3];
            return address;
        }
        throw new AssertionError("Should not reach here");
    }

    static void setSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name, Object value) throws IOException {
        if (value == null)
            throw new IllegalArgumentException("Invalid option value");
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class)
            throw new AssertionError("Should not reach here");
        if (name == StandardSocketOptions.SO_RCVBUF || name == StandardSocketOptions.SO_SNDBUF) {
            int i = ((Integer) value).intValue();
            if (i < 0)
                throw new IllegalArgumentException("Invalid send/receive buffer size");
        }
        if (name == StandardSocketOptions.SO_LINGER) {
            int i = ((Integer) value).intValue();
            if (i < 0)
                value = Integer.valueOf(-1);
            if (i > 65535)
                value = Integer.valueOf(65535);
        }
        if (name == StandardSocketOptions.IP_TOS) {
            int i = ((Integer) value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid IP_TOS value");
        }
        if (name == StandardSocketOptions.IP_MULTICAST_TTL) {
            int i = ((Integer) value).intValue();
            if (i < 0 || i > 255)
                throw new IllegalArgumentException("Invalid TTL/hop value");
        }
        OptionKey key = SocketOptionRegistry.findOption(name, family);
        if (key == null)
            throw new AssertionError("Option not found");
        int arg;
        if (type == Integer.class) {
            arg = ((Integer) value).intValue();
        } else {
            boolean b = ((Boolean) value).booleanValue();
            arg = (b) ? 1 : 0;
        }
        boolean mayNeedConversion = (family == UNSPEC);
        setIntOption0(fd, mayNeedConversion, key.level(), key.name(), arg);
    }

    static Object getSocketOption(FileDescriptor fd, ProtocolFamily family, SocketOption<?> name) throws IOException {
        Class<?> type = name.type();
        if (type != Integer.class && type != Boolean.class)
            throw new AssertionError("Should not reach here");
        OptionKey key = SocketOptionRegistry.findOption(name, family);
        if (key == null)
            throw new AssertionError("Option not found");
        boolean mayNeedConversion = (family == UNSPEC);
        int value = getIntOption0(fd, mayNeedConversion, key.level(), key.name());
        if (type == Integer.class) {
            return Integer.valueOf(value);
        } else {
            return (value == 0) ? Boolean.FALSE : Boolean.TRUE;
        }
    }

    private static native boolean isIPv6Available0();

    private static native int isExclusiveBindAvailable();

    private static native boolean canIPv6SocketJoinIPv4Group0();

    private static native boolean canJoin6WithIPv4Group0();

    static FileDescriptor socket(boolean stream) throws IOException {
        return socket(UNSPEC, stream);
    }

    static FileDescriptor socket(ProtocolFamily family, boolean stream) throws IOException {
        boolean preferIPv6 = isIPv6Available() && (family != StandardProtocolFamily.INET);
        return IOUtil.newFD(socket0(preferIPv6, stream, false));
    }

    static FileDescriptor serverSocket(boolean stream) {
        return IOUtil.newFD(socket0(isIPv6Available(), stream, true));
    }

    private static native int socket0(boolean preferIPv6, boolean stream, boolean reuse);

    public static void bind(FileDescriptor fd, InetAddress addr, int port) throws IOException {
        bind(UNSPEC, fd, addr, port);
    }

    static void bind(ProtocolFamily family, FileDescriptor fd, InetAddress addr, int port) throws IOException {
        boolean preferIPv6 = isIPv6Available() && (family != StandardProtocolFamily.INET);
        bind0(fd, preferIPv6, exclusiveBind, addr, port);
    }

    private static native void bind0(FileDescriptor fd, boolean preferIPv6, boolean useExclBind, InetAddress addr, int port) throws IOException;

    static native void listen(FileDescriptor fd, int backlog) throws IOException;

    static int connect(FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        return connect(UNSPEC, fd, remote, remotePort);
    }

    static int connect(ProtocolFamily family, FileDescriptor fd, InetAddress remote, int remotePort) throws IOException {
        boolean preferIPv6 = isIPv6Available() && (family != StandardProtocolFamily.INET);
        return connect0(preferIPv6, fd, remote, remotePort);
    }

    private static native int connect0(boolean preferIPv6, FileDescriptor fd, InetAddress remote, int remotePort) throws IOException;

    public final static int SHUT_RD = 0;

    public final static int SHUT_WR = 1;

    public final static int SHUT_RDWR = 2;

    static native void shutdown(FileDescriptor fd, int how) throws IOException;

    private static native int localPort(FileDescriptor fd) throws IOException;

    private static native InetAddress localInetAddress(FileDescriptor fd) throws IOException;

    public static InetSocketAddress localAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(localInetAddress(fd), localPort(fd));
    }

    private static native int remotePort(FileDescriptor fd) throws IOException;

    private static native InetAddress remoteInetAddress(FileDescriptor fd) throws IOException;

    static InetSocketAddress remoteAddress(FileDescriptor fd) throws IOException {
        return new InetSocketAddress(remoteInetAddress(fd), remotePort(fd));
    }

    private static native int getIntOption0(FileDescriptor fd, boolean mayNeedConversion, int level, int opt) throws IOException;

    private static native void setIntOption0(FileDescriptor fd, boolean mayNeedConversion, int level, int opt, int arg) throws IOException;

    static native int poll(FileDescriptor fd, int events, long timeout) throws IOException;

    static int join4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return joinOrDrop4(true, fd, group, interf, source);
    }

    static void drop4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        joinOrDrop4(false, fd, group, interf, source);
    }

    private static native int joinOrDrop4(boolean join, FileDescriptor fd, int group, int interf, int source) throws IOException;

    static int block4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        return blockOrUnblock4(true, fd, group, interf, source);
    }

    static void unblock4(FileDescriptor fd, int group, int interf, int source) throws IOException {
        blockOrUnblock4(false, fd, group, interf, source);
    }

    private static native int blockOrUnblock4(boolean block, FileDescriptor fd, int group, int interf, int source) throws IOException;

    static int join6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return joinOrDrop6(true, fd, group, index, source);
    }

    static void drop6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        joinOrDrop6(false, fd, group, index, source);
    }

    private static native int joinOrDrop6(boolean join, FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException;

    static int block6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        return blockOrUnblock6(true, fd, group, index, source);
    }

    static void unblock6(FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException {
        blockOrUnblock6(false, fd, group, index, source);
    }

    static native int blockOrUnblock6(boolean block, FileDescriptor fd, byte[] group, int index, byte[] source) throws IOException;

    static native void setInterface4(FileDescriptor fd, int interf) throws IOException;

    static native int getInterface4(FileDescriptor fd) throws IOException;

    static native void setInterface6(FileDescriptor fd, int index) throws IOException;

    static native int getInterface6(FileDescriptor fd) throws IOException;

    private static native void initIDs();

    static {
        Util.load();
        initIDs();
    }
}
