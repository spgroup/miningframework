package jdk.nashorn.internal.runtime;

import static jdk.nashorn.internal.codegen.CompilerConstants.RUN_SCRIPT;
import static jdk.nashorn.internal.codegen.CompilerConstants.STRICT_MODE;
import static jdk.nashorn.internal.lookup.Lookup.MH;
import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Map;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.codegen.Compiler;
import jdk.nashorn.internal.codegen.ObjectClassGenerator;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.debug.ASTWriter;
import jdk.nashorn.internal.ir.debug.PrintVisitor;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.options.Options;

public final class Context {

    public static final String NASHORN_SET_CONFIG = "nashorn.setConfig";

    public static final String NASHORN_CREATE_CONTEXT = "nashorn.createContext";

    public static final String NASHORN_CREATE_GLOBAL = "nashorn.createGlobal";

    public static final String NASHORN_GET_CONTEXT = "nashorn.getContext";

    public static final String NASHORN_JAVA_REFLECTION = "nashorn.JavaReflection";

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
        public Class<?> install(final String className, final byte[] bytecode) {
            return loader.installClass(className, bytecode, codeSource);
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
    }

    public static final boolean DEBUG = Options.getBooleanProperty("nashorn.debug");

    private static final ThreadLocal<ScriptObject> currentGlobal = new ThreadLocal<>();

    public static ScriptObject getGlobal() {
        return getGlobalTrusted();
    }

    public static void setGlobal(final ScriptObject global) {
        if (global != null && !(global instanceof Global)) {
            throw new IllegalArgumentException("global is not an instance of Global!");
        }
        setGlobalTrusted(global);
    }

