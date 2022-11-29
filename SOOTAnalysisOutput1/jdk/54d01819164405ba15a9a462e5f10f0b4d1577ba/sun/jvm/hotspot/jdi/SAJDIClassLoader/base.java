package sun.jvm.hotspot.jdi;

import java.io.*;
import java.net.*;

class SAJDIClassLoader extends URLClassLoader {

    private static final boolean DEBUG;

    static {
        DEBUG = System.getProperty("sun.jvm.hotspot.jdi.SAJDIClassLoader.DEBUG") != null;
    }

    private ClassLoader parent;

    private boolean classPathSet;

    SAJDIClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.parent = parent;
    }

    SAJDIClassLoader(ClassLoader parent, String classPath) {
        this(parent);
        this.classPathSet = true;
        try {
            addURL(new File(classPath).toURI().toURL());
        } catch (MalformedURLException mue) {
            throw new RuntimeException(mue);
        }
    }

    public synchronized Class loadClass(String name) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if (c == null) {
            if (name.startsWith("sun.jvm.hotspot.") && !name.startsWith("sun.jvm.hotspot.debugger.")) {
                return findClass(name);
            }
            if (parent != null) {
                c = parent.loadClass(name);
            } else {
                c = findSystemClass(name);
            }
        }
        return c;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        if (DEBUG) {
            System.out.println("SA/JDI loader: about to load " + name);
        }
        if (classPathSet) {
            return super.findClass(name);
        } else {
            byte[] b = null;
            try {
                InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
                b = new byte[1024];
                int total = 0;
                int len = 0;
                while ((len = in.read(b, total, b.length - total)) != -1) {
                    total += len;
                    if (total >= b.length) {
                        byte[] tmp = new byte[total * 2];
                        System.arraycopy(b, 0, tmp, 0, total);
                        b = tmp;
                    }
                }
                if (total != b.length) {
                    byte[] tmp = new byte[total];
                    System.arraycopy(b, 0, tmp, 0, total);
                    b = tmp;
                }
            } catch (Exception exp) {
                throw (ClassNotFoundException) new ClassNotFoundException().initCause(exp);
            }
            return defineClass(name, b, 0, b.length);
        }
    }
}
