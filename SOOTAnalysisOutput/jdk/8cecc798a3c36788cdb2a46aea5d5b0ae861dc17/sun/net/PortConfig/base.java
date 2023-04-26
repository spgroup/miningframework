package sun.net;

import java.security.AccessController;

public final class PortConfig {

    private static int defaultUpper, defaultLower;

    private static final int upper, lower;

    private PortConfig() {
    }

    static {
        AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public Void run() {
                System.loadLibrary("net");
                String os = System.getProperty("os.name");
                if (os.startsWith("Linux")) {
                    defaultLower = 32768;
                    defaultUpper = 61000;
                } else if (os.startsWith("SunOS")) {
                    defaultLower = 32768;
                    defaultUpper = 65535;
                } else if (os.contains("OS X")) {
                    defaultLower = 49152;
                    defaultUpper = 65535;
                } else if (os.startsWith("AIX")) {
                    defaultLower = 32768;
                    defaultUpper = 65535;
                } else {
                    throw new InternalError("sun.net.PortConfig: unknown OS");
                }
                return null;
            }
        });
        int v = getLower0();
        if (v == -1) {
            v = defaultLower;
        }
        lower = v;
        v = getUpper0();
        if (v == -1) {
            v = defaultUpper;
        }
        upper = v;
    }

    static native int getLower0();

    static native int getUpper0();

    public static int getLower() {
        return lower;
    }

    public static int getUpper() {
        return upper;
    }
}
