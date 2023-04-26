package sun.nio.fs;

import java.security.AccessController;
import java.security.PrivilegedAction;

class AixNativeDispatcher extends UnixNativeDispatcher {

    private AixNativeDispatcher() {
    }

    static native UnixMountEntry[] getmntctl() throws UnixException;

    private static native void init();

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("nio");
                return null;
            }
        });
        init();
    }
}
