package java.net;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.security.AccessController;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class NetworkInterface {

    private String name;

    private String displayName;

    private int index;

    private InetAddress[] addrs;

    private InterfaceAddress[] bindings;

    private NetworkInterface[] childs;

    private NetworkInterface parent = null;

    private boolean virtual = false;

    private static final NetworkInterface defaultInterface;

    private static final int defaultIndex;

    static {
        AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public Void run() {
                System.loadLibrary("net");
                return null;
            }
        });
        init();
        defaultInterface = DefaultInterface.getDefault();
        if (defaultInterface != null) {
            defaultIndex = defaultInterface.getIndex();
        } else {
            defaultIndex = 0;
        }
    }

    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    public String getName() {
        return name;
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return enumerationFromArray(getCheckedInetAddresses());
    }

    public Stream<InetAddress> inetAddresses() {
        return streamFromArray(getCheckedInetAddresses());
    }

    private InetAddress[] getCheckedInetAddresses() {
        InetAddress[] local_addrs = new InetAddress[addrs.length];
        boolean trusted = true;
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            try {
                sec.checkPermission(new NetPermission("getNetworkInformation"));
            } catch (SecurityException e) {
                trusted = false;
            }
        }
        int i = 0;
        for (int j = 0; j < addrs.length; j++) {
            try {
                if (!trusted) {
                    sec.checkConnect(addrs[j].getHostAddress(), -1);
                }
                local_addrs[i++] = addrs[j];
            } catch (SecurityException e) {
            }
        }
        return Arrays.copyOf(local_addrs, i);
    }

    public java.util.List<InterfaceAddress> getInterfaceAddresses() {
        java.util.List<InterfaceAddress> lst = new java.util.ArrayList<>(1);
        if (bindings != null) {
            SecurityManager sec = System.getSecurityManager();
            for (int j = 0; j < bindings.length; j++) {
                try {
                    if (sec != null) {
                        sec.checkConnect(bindings[j].getAddress().getHostAddress(), -1);
                    }
                    lst.add(bindings[j]);
                } catch (SecurityException e) {
                }
            }
        }
        return lst;
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return enumerationFromArray(childs);
    }

    public Stream<NetworkInterface> subInterfaces() {
        return streamFromArray(childs);
    }

    public NetworkInterface getParent() {
        return parent;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return "".equals(displayName) ? null : displayName;
    }

    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null)
            throw new NullPointerException();
        return getByName0(name);
    }

    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index < 0)
            throw new IllegalArgumentException("Interface index can't be negative");
        return getByIndex0(index);
    }

    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (addr instanceof Inet4Address) {
            Inet4Address inet4Address = (Inet4Address) addr;
            if (inet4Address.holder.family != InetAddress.IPv4) {
                throw new IllegalArgumentException("invalid family type: " + inet4Address.holder.family);
            }
        } else if (addr instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) addr;
            if (inet6Address.holder.family != InetAddress.IPv6) {
                throw new IllegalArgumentException("invalid family type: " + inet6Address.holder.family);
            }
        } else {
            throw new IllegalArgumentException("invalid address type: " + addr);
        }
        return getByInetAddress0(addr);
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        NetworkInterface[] netifs = getAll();
        if (netifs != null && netifs.length > 0) {
            return enumerationFromArray(netifs);
        } else {
            throw new SocketException("No network interfaces configured");
        }
    }

    public static Stream<NetworkInterface> networkInterfaces() throws SocketException {
        NetworkInterface[] netifs = getAll();
        if (netifs != null && netifs.length > 0) {
            return streamFromArray(netifs);
        } else {
            throw new SocketException("No network interfaces configured");
        }
    }

    private static <T> Enumeration<T> enumerationFromArray(T[] a) {
        return new Enumeration<>() {

            int i = 0;

            @Override
            public T nextElement() {
                if (i < a.length) {
                    return a[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public boolean hasMoreElements() {
                return i < a.length;
            }
        };
    }

    private static <T> Stream<T> streamFromArray(T[] a) {
        return StreamSupport.stream(Spliterators.spliterator(a, Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    private static native NetworkInterface[] getAll() throws SocketException;

    private static native NetworkInterface getByName0(String name) throws SocketException;

    private static native NetworkInterface getByIndex0(int index) throws SocketException;

    private static native NetworkInterface getByInetAddress0(InetAddress addr) throws SocketException;

    public boolean isUp() throws SocketException {
        return isUp0(name, index);
    }

    public boolean isLoopback() throws SocketException {
        return isLoopback0(name, index);
    }

    public boolean isPointToPoint() throws SocketException {
        return isP2P0(name, index);
    }

    public boolean supportsMulticast() throws SocketException {
        return supportsMulticast0(name, index);
    }

    public byte[] getHardwareAddress() throws SocketException {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            try {
                sec.checkPermission(new NetPermission("getNetworkInformation"));
            } catch (SecurityException e) {
                if (!getInetAddresses().hasMoreElements()) {
                    return null;
                }
            }
        }
        for (InetAddress addr : addrs) {
            if (addr instanceof Inet4Address) {
                return getMacAddr0(((Inet4Address) addr).getAddress(), name, index);
            }
        }
        return getMacAddr0(null, name, index);
    }

    public int getMTU() throws SocketException {
        return getMTU0(name, index);
    }

    public boolean isVirtual() {
        return virtual;
    }

    private static native boolean isUp0(String name, int ind) throws SocketException;

    private static native boolean isLoopback0(String name, int ind) throws SocketException;

    private static native boolean supportsMulticast0(String name, int ind) throws SocketException;

    private static native boolean isP2P0(String name, int ind) throws SocketException;

    private static native byte[] getMacAddr0(byte[] inAddr, String name, int ind) throws SocketException;

    private static native int getMTU0(String name, int ind) throws SocketException;

    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface) obj;
        if (this.name != null) {
            if (!this.name.equals(that.name)) {
                return false;
            }
        } else {
            if (that.name != null) {
                return false;
            }
        }
        if (this.addrs == null) {
            return that.addrs == null;
        } else if (that.addrs == null) {
            return false;
        }
        if (this.addrs.length != that.addrs.length) {
            return false;
        }
        InetAddress[] thatAddrs = that.addrs;
        int count = thatAddrs.length;
        for (int i = 0; i < count; i++) {
            boolean found = false;
            for (int j = 0; j < count; j++) {
                if (addrs[i].equals(thatAddrs[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    public String toString() {
        String result = "name:";
        result += name == null ? "null" : name;
        if (displayName != null) {
            result += " (" + displayName + ")";
        }
        return result;
    }

    private static native void init();

    static NetworkInterface getDefault() {
        return defaultInterface;
    }
}
