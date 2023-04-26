package javax.net.ssl;

import java.net.*;
import javax.net.SocketFactory;
import java.io.IOException;
import java.security.*;
import java.util.Locale;
import sun.security.action.GetPropertyAction;

public abstract class SSLSocketFactory extends SocketFactory {

    private static SSLSocketFactory theFactory;

    private static boolean propertyChecked;

    static final boolean DEBUG;

    static {
        String s = java.security.AccessController.doPrivileged(new GetPropertyAction("javax.net.debug", "")).toLowerCase(Locale.ENGLISH);
        DEBUG = s.contains("all") || s.contains("ssl");
    }

    private static void log(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public SSLSocketFactory() {
    }

    public static synchronized SocketFactory getDefault() {
        if (theFactory != null) {
            return theFactory;
        }
        if (propertyChecked == false) {
            propertyChecked = true;
            String clsName = getSecurityProperty("ssl.SocketFactory.provider");
            if (clsName != null) {
                log("setting up default SSLSocketFactory");
                try {
                    Class cls = null;
                    try {
                        cls = Class.forName(clsName);
                    } catch (ClassNotFoundException e) {
                        ClassLoader cl = ClassLoader.getSystemClassLoader();
                        if (cl != null) {
                            cls = cl.loadClass(clsName);
                        }
                    }
                    log("class " + clsName + " is loaded");
                    SSLSocketFactory fac = (SSLSocketFactory) cls.newInstance();
                    log("instantiated an instance of class " + clsName);
                    theFactory = fac;
                    return fac;
                } catch (Exception e) {
                    log("SSLSocketFactory instantiation failed: " + e.toString());
                    theFactory = new DefaultSSLSocketFactory(e);
                    return theFactory;
                }
            }
        }
        try {
            return SSLContext.getDefault().getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            return new DefaultSSLSocketFactory(e);
        }
    }

    static String getSecurityProperty(final String name) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {

            public String run() {
                String s = java.security.Security.getProperty(name);
                if (s != null) {
                    s = s.trim();
                    if (s.length() == 0) {
                        s = null;
                    }
                }
                return s;
            }
        });
    }

    public abstract String[] getDefaultCipherSuites();

    public abstract String[] getSupportedCipherSuites();

    public abstract Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException;
}

class DefaultSSLSocketFactory extends SSLSocketFactory {

    private Exception reason;

    DefaultSSLSocketFactory(Exception reason) {
        this.reason = reason;
    }

    private Socket throwException() throws SocketException {
        throw (SocketException) new SocketException(reason.toString()).initCause(reason);
    }

    public Socket createSocket() throws IOException {
        return throwException();
    }

    public Socket createSocket(String host, int port) throws IOException {
        return throwException();
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return throwException();
    }

    public Socket createSocket(InetAddress address, int port) throws IOException {
        return throwException();
    }

    public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return throwException();
    }

    public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort) throws IOException {
        return throwException();
    }

    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    public String[] getSupportedCipherSuites() {
        return new String[0];
    }
}