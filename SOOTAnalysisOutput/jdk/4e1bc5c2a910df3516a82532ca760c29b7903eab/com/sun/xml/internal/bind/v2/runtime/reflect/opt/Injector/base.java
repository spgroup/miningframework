package com.sun.xml.internal.bind.v2.runtime.reflect.opt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.internal.bind.Util;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;

final class Injector {

    private static final ReentrantReadWriteLock irwl = new ReentrantReadWriteLock();

    private static final Lock ir = irwl.readLock();

    private static final Lock iw = irwl.writeLock();

    private static final Map<ClassLoader, WeakReference<Injector>> injectors = new WeakHashMap<ClassLoader, WeakReference<Injector>>();

    private static final Logger logger = Util.getClassLogger();

    static Class inject(ClassLoader cl, String className, byte[] image) {
        Injector injector = get(cl);
        if (injector != null) {
            return injector.inject(className, image);
        } else {
            return null;
        }
    }

    static Class find(ClassLoader cl, String className) {
        Injector injector = get(cl);
        if (injector != null) {
            return injector.find(className);
        } else {
            return null;
        }
    }

    private static Injector get(ClassLoader cl) {
        Injector injector = null;
        WeakReference<Injector> wr;
        ir.lock();
        try {
            wr = injectors.get(cl);
        } finally {
            ir.unlock();
        }
        if (wr != null) {
            injector = wr.get();
        }
        if (injector == null) {
            try {
                wr = new WeakReference<Injector>(injector = new Injector(cl));
                iw.lock();
                try {
                    if (!injectors.containsKey(cl)) {
                        injectors.put(cl, wr);
                    }
                } finally {
                    iw.unlock();
                }
            } catch (SecurityException e) {
                logger.log(Level.FINE, "Unable to set up a back-door for the injector", e);
                return null;
            }
        }
        return injector;
    }

    private final Map<String, Class> classes = new HashMap<String, Class>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private final Lock r = rwl.readLock();

    private final Lock w = rwl.writeLock();

    private final ClassLoader parent;

    private final boolean loadable;

    private static final Method defineClass;

    private static final Method resolveClass;

    private static final Method findLoadedClass;

    static {
        Method[] m = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return new Method[] { getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE), getMethod(ClassLoader.class, "resolveClass", Class.class), getMethod(ClassLoader.class, "findLoadedClass", String.class) };
            }
        });
        defineClass = m[0];
        resolveClass = m[1];
        findLoadedClass = m[2];
    }

    private static Method getMethod(final Class<?> c, final String methodname, final Class<?>... params) {
        try {
            Method m = c.getDeclaredMethod(methodname, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    private Injector(ClassLoader parent) {
        this.parent = parent;
        assert parent != null;
        boolean loadableCheck = false;
        try {
            loadableCheck = parent.loadClass(Accessor.class.getName()) == Accessor.class;
        } catch (ClassNotFoundException e) {
        }
        this.loadable = loadableCheck;
    }

    @SuppressWarnings("LockAcquiredButNotSafelyReleased")
    private Class inject(String className, byte[] image) {
        if (!loadable) {
            return null;
        }
        boolean wlocked = false;
        boolean rlocked = false;
        try {
            r.lock();
            rlocked = true;
            Class c = classes.get(className);
            r.unlock();
            rlocked = false;
            if (c == null) {
                try {
                    c = (Class) findLoadedClass.invoke(parent, className.replace('/', '.'));
                } catch (IllegalArgumentException e) {
                    logger.log(Level.FINE, "Unable to find " + className, e);
                } catch (IllegalAccessException e) {
                    logger.log(Level.FINE, "Unable to find " + className, e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    logger.log(Level.FINE, "Unable to find " + className, t);
                }
                if (c != null) {
                    w.lock();
                    wlocked = true;
                    classes.put(className, c);
                    w.unlock();
                    wlocked = false;
                    return c;
                }
            }
            if (c == null) {
                r.lock();
                rlocked = true;
                c = classes.get(className);
                r.unlock();
                rlocked = false;
                if (c == null) {
                    try {
                        c = (Class) defineClass.invoke(parent, className.replace('/', '.'), image, 0, image.length);
                        resolveClass.invoke(parent, c);
                    } catch (IllegalAccessException e) {
                        logger.log(Level.FINE, "Unable to inject " + className, e);
                        return null;
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getTargetException();
                        if (t instanceof LinkageError) {
                            logger.log(Level.FINE, "duplicate class definition bug occured? Please report this : " + className, t);
                        } else {
                            logger.log(Level.FINE, "Unable to inject " + className, t);
                        }
                        return null;
                    } catch (SecurityException e) {
                        logger.log(Level.FINE, "Unable to inject " + className, e);
                        return null;
                    } catch (LinkageError e) {
                        logger.log(Level.FINE, "Unable to inject " + className, e);
                        return null;
                    }
                    w.lock();
                    wlocked = true;
                    if (!classes.containsKey(className)) {
                        classes.put(className, c);
                    }
                    w.unlock();
                    wlocked = false;
                }
            }
            return c;
        } finally {
            if (rlocked) {
                r.unlock();
            }
            if (wlocked) {
                w.unlock();
            }
        }
    }

    private Class find(String className) {
        r.lock();
        try {
            return classes.get(className);
        } finally {
            r.unlock();
        }
    }
}
