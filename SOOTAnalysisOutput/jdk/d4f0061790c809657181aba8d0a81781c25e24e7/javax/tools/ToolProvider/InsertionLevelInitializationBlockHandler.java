package javax.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class ToolProvider {

    private static final String systemJavaCompilerModule = "jdk.compiler";

    private static final String systemJavaCompilerName = "com.sun.tools.javac.api.JavacTool";

    public static JavaCompiler getSystemJavaCompiler() {
        return getSystemTool(JavaCompiler.class, systemJavaCompilerModule, systemJavaCompilerName);
    }

    private static final String systemDocumentationToolModule = "jdk.javadoc";

    private static final String systemDocumentationToolName = "jdk.javadoc.internal.api.JavadocTool";

    public static DocumentationTool getSystemDocumentationTool() {
        return getSystemTool(DocumentationTool.class, systemDocumentationToolModule, systemDocumentationToolName);
    }

    @Deprecated
    public static ClassLoader getSystemToolClassLoader() {
        return null;
    }

    private static final boolean useLegacy;

    static {
        Class<?> c = null;
        try {
            c = Class.forName("java.lang.Module");
        } catch (Throwable t) {
        }
        useLegacy = (c == null);
    }

    private static <T> T getSystemTool(Class<T> clazz, String moduleName, String className) {
        if (useLegacy) {
            try {
                return Class.forName(className, true, ClassLoader.getSystemClassLoader()).asSubclass(clazz).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new Error(e);
            }
        }
        try {
            ServiceLoader<T> sl = ServiceLoader.load(clazz, ClassLoader.getSystemClassLoader());
            for (Iterator<T> iter = sl.iterator(); iter.hasNext(); ) {
                T tool = iter.next();
                if (matches(tool, moduleName))
                    return tool;
            }
        } catch (ServiceConfigurationError e) {
            throw new Error(e);
        }
        return null;
    }

    private static <T> boolean matches(T tool, String moduleName) {
        PrivilegedAction<Boolean> pa = () -> {
            try {
                Method getModuleMethod = Class.class.getDeclaredMethod("getModule");
                Object toolModule = getModuleMethod.invoke(tool.getClass());
                Method getNameMethod = toolModule.getClass().getDeclaredMethod("getName");
                String toolModuleName = (String) getNameMethod.invoke(toolModule);
                return moduleName.equals(toolModuleName);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                return false;
            }
        };
        return AccessController.doPrivileged(pa);
    }
}