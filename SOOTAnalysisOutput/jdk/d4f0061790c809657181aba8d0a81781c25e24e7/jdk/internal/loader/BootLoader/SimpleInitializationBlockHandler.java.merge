package jdk.internal.loader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.module.ServicesCatalog;

public class BootLoader {

    private BootLoader() {
    }

    private static final Module UNNAMED_MODULE;

    private static final String JAVA_HOME = System.getProperty("java.home");

    static {
        UNNAMED_MODULE = SharedSecrets.getJavaLangAccess().defineUnnamedModule(null);
        setBootLoaderUnnamedModule0(UNNAMED_MODULE);
    }

    private static final ServicesCatalog SERVICES_CATALOG = ServicesCatalog.create();

    private static final ConcurrentHashMap<?, ?> CLASS_LOADER_VALUE_MAP = new ConcurrentHashMap<>();

    public static Module getUnnamedModule() {
        return UNNAMED_MODULE;
    }

    public static ServicesCatalog getServicesCatalog() {
        return SERVICES_CATALOG;
    }

    public static ConcurrentHashMap<?, ?> getClassLoaderValueMap() {
        return CLASS_LOADER_VALUE_MAP;
    }

    public static boolean hasClassPath() {
        return ClassLoaders.bootLoader().hasClassPath();
    }

    public static void loadModule(ModuleReference mref) {
        ClassLoaders.bootLoader().loadModule(mref);
    }

    public static Class<?> loadClassOrNull(String name) {
        return ClassLoaders.bootLoader().loadClassOrNull(name);
    }

    public static Class<?> loadClass(Module module, String name) {
        Class<?> c = loadClassOrNull(name);
        if (c != null && c.getModule() == module) {
            return c;
        } else {
            return null;
        }
    }

    public static URL findResource(String mn, String name) throws IOException {
        return ClassLoaders.bootLoader().findResource(mn, name);
    }

    public static InputStream findResourceAsStream(String mn, String name) throws IOException {
        return ClassLoaders.bootLoader().findResourceAsStream(mn, name);
    }

    public static URL findResource(String name) {
        return ClassLoaders.bootLoader().findResource(name);
    }

    public static Enumeration<URL> findResources(String name) throws IOException {
        return ClassLoaders.bootLoader().findResources(name);
    }

    public static Package definePackage(Class<?> c) {
        return getDefinedPackage(c.getPackageName());
    }

    public static Package getDefinedPackage(String pn) {
        Package pkg = ClassLoaders.bootLoader().getDefinedPackage(pn);
        if (pkg == null) {
            String location = getSystemPackageLocation(pn.replace('.', '/'));
            if (location != null) {
                pkg = PackageHelper.definePackage(pn.intern(), location);
            }
        }
        return pkg;
    }

    public static Stream<Package> packages() {
        return Arrays.stream(getSystemPackageNames()).map(name -> getDefinedPackage(name.replace('/', '.')));
    }

    static class PackageHelper {

        private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

        static Package definePackage(String name, String location) {
            Module module = findModule(location);
            if (module != null) {
                if (name.isEmpty())
                    throw new InternalError("empty package in " + location);
                return JLA.definePackage(ClassLoaders.bootLoader(), name, module);
            }
            URL url = toFileURL(location);
            Manifest man = url != null ? getManifest(location) : null;
            return ClassLoaders.bootLoader().defineOrCheckPackage(name, man, url);
        }

        private static Module findModule(String location) {
            String mn = null;
            if (location.startsWith("jrt:/")) {
                mn = location.substring(5, location.length());
            } else if (location.startsWith("file:/")) {
                Path path = Paths.get(URI.create(location));
                Path modulesDir = Paths.get(JAVA_HOME, "modules");
                if (path.startsWith(modulesDir)) {
                    mn = path.getFileName().toString();
                }
            }
            if (mn != null) {
                Optional<Module> om = ModuleLayer.boot().findModule(mn);
                if (!om.isPresent())
                    throw new InternalError(mn + " not in boot layer");
                return om.get();
            }
            return null;
        }

        private static URL toFileURL(String location) {
            return AccessController.doPrivileged(new PrivilegedAction<>() {

                public URL run() {
                    Path path = Paths.get(location);
                    if (Files.isRegularFile(path)) {
                        try {
                            return path.toUri().toURL();
                        } catch (MalformedURLException e) {
                        }
                    }
                    return null;
                }
            });
        }

        private static Manifest getManifest(String location) {
            return AccessController.doPrivileged(new PrivilegedAction<>() {

                public Manifest run() {
                    Path jar = Paths.get(location);
                    try (InputStream in = Files.newInputStream(jar);
                        JarInputStream jis = new JarInputStream(in, false)) {
                        return jis.getManifest();
                    } catch (IOException e) {
                        return null;
                    }
                }
            });
        }
    }

    private static native String[] getSystemPackageNames();

    private static native String getSystemPackageLocation(String name);

    private static native void setBootLoaderUnnamedModule0(Module module);
}