package com.oracle.graal.truffle;

import static com.oracle.graal.truffle.OptimizedCallTargetLog.*;
import static com.oracle.graal.truffle.TruffleCompilerOptions.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import com.oracle.graal.api.code.*;
import com.oracle.graal.debug.*;
import com.oracle.truffle.api.*;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;

public class OptimizedCallTarget extends InstalledCode implements RootCallTarget, LoopCountReceiver, ReplaceObserver {

    protected static final PrintStream OUT = TTY.out().out();

    protected final GraalTruffleRuntime runtime;

    private SpeculationLog speculationLog;

    protected int callCount;

    protected boolean inliningPerformed;

    protected final CompilationProfile compilationProfile;

    protected final CompilationPolicy compilationPolicy;

    private OptimizedCallTarget splitSource;

    private final AtomicInteger callSitesKnown = new AtomicInteger(0);

    @CompilationFinal
    private Class<?>[] profiledArgumentTypes;

    @CompilationFinal
    private Assumption profiledArgumentTypesAssumption;

    @CompilationFinal
    private Class<?> profiledReturnType;

    @CompilationFinal
    private Assumption profiledReturnTypeAssumption;

    private final RootNode rootNode;

    public final RootNode getRootNode() {
        return rootNode;
    }

    public OptimizedCallTarget(RootNode rootNode, GraalTruffleRuntime runtime, int invokeCounter, int compilationThreshold, CompilationPolicy compilationPolicy, SpeculationLog speculationLog) {
        this.runtime = runtime;
        this.speculationLog = speculationLog;
        this.rootNode = rootNode;
        this.rootNode.adoptChildren();
        this.rootNode.setCallTarget(this);
        this.compilationPolicy = compilationPolicy;
        this.compilationProfile = new CompilationProfile(compilationThreshold, invokeCounter);
        if (TruffleCallTargetProfiling.getValue()) {
            registerCallTarget(this);
        }
    }

    public SpeculationLog getSpeculationLog() {
        return speculationLog;
    }

    @Override
    public Object call(Object... args) {
        return callBoundary(args);
    }

    public Object callDirect(Object... args) {
        if (profiledArgumentTypesAssumption == null) {
            CompilerDirectives.transferToInterpreter();
            profiledArgumentTypesAssumption = Truffle.getRuntime().createAssumption("Profiled Argument Types");
            profiledArgumentTypes = new Class<?>[args.length];
        } else if (profiledArgumentTypes != null) {
            if (profiledArgumentTypes.length != args.length) {
                CompilerDirectives.transferToInterpreter();
                profiledArgumentTypesAssumption.invalidate();
                profiledArgumentTypes = null;
            }
        }
        Object result = callBoundary(args);
        Class<?> klass = profiledReturnType;
        if (klass != null && CompilerDirectives.inCompiledCode() && profiledReturnTypeAssumption.isValid()) {
            result = CompilerDirectives.unsafeCast(result, klass, true, true);
        }
        return result;
    }

    @TruffleCallBoundary
    private Object callBoundary(Object[] args) {
        if (CompilerDirectives.inInterpreter()) {
            CompilerDirectives.transferToInterpreter();
            interpreterCall();
        } else {
        }
        return callRoot(args);
    }

    @Override
    public void invalidate() {
        this.runtime.invalidateInstalledCode(this);
    }

    protected void invalidate(Node oldNode, Node newNode, CharSequence reason) {
        if (isValid()) {
            CompilerAsserts.neverPartOfCompilation();
            invalidate();
            compilationProfile.reportInvalidated();
            logOptimizedInvalidated(this, oldNode, newNode, reason);
        }
        cancelInstalledTask(oldNode, newNode, reason);
    }

    private void cancelInstalledTask(Node oldNode, Node newNode, CharSequence reason) {
        if (this.runtime.cancelInstalledTask(this)) {
            logOptimizingUnqueued(this, oldNode, newNode, reason);
            compilationProfile.reportInvalidated();
        }
    }

    private void interpreterCall() {
        CompilerAsserts.neverPartOfCompilation();
        if (this.isValid()) {
            this.runtime.reinstallStubs();
        } else {
            compilationProfile.reportInterpreterCall();
            if (TruffleCallTargetProfiling.getValue()) {
                callCount++;
            }
            if (compilationPolicy.shouldCompile(compilationProfile)) {
                compile();
            }
        }
    }

    public void compile() {
        if (!runtime.isCompiling(this)) {
            performInlining();
            logOptimizingQueued(this);
            runtime.compile(this, TruffleBackgroundCompilation.getValue());
        }
    }

