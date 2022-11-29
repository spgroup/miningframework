package sun.nio.fs;

import java.io.IOException;
import java.nio.file.Path;

class UTIFileTypeDetector extends AbstractFileTypeDetector {

    UTIFileTypeDetector() {
        super();
    }

    private native String probe0(String fileExtension) throws IOException;

    @Override
    protected String implProbeContentType(Path path) throws IOException {
        Path fn = path.getFileName();
        if (fn == null)
            return null;
        String ext = getExtension(fn.toString());
        if (ext.isEmpty())
            return null;
        return probe0(ext);
    }

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
    }
}