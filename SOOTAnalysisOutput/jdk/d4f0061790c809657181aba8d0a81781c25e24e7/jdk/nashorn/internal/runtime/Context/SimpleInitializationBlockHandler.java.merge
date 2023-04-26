package jdk.nashorn.internal.runtime;

import static jdk.internal.org.objectweb.asm.Opcodes.V1_7;
import static jdk.nashorn.internal.codegen.CompilerConstants.CONSTANTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.CREATE_PROGRAM_FUNCTION;
import static jdk.nashorn.internal.codegen.CompilerConstants.SOURCE;
import static jdk.nashorn.internal.codegen.CompilerConstants.STRICT_MODE;
import static jdk.nashorn.internal.runtime.CodeStore.newCodeStore;
import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import static jdk.nashorn.internal.runtime.Source.sourceFor;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.ScriptEngine;
import jdk.dynalink.DynamicLinker;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.WeakValueCache;
import jdk.nashorn.internal.codegen.Compiler;
import jdk.nashorn.internal.codegen.Compiler.CompilationPhases;
import jdk.nashorn.internal.codegen.ObjectClassGenerator;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.debug.ASTWriter;
import jdk.nashorn.internal.ir.debug.PrintVisitor;
import jdk.nashorn.internal.lookup.MethodHandleFactory;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.events.RuntimeEvent;
import jdk.nashorn.internal.runtime.linker.Bootstrap;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import jdk.nashorn.internal.runtime.logging.Loggable;
import jdk.nashorn.internal.runtime.logging.Logger;
import jdk.nashorn.internal.runtime.options.LoggingOption.LoggerInfo;
import jdk.nashorn.internal.runtime.options.Options;
import jdk.internal.misc.Unsafe;

public final class Context {

    public static final String NASHORN_SET_CONFIG = "nashorn.setConfig";

    public static final String NASHORN_CREATE_CONTEXT = "nashorn.createContext";

    public static final String NASHORN_CREATE_GLOBAL = "nashorn.createGlobal";

    public static final String NASHORN_GET_CONTEXT = "nashorn.getContext";

    public static final String NASHORN_JAVA_REFLECTION = "nashorn.JavaReflection";

    public static final String NASHORN_DEBUG_MODE = "nashorn.debugMode";

    private static final String LOAD_CLASSPATH = "classpath:";

    private static final String LOAD_FX = "fx:";

    private static final String LOAD_NASHORN = "nashorn:";

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final MethodType CREATE_PROGRAM_FUNCTION_TYPE = MethodType.methodType(ScriptFunction.class, ScriptObject.class);

    private static final LongAdder NAMED_INSTALLED_SCRIPT_COUNT = new LongAdder();

    private static final LongAdder ANONYMOUS_INSTALLED_SCRIPT_COUNT = new LongAdder();

    private final FieldMode fieldMode;

    private static enum FieldMode {

        AUTO, OBJECTS, DUAL
    }

    private final Map<String, SwitchPoint> builtinSwitchPoints = new HashMap<>();

    static {
        DebuggerSupport.FORCELOAD = true;
    }

    static long getNamedInstalledScriptCount() {
        return NAMED_INSTALLED_SCRIPT_COUNT.sum();
    }

    static long getAnonymousInstalledScriptCount() {
        return ANONYMOUS_INSTALLED_SCRIPT_COUNT.sum();
    }

    private abstract static class ContextCodeInstaller implements CodeInstaller {

        final Context context;

        final CodeSource codeSource;

