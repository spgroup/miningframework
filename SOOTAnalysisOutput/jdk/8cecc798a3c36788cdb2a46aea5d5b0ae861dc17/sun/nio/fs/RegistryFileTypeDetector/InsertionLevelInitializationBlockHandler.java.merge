package sun.nio.fs;

import java.nio.file.*;
import java.io.IOException;

public class RegistryFileTypeDetector extends AbstractFileTypeDetector {

    public RegistryFileTypeDetector() {
        super();
    }

    @Override
    public String implProbeContentType(Path file) throws IOException {
        if (!(file instanceof Path))
            return null;
        Path name = file.getFileName();
        if (name == null)
            return null;
        String filename = name.toString();
        int dot = filename.lastIndexOf('.');
        if ((dot < 0) || (dot == (filename.length() - 1)))
            return null;
        String key = filename.substring(dot);
        NativeBuffer keyBuffer = WindowsNativeDispatcher.asNativeBuffer(key);
        NativeBuffer nameBuffer = WindowsNativeDispatcher.asNativeBuffer("Content Type");
        try {
            return queryStringValue(keyBuffer.address(), nameBuffer.address());
        } finally {
            nameBuffer.release();
            keyBuffer.release();
        }
    }

    private static native String queryStringValue(long subKey, long name);

    static {
        jdk.internal.loader.BootLoader.loadLibrary("net");
        jdk.internal.loader.BootLoader.loadLibrary("nio");
    }
}