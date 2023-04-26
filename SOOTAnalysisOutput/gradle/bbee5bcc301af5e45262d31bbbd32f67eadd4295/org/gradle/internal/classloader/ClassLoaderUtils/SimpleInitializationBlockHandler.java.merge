package org.gradle.internal.classloader;

import org.gradle.api.JavaVersion;
import org.gradle.internal.Cast;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.concurrent.CompositeStoppable;
import org.gradle.internal.reflect.JavaMethod;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLConnection;
import static org.gradle.internal.reflect.JavaReflectionUtil.method;
import static org.gradle.internal.reflect.JavaReflectionUtil.staticMethod;

public abstract class ClassLoaderUtils {

    private static final ClassDefiner CLASS_DEFINER;

    private static final JavaMethod<ClassLoader, Package[]> GET_PACKAGES_METHOD;

    private static final JavaMethod<ClassLoader, Package> GET_PACKAGE_METHOD;

    static {
        CLASS_DEFINER = JavaVersion.current().isJava9Compatible() ? new LookupClassDefiner() : new ReflectionClassDefiner();
        GET_PACKAGES_METHOD = method(ClassLoader.class, Package[].class, "getPackages");
        GET_PACKAGE_METHOD = getMethodWithFallback(Package.class, new Class[] { String.class }, "getDefinedPackage", "getPackage");
    }

    private static <T> JavaMethod<ClassLoader, T> getMethodWithFallback(Class<T> clazz, Class<?>[] params, String firstChoice, String fallback) {
        JavaMethod<ClassLoader, T> method;
        try {
            method = method(ClassLoader.class, clazz, firstChoice, params);
        } catch (Throwable e) {
            method = method(ClassLoader.class, clazz, fallback, params);
        }
        return method;
    }

    public static ClassLoader getPlatformClassLoader() {
        return ClassLoader.getSystemClassLoader().getParent();
    }

    public static void tryClose(@Nullable ClassLoader classLoader) {
        CompositeStoppable.stoppable(classLoader).stop();
    }

    public static void disableUrlConnectionCaching() {
        try {
            URL url = new URL("jar:file://valid_jar_url_syntax.jar!/");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDefaultUseCaches(false);
        } catch (IOException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    public static JavaMethod<ClassLoader, Package[]> getPackagesMethod() {
        return GET_PACKAGES_METHOD;
    }

    public static JavaMethod<ClassLoader, Package> getPackageMethod() {
        return GET_PACKAGE_METHOD;
    }

    public static <T> Class<T> define(ClassLoader targetClassLoader, String className, byte[] clazzBytes) {
        return CLASS_DEFINER.defineClass(targetClassLoader, className, clazzBytes);
    }

    private interface ClassDefiner {

        <T> Class<T> defineClass(ClassLoader classLoader, String className, byte[] classBytes);
    }

    private static class ReflectionClassDefiner implements ClassDefiner {

        private final JavaMethod<ClassLoader, Class> defineClassMethod;

        private ReflectionClassDefiner() {
            defineClassMethod = method(ClassLoader.class, Class.class, "defineClass", String.class, byte[].class, int.class, int.class);
        }

        @Override
        public <T> Class<T> defineClass(ClassLoader classLoader, String className, byte[] classBytes) {
            return Cast.uncheckedCast(defineClassMethod.invoke(classLoader, className, classBytes, 0, classBytes.length));
        }
    }

    private static class LookupClassDefiner implements ClassDefiner {

        private final Class methodHandlesLookupClass;

        private final JavaMethod methodHandlesLookup;

        private final JavaMethod methodHandlesPrivateLookupIn;

        private final JavaMethod lookupFindVirtual;

        private final MethodType defineClassMethodType;

        private LookupClassDefiner() {
            try {
                methodHandlesLookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            methodHandlesLookup = staticMethod(MethodHandles.class, methodHandlesLookupClass, "lookup");
            methodHandlesPrivateLookupIn = staticMethod(MethodHandles.class, methodHandlesLookupClass, "privateLookupIn", Class.class, methodHandlesLookupClass);
            lookupFindVirtual = method(methodHandlesLookupClass, MethodHandle.class, "findVirtual", Class.class, String.class, MethodType.class);
            defineClassMethodType = MethodType.methodType(Class.class, new Class[] { String.class, byte[].class, int.class, int.class });
        }

        @Override
        public <T> Class<T> defineClass(ClassLoader classLoader, String className, byte[] classBytes) {
            Object baseLookup = methodHandlesLookup.invoke(null);
            Object lookup = methodHandlesPrivateLookupIn.invoke(null, ClassLoader.class, baseLookup);
            MethodHandle defineClassMethodHandle = (MethodHandle) lookupFindVirtual.invoke(lookup, ClassLoader.class, "defineClass", defineClassMethodType);
            try {
                return Cast.uncheckedCast(defineClassMethodHandle.bindTo(classLoader).invokeWithArguments(className, classBytes, 0, classBytes.length));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}