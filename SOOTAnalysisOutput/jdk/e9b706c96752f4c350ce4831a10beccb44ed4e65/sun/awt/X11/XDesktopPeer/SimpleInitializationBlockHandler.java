package sun.awt.X11;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.awt.Desktop.Action;
import java.awt.peer.DesktopPeer;

public class XDesktopPeer implements DesktopPeer {

    private static boolean nativeLibraryLoaded = false;

    private static boolean initExecuted = false;

    private static void initWithLock() {
        XToolkit.awtLock();
        try {
            if (!initExecuted) {
                nativeLibraryLoaded = init();
            }
        } finally {
            initExecuted = true;
            XToolkit.awtUnlock();
        }
    }

    XDesktopPeer() {
        initWithLock();
    }

    static boolean isDesktopSupported() {
        initWithLock();
        return nativeLibraryLoaded;
    }

    public boolean isSupported(Action type) {
        return type != Action.PRINT && type != Action.EDIT;
    }

    public void open(File file) throws IOException {
        try {
            launch(file.toURI());
        } catch (MalformedURLException e) {
            throw new IOException(file.toString());
        }
    }

    public void edit(File file) throws IOException {
        throw new UnsupportedOperationException("The current platform " + "doesn't support the EDIT action.");
    }

    public void print(File file) throws IOException {
        throw new UnsupportedOperationException("The current platform " + "doesn't support the PRINT action.");
    }

    public void mail(URI uri) throws IOException {
        launch(uri);
    }

    public void browse(URI uri) throws IOException {
        launch(uri);
    }

    private void launch(URI uri) throws IOException {
        byte[] uriByteArray = (uri.toString() + '\0').getBytes();
        boolean result = false;
        XToolkit.awtLock();
        try {
            if (!nativeLibraryLoaded) {
                throw new IOException("Failed to load native libraries.");
            }
            result = gnome_url_show(uriByteArray);
        } finally {
            XToolkit.awtUnlock();
        }
        if (!result) {
            throw new IOException("Failed to show URI:" + uri);
        }
    }

    private native boolean gnome_url_show(byte[] url);

    private static native boolean init();
}