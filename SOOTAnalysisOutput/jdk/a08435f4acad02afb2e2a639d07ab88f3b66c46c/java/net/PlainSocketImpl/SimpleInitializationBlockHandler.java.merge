package java.net;

import java.io.*;
import java.security.PrivilegedAction;

class PlainSocketImpl extends AbstractPlainSocketImpl {

    private AbstractPlainSocketImpl impl;

    private static float version;

    private static boolean preferIPv4Stack = false;

    private static boolean useDualStackImpl = false;

    private static String exclBindProp;

    private static boolean exclusiveBind = true;

    static {
        java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                version = 0;
                try {
                    version = Float.parseFloat(System.getProperties().getProperty("os.version"));
                    preferIPv4Stack = Boolean.parseBoolean(System.getProperties().getProperty("java.net.preferIPv4Stack"));
                    exclBindProp = System.getProperty("sun.net.useExclusiveBind");
                } catch (NumberFormatException e) {
                    assert false : e;
                }
                return null;
            }
        });
        if (version >= 6.0 && !preferIPv4Stack) {
            useDualStackImpl = true;
        }
        if (exclBindProp != null) {
            exclusiveBind = exclBindProp.length() == 0 ? true : Boolean.parseBoolean(exclBindProp);
        } else if (version < 6.0) {
            exclusiveBind = false;
        }
    }

    PlainSocketImpl() {
        if (useDualStackImpl) {
            impl = new DualStackPlainSocketImpl(exclusiveBind);
        } else {
            impl = new TwoStacksPlainSocketImpl(exclusiveBind);
        }
    }

    PlainSocketImpl(FileDescriptor fd) {
        if (useDualStackImpl) {
            impl = new DualStackPlainSocketImpl(fd, exclusiveBind);
        } else {
            impl = new TwoStacksPlainSocketImpl(fd, exclusiveBind);
        }
    }

    protected FileDescriptor getFileDescriptor() {
        return impl.getFileDescriptor();
    }

    protected InetAddress getInetAddress() {
        return impl.getInetAddress();
    }

    protected int getPort() {
        return impl.getPort();
    }

    protected int getLocalPort() {
        return impl.getLocalPort();
    }

    void setSocket(Socket soc) {
        impl.setSocket(soc);
    }

    Socket getSocket() {
        return impl.getSocket();
    }

    void setServerSocket(ServerSocket soc) {
        impl.setServerSocket(soc);
    }

    ServerSocket getServerSocket() {
        return impl.getServerSocket();
    }

    public String toString() {
        return impl.toString();
    }

    protected synchronized void create(boolean stream) throws IOException {
        impl.create(stream);
        this.fd = impl.fd;
    }

    protected void connect(String host, int port) throws UnknownHostException, IOException {
        impl.connect(host, port);
    }

    protected void connect(InetAddress address, int port) throws IOException {
        impl.connect(address, port);
    }

    protected void connect(SocketAddress address, int timeout) throws IOException {
        impl.connect(address, timeout);
    }

    public void setOption(int opt, Object val) throws SocketException {
        impl.setOption(opt, val);
    }

    public Object getOption(int opt) throws SocketException {
        return impl.getOption(opt);
    }

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        impl.doConnect(address, port, timeout);
    }

    protected synchronized void bind(InetAddress address, int lport) throws IOException {
        impl.bind(address, lport);
    }

    protected synchronized void accept(SocketImpl s) throws IOException {
        SocketImpl delegate = ((PlainSocketImpl) s).impl;
        delegate.address = new InetAddress();
        delegate.fd = new FileDescriptor();
        impl.accept(delegate);
        s.fd = delegate.fd;
    }

    void setFileDescriptor(FileDescriptor fd) {
        impl.setFileDescriptor(fd);
    }

    void setAddress(InetAddress address) {
        impl.setAddress(address);
    }

    void setPort(int port) {
        impl.setPort(port);
    }

    void setLocalPort(int localPort) {
        impl.setLocalPort(localPort);
    }

    protected synchronized InputStream getInputStream() throws IOException {
        return impl.getInputStream();
    }

    void setInputStream(SocketInputStream in) {
        impl.setInputStream(in);
    }

    protected synchronized OutputStream getOutputStream() throws IOException {
        return impl.getOutputStream();
    }

    protected void close() throws IOException {
        try {
            impl.close();
        } finally {
            this.fd = null;
        }
    }

    void reset() throws IOException {
        try {
            impl.reset();
        } finally {
            this.fd = null;
        }
    }

    protected void shutdownInput() throws IOException {
        impl.shutdownInput();
    }

    protected void shutdownOutput() throws IOException {
        impl.shutdownOutput();
    }

    protected void sendUrgentData(int data) throws IOException {
        impl.sendUrgentData(data);
    }

    FileDescriptor acquireFD() {
        return impl.acquireFD();
    }

    void releaseFD() {
        impl.releaseFD();
    }

    public boolean isConnectionReset() {
        return impl.isConnectionReset();
    }

    public boolean isConnectionResetPending() {
        return impl.isConnectionResetPending();
    }

    public void setConnectionReset() {
        impl.setConnectionReset();
    }

    public void setConnectionResetPending() {
        impl.setConnectionResetPending();
    }

    public boolean isClosedOrPending() {
        return impl.isClosedOrPending();
    }

    public int getTimeout() {
        return impl.getTimeout();
    }

    void socketCreate(boolean isServer) throws IOException {
        impl.socketCreate(isServer);
    }

    void socketConnect(InetAddress address, int port, int timeout) throws IOException {
        impl.socketConnect(address, port, timeout);
    }

    void socketBind(InetAddress address, int port) throws IOException {
        impl.socketBind(address, port);
    }

    void socketListen(int count) throws IOException {
        impl.socketListen(count);
    }

    void socketAccept(SocketImpl s) throws IOException {
        impl.socketAccept(s);
    }

    int socketAvailable() throws IOException {
        return impl.socketAvailable();
    }

    void socketClose0(boolean useDeferredClose) throws IOException {
        impl.socketClose0(useDeferredClose);
    }

    void socketShutdown(int howto) throws IOException {
        impl.socketShutdown(howto);
    }

    void socketSetOption(int cmd, boolean on, Object value) throws SocketException {
        impl.socketSetOption(cmd, on, value);
    }

    int socketGetOption(int opt, Object iaContainerObj) throws SocketException {
        return impl.socketGetOption(opt, iaContainerObj);
    }

    void socketSendUrgentData(int data) throws IOException {
        impl.socketSendUrgentData(data);
    }
}