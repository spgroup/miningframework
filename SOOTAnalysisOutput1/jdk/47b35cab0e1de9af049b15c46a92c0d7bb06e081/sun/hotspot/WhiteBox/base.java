package sun.hotspot;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.security.BasicPermission;
import sun.hotspot.parser.DiagnosticCommand;

public class WhiteBox {

    @SuppressWarnings("serial")
    public static class WhiteBoxPermission extends BasicPermission {

        public WhiteBoxPermission(String s) {
            super(s);
        }
    }

    private WhiteBox() {
    }

    private static final WhiteBox instance = new WhiteBox();

    private static native void registerNatives();

    public synchronized static WhiteBox getWhiteBox() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new WhiteBoxPermission("getInstance"));
        }
        return instance;
    }

    static {
        registerNatives();
    }

    public native long getCompressedOopsMaxHeapSize();

    public native void printHeapSizes();

    public native long getObjectAddress(Object o);

    public native int getHeapOopSize();

    public native int getVMPageSize();

    public native boolean isObjectInOldGen(Object o);

    public native long getObjectSize(Object o);

    public boolean isClassAlive(String name) {
        return isClassAlive0(name.replace('.', '/'));
    }

    private native boolean isClassAlive0(String name);

    public native void addToBootstrapClassLoaderSearch(String segment);

    public native void addToSystemClassLoaderSearch(String segment);

    public native boolean g1InConcurrentMark();

    public native boolean g1IsHumongous(Object o);

    public native long g1NumFreeRegions();

    public native int g1RegionSize();

    public native Object[] parseCommandLine(String commandline, char delim, DiagnosticCommand[] args);

    public native long NMTMalloc(long size);

    public native void NMTFree(long mem);

    public native long NMTReserveMemory(long size);

    public native void NMTCommitMemory(long addr, long size);

    public native void NMTUncommitMemory(long addr, long size);

    public native void NMTReleaseMemory(long addr, long size);

    public native long NMTMallocWithPseudoStack(long size, int index);

    public native boolean NMTIsDetailSupported();

    public native boolean NMTChangeTrackingLevel();

    public native int NMTGetHashSize();

    public native void deoptimizeAll();

    public boolean isMethodCompiled(Executable method) {
        return isMethodCompiled(method, false);
    }

    public native boolean isMethodCompiled(Executable method, boolean isOsr);

    public boolean isMethodCompilable(Executable method) {
        return isMethodCompilable(method, -1);
    }

    public boolean isMethodCompilable(Executable method, int compLevel) {
        return isMethodCompilable(method, compLevel, false);
    }

    public native boolean isMethodCompilable(Executable method, int compLevel, boolean isOsr);

    public native boolean isMethodQueuedForCompilation(Executable method);

    public int deoptimizeMethod(Executable method) {
        return deoptimizeMethod(method, false);
    }

    public native int deoptimizeMethod(Executable method, boolean isOsr);

    public void makeMethodNotCompilable(Executable method) {
        makeMethodNotCompilable(method, -1);
    }

    public void makeMethodNotCompilable(Executable method, int compLevel) {
        makeMethodNotCompilable(method, compLevel, false);
    }

    public native void makeMethodNotCompilable(Executable method, int compLevel, boolean isOsr);

    public int getMethodCompilationLevel(Executable method) {
        return getMethodCompilationLevel(method, false);
    }

    public native int getMethodCompilationLevel(Executable method, boolean isOsr);

    public native boolean testSetDontInlineMethod(Executable method, boolean value);

    public int getCompileQueuesSize() {
        return getCompileQueueSize(-1);
    }

    public native int getCompileQueueSize(int compLevel);

    public native boolean testSetForceInlineMethod(Executable method, boolean value);

    public boolean enqueueMethodForCompilation(Executable method, int compLevel) {
        return enqueueMethodForCompilation(method, compLevel, -1);
    }

    public native boolean enqueueMethodForCompilation(Executable method, int compLevel, int entry_bci);

    public native void clearMethodState(Executable method);

    public native void lockCompilation();

    public native void unlockCompilation();

    public native int getMethodEntryBci(Executable method);

    public native Object[] getNMethod(Executable method, boolean isOsr);

    public native long allocateCodeBlob(int size, int type);

    public long allocateCodeBlob(long size, int type) {
        int intSize = (int) size;
        if ((long) intSize != size || size < 0) {
            throw new IllegalArgumentException("size argument has illegal value " + size);
        }
        return allocateCodeBlob(intSize, type);
    }

    public native void freeCodeBlob(long addr);

    public void forceNMethodSweep() {
        try {
            forceNMethodSweep0().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public native Thread forceNMethodSweep0();

    public native Object[] getCodeHeapEntries(int type);

    public native int getCompilationActivityMode();

    public native Object[] getCodeBlob(long addr);

    public native boolean isInStringTable(String str);

    public native void readReservedMemory();

    public native long allocateMetaspace(ClassLoader classLoader, long size);

    public native void freeMetaspace(ClassLoader classLoader, long addr, long size);

    public native long incMetaspaceCapacityUntilGC(long increment);

    public native long metaspaceCapacityUntilGC();

    public native void youngGC();

    public native void fullGC();

    public native boolean g1StartConcMarkCycle();

    public native int stressVirtualSpaceResize(long reservedSpaceSize, long magnitude, long iterations);

    public native void runMemoryUnitTests();

    public native void readFromNoaccessArea();

    public native long getThreadStackSize();

    public native long getThreadRemainingStackSize();

    public native String getCPUFeatures();

    public native long getHeapUsageForContext(int context);

    public native long getHeapRegionCountForContext(int context);

    public native int getContextForObject(Object obj);

    public native void printRegionInfo(int context);

    public native boolean isConstantVMFlag(String name);

    public native boolean isLockedVMFlag(String name);

    public native void setBooleanVMFlag(String name, boolean value);

    public native void setIntxVMFlag(String name, long value);

    public native void setUintxVMFlag(String name, long value);

    public native void setUint64VMFlag(String name, long value);

    public native void setSizeTVMFlag(String name, long value);

    public native void setStringVMFlag(String name, String value);

    public native void setDoubleVMFlag(String name, double value);

    public native Boolean getBooleanVMFlag(String name);

    public native Long getIntxVMFlag(String name);

    public native Long getUintxVMFlag(String name);

    public native Long getUint64VMFlag(String name);

    public native Long getSizeTVMFlag(String name);

    public native String getStringVMFlag(String name);

    public native Double getDoubleVMFlag(String name);

    private final List<Function<String, Object>> flagsGetters = Arrays.asList(this::getBooleanVMFlag, this::getIntxVMFlag, this::getUintxVMFlag, this::getUint64VMFlag, this::getSizeTVMFlag, this::getStringVMFlag, this::getDoubleVMFlag);

    public Object getVMFlag(String name) {
        return flagsGetters.stream().map(f -> f.apply(name)).filter(x -> x != null).findAny().orElse(null);
    }

    public native Object DefineModule(String name, Object loader, Object[] packages);

    public native void AddModuleExports(Object from_module, String pkg, Object to_module);

    public native void AddReadsModule(Object from_module, Object to_module);

    public native boolean CanReadModule(Object asking_module, Object target_module);

    public native boolean IsExportedToModule(Object from_module, String pkg, Object to_module);

    public native Object GetModule(Class clazz);

    public native void AddModulePackage(Object module, String pkg);

    public native boolean readImageFile(String imagefile);

    public native int getOffsetForName0(String name);

    public int getOffsetForName(String name) throws Exception {
        int offset = getOffsetForName0(name);
        if (offset == -1) {
            throw new RuntimeException(name + " not found");
        }
        return offset;
    }
}
