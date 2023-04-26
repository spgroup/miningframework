package org.graalvm.compiler.truffle.runtime.hotspot;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.graalvm.compiler.truffle.common.CompilableTruffleAST;
import org.graalvm.compiler.truffle.common.TruffleCompiler;
import org.graalvm.compiler.truffle.common.hotspot.HotSpotTruffleCompiler;
import org.graalvm.compiler.truffle.common.hotspot.HotSpotTruffleCompilerRuntime;
import org.graalvm.compiler.truffle.options.PolyglotCompilerOptions;
import org.graalvm.compiler.truffle.runtime.BackgroundCompileQueue;
import org.graalvm.compiler.truffle.runtime.CompilationTask;
import org.graalvm.compiler.truffle.runtime.EngineData;
import org.graalvm.compiler.truffle.runtime.GraalTruffleRuntime;
import org.graalvm.compiler.truffle.runtime.OptimizedCallTarget;
import org.graalvm.compiler.truffle.runtime.OptimizedOSRLoopNode;
import org.graalvm.compiler.truffle.runtime.TruffleCallBoundary;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.code.stack.StackIntrospection;
import jdk.vm.ci.common.JVMCIError;
import jdk.vm.ci.hotspot.HotSpotConstantReflectionProvider;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.hotspot.HotSpotMetaAccessProvider;
import jdk.vm.ci.hotspot.HotSpotObjectConstant;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;
import jdk.vm.ci.hotspot.HotSpotSpeculationLog;
import jdk.vm.ci.hotspot.HotSpotVMConfigAccess;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaField;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.SpeculationLog;
import jdk.vm.ci.runtime.JVMCI;
import sun.misc.Unsafe;

public abstract class AbstractHotSpotTruffleRuntime extends GraalTruffleRuntime implements HotSpotTruffleCompilerRuntime {

    private static final sun.misc.Unsafe UNSAFE = getUnsafe();

