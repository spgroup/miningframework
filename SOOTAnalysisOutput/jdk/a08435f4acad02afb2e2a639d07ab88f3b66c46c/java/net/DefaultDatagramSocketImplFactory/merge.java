package java.net;

import java.security.AccessController;
import java.security.PrivilegedAction;

class DefaultDatagramSocketImplFactory {

    static Class<?> prefixImplClass = null;

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
            if (isMulticast)
                exclusiveBind = false;
            if (useDualStackImpl && !isMulticast)
                return new DualStackPlainDatagramSocketImpl(exclusiveBind);
            else
                return new TwoStacksPlainDatagramSocketImpl(exclusiveBind);
        }
    }
}
