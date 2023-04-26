package org.gradle.internal.classloader;

import org.gradle.api.JavaVersion;
import org.gradle.internal.Cast;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.concurrent.CompositeStoppable;
import org.gradle.internal.reflect.JavaMethod;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import static org.gradle.internal.reflect.JavaReflectionUtil.method;

public abstract class ClassLoaderUtils {

    private static final ClassDefiner CLASS_DEFINER;

    private static final ClassLoaderPackagesFetcher CLASS_LOADER_PACKAGES_FETCHER;

    static {
        CLASS_DEFINER = JavaVersion.current().isJava9Compatible() ? new LookupClassDefiner() : new ReflectionClassDefiner();
        CLASS_LOADER_PACKAGES_FETCHER = JavaVersion.current().isJava9Compatible() ? new LookupPackagesFetcher() : new ReflectionPackagesFetcher();
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

    static Package[] getPackages(ClassLoader classLoader) {
        return CLASS_LOADER_PACKAGES_FETCHER.getPackages(classLoader);
    }

    static Package getPackage(ClassLoader classLoader, String name) {
        return CLASS_LOADER_PACKAGES_FETCHER.getPackage(classLoader, name);
    }

    public static <T> Class<T> define(ClassLoader targetClassLoader, String className, byte[] clazzBytes) {
        return CLASS_DEFINER.defineClass(targetClassLoader, className, clazzBytes);
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

    private static class ReflectionPackagesFetcher implements ClassLoaderPackagesFetcher {

        private static final JavaMethod<ClassLoader, Package[]> GET_PACKAGES_METHOD = method(ClassLoader.class, Package[].class, "getPackages");

        private static final JavaMethod<ClassLoader, Package> GET_PACKAGE_METHOD = method(ClassLoader.class, Package.class, "getPackage", String.class);

        @Override
        public Package[] getPackages(ClassLoader classLoader) {
            return GET_PACKAGES_METHOD.invoke(classLoader);
        }

        @Override
        public Package getPackage(ClassLoader classLoader, String name) {
            return GET_PACKAGE_METHOD.invoke(classLoader, name);
        }
    }
}