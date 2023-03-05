package org.graalvm.compiler.hotspot;

import static org.graalvm.compiler.core.GraalCompilerOptions.ExitVMOnBailout;
import static org.graalvm.compiler.core.GraalCompilerOptions.ExitVMOnException;
import static org.graalvm.compiler.core.GraalCompilerOptions.PrintAfterCompilation;
import static org.graalvm.compiler.core.GraalCompilerOptions.PrintBailout;
import static org.graalvm.compiler.core.GraalCompilerOptions.PrintCompilation;
import static org.graalvm.compiler.core.GraalCompilerOptions.PrintFilter;
import static org.graalvm.compiler.core.GraalCompilerOptions.PrintStackTraceOnException;
import static org.graalvm.compiler.core.phases.HighTier.Options.Inline;
import java.util.List;
import org.graalvm.compiler.code.CompilationResult;
import org.graalvm.compiler.debug.Debug;
import org.graalvm.compiler.debug.Debug.Scope;
import org.graalvm.compiler.debug.DebugCloseable;
import org.graalvm.compiler.debug.DebugCounter;
import org.graalvm.compiler.debug.DebugDumpScope;
import org.graalvm.compiler.debug.DebugTimer;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.debug.Management;
import org.graalvm.compiler.debug.TTY;
import org.graalvm.compiler.debug.TimeSource;
import org.graalvm.compiler.options.OptionValue;
import org.graalvm.compiler.options.OptionValue.OverrideScope;
import jdk.vm.ci.code.BailoutException;
import jdk.vm.ci.code.CodeCacheProvider;
import jdk.vm.ci.hotspot.EventProvider;
import jdk.vm.ci.hotspot.HotSpotCompilationRequest;
import jdk.vm.ci.hotspot.HotSpotCompilationRequestResult;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotInstalledCode;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntimeProvider;
import jdk.vm.ci.hotspot.HotSpotNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.runtime.JVMCICompiler;
import jdk.vm.ci.services.JVMCIServiceLocator;

public class CompilationTask {

    private static final DebugCounter BAILOUTS = Debug.counter("Bailouts");

    private static final EventProvider eventProvider;

    static {
        List<EventProvider> providers = JVMCIServiceLocator.getProviders(EventProvider.class);
        if (providers.size() > 1) {
            throw new GraalError("Multiple %s providers found: %s", EventProvider.class.getName(), providers);
        } else if (providers.isEmpty()) {
            eventProvider = EventProvider.createEmptyEventProvider();
        } else {
            eventProvider = providers.get(0);
        }
    }

    private final HotSpotJVMCIRuntimeProvider jvmciRuntime;

    private final HotSpotGraalCompiler compiler;

    private final HotSpotCompilationIdentifier compilationId;

    private HotSpotInstalledCode installedCode;

    private final boolean installAsDefault;

    private final boolean useProfilingInfo;

    static class Lazy {