    public void compilationFinished(Throwable t) {
        if (t == null) {
        } else {
            compilationPolicy.recordCompilationFailure(t);
            logOptimizingFailed(this, t.getMessage());
            if (t instanceof BailoutException) {
            } else {
                if (TruffleCompilationExceptionsAreFatal.getValue()) {
                    t.printStackTrace(OUT);
                    System.exit(-1);
                }
            }
        }
    }

    protected final Object callProxy(VirtualFrame frame) {
        try {
            return getRootNode().execute(frame);
        } finally {
            assert frame != null && this != null;
        }
    }

    public final int getKnownCallSiteCount() {
        return callSitesKnown.get();
    }

    public final void incrementKnownCallSites() {
        callSitesKnown.incrementAndGet();
    }

    public final void decrementKnownCallSites() {
        callSitesKnown.decrementAndGet();
    }

    public final OptimizedCallTarget getSplitSource() {
        return splitSource;
    }

    public final void setSplitSource(OptimizedCallTarget splitSource) {
        this.splitSource = splitSource;
    }

    @Override
    public String toString() {
        String superString = rootNode.toString();
        if (isValid()) {
            superString += " <compiled>";
        }
        if (splitSource != null) {
            superString += " <split>";
        }
        return superString;
    }

    public CompilationProfile getCompilationProfile() {
        return compilationProfile;
    }

    public final Object callInlined(Object[] arguments) {
        if (CompilerDirectives.inInterpreter()) {
            compilationProfile.reportInlinedCall();
        }
        VirtualFrame frame = createFrame(getRootNode().getFrameDescriptor(), arguments);
        return callProxy(frame);
    }

    public final void performInlining() {
        if (!TruffleFunctionInlining.getValue()) {
            return;
        }
        if (inliningPerformed) {
            return;
        }
<<<<<<< MINE
=======
        inliningPerformed = true;
>>>>>>> YOURS
        TruffleInliningHandler handler = new TruffleInliningHandler(new DefaultInliningPolicy());
        TruffleInliningResult result = handler.decideInlining(this, 0);
        performInlining(result);
        logInliningDecision(result);
    }

    private static void performInlining(TruffleInliningResult result) {
<<<<<<< MINE
        if (result.getCallTarget().inliningPerformed) {
            return;
        }
        result.getCallTarget().inliningPerformed = true;
=======
>>>>>>> YOURS
        for (TruffleInliningProfile profile : result) {
            profile.getCallNode().inline();
            TruffleInliningResult recursiveResult = profile.getRecursiveResult();
            if (recursiveResult != null) {
                performInlining(recursiveResult);
            }
        }
    }

    public final Object callRoot(Object[] originalArguments) {
        Object[] args = originalArguments;
        if (this.profiledArgumentTypesAssumption != null && CompilerDirectives.inCompiledCode() && profiledArgumentTypesAssumption.isValid()) {
            args = CompilerDirectives.unsafeCast(castArrayFixedLength(args, profiledArgumentTypes.length), Object[].class, true, true);
        }
        VirtualFrame frame = createFrame(getRootNode().getFrameDescriptor(), args);
        Object result = callProxy(frame);
        if (profiledReturnTypeAssumption == null) {
            if (TruffleReturnTypeSpeculation.getValue()) {
                CompilerDirectives.transferToInterpreter();
<<<<<<< MINE
                profiledReturnType = (result == null ? null : result.getClass());
=======
                profiledReturnType = result.getClass();
>>>>>>> YOURS
                profiledReturnTypeAssumption = Truffle.getRuntime().createAssumption("Profiled Return Type");
            }
        } else if (profiledReturnType != null) {
            if (result == null || profiledReturnType != result.getClass()) {
                CompilerDirectives.transferToInterpreter();
                profiledReturnType = null;
                profiledReturnTypeAssumption.invalidate();
            }
        }
        return result;
    }

    private static Object castArrayFixedLength(Object[] args, @SuppressWarnings("unused") int length) {
        return args;
    }

    public static FrameWithoutBoxing createFrame(FrameDescriptor descriptor, Object[] args) {
        return new FrameWithoutBoxing(descriptor, args);
    }

    @Override
    public void reportLoopCount(int count) {
        compilationProfile.reportLoopCount(count);
    }

    @Override
    public void nodeReplaced(Node oldNode, Node newNode, CharSequence reason) {
        compilationProfile.reportNodeReplaced();
        invalidate(oldNode, newNode, reason);
    }

    public Map<String, Object> getDebugProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();
        addASTSizeProperty(this, properties);
        properties.putAll(getCompilationProfile().getDebugProperties());
        return properties;
    }
}