    private static volatile HotSpotVMConfigAccess vmConfigAccess;

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException e) {
        }
        try {
            Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeInstance.setAccessible(true);
            return (Unsafe) theUnsafeInstance.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("exception while trying to get Unsafe.theUnsafe via reflection:", e);
        }
    }

    static final class Lazy extends BackgroundCompileQueue {

        StackIntrospection stackIntrospection;

        Lazy(AbstractHotSpotTruffleRuntime runtime) {
            super(runtime);
            runtime.installDefaultListeners();
        }

        @Override
        protected void compilerThreadIdled() {
            TruffleCompiler compiler = ((AbstractHotSpotTruffleRuntime) runtime).truffleCompiler;
            if (compiler != null) {
                ((HotSpotTruffleCompiler) compiler).purgeCaches();
            }
        }
    }

    private volatile boolean traceTransferToInterpreter;

    private Boolean profilingEnabled;

    private volatile Lazy lazy;

    private volatile String lazyConfigurationName;

    private Lazy lazy() {
        if (lazy == null) {
            synchronized (this) {
                if (lazy == null) {
                    lazy = new Lazy(this);
                }
            }
        }
        return lazy;
    }

    private final List<ResolvedJavaMethod> truffleCallBoundaryMethods;

    private volatile CompilationTask initializationTask;

    private volatile boolean truffleCompilerInitialized;

    private volatile Throwable truffleCompilerInitializationException;

    public AbstractHotSpotTruffleRuntime() {
        super(Arrays.asList(HotSpotOptimizedCallTarget.class, InstalledCode.class));
        List<ResolvedJavaMethod> boundaryMethods = new ArrayList<>();
        MetaAccessProvider metaAccess = getMetaAccess();
        ResolvedJavaType type = metaAccess.lookupJavaType(OptimizedCallTarget.class);
        for (ResolvedJavaMethod method : type.getDeclaredMethods()) {
            if (method.getAnnotation(TruffleCallBoundary.class) != null) {
                boundaryMethods.add(method);
            }
        }
        this.truffleCallBoundaryMethods = boundaryMethods;
        setDontInlineCallBoundaryMethod(boundaryMethods);
    }

    @Override
    public final Iterable<ResolvedJavaMethod> getTruffleCallBoundaryMethods() {
        return truffleCallBoundaryMethods;
    }

    @Override
    protected StackIntrospection getStackIntrospection() {
        Lazy l = lazy();
        if (l.stackIntrospection == null) {
            l.stackIntrospection = HotSpotJVMCIRuntime.runtime().getHostJVMCIBackend().getStackIntrospection();
        }
        return l.stackIntrospection;
    }

    @Override
    public HotSpotTruffleCompiler getTruffleCompiler(CompilableTruffleAST compilable) {
        Objects.requireNonNull(compilable, "Compilable must be non null.");
        if (truffleCompiler == null) {
            initializeTruffleCompiler((OptimizedCallTarget) compilable);
            rethrowTruffleCompilerInitializationException();
            assert truffleCompiler != null : "TruffleCompiler must be non null";
        }
        return (HotSpotTruffleCompiler) truffleCompiler;
    }

    private void ensureInitialized(OptimizedCallTarget firstCallTarget) {
        if (truffleCompilerInitialized) {
            return;
        }
        CompilationTask localTask = initializationTask;
        if (localTask == null) {
            final Object lock = this;
            synchronized (lock) {
                localTask = initializationTask;
                if (localTask == null && !truffleCompilerInitialized) {
                    rethrowTruffleCompilerInitializationException();
                    initializationTask = localTask = getCompileQueue().submitInitialization(firstCallTarget, new Consumer<CompilationTask>() {

                        @Override
                        public void accept(CompilationTask task) {
                            synchronized (lock) {
                                initializeTruffleCompiler(firstCallTarget);
                                assert truffleCompilerInitialized || truffleCompilerInitializationException != null;
                                assert initializationTask != null;
                                initializationTask = null;
                            }
                        }
                    });
                }
            }
        }
        if (localTask != null) {
            firstCallTarget.maybeWaitForTask(localTask);
            rethrowTruffleCompilerInitializationException();
        } else {
            assert truffleCompilerInitialized || truffleCompilerInitializationException != null;
        }
    }

    public final void resetCompiler() {
        truffleCompiler = null;
        truffleCompilerInitialized = false;
        truffleCompilerInitializationException = null;
    }

    private synchronized void initializeTruffleCompiler(OptimizedCallTarget callTarget) {
        if (!truffleCompilerInitialized) {
            rethrowTruffleCompilerInitializationException();
            try {
                EngineData engineData = callTarget.engine;
                profilingEnabled = engineData.profilingEnabled;
                TruffleCompiler compiler = newTruffleCompiler();
                compiler.initialize(getOptionsForCompiler(callTarget), callTarget, true);
                truffleCompiler = compiler;
                traceTransferToInterpreter = engineData.traceTransferToInterpreter;
                truffleCompilerInitialized = true;
            } catch (Throwable e) {
                truffleCompilerInitializationException = e;
            }
        }
    }

    private void rethrowTruffleCompilerInitializationException() {
        if (truffleCompilerInitializationException != null) {
            throw sthrow(RuntimeException.class, truffleCompilerInitializationException);
        }
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private static <T extends Throwable> T sthrow(Class<T> type, Throwable t) throws T {
        throw (T) t;
    }

    @Override
    public final OptimizedCallTarget createOptimizedCallTarget(OptimizedCallTarget source, RootNode rootNode) {
        OptimizedCallTarget target = new HotSpotOptimizedCallTarget(source, rootNode);
        ensureInitialized(target);
        return target;
    }

    @Override
    public void onCodeInstallation(CompilableTruffleAST compilable, InstalledCode installedCode) {
        HotSpotOptimizedCallTarget callTarget = (HotSpotOptimizedCallTarget) compilable;
        callTarget.setInstalledCode(installedCode);
    }

    @Override
    public SpeculationLog createSpeculationLog() {
        return new HotSpotSpeculationLog();
    }

    public static void setDontInlineCallBoundaryMethod(List<ResolvedJavaMethod> callBoundaryMethods) {
        for (ResolvedJavaMethod method : callBoundaryMethods) {
            setNotInlinableOrCompilable(method);
        }
    }

    static MetaAccessProvider getMetaAccess() {
        return JVMCI.getRuntime().getHostJVMCIBackend().getMetaAccess();
    }

    private static void setNotInlinableOrCompilable(ResolvedJavaMethod method) {
        Method[] methods = HotSpotResolvedJavaMethod.class.getMethods();
        for (Method m : methods) {
            if (m.getName().equals("setNotInlineable") || m.getName().equals("setNotInlinableOrCompilable") || m.getName().equals("setNotInlineableOrCompileable")) {
                try {
                    m.invoke(method);
                    return;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new InternalError(e);
                }
            }
        }
        throw new InternalError(String.format("Could not find setNotInlineable, setNotInlinableOrCompilable or setNotInlineableOrCompileable in %s", HotSpotResolvedJavaMethod.class));
    }

    @Override
    public BackgroundCompileQueue getCompileQueue() {
        return lazy();
    }

    @Override
    protected String getCompilerConfigurationName() {
        TruffleCompiler compiler = truffleCompiler;
        String compilerConfig;
        if (compiler != null) {
            compilerConfig = compiler.getCompilerConfigurationName();
        } else {
            compilerConfig = getLazyCompilerConfigurationName();
        }
        return compilerConfig;
    }

    @SuppressWarnings("unused")
    private boolean verifyCompilerConfiguration(String name) {
        String lazyName = getLazyCompilerConfigurationName();
        if (!name.equals(lazyName)) {
            throw new AssertionError("Expected compiler configuration name " + name + " but was " + lazyName + ".");
        }
        return true;
    }

    private String getLazyCompilerConfigurationName() {
        String compilerConfig = this.lazyConfigurationName;
        if (compilerConfig == null) {
            synchronized (this) {
                compilerConfig = this.lazyConfigurationName;
                if (compilerConfig == null) {
                    compilerConfig = initLazyCompilerConfigurationName();
                    this.lazyConfigurationName = compilerConfig;
                }
            }
        }
        return compilerConfig;
    }

    protected abstract String initLazyCompilerConfigurationName();

    @SuppressWarnings("try")
    @Override
    public void bypassedInstalledCode(OptimizedCallTarget target) {
        if (!truffleCompilerInitialized) {
            return;
        }
        getTruffleCompiler(target).installTruffleCallBoundaryMethods(target);
    }

    @Override
    protected CallMethods getCallMethods() {
        if (callMethods == null) {
            lookupCallMethods(getMetaAccess());
        }
        return callMethods;
    }

    @Override
    public void notifyTransferToInterpreter() {
        CompilerAsserts.neverPartOfCompilation();
        if (traceTransferToInterpreter) {
            TruffleCompiler compiler = truffleCompiler;
            assert compiler != null;
            TraceTransferToInterpreterHelper.traceTransferToInterpreter(this, (HotSpotTruffleCompiler) compiler);
        }
    }

    @Override
    public final boolean isProfilingEnabled() {
        if (profilingEnabled == null) {
            return true;
        }
        return profilingEnabled;
    }

    @Override
    protected JavaConstant forObject(final Object object) {
        final HotSpotConstantReflectionProvider constantReflection = (HotSpotConstantReflectionProvider) HotSpotJVMCIRuntime.runtime().getHostJVMCIBackend().getConstantReflection();
        return constantReflection.forObject(object);
    }

    @Override
    protected int getBaseInstanceSize(Class<?> type) {
        if (type.isArray() || type.isPrimitive()) {
            throw new IllegalArgumentException("Class " + type.getName() + " is a primitive type or an array class!");
        }
        HotSpotMetaAccessProvider meta = (HotSpotMetaAccessProvider) getMetaAccess();
        HotSpotResolvedObjectType resolvedType = (HotSpotResolvedObjectType) meta.lookupJavaType(type);
        return resolvedType.instanceSize();
    }

    private static boolean fieldIsNotEligible(Class<?> clazz, ResolvedJavaField f) {
        return (Reference.class.isAssignableFrom(clazz) && f.getName().equals("discovered") && f.getDeclaringClass().isAssignableFrom(getMetaAccess().lookupJavaType(Reference.class)));
    }

    @Override
    protected Object[] getNonPrimitiveResolvedFields(Class<?> type) {
        if (type.isArray() || type.isPrimitive()) {
            throw new IllegalArgumentException("Class " + type.getName() + " is a primitive type or an array class!");
        }
        HotSpotMetaAccessProvider meta = (HotSpotMetaAccessProvider) getMetaAccess();
        ResolvedJavaType javaType = meta.lookupJavaType(type);
        ResolvedJavaField[] fields = javaType.getInstanceFields(true);
        ResolvedJavaField[] fieldsToReturn = new ResolvedJavaField[fields.length];
        int fieldsCount = 0;
        for (int i = 0; i < fields.length; i++) {
            final ResolvedJavaField f = fields[i];
            if (!f.getJavaKind().isPrimitive() && !fieldIsNotEligible(type, f)) {
                fieldsToReturn[fieldsCount++] = f;
            }
        }
        return Arrays.copyOf(fieldsToReturn, fieldsCount);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Object getFieldValue(ResolvedJavaField resolvedJavaField, Object obj) {
        assert obj != null;
        assert !resolvedJavaField.isStatic();
        assert resolvedJavaField.getJavaKind() == JavaKind.Object;
        assert resolvedJavaField.getDeclaringClass().isAssignableFrom(getMetaAccess().lookupJavaType(obj.getClass()));
        Object value;
        if (resolvedJavaField.isVolatile()) {
            value = UNSAFE.getObjectVolatile(obj, resolvedJavaField.getOffset());
        } else {
            value = UNSAFE.getObject(obj, resolvedJavaField.getOffset());
        }
        return value;
    }

    private static <T> T getVMOptionValue(String name, Class<T> type) {
        HotSpotVMConfigAccess vmConfig = vmConfigAccess;
        if (vmConfig == null) {
            vmConfig = new HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime().getConfigStore());
            vmConfigAccess = vmConfig;
        }
        try {
            return vmConfig.getFlag(name, type);
        } catch (JVMCIError jvmciError) {
            throw new IllegalArgumentException(jvmciError);
        }
    }

    @Override
    protected int getObjectAlignment() {
        return getVMOptionValue("ObjectAlignmentInBytes", Integer.class);
    }

    @Override
    protected int getArrayIndexScale(Class<?> componentType) {
        MetaAccessProvider meta = getMetaAccess();
        ResolvedJavaType resolvedType = meta.lookupJavaType(componentType);
        return ((HotSpotJVMCIRuntime) JVMCI.getRuntime()).getArrayIndexScale(resolvedType.getJavaKind());
    }

    @Override
    protected int getArrayBaseOffset(Class<?> componentType) {
        MetaAccessProvider meta = getMetaAccess();
        ResolvedJavaType resolvedType = meta.lookupJavaType(componentType);
        return ((HotSpotJVMCIRuntime) JVMCI.getRuntime()).getArrayBaseOffset(resolvedType.getJavaKind());
    }

    @Override
    protected <T> T asObject(final Class<T> type, final JavaConstant constant) {
        if (constant.isNull()) {
            return null;
        }
        final HotSpotObjectConstant hsConstant = (HotSpotObjectConstant) constant;
        return hsConstant.asObject(type);
    }

    private static class TraceTransferToInterpreterHelper {

        private static final long THREAD_EETOP_OFFSET;

        static {
            try {
                THREAD_EETOP_OFFSET = UNSAFE.objectFieldOffset(Thread.class.getDeclaredField("eetop"));
            } catch (Exception e) {
                throw new InternalError(e);
            }
        }

        static void traceTransferToInterpreter(AbstractHotSpotTruffleRuntime runtime, HotSpotTruffleCompiler compiler) {
            FrameInstance currentFrame = runtime.getCurrentFrame();
            if (currentFrame == null) {
                return;
            }
            OptimizedCallTarget callTarget = (OptimizedCallTarget) currentFrame.getCallTarget();
            long thread = UNSAFE.getLong(Thread.currentThread(), THREAD_EETOP_OFFSET);
            long pendingTransferToInterpreterAddress = thread + compiler.pendingTransferToInterpreterOffset(callTarget);
            boolean deoptimized = UNSAFE.getByte(pendingTransferToInterpreterAddress) != 0;
            if (deoptimized) {
                logTransferToInterpreter(runtime, callTarget);
                UNSAFE.putByte(pendingTransferToInterpreterAddress, (byte) 0);
            }
        }

        private static String formatStackFrame(FrameInstance frameInstance, CallTarget target) {
            StringBuilder builder = new StringBuilder();
            if (target instanceof RootCallTarget) {
                RootNode root = ((RootCallTarget) target).getRootNode();
                String name = root.getName();
                if (name == null) {
                    builder.append("unnamed-root");
                } else {
                    builder.append(name);
                }
                Node callNode = frameInstance.getCallNode();
                SourceSection sourceSection = null;
                if (callNode != null) {
                    sourceSection = callNode.getEncapsulatingSourceSection();
                }
                if (sourceSection == null) {
                    sourceSection = root.getSourceSection();
                }
                if (sourceSection == null || sourceSection.getSource() == null) {
                    builder.append("(Unknown)");
                } else {
                    builder.append("(").append(formatPath(sourceSection)).append(":").append(sourceSection.getStartLine()).append(")");
                }
                if (target instanceof OptimizedCallTarget) {
                    OptimizedCallTarget callTarget = ((OptimizedCallTarget) target);
                    if (callTarget.getSourceCallTarget() != null) {
                        builder.append(" <split-" + Integer.toHexString(callTarget.hashCode()) + ">");
                    }
                }
            } else {
                builder.append(target.toString());
            }
            return builder.toString();
        }

        private static String formatPath(SourceSection sourceSection) {
            if (sourceSection.getSource().getPath() != null) {
                Path path = FileSystems.getDefault().getPath(".").toAbsolutePath();
                Path filePath = FileSystems.getDefault().getPath(sourceSection.getSource().getPath()).toAbsolutePath();
                try {
                    return path.relativize(filePath).toString();
                } catch (IllegalArgumentException e) {
                }
            }
            return sourceSection.getSource().getName();
        }

        private static void logTransferToInterpreter(AbstractHotSpotTruffleRuntime runtime, OptimizedCallTarget callTarget) {
            final int limit = callTarget.getOptionValue(PolyglotCompilerOptions.TraceStackTraceLimit);
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("transferToInterpreter at\n");
            runtime.iterateFrames(new FrameInstanceVisitor<Object>() {

                int frameIndex = 0;

                @Override
                public Object visitFrame(FrameInstance frameInstance) {
                    CallTarget target = frameInstance.getCallTarget();
                    StringBuilder line = new StringBuilder("  ");
                    if (frameIndex > 0) {
                        line.append("  ");
                    }
                    line.append(formatStackFrame(frameInstance, target)).append("\n");
                    frameIndex++;
                    messageBuilder.append(line);
                    if (frameIndex < limit) {
                        return null;
                    } else {
                        messageBuilder.append("    ...\n");
                        return frameInstance;
                    }
                }
            });
            final int skip = 3;
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            String suffix = stackTrace.length > skip + limit ? "\n    ..." : "";
            messageBuilder.append(Arrays.stream(stackTrace).skip(skip).limit(limit).map(StackTraceElement::toString).collect(Collectors.joining("\n    ", "  ", suffix)));
            runtime.log(callTarget, messageBuilder.toString());
        }
    }
}