package com.sun.xml.internal.ws.model;

import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Injector {

    private static final Logger LOGGER = Logger.getLogger(Injector.class.getName());

    private static final Method defineClass;

    private static final Method resolveClass;

    private static final Method getPackage;

    private static final Method definePackage;

    static {
        Method[] m = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return new Method[] { getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE), getMethod(ClassLoader.class, "resolveClass", Class.class), getMethod(ClassLoader.class, "getPackage", String.class), getMethod(ClassLoader.class, "definePackage", String.class, String.class, String.class, String.class, String.class, String.class, String.class, URL.class) };
            }
        });
        defineClass = m[0];
        resolveClass = m[1];
        getPackage = m[2];
        definePackage = m[3];
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

    static synchronized Class inject(ClassLoader cl, String className, byte[] image) {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
        }
        try {
            int packIndex = className.lastIndexOf('.');
            if (packIndex != -1) {
                String pkgname = className.substring(0, packIndex);
                Package pkg = (Package) getPackage.invoke(cl, pkgname);
                if (pkg == null) {
                    definePackage.invoke(cl, pkgname, null, null, null, null, null, null, null);
                }
            }
            Class c = (Class) defineClass.invoke(cl, className.replace('/', '.'), image, 0, image.length);
            resolveClass.invoke(cl, c);
            return c;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.FINE, "Unable to inject " + className, e);
            throw new WebServiceException(e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.FINE, "Unable to inject " + className, e);
            throw new WebServiceException(e);
        }
    }
}
