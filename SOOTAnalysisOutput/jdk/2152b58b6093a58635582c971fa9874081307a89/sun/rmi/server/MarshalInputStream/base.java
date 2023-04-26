package sun.rmi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.*;
import java.security.AccessControlException;
import java.security.Permission;
import java.rmi.server.RMIClassLoader;

public class MarshalInputStream extends ObjectInputStream {

    private static final boolean useCodebaseOnlyProperty = java.security.AccessController.doPrivileged(new sun.security.action.GetBooleanAction("java.rmi.server.useCodebaseOnly")).booleanValue();

    protected static Map<String, Class<?>> permittedSunClasses = new HashMap<String, Class<?>>(3);

    private boolean skipDefaultResolveClass = false;

    private final Map<Object, Runnable> doneCallbacks = new HashMap<Object, Runnable>(3);

    private boolean useCodebaseOnly = useCodebaseOnlyProperty;

    static {
        try {
            String system = "sun.rmi.server.Activation$ActivationSystemImpl_Stub";
            String registry = "sun.rmi.registry.RegistryImpl_Stub";
            permittedSunClasses.put(system, Class.forName(system));
            permittedSunClasses.put(registry, Class.forName(registry));
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("Missing system class: " + e.getMessage());
        }
    }

    static {
        java.security.AccessController.doPrivileged(new sun.security.action.LoadLibraryAction("rmi"));
    }

    public MarshalInputStream(InputStream in) throws IOException, StreamCorruptedException {
        super(in);
    }

    public Runnable getDoneCallback(Object key) {
        return doneCallbacks.get(key);
    }

    public void setDoneCallback(Object key, Runnable callback) {
        doneCallbacks.put(key, callback);
    }

    public void done() {
        Iterator<Runnable> iter = doneCallbacks.values().iterator();
        while (iter.hasNext()) {
            Runnable callback = iter.next();
            callback.run();
        }
        doneCallbacks.clear();
    }

    public void close() throws IOException {
        done();
        super.close();
    }

    protected Class resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        Object annotation = readLocation();
        String className = classDesc.getName();
        ClassLoader defaultLoader = skipDefaultResolveClass ? null : latestUserDefinedLoader();
        String codebase = null;
        if (!useCodebaseOnly && annotation instanceof String) {
            codebase = (String) annotation;
        }
        try {
            return RMIClassLoader.loadClass(codebase, className, defaultLoader);
        } catch (AccessControlException e) {
            return checkSunClass(className, e);
        } catch (ClassNotFoundException e) {
            try {
                if (Character.isLowerCase(className.charAt(0)) && className.indexOf('.') == -1) {
                    return super.resolveClass(classDesc);
                }
            } catch (ClassNotFoundException e2) {
            }
            throw e;
        }
    }

    protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        Object annotation = readLocation();
        ClassLoader defaultLoader = skipDefaultResolveClass ? null : latestUserDefinedLoader();
        String codebase = null;
        if (!useCodebaseOnly && annotation instanceof String) {
            codebase = (String) annotation;
        }
        return RMIClassLoader.loadProxyClass(codebase, interfaces, defaultLoader);
    }

    private static native ClassLoader latestUserDefinedLoader();

    private Class checkSunClass(String className, AccessControlException e) throws AccessControlException {
        Permission perm = e.getPermission();
        String name = null;
        if (perm != null) {
            name = perm.getName();
        }
        Class<?> resolvedClass = permittedSunClasses.get(className);
        if ((name == null) || (resolvedClass == null) || ((!name.equals("accessClassInPackage.sun.rmi.server")) && (!name.equals("accessClassInPackage.sun.rmi.registry")))) {
            throw e;
        }
        return resolvedClass;
    }

    protected Object readLocation() throws IOException, ClassNotFoundException {
        return readObject();
    }

    void skipDefaultResolveClass() {
        skipDefaultResolveClass = true;
    }

    void useCodebaseOnly() {
        useCodebaseOnly = true;
    }
}
