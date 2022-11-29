package sun.jvm.hotspot.code;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class NMethod extends CodeBlob {

    private static long pcDescSize;

    private static AddressField methodField;

    private static CIntegerField entryBCIField;

    private static AddressField osrLinkField;

    private static AddressField scavengeRootLinkField;

    private static JByteField scavengeRootStateField;

    private static CIntegerField exceptionOffsetField;

    private static CIntegerField deoptOffsetField;

    private static CIntegerField deoptMhOffsetField;

    private static CIntegerField origPCOffsetField;

    private static CIntegerField stubOffsetField;

    private static CIntegerField oopsOffsetField;

    private static CIntegerField metadataOffsetField;

    private static CIntegerField scopesDataOffsetField;

    private static CIntegerField scopesPCsOffsetField;

    private static CIntegerField dependenciesOffsetField;

    private static CIntegerField handlerTableOffsetField;

    private static CIntegerField nulChkTableOffsetField;

    private static CIntegerField nmethodEndOffsetField;

    private static AddressField entryPointField;

    private static AddressField verifiedEntryPointField;

    private static AddressField osrEntryPointField;

    private static JIntField lockCountField;

    private static CIntegerField stackTraversalMarkField;

    private static CIntegerField compLevelField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("nmethod");
        methodField = type.getAddressField("_method");
        entryBCIField = type.getCIntegerField("_entry_bci");
        osrLinkField = type.getAddressField("_osr_link");
        scavengeRootLinkField = type.getAddressField("_scavenge_root_link");
        scavengeRootStateField = type.getJByteField("_scavenge_root_state");
        exceptionOffsetField = type.getCIntegerField("_exception_offset");
        deoptOffsetField = type.getCIntegerField("_deoptimize_offset");
        deoptMhOffsetField = type.getCIntegerField("_deoptimize_mh_offset");
        origPCOffsetField = type.getCIntegerField("_orig_pc_offset");
        stubOffsetField = type.getCIntegerField("_stub_offset");
        oopsOffsetField = type.getCIntegerField("_oops_offset");
        metadataOffsetField = type.getCIntegerField("_metadata_offset");
        scopesDataOffsetField = type.getCIntegerField("_scopes_data_offset");
        scopesPCsOffsetField = type.getCIntegerField("_scopes_pcs_offset");
        dependenciesOffsetField = type.getCIntegerField("_dependencies_offset");
        handlerTableOffsetField = type.getCIntegerField("_handler_table_offset");
        nulChkTableOffsetField = type.getCIntegerField("_nul_chk_table_offset");
        nmethodEndOffsetField = type.getCIntegerField("_nmethod_end_offset");
        entryPointField = type.getAddressField("_entry_point");
        verifiedEntryPointField = type.getAddressField("_verified_entry_point");
        osrEntryPointField = type.getAddressField("_osr_entry_point");
        lockCountField = type.getJIntField("_lock_count");
        stackTraversalMarkField = type.getCIntegerField("_stack_traversal_mark");
        compLevelField = type.getCIntegerField("_comp_level");
        pcDescSize = db.lookupType("PcDesc").getSize();
    }

    public NMethod(Address addr) {
        super(addr);
    }

    public Address getAddress() {
        return addr;
    }

    public Method getMethod() {
        return (Method) Metadata.instantiateWrapperFor(methodField.getValue(addr));
    }

    public boolean isNMethod() {
        return true;
    }

    public boolean isJavaMethod() {
        return !getMethod().isNative();
    }

    public boolean isNativeMethod() {
        return getMethod().isNative();
    }

    public boolean isOSRMethod() {
        return getEntryBCI() != VM.getVM().getInvocationEntryBCI();
    }

    public Address constantsBegin() {
        return contentBegin();
    }

    public Address constantsEnd() {
        return getEntryPoint();
    }

    public Address instsBegin() {
        return codeBegin();
    }

    public Address instsEnd() {
        return headerBegin().addOffsetTo(getStubOffset());
    }

    public Address exceptionBegin() {
        return headerBegin().addOffsetTo(getExceptionOffset());
    }

    public Address deoptHandlerBegin() {
        return headerBegin().addOffsetTo(getDeoptOffset());
    }

    public Address deoptMhHandlerBegin() {
        return headerBegin().addOffsetTo(getDeoptMhOffset());
    }

    public Address stubBegin() {
        return headerBegin().addOffsetTo(getStubOffset());
    }

    public Address stubEnd() {
        return headerBegin().addOffsetTo(getOopsOffset());
    }

    public Address oopsBegin() {
        return headerBegin().addOffsetTo(getOopsOffset());
    }

    public Address oopsEnd() {
        return headerBegin().addOffsetTo(getMetadataOffset());
    }

    public Address metadataBegin() {
        return headerBegin().addOffsetTo(getMetadataOffset());
    }

    public Address metadataEnd() {
        return headerBegin().addOffsetTo(getScopesDataOffset());
    }

    public Address scopesDataBegin() {
        return headerBegin().addOffsetTo(getScopesDataOffset());
    }

    public Address scopesDataEnd() {
        return headerBegin().addOffsetTo(getScopesPCsOffset());
    }

    public Address scopesPCsBegin() {
        return headerBegin().addOffsetTo(getScopesPCsOffset());
    }

    public Address scopesPCsEnd() {
        return headerBegin().addOffsetTo(getDependenciesOffset());
    }

    public Address dependenciesBegin() {
        return headerBegin().addOffsetTo(getDependenciesOffset());
    }

    public Address dependenciesEnd() {
        return headerBegin().addOffsetTo(getHandlerTableOffset());
    }

    public Address handlerTableBegin() {
        return headerBegin().addOffsetTo(getHandlerTableOffset());
    }

    public Address handlerTableEnd() {
        return headerBegin().addOffsetTo(getNulChkTableOffset());
    }

    public Address nulChkTableBegin() {
        return headerBegin().addOffsetTo(getNulChkTableOffset());
    }

    public Address nulChkTableEnd() {
        return headerBegin().addOffsetTo(getNMethodEndOffset());
    }

    public int constantsSize() {
        return (int) constantsEnd().minus(constantsBegin());
    }

    public int instsSize() {
        return (int) instsEnd().minus(instsBegin());
    }

    public int stubSize() {
        return (int) stubEnd().minus(stubBegin());
    }

    public int oopsSize() {
        return (int) oopsEnd().minus(oopsBegin());
    }

    public int metadataSize() {
        return (int) metadataEnd().minus(metadataBegin());
    }

    public int scopesDataSize() {
        return (int) scopesDataEnd().minus(scopesDataBegin());
    }

    public int scopesPCsSize() {
        return (int) scopesPCsEnd().minus(scopesPCsBegin());
    }

    public int dependenciesSize() {
        return (int) dependenciesEnd().minus(dependenciesBegin());
    }

    public int handlerTableSize() {
        return (int) handlerTableEnd().minus(handlerTableBegin());
    }

    public int nulChkTableSize() {
        return (int) nulChkTableEnd().minus(nulChkTableBegin());
    }

    public int origPCOffset() {
        return (int) origPCOffsetField.getValue(addr);
    }

    public int totalSize() {
        return constantsSize() + instsSize() + stubSize() + scopesDataSize() + scopesPCsSize() + dependenciesSize() + handlerTableSize() + nulChkTableSize();
    }

    public boolean constantsContains(Address addr) {
        return constantsBegin().lessThanOrEqual(addr) && constantsEnd().greaterThan(addr);
    }

    public boolean instsContains(Address addr) {
        return instsBegin().lessThanOrEqual(addr) && instsEnd().greaterThan(addr);
    }

    public boolean stubContains(Address addr) {
        return stubBegin().lessThanOrEqual(addr) && stubEnd().greaterThan(addr);
    }

    public boolean oopsContains(Address addr) {
        return oopsBegin().lessThanOrEqual(addr) && oopsEnd().greaterThan(addr);
    }

    public boolean metadataContains(Address addr) {
        return metadataBegin().lessThanOrEqual(addr) && metadataEnd().greaterThan(addr);
    }

    public boolean scopesDataContains(Address addr) {
        return scopesDataBegin().lessThanOrEqual(addr) && scopesDataEnd().greaterThan(addr);
    }

    public boolean scopesPCsContains(Address addr) {
        return scopesPCsBegin().lessThanOrEqual(addr) && scopesPCsEnd().greaterThan(addr);
    }

    public boolean handlerTableContains(Address addr) {
        return handlerTableBegin().lessThanOrEqual(addr) && handlerTableEnd().greaterThan(addr);
    }

    public boolean nulChkTableContains(Address addr) {
        return nulChkTableBegin().lessThanOrEqual(addr) && nulChkTableEnd().greaterThan(addr);
    }

    public int getOopsLength() {
        return (int) (oopsSize() / VM.getVM().getOopSize());
    }

    public int getMetadataLength() {
        return (int) (metadataSize() / VM.getVM().getOopSize());
    }

    public Address getEntryPoint() {
        return entryPointField.getValue(addr);
    }

    public Address getVerifiedEntryPoint() {
        return verifiedEntryPointField.getValue(addr);
    }

    public OopHandle getOopAt(int index) {
        if (index == 0)
            return null;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(index > 0 && index <= getOopsLength(), "must be a valid non-zero index");
        }
        return oopsBegin().getOopHandleAt((index - 1) * VM.getVM().getOopSize());
    }

    public Address getMetadataAt(int index) {
        if (index == 0)
            return null;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(index > 0 && index <= getMetadataLength(), "must be a valid non-zero index");
        }
        return metadataBegin().getAddressAt((index - 1) * VM.getVM().getOopSize());
    }

    public Method getMethodAt(int index) {
        return (Method) Metadata.instantiateWrapperFor(getMetadataAt(index));
    }

    public boolean isZombie() {
        return false;
    }

    public int getOSREntryBCI() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(getEntryBCI() != VM.getVM().getInvocationEntryBCI(), "wrong kind of nmethod");
        }
        return getEntryBCI();
    }

    public NMethod getOSRLink() {
        return (NMethod) VMObjectFactory.newObject(NMethod.class, osrLinkField.getValue(addr));
    }

    public NMethod getScavengeRootLink() {
        return (NMethod) VMObjectFactory.newObject(NMethod.class, scavengeRootLinkField.getValue(addr));
    }

    public int getScavengeRootState() {
        return (int) scavengeRootStateField.getValue(addr);
    }

    public boolean isMethodHandleReturn(Address returnPc) {
        PCDesc pd = getPCDescAt(returnPc);
        if (pd == null)
            return false;
        return pd.isMethodHandleInvoke();
    }

    public boolean isDeoptPc(Address pc) {
        return isDeoptEntry(pc) || isDeoptMhEntry(pc);
    }

    public boolean isDeoptEntry(Address pc) {
        return pc == deoptHandlerBegin();
    }

    public boolean isDeoptMhEntry(Address pc) {
        return pc == deoptMhHandlerBegin();
    }

    public boolean canBeDeoptimized() {
        return isJavaMethod();
    }

    public boolean isLockedByVM() {
        return lockCountField.getValue(addr) > 0;
    }

    public PCDesc getPCDescAt(Address pc) {
        for (Address p = scopesPCsBegin(); p.lessThan(scopesPCsEnd()); p = p.addOffsetTo(pcDescSize)) {
            PCDesc pcDesc = new PCDesc(p);
            if (pcDesc.getRealPC(this).equals(pc)) {
                return pcDesc;
            }
        }
        return null;
    }

    public ScopeDesc getScopeDescAt(Address pc) {
        PCDesc pd = getPCDescAt(pc);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(pd != null, "scope must be present");
        }
        return new ScopeDesc(this, pd.getScopeDecodeOffset(), pd.getObjDecodeOffset(), pd.getReexecute());
    }

    public PCDesc getPCDescNearDbg(Address pc) {
        PCDesc bestGuessPCDesc = null;
        long bestDistance = 0;
        for (Address p = scopesPCsBegin(); p.lessThan(scopesPCsEnd()); p = p.addOffsetTo(pcDescSize)) {
            PCDesc pcDesc = new PCDesc(p);
            long distance = -pcDesc.getRealPC(this).minus(pc);
            if ((bestGuessPCDesc == null) || ((distance >= 0) && (distance < bestDistance))) {
                bestGuessPCDesc = pcDesc;
                bestDistance = distance;
            }
        }
        return bestGuessPCDesc;
    }

    PCDesc find_pc_desc(long pc, boolean approximate) {
        return find_pc_desc_internal(pc, approximate);
    }

    PCDesc find_pc_desc_internal(long pc, boolean approximate) {
        long base_address = VM.getAddressValue(codeBegin());
        int pc_offset = (int) (pc - base_address);
        Address lower = scopesPCsBegin();
        Address upper = scopesPCsEnd();
        upper = upper.addOffsetTo(-pcDescSize);
        if (lower.greaterThan(upper))
            return null;
        int LOG2_RADIX = 4;
        int RADIX = (1 << LOG2_RADIX);
        Address mid;
        for (int step = (1 << (LOG2_RADIX * 3)); step > 1; step >>= LOG2_RADIX) {
            while ((mid = lower.addOffsetTo(step * pcDescSize)).lessThan(upper)) {
                PCDesc m = new PCDesc(mid);
                if (m.getPCOffset() < pc_offset) {
                    lower = mid;
                } else {
                    upper = mid;
                    break;
                }
            }
        }
        while (true) {
            mid = lower.addOffsetTo(pcDescSize);
            PCDesc m = new PCDesc(mid);
            if (m.getPCOffset() < pc_offset) {
                lower = mid;
            } else {
                upper = mid;
                break;
            }
        }
        PCDesc u = new PCDesc(upper);
        if (match_desc(u, pc_offset, approximate)) {
            return u;
        } else {
            return null;
        }
    }

    PCDesc pc_desc_at(long pc) {
        return find_pc_desc(pc, false);
    }

    PCDesc pc_desc_near(long pc) {
        return find_pc_desc(pc, true);
    }

    public ScopeDesc scope_desc_in(long begin, long end) {
        PCDesc p = pc_desc_near(begin + 1);
        if (p != null && VM.getAddressValue(p.getRealPC(this)) <= end) {
            return new ScopeDesc(this, p.getScopeDecodeOffset(), p.getObjDecodeOffset(), p.getReexecute());
        }
        return null;
    }

    static boolean match_desc(PCDesc pc, int pc_offset, boolean approximate) {
        if (!approximate) {
            return pc.getPCOffset() == pc_offset;
        } else {
            PCDesc prev = new PCDesc(pc.getAddress().addOffsetTo(-pcDescSize));
            return prev.getPCOffset() < pc_offset && pc_offset <= pc.getPCOffset();
        }
    }

    public ScopeDesc getScopeDescNearDbg(Address pc) {
        PCDesc pd = getPCDescNearDbg(pc);
        if (pd == null)
            return null;
        return new ScopeDesc(this, pd.getScopeDecodeOffset(), pd.getObjDecodeOffset(), pd.getReexecute());
    }

    public Map getSafepoints() {
        Map safepoints = new HashMap();
        sun.jvm.hotspot.debugger.Address p = null;
        for (p = scopesPCsBegin(); p.lessThan(scopesPCsEnd()); p = p.addOffsetTo(pcDescSize)) {
            PCDesc pcDesc = new PCDesc(p);
            sun.jvm.hotspot.debugger.Address pc = pcDesc.getRealPC(this);
            safepoints.put(pc, pcDesc);
        }
        return safepoints;
    }

    public static int getEntryPointOffset() {
        return (int) entryPointField.getOffset();
    }

    public static int getVerifiedEntryPointOffset() {
        return (int) verifiedEntryPointField.getOffset();
    }

    public static int getOSREntryPointOffset() {
        return (int) osrEntryPointField.getOffset();
    }

    public static int getEntryBCIOffset() {
        return (int) entryBCIField.getOffset();
    }

    public static int getMethodOffset() {
        return (int) methodField.getOffset();
    }

    public void print() {
        printOn(System.out);
    }

    protected void printComponentsOn(PrintStream tty) {
        tty.println(" content: [" + contentBegin() + ", " + contentEnd() + "), " + " code: [" + codeBegin() + ", " + codeEnd() + "), " + " data: [" + dataBegin() + ", " + dataEnd() + "), " + " oops: [" + oopsBegin() + ", " + oopsEnd() + "), " + " frame size: " + getFrameSize());
    }

    public String toString() {
        Method method = getMethod();
        return "NMethod for " + method.getMethodHolder().getName().asString() + "." + method.getName().asString() + method.getSignature().asString() + "==>n" + super.toString();
    }

    public String flagsToString() {
        return "";
    }

    public String getName() {
        Method method = getMethod();
        return "NMethod for " + method.getMethodHolder().getName().asString() + "." + method.getName().asString() + method.getSignature().asString();
    }

    public void dumpReplayData(PrintStream out) {
        HashMap h = new HashMap();
        for (int i = 1; i < getMetadataLength(); i++) {
            Metadata meta = Metadata.instantiateWrapperFor(getMetadataAt(i));
            System.err.println(meta);
            if (h.get(meta) != null)
                continue;
            h.put(meta, meta);
            if (meta instanceof InstanceKlass) {
                ((InstanceKlass) meta).dumpReplayData(out);
            } else if (meta instanceof Method) {
                ((Method) meta).dumpReplayData(out);
                MethodData mdo = ((Method) meta).getMethodData();
                if (mdo != null) {
                    mdo.dumpReplayData(out);
                }
            }
        }
        Method method = getMethod();
        if (h.get(method) == null) {
            method.dumpReplayData(out);
            MethodData mdo = method.getMethodData();
            if (mdo != null) {
                mdo.dumpReplayData(out);
            }
        }
        if (h.get(method.getMethodHolder()) == null) {
            ((InstanceKlass) method.getMethodHolder()).dumpReplayData(out);
        }
        Klass holder = method.getMethodHolder();
        out.println("compile " + holder.getName().asString() + " " + OopUtilities.escapeString(method.getName().asString()) + " " + method.getSignature().asString() + " " + getEntryBCI() + " " + getCompLevel());
    }

    private int getEntryBCI() {
        return (int) entryBCIField.getValue(addr);
    }

    private int getExceptionOffset() {
        return (int) exceptionOffsetField.getValue(addr);
    }

    private int getDeoptOffset() {
        return (int) deoptOffsetField.getValue(addr);
    }

    private int getDeoptMhOffset() {
        return (int) deoptMhOffsetField.getValue(addr);
    }

    private int getStubOffset() {
        return (int) stubOffsetField.getValue(addr);
    }

    private int getOopsOffset() {
        return (int) oopsOffsetField.getValue(addr);
    }

    private int getMetadataOffset() {
        return (int) metadataOffsetField.getValue(addr);
    }

    private int getScopesDataOffset() {
        return (int) scopesDataOffsetField.getValue(addr);
    }

    private int getScopesPCsOffset() {
        return (int) scopesPCsOffsetField.getValue(addr);
    }

    private int getDependenciesOffset() {
        return (int) dependenciesOffsetField.getValue(addr);
    }

    private int getHandlerTableOffset() {
        return (int) handlerTableOffsetField.getValue(addr);
    }

    private int getNulChkTableOffset() {
        return (int) nulChkTableOffsetField.getValue(addr);
    }

    private int getNMethodEndOffset() {
        return (int) nmethodEndOffsetField.getValue(addr);
    }

    private int getCompLevel() {
        return (int) compLevelField.getValue(addr);
    }
}
