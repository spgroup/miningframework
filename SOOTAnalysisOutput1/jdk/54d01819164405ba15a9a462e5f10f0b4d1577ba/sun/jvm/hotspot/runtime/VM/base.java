package sun.jvm.hotspot.runtime;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.c1.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.interpreter.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.runtime.*;

public class VM {

    private static VM soleInstance;

    private static List vmInitializedObservers = new ArrayList();

    private List vmResumedObservers = new ArrayList();

    private List vmSuspendedObservers = new ArrayList();

    private TypeDataBase db;

    private boolean isBigEndian;

    private JVMDebugger debugger;

    private long stackBias;

    private long logAddressSize;

    private Universe universe;

    private ObjectHeap heap;

    private SymbolTable symbols;

    private StringTable strings;

    private SystemDictionary dict;

    private Threads threads;

    private ObjectSynchronizer synchronizer;

    private JNIHandles handles;

    private Interpreter interpreter;

    private StubRoutines stubRoutines;

    private Bytes bytes;

    private boolean usingClientCompiler;

    private boolean usingServerCompiler;

    private boolean isLP64;

    private int bytesPerLong;

    private int bytesPerWord;

    private int objectAlignmentInBytes;

    private int minObjAlignmentInBytes;

    private int logMinObjAlignmentInBytes;

    private int heapWordSize;

    private int heapOopSize;

    private int klassPtrSize;

    private int oopSize;

    private CodeCache codeCache;

    private Runtime1 runtime1;

    private int invocationEntryBCI;

    private ReversePtrs revPtrs;

    private VMRegImpl vmregImpl;

    private int reserveForAllocationPrefetch;

    private Properties sysProps;

    private String vmRelease;

    private String vmInternalInfo;

    private Flag[] commandLineFlags;

    private Map flagsMap;

    private static Type intType;

    private static Type uintType;

    private static Type intxType;

    private static Type uintxType;

    private static Type sizetType;

    private static CIntegerType boolType;

    private Boolean sharingEnabled;

    private Boolean compressedOopsEnabled;

    private Boolean compressedKlassPointersEnabled;

    public static final class Flag {

        private String type;

        private String name;

        private Address addr;

        private int flags;

