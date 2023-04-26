package sun.net.www.protocol.jrt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import jdk.internal.jimage.ImageLocation;
import jdk.internal.jimage.ImageReader;
import jdk.internal.jimage.ImageReaderFactory;
import jdk.internal.loader.URLClassPath;
import jdk.internal.loader.Resource;
import sun.net.www.ParseUtil;
import sun.net.www.URLConnection;
import sun.security.action.GetPropertyAction;

public class JavaRuntimeURLConnection extends URLConnection {

    private static final ImageReader reader;

    static {
        PrivilegedAction<ImageReader> pa = ImageReaderFactory::getImageReader;
        reader = AccessController.doPrivileged(pa);
    }

    private final String module;

    private final String name;

    private volatile Resource resource;

    private static volatile Permission permission;

    JavaRuntimeURLConnection(URL url) throws IOException {
        super(url);
        String path = url.getPath();
        if (path.length() == 0 || path.charAt(0) != '/')
            throw new MalformedURLException(url + " missing path or /");
        if (path.length() == 1) {
            this.module = null;
            this.name = null;
        } else {
            int pos = path.indexOf('/', 1);
            if (pos == -1) {
                this.module = path.substring(1);
                this.name = null;
            } else {
                this.module = path.substring(1, pos);
                this.name = ParseUtil.decode(path.substring(pos + 1));
            }
        }
    }

    private static Resource findResource(String module, String name) {
        if (reader != null) {
            URL url = toJrtURL(module, name);
            ImageLocation location = reader.findLocation(module, name);
            if (location != null && URLClassPath.checkURL(url) != null) {
                return new Resource() {

                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public URL getURL() {
                        return url;
                    }

                    @Override
                    public URL getCodeSourceURL() {
                        return toJrtURL(module);
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        byte[] resource = reader.getResource(location);
                        return new ByteArrayInputStream(resource);
                    }

                    @Override
                    public int getContentLength() {
                        long size = location.getUncompressedSize();
                        return (size > Integer.MAX_VALUE) ? -1 : (int) size;
                    }
                };
            }
        }
        return null;
    }

    @Override
    public synchronized void connect() throws IOException {
        if (!connected) {
            if (name == null) {
                String s = (module == null) ? "" : module;
                throw new IOException("cannot connect to jrt:/" + s);
            }
            resource = findResource(module, name);
            if (resource == null)
                throw new IOException(module + "/" + name + " not found");
            connected = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();
        return resource.getInputStream();
    }

    @Override
    public long getContentLengthLong() {
        try {
            connect();
            return resource.getContentLength();
        } catch (IOException ioe) {
            return -1L;
        }
    }

    @Override
    public int getContentLength() {
        long len = getContentLengthLong();
        return len > Integer.MAX_VALUE ? -1 : (int) len;
    }

    @Override
    public Permission getPermission() throws IOException {
        Permission p = permission;
        if (p == null) {
            String home = GetPropertyAction.privilegedGetProperty("java.home");
            p = new FilePermission(home + File.separator + "-", "read");
            permission = p;
        }
        return p;
    }

    private static URL toJrtURL(String module, String name) {
        try {
            return new URL("jrt:/" + module + "/" + name);
        } catch (MalformedURLException e) {
            throw new InternalError(e);
        }
    }

    private static URL toJrtURL(String module) {
        try {
            return new URL("jrt:/" + module);
        } catch (MalformedURLException e) {
            throw new InternalError(e);
        }
    }
}