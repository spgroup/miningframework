package jdk.vm.ci.services;

import java.lang.reflect.Module;
import java.util.Formatter;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class Services {

    private Services() {
    }

    public static void exportJVMCITo(Class<?> requestor) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        Module jvmci = Services.class.getModule();
        Module requestorModule = requestor.getModule();
        if (jvmci != requestorModule) {
            for (String pkg : jvmci.getPackages()) {
                if (!jvmci.isExported(pkg, requestorModule)) {
                    jvmci.addExports(pkg, requestorModule);
                }
            }
        }
    }

    public static <S> Iterable<S> load(Class<S> service) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        Module jvmci = Services.class.getModule();
        jvmci.addUses(service);
        return ServiceLoader.load(service, ClassLoader.getSystemClassLoader());
    }

    public static <S> S loadSingle(Class<S> service, boolean required) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        Module jvmci = Services.class.getModule();
        jvmci.addUses(service);
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
