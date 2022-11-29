package sun.jvm.hotspot.runtime.ppc64;

import java.util.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.compiler.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class PPC64Frame extends Frame {

    private static final boolean DEBUG;

    static {
        DEBUG = System.getProperty("sun.jvm.hotspot.runtime.ppc64.PPC64Frame.DEBUG") != null;
    }

    private static final int SENDER_SP_OFFSET = 0;

    private static final int INTERPRETER_FRAME_MIRROR_OFFSET = -3;

    private static final int INTERPRETER_FRAME_SENDER_SP_OFFSET = -4;

    private static final int INTERPRETER_FRAME_LAST_SP_OFFSET = INTERPRETER_FRAME_SENDER_SP_OFFSET - 1;

    private static final int INTERPRETER_FRAME_MDX_OFFSET = INTERPRETER_FRAME_LAST_SP_OFFSET - 1;

    private static final int INTERPRETER_FRAME_ESP_OFFSET = INTERPRETER_FRAME_MDX_OFFSET - 1;

    private static final int INTERPRETER_FRAME_BCX_OFFSET = INTERPRETER_FRAME_ESP_OFFSET - 1;

    private static final int INTERPRETER_FRAME_CACHE_OFFSET = INTERPRETER_FRAME_BCX_OFFSET - 1;

    private static final int INTERPRETER_FRAME_MONITORS_OFFSET = INTERPRETER_FRAME_CACHE_OFFSET - 1;

    private static final int INTERPRETER_FRAME_LOCALS_OFFSET = INTERPRETER_FRAME_MONITORS_OFFSET - 1;

    private static final int INTERPRETER_FRAME_METHOD_OFFSET = INTERPRETER_FRAME_LOCALS_OFFSET - 1;

    private static final int INTERPRETER_FRAME_INITIAL_SP_OFFSET = INTERPRETER_FRAME_BCX_OFFSET - 1;

    private static final int INTERPRETER_FRAME_MONITOR_BLOCK_TOP_OFFSET = INTERPRETER_FRAME_INITIAL_SP_OFFSET;

    private static final int INTERPRETER_FRAME_MONITOR_BLOCK_BOTTOM_OFFSET = INTERPRETER_FRAME_INITIAL_SP_OFFSET;

    private static int ENTRY_FRAME_CALL_WRAPPER_OFFSET;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        int entry_frame_locals_size = db.lookupIntConstant("frame::entry_frame_locals_size").intValue();
        int wordLength = (int) VM.getVM().getAddressSize();
        ENTRY_FRAME_CALL_WRAPPER_OFFSET = -entry_frame_locals_size / wordLength;
    }

    Address raw_fp;

    private Address raw_unextendedSP;

    private PPC64Frame() {
    }

    private void adjustForDeopt() {
        if (pc != null) {
            CodeBlob cb = VM.getVM().getCodeCache().findBlob(pc);
            if (cb != null && cb.isJavaMethod()) {
                NMethod nm = (NMethod) cb;
                if (pc.equals(nm.deoptHandlerBegin())) {
                    if (Assert.ASSERTS_ENABLED) {
                        Assert.that(this.getUnextendedSP() != null, "null SP in Java frame");
                    }
                    pc = this.getUnextendedSP().getAddressAt(nm.origPCOffset());
                    deoptimized = true;
                }
            }
        }
    }

    public PPC64Frame(Address raw_sp, Address raw_fp, Address pc) {
        this.raw_sp = raw_sp;
        this.raw_unextendedSP = raw_sp;
        if (raw_fp == null) {
            this.raw_fp = raw_sp.getAddressAt(0);
        } else {
            this.raw_fp = raw_fp;
        }
        if (pc == null) {
            this.pc = raw_sp.getAddressAt(2 * VM.getVM().getAddressSize());
        } else {
            this.pc = pc;
        }
        adjustUnextendedSP();
        adjustForDeopt();
        if (DEBUG) {
            System.out.println("PPC64Frame(sp, fp, pc): " + this);
            dumpStack();
        }
    }

    public PPC64Frame(Address raw_sp, Address raw_fp) {
        this.raw_sp = raw_sp;
        this.raw_unextendedSP = raw_sp;
        if (raw_fp == null) {
            this.raw_fp = raw_sp.getAddressAt(0);
        } else {
            this.raw_fp = raw_fp;
        }
        this.pc = raw_sp.getAddressAt(2 * VM.getVM().getAddressSize());
        adjustUnextendedSP();
        adjustForDeopt();
        if (DEBUG) {
            System.out.println("PPC64Frame(sp, fp): " + this);
            dumpStack();
        }
    }

    public PPC64Frame(Address raw_sp, Address raw_unextendedSp, Address raw_fp, Address pc) {
        this.raw_sp = raw_sp;
        this.raw_unextendedSP = raw_unextendedSp;
        if (raw_fp == null) {
            this.raw_fp = raw_sp.getAddressAt(0);
        } else {
            this.raw_fp = raw_fp;
        }
        if (pc == null) {
            this.pc = raw_sp.getAddressAt(2 * VM.getVM().getAddressSize());
        } else {
            this.pc = pc;
        }
        adjustUnextendedSP();
        adjustForDeopt();
        if (DEBUG) {
            System.out.println("PPC64Frame(sp, unextendedSP, fp, pc): " + this);
            dumpStack();
        }
    }

    public Object clone() {
        PPC64Frame frame = new PPC64Frame();
        frame.raw_sp = raw_sp;
        frame.raw_unextendedSP = raw_unextendedSP;
        frame.raw_fp = raw_fp;
        frame.pc = pc;
        frame.deoptimized = deoptimized;
        return frame;
    }

    public boolean equals(Object arg) {
        if (arg == null) {
            return false;
        }
        if (!(arg instanceof PPC64Frame)) {
            return false;
        }
        PPC64Frame other = (PPC64Frame) arg;
        return (AddressOps.equal(getSP(), other.getSP()) && AddressOps.equal(getUnextendedSP(), other.getUnextendedSP()) && AddressOps.equal(getFP(), other.getFP()) && AddressOps.equal(getPC(), other.getPC()));
    }

    public int hashCode() {
        if (raw_sp == null) {
            return 0;
        }
        return raw_sp.hashCode();
    }

    public String toString() {
        return "sp: " + (getSP() == null ? "null" : getSP().toString()) + ", unextendedSP: " + (getUnextendedSP() == null ? "null" : getUnextendedSP().toString()) + ", fp: " + (getFP() == null ? "null" : getFP().toString()) + ", pc: " + (pc == null ? "null" : pc.toString());
    }

    public Address getFP() {
        return raw_fp;
    }

    public Address getSP() {
        return raw_sp;
    }

    public Address getID() {
        return raw_sp;
    }

    public boolean isSignalHandlerFrameDbg() {
        return false;
    }

    public int getSignalNumberDbg() {
        return 0;
    }

    public String getSignalNameDbg() {
        return null;
    }

    public boolean isInterpretedFrameValid() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(isInterpretedFrame(), "Not an interpreted frame");
        }
        if (getFP() == null || getFP().andWithMask(0x3) != null) {
            return false;
        }
        if (getSP() == null || getSP().andWithMask(0x3) != null) {
            return false;
        }
        if (getFP().lessThanOrEqual(getSP())) {
            return false;
        }
        if (getFP().minus(getSP()) > 4096 * VM.getVM().getAddressSize()) {
            return false;
        }
        return true;
    }

    public Frame sender(RegisterMap regMap, CodeBlob cb) {
        PPC64RegisterMap map = (PPC64RegisterMap) regMap;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map != null, "map must be set");
        }
        map.setIncludeArgumentOops(false);
        if (isEntryFrame())
            return senderForEntryFrame(map);
        if (isInterpretedFrame())
            return senderForInterpreterFrame(map);
        if (cb == null) {
            cb = VM.getVM().getCodeCache().findBlob(getPC());
        } else {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(cb.equals(VM.getVM().getCodeCache().findBlob(getPC())), "Must be the same");
            }
        }
        if (cb != null) {
            return senderForCompiledFrame(map, cb);
        }
        return new PPC64Frame(getSenderSP(), getLink(), getSenderPC());
    }

    private Frame senderForEntryFrame(PPC64RegisterMap map) {
        if (DEBUG) {
            System.out.println("senderForEntryFrame");
        }
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map != null, "map must be set");
        }
        PPC64JavaCallWrapper jcw = (PPC64JavaCallWrapper) getEntryFrameCallWrapper();
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!entryFrameIsFirst(), "next Java fp must be non zero");
            Assert.that(jcw.getLastJavaSP().greaterThan(getSP()), "must be above this frame on stack");
        }
        PPC64Frame fr;
        if (jcw.getLastJavaPC() != null) {
            fr = new PPC64Frame(jcw.getLastJavaSP(), jcw.getLastJavaFP(), jcw.getLastJavaPC());
        } else {
            fr = new PPC64Frame(jcw.getLastJavaSP(), jcw.getLastJavaFP());
        }
        map.clear();
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map.getIncludeArgumentOops(), "should be set by clear");
        }
        return fr;
    }

    private void adjustUnextendedSP() {
        raw_unextendedSP = getFP();
    }

    private Frame senderForInterpreterFrame(PPC64RegisterMap map) {
        if (DEBUG) {
            System.out.println("senderForInterpreterFrame");
        }
        Address unextendedSP = addressOfStackSlot(INTERPRETER_FRAME_SENDER_SP_OFFSET).getAddressAt(0);
        Address sp = getSenderSP();
        return new PPC64Frame(sp, unextendedSP, getLink(), getSenderPC());
    }

    private Frame senderForCompiledFrame(PPC64RegisterMap map, CodeBlob cb) {
        if (DEBUG) {
            System.out.println("senderForCompiledFrame");
        }
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map != null, "map must be set");
        }
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(cb.getFrameSize() >= 0, "must have non-zero frame size");
        }
        Address senderSP = getSenderSP();
        Address senderPC = getSenderPC();
        if (map.getUpdateMap()) {
            map.setIncludeArgumentOops(cb.callerMustGCArguments());
            if (cb.getOopMaps() != null) {
                ImmutableOopMapSet.updateRegisterMap(this, cb, map, true);
            }
        }
        return new PPC64Frame(senderSP, getLink(), senderPC);
    }

    protected boolean hasSenderPD() {
        return true;
    }

    public long frameSize() {
        return (getSenderSP().minus(getSP()) / VM.getVM().getAddressSize());
    }

    public Address getLink() {
        return getSenderSP().getAddressAt(0);
    }

    public Address getUnextendedSP() {
        return raw_unextendedSP;
    }

    public Address getSenderPC() {
        return getSenderSP().getAddressAt(2 * VM.getVM().getAddressSize());
    }

    public Address getSenderSP() {
        return getFP();
    }

    public Address addressOfInterpreterFrameLocals() {
        return addressOfStackSlot(INTERPRETER_FRAME_LOCALS_OFFSET);
    }

    private Address addressOfInterpreterFrameBCX() {
        return addressOfStackSlot(INTERPRETER_FRAME_BCX_OFFSET);
    }

    public int getInterpreterFrameBCI() {
        Address bcp = addressOfInterpreterFrameBCX().getAddressAt(0);
        Address methodHandle = addressOfInterpreterFrameMethod().getAddressAt(0);
        Method method = (Method) Metadata.instantiateWrapperFor(methodHandle);
        return bcpToBci(bcp, method);
    }

    public Address addressOfInterpreterFrameMDX() {
        return addressOfStackSlot(INTERPRETER_FRAME_MDX_OFFSET);
    }

    public Address addressOfInterpreterFrameExpressionStack() {
        Address monitorEnd = interpreterFrameMonitorEnd().address();
        return monitorEnd.addOffsetTo(-1 * VM.getVM().getAddressSize());
    }

    public int getInterpreterFrameExpressionStackDirection() {
        return -1;
    }

    public Address addressOfInterpreterFrameTOS() {
        return getSP();
    }

    public Address addressOfInterpreterFrameTOSAt(int slot) {
        return addressOfInterpreterFrameTOS().addOffsetTo(slot * VM.getVM().getAddressSize());
    }

    public Address getInterpreterFrameSenderSP() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(isInterpretedFrame(), "interpreted frame expected");
        }
        return addressOfStackSlot(INTERPRETER_FRAME_SENDER_SP_OFFSET).getAddressAt(0);
    }

    public BasicObjectLock interpreterFrameMonitorBegin() {
        return new BasicObjectLock(addressOfStackSlot(INTERPRETER_FRAME_MONITOR_BLOCK_BOTTOM_OFFSET));
    }

    public BasicObjectLock interpreterFrameMonitorEnd() {
        Address result = addressOfStackSlot(INTERPRETER_FRAME_MONITOR_BLOCK_TOP_OFFSET).getAddressAt(0);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(AddressOps.gt(getFP(), result), "result must <  than frame pointer");
            Assert.that(AddressOps.lte(getSP(), result), "result must >= than stack pointer");
        }
        return new BasicObjectLock(result);
    }

    public int interpreterFrameMonitorSize() {
        return BasicObjectLock.size();
    }

    public Address addressOfInterpreterFrameMethod() {
        return addressOfStackSlot(INTERPRETER_FRAME_METHOD_OFFSET);
    }

    public Address addressOfInterpreterFrameCPCache() {
        return addressOfStackSlot(INTERPRETER_FRAME_CACHE_OFFSET);
    }

    public JavaCallWrapper getEntryFrameCallWrapper() {
        return new PPC64JavaCallWrapper(addressOfStackSlot(ENTRY_FRAME_CALL_WRAPPER_OFFSET).getAddressAt(0));
    }

    protected Address addressOfSavedOopResult() {
        return getSP().addOffsetTo((VM.getVM().isClientCompiler() ? 2 : 3) * VM.getVM().getAddressSize());
    }

    protected Address addressOfSavedReceiver() {
        return getSP().addOffsetTo(-4 * VM.getVM().getAddressSize());
    }

    private void dumpStack() {
        if (getFP() != null) {
            for (Address addr = getSP().addOffsetTo(-5 * VM.getVM().getAddressSize()); AddressOps.lte(addr, getFP().addOffsetTo(5 * VM.getVM().getAddressSize())); addr = addr.addOffsetTo(VM.getVM().getAddressSize())) {
                System.out.println(addr + ": " + addr.getAddressAt(0));
            }
        } else {
            for (Address addr = getSP().addOffsetTo(-5 * VM.getVM().getAddressSize()); AddressOps.lte(addr, getSP().addOffsetTo(20 * VM.getVM().getAddressSize())); addr = addr.addOffsetTo(VM.getVM().getAddressSize())) {
                System.out.println(addr + ": " + addr.getAddressAt(0));
            }
        }
    }
}