    public static Context getContext() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission(NASHORN_GET_CONTEXT));
        }
        return getContextTrusted();
    }

    public static PrintWriter getCurrentErr() {
        final ScriptObject global = getGlobalTrusted();
        return (global != null) ? global.getContext().getErr() : new PrintWriter(System.err);
    }

    public static void err(final String str) {
        err(str, true);
    }

    @SuppressWarnings("resource")
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
        if (env._version) {
            getErr().println("nashorn " + Version.version());
        }
        if (env._fullversion) {
            getErr().println("nashorn full version " + Version.fullVersion());
        }
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
        return Context.getGlobalTrusted().getMap();
    }

    public ScriptFunction compileScript(final Source source, final ScriptObject scope) {
        return compileScript(source, scope, this.errors);
    }

    public Object eval(final ScriptObject initialScope, final String string, final Object callThis, final Object location, final boolean strict) {
        final String file = (location == UNDEFINED || location == null) ? "<eval>" : location.toString();
        final Source source = new Source(file, string);
        final boolean directEval = location != UNDEFINED;
        final ScriptObject global = Context.getGlobalTrusted();
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
            final ScriptObject strictEvalScope = ((GlobalObject) global).newObject();
            strictEvalScope.setIsScope();
            strictEvalScope.setProto(scope);
            scope = strictEvalScope;
        }
        ScriptFunction func = getRunScriptFunction(clazz, scope);
        Object evalThis;
        if (directEval) {
            evalThis = (callThis instanceof ScriptObject || strictFlag) ? callThis : global;
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
                        return (resURL != null) ? new Source(srcStr, resURL) : null;
                    } catch (final IOException exp) {
                        return null;
                    }
                }
            });
        }
        return null;
    }

    public Object load(final ScriptObject scope, final Object from) throws IOException {
        final Object src = (from instanceof ConsString) ? from.toString() : from;
        Source source = null;
        if (src instanceof String) {
            final String srcStr = (String) src;
            if (srcStr.startsWith(LOAD_CLASSPATH)) {
                URL url = getResourceURL(srcStr.substring(LOAD_CLASSPATH.length()));
                source = (url != null) ? new Source(url.toString(), url) : null;
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
                        source = new Source(url.toString(), url);
                    }
                } else if (file.isFile()) {
                    source = new Source(srcStr, file);
                }
            }
        } else if (src instanceof File && ((File) src).isFile()) {
            final File file = (File) src;
            source = new Source(file.getName(), file);
        } else if (src instanceof URL) {
            final URL url = (URL) src;
            source = new Source(url.toString(), url);
        } else if (src instanceof ScriptObject) {
            final ScriptObject sobj = (ScriptObject) src;
            if (sobj.has("script") && sobj.has("name")) {
                final String script = JSType.toString(sobj.get("script"));
                final String name = JSType.toString(sobj.get("name"));
                source = new Source(name, script);
            }
        } else if (src instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) src;
            if (map.containsKey("script") && map.containsKey("name")) {
                final String script = JSType.toString(map.get("script"));
                final String name = JSType.toString(map.get("name"));
                source = new Source(name, script);
            }
        }
        if (source != null) {
            return evaluateSource(source, scope, scope);
        }
        throw typeError("cant.load.script", ScriptRuntime.safeToString(from));
    }

    public Object loadWithNewGlobal(final Object from, final Object... args) throws IOException {
        final ScriptObject oldGlobal = getGlobalTrusted();
        final ScriptObject newGlobal = AccessController.doPrivileged(new PrivilegedAction<ScriptObject>() {

            @Override
            public ScriptObject run() {
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
        setGlobalTrusted(newGlobal);
        final Object[] wrapped = args == null ? ScriptRuntime.EMPTY_ARRAY : ScriptObjectMirror.wrapArray(args, oldGlobal);
        newGlobal.put("arguments", ((GlobalObject) newGlobal).wrapAsObject(wrapped), env._strict);
        try {
            return ScriptObjectMirror.unwrap(ScriptObjectMirror.wrap(load(newGlobal, from), newGlobal), oldGlobal);
        } finally {
            setGlobalTrusted(oldGlobal);
        }
    }

    public static Class<?> forStructureClass(final String fullName) throws ClassNotFoundException {
        if (System.getSecurityManager() != null && !StructureLoader.isStructureClass(fullName)) {
            throw new ClassNotFoundException(fullName);
        }
        return Class.forName(fullName, true, sharedLoader);
    }

<<<<<<< MINE
    public static void checkPackageAccess(final Class<?> clazz) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Class<?> bottomClazz = clazz;
=======
    public static void checkPackageAccess(final Class clazz) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Class bottomClazz = clazz;
>>>>>>> YOURS
            while (bottomClazz.isArray()) {
                bottomClazz = bottomClazz.getComponentType();
            }
            checkPackageAccess(sm, bottomClazz.getName());
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

<<<<<<< MINE
    private static boolean isAccessiblePackage(final Class<?> clazz) {
=======
    private static boolean isAccessiblePackage(final Class clazz) {
>>>>>>> YOURS
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

    public ScriptObject createGlobal() {
        return initGlobal(newGlobal());
    }

    public ScriptObject newGlobal() {
        return new Global(this);
    }

    public ScriptObject initGlobal(final ScriptObject global) {
        if (!(global instanceof GlobalObject)) {
            throw new IllegalArgumentException("not a global object!");
        }
        if (!env._compile_only) {
            final ScriptObject oldGlobal = Context.getGlobalTrusted();
            try {
                Context.setGlobalTrusted(global);
                ((GlobalObject) global).initBuiltinObjects();
            } finally {
                Context.setGlobalTrusted(oldGlobal);
            }
        }
        return global;
    }

    static ScriptObject getGlobalTrusted() {
        return currentGlobal.get();
    }

    static void setGlobalTrusted(ScriptObject global) {
        currentGlobal.set(global);
    }

    static Context getContextTrusted() {
        return Context.getGlobalTrusted().getContext();
    }

    static Context fromClass(final Class<?> clazz) {
        final ClassLoader loader = clazz.getClassLoader();
        if (loader instanceof ScriptLoader) {
            return ((ScriptLoader) loader).getContext();
        }
        return Context.getContextTrusted();
    }

<<<<<<< MINE
    private URL getResourceURL(final String resName) {
=======
    private URL getResourceURL(final String resName) throws IOException {
>>>>>>> YOURS
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

    private static ScriptFunction getRunScriptFunction(final Class<?> script, final ScriptObject scope) {
        if (script == null) {
            return null;
        }
        final MethodHandle runMethodHandle = MH.findStatic(MethodHandles.lookup(), script, RUN_SCRIPT.symbolName(), MH.type(Object.class, ScriptFunction.class, Object.class));
        boolean strict;
        try {
            strict = script.getField(STRICT_MODE.symbolName()).getBoolean(null);
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            strict = false;
        }
        return ((GlobalObject) Context.getGlobalTrusted()).newScriptFunction(RUN_SCRIPT.symbolName(), runMethodHandle, scope, strict);
    }

    private ScriptFunction compileScript(final Source source, final ScriptObject scope, final ErrorManager errMan) {
        return getRunScriptFunction(compile(source, errMan, this._strict), scope);
    }

    private synchronized Class<?> compile(final Source source, final ErrorManager errMan, final boolean strict) {
        errMan.reset();
        GlobalObject global = null;
        Class<?> script;
        if (env._class_cache_size > 0) {
            global = (GlobalObject) Context.getGlobalTrusted();
            script = global.findCachedClass(source);
            if (script != null) {
                Compiler.LOG.fine("Code cache hit for ", source, " avoiding recompile.");
                return script;
            }
        }
        final FunctionNode functionNode = new Parser(env, source, errMan, strict).parse();
        if (errors.hasErrors()) {
            return null;
        }
        if (env._print_ast) {
            getErr().println(new ASTWriter(functionNode));
        }
        if (env._print_parse) {
            getErr().println(new PrintVisitor(functionNode));
        }
        if (env._parse_only) {
            return null;
        }
        final URL url = source.getURL();
        final ScriptLoader loader = env._loader_per_compile ? createNewLoader() : scriptLoader;
        final CodeSource cs = url == null ? null : new CodeSource(url, (CodeSigner[]) null);
        final CodeInstaller<ScriptEnvironment> installer = new ContextCodeInstaller(this, loader, cs);
        final Compiler compiler = new Compiler(installer, strict);
        final FunctionNode newFunctionNode = compiler.compile(functionNode);
        script = compiler.install(newFunctionNode);
        if (global != null) {
            global.cacheClass(source, script);
        }
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
}