package com.oracle.truffle.api.library;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.library.LibraryExport.DelegateExport;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.utilities.FinalBitSet;
import sun.misc.Unsafe;

public abstract class LibraryFactory<T extends Library> {

    private static final ConcurrentHashMap<Class<?>, LibraryFactory<?>> LIBRARIES;

    private static final DefaultExportProvider[] EMPTY_DEFAULT_EXPORT_ARRAY = new DefaultExportProvider[0];

    static {
        LIBRARIES = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unused")
    private static void resetNativeImageState() {
        assert TruffleOptions.AOT : "Only supported during image generation";
        clearNonTruffleClasses(LIBRARIES);
        clearNonTruffleClasses(ResolvedDispatch.CACHE);
        clearNonTruffleClasses(ResolvedDispatch.REGISTRY);
    }

    private static void clearNonTruffleClasses(Map<Class<?>, ?> map) {
        Class<?>[] classes = map.keySet().toArray(new Class<?>[0]);
        for (Class<?> clazz : classes) {
            if (LibraryAccessor.jdkServicesAccessor().isNonTruffleClass(clazz)) {
                map.remove(clazz);
            }
        }
    }

    private final Class<T> libraryClass;

    private final List<Message> messages;

    private final ConcurrentHashMap<Class<?>, LibraryExport<T>> exportCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, T> uncachedCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, T> cachedCache = new ConcurrentHashMap<>();

    private final ProxyExports proxyExports = new ProxyExports();

    final Map<String, Message> nameToMessages;

    @CompilationFinal
    private volatile T uncachedDispatch;

    final DynamicDispatchLibrary dispatchLibrary;

    final DefaultExportProvider[] beforeBuiltinDefaultExports;

    final DefaultExportProvider[] afterBuiltinDefaultExports;

    @SuppressWarnings("unchecked")
    protected LibraryFactory(Class<T> libraryClass, List<Message> messages) {
        assert this.getClass().getName().endsWith(LibraryExport.GENERATED_CLASS_SUFFIX);
        assert this.getClass().getAnnotation(GeneratedBy.class) != null;
        assert this.getClass().getAnnotation(GeneratedBy.class).value() == libraryClass;
        this.libraryClass = libraryClass;
        this.messages = Collections.unmodifiableList(messages);
        Map<String, Message> messagesMap = new LinkedHashMap<>();
        for (Message message : getMessages()) {
            assert message.library == null;
            message.library = (LibraryFactory<Library>) this;
            messagesMap.put(message.getSimpleName(), message);
        }
        this.nameToMessages = messagesMap;
        if (libraryClass == DynamicDispatchLibrary.class) {
            this.dispatchLibrary = null;
        } else {
            GenerateLibrary annotation = libraryClass.getAnnotation(GenerateLibrary.class);
            boolean dynamicDispatchEnabled = annotation == null || libraryClass.getAnnotation(GenerateLibrary.class).dynamicDispatchEnabled();
            if (dynamicDispatchEnabled) {
                this.dispatchLibrary = LibraryFactory.resolve(DynamicDispatchLibrary.class).getUncached();
            } else {
                this.dispatchLibrary = null;
            }
        }
        List<DefaultExportProvider> providers = getExternalDefaultProviders().get(libraryClass.getName());
        List<DefaultExportProvider> beforeBuiltin = null;
        List<DefaultExportProvider> afterBuiltin = null;
        if (providers != null && !providers.isEmpty()) {
            for (DefaultExportProvider provider : providers) {
                List<DefaultExportProvider> providerList = new ArrayList<>();
                if (provider.getPriority() > 0) {
                    if (beforeBuiltin == null) {
                        beforeBuiltin = new ArrayList<>();
                    }
                    providerList = beforeBuiltin;
                } else {
                    if (afterBuiltin == null) {
                        afterBuiltin = new ArrayList<>();
                    }
                    providerList = afterBuiltin;
                }
                providerList.add(provider);
            }
        }
        if (beforeBuiltin != null) {
            beforeBuiltinDefaultExports = beforeBuiltin.toArray(new DefaultExportProvider[beforeBuiltin.size()]);
        } else {
            beforeBuiltinDefaultExports = EMPTY_DEFAULT_EXPORT_ARRAY;
        }
        if (afterBuiltin != null) {
            afterBuiltinDefaultExports = afterBuiltin.toArray(new DefaultExportProvider[afterBuiltin.size()]);
        } else {
            afterBuiltinDefaultExports = EMPTY_DEFAULT_EXPORT_ARRAY;
        }
    }

    @TruffleBoundary
    public final T createDispatched(int limit) {
        if (limit <= 0) {
            return getUncached();
        } else {
            ensureLibraryInitialized();
            return createDispatchImpl(limit);
        }
    }

    @TruffleBoundary
    public final T create(Object receiver) {
        Class<?> dispatchClass = dispatch(receiver);
        T cached = cachedCache.get(dispatchClass);
        if (cached != null) {
            assert validateExport(receiver, dispatchClass, cached);
            return cached;
        }
        ensureLibraryInitialized();
        LibraryExport<T> export = lookupExport(receiver, dispatchClass);
        cached = export.createCached(receiver);
        assert (cached = createAssertionsImpl(export, cached)) != null;
        if (!cached.isAdoptable()) {
            assert cached.accepts(receiver) : String.format("Invalid accepts implementation detected in '%s'", dispatchClass.getName());
            T otherCached = cachedCache.putIfAbsent(dispatchClass, cached);
            if (otherCached != null) {
                return otherCached;
            }
        }
        return cached;
    }

    public final T getUncached() {
        T dispatch = this.uncachedDispatch;
        if (dispatch == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            ensureLibraryInitialized();
            dispatch = createUncachedDispatch();
            T otherDispatch = this.uncachedDispatch;
            if (otherDispatch != null) {
                dispatch = otherDispatch;
            } else {
                this.uncachedDispatch = dispatch;
            }
        }
        return dispatch;
    }

    private void ensureLibraryInitialized() {
        CompilerAsserts.neverPartOfCompilation();
        UNSAFE.ensureClassInitialized(libraryClass);
    }

    @TruffleBoundary
    public final T getUncached(Object receiver) {
        Class<?> dispatchClass = dispatch(receiver);
        T uncached = uncachedCache.get(dispatchClass);
        if (uncached != null) {
            assert validateExport(receiver, dispatchClass, uncached);
            return uncached;
        }
        ensureLibraryInitialized();
        LibraryExport<T> export = lookupExport(receiver, dispatchClass);
        uncached = export.createUncached(receiver);
        assert validateExport(receiver, dispatchClass, uncached);
        assert uncached.accepts(receiver);
        assert (uncached = createAssertionsImpl(export, uncached)) != null;
        T otherUncached = uncachedCache.putIfAbsent(dispatchClass, uncached);
        if (otherUncached != null) {
            return otherUncached;
        }
        return uncached;
    }

    private static volatile Map<String, List<DefaultExportProvider>> externalDefaultProviders;

    private static Map<String, List<DefaultExportProvider>> getExternalDefaultProviders() {
        Map<String, List<DefaultExportProvider>> providers = externalDefaultProviders;
        if (providers == null) {
            synchronized (LibraryFactory.class) {
                providers = externalDefaultProviders;
                if (providers == null) {
                    providers = loadExternalDefaultProviders();
                }
            }
        }
        return providers;
    }

    private static Map<String, List<DefaultExportProvider>> loadExternalDefaultProviders() {
        Map<String, List<DefaultExportProvider>> providers;
        providers = new LinkedHashMap<>();
        for (DefaultExportProvider provider : LibraryAccessor.engineAccessor().loadServices(DefaultExportProvider.class)) {
            String libraryClassName = provider.getLibraryClassName();
            List<DefaultExportProvider> providerList = providers.get(libraryClassName);
            if (providerList == null) {
                providerList = new ArrayList<>();
                providers.put(libraryClassName, providerList);
            }
            providerList.add(provider);
        }
        for (List<DefaultExportProvider> providerList : providers.values()) {
            Collections.sort(providerList, new Comparator<DefaultExportProvider>() {

                public int compare(DefaultExportProvider o1, DefaultExportProvider o2) {
                    return Integer.compare(o2.getPriority(), o1.getPriority());
                }
            });
        }
        return providers;
    }

    final Class<T> getLibraryClass() {
        return libraryClass;
    }

    final List<Message> getMessages() {
        return messages;
    }

    private T createAssertionsImpl(LibraryExport<T> export, T cached) {
        if (needsAssertions(export)) {
            return createAssertions(cached);
        } else {
            return cached;
        }
    }

    private boolean needsAssertions(LibraryExport<T> export) {
        Class<?> registerClass = export.registerClass;
        if (export.isDefaultExport() && registerClass != null && registerClass.getName().equals("com.oracle.truffle.api.interop.DefaultTruffleObjectExports")) {
            return false;
        } else {
            return true;
        }
    }

    private boolean validateExport(Object receiver, Class<?> dispatchClass, T library) {
        validateExport(receiver, dispatchClass, lookupExport(receiver, dispatchClass));
        assert library.accepts(receiver) : library.getClass().getName();
        return true;
    }

    private Class<?> dispatch(Object receiver) {
        if (receiver == null) {
            throw new NullPointerException("Null receiver values are not supported by libraries.");
        }
        if (dispatchLibrary == null) {
            return receiver.getClass();
        } else {
            Class<?> dispatch = dispatchLibrary.dispatch(receiver);
            if (dispatch == null) {
                return receiver.getClass();
            }
            return dispatch;
        }
    }

    protected abstract T createDispatchImpl(int limit);

    protected abstract T createUncachedDispatch();

    protected abstract T createProxy(ReflectionLibrary lib);

    protected T createDelegate(T original) {
        return original;
    }

    protected T createAssertions(T delegate) {
        return delegate;
    }

    protected abstract Class<?> getDefaultClass(Object receiver);

    private Class<?> getDefaultClassImpl(Object receiver) {
        for (DefaultExportProvider defaultExport : beforeBuiltinDefaultExports) {
            if (defaultExport.getReceiverClass().isInstance(receiver)) {
                return defaultExport.getDefaultExport();
            }
        }
        Class<?> defaultClass = getDefaultClass(receiver);
        if (defaultClass != getLibraryClass()) {
            return defaultClass;
        }
        for (DefaultExportProvider defaultExport : afterBuiltinDefaultExports) {
            if (defaultExport.getReceiverClass().isInstance(receiver)) {
                return defaultExport.getDefaultExport();
            }
        }
        return defaultClass;
    }

    protected abstract Object genericDispatch(Library library, Object receiver, Message message, Object[] arguments, int parameterOffset) throws Exception;

    protected FinalBitSet createMessageBitSet(@SuppressWarnings({ "unused", "hiding" }) Message... enabledMessages) {
        throw new AssertionError("should be generated");
    }

    protected static boolean isDelegated(Library lib, int index) {
        boolean result = ((DelegateExport) lib).getDelegateExportMessages().get(index);
        CompilerAsserts.partialEvaluationConstant(result);
        return !result;
    }

    protected static Object readDelegate(Library lib, Object receiver) {
        return ((DelegateExport) lib).readDelegateExport(receiver);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Library> T getDelegateLibrary(T lib, Object delegate) {
        return (T) ((DelegateExport) lib).getDelegateExportLibrary(delegate);
    }

    final LibraryExport<T> lookupExport(Object receiver, Class<?> dispatchedClass) {
        LibraryExport<T> lib = this.exportCache.get(dispatchedClass);
        if (lib != null) {
            return lib;
        }
        ResolvedDispatch resolvedLibrary = ResolvedDispatch.lookup(dispatchedClass);
        lib = resolvedLibrary.getLibrary(libraryClass);
        if (lib == null) {
            if (libraryClass != DynamicDispatchLibrary.class && resolvedLibrary.getLibrary(ReflectionLibrary.class) != null) {
                lib = proxyExports;
            } else {
                Class<?> defaultClass = getDefaultClassImpl(receiver);
                lib = ResolvedDispatch.lookup(defaultClass).getLibrary(libraryClass);
            }
        } else {
            assert !lib.isDefaultExport() : String.format("Dynamic dispatch from receiver class '%s' to default export '%s' detected. " + "Use null instead to dispatch to a default export.", receiver.getClass().getName(), dispatchedClass.getName());
            validateExport(receiver, dispatchedClass, lib);
        }
        LibraryExport<T> concurrent = this.exportCache.putIfAbsent(dispatchedClass, lib);
        return concurrent != null ? concurrent : lib;
    }

    private void validateExport(Object receiver, Class<?> dispatchedClass, LibraryExport<T> exports) throws AssertionError {
        if (!exports.getReceiverClass().isInstance(receiver)) {
            throw new AssertionError(String.format("Receiver class %s was dynamically dispatched to incompatible exports %s. Expected receiver class %s.", receiver.getClass().getName(), dispatchedClass.getName(), exports.getReceiverClass().getName()));
        }
    }

    @TruffleBoundary
    public static <T extends Library> LibraryFactory<T> resolve(Class<T> library) {
        Objects.requireNonNull(library);
        return resolveImpl(library, true);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Library> LibraryFactory<T> resolveImpl(Class<T> library, boolean fail) {
        LibraryFactory<?> lib = LIBRARIES.get(library);
        if (lib == null) {
            loadGeneratedClass(library);
            lib = LIBRARIES.get(library);
            if (lib == null) {
                if (fail) {
                    throw new IllegalArgumentException(String.format("Class '%s' is not a registered library. Truffle libraries must be annotated with @%s to be registered. Did the Truffle annotation processor run?", library.getName(), GenerateLibrary.class.getSimpleName()));
                }
                return null;
            }
        }
        return (LibraryFactory<T>) lib;
    }

    private static final sun.misc.Unsafe UNSAFE;

    static {
        Unsafe unsafe;
        try {
            unsafe = Unsafe.getUnsafe();
        } catch (SecurityException e) {
            try {
                Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafeInstance.setAccessible(true);
                unsafe = (Unsafe) theUnsafeInstance.get(Unsafe.class);
            } catch (Exception e2) {
                throw new RuntimeException("exception while trying to get Unsafe.theUnsafe via reflection:", e2);
            }
        }
        UNSAFE = unsafe;
    }

    static LibraryFactory<?> loadGeneratedClass(Class<?> libraryClass) {
        if (Library.class.isAssignableFrom(libraryClass)) {
            String generatedClassName = libraryClass.getPackage().getName() + "." + libraryClass.getSimpleName() + "Gen";
            try {
                Class.forName(generatedClassName, true, libraryClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                return null;
            }
            return LIBRARIES.get(libraryClass);
        }
        return null;
    }

    static Message resolveMessage(Class<? extends Library> library, String message, boolean fail) {
        Objects.requireNonNull(message);
        LibraryFactory<?> lib = resolveImpl(library, fail);
        if (lib == null) {
            assert !fail;
            return null;
        }
        return resolveLibraryMessage(lib, message, fail);
    }

    private static Message resolveLibraryMessage(LibraryFactory<?> lib, String message, boolean fail) {
        Message foundMessage = lib.nameToMessages.get(message);
        if (fail && foundMessage == null) {
            throw new IllegalArgumentException(String.format("Unknown message '%s' for library '%s' specified.", message, lib.getLibraryClass().getName()));
        }
        return foundMessage;
    }

    protected static <T extends Library> void register(Class<T> libraryClass, LibraryFactory<T> library) {
        LibraryFactory<?> lib = LIBRARIES.putIfAbsent(libraryClass, library);
        if (lib != null) {
            throw new AssertionError("Reflection cannot be installed for a library twice.");
        }
    }

    @Override
    public String toString() {
        return "LibraryFactory [library=" + libraryClass.getName() + "]";
    }

    final class ProxyExports extends LibraryExport<T> {

        protected ProxyExports() {
            super(libraryClass, Object.class, true);
        }

        @Override
        public T createUncached(Object receiver) {
            return createProxy(ReflectionLibrary.getFactory().getUncached(receiver));
        }

        @Override
        protected T createCached(Object receiver) {
            return createProxy(ReflectionLibrary.getFactory().create(receiver));
        }
    }

    static final class ResolvedDispatch {

        private static final ConcurrentHashMap<Class<?>, ResolvedDispatch> CACHE = new ConcurrentHashMap<>();

        private static final ConcurrentHashMap<Class<?>, LibraryExport<?>[]> REGISTRY = new ConcurrentHashMap<>();

        private static final ResolvedDispatch OBJECT_RECEIVER = new ResolvedDispatch(null, Object.class);

        private final ResolvedDispatch parent;

        private final Class<?> dispatchClass;

        private final Map<Class<?>, LibraryExport<?>> libraries;

        @SuppressWarnings({ "hiding", "unchecked" })
        private ResolvedDispatch(ResolvedDispatch parent, Class<?> dispatchClass, LibraryExport<?>... libs) {
            this.parent = parent;
            this.dispatchClass = dispatchClass;
            Map<Class<?>, LibraryExport<?>> libraries = new LinkedHashMap<>();
            for (LibraryExport<?> lib : libs) {
                libraries.put(lib.getLibrary(), lib);
            }
            this.libraries = libraries;
        }

        @SuppressWarnings("unchecked")
        <T extends Library> LibraryExport<T> getLibrary(Class<T> libraryClass) {
            LibraryExport<?> lib = libraries.get(libraryClass);
            if (lib == null && parent != null) {
                lib = parent.getLibrary(libraryClass);
            }
            return (LibraryExport<T>) lib;
        }

        @TruffleBoundary
        static ResolvedDispatch lookup(Class<?> receiverClass) {
            ResolvedDispatch type = CACHE.get(receiverClass);
            if (type == null) {
                type = resolveClass(receiverClass);
            }
            return type;
        }

        static <T extends Library> void register(Class<?> receiverClass, LibraryExport<?>... libs) {
            for (LibraryExport<?> lib : libs) {
                lib.registerClass = receiverClass;
            }
            LibraryExport<?>[] prevLibs = REGISTRY.put(receiverClass, libs);
            if (prevLibs != null) {
                throw new IllegalStateException("Receiver " + receiverClass + " is already registered.");
            }
            if (TruffleOptions.AOT) {
                lookup(receiverClass);
            }
        }

        @Override
        public String toString() {
            return "ResolvedDispatch[" + dispatchClass.getName() + "]";
        }

        Set<Class<?>> getLibraries() {
            return libraries.keySet();
        }

        private static boolean hasExports(Class<?> c) {
            return c.getAnnotationsByType(ExportLibrary.class).length > 0;
        }

        private static ResolvedDispatch resolveClass(Class<?> dispatchClass) {
            if (dispatchClass == null) {
                return OBJECT_RECEIVER;
            }
            ResolvedDispatch parent = resolveClass(dispatchClass.getSuperclass());
            ResolvedDispatch resolved;
            LibraryExport<?>[] libs = REGISTRY.get(dispatchClass);
            if (libs == null && hasExports(dispatchClass)) {
                loadGeneratedClass(dispatchClass);
                libs = REGISTRY.get(dispatchClass);
                if (libs == null) {
                    throw new AssertionError(String.format("Libraries for class '%s' could not be resolved. Not registered?", dispatchClass.getName()));
                }
            }
            if (libs != null) {
                resolved = new ResolvedDispatch(parent, dispatchClass, libs);
            } else {
                resolved = parent;
            }
            ResolvedDispatch concurrent = CACHE.putIfAbsent(dispatchClass, resolved);
            if (concurrent != null) {
                return concurrent;
            } else {
                return resolved;
            }
        }

        static void loadGeneratedClass(Class<?> currentReceiverClass) {
            String generatedClassName = currentReceiverClass.getPackage().getName() + "." + currentReceiverClass.getSimpleName() + "Gen";
            try {
                Class.forName(generatedClassName, true, currentReceiverClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new AssertionError(String.format("Generated class '%s' for class '%s' not found. " + "Did the Truffle annotation processor run?", generatedClassName, currentReceiverClass.getName()), e);
            }
        }
    }
}
