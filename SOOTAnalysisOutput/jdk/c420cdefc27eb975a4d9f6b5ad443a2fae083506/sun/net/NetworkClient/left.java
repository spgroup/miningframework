package sun.net;

import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.Proxy;
import java.util.Arrays;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class NetworkClient {

    public static final int DEFAULT_READ_TIMEOUT = -1;

    public static final int DEFAULT_CONNECT_TIMEOUT = -1;

    protected Proxy proxy = Proxy.NO_PROXY;

    protected Socket serverSocket = null;

    public PrintStream serverOutput;

    public InputStream serverInput;

    protected static int defaultSoTimeout;

    protected static int defaultConnectTimeout;

    protected int readTimeout = DEFAULT_READ_TIMEOUT;

    protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    protected static String encoding;

    static {
        final int[] vals = { 0, 0 };
        final String[] encs = { null };
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0).intValue();
                vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0).intValue();
                encs[0] = System.getProperty("file.encoding", "ISO8859_1");
                return null;
            }
        });
        if (vals[0] != 0) {
            defaultSoTimeout = vals[0];
        }
        if (vals[1] != 0) {
            defaultConnectTimeout = vals[1];
        }
        encoding = encs[0];
        try {
            if (!isASCIISuperset(encoding)) {
                encoding = "ISO8859_1";
            }
        } catch (Exception e) {
            encoding = "ISO8859_1";
        }
    }

    private static boolean isASCIISuperset(String encoding) throws Exception {
        String chkS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,";
        byte[] chkB = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 45, 95, 46, 33, 126, 42, 39, 40, 41, 59, 47, 63, 58, 64, 38, 61, 43, 36, 44 };
        byte[] b = chkS.getBytes(encoding);
        return Arrays.equals(b, chkB);
    }

    public void openServer(String server, int port) throws IOException, UnknownHostException {
        if (serverSocket != null)
            closeServer();
        serverSocket = doConnect(server, port);
        try {
            serverOutput = new PrintStream(new BufferedOutputStream(serverSocket.getOutputStream()), true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found");
        }
        serverInput = new BufferedInputStream(serverSocket.getInputStream());
    }

    protected Socket doConnect(String server, int port) throws IOException, UnknownHostException {
        Socket s;
        if (proxy != null) {
            if (proxy.type() == Proxy.Type.SOCKS) {
                s = AccessController.doPrivileged(new PrivilegedAction<Socket>() {

                    public Socket run() {
                        return new Socket(proxy);
                    }
                });
            } else if (proxy.type() == Proxy.Type.DIRECT) {
                s = createSocket();
            } else {
                s = new Socket(Proxy.NO_PROXY);
            }
        } else
            s = createSocket();
        if (connectTimeout >= 0) {
            s.connect(new InetSocketAddress(server, port), connectTimeout);
        } else {
            if (defaultConnectTimeout > 0) {
                s.connect(new InetSocketAddress(server, port), defaultConnectTimeout);
            } else {
                s.connect(new InetSocketAddress(server, port));
            }
        }
        if (readTimeout >= 0)
            s.setSoTimeout(readTimeout);
        else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        return s;
    }

    protected Socket createSocket() throws IOException {
        return new java.net.Socket();
    }

    protected InetAddress getLocalAddress() throws IOException {
        if (serverSocket == null)
            throw new IOException("not connected");
        return serverSocket.getLocalAddress();
    }

    public void closeServer() throws IOException {
        if (!serverIsOpen()) {
            return;
        }
        serverSocket.close();
        serverSocket = null;
        serverInput = null;
        serverOutput = null;
    }

    public boolean serverIsOpen() {
        return serverSocket != null;
    }

    public NetworkClient(String host, int port) throws IOException {
        openServer(host, port);
    }

    public NetworkClient() {
    }

    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout == DEFAULT_READ_TIMEOUT)
            timeout = defaultSoTimeout;
        if (serverSocket != null && timeout >= 0) {
            try {
                serverSocket.setSoTimeout(timeout);
            } catch (IOException e) {
            }
        }
        readTimeout = timeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }
}
