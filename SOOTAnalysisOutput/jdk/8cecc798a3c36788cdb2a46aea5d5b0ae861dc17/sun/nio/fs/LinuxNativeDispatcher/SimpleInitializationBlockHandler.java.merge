package sun.nio.fs;

class LinuxNativeDispatcher extends UnixNativeDispatcher {

    private LinuxNativeDispatcher() {
    }

    static long setmntent(byte[] filename, byte[] type) throws UnixException {
        NativeBuffer pathBuffer = NativeBuffers.asNativeBuffer(filename);
        NativeBuffer typeBuffer = NativeBuffers.asNativeBuffer(type);
        try {
            return setmntent0(pathBuffer.address(), typeBuffer.address());
        } finally {
            typeBuffer.release();
            pathBuffer.release();
        }
    }

    private static native long setmntent0(long pathAddress, long typeAddress) throws UnixException;

    static native int getmntent(long fp, UnixMountEntry entry) throws UnixException;

    static native void endmntent(long stream) throws UnixException;

    static int fgetxattr(int filedes, byte[] name, long valueAddress, int valueLen) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            return fgetxattr0(filedes, buffer.address(), valueAddress, valueLen);
        } finally {
            buffer.release();
        }
    }

    private static native int fgetxattr0(int filedes, long nameAddress, long valueAddress, int valueLen) throws UnixException;

    static void fsetxattr(int filedes, byte[] name, long valueAddress, int valueLen) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            fsetxattr0(filedes, buffer.address(), valueAddress, valueLen);
        } finally {
            buffer.release();
        }
    }

    private static native void fsetxattr0(int filedes, long nameAddress, long valueAddress, int valueLen) throws UnixException;

    static void fremovexattr(int filedes, byte[] name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name);
        try {
            fremovexattr0(filedes, buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native void fremovexattr0(int filedes, long nameAddress) throws UnixException;

    static native int flistxattr(int filedes, long listAddress, int size) throws UnixException;

    private static native void init();

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
        init();
    }
}