        private Flag(String type, String name, Address addr, int flags) {
            this.type = type;
            this.name = name;
            this.addr = addr;
            this.flags = flags;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Address getAddress() {
            return addr;
        }

        public int getOrigin() {
            return flags & 0xF;
        }

        public boolean isBool() {
            return type.equals("bool");
        }

        public boolean getBool() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isBool(), "not a bool flag!");
            }
            return addr.getCIntegerAt(0, boolType.getSize(), boolType.isUnsigned()) != 0;
        }

        public boolean isInt() {
            return type.equals("int");
        }

        public long getInt() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isInt(), "not an int flag!");
            }
            return addr.getCIntegerAt(0, intType.getSize(), false);
        }

        public boolean isUInt() {
            return type.equals("uint");
        }

        public long getUInt() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isUInt(), "not a uint flag!");
            }
            return addr.getCIntegerAt(0, uintType.getSize(), false);
        }

        public boolean isIntx() {
            return type.equals("intx");
        }

        public long getIntx() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isIntx(), "not an intx flag!");
            }
            return addr.getCIntegerAt(0, intxType.getSize(), false);
        }

        public boolean isUIntx() {
            return type.equals("uintx");
        }

        public long getUIntx() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isUIntx(), "not a uintx flag!");
            }
            return addr.getCIntegerAt(0, uintxType.getSize(), true);
        }

        public boolean isSizet() {
            return type.equals("size_t");
        }

        public long getSizet() {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isSizet(), "not a size_t flag!");
            }
            return addr.getCIntegerAt(0, sizetType.getSize(), true);
        }

        public String getValue() {
            if (isBool()) {
                return Boolean.toString(getBool());
            } else if (isInt()) {
                return Long.toString(getInt());
            } else if (isUInt()) {
                return Long.toString(getUInt());
            } else if (isIntx()) {
                return Long.toString(getIntx());
            } else if (isUIntx()) {
                return Long.toString(getUIntx());
            } else if (isSizet()) {
                return Long.toString(getSizet());
            } else {
                return null;
            }
        }
    }

    private static void checkVMVersion(String vmRelease) {
        if (System.getProperty("sun.jvm.hotspot.runtime.VM.disableVersionCheck") == null) {
            String versionProp = "sun.jvm.hotspot.runtime.VM.saBuildVersion";
            String saVersion = saProps.getProperty(versionProp);
            if (saVersion == null)
                throw new RuntimeException("Missing property " + versionProp);
            String vmVersion = vmRelease.replaceAll("(-fastdebug)|(-debug)|(-jvmg)|(-optimized)|(-profiled)", "");
            if (saVersion.equals(vmVersion)) {
                return;
            }
            if (saVersion.indexOf('-') == saVersion.lastIndexOf('-') && vmVersion.indexOf('-') == vmVersion.lastIndexOf('-')) {
                throw new VMVersionMismatchException(saVersion, vmRelease);
            } else {
                System.err.println("WARNING: Hotspot VM version " + vmRelease + " does not match with SA version " + saVersion + "." + " You may see unexpected results. ");
            }
        } else {
            System.err.println("WARNING: You have disabled SA and VM version check. You may be " + "using incompatible version of SA and you may see unexpected " + "results.");
        }
    }

    private static final boolean disableDerivedPointerTableCheck;

    private static final Properties saProps;

    static {
        saProps = new Properties();
        URL url = null;
        try {
            saProps.load(VM.class.getResourceAsStream("/sa.properties"));
        } catch (Exception e) {
            System.err.println("Unable to load properties  " + (url == null ? "null" : url.toString()) + ": " + e.getMessage());
        }
        disableDerivedPointerTableCheck = System.getProperty("sun.jvm.hotspot.runtime.VM.disableDerivedPointerTableCheck") != null;
    }

    private VM(TypeDataBase db, JVMDebugger debugger, boolean isBigEndian) {
        this.db = db;
        this.debugger = debugger;
        this.isBigEndian = isBigEndian;
        if (db.getAddressSize() == 4) {
            logAddressSize = 2;
        } else if (db.getAddressSize() == 8) {
            logAddressSize = 3;
        } else {
            throw new RuntimeException("Address size " + db.getAddressSize() + " not yet supported");
        }
        try {
            Type vmVersion = db.lookupType("Abstract_VM_Version");
            Address releaseAddr = vmVersion.getAddressField("_s_vm_release").getValue();
            vmRelease = CStringUtilities.getString(releaseAddr);
            Address vmInternalInfoAddr = vmVersion.getAddressField("_s_internal_vm_info_string").getValue();
            vmInternalInfo = CStringUtilities.getString(vmInternalInfoAddr);
            CIntegerType intType = (CIntegerType) db.lookupType("int");
            CIntegerField reserveForAllocationPrefetchField = vmVersion.getCIntegerField("_reserve_for_allocation_prefetch");
            reserveForAllocationPrefetch = (int) reserveForAllocationPrefetchField.getCInteger(intType);
        } catch (Exception exp) {
            throw new RuntimeException("can't determine target's VM version : " + exp.getMessage());
        }
        checkVMVersion(vmRelease);
        stackBias = db.lookupIntConstant("STACK_BIAS").intValue();
        invocationEntryBCI = db.lookupIntConstant("InvocationEntryBci").intValue();
        {
            Type type = db.lookupType("Method");
            if (type.getField("_from_compiled_entry", false, false) == null) {
                usingClientCompiler = false;
                usingServerCompiler = false;
            } else {
                if (db.lookupType("Matcher", false) != null) {
                    usingServerCompiler = true;
                } else {
                    usingClientCompiler = true;
                }
            }
        }
        if (debugger != null) {
            isLP64 = debugger.getMachineDescription().isLP64();
        }
        bytesPerLong = db.lookupIntConstant("BytesPerLong").intValue();
        bytesPerWord = db.lookupIntConstant("BytesPerWord").intValue();
        heapWordSize = db.lookupIntConstant("HeapWordSize").intValue();
        oopSize = db.lookupIntConstant("oopSize").intValue();
        intType = db.lookupType("int");
        uintType = db.lookupType("uint");
        intxType = db.lookupType("intx");
        uintxType = db.lookupType("uintx");
        sizetType = db.lookupType("size_t");
        boolType = (CIntegerType) db.lookupType("bool");
        minObjAlignmentInBytes = getObjectAlignmentInBytes();
        if (minObjAlignmentInBytes == 8) {
            logMinObjAlignmentInBytes = 3;
        } else if (minObjAlignmentInBytes == 16) {
            logMinObjAlignmentInBytes = 4;
        } else {
            throw new RuntimeException("Object alignment " + minObjAlignmentInBytes + " not yet supported");
        }
        if (isCompressedOopsEnabled()) {
            heapOopSize = (int) getIntSize();
        } else {
            heapOopSize = (int) getOopSize();
        }
        if (isCompressedKlassPointersEnabled()) {
            klassPtrSize = (int) getIntSize();
        } else {
            klassPtrSize = (int) getOopSize();
        }
    }

    public static void initialize(TypeDataBase db, boolean isBigEndian) {
        if (soleInstance != null) {
            throw new RuntimeException("Attempt to initialize VM twice");
        }
        soleInstance = new VM(db, null, isBigEndian);
        for (Iterator iter = vmInitializedObservers.iterator(); iter.hasNext(); ) {
            ((Observer) iter.next()).update(null, null);
        }
    }

    public static void initialize(TypeDataBase db, JVMDebugger debugger) {
        if (soleInstance != null) {
            return;
        }
        soleInstance = new VM(db, debugger, debugger.getMachineDescription().isBigEndian());
        for (Iterator iter = vmInitializedObservers.iterator(); iter.hasNext(); ) {
            ((Observer) iter.next()).update(null, null);
        }
        debugger.putHeapConst(soleInstance.getHeapOopSize(), soleInstance.getKlassPtrSize(), Universe.getNarrowOopBase(), Universe.getNarrowOopShift(), Universe.getNarrowKlassBase(), Universe.getNarrowKlassShift());
    }

    public static void shutdown() {
        soleInstance = null;
    }

    public static void registerVMInitializedObserver(Observer o) {
        vmInitializedObservers.add(o);
        o.update(null, null);
    }

    public static VM getVM() {
        if (soleInstance == null) {
            throw new RuntimeException("VM.initialize() was not yet called");
        }
        return soleInstance;
    }

    public void registerVMResumedObserver(Observer o) {
        vmResumedObservers.add(o);
    }

    public void registerVMSuspendedObserver(Observer o) {
        vmSuspendedObservers.add(o);
    }

    public void fireVMResumed() {
        for (Iterator iter = vmResumedObservers.iterator(); iter.hasNext(); ) {
            ((Observer) iter.next()).update(null, null);
        }
    }

    public void fireVMSuspended() {
        for (Iterator iter = vmSuspendedObservers.iterator(); iter.hasNext(); ) {
            ((Observer) iter.next()).update(null, null);
        }
    }

    public String getOS() {
        if (debugger != null) {
            return debugger.getOS();
        }
        return PlatformInfo.getOS();
    }

    public String getCPU() {
        if (debugger != null) {
            return debugger.getCPU();
        }
        return PlatformInfo.getCPU();
    }

    public Type lookupType(String cTypeName) {
        return db.lookupType(cTypeName);
    }

    public Integer lookupIntConstant(String name) {
        return db.lookupIntConstant(name);
    }

    static public long getAddressValue(Address addr) {
        return VM.getVM().getDebugger().getAddressValue(addr);
    }

    public long getAddressSize() {
        return db.getAddressSize();
    }

    public long getOopSize() {
        return oopSize;
    }

    public long getLogAddressSize() {
        return logAddressSize;
    }

    public long getIntSize() {
        return db.getJIntType().getSize();
    }

    public long getStackBias() {
        return stackBias;
    }

    public boolean isLP64() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(isDebugging(), "Debugging system only for now");
        }
        return isLP64;
    }

    public int getBytesPerLong() {
        return bytesPerLong;
    }

    public int getBytesPerWord() {
        return bytesPerWord;
    }

    public int getMinObjAlignmentInBytes() {
        return minObjAlignmentInBytes;
    }

    public int getLogMinObjAlignmentInBytes() {
        return logMinObjAlignmentInBytes;
    }

    public int getHeapWordSize() {
        return heapWordSize;
    }

    public int getHeapOopSize() {
        return heapOopSize;
    }

    public int getKlassPtrSize() {
        return klassPtrSize;
    }

    public long alignUp(long size, long alignment) {
        return (size + alignment - 1) & ~(alignment - 1);
    }

    public long alignDown(long size, long alignment) {
        return size & ~(alignment - 1);
    }

    public int buildIntFromShorts(short low, short high) {
        return (((int) high) << 16) | (((int) low) & 0xFFFF);
    }

    public long buildLongFromIntsPD(int oneHalf, int otherHalf) {
        if (isBigEndian) {
            return (((long) otherHalf) << 32) | (((long) oneHalf) & 0x00000000FFFFFFFFL);
        } else {
            return (((long) oneHalf) << 32) | (((long) otherHalf) & 0x00000000FFFFFFFFL);
        }
    }

    public TypeDataBase getTypeDataBase() {
        return db;
    }

    public Universe getUniverse() {
        if (universe == null) {
            universe = new Universe();
        }
        return universe;
    }

    public ObjectHeap getObjectHeap() {
        if (heap == null) {
            heap = new ObjectHeap(db);
        }
        return heap;
    }

    public SymbolTable getSymbolTable() {
        if (symbols == null) {
            symbols = SymbolTable.getTheTable();
        }
        return symbols;
    }

    public StringTable getStringTable() {
        if (strings == null) {
            strings = StringTable.getTheTable();
        }
        return strings;
    }

    public SystemDictionary getSystemDictionary() {
        if (dict == null) {
            dict = new SystemDictionary();
        }
        return dict;
    }

    public Threads getThreads() {
        if (threads == null) {
            threads = new Threads();
        }
        return threads;
    }

    public ObjectSynchronizer getObjectSynchronizer() {
        if (synchronizer == null) {
            synchronizer = new ObjectSynchronizer();
        }
        return synchronizer;
    }

    public JNIHandles getJNIHandles() {
        if (handles == null) {
            handles = new JNIHandles();
        }
        return handles;
    }

    public Interpreter getInterpreter() {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        return interpreter;
    }

    public StubRoutines getStubRoutines() {
        if (stubRoutines == null) {
            stubRoutines = new StubRoutines();
        }
        return stubRoutines;
    }

    public VMRegImpl getVMRegImplInfo() {
        if (vmregImpl == null) {
            vmregImpl = new VMRegImpl();
        }
        return vmregImpl;
    }

    public Bytes getBytes() {
        if (bytes == null) {
            bytes = new Bytes(debugger.getMachineDescription());
        }
        return bytes;
    }

    public boolean isBigEndian() {
        return isBigEndian;
    }

    public boolean isCore() {
        return (!(usingClientCompiler || usingServerCompiler));
    }

    public boolean isClientCompiler() {
        return usingClientCompiler;
    }

    public boolean isServerCompiler() {
        return usingServerCompiler;
    }

    public boolean useDerivedPointerTable() {
        return !disableDerivedPointerTableCheck;
    }

    public CodeCache getCodeCache() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!isCore(), "noncore builds only");
        }
        if (codeCache == null) {
            codeCache = new CodeCache();
        }
        return codeCache;
    }

    public Runtime1 getRuntime1() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(isClientCompiler(), "C1 builds only");
        }
        if (runtime1 == null) {
            runtime1 = new Runtime1();
        }
        return runtime1;
    }

    public boolean isDebugging() {
        return (debugger != null);
    }

    public JVMDebugger getDebugger() {
        if (debugger == null) {
            throw new RuntimeException("Attempt to use debugger in runtime system");
        }
        return debugger;
    }

    public boolean isJavaPCDbg(Address addr) {
        return (getInterpreter().contains(addr) || getCodeCache().contains(addr));
    }

    public int getInvocationEntryBCI() {
        return invocationEntryBCI;
    }

    public boolean wizardMode() {
        return true;
    }

    public ReversePtrs getRevPtrs() {
        return revPtrs;
    }

    public void setRevPtrs(ReversePtrs rp) {
        revPtrs = rp;
    }

    public String getVMRelease() {
        return vmRelease;
    }

    public String getVMInternalInfo() {
        return vmInternalInfo;
    }

    public int getReserveForAllocationPrefetch() {
        return reserveForAllocationPrefetch;
    }

    public boolean isSharingEnabled() {
        if (sharingEnabled == null) {
            Flag flag = getCommandLineFlag("UseSharedSpaces");
            sharingEnabled = (flag == null) ? Boolean.FALSE : (flag.getBool() ? Boolean.TRUE : Boolean.FALSE);
        }
        return sharingEnabled.booleanValue();
    }

    public boolean isCompressedOopsEnabled() {
        if (compressedOopsEnabled == null) {
            Flag flag = getCommandLineFlag("UseCompressedOops");
            compressedOopsEnabled = (flag == null) ? Boolean.FALSE : (flag.getBool() ? Boolean.TRUE : Boolean.FALSE);
        }
        return compressedOopsEnabled.booleanValue();
    }

    public boolean isCompressedKlassPointersEnabled() {
        if (compressedKlassPointersEnabled == null) {
            Flag flag = getCommandLineFlag("UseCompressedClassPointers");
            compressedKlassPointersEnabled = (flag == null) ? Boolean.FALSE : (flag.getBool() ? Boolean.TRUE : Boolean.FALSE);
        }
        return compressedKlassPointersEnabled.booleanValue();
    }

    public int getObjectAlignmentInBytes() {
        if (objectAlignmentInBytes == 0) {
            Flag flag = getCommandLineFlag("ObjectAlignmentInBytes");
            objectAlignmentInBytes = (flag == null) ? 8 : (int) flag.getIntx();
        }
        return objectAlignmentInBytes;
    }

    public boolean getUseTLAB() {
        Flag flag = getCommandLineFlag("UseTLAB");
        return (flag == null) ? false : flag.getBool();
    }

    public Flag[] getCommandLineFlags() {
        if (commandLineFlags == null) {
            readCommandLineFlags();
        }
        return commandLineFlags;
    }

    public Flag getCommandLineFlag(String name) {
        if (flagsMap == null) {
            flagsMap = new HashMap();
            Flag[] flags = getCommandLineFlags();
            for (int i = 0; i < flags.length; i++) {
                flagsMap.put(flags[i].getName(), flags[i]);
            }
        }
        return (Flag) flagsMap.get(name);
    }

    private void readCommandLineFlags() {
        TypeDataBase db = getTypeDataBase();
        Type flagType = db.lookupType("Flag");
        int numFlags = (int) flagType.getCIntegerField("numFlags").getValue();
        commandLineFlags = new Flag[numFlags - 1];
        Address flagAddr = flagType.getAddressField("flags").getValue();
        AddressField typeFld = flagType.getAddressField("_type");
        AddressField nameFld = flagType.getAddressField("_name");
        AddressField addrFld = flagType.getAddressField("_addr");
        CIntField flagsFld = new CIntField(flagType.getCIntegerField("_flags"), 0);
        long flagSize = flagType.getSize();
        for (int f = 0; f < numFlags - 1; f++) {
            String type = CStringUtilities.getString(typeFld.getValue(flagAddr));
            String name = CStringUtilities.getString(nameFld.getValue(flagAddr));
            Address addr = addrFld.getValue(flagAddr);
            int flags = (int) flagsFld.getValue(flagAddr);
            commandLineFlags[f] = new Flag(type, name, addr, flags);
            flagAddr = flagAddr.addOffsetTo(flagSize);
        }
        Arrays.sort(commandLineFlags, new Comparator() {

            public int compare(Object o1, Object o2) {
                Flag f1 = (Flag) o1;
                Flag f2 = (Flag) o2;
                return f1.getName().compareTo(f2.getName());
            }
        });
    }

    public String getSystemProperty(String key) {
        Properties props = getSystemProperties();
        return (props != null) ? props.getProperty(key) : null;
    }

    public Properties getSystemProperties() {
        if (sysProps == null) {
            readSystemProperties();
        }
        return sysProps;
    }

    private void readSystemProperties() {
        final InstanceKlass systemKls = getSystemDictionary().getSystemKlass();
        systemKls.iterateStaticFields(new DefaultOopVisitor() {

            ObjectReader objReader = new ObjectReader();

            public void doOop(sun.jvm.hotspot.oops.OopField field, boolean isVMField) {
                if (field.getID().getName().equals("props")) {
                    try {
                        sysProps = (Properties) objReader.readObject(field.getValue(getObj()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
