package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public abstract class SocketImpl implements SocketOptions {

    Socket socket = null;

    ServerSocket serverSocket = null;

    protected FileDescriptor fd;

    protected InetAddress address;

    protected int port;

    protected int localport;

    protected abstract void create(boolean stream) throws IOException;

    protected abstract void connect(String host, int port) throws IOException;

    protected abstract void connect(InetAddress address, int port) throws IOException;

    protected abstract void connect(SocketAddress address, int timeout) throws IOException;

    protected abstract void bind(InetAddress host, int port) throws IOException;

    protected abstract void listen(int backlog) throws IOException;

    protected abstract void accept(SocketImpl s) throws IOException;

    protected abstract InputStream getInputStream() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    protected abstract int available() throws IOException;

    protected abstract void close() throws IOException;

    protected void shutdownInput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected void shutdownOutput() throws IOException {
        throw new IOException("Method not implemented!");
    }

    protected FileDescriptor getFileDescriptor() {
        return fd;
    }

    protected InetAddress getInetAddress() {
        return address;
    }

    protected int getPort() {
        return port;
    }

    protected boolean supportsUrgentData() {
        return false;
    }

    protected abstract void sendUrgentData(int data) throws IOException;

    protected int getLocalPort() {
        return localport;
    }

    void setSocket(Socket soc) {
        this.socket = soc;
    }

    Socket getSocket() {
        return socket;
    }

    void setServerSocket(ServerSocket soc) {
        this.serverSocket = soc;
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    public String toString() {
        return "Socket[addr=" + getInetAddress() + ",port=" + getPort() + ",localport=" + getLocalPort() + "]";
    }

    void reset() throws IOException {
        address = null;
        port = 0;
        localport = 0;
    }

    protected void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE && (getSocket() != null)) {
            setOption(SocketOptions.SO_KEEPALIVE, value);
        } else if (name == StandardSocketOptions.SO_SNDBUF && (getSocket() != null)) {
            setOption(SocketOptions.SO_SNDBUF, value);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            setOption(SocketOptions.SO_RCVBUF, value);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            setOption(SocketOptions.SO_REUSEADDR, value);
        } else if (name == StandardSocketOptions.SO_REUSEPORT && supportedOptions().contains(name)) {
            setOption(SocketOptions.SO_REUSEPORT, value);
        } else if (name == StandardSocketOptions.SO_LINGER && (getSocket() != null)) {
            setOption(SocketOptions.SO_LINGER, value);
        } else if (name == StandardSocketOptions.IP_TOS) {
            setOption(SocketOptions.IP_TOS, value);
        } else if (name == StandardSocketOptions.TCP_NODELAY && (getSocket() != null)) {
            setOption(SocketOptions.TCP_NODELAY, value);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getOption(SocketOption<T> name) throws IOException {
        if (name == StandardSocketOptions.SO_KEEPALIVE && (getSocket() != null)) {
            return (T) getOption(SocketOptions.SO_KEEPALIVE);
        } else if (name == StandardSocketOptions.SO_SNDBUF && (getSocket() != null)) {
            return (T) getOption(SocketOptions.SO_SNDBUF);
        } else if (name == StandardSocketOptions.SO_RCVBUF) {
            return (T) getOption(SocketOptions.SO_RCVBUF);
        } else if (name == StandardSocketOptions.SO_REUSEADDR) {
            return (T) getOption(SocketOptions.SO_REUSEADDR);
        } else if (name == StandardSocketOptions.SO_REUSEPORT && supportedOptions().contains(name)) {
            return (T) getOption(SocketOptions.SO_REUSEPORT);
        } else if (name == StandardSocketOptions.SO_LINGER && (getSocket() != null)) {
            return (T) getOption(SocketOptions.SO_LINGER);
        } else if (name == StandardSocketOptions.IP_TOS) {
            return (T) getOption(SocketOptions.IP_TOS);
        } else if (name == StandardSocketOptions.TCP_NODELAY && (getSocket() != null)) {
            return (T) getOption(SocketOptions.TCP_NODELAY);
        } else {
            throw new UnsupportedOperationException("unsupported option");
        }
    }

    private static final Set<SocketOption<?>> socketOptions = new HashSet<>();

    private static final Set<SocketOption<?>> serverSocketOptions = new HashSet<>();

    static {
        socketOptions.add(StandardSocketOptions.SO_KEEPALIVE);
        socketOptions.add(StandardSocketOptions.SO_SNDBUF);
        socketOptions.add(StandardSocketOptions.SO_RCVBUF);
        socketOptions.add(StandardSocketOptions.SO_REUSEADDR);
        socketOptions.add(StandardSocketOptions.SO_LINGER);
        socketOptions.add(StandardSocketOptions.IP_TOS);
        socketOptions.add(StandardSocketOptions.TCP_NODELAY);
        serverSocketOptions.add(StandardSocketOptions.SO_RCVBUF);
        serverSocketOptions.add(StandardSocketOptions.SO_REUSEADDR);
        serverSocketOptions.add(StandardSocketOptions.IP_TOS);
    }

    protected Set<SocketOption<?>> supportedOptions() {
        if (getSocket() != null) {
            return socketOptions;
        } else {
            return serverSocketOptions;
        }
    }
}
