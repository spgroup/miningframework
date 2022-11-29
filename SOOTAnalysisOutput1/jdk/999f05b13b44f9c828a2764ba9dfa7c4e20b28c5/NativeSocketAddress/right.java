package sun.nio.ch;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import jdk.internal.misc.Unsafe;
import jdk.internal.util.ArraysSupport;

class NativeSocketAddress {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    private static final int AF_INET = AFINET();

    private static final int AF_INET6 = AFINET6();

    private static final int SIZEOF_SOCKETADDRESS = sizeofSOCKETADDRESS();

    private static final int SIZEOF_FAMILY = sizeofFamily();

    private static final int OFFSET_FAMILY = offsetFamily();

    private static final int OFFSET_SIN4_PORT = offsetSin4Port();

    private static final int OFFSET_SIN4_ADDR = offsetSin4Addr();

    private static final int OFFSET_SIN6_PORT = offsetSin6Port();

    private static final int OFFSET_SIN6_ADDR = offsetSin6Addr();

    private static final int OFFSET_SIN6_SCOPE_ID = offsetSin6ScopeId();

    private final long address;

    private final long cachedSocketAddress;

    private InetSocketAddress cachedInetSocketAddress;

    NativeSocketAddress() {
        int size = SIZEOF_SOCKETADDRESS << 1;
        long base = UNSAFE.allocateMemory(size);
        UNSAFE.setMemory(base, size, (byte) 0);
        this.address = base;
        this.cachedSocketAddress = base + SIZEOF_SOCKETADDRESS;
    }

    long address() {
        return address;
    }

    void free() {
        UNSAFE.freeMemory(address);
    }

    InetSocketAddress toInetSocketAddress() throws SocketException {
        if (cachedInetSocketAddress != null && mismatch() < 0) {
            return cachedInetSocketAddress;
        }
        int family = family();
        if (family != AF_INET && family != AF_INET6)
            throw new SocketException("Socket family not recognized");
        var isa = new InetSocketAddress(address(family), port(family));
        UNSAFE.copyMemory(null, address, null, cachedSocketAddress, SIZEOF_SOCKETADDRESS);
        this.cachedInetSocketAddress = isa;
        return isa;
    }

    private int mismatch() {
        int i = ArraysSupport.vectorizedMismatch(null, address, null, cachedSocketAddress, SIZEOF_SOCKETADDRESS, ArraysSupport.LOG2_ARRAY_BYTE_INDEX_SCALE);
        if (i >= 0)
            return i;
        i = SIZEOF_SOCKETADDRESS - ~i;
        for (; i < SIZEOF_SOCKETADDRESS; i++) {
            if (UNSAFE.getByte(address + i) != UNSAFE.getByte(cachedSocketAddress + i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        int family = family();
        if (family == AF_INET || family == AF_INET6) {
            return ((family == AF_INET) ? "AF_INET" : "AF_INET6") + ", address=" + address(family) + ", port=" + port(family);
        } else {
            return "<unknown>";
        }
    }

    private int family() {
        if (SIZEOF_FAMILY == 1) {
            return UNSAFE.getByte(address + OFFSET_FAMILY);
        } else if (SIZEOF_FAMILY == 2) {
            return UNSAFE.getShort(address + OFFSET_FAMILY);
        } else {
            throw new InternalError();
        }
    }

    private int port(int family) {
        byte b1, b2;
        if (family == AF_INET) {
            b1 = UNSAFE.getByte(address + OFFSET_SIN4_PORT);
            b2 = UNSAFE.getByte(address + OFFSET_SIN4_PORT + 1);
        } else {
            b1 = UNSAFE.getByte(address + OFFSET_SIN6_PORT);
            b2 = UNSAFE.getByte(address + OFFSET_SIN6_PORT + 1);
        }
        return (Byte.toUnsignedInt(b1) << 8) + Byte.toUnsignedInt(b2);
    }

    private InetAddress address(int family) {
        int len;
        int offset;
        int scope_id;
        if (family == AF_INET) {
            len = 4;
            offset = OFFSET_SIN4_ADDR;
            scope_id = 0;
        } else {
            len = 16;
            offset = OFFSET_SIN6_ADDR;
            scope_id = UNSAFE.getInt(address + OFFSET_SIN6_SCOPE_ID);
        }
        byte[] bytes = new byte[len];
        UNSAFE.copyMemory(null, address + offset, bytes, ARRAY_BASE_OFFSET, len);
        try {
            if (scope_id == 0) {
                return InetAddress.getByAddress(bytes);
            } else {
                return Inet6Address.getByAddress(null, bytes, scope_id);
            }
        } catch (UnknownHostException e) {
            throw new InternalError(e);
        }
    }

    private static native int AFINET();

    private static native int AFINET6();

    private static native int sizeofSOCKETADDRESS();

    private static native int sizeofFamily();

    private static native int offsetFamily();

    private static native int offsetSin4Port();

    private static native int offsetSin4Addr();

    private static native int offsetSin6Port();

    private static native int offsetSin6Addr();

    private static native int offsetSin6ScopeId();

    static {
        IOUtil.load();
    }
}
