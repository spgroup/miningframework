package jdk.vm.ci.hotspot;

import static jdk.vm.ci.common.InitTimer.timer;
import static jdk.vm.ci.hotspot.HotSpotJVMCIRuntime.runtime;
import java.lang.reflect.Executable;
import jdk.vm.ci.code.BytecodeFrame;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.code.InvalidInstalledCodeException;
import jdk.vm.ci.code.TargetDescription;
import jdk.vm.ci.common.InitTimer;
import jdk.vm.ci.common.JVMCIError;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

final class CompilerToVM {

    private static native void registerNatives();

    static {
        initialize();
    }

    @SuppressWarnings("try")
    private static void initialize() {
        try (InitTimer t = timer("CompilerToVM.registerNatives")) {
            registerNatives();
        }
    }

    public static CompilerToVM compilerToVM() {
        return runtime().getCompilerToVM();
    }

    native byte[] getBytecode(HotSpotResolvedJavaMethodImpl method);

    native int getExceptionTableLength(HotSpotResolvedJavaMethodImpl method);

    native long getExceptionTableStart(HotSpotResolvedJavaMethodImpl method);

    native boolean isCompilable(HotSpotResolvedJavaMethodImpl method);

    native boolean hasNeverInlineDirective(HotSpotResolvedJavaMethodImpl method);

    native boolean shouldInlineMethod(HotSpotResolvedJavaMethodImpl method);

    native HotSpotResolvedJavaMethodImpl findUniqueConcreteMethod(HotSpotResolvedObjectTypeImpl actualHolderType, HotSpotResolvedJavaMethodImpl method);

    native HotSpotResolvedObjectTypeImpl getImplementor(HotSpotResolvedObjectTypeImpl type);

    native boolean methodIsIgnoredBySecurityStackWalk(HotSpotResolvedJavaMethodImpl method);

    native HotSpotResolvedObjectTypeImpl lookupType(String name, Class<?> accessingClass, boolean resolve);

    native Object resolveConstantInPool(HotSpotConstantPool constantPool, int cpi);

    native Object resolvePossiblyCachedConstantInPool(HotSpotConstantPool constantPool, int cpi);

    native int lookupNameAndTypeRefIndexInPool(HotSpotConstantPool constantPool, int cpi);

    native String lookupNameInPool(HotSpotConstantPool constantPool, int which);

    native String lookupSignatureInPool(HotSpotConstantPool constantPool, int which);

    native int lookupKlassRefIndexInPool(HotSpotConstantPool constantPool, int cpi);

    native Object lookupKlassInPool(HotSpotConstantPool constantPool, int cpi);

    native HotSpotResolvedJavaMethodImpl lookupMethodInPool(HotSpotConstantPool constantPool, int cpi, byte opcode);

    native void resolveInvokeDynamicInPool(HotSpotConstantPool constantPool, int cpi);

    native void resolveInvokeHandleInPool(HotSpotConstantPool constantPool, int cpi);

    native String[] getSignaturePolymorphicHolders();

    native HotSpotResolvedObjectTypeImpl resolveTypeInPool(HotSpotConstantPool constantPool, int cpi) throws LinkageError;

    native HotSpotResolvedObjectTypeImpl resolveFieldInPool(HotSpotConstantPool constantPool, int cpi, HotSpotResolvedJavaMethodImpl method, byte opcode, long[] info);

    native int constantPoolRemapInstructionOperandFromCache(HotSpotConstantPool constantPool, int cpci);

    native Object lookupAppendixInPool(HotSpotConstantPool constantPool, int cpi);

    native int installCode(TargetDescription target, HotSpotCompiledCode compiledCode, InstalledCode code, HotSpotSpeculationLog speculationLog);

    native int getMetadata(TargetDescription target, HotSpotCompiledCode compiledCode, HotSpotMetaData metaData);

    native void resetCompilationStatistics();

    native Object[] readConfiguration();

    native HotSpotResolvedJavaMethodImpl resolveMethod(HotSpotResolvedObjectTypeImpl exactReceiver, HotSpotResolvedJavaMethodImpl method, HotSpotResolvedObjectTypeImpl caller);

    native HotSpotResolvedJavaMethodImpl getClassInitializer(HotSpotResolvedObjectTypeImpl type);

    native boolean hasFinalizableSubclass(HotSpotResolvedObjectTypeImpl type);

    native HotSpotResolvedJavaMethodImpl asResolvedJavaMethod(Executable executable);

    native long getMaxCallTargetOffset(long address);

    synchronized native String disassembleCodeBlob(InstalledCode installedCode);

    native StackTraceElement getStackTraceElement(HotSpotResolvedJavaMethodImpl method, int bci);

    native Object executeInstalledCode(Object[] args, InstalledCode installedCode) throws InvalidInstalledCodeException;

    native long[] getLineNumberTable(HotSpotResolvedJavaMethodImpl method);

    native int getLocalVariableTableLength(HotSpotResolvedJavaMethodImpl method);

    native long getLocalVariableTableStart(HotSpotResolvedJavaMethodImpl method);

    native void doNotInlineOrCompile(HotSpotResolvedJavaMethodImpl method);

    native void reprofile(HotSpotResolvedJavaMethodImpl method);

    native void invalidateInstalledCode(InstalledCode installedCode);

    native long[] collectCounters();

    native boolean isMature(long metaspaceMethodData);

    native int allocateCompileId(HotSpotResolvedJavaMethodImpl method, int entryBCI);

    native boolean hasCompiledCodeForOSR(HotSpotResolvedJavaMethodImpl method, int entryBCI, int level);

    native String getSymbol(long metaspaceSymbol);

    native HotSpotStackFrameReference getNextStackFrame(HotSpotStackFrameReference frame, ResolvedJavaMethod[] methods, int initialSkip);

    native void materializeVirtualObjects(HotSpotStackFrameReference stackFrame, boolean invalidate);

    native int getVtableIndexForInterfaceMethod(HotSpotResolvedObjectTypeImpl type, HotSpotResolvedJavaMethodImpl method);

    native boolean shouldDebugNonSafepoints();

    native void writeDebugOutput(byte[] bytes, int offset, int length);

    native void flushDebugOutput();

    native HotSpotResolvedJavaMethodImpl getResolvedJavaMethod(Object base, long displacement);

    native HotSpotConstantPool getConstantPool(Object object);

    native HotSpotResolvedObjectTypeImpl getResolvedJavaType(Object base, long displacement, boolean compressed);

    native int methodDataProfileDataSize(long metaspaceMethodData, int position);

    native long getFingerprint(long metaspaceKlass);

    native int interpreterFrameSize(BytecodeFrame frame);

    native void compileToBytecode(Object lambdaForm);
}
