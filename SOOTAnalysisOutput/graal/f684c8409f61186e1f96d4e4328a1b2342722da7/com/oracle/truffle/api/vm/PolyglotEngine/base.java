package com.oracle.truffle.api.vm;

import static com.oracle.truffle.api.vm.VMAccessor.INSTRUMENT;
import static com.oracle.truffle.api.vm.VMAccessor.LANGUAGE;
import static com.oracle.truffle.api.vm.VMAccessor.NODES;
import static com.oracle.truffle.api.vm.VMAccessor.engine;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graalvm.options.OptionValues;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.InstrumentInfo;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.impl.Accessor.EngineSupport;
import com.oracle.truffle.api.impl.DispatchOutputStream;
import com.oracle.truffle.api.instrumentation.TruffleInstrument;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.vm.ComputeInExecutor.Info;
import com.oracle.truffle.api.vm.PolyglotEngine.Builder;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;
import com.oracle.truffle.api.vm.PolyglotRootNode.EvalRootNode;
import com.oracle.truffle.api.vm.PolyglotRuntime.LanguageShared;

@SuppressWarnings({ "rawtypes" })
public class PolyglotEngine {

    static final Logger LOG = Logger.getLogger(PolyglotEngine.class.getName());

    static final PolyglotEngine UNUSABLE_ENGINE = new PolyglotEngine();

    static {
        ensureInitialized();
    }

    static void ensureInitialized() {
        if (VMAccessor.SPI == null || !(VMAccessor.SPI.engineSupport() instanceof LegacyEngineImpl)) {
            VMAccessor.initialize(new LegacyEngineImpl());
        }
    }

    static final PolyglotEngineProfile GLOBAL_PROFILE = new PolyglotEngineProfile(null);

    static final Object UNSET_CONTEXT = new Object();

    private final Thread initThread;

    private final PolyglotCache cachedTargets;

    private final Map<LanguageShared, Language> sharedToLanguage;

    private final Map<String, Language> mimeTypeToLanguage;

    @CompilationFinal(dimensions = 1)
    final Language[] languageArray;

    final PolyglotRuntime runtime;

    final InputStream in;

    final DispatchOutputStream err;

    final DispatchOutputStream out;

    final ComputeInExecutor.Info executor;

    private volatile boolean disposed;

    static final boolean JDK8OrEarlier = System.getProperty("java.specification.version").compareTo("1.9") < 0;

