package jdk.nashorn.internal.runtime;

<<<<<<< MINE
import static jdk.nashorn.internal.codegen.CompilerConstants.CREATE_PROGRAM_FUNCTION;
=======
import static jdk.nashorn.internal.codegen.CompilerConstants.CONSTANTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.RUN_SCRIPT;
import static jdk.nashorn.internal.codegen.CompilerConstants.SOURCE;
>>>>>>> YOURS
import static jdk.nashorn.internal.codegen.CompilerConstants.STRICT_MODE;
import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import static jdk.nashorn.internal.runtime.Source.sourceFor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
<<<<<<< MINE
import java.lang.reflect.InvocationTargetException;
=======
import java.lang.reflect.Field;
>>>>>>> YOURS
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Level;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
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
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import jdk.nashorn.internal.runtime.logging.Loggable;
import jdk.nashorn.internal.runtime.logging.Logger;
import jdk.nashorn.internal.runtime.options.Options;
import jdk.nashorn.internal.runtime.options.LoggingOption.LoggerInfo;

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

    static {
        DebuggerSupport.FORCELOAD = true;
    }

    public static class ContextCodeInstaller implements CodeInstaller<ScriptEnvironment> {

        private final Context context;

        private final ScriptLoader loader;

        private final CodeSource codeSource;

        private ContextCodeInstaller(final Context context, final ScriptLoader loader, final CodeSource codeSource) {
            this.context = context;
            this.loader = loader;
            this.codeSource = codeSource;
        }

        @Override
        public ScriptEnvironment getOwner() {
            return context.env;
        }

        @Override
        public Class<?> install(final String className, final byte[] bytecode, final Source source, final Object[] constants) {
            Compiler.LOG.fine("Installing class ", className);
            final String binaryName = Compiler.binaryName(className);
            final Class<?> clazz = loader.installClass(binaryName, bytecode, codeSource);
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                    @Override
                    public Void run() throws Exception {
                        final Field sourceField = clazz.getDeclaredField(SOURCE.symbolName());
                        final Field constantsField = clazz.getDeclaredField(CONSTANTS.symbolName());
                        sourceField.setAccessible(true);
                        constantsField.setAccessible(true);
                        sourceField.set(null, source);
                        constantsField.set(null, constants);
                        return null;
                    }
                });
            } catch (final PrivilegedActionException e) {
                throw new RuntimeException(e);
            }
            return clazz;
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
        public long getUniqueEvalId() {
            return context.getUniqueEvalId();
        }

        @Override
        public void storeCompiledScript(final Source source, final String mainClassName, final Map<String, byte[]> classBytes, final Object[] constants) {
            if (context.codeStore != null) {
                try {
                    context.codeStore.putScript(source, mainClassName, classBytes, constants);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static final boolean DEBUG = Options.getBooleanProperty("nashorn.debug");

    private static final ThreadLocal<Global> currentGlobal = new ThreadLocal<>();

    private ClassCache classCache;

    private CodeStore codeStore;

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
            Global.getConstants().invalidateAll();
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

    private final ClassLoader classPathLoader;

    private final ScriptLoader scriptLoader;

    private final ErrorManager errors;

    private final AtomicLong uniqueScriptId;

    private final AtomicLong uniqueEvalId;

    private static final ClassLoader myLoader = Context.class.getClassLoader();

    private static final StructureLoader sharedLoader;

    @SuppressWarnings("static-method")
    ClassLoader getSharedLoader() {
        return sharedLoader;
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

    static {
        sharedLoader = AccessController.doPrivileged(new PrivilegedAction<StructureLoader>() {

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
        this(options, errors, new PrintWriter(System.out, true), new PrintWriter(System.err, true), appLoader);
    }

    public Context(final Options options, final ErrorManager errors, final PrintWriter out, final PrintWriter err, final ClassLoader appLoader) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission(NASHORN_CREATE_CONTEXT));
        }
        this.env = new ScriptEnvironment(options, out, err);
        this._strict = env._strict;
        this.appLoader = appLoader;
        if (env._loader_per_compile) {
            this.scriptLoader = null;
            this.uniqueScriptId = null;
        } else {
            this.scriptLoader = createNewLoader();
            this.uniqueScriptId = new AtomicLong();
        }
        this.errors = errors;
        this.uniqueEvalId = new AtomicLong();
        final String classPath = options.getString("classpath");
        if (!env._compile_only && classPath != null && !classPath.isEmpty()) {
            if (sm != null) {
                sm.checkPermission(new RuntimePermission("createClassLoader"));
            }
            this.classPathLoader = NashornLoader.createClassLoader(classPath);
        } else {
            this.classPathLoader = null;
        }
        final int cacheSize = env._class_cache_size;
        if (cacheSize > 0) {
            classCache = new ClassCache(cacheSize);
        }
        if (env._persistent_cache) {
            if (env._lazy_compilation || env._specialize_calls != null) {
                getErr().println("Can not use persistent class caching with lazy compilation or call specialization.");
            } else {
                try {
                    final String cacheDir = Options.getStringProperty("nashorn.persistent.code.cache", "nashorn_code_cache");
                    codeStore = new CodeStore(cacheDir);
                } catch (IOException e) {
                    throw new RuntimeException("Error initializing code cache", e);
                }
            }
        }
        if (env._version) {
            getErr().println("nashorn " + Version.version());
        }
        if (env._fullversion) {
            getErr().println("nashorn full version " + Version.fullVersion());
        }
        initLoggers();
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
        final Class<?> clazz = compile(source, this.errors, this._strict);
        final MethodHandle runMethodHandle = getRunScriptHandle(clazz);
        final boolean strict = isStrict(clazz);
        return new MultiGlobalCompiledScript() {

            @Override
            public ScriptFunction getFunction(final Global newGlobal) {
                return Context.getGlobal().newScriptFunction(RUN_SCRIPT.symbolName(), runMethodHandle, newGlobal, strict);
            }
        };
    }

    public Object eval(final ScriptObject initialScope, final String string, final Object callThis, final Object location, final boolean strict) {
<<<<<<< MINE
        final String file = location == UNDEFINED || location == null ? "<eval>" : location.toString();
        final Source source = new Source(file, string);
=======
        final String file = (location == UNDEFINED || location == null) ? "<eval>" : location.toString();
        final Source source = sourceFor(file, string);
>>>>>>> YOURS
        final boolean directEval = location != UNDEFINED;
        final Global global = Context.getGlobal();
        ScriptObject scope = initialScope;
        boolean strictFlag = directEval && strict;
        Class<?> clazz = null;
        try {
            clazz = compile(source, new ThrowErrorManager(), strictFlag);
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
            final ScriptObject strictEvalScope = global.newObject();
            strictEvalScope.setIsScope();
            strictEvalScope.setProto(scope);
            scope = strictEvalScope;
        }
        final ScriptFunction func = getProgramFunction(clazz, scope);
        Object evalThis;
        if (directEval) {
            evalThis = callThis instanceof ScriptObject || strictFlag ? callThis : global;
        } else {
            evalThis = global;
        }
        return ScriptRuntime.apply(func, evalThis);
    }

    private static Source loadInternal(final String srcStr, final String prefix, final String resourcePath) {
        if (srcStr.startsWith(prefix)) {
            final String resource = resourcePath + srcStr.substring(prefix.length());
            return AccessController.doPrivileged(new PrivilegedAction<Source>() {

                @Override
                public Source run() {
                    try {
                        final URL resURL = Context.class.getResource(resource);
<<<<<<< MINE
                        return resURL != null ? new Source(srcStr, resURL) : null;
=======
                        return (resURL != null) ? sourceFor(srcStr, resURL) : null;
>>>>>>> YOURS
                    } catch (final IOException exp) {
                        return null;
                    }
                }
            });
        }
        return null;
    }

    public Object load(final ScriptObject scope, final Object from) throws IOException {
        final Object src = from instanceof ConsString ? from.toString() : from;
        Source source = null;
        if (src instanceof String) {
            final String srcStr = (String) src;
            if (srcStr.startsWith(LOAD_CLASSPATH)) {
<<<<<<< MINE
                final URL url = getResourceURL(srcStr.substring(LOAD_CLASSPATH.length()));
                source = url != null ? new Source(url.toString(), url) : null;
=======
                URL url = getResourceURL(srcStr.substring(LOAD_CLASSPATH.length()));
                source = (url != null) ? sourceFor(url.toString(), url) : null;
>>>>>>> YOURS
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
            return evaluateSource(source, scope, scope);
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
        return (Class<? extends ScriptObject>) Class.forName(fullName, true, sharedLoader);
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
        sm.getClass();
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
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkPackageAccess(sm, fullName);
        }
        if (classPathLoader != null) {
            try {
                return Class.forName(fullName, true, classPathLoader);
            } catch (final ClassNotFoundException ignored) {
            }
        }
        return Class.forName(fullName, true, appLoader);
    }

    public static void printStackTrace(final Throwable t) {
        if (Context.DEBUG) {
            t.printStackTrace(Context.getCurrentErr());
        }
    }

    public void verify(final byte[] bytecode) {
        if (env._verify_code) {
            if (System.getSecurityManager() == null) {
                CheckClassAdapter.verify(new ClassReader(bytecode), sharedLoader, false, new PrintWriter(System.err, true));
            }
        }
    }

    public Global createGlobal() {
        return initGlobal(newGlobal());
    }

    public Global newGlobal() {
        return new Global(this);
    }

    public Global initGlobal(final Global global) {
        if (!env._compile_only) {
            final Global oldGlobal = Context.getGlobal();
            try {
                Context.setGlobal(global);
                global.initBuiltinObjects();
            } finally {
                Context.setGlobal(oldGlobal);
            }
        }
        return global;
    }

    static Context getContextTrusted() {
        return ((ScriptObject) Context.getGlobal()).getContext();
    }

    static Context fromClass(final Class<?> clazz) {
        final ClassLoader loader = clazz.getClassLoader();
        if (loader instanceof ScriptLoader) {
            return ((ScriptLoader) loader).getContext();
        }
        return Context.getContextTrusted();
    }

    private URL getResourceURL(final String resName) {
        if (classPathLoader != null) {
            return classPathLoader.getResource(resName);
        } else if (appLoader != null) {
            return appLoader.getResource(resName);
        }
        return null;
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

<<<<<<< MINE
    private static ScriptFunction getProgramFunction(final Class<?> script, final ScriptObject scope) {
        if (script == null) {
            return null;
        }
        try {
            return (ScriptFunction) script.getMethod(CREATE_PROGRAM_FUNCTION.symbolName(), ScriptObject.class).invoke(null, scope);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Failed to create a program function for " + script.getName(), e);
        }
=======
    private static MethodHandle getRunScriptHandle(final Class<?> script) {
        return MH.findStatic(MethodHandles.lookup(), script, RUN_SCRIPT.symbolName(), MH.type(Object.class, ScriptFunction.class, Object.class));
    }

    private static boolean isStrict(final Class<?> script) {
        try {
            return script.getField(STRICT_MODE.symbolName()).getBoolean(null);
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return false;
        }
    }

    private static ScriptFunction getRunScriptFunction(final Class<?> script, final ScriptObject scope) {
        if (script == null) {
            return null;
        }
        final MethodHandle runMethodHandle = getRunScriptHandle(script);
        boolean strict = isStrict(script);
        return Context.getGlobal().newScriptFunction(RUN_SCRIPT.symbolName(), runMethodHandle, scope, strict);
>>>>>>> YOURS
    }

    private ScriptFunction compileScript(final Source source, final ScriptObject scope, final ErrorManager errMan) {
        return getProgramFunction(compile(source, errMan, this._strict), scope);
    }

    private synchronized Class<?> compile(final Source source, final ErrorManager errMan, final boolean strict) {
        errMan.reset();
        Class<?> script = findCachedClass(source);
        if (script != null) {
            final DebugLogger log = getLogger(Compiler.class);
            if (log.isEnabled()) {
                log.fine(new RuntimeEvent<>(Level.INFO, source), "Code cache hit for ", source, " avoiding recompile.");
            }
            return script;
        }
<<<<<<< MINE
        final FunctionNode functionNode = new Parser(env, source, errMan, strict, getLogger(Parser.class)).parse();
        if (errors.hasErrors()) {
            return null;
        }
        if (env._print_ast) {
            getErr().println(new ASTWriter(functionNode));
        }
        if (env._print_parse) {
            getErr().println(new PrintVisitor(functionNode, true, false));
=======
        CompiledScript compiledScript = null;
        FunctionNode functionNode = null;
        if (!env._parse_only && codeStore != null) {
            try {
                compiledScript = codeStore.getScript(source);
            } catch (IOException | ClassNotFoundException e) {
                Compiler.LOG.warning("Error loading ", source, " from cache: ", e);
            }
        }
        if (compiledScript == null) {
            functionNode = new Parser(env, source, errMan, strict).parse();
            if (errors.hasErrors()) {
                return null;
            }
            if (env._print_ast) {
                getErr().println(new ASTWriter(functionNode));
            }
            if (env._print_parse) {
                getErr().println(new PrintVisitor(functionNode));
            }
>>>>>>> YOURS
        }
        if (env._parse_only) {
            return null;
        }
        final URL url = source.getURL();
        final ScriptLoader loader = env._loader_per_compile ? createNewLoader() : scriptLoader;
        final CodeSource cs = new CodeSource(url, (CodeSigner[]) null);
        final CodeInstaller<ScriptEnvironment> installer = new ContextCodeInstaller(this, loader, cs);
<<<<<<< MINE
        final CompilationPhases phases = Compiler.CompilationPhases.COMPILE_ALL;
        final Compiler compiler = new Compiler(this, env, installer, source, functionNode.getSourceURL(), strict | functionNode.isStrict());
        script = compiler.compile(functionNode, phases).getRootClass();
=======
        if (functionNode != null) {
            final Compiler compiler = new Compiler(installer, strict);
            final FunctionNode newFunctionNode = compiler.compile(functionNode);
            script = compiler.install(newFunctionNode);
        } else {
            script = install(compiledScript, installer);
        }
>>>>>>> YOURS
        cacheClass(source, script);
        return script;
    }

    private ScriptLoader createNewLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ScriptLoader>() {

            @Override
            public ScriptLoader run() {
                return new ScriptLoader(appLoader, Context.this);
            }
        }, CREATE_LOADER_ACC_CTXT);
    }

    private long getUniqueEvalId() {
        return uniqueEvalId.getAndIncrement();
    }

    private long getUniqueScriptId() {
        return uniqueScriptId.getAndIncrement();
    }

    private Class<?> install(final CompiledScript compiledScript, final CodeInstaller<ScriptEnvironment> installer) {
        final Map<String, Class<?>> installedClasses = new HashMap<>();
        final Source source = compiledScript.getSource();
        final Object[] constants = compiledScript.getConstants();
        final String rootClassName = compiledScript.getMainClassName();
        final byte[] rootByteCode = compiledScript.getClassBytes().get(rootClassName);
        final Class<?> rootClass = installer.install(rootClassName, rootByteCode, source, constants);
        installedClasses.put(rootClassName, rootClass);
        for (final Map.Entry<String, byte[]> entry : compiledScript.getClassBytes().entrySet()) {
            final String className = entry.getKey();
            if (className.equals(rootClassName)) {
                continue;
            }
            final byte[] code = entry.getValue();
            installedClasses.put(className, installer.install(className, code, source, constants));
        }
        for (Object constant : constants) {
            if (constant instanceof RecompilableScriptFunctionData) {
                ((RecompilableScriptFunctionData) constant).setCodeAndSource(installedClasses, source);
            }
        }
        return rootClass;
    }

    @SuppressWarnings("serial")
    private static class ClassCache extends LinkedHashMap<Source, ClassReference> {

        private final int size;

        private final ReferenceQueue<Class<?>> queue;

        ClassCache(final int size) {
            super(size, 0.75f, true);
            this.size = size;
            this.queue = new ReferenceQueue<>();
        }

        void cache(final Source source, final Class<?> clazz) {
            put(source, new ClassReference(clazz, queue, source));
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Source, ClassReference> eldest) {
            return size() > size;
        }

        @Override
        public ClassReference get(final Object key) {
            for (ClassReference ref; (ref = (ClassReference) queue.poll()) != null; ) {
                remove(ref.source);
            }
            return super.get(key);
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
        final String name = getLoggerName(clazz);
        DebugLogger logger = loggers.get(name);
        if (logger == null) {
            if (!env.hasLogger(name)) {
                return DebugLogger.DISABLED_LOGGER;
            }
            final LoggerInfo info = env._loggers.get(name);
            logger = new DebugLogger(name, info.getLevel(), info.isQuiet());
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
}
