package org.graalvm.compiler.core.common.util;

import static org.graalvm.compiler.core.common.util.Util.JAVA_SPECIFICATION_VERSION;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ModuleAPI {

    private ModuleAPI(Method method) {
        this.method = method;
    }

    private final Method method;

    public static final ModuleAPI getModule;

    public static final ModuleAPI getResourceAsStream;

    public static final ModuleAPI canRead;

    public static final ModuleAPI isExported;

    public static final ModuleAPI isExportedTo;

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
            try {
                getModule = new ModuleAPI(Class.class.getMethod("getModule"));
                Class<?> moduleClass = getModule.method.getReturnType();
                getResourceAsStream = new ModuleAPI(moduleClass.getMethod("getResourceAsStream", String.class));
                canRead = new ModuleAPI(moduleClass.getMethod("canRead", moduleClass));
                isExported = new ModuleAPI(moduleClass.getMethod("isExported", String.class));
                isExportedTo = new ModuleAPI(moduleClass.getMethod("isExported", String.class, moduleClass));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new InternalError(e);
            }
        } else {
            ModuleAPI unavailable = new ModuleAPI(null);
            getModule = unavailable;
            getResourceAsStream = unavailable;
            canRead = unavailable;
            isExported = unavailable;
            isExportedTo = unavailable;
        }
    }
}
