package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;

public class InetSocketAddress extends SocketAddress {

    private static class InetSocketAddressHolder {

        private String hostname;

        private InetAddress addr;

        private int port;

        private InetSocketAddressHolder(String hostname, InetAddress addr, int port) {
            this.hostname = hostname;
            this.addr = addr;
            this.port = port;
        }

        private int getPort() {
            return port;
        }

        private InetAddress getAddress() {
            return addr;
        }

        private String getHostName() {
            if (hostname != null)
                return hostname;
            if (addr != null)
                return addr.getHostName();
            return null;
        }

        private String getHostString() {
            if (hostname != null)
                return hostname;
            if (addr != null) {
                if (addr.holder().getHostName() != null)
                    return addr.holder().getHostName();
                else
                    return addr.getHostAddress();
            }
            return null;
        }

        private boolean isUnresolved() {
            return addr == null;
        }

        @Override
        public String toString() {
            if (isUnresolved()) {
                return hostname + ":" + port;
            } else {
                return addr.toString() + ":" + port;
            }
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null || !(obj instanceof InetSocketAddressHolder))
                return false;
            InetSocketAddressHolder that = (InetSocketAddressHolder) obj;
            boolean sameIP;
            if (addr != null)
                sameIP = addr.equals(that.addr);
            else if (hostname != null)
                sameIP = (that.addr == null) && hostname.equalsIgnoreCase(that.hostname);
            else
                sameIP = (that.addr == null) && (that.hostname == null);
            return sameIP && (port == that.port);
        }

        @Override
        public final int hashCode() {
            if (addr != null)
                return addr.hashCode() + port;
            if (hostname != null)
                return hostname.toLowerCase().hashCode() + port;
            return port;
        }
    }

    private final transient InetSocketAddressHolder holder;

    private static final long serialVersionUID = 5076001401234631237L;

    private static int checkPort(int port) {
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException("port out of range:" + port);
        return port;
    }

    private static String checkHost(String hostname) {
        if (hostname == null)
            throw new IllegalArgumentException("hostname can't be null");
        return hostname;
    }

    public InetSocketAddress(int port) {
        this(InetAddress.anyLocalAddress(), port);
    }

    public InetSocketAddress(InetAddress addr, int port) {
        holder = new InetSocketAddressHolder(null, addr == null ? InetAddress.anyLocalAddress() : addr, checkPort(port));
    }

    public InetSocketAddress(String hostname, int port) {
        checkHost(hostname);
        InetAddress addr = null;
        String host = null;
        try {
            addr = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            host = hostname;
        }
        holder = new InetSocketAddressHolder(host, addr, checkPort(port));
    }

    private InetSocketAddress(int port, String hostname) {
        holder = new InetSocketAddressHolder(hostname, null, port);
    }

    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(checkPort(port), checkHost(host));
    }

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("hostname", String.class), new ObjectStreamField("addr", InetAddress.class), new ObjectStreamField("port", int.class) };

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("hostname", holder.hostname);
        pfields.put("addr", holder.addr);
        pfields.put("port", holder.port);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField oisFields = in.readFields();
        final String oisHostname = (String) oisFields.get("hostname", null);
        final InetAddress oisAddr = (InetAddress) oisFields.get("addr", null);
        final int oisPort = oisFields.get("port", -1);
        checkPort(oisPort);
        if (oisHostname == null && oisAddr == null)
            throw new InvalidObjectException("hostname and addr " + "can't both be null");
        InetSocketAddressHolder h = new InetSocketAddressHolder(oisHostname, oisAddr, oisPort);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Stream data required");
    }

    private static final long FIELDS_OFFSET;

    private static final jdk.internal.misc.Unsafe UNSAFE;

    static {
        try {
            jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(InetSocketAddress.class.getDeclaredField("holder"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public final int getPort() {
        return holder.getPort();
    }

    public final InetAddress getAddress() {
        return holder.getAddress();
    }

    public final String getHostName() {
        return holder.getHostName();
    }

    public final String getHostString() {
        return holder.getHostString();
    }

    public final boolean isUnresolved() {
        return holder.isUnresolved();
    }

    @Override
    public String toString() {
        return holder.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InetSocketAddress))
            return false;
        return holder.equals(((InetSocketAddress) obj).holder);
    }

    @Override
    public final int hashCode() {
        return holder.hashCode();
    }
}
