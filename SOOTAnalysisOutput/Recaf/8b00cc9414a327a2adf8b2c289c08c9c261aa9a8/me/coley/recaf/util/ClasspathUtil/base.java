package me.coley.recaf.util;

import com.google.common.reflect.ClassPath;
import java.io.*;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import static me.coley.recaf.util.Log.*;
import static java.lang.Class.forName;

@SuppressWarnings("UnstableApiUsage")
public class ClasspathUtil {

    public static final ClassLoader scl = ClassLoader.getSystemClassLoader();

    public static final ClassPath cp;

    private static final List<String> systemClassNames;

    static {
        ClassPathScanner scanner = new ClassPathScanner();
        cp = scanner.scan(scl);
        systemClassNames = Collections.unmodifiableList(scanner.internalNames);
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

    public static List<String> getSystemClassNames() {
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

    private static ClassPath getClassPath(ClassLoader loader) throws IOException {
        return ClassPath.from(loader);
    }

    private static class ClassPathScanner {

        Set<String> build = new LinkedHashSet<>();

        ArrayList<String> internalNames;

        private ClassPath updateClassPath(ClassLoader loader) {
            try {
                ClassPath classPath = getClassPath(loader);
                Set<String> tmp = classPath.getResources().stream().filter(ClassPath.ClassInfo.class::isInstance).map(ClassPath.ClassInfo.class::cast).map(ClassPath.ClassInfo::getName).collect(Collectors.toCollection(LinkedHashSet::new));
                build.addAll(tmp);
                return classPath;
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to scan classpath entries: " + loader.getClass().getName(), e);
            }
        }

        private boolean checkBootstrapClass() {
            return checkBootstrapClassExists(build);
        }

        ClassPath scan(ClassLoader classLoader) {
            ClassPath scl = updateClassPath(classLoader);
            if (!checkBootstrapClass()) {
                float vmVersion = Float.parseFloat(System.getProperty("java.class.version")) - 44;
                if (vmVersion < 9) {
                    try {
                        Method method = ClassLoader.class.getDeclaredMethod("getBootstrapClassPath");
                        method.setAccessible(true);
                        Field field = URLClassLoader.class.getDeclaredField("ucp");
                        field.setAccessible(true);
                        Object bootstrapClasspath = method.invoke(null);
                        URLClassLoader dummyLoader = new URLClassLoader(new URL[0], classLoader);
                        Field modifiers = Field.class.getDeclaredField("modifiers");
                        modifiers.setAccessible(true);
                        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        field.set(dummyLoader, bootstrapClasspath);
                        updateClassPath(dummyLoader);
                        verifyScan();
                    } catch (ReflectiveOperationException | SecurityException e) {
                        throw new ExceptionInInitializerError(e);
                    }
                } else {
                    try {
                        Set<ModuleReference> references = ModuleFinder.ofSystem().findAll();
                        for (ModuleReference ref : references) {
                            try (ModuleReader mr = ref.open()) {
                                mr.list().forEach(s -> {
                                    build.add(s.replace('/', '.').substring(0, s.length() - 6));
                                });
                            }
                        }
                        verifyScan();
                    } catch (Throwable t) {
                        throw new ExceptionInInitializerError(t);
                    }
                }
            }
            internalNames = build.stream().map(name -> name.replace('.', '/')).sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(ArrayList::new));
            internalNames.trimToSize();
            return scl;
        }

        private void verifyScan() {
            if (!checkBootstrapClass()) {
                warn("Bootstrap classes are (still) missing from the classpath scan!");
            }
        }
    }
}
