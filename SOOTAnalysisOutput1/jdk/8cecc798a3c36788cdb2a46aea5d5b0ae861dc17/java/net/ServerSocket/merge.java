package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import sun.net.PlatformSocketImpl;

public class ServerSocket implements java.io.Closeable {

    private boolean created = false;

    private boolean bound = false;

    private boolean closed = false;

    private Object closeLock = new Object();

    private SocketImpl impl;

    protected ServerSocket(SocketImpl impl) {
        Objects.requireNonNull(impl);
        this.impl = impl;
    }

    public ServerSocket() throws IOException {
        setImpl();
    }

    public ServerSocket(int port) throws IOException {
        this(port, 50, null);
    }

    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, null);
    }

    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        setImpl();
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException("Port value out of range: " + port);
        if (backlog < 1)
            backlog = 50;
        try {
            bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch (SecurityException e) {
            close();
            throw e;
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl();
        return impl;
    }

    private void setImpl() {
        SocketImplFactory factory = ServerSocket.factory;
        if (factory != null) {
            impl = factory.createSocketImpl();
        } else {
            impl = SocketImpl.createPlatformSocketImpl(true);
        }
    }

    void createImpl() throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(true);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, 50);
    }

    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (isBound())
            throw new SocketException("Already bound");
        if (endpoint == null)
            endpoint = new InetSocketAddress(0);
        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if (backlog < 1)
            backlog = 50;
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null)
                security.checkListen(epoint.getPort());
            getImpl().bind(epoint.getAddress(), epoint.getPort());
            getImpl().listen(backlog);
            bound = true;
        } catch (SecurityException e) {
            bound = false;
            throw e;
        } catch (IOException e) {
            bound = false;
            throw e;
        }
    }

    public InetAddress getInetAddress() {
        if (!isBound())
            return null;
        try {
            InetAddress in = getImpl().getInetAddress();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkConnect(in.getHostAddress(), -1);
            return in;
        } catch (SecurityException e) {
            return InetAddress.getLoopbackAddress();
        } catch (SocketException e) {
        }
        return null;
    }

    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
        }
        return -1;
    }

    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");
        Socket s = new Socket((SocketImpl) null);
        implAccept(s);
        return s;
    }

    protected final void implAccept(Socket s) throws IOException {
        SocketImpl si = s.impl;
        if (si == null) {
            si = implAccept();
            s.setImpl(si);
            s.postAccept();
            return;
        }
        if (si instanceof DelegatingSocketImpl) {
            si = ((DelegatingSocketImpl) si).delegate();
            assert si instanceof PlatformSocketImpl;
        }
        ensureCompatible(si);
        if (impl instanceof PlatformSocketImpl) {
            SocketImpl psi = platformImplAccept();
            si.copyOptionsTo(psi);
            s.setImpl(psi);
            si.closeQuietly();
        } else {
            s.impl = null;
            try {
                customImplAccept(si);
            } finally {
                s.impl = si;
            }
        }
        s.postAccept();
    }

    private SocketImpl implAccept() throws IOException {
        if (impl instanceof PlatformSocketImpl) {
            return platformImplAccept();
        } else {
            SocketImplFactory factory = Socket.socketImplFactory();
            if (factory == null) {
                throw new IOException("An instance of " + impl.getClass() + " cannot accept connection with 'null' SocketImpl:" + " client socket implementation factory not set");
            }
            SocketImpl si = factory.createSocketImpl();
            customImplAccept(si);
            return si;
        }
    }

    private SocketImpl platformImplAccept() throws IOException {
        assert impl instanceof PlatformSocketImpl;
        SocketImpl psi = SocketImpl.createPlatformSocketImpl(false);
        implAccept(psi);
        return psi;
    }

    private void customImplAccept(SocketImpl si) throws IOException {
        assert !(impl instanceof PlatformSocketImpl) && !(si instanceof PlatformSocketImpl);
        si.reset();
        try {
            si.fd = new FileDescriptor();
            si.address = new InetAddress();
            implAccept(si);
        } catch (Exception e) {
            si.reset();
            throw e;
        }
    }

    private void implAccept(SocketImpl si) throws IOException {
        assert !(si instanceof DelegatingSocketImpl);
        impl.accept(si);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkAccept(si.getInetAddress().getHostAddress(), si.getPort());
            } catch (SecurityException se) {
                si.close();
                throw se;
            }
        }
    }

    private void ensureCompatible(SocketImpl si) throws IOException {
        if ((impl instanceof PlatformSocketImpl) != (si instanceof PlatformSocketImpl)) {
            throw new IOException("An instance of " + impl.getClass() + " cannot accept a connection with an instance of " + si.getClass());
        }
    }

    public void close() throws IOException {
        synchronized (closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }

    public ServerSocketChannel getChannel() {
        return null;
    }

    public boolean isBound() {
        return bound;
    }

    public boolean isClosed() {
        synchronized (closeLock) {
            return closed;
        }
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (timeout < 0)
            throw new IllegalArgumentException("timeout < 0");
        getImpl().setOption(SocketOptions.SO_TIMEOUT, timeout);
    }

    public synchronized int getSoTimeout() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }

    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }

    public boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }

    public String toString() {
        if (!isBound())
            return "ServerSocket[unbound]";
        InetAddress in;
        if (System.getSecurityManager() != null)
            in = getInetAddress();
        else
            in = impl.getInetAddress();
        return "ServerSocket[addr=" + in + ",localport=" + impl.getLocalPort() + "]";
    }

    private static volatile SocketImplFactory factory;

    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, size);
    }

    public synchronized int getReceiveBufferSize() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer) o).intValue();
        }
        return result;
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    public <T> ServerSocket setOption(SocketOption<T> name, T value) throws IOException {
        Objects.requireNonNull(name);
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(name, value);
        return this;
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        Objects.requireNonNull(name);
        if (isClosed())
            throw new SocketException("Socket is closed");
        return getImpl().getOption(name);
    }

    private volatile Set<SocketOption<?>> options;

    public Set<SocketOption<?>> supportedOptions() {
        Set<SocketOption<?>> so = options;
        if (so != null)
            return so;
        try {
            SocketImpl impl = getImpl();
            options = Collections.unmodifiableSet(impl.supportedOptions());
        } catch (IOException e) {
            options = Collections.emptySet();
        }
        return options;
    }
}
