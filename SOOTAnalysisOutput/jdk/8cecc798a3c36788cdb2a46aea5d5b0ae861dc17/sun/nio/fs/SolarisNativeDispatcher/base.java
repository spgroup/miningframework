package sun.nio.fs;

import java.security.AccessController;
import java.security.PrivilegedAction;

class SolarisNativeDispatcher extends UnixNativeDispatcher {

    private SolarisNativeDispatcher() {
    }

    static native int getextmntent(long fp, UnixMountEntry entry) throws UnixException;

    static native int facl(int fd, int cmd, int nentries, long aclbufp) throws UnixException;

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
