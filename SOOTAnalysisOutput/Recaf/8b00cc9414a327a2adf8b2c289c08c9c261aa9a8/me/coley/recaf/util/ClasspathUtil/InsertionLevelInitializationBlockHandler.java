package me.coley.recaf.util;

import java.io.*;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import static java.lang.Class.forName;

public class ClasspathUtil {

    public static final ClassLoader scl = ClassLoader.getSystemClassLoader();

    private static final Set<String> systemClassNames;

    static {
        try {
            systemClassNames = Collections.unmodifiableSet(scanBootstrapClasses());
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
        if (!areBootstrapClassesFound()) {
            Log.warn("Bootstrap classes are missing!");
        }
    }

    public static boolean resourceExists(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        return ClasspathUtil.class.getResource(path) != null;
    }

    public static InputStream resource(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        return ClasspathUtil.class.getResourceAsStream(path);
    }

    public static Set<String> getSystemClassNames() {
        return systemClassNames;
    }

    public static boolean areBootstrapClassesFound() {
        return checkBootstrapClassExists(getSystemClassNames());
    }

    public static Class<?> getSystemClass(String className) throws ClassNotFoundException {
        return forName(className, false, ClasspathUtil.scl);
    }

    public static boolean classExists(String name) {
        try {
            getSystemClass(name);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Optional<Class<?>> getSystemClassIfExists(String className) {
        try {
            return Optional.of(getSystemClass(className));
        } catch (ClassNotFoundException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static boolean checkBootstrapClassExists(Collection<String> names) {
        String name = Object.class.getName();
        return names.contains(name) || names.contains(name.replace('.', '/'));
    }

    private static Set<String> scanBootstrapClasses() throws Exception {
        float vmVersion = Float.parseFloat(System.getProperty("java.class.version")) - 44;
        Set<String> classes = new LinkedHashSet<>(4096, 1F);
        if (vmVersion < 9) {
            Method method = ClassLoader.class.getDeclaredMethod("getBootstrapClassPath");
            method.setAccessible(true);
            Field field = URLClassLoader.class.getDeclaredField("ucp");
            field.setAccessible(true);
            Object bootstrapClasspath = method.invoke(null);
            URLClassLoader dummyLoader = new URLClassLoader(new URL[0]);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(dummyLoader, bootstrapClasspath);
            URL[] urls = dummyLoader.getURLs();
            for (URL url : urls) {
                String protocol = url.getProtocol();
                JarFile jar = null;
                if ("jar".equals(protocol)) {
                    jar = ((JarURLConnection) url.openConnection()).getJarFile();
                } else if ("file".equals(protocol)) {
                    File file = new File(url.toURI());
                    if (!file.isFile())
                        continue;
                    jar = new JarFile(file);
                }
                if (jar == null)
                    continue;
                try {
                    Enumeration<? extends JarEntry> enumeration = jar.entries();
                    while (enumeration.hasMoreElements()) {
                        JarEntry entry = enumeration.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            classes.add(name.substring(0, name.length() - 6));
                        }
                    }
                } finally {
                    jar.close();
                }
            }
            return classes;
        } else {
            Set<ModuleReference> references = ModuleFinder.ofSystem().findAll();
            for (ModuleReference ref : references) {
                try (ModuleReader mr = ref.open()) {
                    mr.list().forEach(s -> {
                        classes.add(s.substring(0, s.length() - 6));
                    });
                }
            }
        }
        return classes;
    }
}