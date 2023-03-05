package org.graalvm.compiler.serviceprovider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class JDK9Method {

    private static int getJavaSpecificationVersion() {
        String value = System.getProperty("java.specification.version");
        if (value.startsWith("1.")) {
            value = value.substring(2);
        }
        return Integer.parseInt(value);
    }

    public static final int JAVA_SPECIFICATION_VERSION = getJavaSpecificationVersion();

    public JDK9Method(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
        try {
            this.method = declaringClass.getMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    public static final boolean Java8OrEarlier = JAVA_SPECIFICATION_VERSION <= 8;

    public final Method method;

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public static final JDK9Method getModule;

    public static final JDK9Method getPackages;

    public static final JDK9Method getResourceAsStream;

    public static final JDK9Method addOpens;

    public static final JDK9Method isOpenTo;

    @SuppressWarnings("unchecked")
    public <T> T invokeStatic(Object... args) {
        checkAvailability();
        assert Modifier.isStatic(method.getModifiers());
        try {
            return (T) method.invoke(null, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T invoke(Object receiver, Object... args) {
        checkAvailability();
        assert !Modifier.isStatic(method.getModifiers());
        try {
            return (T) method.invoke(receiver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError(e);
        }
    }

    private void checkAvailability() throws InternalError {
        if (method == null) {
            throw new InternalError("Cannot use Module API on JDK " + JAVA_SPECIFICATION_VERSION);
        }
    }

    static {
        if (JAVA_SPECIFICATION_VERSION >= 9) {
            getModule = new JDK9Method(Class.class, "getModule");
            Class<?> moduleClass = getModule.getReturnType();
            getPackages = new JDK9Method(moduleClass, "getPackages");
            addOpens = new JDK9Method(moduleClass, "addOpens", String.class, moduleClass);
            getResourceAsStream = new JDK9Method(moduleClass, "getResourceAsStream", String.class);
            isOpenTo = new JDK9Method(moduleClass, "isOpen", String.class, moduleClass);
        } else {
            JDK9Method unavailable = new JDK9Method();
            getModule = unavailable;
            getPackages = unavailable;
            addOpens = unavailable;
            getResourceAsStream = unavailable;
            isOpenTo = unavailable;
        }
    }

    private JDK9Method() {
        method = null;
    }
}