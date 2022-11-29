package jdk.vm.ci.services.internal;

import java.lang.reflect.Method;
import java.util.Set;
import jdk.vm.ci.services.Services;

public final class ReflectionAccessJDK {

    private static final Method getModule;

    private static final Method addOpens;

    private static final Method getPackages;

    private static final Method isOpenTo;

    @SuppressWarnings("unchecked")
    public static void openJVMCITo(Class<?> other) {
        try {
            Object jvmci = getModule.invoke(Services.class);
            Object otherModule = getModule.invoke(other);
            if (jvmci != otherModule) {
                Set<String> packages = (Set<String>) getPackages.invoke(jvmci);
                for (String pkg : packages) {
                    boolean opened = (Boolean) isOpenTo.invoke(jvmci, pkg, otherModule);
                    if (!opened) {
                        addOpens.invoke(jvmci, pkg, otherModule);
                    }
                }
            }
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    static {
        try {
            getModule = Class.class.getMethod("getModule");
            Class<?> moduleClass = getModule.getReturnType();
            getPackages = moduleClass.getMethod("getPackages");
            isOpenTo = moduleClass.getMethod("isOpen", String.class, moduleClass);
            addOpens = moduleClass.getDeclaredMethod("addOpens", String.class, moduleClass);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError(e);
        }
    }
}