package java.net;

import java.security.AccessController;
import java.security.PrivilegedAction;

class DefaultDatagramSocketImplFactory {

    static Class<?> prefixImplClass = null;

    private static float version;

    private static boolean preferIPv4Stack = false;

    private static boolean useDualStackImpl = false;

    static {
        java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                version = 0;
                try {
                    version = Float.parseFloat(System.getProperties().getProperty("os.version"));
                    preferIPv4Stack = Boolean.parseBoolean(System.getProperties().getProperty("java.net.preferIPv4Stack"));
                } catch (NumberFormatException e) {
                    assert false : e;
                }
                return null;
            }
        });
        if (version >= 6.0 && !preferIPv4Stack) {
            useDualStackImpl = true;
        }
        String prefix = null;
        try {
            prefix = AccessController.doPrivileged(new sun.security.action.GetPropertyAction("impl.prefix", null));
            if (prefix != null)
                prefixImplClass = Class.forName("java.net." + prefix + "DatagramSocketImpl");
        } catch (Exception e) {
            System.err.println("Can't find class: java.net." + prefix + "DatagramSocketImpl: check impl.prefix property");
        }
    }

    static DatagramSocketImpl createDatagramSocketImpl(boolean isMulticast) throws SocketException {
        if (prefixImplClass != null) {
            try {
                return (DatagramSocketImpl) prefixImplClass.newInstance();
            } catch (Exception e) {
                throw new SocketException("can't instantiate DatagramSocketImpl");
            }
        } else {
            if (useDualStackImpl && !isMulticast)
                return new DualStackPlainDatagramSocketImpl();
            else
                return new TwoStacksPlainDatagramSocketImpl();
        }
    }
}