        static final com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean) Management.getThreadMXBean();
    }

    public CompilationTask(HotSpotJVMCIRuntimeProvider jvmciRuntime, HotSpotGraalCompiler compiler, HotSpotCompilationRequest request, boolean useProfilingInfo, boolean installAsDefault) {
        this.jvmciRuntime = jvmciRuntime;
        this.compiler = compiler;
        this.compilationId = new HotSpotCompilationIdentifier(request);
        this.useProfilingInfo = useProfilingInfo;
        this.installAsDefault = installAsDefault;
    }

    public HotSpotResolvedJavaMethod getMethod() {
        return getRequest().getMethod();
    }

    public int getId() {
        return getRequest().getId();
    }

    public int getEntryBCI() {
        return getRequest().getEntryBCI();
    }

    public String getIdString() {
        if (getEntryBCI() != JVMCICompiler.INVOCATION_ENTRY_BCI) {
            return getId() + "%";
        } else {
            return Integer.toString(getId());
        }
    }

    public HotSpotInstalledCode getInstalledCode() {
        return installedCode;
    }

    private static final DebugTimer CompilationTime = Debug.timer("CompilationTime");

    private static final DebugCounter CompiledBytecodes = Debug.counter("CompiledBytecodes");

    private static final DebugCounter CompiledAndInstalledBytecodes = Debug.counter("CompiledAndInstalledBytecodes");

    private static final DebugCounter InstalledCodeSize = Debug.counter("InstalledCodeSize");

    public static final DebugTimer CodeInstallationTime = Debug.timer("CodeInstallation");

    @SuppressWarnings("try")
    public HotSpotCompilationRequestResult runCompilation() {
        HotSpotGraalRuntimeProvider graalRuntime = compiler.getGraalRuntime();
        GraalHotSpotVMConfig config = graalRuntime.getVMConfig();
        final long threadId = Thread.currentThread().getId();
        int entryBCI = getEntryBCI();
        final boolean isOSR = entryBCI != JVMCICompiler.INVOCATION_ENTRY_BCI;
        HotSpotResolvedJavaMethod method = getMethod();
        if (Debug.isMethodMeterEnabled()) {
            if (getEntryBCI() != JVMCICompiler.INVOCATION_ENTRY_BCI) {
                Debug.methodMetrics(method).addToMetric(getId(), "CompilationIdOSR");
            } else {
                Debug.methodMetrics(method).addToMetric(getId(), "CompilationId");
            }
        }
        EventProvider.CompilationEvent compilationEvent = eventProvider.newCompilationEvent();
        if (method.hasCodeAtLevel(entryBCI, config.compilationLevelFullOptimization)) {
            return null;
        }
        CompilationResult result = null;
        try (DebugCloseable a = CompilationTime.start()) {
            CompilationStatistics stats = CompilationStatistics.create(method, isOSR);
            final boolean printCompilation = PrintCompilation.getValue() && !TTY.isSuppressed();
            final boolean printAfterCompilation = PrintAfterCompilation.getValue() && !TTY.isSuppressed();
            if (printCompilation) {
                TTY.println(getMethodDescription() + "...");
            }
            TTY.Filter filter = new TTY.Filter(PrintFilter.getValue(), method);
            final long start;
            final long allocatedBytesBefore;
            if (printAfterCompilation || printCompilation) {
                start = TimeSource.getTimeNS();
                allocatedBytesBefore = printAfterCompilation || printCompilation ? Lazy.threadMXBean.getThreadAllocatedBytes(threadId) : 0L;
            } else {
                start = 0L;
                allocatedBytesBefore = 0L;
            }
            try (Scope s = Debug.scope("Compiling", new DebugDumpScope(getIdString(), true))) {
                compilationEvent.begin();
                boolean disableInlining = !config.inline && !Inline.hasBeenSet();
                try (OverrideScope s1 = disableInlining ? OptionValue.override(Inline, false) : null) {
                    result = compiler.compile(method, entryBCI, useProfilingInfo, compilationId);
                }
            } catch (Throwable e) {
                throw Debug.handle(e);
            } finally {
                compilationEvent.end();
                filter.remove();
                if (printAfterCompilation || printCompilation) {
                    final long stop = TimeSource.getTimeNS();
                    final long duration = (stop - start) / 1000000;
                    final int targetCodeSize = result != null ? result.getTargetCodeSize() : -1;
                    final int bytecodeSize = result != null ? result.getBytecodeSize() : 0;
                    final long allocatedBytesAfter = Lazy.threadMXBean.getThreadAllocatedBytes(threadId);
                    final long allocatedKBytes = (allocatedBytesAfter - allocatedBytesBefore) / 1024;
                    if (printAfterCompilation) {
                        TTY.println(getMethodDescription() + String.format(" | %4dms %5dB %5dB %5dkB", duration, bytecodeSize, targetCodeSize, allocatedKBytes));
                    } else if (printCompilation) {
                        TTY.println(String.format("%-6d JVMCI %-70s %-45s %-50s | %4dms %5dB %5dB %5dkB", getId(), "", "", "", duration, bytecodeSize, targetCodeSize, allocatedKBytes));
                    }
                }
            }
            if (result != null) {
                try (DebugCloseable b = CodeInstallationTime.start()) {
                    installMethod(result);
                }
            }
            stats.finish(method, installedCode);
            if (result != null) {
                return HotSpotCompilationRequestResult.success(result.getBytecodeSize() - method.getCodeSize());
            }
            return null;
        } catch (BailoutException bailout) {
            BAILOUTS.increment();
            if (ExitVMOnBailout.getValue()) {
                TTY.out.println(method.format("Bailout in %H.%n(%p)"));
                bailout.printStackTrace(TTY.out);
                System.exit(-1);
            } else if (PrintBailout.getValue()) {
                TTY.out.println(method.format("Bailout in %H.%n(%p)"));
                bailout.printStackTrace(TTY.out);
            }
            final boolean permanentBailout = bailout.isPermanent();
            if (permanentBailout && PrintBailout.getValue()) {
                TTY.println("Permanent bailout %s compiling method %s %s.", bailout.getMessage(), HotSpotGraalCompiler.str(method), (isOSR ? "OSR" : ""));
            }
            return HotSpotCompilationRequestResult.failure(bailout.getMessage(), !permanentBailout);
        } catch (Throwable t) {
            EventProvider.CompilerFailureEvent event = eventProvider.newCompilerFailureEvent();
            if (event.shouldWrite()) {
                event.setCompileId(getId());
                event.setMessage(t.getMessage());
                event.commit();
            }
            handleException(t);
            return HotSpotCompilationRequestResult.failure(t.toString(), false);
        } finally {
            try {
                int compiledBytecodes = 0;
                int codeSize = 0;
                if (result != null) {
                    compiledBytecodes = result.getBytecodeSize();
                    CompiledBytecodes.add(compiledBytecodes);
                    if (installedCode != null) {
                        codeSize = installedCode.getSize();
                        CompiledAndInstalledBytecodes.add(compiledBytecodes);
                        InstalledCodeSize.add(codeSize);
                    }
                }
                if (compilationEvent.shouldWrite()) {
                    compilationEvent.setMethod(method.format("%H.%n(%p)"));
                    compilationEvent.setCompileId(getId());
                    compilationEvent.setCompileLevel(config.compilationLevelFullOptimization);
                    compilationEvent.setSucceeded(result != null && installedCode != null);
                    compilationEvent.setIsOsr(isOSR);
                    compilationEvent.setCodeSize(codeSize);
                    compilationEvent.setInlinedBytes(compiledBytecodes);
                    compilationEvent.commit();
                }
            } catch (Throwable t) {
                handleException(t);
            }
        }
    }

    protected void handleException(Throwable t) {
        boolean exitVMOnException = ExitVMOnException.getValue();
        if (!ExitVMOnException.hasBeenSet()) {
            assert (exitVMOnException = true) == true;
            if (!exitVMOnException) {
                HotSpotGraalRuntimeProvider runtime = compiler.getGraalRuntime();
                if (runtime.isBootstrapping()) {
                    exitVMOnException = true;
                }
            }
        }
        if (PrintStackTraceOnException.getValue() || exitVMOnException) {
            try {
                t.printStackTrace(TTY.out);
            } catch (Throwable throwable) {
            }
        }
        if (exitVMOnException) {
            System.exit(-1);
        }
    }

    private String getMethodDescription() {
        HotSpotResolvedJavaMethod method = getMethod();
        return String.format("%-6d JVMCI %-70s %-45s %-50s %s", getId(), method.getDeclaringClass().getName(), method.getName(), method.getSignature().toMethodDescriptor(), getEntryBCI() == JVMCICompiler.INVOCATION_ENTRY_BCI ? "" : "(OSR@" + getEntryBCI() + ") ");
    }

    @SuppressWarnings("try")
    private void installMethod(final CompilationResult compResult) {
        final CodeCacheProvider codeCache = jvmciRuntime.getHostJVMCIBackend().getCodeCache();
        installedCode = null;
        Object[] context = { new DebugDumpScope(getIdString(), true), codeCache, getMethod(), compResult };
        try (Scope s = Debug.scope("CodeInstall", context)) {
            HotSpotCompiledCode compiledCode = HotSpotCompiledCodeBuilder.createCompiledCode(getRequest().getMethod(), getRequest(), compResult);
            installedCode = (HotSpotInstalledCode) codeCache.installCode(getRequest().getMethod(), compiledCode, null, getRequest().getMethod().getSpeculationLog(), installAsDefault);
        } catch (Throwable e) {
            throw Debug.handle(e);
        }
    }

    @Override
    public String toString() {
        return "Compilation[id=" + getId() + ", " + getMethod().format("%H.%n(%p)") + (getEntryBCI() == JVMCICompiler.INVOCATION_ENTRY_BCI ? "" : "@" + getEntryBCI()) + "]";
    }

    private HotSpotCompilationRequest getRequest() {
        return compilationId.getRequest();
    }
}