    static {
        try {
            Class.forName(TruffleInstrument.class.getName(), true, TruffleInstrument.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Object[]> config;

    private HashMap<String, Object> globals;

    PolyglotEngine() {
        assertNoCompilation();
        ensureInitialized();
        this.initThread = null;
        this.runtime = null;
        this.cachedTargets = null;
        this.languageArray = null;
        this.sharedToLanguage = null;
        this.mimeTypeToLanguage = null;
        this.in = null;
        this.out = null;
        this.err = null;
        this.executor = null;
    }

    PolyglotEngine(PolyglotRuntime runtime, Executor executor, InputStream in, DispatchOutputStream out, DispatchOutputStream err, Map<String, Object> globals, List<Object[]> config) {
        assertNoCompilation();
        this.initThread = Thread.currentThread();
        this.runtime = runtime;
        this.languageArray = new Language[runtime.getLanguages().size()];
        this.cachedTargets = new PolyglotCache(this);
        this.sharedToLanguage = new HashMap<>();
        this.mimeTypeToLanguage = new HashMap<>();
        this.in = in;
        this.out = out;
        this.err = err;
        this.executor = ComputeInExecutor.wrap(executor);
        this.globals = new HashMap<>(globals);
        this.config = config;
        initLanguages();
        runtime.notifyEngineCreated();
    }

    private void initLanguages() {
        for (LanguageShared languageShared : runtime.getLanguages()) {
            Language newLanguage = new Language(languageShared);
            sharedToLanguage.put(languageShared, newLanguage);
            assert languageArray[languageShared.languageId] == null : "attempting to overwrite language";
            languageArray[languageShared.languageId] = newLanguage;
            for (String mimeType : languageShared.cache.getMimeTypes()) {
                mimeTypeToLanguage.put(mimeType, newLanguage);
            }
        }
    }

    PolyglotEngine enter() {
        return runtime.engineProfile.enter(this);
    }

    void leave(Object prev) {
        runtime.engineProfile.leave((PolyglotEngine) prev);
    }

    Info executor() {
        return executor;
    }

    private boolean isCurrentVM() {
        return this == runtime.engineProfile.get();
    }

    public static PolyglotEngine.Builder newBuilder() {
        PolyglotEngine engine = new PolyglotEngine();
        return engine.new Builder();
    }

    @Deprecated
    public static PolyglotEngine.Builder buildNew() {
        return newBuilder();
    }

    public class Builder {

        private OutputStream out;

        private OutputStream err;

        private InputStream in;

        private PolyglotRuntime runtime;

        private final Map<String, Object> globals = new HashMap<>();

        private Executor executor;

        private List<Object[]> arguments;

        Builder() {
        }

        public Builder setOut(OutputStream os) {
            out = os;
            return this;
        }

        public Builder setErr(OutputStream os) {
            err = os;
            return this;
        }

        public Builder setIn(InputStream is) {
            in = is;
            return this;
        }

        public Builder config(String mimeType, String key, Object value) {
            if (this.arguments == null) {
                this.arguments = new ArrayList<>();
            }
            this.arguments.add(new Object[] { mimeType, key, value });
            return this;
        }

        public Builder globalSymbol(String name, Object obj) {
            final Object truffleReady = JavaInterop.asTruffleValue(obj);
            globals.put(name, truffleReady);
            return this;
        }

        @SuppressWarnings("hiding")
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder runtime(PolyglotRuntime polyglotRuntime) {
            checkRuntime(polyglotRuntime);
            this.runtime = polyglotRuntime;
            return this;
        }

        public PolyglotEngine build() {
            assertNoCompilation();
            InputStream realIn;
            DispatchOutputStream realOut;
            DispatchOutputStream realErr;
            PolyglotRuntime realRuntime = runtime;
            if (realRuntime == null) {
                realRuntime = PolyglotRuntime.newBuilder().setIn(in).setOut(out).setErr(err).build(true);
                realIn = realRuntime.in;
                realOut = realRuntime.out;
                realErr = realRuntime.err;
            } else {
                checkRuntime(realRuntime);
                if (out == null) {
                    realOut = realRuntime.out;
                } else {
                    realOut = INSTRUMENT.createDispatchOutput(out);
                    engine().attachOutputConsumer(realOut, realRuntime.out);
                }
                if (err == null) {
                    realErr = realRuntime.err;
                } else {
                    realErr = INSTRUMENT.createDispatchOutput(err);
                    engine().attachOutputConsumer(realErr, realRuntime.err);
                }
                realIn = in == null ? realRuntime.in : in;
            }
            return new PolyglotEngine(realRuntime, executor, realIn, realOut, realErr, globals, arguments);
        }

        private void checkRuntime(PolyglotRuntime realRuntime) {
            if (realRuntime.disposed) {
                throw new IllegalArgumentException("Given runtime already disposed.");
            }
            if (realRuntime.automaticDispose) {
                throw new IllegalArgumentException("Cannot reuse private/create runtime of another engine. " + "Please usine an explicitely created PolyglotRuntime instead.");
            }
        }
    }

    public Map<String, ? extends Language> getLanguages() {
        return Collections.unmodifiableMap(mimeTypeToLanguage);
    }

    @Deprecated
    public Map<String, Instrument> getInstruments() {
        return runtime.instruments;
    }

    public PolyglotRuntime getRuntime() {
        return runtime;
    }

    public Value eval(Source source) {
        assertNoCompilation();
        assert checkThread();
        return evalImpl(findLanguage(source.getMimeType(), true), source);
    }

    private Value evalImpl(final Language l, final Source source) {
        assert checkThread();
        ComputeInExecutor<Object> compute = new ComputeInExecutor<Object>(executor()) {

            @Override
            protected Object compute() {
                CallTarget evalTarget = l.parserCache.get(source);
                if (evalTarget == null) {
                    evalTarget = PolyglotRootNode.createEval(PolyglotEngine.this, l, source);
                    l.parserCache.put(source, evalTarget);
                }
                return evalTarget.call();
            }
        };
        compute.perform();
        return new ExecutorValue(l, compute);
    }

    public void dispose() {
        assert checkThread();
        assertNoCompilation();
        disposed = true;
        ComputeInExecutor<Void> compute = new ComputeInExecutor<Void>(executor()) {

            @Override
            protected Void compute() {
                Object prev = enter();
                try {
                    disposeImpl();
                    return null;
                } finally {
                    leave(prev);
                }
            }
        };
        compute.get();
    }

    private void disposeImpl() {
        for (Language language : getLanguages().values()) {
            language.disposeContext();
        }
        runtime.notifyEngineDisposed();
    }

    private Object[] debugger() {
        return runtime.debugger;
    }

    public Value findGlobalSymbol(final String globalName) {
        assert checkThread();
        assertNoCompilation();
        for (Object v : findGlobalSymbols(globalName)) {
            return (Value) v;
        }
        return null;
    }

    public Iterable<Value> findGlobalSymbols(String globalName) {
        assert checkThread();
        assertNoCompilation();
        return new Iterable<Value>() {

            final Iterable<? extends Object> iterable = importSymbol(null, globalName, true);

            public Iterator<Value> iterator() {
                return new Iterator<PolyglotEngine.Value>() {

                    final Iterator<? extends Object> iterator = iterable.iterator();

                    public boolean hasNext() {
                        ComputeInExecutor<Boolean> invokeCompute = new ComputeInExecutor<Boolean>(executor()) {

                            @SuppressWarnings("try")
                            @Override
                            protected Boolean compute() {
                                Object prev = enter();
                                try {
                                    return iterator.hasNext();
                                } finally {
                                    leave(prev);
                                }
                            }
                        };
                        return invokeCompute.get().booleanValue();
                    }

                    public Value next() {
                        ComputeInExecutor<Value> invokeCompute = new ComputeInExecutor<Value>(executor()) {

                            @SuppressWarnings("try")
                            @Override
                            protected Value compute() {
                                Object prev = enter();
                                try {
                                    return (Value) iterator.next();
                                } finally {
                                    leave(prev);
                                }
                            }
                        };
                        return invokeCompute.get();
                    }
                };
            }
        };
    }

    private Iterable<? extends Object> importSymbol(Language filterLanguage, String globalName, boolean needsValue) {
        class SymbolIterator implements Iterator<Object> {

            private final Collection<? extends Language> uniqueLang;

            private Object next;

            private Iterator<? extends Language> explicit;

            private Iterator<? extends Language> implicit;

            SymbolIterator(Collection<? extends Language> uniqueLang, Object first) {
                this.uniqueLang = uniqueLang;
                if (first instanceof DirectValue) {
                    if (needsValue) {
                        this.next = first;
                    } else {
                        this.next = ((DirectValue) first).value;
                    }
                } else {
                    this.next = (needsValue && first != null) ? new DirectValue(null, first) : first;
                }
            }

            @Override
            public boolean hasNext() {
                return findNext() != this;
            }

            @Override
            public Object next() {
                Object res = findNext();
                if (res == this) {
                    throw new NoSuchElementException();
                }
                assert !needsValue || res instanceof Value;
                next = null;
                return res;
            }

            private Object findNext() {
                if (next != null) {
                    return next;
                }
                if (explicit == null) {
                    explicit = uniqueLang.iterator();
                }
                while (explicit.hasNext()) {
                    Language dl = explicit.next();
                    TruffleLanguage.Env env = dl.getEnv(false);
                    if (dl != filterLanguage && env != null) {
                        Object obj = findExportedSymbol(dl, env, globalName, true);
                        if (obj != null) {
                            next = obj;
                            explicit.remove();
                            return next;
                        }
                    }
                }
                if (implicit == null) {
                    implicit = uniqueLang.iterator();
                }
                while (implicit.hasNext()) {
                    Language dl = implicit.next();
                    TruffleLanguage.Env env = dl.getEnv(false);
                    if (dl != filterLanguage && env != null) {
                        Object obj = findExportedSymbol(dl, env, globalName, false);
                        if (obj != null) {
                            next = obj;
                            return next;
                        }
                    }
                }
                return next = this;
            }

            private Object findExportedSymbol(Language lang, TruffleLanguage.Env env, String name, boolean onlyExplicit) {
                Object value = LANGUAGE.findExportedSymbol(env, name, onlyExplicit);
                if (needsValue && value != null) {
                    value = new DirectValue(lang, value);
                }
                return value;
            }
        }
        Object globalObj = globals.get(globalName);
        final Collection<? extends Language> uniqueLang = getLanguages().values();
        return new Iterable<Object>() {

            @Override
            public Iterator<Object> iterator() {
                return new SymbolIterator(new LinkedHashSet<>(uniqueLang), globalObj);
            }
        };
    }

    private static void assertNoCompilation() {
        CompilerAsserts.neverPartOfCompilation("Methods of PolyglotEngine must not be compiled by Truffle. Use Truffle interoperability or a @TruffleBoundary instead.");
    }

    private boolean checkThread() {
        if (initThread != Thread.currentThread()) {
            throw new IllegalStateException("PolyglotEngine created on " + initThread.getName() + " but used on " + Thread.currentThread().getName());
        }
        if (disposed) {
            throw new IllegalStateException("Engine has already been disposed");
        }
        return true;
    }

    private Language findLanguage(String mimeType, boolean failOnError) {
        Language l = mimeTypeToLanguage.get(mimeType);
        if (failOnError && l == null) {
            throw new IllegalStateException("No language for MIME type " + mimeType + " found. Supported types: " + mimeTypeToLanguage.keySet());
        }
        return l;
    }

    Language findLanguage(LanguageShared env) {
        return sharedToLanguage.get(env);
    }

    Language getLanguage(Class<? extends TruffleLanguage<?>> languageClass) {
        if (CompilerDirectives.isPartialEvaluationConstant(this)) {
            return getLanguageImpl(languageClass);
        } else {
            return getLanguageBoundary(languageClass);
        }
    }

    @TruffleBoundary
    private Language getLanguageBoundary(Class<? extends TruffleLanguage<?>> languageClass) {
        return getLanguageImpl(languageClass);
    }

    private final FinalIntMap languageIndexMap = new FinalIntMap();

    private Language getLanguageImpl(Class<? extends TruffleLanguage<?>> languageClass) {
        int indexValue = languageIndexMap.get(languageClass);
        if (indexValue == -1) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            Language language = findLanguage(languageClass, false, true);
            indexValue = language.shared.languageId;
            languageIndexMap.put(languageClass, indexValue);
        }
        return languageArray[indexValue];
    }

    Language findLanguage(Class<? extends TruffleLanguage> languageClazz, boolean onlyInitialized, boolean failIfNotFound) {
        for (Language lang : languageArray) {
            assert lang.shared.language != null;
            if (onlyInitialized && lang.getEnv(false) == null) {
                continue;
            }
            TruffleLanguage<?> spi = NODES.getLanguageSpi(lang.shared.language);
            if (languageClazz.isInstance(spi)) {
                return lang;
            }
        }
        if (failIfNotFound) {
            Set<String> languageNames = new HashSet<>();
            for (Language lang : languageArray) {
                languageNames.add(lang.shared.cache.getClassName());
            }
            throw new IllegalStateException("Cannot find language " + languageClazz + " among " + languageNames);
        } else {
            return null;
        }
    }

    Env findEnv(Class<? extends TruffleLanguage> languageClazz, boolean failIfNotFound) {
        Language language = findLanguage(languageClazz, true, failIfNotFound);
        if (language != null) {
            return language.getEnv(false);
        }
        return null;
    }

    public abstract class Value {

        private final Language language;

        private CallTarget executeTarget;

        private CallTarget asJavaObjectTarget;

        Value(Language language) {
            this.language = language;
        }

        abstract boolean isDirect();

        abstract Object value();

        @SuppressWarnings("unchecked")
        private <T> T unwrapJava(Object value) {
            CallTarget unwrapTarget = cachedTargets.lookupAsJava(value.getClass());
            return (T) unwrapTarget.call(value, Object.class);
        }

        @SuppressWarnings("unchecked")
        private <T> T asJavaObject(Class<T> type, Object value) {
            if (asJavaObjectTarget == null) {
                asJavaObjectTarget = cachedTargets.lookupAsJava(value == null ? void.class : value.getClass());
            }
            return (T) asJavaObjectTarget.call(value, type);
        }

        @SuppressWarnings("try")
        private Object executeDirect(Object[] args) {
            Object value = value();
            if (executeTarget == null) {
                executeTarget = cachedTargets.lookupExecute(value.getClass());
            }
            return executeTarget.call(value, args);
        }

        public Object get() {
            Object result = waitForSymbol();
            if (result instanceof TruffleObject) {
                result = unwrapJava(ConvertedObject.value(result));
                if (result instanceof TruffleObject) {
                    result = EngineTruffleObject.wrap(PolyglotEngine.this, result);
                }
            }
            return ConvertedObject.isNull(result) ? null : result;
        }

        @SuppressWarnings("unchecked")
        public <T> T as(final Class<T> representation) {
            Object original = waitForSymbol();
            Object unwrapped = original;
            if (original instanceof TruffleObject) {
                Object realOrig = ConvertedObject.original(original);
                unwrapped = new ConvertedObject((TruffleObject) realOrig, unwrapJava(realOrig));
            }
            if (representation == String.class) {
                Object unwrappedConverted = ConvertedObject.original(unwrapped);
                Object string;
                if (language != null) {
                    PolyglotEngine prev = enter();
                    try {
                        string = LANGUAGE.toStringIfVisible(language.getEnv(false), unwrappedConverted, false);
                    } finally {
                        leave(prev);
                    }
                } else {
                    string = unwrappedConverted.toString();
                }
                return representation.cast(string);
            }
            if (ConvertedObject.isInstance(representation, unwrapped)) {
                return ConvertedObject.cast(representation, unwrapped);
            }
            if (original instanceof TruffleObject) {
                original = EngineTruffleObject.wrap(PolyglotEngine.this, original);
            }
            Object javaValue = asJavaObject(representation, original);
            if (representation.isPrimitive()) {
                return (T) javaValue;
            } else {
                return representation.cast(javaValue);
            }
        }

        @Deprecated
        public Value invoke(final Object thiz, final Object... args) {
            return execute(args);
        }

        public Value execute(final Object... args) {
            if (isDirect()) {
                Object ret = executeDirect(args);
                return new DirectValue(language, ret);
            }
            assertNoCompilation();
            get();
            ComputeInExecutor<Object> invokeCompute = new ComputeInExecutor<Object>(PolyglotEngine.this.executor()) {

                @SuppressWarnings("try")
                @Override
                protected Object compute() {
                    return executeDirect(args);
                }
            };
            invokeCompute.perform();
            return new ExecutorValue(language, invokeCompute);
        }

        private Object waitForSymbol() {
            assertNoCompilation();
            assert PolyglotEngine.this.checkThread();
            Object value = value();
            assert value != null;
            assert !(value instanceof EngineTruffleObject);
            return value;
        }

        public Value getMetaObject() {
            if (language == null) {
                return null;
            }
            ComputeInExecutor<Object> invokeCompute = new ComputeInExecutor<Object>(executor()) {

                @SuppressWarnings("try")
                @Override
                protected Object compute() {
                    Object prev = enter();
                    try {
                        return LANGUAGE.findMetaObject(language.getEnv(true), ConvertedObject.original(value()));
                    } finally {
                        leave(prev);
                    }
                }
            };
            Object value = invokeCompute.get();
            if (value != null) {
                return new DirectValue(language, value);
            } else {
                return null;
            }
        }

        public SourceSection getSourceLocation() {
            if (language == null) {
                return null;
            }
            ComputeInExecutor<SourceSection> invokeCompute = new ComputeInExecutor<SourceSection>(executor()) {

                @SuppressWarnings("try")
                @Override
                protected SourceSection compute() {
                    Object prev = enter();
                    try {
                        return LANGUAGE.findSourceLocation(language.getEnv(true), ConvertedObject.original(value()));
                    } finally {
                        leave(prev);
                    }
                }
            };
            return invokeCompute.get();
        }
    }

    private class DirectValue extends Value {

        private final Object value;

        DirectValue(Language language, Object value) {
            super(language);
            this.value = value;
            assert value != null;
        }

        @Override
        boolean isDirect() {
            return true;
        }

        @Override
        Object value() {
            return value;
        }

        @Override
        public String toString() {
            return "PolyglotEngine.Value[value=" + value + ",computed=true,exception=null]";
        }
    }

    private class ExecutorValue extends Value {

        private final ComputeInExecutor<Object> compute;

        ExecutorValue(Language language, ComputeInExecutor<Object> compute) {
            super(language);
            this.compute = compute;
        }

        @Override
        boolean isDirect() {
            return false;
        }

        @Override
        Object value() {
            return compute.get();
        }

        @Override
        public String toString() {
            return "PolyglotEngine.Value[" + compute + "]";
        }
    }

    @Deprecated
    public final class Instrument extends PolyglotRuntime.Instrument {

        Instrument(PolyglotRuntime runtime, InstrumentCache cache) {
            runtime.super(cache);
        }
    }

    public class Language {

        private volatile TruffleLanguage.Env env;

        final LanguageShared shared;

        @CompilationFinal
        volatile Object context = UNSET_CONTEXT;

        private final Map<Source, CallTarget> parserCache;

        Language(LanguageShared shared) {
            this.shared = shared;
            this.parserCache = new WeakHashMap<>();
        }

        PolyglotEngine engine() {
            return PolyglotEngine.this;
        }

        Object context() {
            return context;
        }

        public Set<String> getMimeTypes() {
            return shared.cache.getMimeTypes();
        }

        public String getName() {
            return shared.cache.getName();
        }

        public String getVersion() {
            return shared.cache.getVersion();
        }

        public boolean isInteractive() {
            return shared.cache.isInteractive();
        }

        public Value eval(Source source) {
            assertNoCompilation();
            return evalImpl(this, source);
        }

        @SuppressWarnings("try")
        public Value getGlobalObject() {
            assert checkThread();
            ComputeInExecutor<Value> compute = new ComputeInExecutor<Value>(executor()) {

                @Override
                protected Value compute() {
                    Object prev = enter();
                    try {
                        Object res = LANGUAGE.languageGlobal(getEnv(true));
                        if (res == null) {
                            return null;
                        }
                        return new DirectValue(Language.this, res);
                    } finally {
                        leave(prev);
                    }
                }
            };
            return compute.get();
        }

        void disposeContext() {
            if (env != null) {
                synchronized (this) {
                    Env localEnv = this.env;
                    assert localEnv != null;
                    if (localEnv != null) {
                        try {
                            LANGUAGE.dispose(localEnv);
                        } catch (Exception | Error ex) {
                            LOG.log(Level.SEVERE, "Error disposing " + this, ex);
                        }
                        this.env = null;
                        context = UNSET_CONTEXT;
                    }
                }
            }
        }

        TruffleLanguage.Env getEnv(boolean create) {
            TruffleLanguage.Env localEnv = env;
            if ((localEnv == null && create)) {
                synchronized (this) {
                    localEnv = env;
                    if (localEnv == null && create) {
                        localEnv = LANGUAGE.createEnv(this, shared.getLanguageEnsureInitialized(), engine().out, engine().err, engine().in, getArgumentsForLanguage(), new OptionValuesImpl(null, shared.options), new String[0]);
                        context = LANGUAGE.getContext(localEnv);
                        this.env = localEnv;
                        LANGUAGE.postInitEnv(localEnv);
                    }
                }
            }
            return localEnv;
        }

        @Override
        public String toString() {
            return "[" + getName() + "@ " + getVersion() + " for " + getMimeTypes() + "]";
        }

        private Map<String, Object> getArgumentsForLanguage() {
            if (config == null) {
                return Collections.emptyMap();
            }
            Map<String, Object> forLanguage = new HashMap<>();
            for (Object[] mimeKeyValue : config) {
                if (shared.cache.getMimeTypes().contains(mimeKeyValue[0])) {
                    forLanguage.put((String) mimeKeyValue[1], mimeKeyValue[2]);
                }
            }
            return Collections.unmodifiableMap(forLanguage);
        }
    }

    static final class LegacyEngineImpl extends EngineSupport {

        @Override
        public boolean isDisposed(Object vmObject) {
            return ((Language) vmObject).engine().disposed;
        }

        @Override
        public Object contextReferenceGet(Object vmObject) {
            return findVMObject(vmObject).getCurrentContext();
        }

        @Override
        public Env getEnvForLanguage(Object vmObject, String mimeType) {
            return ((Language) vmObject).engine().findLanguage(mimeType, true).getEnv(true);
        }

        @Override
        public OptionValues getCompilerOptionValues(RootNode rootNode) {
            return null;
        }

        @Override
        public Object getVMFromLanguageObject(Object engineObject) {
            return ((LanguageShared) engineObject).runtime;
        }

        @Override
        public Env getEnvForInstrument(Object vmObject, String mimeType) {
            PolyglotEngine currentVM = ((Instrument) vmObject).getRuntime().currentVM();
            if (currentVM == null) {
                throw new IllegalStateException("No current engine found.");
            }
            Language lang = currentVM.findLanguage(mimeType, true);
            Env env = lang.getEnv(true);
            assert env != null;
            return env;
        }

        @Override
        public <T> T lookup(InstrumentInfo info, Class<T> serviceClass) {
            Object vmObject = LANGUAGE.getVMObject(info);
            Instrument instrument = (Instrument) vmObject;
            return instrument.lookup(serviceClass);
        }

        @Override
        public <S> S lookup(LanguageInfo language, Class<S> type) {
            LanguageShared cache = (LanguageShared) NODES.getEngineObject(language);
            return LANGUAGE.lookup(cache.getLanguageEnsureInitialized(), type);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <C, T extends TruffleLanguage<C>> C getCurrentContext(Class<T> languageClass) {
            PolyglotEngine engine = PolyglotEngine.GLOBAL_PROFILE.get();
            if (engine == null) {
                CompilerDirectives.transferToInterpreter();
                throw new IllegalStateException("No current context available.");
            }
            Language language = engine.getLanguage(languageClass);
            if (language.env == null) {
                CompilerDirectives.transferToInterpreter();
                throw new IllegalStateException("No current context available.");
            }
            return (C) language.context;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends TruffleLanguage<?>> T getCurrentLanguage(Class<T> languageClass) {
            PolyglotEngine engine = PolyglotEngine.GLOBAL_PROFILE.get();
            if (engine == null) {
                CompilerDirectives.transferToInterpreter();
                throw new IllegalStateException("No current language available.");
            }
            Language language = engine.getLanguage(languageClass);
            return (T) NODES.getLanguageSpi(language.shared.language);
        }

        @Override
        public Map<String, LanguageInfo> getLanguages(Object vmObject) {
            PolyglotRuntime vm;
            if (vmObject instanceof Language) {
                vm = ((Language) vmObject).shared.getRuntime();
            } else if (vmObject instanceof Instrument) {
                vm = ((Instrument) vmObject).getRuntime();
            } else {
                throw new AssertionError();
            }
            return vm.languageInfos;
        }

        @Override
        public Map<String, InstrumentInfo> getInstruments(Object vmObject) {
            PolyglotRuntime vm;
            if (vmObject instanceof Language) {
                vm = ((Language) vmObject).shared.getRuntime();
            } else if (vmObject instanceof Instrument) {
                vm = ((Instrument) vmObject).getRuntime();
            } else {
                throw new AssertionError();
            }
            return vm.instrumentInfos;
        }

        @Override
        public Env getEnvForInstrument(LanguageInfo language) {
            return ((LanguageShared) NODES.getEngineObject(language)).currentLanguage().getEnv(true);
        }

        @Override
        public Object getCurrentVM() {
            return PolyglotEngine.GLOBAL_PROFILE.get();
        }

        @Override
        public boolean isEvalRoot(RootNode target) {
            if (target instanceof EvalRootNode) {
                PolyglotEngine engine = ((EvalRootNode) target).getEngine();
                return engine.isCurrentVM();
            }
            return false;
        }

        @Override
        public boolean isMimeTypeSupported(Object vmObject, String mimeType) {
            return ((Language) vmObject).engine().findLanguage(mimeType, false) != null;
        }

        @Override
        public Env findEnv(Object vmObject, Class<? extends TruffleLanguage> languageClass, boolean failIfNotFound) {
            return ((PolyglotEngine) vmObject).findEnv(languageClass, failIfNotFound);
        }

        @Override
        public Object getInstrumentationHandler(Object vmObject) {
            return ((LanguageShared) vmObject).getRuntime().instrumentationHandler;
        }

        @Override
        public Iterable<? extends Object> importSymbols(Object languageShared, Env env, String globalName) {
            Language language = (Language) languageShared;
            return language.engine().importSymbol(language, globalName, false);
        }

        @Override
        public Object importSymbol(Object vmObject, Env env, String symbolName) {
            Iterator<? extends Object> symbolIterator = importSymbols(vmObject, env, symbolName).iterator();
            if (!symbolIterator.hasNext()) {
                return null;
            }
            return symbolIterator.next();
        }

        @Override
        public void exportSymbol(Object vmObject, String symbolName, Object value) {
            Language language = (Language) vmObject;
            HashMap<String, Object> global = language.engine().globals;
            if (value == null) {
                global.remove(symbolName);
            } else {
                global.put(symbolName, language.engine().new DirectValue(language, value));
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public <C> com.oracle.truffle.api.impl.FindContextNode<C> createFindContextNode(TruffleLanguage<C> lang) {
            Object vm = getCurrentVM();
            if (vm == null) {
                throw new IllegalStateException("Cannot access current vm.");
            }
            return new FindContextNodeImpl<>(findEnv(vm, lang.getClass(), true));
        }

        @Override
        public void registerDebugger(Object vm, Object debugger) {
            PolyglotEngine engine = (PolyglotEngine) vm;
            assert engine.debugger()[0] == null || engine.debugger()[0] == debugger;
            engine.debugger()[0] = debugger;
        }

        @Override
        public Object findOriginalObject(Object truffleObject) {
            if (truffleObject instanceof EngineTruffleObject) {
                return ((EngineTruffleObject) truffleObject).getDelegate();
            }
            return truffleObject;
        }

        @Override
        public CallTarget lookupOrRegisterComputation(Object truffleObject, RootNode computation, Object... keys) {
            CompilerAsserts.neverPartOfCompilation();
            assert keys.length > 0;
            Object key;
            if (keys.length == 1) {
                key = keys[0];
                assert TruffleOptions.AOT || assertKeyType(key);
            } else {
                Pair p = null;
                for (Object k : keys) {
                    assert TruffleOptions.AOT || assertKeyType(k);
                    p = new Pair(k, p);
                }
                key = p;
            }
            if (truffleObject instanceof EngineTruffleObject) {
                PolyglotEngine engine = ((EngineTruffleObject) truffleObject).engine();
                return engine.cachedTargets.lookupComputation(key, computation);
            }
            if (computation == null) {
                return null;
            }
            return Truffle.getRuntime().createCallTarget(computation);
        }

        private static boolean assertKeyType(Object key) {
            assert key instanceof Class || key instanceof Method || key instanceof Message : "Unexpected key: " + key;
            return true;
        }

        private static LanguageShared findVMObject(Object obj) {
            return ((LanguageShared) obj);
        }

        @Override
        public Object toGuestValue(Object obj, Object languageContext) {
            return JavaInterop.asTruffleValue(obj);
        }

        @Override
        public org.graalvm.polyglot.Value toHostValue(Object obj, Object languageContext) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class Pair {

        final Object key;

        final Pair next;

        Pair(Object key, Pair next) {
            this.key = key;
            this.next = next;
        }

        @Override
        public int hashCode() {
            return this.key.hashCode() + (next == null ? 3754 : next.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pair other = (Pair) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            if (!Objects.equals(this.next, other.next)) {
                return false;
            }
            return true;
        }
    }
}

class PolyglotEngineSnippets {

    abstract class YourLang extends TruffleLanguage<Object> {

        public static final String MIME_TYPE = "application/my-test-lang";
    }

    public static PolyglotEngine defaultPolyglotEngine() {
        PolyglotEngine engine = PolyglotEngine.newBuilder().build();
        return engine;
    }

    public static PolyglotEngine createPolyglotEngine(OutputStream yourOutput, InputStream yourInput) {
        PolyglotEngine engine = PolyglotEngine.newBuilder().setOut(yourOutput).setErr(yourOutput).setIn(yourInput).build();
        return engine;
    }

    public static int evalCode() {
        Source src = Source.newBuilder("3 + 39").mimeType("application/my-test-lang").name("example.test-lang").build();
        PolyglotEngine engine = PolyglotEngine.newBuilder().build();
        Value result = engine.eval(src);
        int answer = result.as(Integer.class);
        return answer;
    }

    public static PolyglotEngine initializeWithParameters() {
        String[] args = { "--kernel", "Kernel.som", "--instrument", "dyn-metrics" };
        PolyglotEngine.Builder builder = PolyglotEngine.newBuilder();
        builder.config(YourLang.MIME_TYPE, "CMD_ARGS", args);
        PolyglotEngine engine = builder.build();
        return engine;
    }

    public static final class Multiplier {

        public static int mul(int x, int y) {
            return x * y;
        }
    }

    public interface Multiply {

        int mul(int x, int y);
    }

    public static PolyglotEngine configureJavaInterop(Multiply multiply) {
        TruffleObject staticAccess = JavaInterop.asTruffleObject(Multiplier.class);
        TruffleObject instanceAccess = JavaInterop.asTruffleObject(multiply);
        PolyglotEngine engine = PolyglotEngine.newBuilder().globalSymbol("mul", staticAccess).globalSymbol("compose", instanceAccess).build();
        return engine;
    }

    static PolyglotEngine configureJavaInteropWithMul() {
        PolyglotEngineSnippets.Multiply multi = new PolyglotEngineSnippets.Multiply() {

            @Override
            public int mul(int x, int y) {
                return x * y;
            }
        };
        return configureJavaInterop(multi);
    }

    static Value findAndReportMultipleExportedSymbols(PolyglotEngine engine, String name) {
        Value found = null;
        for (Value value : engine.findGlobalSymbols(name)) {
            if (found != null) {
                throw new IllegalStateException("Multiple global symbols exported with " + name + " name");
            }
            found = value;
        }
        return found;
    }

    static PrintStream out = System.out;

    static PrintStream err = System.err;

    @SuppressWarnings("unused")
    static void createEngines(String mimeType) {
        PolyglotRuntime runtime = PolyglotRuntime.newBuilder().setOut(out).setErr(err).build();
        Builder builder = PolyglotEngine.newBuilder().runtime(runtime);
        PolyglotEngine engine1 = builder.build();
        PolyglotEngine engine2 = builder.build();
        PolyglotEngine engine3 = builder.build();
    }
}
