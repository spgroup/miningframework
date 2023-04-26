package java.net;

public final class DatagramPacket {

    static {
        jdk.internal.loader.BootLoader.loadLibrary("net");
        init();
    }

    byte[] buf;

    int offset;

    int length;

    int bufLength;

    InetAddress address;

    int port;

    public DatagramPacket(byte[] buf, int offset, int length) {
        setData(buf, offset, length);
        this.address = null;
        this.port = -1;
    }

    public DatagramPacket(byte[] buf, int length) {
        this(buf, 0, length);
    }

    public DatagramPacket(byte[] buf, int offset, int length, InetAddress address, int port) {
        setData(buf, offset, length);
        setAddress(address);
        setPort(port);
    }

    public DatagramPacket(byte[] buf, int offset, int length, SocketAddress address) {
        setData(buf, offset, length);
        setSocketAddress(address);
    }

    public DatagramPacket(byte[] buf, int length, InetAddress address, int port) {
        this(buf, 0, length, address, port);
    }

    public DatagramPacket(byte[] buf, int length, SocketAddress address) {
        this(buf, 0, length, address);
    }

    public synchronized InetAddress getAddress() {
        return address;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized byte[] getData() {
        return buf;
    }

    public synchronized int getOffset() {
        return offset;
    }

    public synchronized int getLength() {
        return length;
    }

    public synchronized void setData(byte[] buf, int offset, int length) {
        if (length < 0 || offset < 0 || (length + offset) < 0 || ((length + offset) > buf.length)) {
            throw new IllegalArgumentException("illegal length or offset");
        }
        this.buf = buf;
        this.length = length;
        this.bufLength = length;
        this.offset = offset;
    }

    public synchronized void setAddress(InetAddress iaddr) {
        address = iaddr;
    }

    public synchronized void setPort(int iport) {
        if (iport < 0 || iport > 0xFFFF) {
            throw new IllegalArgumentException("Port out of range:" + iport);
        }
        port = iport;
    }

    public synchronized void setSocketAddress(SocketAddress address) {
        if (address == null || !(address instanceof InetSocketAddress))
            throw new IllegalArgumentException("unsupported address type");
        InetSocketAddress addr = (InetSocketAddress) address;
        if (addr.isUnresolved())
            throw new IllegalArgumentException("unresolved address");
        setAddress(addr.getAddress());
        setPort(addr.getPort());
    }

    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }

    public synchronized void setData(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("null packet buffer");
        }
        this.buf = buf;
        this.offset = 0;
        this.length = buf.length;
        this.bufLength = buf.length;
    }

    public synchronized void setLength(int length) {
        if ((length + offset) > buf.length || length < 0 || (length + offset) < 0) {
            throw new IllegalArgumentException("illegal length");
        }
        this.length = length;
        this.bufLength = this.length;
    }

    private static native void init();
}
