package jdk.vm.ci.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class Services {

    private Services() {
    }

    private static int getJavaSpecificationVersion() {
        String value = System.getProperty("java.specification.version");
        if (value.startsWith("1.")) {
            value = value.substring(2);
        }
        return Integer.parseInt(value);
    }

    public static final int JAVA_SPECIFICATION_VERSION = getJavaSpecificationVersion();

    private static final Method getModule;

    private static final Method getPackages;

    private static final Method addUses;

    private static final Method isExported;

    private static final Method addExports;

    static {
        if (JAVA_SPECIFICATION_VERSION >= 9) {
            try {
                getModule = Class.class.getMethod("getModule");
                Class<?> moduleClass = getModule.getReturnType();
                getPackages = moduleClass.getMethod("getPackages");
                addUses = moduleClass.getMethod("addUses", Class.class);
                isExported = moduleClass.getMethod("isExported", String.class, moduleClass);
                addExports = moduleClass.getMethod("addExports", String.class, moduleClass);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new InternalError(e);
            }
        } else {
            getModule = null;
            getPackages = null;
            addUses = null;
            isExported = null;
            addExports = null;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T invoke(Method method, Object receiver, Object... args) {
        try {
            return (T) method.invoke(receiver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError(e);
        }
    }

    public static void exportJVMCITo(Class<?> requestor) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        if (JAVA_SPECIFICATION_VERSION >= 9) {
            Object jvmci = invoke(getModule, Services.class);
            Object requestorModule = invoke(getModule, requestor);
            if (jvmci != requestorModule) {
                String[] packages = invoke(getPackages, jvmci);
                for (String pkg : packages) {
                    boolean exported = invoke(isExported, jvmci, pkg, requestorModule);
                    if (!exported) {
                        invoke(addExports, jvmci, pkg, requestorModule);
                    }
                }
            }
        }
    }

    public static <S> Iterable<S> load(Class<S> service) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        if (JAVA_SPECIFICATION_VERSION >= 9) {
            Object jvmci = invoke(getModule, Services.class);
            invoke(addUses, jvmci, service);
        }
        return ServiceLoader.load(service, ClassLoader.getSystemClassLoader());
    }

    public static <S> S loadSingle(Class<S> service, boolean required) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        if (JAVA_SPECIFICATION_VERSION >= 9) {
            Object jvmci = invoke(getModule, Services.class);
            invoke(addUses, jvmci, service);
        }
        Iterable<S> providers = ServiceLoader.load(service, ClassLoader.getSystemClassLoader());
        S singleProvider = null;
        try {
            for (Iterator<S> it = providers.iterator(); it.hasNext(); ) {
                singleProvider = it.next();
                if (it.hasNext()) {
                    throw new InternalError(String.format("Multiple %s providers found", service.getName()));
                }
            }
        } catch (ServiceConfigurationError e) {
        }
        if (singleProvider == null && required) {
            String javaHome = System.getProperty("java.home");
            String vmName = System.getProperty("java.vm.name");
            Formatter errorMessage = new Formatter();
            errorMessage.format("The VM does not expose required service %s.%n", service.getName());
            errorMessage.format("Currently used Java home directory is %s.%n", javaHome);
            errorMessage.format("Currently used VM configuration is: %s", vmName);
            throw new UnsupportedOperationException(errorMessage.toString());
        }
        return singleProvider;
    }
}
