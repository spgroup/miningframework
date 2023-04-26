package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.loader.BootLoader;
import jdk.internal.loader.ClassLoaders;
import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.misc.VM;
import jdk.internal.module.ServicesCatalog;
import jdk.internal.module.ServicesCatalog.ServiceProvider;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;

public final class ServiceLoader<S> implements Iterable<S> {

    private final Class<S> service;

    private final String serviceName;

    private final ModuleLayer layer;

    private final ClassLoader loader;

    private final AccessControlContext acc;

    private Iterator<Provider<S>> lookupIterator1;

    private final List<S> instantiatedProviders = new ArrayList<>();

    private Iterator<Provider<S>> lookupIterator2;

    private final List<Provider<S>> loadedProviders = new ArrayList<>();

    private boolean loadedAllProviders;

    private int reloadCount;

    private static JavaLangAccess LANG_ACCESS;

    static {
        LANG_ACCESS = SharedSecrets.getJavaLangAccess();
    }

    public static interface Provider<S> extends Supplier<S> {

        Class<? extends S> type();

        @Override
        S get();
    }

    private ServiceLoader(Class<?> caller, ModuleLayer layer, Class<S> svc) {
        Objects.requireNonNull(caller);
        Objects.requireNonNull(layer);
        Objects.requireNonNull(svc);
        checkCaller(caller, svc);
        this.service = svc;
        this.serviceName = svc.getName();
        this.layer = layer;
        this.loader = null;
        this.acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    }

    private ServiceLoader(Class<?> caller, Class<S> svc, ClassLoader cl) {
        Objects.requireNonNull(svc);
        if (VM.isBooted()) {
            checkCaller(caller, svc);
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
        } else {
            Module callerModule = caller.getModule();
            Module base = Object.class.getModule();
            Module svcModule = svc.getModule();
            if (callerModule != base || svcModule != base) {
                fail(svc, "not accessible to " + callerModule + " during VM init");
            }
            cl = null;
        }
        this.service = svc;
        this.serviceName = svc.getName();
        this.layer = null;
        this.loader = cl;
        this.acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    }

    private ServiceLoader(Module callerModule, Class<S> svc, ClassLoader cl) {
        if (!callerModule.canUse(svc)) {
            fail(svc, callerModule + " does not declare `uses`");
        }
        this.service = Objects.requireNonNull(svc);
        this.serviceName = svc.getName();
        this.layer = null;
        this.loader = cl;
        this.acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    }

    private static void checkCaller(Class<?> caller, Class<?> svc) {
        if (caller == null) {
            fail(svc, "no caller to check if it declares `uses`");
        }
        Module callerModule = caller.getModule();
        int mods = svc.getModifiers();
        if (!Reflection.verifyMemberAccess(caller, svc, null, mods)) {
            fail(svc, "service type not accessible to " + callerModule);
        }
        if (!callerModule.canUse(svc)) {
            fail(svc, callerModule + " does not declare `uses`");
        }
    }

    private static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    private Class<?> loadProviderInModule(Module module, String cn) {
        Class<?> clazz = null;
        if (acc == null) {
            try {
                clazz = Class.forName(module, cn);
            } catch (LinkageError e) {
                fail(service, "Unable to load " + cn, e);
            }
        } else {
            PrivilegedExceptionAction<Class<?>> pa = () -> Class.forName(module, cn);
            try {
                clazz = AccessController.doPrivileged(pa);
            } catch (PrivilegedActionException pae) {
                Throwable x = pae.getCause();
                fail(service, "Unable to load " + cn, x);
                return null;
            }
        }
        if (clazz == null)
            fail(service, "Provider " + cn + " not found");
        return clazz;
    }

    private final static class ProviderImpl<S> implements Provider<S> {

        final Class<S> service;

        final AccessControlContext acc;

        final Method factoryMethod;

        final Class<? extends S> type;

        final Constructor<? extends S> ctor;

