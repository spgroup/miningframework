package sun.nio.fs;

class SolarisNativeDispatcher extends UnixNativeDispatcher {

    private SolarisNativeDispatcher() {
    }

    static native int getextmntent(long fp, UnixMountEntry entry) throws UnixException;

    static native int facl(int fd, int cmd, int nentries, long aclbufp) throws UnixException;

    private static native void init();

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
        init();
    }
}
