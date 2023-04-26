package sun.nio.fs;

class AixNativeDispatcher extends UnixNativeDispatcher {

    private AixNativeDispatcher() {
    }

    static native UnixMountEntry[] getmntctl() throws UnixException;

    private static native void init();

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
        init();
    }
}