        @SuppressWarnings("unchecked")
        ProviderImpl(Class<?> service, Class<?> clazz, AccessControlContext acc) {
            this.service = (Class<S>) service;
            this.acc = acc;
            int mods = clazz.getModifiers();
            if (!Modifier.isPublic(mods)) {
                fail(service, clazz + " is not public");
            }
            Method factoryMethod = null;
            if (inExplicitModule(clazz)) {
                factoryMethod = findStaticProviderMethod(clazz);
                if (factoryMethod != null) {
                    Class<?> returnType = factoryMethod.getReturnType();
                    if (!service.isAssignableFrom(returnType)) {
                        fail(service, factoryMethod + " return type not a subtype");
                    }
                }
            }
            this.factoryMethod = factoryMethod;
            if (factoryMethod == null) {
                if (!service.isAssignableFrom(clazz)) {
                    fail(service, clazz.getName() + " not a subtype");
                }
                this.type = (Class<? extends S>) clazz;
                this.ctor = (Constructor<? extends S>) getConstructor(clazz);
            } else {
                this.type = (Class<? extends S>) factoryMethod.getReturnType();
                this.ctor = null;
            }
        }

        @Override
        public Class<? extends S> type() {
            return type;
        }

        @Override
        public S get() {
            if (factoryMethod != null) {
                return invokeFactoryMethod();
            } else {
                return newInstance();
            }
        }

        private boolean inExplicitModule(Class<?> clazz) {
            Module module = clazz.getModule();
            return module.isNamed() && !module.getDescriptor().isAutomatic();
        }

        private Method findStaticProviderMethod(Class<?> clazz) {
            Method method = null;
            try {
                method = LANG_ACCESS.getMethodOrNull(clazz, "provider");
            } catch (Throwable x) {
                fail(service, "Unable to get public provider() method", x);
            }
            if (method != null) {
                int mods = method.getModifiers();
                if (Modifier.isStatic(mods)) {
                    assert Modifier.isPublic(mods);
                    Method m = method;
                    PrivilegedAction<Void> pa = () -> {
                        m.setAccessible(true);
                        return null;
                    };
                    AccessController.doPrivileged(pa);
                    return method;
                }
            }
            return null;
        }

        private Constructor<?> getConstructor(Class<?> clazz) {
            PrivilegedExceptionAction<Constructor<?>> pa = new PrivilegedExceptionAction<>() {

                @Override
                public Constructor<?> run() throws Exception {
                    Constructor<?> ctor = clazz.getConstructor();
                    if (inExplicitModule(clazz))
                        ctor.setAccessible(true);
                    return ctor;
                }
            };
            Constructor<?> ctor = null;
            try {
                ctor = AccessController.doPrivileged(pa);
            } catch (Throwable x) {
                if (x instanceof PrivilegedActionException)
                    x = x.getCause();
                String cn = clazz.getName();
                fail(service, cn + " Unable to get public no-arg constructor", x);
            }
            return ctor;
        }

        private S invokeFactoryMethod() {
            Object result = null;
            Throwable exc = null;
            if (acc == null) {
                try {
                    result = factoryMethod.invoke(null);
                } catch (Throwable x) {
                    exc = x;
                }
            } else {
                PrivilegedExceptionAction<?> pa = new PrivilegedExceptionAction<>() {

                    @Override
                    public Object run() throws Exception {
                        return factoryMethod.invoke(null);
                    }
                };
                try {
                    result = AccessController.doPrivileged(pa, acc);
                } catch (PrivilegedActionException pae) {
                    exc = pae.getCause();
                }
            }
            if (exc != null) {
                if (exc instanceof InvocationTargetException)
                    exc = exc.getCause();
                fail(service, factoryMethod + " failed", exc);
            }
            if (result == null) {
                fail(service, factoryMethod + " returned null");
            }
            @SuppressWarnings("unchecked")
            S p = (S) result;
            return p;
        }

