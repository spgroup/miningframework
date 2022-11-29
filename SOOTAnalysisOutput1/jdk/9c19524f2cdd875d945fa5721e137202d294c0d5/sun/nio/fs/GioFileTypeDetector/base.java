package sun.nio.fs;

import java.nio.file.Path;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class GioFileTypeDetector extends AbstractFileTypeDetector {

    private final boolean gioAvailable;

    public GioFileTypeDetector() {
        gioAvailable = initializeGio();
    }

    @Override
    public String implProbeContentType(Path obj) throws IOException {
        if (!gioAvailable)
            return null;
        if (!(obj instanceof UnixPath))
            return null;
        UnixPath path = (UnixPath) obj;
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path.getByteArrayForSysCalls());
        try {
            path.checkRead();
            byte[] type = probeGio(buffer.address());
            return (type == null) ? null : Util.toString(type);
        } finally {
            buffer.release();
        }
    }

    private static native boolean initializeGio();

    private static synchronized native byte[] probeGio(long pathAddress);

    static {
        AccessController.doPrivileged(new PrivilegedAction<>() {

            public Void run() {
                System.loadLibrary("nio");
                return null;
            }
        });
    }
}
