package sun.net.sdp;

import java.io.IOException;
import java.io.FileDescriptor;
import jdk.internal.access.SharedSecrets;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import sun.security.action.GetPropertyAction;

public final class SdpSupport {

    private static final String os = GetPropertyAction.privilegedGetProperty("os.name");

    private static final boolean isSupported = (os.equals("SunOS") || (os.equals("Linux")));

    private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();

    private SdpSupport() {
    }

    public static FileDescriptor createSocket() throws IOException {
        if (!isSupported)
            throw new UnsupportedOperationException("SDP not supported on this platform");
        int fdVal = create0();
        FileDescriptor fd = new FileDescriptor();
        fdAccess.set(fd, fdVal);
        return fd;
    }

    public static void convertSocket(FileDescriptor fd) throws IOException {
        if (!isSupported)
            throw new UnsupportedOperationException("SDP not supported on this platform");
        int fdVal = fdAccess.get(fd);
        convert0(fdVal);
    }

    private static native int create0() throws IOException;

    private static native void convert0(int fd) throws IOException;

    static {
        jdk.internal.loader.BootLoader.loadLibrary("net");
    }
}