        private S newInstance() {
            S p = null;
            Throwable exc = null;
            if (acc == null) {
                try {
                    p = ctor.newInstance();
                } catch (Throwable x) {
                    exc = x;
                }
            } else {
                PrivilegedExceptionAction<S> pa = new PrivilegedExceptionAction<>() {

                    @Override
                    public S run() throws Exception {
                        return ctor.newInstance();
                    }
                };
                try {
                    p = AccessController.doPrivileged(pa, acc);
                } catch (PrivilegedActionException pae) {
                    exc = pae.getCause();
                }
            }
            if (exc != null) {
                if (exc instanceof InvocationTargetException)
                    exc = exc.getCause();
                String cn = ctor.getDeclaringClass().getName();
                fail(service, "Provider " + cn + " could not be instantiated", exc);
            }
            return p;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, acc);
        }

        @Override
        public boolean equals(Object ob) {
            if (!(ob instanceof ProviderImpl))
                return false;
            @SuppressWarnings("unchecked")
            ProviderImpl<?> that = (ProviderImpl<?>) ob;
            return this.type == that.type && Objects.equals(this.acc, that.acc);
        }
    }

    private final class LayerLookupIterator<T> implements Iterator<Provider<T>> {

        Deque<ModuleLayer> stack = new ArrayDeque<>();

        Set<ModuleLayer> visited = new HashSet<>();

        Iterator<ServiceProvider> iterator;

        ServiceProvider next;

        LayerLookupIterator() {
            visited.add(layer);
            stack.push(layer);
        }

        private Iterator<ServiceProvider> providers(ModuleLayer layer) {
            ServicesCatalog catalog = LANG_ACCESS.getServicesCatalog(layer);
            return catalog.findServices(serviceName).iterator();
        }

        @Override
        public boolean hasNext() {
            if (next != null)
                return true;
            while (true) {
                if (iterator != null && iterator.hasNext()) {
                    next = iterator.next();
                    return true;
                }
                if (stack.isEmpty())
                    return false;
                ModuleLayer layer = stack.pop();
                List<ModuleLayer> parents = layer.parents();
                for (int i = parents.size() - 1; i >= 0; i--) {
                    ModuleLayer parent = parents.get(i);
                    if (!visited.contains(parent)) {
                        visited.add(parent);
                        stack.push(parent);
                    }
                }
                iterator = providers(layer);
            }
        }

        @Override
        public Provider<T> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            ServiceProvider provider = next;
            next = null;
            Module module = provider.module();
            String cn = provider.providerName();
            Class<?> clazz = loadProviderInModule(module, cn);
            return new ProviderImpl<T>(service, clazz, acc);
        }
    }

    private final class ModuleServicesLookupIterator<T> implements Iterator<Provider<T>> {

        ClassLoader currentLoader;

        Iterator<ServiceProvider> iterator;

        ServiceProvider next;

        ModuleServicesLookupIterator() {
            this.currentLoader = loader;
            this.iterator = iteratorFor(loader);
        }

        private List<ServiceProvider> providers(ModuleLayer layer) {
            ServicesCatalog catalog = LANG_ACCESS.getServicesCatalog(layer);
            return catalog.findServices(serviceName);
        }

        private Iterator<ServiceProvider> iteratorFor(ClassLoader loader) {
            ServicesCatalog catalog;
            if (loader == null) {
                catalog = BootLoader.getServicesCatalog();
            } else {
                catalog = ServicesCatalog.getServicesCatalogOrNull(loader);
            }
            List<ServiceProvider> providers;
            if (catalog == null) {
                providers = List.of();
            } else {
                providers = catalog.findServices(serviceName);
            }
            if (loader == null) {
                return providers.iterator();
            } else {
                List<ServiceProvider> allProviders = new ArrayList<>(providers);
                ModuleLayer bootLayer = ModuleLayer.boot();
                Iterator<ModuleLayer> iterator = LANG_ACCESS.layers(loader).iterator();
                while (iterator.hasNext()) {
                    ModuleLayer layer = iterator.next();
                    if (layer != bootLayer) {
                        allProviders.addAll(providers(layer));
                    }
                }
                return allProviders.iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (next != null)
                return true;
            while (true) {
                if (iterator.hasNext()) {
                    next = iterator.next();
                    return true;
                }
                if (currentLoader == null) {
                    return false;
                } else {
                    currentLoader = currentLoader.getParent();
                    iterator = iteratorFor(currentLoader);
                }
            }
        }

        @Override
        public Provider<T> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            ServiceProvider provider = next;
            next = null;
            Module module = provider.module();
            String cn = provider.providerName();
            Class<?> clazz = loadProviderInModule(module, cn);
            return new ProviderImpl<T>(service, clazz, acc);
        }
    }

    private final class LazyClassPathLookupIterator<T> implements Iterator<Provider<T>> {

        static final String PREFIX = "META-INF/services/";

        Set<String> providerNames = new HashSet<>();

        Enumeration<URL> configs;

        Iterator<String> pending;

        Class<?> nextClass;

        String nextErrorMessage;

        LazyClassPathLookupIterator() {
        }

        private int parseLine(URL u, BufferedReader r, int lc, Set<String> names) throws IOException {
            String ln = r.readLine();
            if (ln == null) {
                return -1;
            }
            int ci = ln.indexOf('#');
            if (ci >= 0)
                ln = ln.substring(0, ci);
            ln = ln.trim();
            int n = ln.length();
            if (n != 0) {
                if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                    fail(service, u, lc, "Illegal configuration-file syntax");
                int cp = ln.codePointAt(0);
                if (!Character.isJavaIdentifierStart(cp))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
                int start = Character.charCount(cp);
                for (int i = start; i < n; i += Character.charCount(cp)) {
                    cp = ln.codePointAt(i);
                    if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                        fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
                if (providerNames.add(ln)) {
                    names.add(ln);
                }
            }
            return lc + 1;
        }

        private Iterator<String> parse(URL u) {
            Set<String> names = new LinkedHashSet<>();
            try {
                URLConnection uc = u.openConnection();
                uc.setUseCaches(false);
                try (InputStream in = uc.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                    int lc = 1;
                    while ((lc = parseLine(u, r, lc, names)) >= 0) ;
                }
            } catch (IOException x) {
                fail(service, "Error accessing configuration file", x);
            }
            return names.iterator();
        }

        private boolean hasNextService() {
            if (nextClass != null || nextErrorMessage != null) {
                return true;
            }
            Class<?> clazz;
            do {
                if (configs == null) {
                    try {
                        String fullName = PREFIX + service.getName();
                        if (loader == null) {
                            configs = ClassLoader.getSystemResources(fullName);
                        } else if (loader == ClassLoaders.platformClassLoader()) {
                            if (BootLoader.hasClassPath()) {
                                configs = BootLoader.findResources(fullName);
                            } else {
                                configs = Collections.emptyEnumeration();
                            }
                        } else {
                            configs = loader.getResources(fullName);
                        }
                    } catch (IOException x) {
                        fail(service, "Error locating configuration files", x);
                    }
                }
                while ((pending == null) || !pending.hasNext()) {
                    if (!configs.hasMoreElements()) {
                        return false;
                    }
                    pending = parse(configs.nextElement());
                }
                String cn = pending.next();
                try {
                    clazz = Class.forName(cn, false, loader);
                } catch (ClassNotFoundException x) {
                    nextErrorMessage = "Provider " + cn + " not found";
                    return true;
                }
            } while (clazz.getModule().isNamed());
            nextClass = clazz;
            return true;
        }

        private Provider<T> nextService() {
            if (!hasNextService())
                throw new NoSuchElementException();
            if (nextErrorMessage != null) {
                String msg = nextErrorMessage;
                nextErrorMessage = null;
                fail(service, msg);
            }
            Class<?> clazz = nextClass;
            nextClass = null;
            return new ProviderImpl<T>(service, clazz, acc);
        }

        @Override
        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<>() {

                    public Boolean run() {
                        return hasNextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        @Override
        public Provider<T> next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<Provider<T>> action = new PrivilegedAction<>() {

                    public Provider<T> run() {
                        return nextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }
    }

    private Iterator<Provider<S>> newLookupIterator() {
        assert layer == null || loader == null;
        if (layer != null) {
            return new LayerLookupIterator<>();
        } else {
            Iterator<Provider<S>> first = new ModuleServicesLookupIterator<>();
            Iterator<Provider<S>> second = new LazyClassPathLookupIterator<>();
            return new Iterator<Provider<S>>() {

                @Override
                public boolean hasNext() {
                    return (first.hasNext() || second.hasNext());
                }

                @Override
                public Provider<S> next() {
                    if (first.hasNext()) {
                        return first.next();
                    } else if (second.hasNext()) {
                        return second.next();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    public Iterator<S> iterator() {
        if (lookupIterator1 == null) {
            lookupIterator1 = newLookupIterator();
        }
        return new Iterator<S>() {

            final int expectedReloadCount = ServiceLoader.this.reloadCount;

            int index;

            private void checkReloadCount() {
                if (ServiceLoader.this.reloadCount != expectedReloadCount)
                    throw new ConcurrentModificationException();
            }

            @Override
            public boolean hasNext() {
                checkReloadCount();
                if (index < instantiatedProviders.size())
                    return true;
                return lookupIterator1.hasNext();
            }

            @Override
            public S next() {
                checkReloadCount();
                S next;
                if (index < instantiatedProviders.size()) {
                    next = instantiatedProviders.get(index);
                } else {
                    next = lookupIterator1.next().get();
                    instantiatedProviders.add(next);
                }
                index++;
                return next;
            }
        };
    }

    public Stream<Provider<S>> stream() {
        if (loadedAllProviders) {
            return loadedProviders.stream();
        }
        if (lookupIterator2 == null) {
            lookupIterator2 = newLookupIterator();
        }
        Spliterator<Provider<S>> s = new ProviderSpliterator<>(lookupIterator2);
        return StreamSupport.stream(s, false);
    }

    private class ProviderSpliterator<T> implements Spliterator<Provider<T>> {

        final int expectedReloadCount = ServiceLoader.this.reloadCount;

        final Iterator<Provider<T>> iterator;

        int index;

        ProviderSpliterator(Iterator<Provider<T>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Spliterator<Provider<T>> trySplit() {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super Provider<T>> action) {
            if (ServiceLoader.this.reloadCount != expectedReloadCount)
                throw new ConcurrentModificationException();
            Provider<T> next = null;
            if (index < loadedProviders.size()) {
                next = (Provider<T>) loadedProviders.get(index++);
            } else if (iterator.hasNext()) {
                next = iterator.next();
            } else {
                loadedAllProviders = true;
            }
            if (next != null) {
                action.accept(next);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }
    }

    static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader, Module callerModule) {
        return new ServiceLoader<>(callerModule, service, loader);
    }

    @CallerSensitive
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoader<>(Reflection.getCallerClass(), service, loader);
    }

    @CallerSensitive
    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new ServiceLoader<>(Reflection.getCallerClass(), service, cl);
    }

    @CallerSensitive
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getPlatformClassLoader();
        return new ServiceLoader<>(Reflection.getCallerClass(), service, cl);
    }

    @CallerSensitive
    public static <S> ServiceLoader<S> load(ModuleLayer layer, Class<S> service) {
        return new ServiceLoader<>(Reflection.getCallerClass(), layer, service);
    }

    public Optional<S> findFirst() {
        Iterator<S> iterator = iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    public void reload() {
        lookupIterator1 = null;
        instantiatedProviders.clear();
        lookupIterator2 = null;
        loadedProviders.clear();
        loadedAllProviders = false;
        reloadCount++;
    }

    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }
}