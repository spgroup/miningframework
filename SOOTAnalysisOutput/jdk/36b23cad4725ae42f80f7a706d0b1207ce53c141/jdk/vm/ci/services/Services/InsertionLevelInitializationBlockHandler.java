package jdk.vm.ci.services;

import java.lang.reflect.Method;
import java.util.Map;

public final class Services {

    private Services() {
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> initSavedProperties() throws InternalError {
        try {
            Class<?> vmClass = Class.forName("jdk.internal.misc.VM");
            Method m = vmClass.getMethod("getSavedProperties");
            return (Map<String, String>) m.invoke(null);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }

    static final Map<String, String> SAVED_PROPERTIES = initSavedProperties();

    static final boolean JVMCI_ENABLED = Boolean.parseBoolean(SAVED_PROPERTIES.get("jdk.internal.vm.ci.enabled"));

    static void checkJVMCIEnabled() {
        if (!JVMCI_ENABLED) {
            throw new Error("The EnableJVMCI VM option must be true (i.e., -XX:+EnableJVMCI) to use JVMCI");
        }
    }

    public static Map<String, String> getSavedProperties() {
        checkJVMCIEnabled();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        return SAVED_PROPERTIES;
    }

    public static void initializeJVMCI() {
        checkJVMCIEnabled();
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI");
        } catch (ClassNotFoundException e) {
            throw new InternalError(e);
        }
    }
}