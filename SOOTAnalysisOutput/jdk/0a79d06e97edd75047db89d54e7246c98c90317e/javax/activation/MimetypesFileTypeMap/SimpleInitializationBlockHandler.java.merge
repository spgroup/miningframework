package javax.activation;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import com.sun.activation.registries.MimeTypeFile;
import com.sun.activation.registries.LogSupport;

public class MimetypesFileTypeMap extends FileTypeMap {

    private MimeTypeFile[] DB;

    private static final int PROG = 0;

    private static final String defaultType = "application/octet-stream";

    private static final String confDir;

    static {
        String dir = null;
        try {
            dir = (String) AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    String home = System.getProperty("java.home");
                    String newdir = home + File.separator + "conf";
                    File conf = new File(newdir);
                    if (conf.exists())
                        return newdir + File.separator;
                    else
                        return home + File.separator + "lib" + File.separator;
                }
            });
        } catch (Exception ex) {
        }
        confDir = dir;
    }

    public MimetypesFileTypeMap() {
        Vector dbv = new Vector(5);
        MimeTypeFile mf = null;
        dbv.addElement(null);
        LogSupport.log("MimetypesFileTypeMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (user_home != null) {
                String path = user_home + File.separator + ".mime.types";
                mf = loadFile(path);
                if (mf != null)
                    dbv.addElement(mf);
            }
        } catch (SecurityException ex) {
        }
        LogSupport.log("MimetypesFileTypeMap: load SYS");
        try {
            if (confDir != null) {
                mf = loadFile(confDir + "mime.types");
                if (mf != null)
                    dbv.addElement(mf);
            }
        } catch (SecurityException ex) {
        }
        LogSupport.log("MimetypesFileTypeMap: load JAR");
        loadAllResources(dbv, "META-INF/mime.types");
        LogSupport.log("MimetypesFileTypeMap: load DEF");
        mf = loadResource("/META-INF/mimetypes.default");
        if (mf != null)
            dbv.addElement(mf);
        DB = new MimeTypeFile[dbv.size()];
        dbv.copyInto(DB);
    }

    private MimeTypeFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(this.getClass(), name);
            if (clis != null) {
                MimeTypeFile mf = new MimeTypeFile(clis);
                if (LogSupport.isLoggable())
                    LogSupport.log("MimetypesFileTypeMap: successfully " + "loaded mime types file: " + name);
                return mf;
            } else {
                if (LogSupport.isLoggable())
                    LogSupport.log("MimetypesFileTypeMap: not loading " + "mime types file: " + name);
            }
        } catch (IOException e) {
            if (LogSupport.isLoggable())
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, e);
        } catch (SecurityException sex) {
            if (LogSupport.isLoggable())
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, sex);
        } finally {
            try {
                if (clis != null)
                    clis.close();
            } catch (IOException ex) {
            }
        }
        return null;
    }

    private void loadAllResources(Vector v, String name) {
        boolean anyLoaded = false;
        try {
            URL[] urls;
            ClassLoader cld = null;
            cld = SecuritySupport.getContextClassLoader();
            if (cld == null)
                cld = this.getClass().getClassLoader();
            if (cld != null)
                urls = SecuritySupport.getResources(cld, name);
            else
                urls = SecuritySupport.getSystemResources(name);
            if (urls != null) {
                if (LogSupport.isLoggable())
                    LogSupport.log("MimetypesFileTypeMap: getResources");
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    InputStream clis = null;
                    if (LogSupport.isLoggable())
                        LogSupport.log("MimetypesFileTypeMap: URL " + url);
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.addElement(new MimeTypeFile(clis));
                            anyLoaded = true;
                            if (LogSupport.isLoggable())
                                LogSupport.log("MimetypesFileTypeMap: " + "successfully loaded " + "mime types from URL: " + url);
                        } else {
                            if (LogSupport.isLoggable())
                                LogSupport.log("MimetypesFileTypeMap: " + "not loading " + "mime types from URL: " + url);
                        }
                    } catch (IOException ioex) {
                        if (LogSupport.isLoggable())
                            LogSupport.log("MimetypesFileTypeMap: can't load " + url, ioex);
                    } catch (SecurityException sex) {
                        if (LogSupport.isLoggable())
                            LogSupport.log("MimetypesFileTypeMap: can't load " + url, sex);
                    } finally {
                        try {
                            if (clis != null)
                                clis.close();
                        } catch (IOException cex) {
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (LogSupport.isLoggable())
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, ex);
        }
        if (!anyLoaded) {
            LogSupport.log("MimetypesFileTypeMap: !anyLoaded");
            MimeTypeFile mf = loadResource("/" + name);
            if (mf != null)
                v.addElement(mf);
        }
    }

    private MimeTypeFile loadFile(String name) {
        MimeTypeFile mtf = null;
        try {
            mtf = new MimeTypeFile(name);
        } catch (IOException e) {
        }
        return mtf;
    }

    public MimetypesFileTypeMap(String mimeTypeFileName) throws IOException {
        this();
        DB[PROG] = new MimeTypeFile(mimeTypeFileName);
    }

    public MimetypesFileTypeMap(InputStream is) {
        this();
        try {
            DB[PROG] = new MimeTypeFile(is);
        } catch (IOException ex) {
        }
    }

    public synchronized void addMimeTypes(String mime_types) {
        if (DB[PROG] == null)
            DB[PROG] = new MimeTypeFile();
        DB[PROG].appendToRegistry(mime_types);
    }

    public String getContentType(File f) {
        return this.getContentType(f.getName());
    }

    public synchronized String getContentType(String filename) {
        int dot_pos = filename.lastIndexOf(".");
        if (dot_pos < 0)
            return defaultType;
        String file_ext = filename.substring(dot_pos + 1);
        if (file_ext.length() == 0)
            return defaultType;
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            String result = DB[i].getMIMETypeString(file_ext);
            if (result != null)
                return result;
        }
        return defaultType;
    }
}