        ContextCodeInstaller(final Context context, final CodeSource codeSource) {
            this.context = context;
            this.codeSource = codeSource;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void initialize(final Collection<Class<?>> classes, final Source source, final Object[] constants) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        for (final Class<?> clazz : classes) {
                            final Field sourceField = clazz.getDeclaredField(SOURCE.symbolName());
                            sourceField.setAccessible(true);
                            sourceField.set(null, source);
                            final Field constantsField = clazz.getDeclaredField(CONSTANTS.symbolName());
                            constantsField.setAccessible(true);
                            constantsField.set(null, constants);
                        }
                        return null;
                    }
                });
            } catch (final PrivilegedActionException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void verify(final byte[] code) {
            context.verify(code);
        }

        @Override
        public long getUniqueScriptId() {
            return context.getUniqueScriptId();
        }

        @Override
        public void storeScript(final String cacheKey, final Source source, final String mainClassName, final Map<String, byte[]> classBytes, final Map<Integer, FunctionInitializer> initializers, final Object[] constants, final int compilationId) {
            if (context.codeStore != null) {
                context.codeStore.store(cacheKey, source, mainClassName, classBytes, initializers, constants, compilationId);
            }
        }

        @Override
        public StoredScript loadScript(final Source source, final String functionKey) {
            if (context.codeStore != null) {
                return context.codeStore.load(source, functionKey);
            }
            return null;
        }

        @Override
        public boolean isCompatibleWith(final CodeInstaller other) {
            if (other instanceof ContextCodeInstaller) {
                final ContextCodeInstaller cci = (ContextCodeInstaller) other;
                return cci.context == context && cci.codeSource == codeSource;
            }
            return false;
        }
    }

    private static class NamedContextCodeInstaller extends ContextCodeInstaller {

        private final ScriptLoader loader;

        private int usageCount = 0;

        private int bytesDefined = 0;

        private final static int MAX_USAGES = 10;

        private final static int MAX_BYTES_DEFINED = 200_000;

        private NamedContextCodeInstaller(final Context context, final CodeSource codeSource, final ScriptLoader loader) {
            super(context, codeSource);
            this.loader = loader;
        }

        @Override
        public Class<?> install(final String className, final byte[] bytecode) {
            usageCount++;
            bytesDefined += bytecode.length;
            NAMED_INSTALLED_SCRIPT_COUNT.increment();
            return loader.installClass(Compiler.binaryName(className), bytecode, codeSource);
        }

        @Override
        public CodeInstaller getOnDemandCompilationInstaller() {
            if (usageCount < MAX_USAGES && bytesDefined < MAX_BYTES_DEFINED) {
                return this;
            }
            return new NamedContextCodeInstaller(context, codeSource, context.createNewLoader());
        }

        @Override
        public CodeInstaller getMultiClassCodeInstaller() {
            return this;
        }
    }

    private final WeakValueCache<CodeSource, Class<?>> anonymousHostClasses = new WeakValueCache<>();

    private static final class AnonymousContextCodeInstaller extends ContextCodeInstaller {

        private static final Unsafe UNSAFE = Unsafe.getUnsafe();

        private static final String ANONYMOUS_HOST_CLASS_NAME = Compiler.SCRIPTS_PACKAGE.replace('/', '.') + ".AnonymousHost";

        private static final byte[] ANONYMOUS_HOST_CLASS_BYTES = getAnonymousHostClassBytes();

        private final Class<?> hostClass;

        private AnonymousContextCodeInstaller(final Context context, final CodeSource codeSource, final Class<?> hostClass) {
            super(context, codeSource);
            this.hostClass = hostClass;
        }

        @Override
        public Class<?> install(final String className, final byte[] bytecode) {
            ANONYMOUS_INSTALLED_SCRIPT_COUNT.increment();
            return UNSAFE.defineAnonymousClass(hostClass, bytecode, null);
        }

        @Override
        public CodeInstaller getOnDemandCompilationInstaller() {
            return this;
        }

        @Override
        public CodeInstaller getMultiClassCodeInstaller() {
            return new NamedContextCodeInstaller(context, codeSource, context.createNewLoader());
        }

        private static byte[] getAnonymousHostClassBytes() {
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cw.visit(V1_7, Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, ANONYMOUS_HOST_CLASS_NAME.replace('.', '/'), null, "java/lang/Object", null);
            cw.visitEnd();
            return cw.toByteArray();
        }
    }

    public static final boolean DEBUG = Options.getBooleanProperty("nashorn.debug");

    private static final ThreadLocal<Global> currentGlobal = new ThreadLocal<>();

    private ClassCache classCache;

    private CodeStore codeStore;

    private final AtomicReference<GlobalConstants> globalConstantsRef = new AtomicReference<>();

    static final boolean javaSqlFound, javaSqlRowsetFound;

    static {
        final ModuleLayer boot = ModuleLayer.boot();
        javaSqlFound = boot.findModule("java.sql").isPresent();
        javaSqlRowsetFound = boot.findModule("java.sql.rowset").isPresent();
    }

    public static Global getGlobal() {
        return currentGlobal.get();
    }

    public static void setGlobal(final ScriptObject global) {
        if (global != null && !(global instanceof Global)) {
            throw new IllegalArgumentException("not a global!");
        }
        setGlobal((Global) global);
    }

    public static void setGlobal(final Global global) {
        assert getGlobal() != global;
        if (global != null) {
            final GlobalConstants globalConstants = getContext(global).getGlobalConstants();
            if (globalConstants != null) {
                globalConstants.invalidateAll();
            }
        }
        currentGlobal.set(global);
    }

    public static Context getContext() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission(NASHORN_GET_CONTEXT));
        }
        return getContextTrusted();
    }

    public static PrintWriter getCurrentErr() {
        final ScriptObject global = getGlobal();
        return (global != null) ? global.getContext().getErr() : new PrintWriter(System.err);
    }

    public static void err(final String str) {
        err(str, true);
    }

    public static void err(final String str, final boolean crlf) {
        final PrintWriter err = Context.getCurrentErr();
        if (err != null) {
            if (crlf) {
                err.println(str);
            } else {
                err.print(str);
            }
        }
    }

    private final ScriptEnvironment env;

    final boolean _strict;

    private final ClassLoader appLoader;

    ClassLoader getAppLoader() {
        return appLoader;
    }

    private final ScriptLoader scriptLoader;

    private final DynamicLinker dynamicLinker;

    private final ErrorManager errors;

    private final AtomicLong uniqueScriptId;

    private final ClassFilter classFilter;

    private static final StructureLoader theStructLoader;

    private static final ConcurrentMap<String, Class<?>> structureClasses = new ConcurrentHashMap<>();

    @SuppressWarnings("static-method")
    StructureLoader getStructLoader() {
        return theStructLoader;
    }

    private static AccessControlContext createNoPermAccCtxt() {
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, new Permissions()) });
    }

    private static AccessControlContext createPermAccCtxt(final String permName) {
        final Permissions perms = new Permissions();
        perms.add(new RuntimePermission(permName));
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, perms) });
    }

    private static final AccessControlContext NO_PERMISSIONS_ACC_CTXT = createNoPermAccCtxt();

    private static final AccessControlContext CREATE_LOADER_ACC_CTXT = createPermAccCtxt("createClassLoader");

    private static final AccessControlContext CREATE_GLOBAL_ACC_CTXT = createPermAccCtxt(NASHORN_CREATE_GLOBAL);

    private static final AccessControlContext GET_LOADER_ACC_CTXT = createPermAccCtxt("getClassLoader");

    static {
        final ClassLoader myLoader = Context.class.getClassLoader();
        theStructLoader = AccessController.doPrivileged(new PrivilegedAction<StructureLoader>() {

            @Override
            public StructureLoader run() {
                return new StructureLoader(myLoader);
            }
        }, CREATE_LOADER_ACC_CTXT);
    }

    public static class ThrowErrorManager extends ErrorManager {

        @Override
        public void error(final String message) {
            throw new ParserException(message);
        }

        @Override
        public void error(final ParserException e) {
            throw e;
        }
    }

    public Context(final Options options, final ErrorManager errors, final ClassLoader appLoader) {
        this(options, errors, appLoader, null);
    }

    public Context(final Options options, final ErrorManager errors, final ClassLoader appLoader, final ClassFilter classFilter) {
        this(options, errors, new PrintWriter(System.out, true), new PrintWriter(System.err, true), appLoader, classFilter);
    }

    public Context(final Options options, final ErrorManager errors, final PrintWriter out, final PrintWriter err, final ClassLoader appLoader) {
        this(options, errors, out, err, appLoader, (ClassFilter) null);
    }

    public Context(final Options options, final ErrorManager errors, final PrintWriter out, final PrintWriter err, final ClassLoader appLoader, final ClassFilter classFilter) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission(NASHORN_CREATE_CONTEXT));
        }
        this.classFilter = classFilter;
        this.env = new ScriptEnvironment(options, out, err);
        this._strict = env._strict;
        if (env._loader_per_compile) {
            this.scriptLoader = null;
            this.uniqueScriptId = null;
        } else {
            this.scriptLoader = createNewLoader();
            this.uniqueScriptId = new AtomicLong();
        }
        this.errors = errors;
        final String modulePath = env._module_path;
        ClassLoader appCl = null;
        if (!env._compile_only && modulePath != null && !modulePath.isEmpty()) {
            if (sm != null) {
                sm.checkCreateClassLoader();
            }
            appCl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return createModuleLoader(appLoader, modulePath, env._add_modules);
                }
            });
        } else {
            appCl = appLoader;
        }
        final String classPath = env._classpath;
        if (!env._compile_only && classPath != null && !classPath.isEmpty()) {
            if (sm != null) {
                sm.checkCreateClassLoader();
            }
            appCl = NashornLoader.createClassLoader(classPath, appCl);
        }
        this.appLoader = appCl;
        this.dynamicLinker = Bootstrap.createDynamicLinker(this.appLoader, env._unstable_relink_threshold);
        final int cacheSize = env._class_cache_size;
        if (cacheSize > 0) {
            classCache = new ClassCache(this, cacheSize);
        }
        if (env._persistent_cache) {
            codeStore = newCodeStore(this);
        }
        if (env._version) {
            getErr().println("nashorn " + Version.version());
        }
        if (env._fullversion) {
            getErr().println("nashorn full version " + Version.fullVersion());
        }
        if (Options.getBooleanProperty("nashorn.fields.dual")) {
            fieldMode = FieldMode.DUAL;
        } else if (Options.getBooleanProperty("nashorn.fields.objects")) {
            fieldMode = FieldMode.OBJECTS;
        } else {
            fieldMode = FieldMode.AUTO;
        }
        initLoggers();
    }

    public ClassFilter getClassFilter() {
        return classFilter;
    }

    GlobalConstants getGlobalConstants() {
        return globalConstantsRef.get();
    }

    public ErrorManager getErrorManager() {
        return errors;
    }

    public ScriptEnvironment getEnv() {
        return env;
    }

    public PrintWriter getOut() {
        return env.getOut();
    }

    public PrintWriter getErr() {
        return env.getErr();
    }

    public boolean useDualFields() {
        return fieldMode == FieldMode.DUAL || (fieldMode == FieldMode.AUTO && env._optimistic_types);
    }

    public static PropertyMap getGlobalMap() {
        return Context.getGlobal().getMap();
    }

    public ScriptFunction compileScript(final Source source, final ScriptObject scope) {
        return compileScript(source, scope, this.errors);
    }

    public static interface MultiGlobalCompiledScript {

        public ScriptFunction getFunction(final Global newGlobal);
    }

    public MultiGlobalCompiledScript compileScript(final Source source) {
        final Class<?> clazz = compile(source, this.errors, this._strict, false);
        final MethodHandle createProgramFunctionHandle = getCreateProgramFunctionHandle(clazz);
        return new MultiGlobalCompiledScript() {

            @Override
            public ScriptFunction getFunction(final Global newGlobal) {
                return invokeCreateProgramFunctionHandle(createProgramFunctionHandle, newGlobal);
            }
        };
    }

    public Object eval(final ScriptObject initialScope, final String string, final Object callThis, final Object location) {
        return eval(initialScope, string, callThis, location, false, false);
    }

    public Object eval(final ScriptObject initialScope, final String string, final Object callThis, final Object location, final boolean strict, final boolean evalCall) {
        final String file = location == UNDEFINED || location == null ? "<eval>" : location.toString();
        final Source source = sourceFor(file, string, evalCall);
        final boolean directEval = evalCall && (location != UNDEFINED);
        final Global global = Context.getGlobal();
        ScriptObject scope = initialScope;
        boolean strictFlag = strict || this._strict;
        Class<?> clazz;
        try {
            clazz = compile(source, new ThrowErrorManager(), strictFlag, true);
        } catch (final ParserException e) {
            e.throwAsEcmaException(global);
            return null;
        }
        if (!strictFlag) {
            try {
                strictFlag = clazz.getField(STRICT_MODE.symbolName()).getBoolean(null);
            } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                strictFlag = false;
            }
        }
        if (strictFlag) {
            scope = newScope(scope);
        }
        final ScriptFunction func = getProgramFunction(clazz, scope);
        Object evalThis;
        if (directEval) {
            evalThis = (callThis != UNDEFINED && callThis != null) || strictFlag ? callThis : global;
        } else {
            evalThis = callThis;
        }
        return ScriptRuntime.apply(func, evalThis);
    }

    private static ScriptObject newScope(final ScriptObject callerScope) {
        return new Scope(callerScope, PropertyMap.newMap(Scope.class));
    }

    private static Source loadInternal(final String srcStr, final String prefix, final String resourcePath) {
        if (srcStr.startsWith(prefix)) {
            final String resource = resourcePath + srcStr.substring(prefix.length());
            return AccessController.doPrivileged(new PrivilegedAction<Source>() {

                @Override
                public Source run() {
                    try {
                        final InputStream resStream = Context.class.getResourceAsStream(resource);
                        return resStream != null ? sourceFor(srcStr, Source.readFully(resStream)) : null;
                    } catch (final IOException exp) {
                        return null;
                    }
                }
            });
        }
        return null;
    }

    public Object load(final Object scope, final Object from) throws IOException {
        final Object src = from instanceof ConsString ? from.toString() : from;
        Source source = null;
        if (src instanceof String) {
            final String srcStr = (String) src;
            if (srcStr.startsWith(LOAD_CLASSPATH)) {
                final URL url = getResourceURL(srcStr.substring(LOAD_CLASSPATH.length()));
                source = url != null ? sourceFor(url.toString(), url) : null;
            } else {
                final File file = new File(srcStr);
                if (srcStr.indexOf(':') != -1) {
                    if ((source = loadInternal(srcStr, LOAD_NASHORN, "resources/")) == null && (source = loadInternal(srcStr, LOAD_FX, "resources/fx/")) == null) {
                        URL url;
                        try {
                            url = new URL(srcStr);
                        } catch (final MalformedURLException e) {
                            url = file.toURI().toURL();
                        }
                        source = sourceFor(url.toString(), url);
                    }
                } else if (file.isFile()) {
                    source = sourceFor(srcStr, file);
                }
            }
        } else if (src instanceof File && ((File) src).isFile()) {
            final File file = (File) src;
            source = sourceFor(file.getName(), file);
        } else if (src instanceof URL) {
            final URL url = (URL) src;
            source = sourceFor(url.toString(), url);
        } else if (src instanceof ScriptObject) {
            final ScriptObject sobj = (ScriptObject) src;
            if (sobj.has("script") && sobj.has("name")) {
                final String script = JSType.toString(sobj.get("script"));
                final String name = JSType.toString(sobj.get("name"));
                source = sourceFor(name, script);
            }
        } else if (src instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) src;
            if (map.containsKey("script") && map.containsKey("name")) {
                final String script = JSType.toString(map.get("script"));
                final String name = JSType.toString(map.get("name"));
                source = sourceFor(name, script);
            }
        }
        if (source != null) {
            if (scope instanceof ScriptObject && ((ScriptObject) scope).isScope()) {
                final ScriptObject sobj = (ScriptObject) scope;
                assert sobj.isGlobal() : "non-Global scope object!!";
                return evaluateSource(source, sobj, sobj);
            } else if (scope == null || scope == UNDEFINED) {
                final Global global = getGlobal();
                return evaluateSource(source, global, global);
            } else {
                final Global global = getGlobal();
                final ScriptObject evalScope = newScope(global);
                final ScriptObject withObj = ScriptRuntime.openWith(evalScope, scope);
                return evaluateSource(source, withObj, global);
            }
        }
        throw typeError("cant.load.script", ScriptRuntime.safeToString(from));
    }

    public Object loadWithNewGlobal(final Object from, final Object... args) throws IOException {
        final Global oldGlobal = getGlobal();
        final Global newGlobal = AccessController.doPrivileged(new PrivilegedAction<Global>() {

            @Override
            public Global run() {
                try {
                    return newGlobal();
                } catch (final RuntimeException e) {
                    if (Context.DEBUG) {
                        e.printStackTrace();
                    }
                    throw e;
                }
            }
        }, CREATE_GLOBAL_ACC_CTXT);
        initGlobal(newGlobal);
        setGlobal(newGlobal);
        final Object[] wrapped = args == null ? ScriptRuntime.EMPTY_ARRAY : ScriptObjectMirror.wrapArray(args, oldGlobal);
        newGlobal.put("arguments", newGlobal.wrapAsObject(wrapped), env._strict);
        try {
            return ScriptObjectMirror.unwrap(ScriptObjectMirror.wrap(load(newGlobal, from), newGlobal), oldGlobal);
        } finally {
            setGlobal(oldGlobal);
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ScriptObject> forStructureClass(final String fullName) throws ClassNotFoundException {
        if (System.getSecurityManager() != null && !StructureLoader.isStructureClass(fullName)) {
            throw new ClassNotFoundException(fullName);
        }
        return (Class<? extends ScriptObject>) structureClasses.computeIfAbsent(fullName, (name) -> {
            try {
                return Class.forName(name, true, theStructLoader);
            } catch (final ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        });
    }

    public static boolean isStructureClass(final String className) {
        return StructureLoader.isStructureClass(className);
    }

    public static void checkPackageAccess(final Class<?> clazz) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Class<?> bottomClazz = clazz;
            while (bottomClazz.isArray()) {
                bottomClazz = bottomClazz.getComponentType();
            }
            checkPackageAccess(sm, bottomClazz.getName());
        }
    }

    public static void checkPackageAccess(final String pkgName) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkPackageAccess(sm, pkgName.endsWith(".") ? pkgName : pkgName + ".");
        }
    }

    private static void checkPackageAccess(final SecurityManager sm, final String fullName) {
        Objects.requireNonNull(sm);
        final int index = fullName.lastIndexOf('.');
        if (index != -1) {
            final String pkgName = fullName.substring(0, index);
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    sm.checkPackageAccess(pkgName);
                    return null;
                }
            }, NO_PERMISSIONS_ACC_CTXT);
        }
    }

    private static boolean isAccessiblePackage(final Class<?> clazz) {
        try {
            checkPackageAccess(clazz);
            return true;
        } catch (final SecurityException se) {
            return false;
        }
    }

    public static boolean isAccessibleClass(final Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers()) && Context.isAccessiblePackage(clazz);
    }

    public Class<?> findClass(final String fullName) throws ClassNotFoundException {
        if (fullName.indexOf('[') != -1 || fullName.indexOf('/') != -1) {
            throw new ClassNotFoundException(fullName);
        }
        if (classFilter != null && !classFilter.exposeToScripts(fullName)) {
            throw new ClassNotFoundException(fullName);
        }
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkPackageAccess(sm, fullName);
        }
        if (appLoader != null) {
            return Class.forName(fullName, true, appLoader);
        } else {
            final Class<?> cl = Class.forName(fullName);
            if (cl.getClassLoader() == null) {
                return cl;
            } else {
                throw new ClassNotFoundException(fullName);
            }
        }
    }

    public static void printStackTrace(final Throwable t) {
        if (Context.DEBUG) {
            t.printStackTrace(Context.getCurrentErr());
        }
    }

    public void verify(final byte[] bytecode) {
        if (env._verify_code) {
            if (System.getSecurityManager() == null) {
                CheckClassAdapter.verify(new ClassReader(bytecode), theStructLoader, false, new PrintWriter(System.err, true));
            }
        }
    }

    public Global createGlobal() {
        return initGlobal(newGlobal());
    }

    public Global newGlobal() {
        createOrInvalidateGlobalConstants();
        return new Global(this);
    }

    private void createOrInvalidateGlobalConstants() {
        for (; ; ) {
            final GlobalConstants currentGlobalConstants = getGlobalConstants();
            if (currentGlobalConstants != null) {
                currentGlobalConstants.invalidateForever();
                return;
            }
            final GlobalConstants newGlobalConstants = new GlobalConstants(getLogger(GlobalConstants.class));
            if (globalConstantsRef.compareAndSet(null, newGlobalConstants)) {
                return;
            }
        }
    }

    public Global initGlobal(final Global global, final ScriptEngine engine) {
        if (!env._compile_only) {
            final Global oldGlobal = Context.getGlobal();
            try {
                Context.setGlobal(global);
                global.initBuiltinObjects(engine);
            } finally {
                Context.setGlobal(oldGlobal);
            }
        }
        return global;
    }

    public Global initGlobal(final Global global) {
        return initGlobal(global, null);
    }

    static Context getContextTrusted() {
        return getContext(getGlobal());
    }

    public static DynamicLinker getDynamicLinker(final Class<?> clazz) {
        return fromClass(clazz).dynamicLinker;
    }

    public static DynamicLinker getDynamicLinker() {
        return getContextTrusted().dynamicLinker;
    }

    static Module createModuleTrusted(final ModuleDescriptor descriptor, final ClassLoader loader) {
        return createModuleTrusted(ModuleLayer.boot(), descriptor, loader);
    }

    static Module createModuleTrusted(final ModuleLayer parent, final ModuleDescriptor descriptor, final ClassLoader loader) {
        final String mn = descriptor.name();
        final ModuleReference mref = new ModuleReference(descriptor, null) {

            @Override
            public ModuleReader open() {
                throw new UnsupportedOperationException();
            }
        };
        final ModuleFinder finder = new ModuleFinder() {

            @Override
            public Optional<ModuleReference> find(final String name) {
                if (name.equals(mn)) {
                    return Optional.of(mref);
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public Set<ModuleReference> findAll() {
                return Set.of(mref);
            }
        };
        final Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of(mn));
        final PrivilegedAction<ModuleLayer> pa = () -> parent.defineModules(cf, name -> loader);
        final ModuleLayer layer = AccessController.doPrivileged(pa, GET_LOADER_ACC_CTXT);
        final Module m = layer.findModule(mn).get();
        assert m.getLayer() == layer;
        return m;
    }

    static Context getContextTrustedOrNull() {
        final Global global = Context.getGlobal();
        return global == null ? null : getContext(global);
    }

    private static Context getContext(final Global global) {
        return ((ScriptObject) global).getContext();
    }

    static Context fromClass(final Class<?> clazz) {
        ClassLoader loader = null;
        try {
            loader = clazz.getClassLoader();
        } catch (final SecurityException ignored) {
        }
        if (loader instanceof ScriptLoader) {
            return ((ScriptLoader) loader).getContext();
        }
        return Context.getContextTrusted();
    }

    private URL getResourceURL(final String resName) {
        if (appLoader != null) {
            return appLoader.getResource(resName);
        }
        return ClassLoader.getSystemResource(resName);
    }

    private Object evaluateSource(final Source source, final ScriptObject scope, final ScriptObject thiz) {
        ScriptFunction script = null;
        try {
            script = compileScript(source, scope, new Context.ThrowErrorManager());
        } catch (final ParserException e) {
            e.throwAsEcmaException();
        }
        return ScriptRuntime.apply(script, thiz);
    }

    private static ScriptFunction getProgramFunction(final Class<?> script, final ScriptObject scope) {
        if (script == null) {
            return null;
        }
        return invokeCreateProgramFunctionHandle(getCreateProgramFunctionHandle(script), scope);
    }

    private static MethodHandle getCreateProgramFunctionHandle(final Class<?> script) {
        try {
            return LOOKUP.findStatic(script, CREATE_PROGRAM_FUNCTION.symbolName(), CREATE_PROGRAM_FUNCTION_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError("Failed to retrieve a handle for the program function for " + script.getName(), e);
        }
    }

    private static ScriptFunction invokeCreateProgramFunctionHandle(final MethodHandle createProgramFunctionHandle, final ScriptObject scope) {
        try {
            return (ScriptFunction) createProgramFunctionHandle.invokeExact(scope);
        } catch (final RuntimeException | Error e) {
            throw e;
        } catch (final Throwable t) {
            throw new AssertionError("Failed to create a program function", t);
        }
    }

    private ScriptFunction compileScript(final Source source, final ScriptObject scope, final ErrorManager errMan) {
        return getProgramFunction(compile(source, errMan, this._strict, false), scope);
    }

    private synchronized Class<?> compile(final Source source, final ErrorManager errMan, final boolean strict, final boolean isEval) {
        errMan.reset();
        Class<?> script = findCachedClass(source);
        if (script != null) {
            final DebugLogger log = getLogger(Compiler.class);
            if (log.isEnabled()) {
                log.fine(new RuntimeEvent<>(Level.INFO, source), "Code cache hit for ", source, " avoiding recompile.");
            }
            return script;
        }
        StoredScript storedScript = null;
        FunctionNode functionNode = null;
        final boolean useCodeStore = codeStore != null && !env._parse_only && (!env._optimistic_types || env._lazy_compilation);
        final String cacheKey = useCodeStore ? CodeStore.getCacheKey("script", null) : null;
        if (useCodeStore) {
            storedScript = codeStore.load(source, cacheKey);
        }
        if (storedScript == null) {
            if (env._dest_dir != null) {
                source.dump(env._dest_dir);
            }
            functionNode = new Parser(env, source, errMan, strict, getLogger(Parser.class)).parse();
            if (errMan.hasErrors()) {
                return null;
            }
            if (env._print_ast || functionNode.getDebugFlag(FunctionNode.DEBUG_PRINT_AST)) {
                getErr().println(new ASTWriter(functionNode));
            }
            if (env._print_parse || functionNode.getDebugFlag(FunctionNode.DEBUG_PRINT_PARSE)) {
                getErr().println(new PrintVisitor(functionNode, true, false));
            }
        }
        if (env._parse_only) {
            return null;
        }
        final URL url = source.getURL();
        final CodeSource cs = new CodeSource(url, (CodeSigner[]) null);
        final CodeInstaller installer;
        if (!env.useAnonymousClasses(source.getLength()) || env._persistent_cache || !env._lazy_compilation) {
            final ScriptLoader loader = env._loader_per_compile ? createNewLoader() : scriptLoader;
            installer = new NamedContextCodeInstaller(this, cs, loader);
        } else {
            installer = new AnonymousContextCodeInstaller(this, cs, anonymousHostClasses.getOrCreate(cs, (key) -> createNewLoader().installClass(AnonymousContextCodeInstaller.ANONYMOUS_HOST_CLASS_NAME, AnonymousContextCodeInstaller.ANONYMOUS_HOST_CLASS_BYTES, cs)));
        }
        if (storedScript == null) {
            final CompilationPhases phases = Compiler.CompilationPhases.COMPILE_ALL;
            final Compiler compiler = Compiler.forInitialCompilation(installer, source, errMan, strict | functionNode.isStrict());
            final FunctionNode compiledFunction = compiler.compile(functionNode, phases);
            if (errMan.hasErrors()) {
                return null;
            }
            script = compiledFunction.getRootClass();
            compiler.persistClassInfo(cacheKey, compiledFunction);
        } else {
            Compiler.updateCompilationId(storedScript.getCompilationId());
            script = storedScript.installScript(source, installer);
        }
        cacheClass(source, script);
        return script;
    }

    private ScriptLoader createNewLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptLoader>() {

            @Override
            public ScriptLoader run() {
                return new ScriptLoader(Context.this);
            }
        }, CREATE_LOADER_ACC_CTXT);
    }

    private long getUniqueScriptId() {
        return uniqueScriptId.getAndIncrement();
    }

    @SuppressWarnings("serial")
    @Logger(name = "classcache")
    private static class ClassCache extends LinkedHashMap<Source, ClassReference> implements Loggable {

        private final int size;

        private final ReferenceQueue<Class<?>> queue;

        private final DebugLogger log;

        ClassCache(final Context context, final int size) {
            super(size, 0.75f, true);
            this.size = size;
            this.queue = new ReferenceQueue<>();
            this.log = initLogger(context);
        }

        void cache(final Source source, final Class<?> clazz) {
            if (log.isEnabled()) {
                log.info("Caching ", source, " in class cache");
            }
            put(source, new ClassReference(clazz, queue, source));
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Source, ClassReference> eldest) {
            return size() > size;
        }

        @Override
        public ClassReference get(final Object key) {
            for (ClassReference ref; (ref = (ClassReference) queue.poll()) != null; ) {
                final Source source = ref.source;
                if (log.isEnabled()) {
                    log.info("Evicting ", source, " from class cache.");
                }
                remove(source);
            }
            final ClassReference ref = super.get(key);
            if (ref != null && log.isEnabled()) {
                log.info("Retrieved class reference for ", ref.source, " from class cache");
            }
            return ref;
        }

        @Override
        public DebugLogger initLogger(final Context context) {
            return context.getLogger(getClass());
        }

        @Override
        public DebugLogger getLogger() {
            return log;
        }
    }

    private static class ClassReference extends SoftReference<Class<?>> {

        private final Source source;

        ClassReference(final Class<?> clazz, final ReferenceQueue<Class<?>> queue, final Source source) {
            super(clazz, queue);
            this.source = source;
        }
    }

    private Class<?> findCachedClass(final Source source) {
        final ClassReference ref = classCache == null ? null : classCache.get(source);
        return ref != null ? ref.get() : null;
    }

    private void cacheClass(final Source source, final Class<?> clazz) {
        if (classCache != null) {
            classCache.cache(source, clazz);
        }
    }

    private final Map<String, DebugLogger> loggers = new HashMap<>();

    private void initLoggers() {
        ((Loggable) MethodHandleFactory.getFunctionality()).initLogger(this);
    }

    public DebugLogger getLogger(final Class<? extends Loggable> clazz) {
        return getLogger(clazz, null);
    }

    public DebugLogger getLogger(final Class<? extends Loggable> clazz, final Consumer<DebugLogger> initHook) {
        final String name = getLoggerName(clazz);
        DebugLogger logger = loggers.get(name);
        if (logger == null) {
            if (!env.hasLogger(name)) {
                return DebugLogger.DISABLED_LOGGER;
            }
            final LoggerInfo info = env._loggers.get(name);
            logger = new DebugLogger(name, info.getLevel(), info.isQuiet());
            if (initHook != null) {
                initHook.accept(logger);
            }
            loggers.put(name, logger);
        }
        return logger;
    }

    public MethodHandle addLoggingToHandle(final Class<? extends Loggable> clazz, final MethodHandle mh, final Supplier<String> text) {
        return addLoggingToHandle(clazz, Level.INFO, mh, Integer.MAX_VALUE, false, text);
    }

    public MethodHandle addLoggingToHandle(final Class<? extends Loggable> clazz, final Level level, final MethodHandle mh, final int paramStart, final boolean printReturnValue, final Supplier<String> text) {
        final DebugLogger log = getLogger(clazz);
        if (log.isEnabled()) {
            return MethodHandleFactory.addDebugPrintout(log, level, mh, paramStart, printReturnValue, text.get());
        }
        return mh;
    }

    private static String getLoggerName(final Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null) {
            final Logger log = current.getAnnotation(Logger.class);
            if (log != null) {
                assert !"".equals(log.name());
                return log.name();
            }
            current = current.getSuperclass();
        }
        assert false;
        return null;
    }

    public static final class BuiltinSwitchPoint extends SwitchPoint {
    }

    public SwitchPoint newBuiltinSwitchPoint(final String name) {
        assert builtinSwitchPoints.get(name) == null;
        final SwitchPoint sp = new BuiltinSwitchPoint();
        builtinSwitchPoints.put(name, sp);
        return sp;
    }

    public SwitchPoint getBuiltinSwitchPoint(final String name) {
        return builtinSwitchPoints.get(name);
    }

    private static ClassLoader createModuleLoader(final ClassLoader cl, final String modulePath, final String addModules) {
        if (addModules == null) {
            throw new IllegalArgumentException("--module-path specified with no --add-modules");
        }
        final Path[] paths = Stream.of(modulePath.split(File.pathSeparator)).map(s -> Paths.get(s)).toArray(sz -> new Path[sz]);
        final ModuleFinder mf = ModuleFinder.of(paths);
        final Set<ModuleReference> mrefs = mf.findAll();
        if (mrefs.isEmpty()) {
            throw new RuntimeException("No modules in script --module-path: " + modulePath);
        }
        final Set<String> rootMods;
        if (addModules.equals("ALL-MODULE-PATH")) {
            rootMods = mrefs.stream().map(mr -> mr.descriptor().name()).collect(Collectors.toSet());
        } else {
            rootMods = Stream.of(addModules.split(",")).map(String::trim).collect(Collectors.toSet());
        }
        final ModuleLayer boot = ModuleLayer.boot();
        final Configuration conf = boot.configuration().resolve(mf, ModuleFinder.of(), rootMods);
        final String firstMod = rootMods.iterator().next();
        return boot.defineModulesWithOneLoader(conf, cl).findLoader(firstMod);
    }
}