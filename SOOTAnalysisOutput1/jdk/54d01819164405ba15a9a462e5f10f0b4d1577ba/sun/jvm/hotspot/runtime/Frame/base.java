package sun.jvm.hotspot.runtime;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.compiler.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.interpreter.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public abstract class Frame implements Cloneable {

    protected Address raw_sp;

    protected Address pc;

    protected boolean deoptimized;

    public Frame() {
        deoptimized = false;
    }

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static long ConstMethodSize;

    private static int pcReturnOffset;

    public static int pcReturnOffset() {
        return pcReturnOffset;
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type ConstMethodType = db.lookupType("ConstMethod");
        ConstMethodSize = ConstMethodType.getSize();
        pcReturnOffset = db.lookupIntConstant("frame::pc_return_offset").intValue();
    }

    protected int bcpToBci(Address bcp, ConstMethod cm) {
        if (bcp == null)
            return 0;
        long bci = bcp.minus(null);
        if (bci >= 0 && bci < cm.getCodeSize())
            return (int) bci;
        return (int) (bcp.minus(cm.getAddress()) - ConstMethodSize);
    }

    protected int bcpToBci(Address bcp, Method m) {
        return bcpToBci(bcp, m.getConstMethod());
    }

    public abstract Object clone();

    public Address getPC() {
        return pc;
    }

    public void setPC(Address newpc) {
        pc = newpc;
    }

    public boolean isDeoptimized() {
        return deoptimized;
    }

    public CodeBlob cb() {
        return VM.getVM().getCodeCache().findBlob(getPC());
    }

    public abstract Address getSP();

    public abstract Address getID();

    public abstract Address getFP();

    public abstract boolean equals(Object arg);

    public boolean isInterpretedFrame() {
        return VM.getVM().getInterpreter().contains(getPC());
    }

    public boolean isJavaFrame() {
        if (isInterpretedFrame())
            return true;
        if (!VM.getVM().isCore()) {
            if (isCompiledFrame())
                return true;
        }
        return false;
    }

    public boolean isEntryFrame() {
        return VM.getVM().getStubRoutines().returnsToCallStub(getPC());
    }

    public boolean isNativeFrame() {
        if (!VM.getVM().isCore()) {
            CodeBlob cb = VM.getVM().getCodeCache().findBlob(getPC());
            return (cb != null && cb.isNativeMethod());
        } else {
            return false;
        }
    }

    public boolean isCompiledFrame() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!VM.getVM().isCore(), "noncore builds only");
        }
        CodeBlob cb = VM.getVM().getCodeCache().findBlob(getPC());
        return (cb != null && cb.isJavaMethod());
    }

    public boolean isRuntimeFrame() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!VM.getVM().isCore(), "noncore builds only");
        }
        CodeBlob cb = VM.getVM().getCodeCache().findBlob(getPC());
        if (cb == null) {
            return false;
        }
        if (cb.isRuntimeStub())
            return true;
        else
            return false;
    }

    public boolean isFirstFrame() {
        return ((isEntryFrame() && entryFrameIsFirst()) || (!isJavaFrame() && !hasSenderPD()));
    }

    public boolean isFirstJavaFrame() {
        throw new RuntimeException("not yet implemented");
    }

    public abstract boolean isSignalHandlerFrameDbg();

    public abstract int getSignalNumberDbg();

    public abstract String getSignalNameDbg();

    public abstract boolean isInterpretedFrameValid();

    public boolean shouldBeDeoptimized() {
        throw new RuntimeException("not yet implemented");
    }

    public boolean canBeDeoptimized() {
        throw new RuntimeException("not yet implemented");
    }

    public abstract Frame sender(RegisterMap map, CodeBlob nm);

    public Frame sender(RegisterMap map) {
        return sender(map, null);
    }

    public Frame realSender(RegisterMap map) {
        if (!VM.getVM().isCore()) {
            Frame result = sender(map);
            while (result.isRuntimeFrame()) {
                result = result.sender(map);
            }
            return result;
        } else {
            return sender(map);
        }
    }

    protected abstract boolean hasSenderPD();

    public Address addressOfStackSlot(int slot) {
        return getFP().addOffsetTo(slot * VM.getVM().getAddressSize());
    }

    public OopHandle getOopHandleAt(int slot) {
        return addressOfStackSlot(slot).getOopHandleAt(0);
    }

    public int getIntAt(int slot) {
        return addressOfStackSlot(slot).getJIntAt(0);
    }

    public abstract long frameSize();

    public abstract Address getLink();

    public abstract Address getSenderPC();

    public abstract Address getUnextendedSP();

    public abstract Address getSenderSP();

    public abstract Address addressOfInterpreterFrameLocals();

    public Address addressOfInterpreterFrameLocal(int slot) {
        return addressOfInterpreterFrameLocals().getAddressAt(0).addOffsetTo(-slot * VM.getVM().getAddressSize());
    }

    public abstract int getInterpreterFrameBCI();

    public abstract Address addressOfInterpreterFrameExpressionStack();

    public abstract int getInterpreterFrameExpressionStackDirection();

    public Address addressOfInterpreterFrameExpressionStackSlot(int slot) {
        return addressOfInterpreterFrameExpressionStack().addOffsetTo(-slot * VM.getVM().getAddressSize());
    }

    public abstract Address addressOfInterpreterFrameTOS();

    public abstract Address addressOfInterpreterFrameTOSAt(int slot);

    public int getInterpreterFrameExpressionStackSize() {
        return (int) (1 + (getInterpreterFrameExpressionStackDirection() * (addressOfInterpreterFrameTOS().minus(addressOfInterpreterFrameExpressionStack()))));
    }

    public abstract Address getInterpreterFrameSenderSP();

    public abstract BasicObjectLock interpreterFrameMonitorBegin();

    public abstract BasicObjectLock interpreterFrameMonitorEnd();

    public abstract int interpreterFrameMonitorSize();

    public BasicObjectLock nextMonitorInInterpreterFrame(BasicObjectLock cur) {
        return new BasicObjectLock(cur.address().addOffsetTo(interpreterFrameMonitorSize()));
    }

    public BasicObjectLock previousMonitorInInterpreterFrame(BasicObjectLock cur) {
        return new BasicObjectLock(cur.address().addOffsetTo(-1 * interpreterFrameMonitorSize()));
    }

    public abstract Address addressOfInterpreterFrameMethod();

    public Method getInterpreterFrameMethod() {
        return (Method) Metadata.instantiateWrapperFor(addressOfInterpreterFrameMethod().getAddressAt(0));
    }

    public abstract Address addressOfInterpreterFrameCPCache();

    public ConstantPoolCache getInterpreterFrameCPCache() {
        return (ConstantPoolCache) Metadata.instantiateWrapperFor(addressOfInterpreterFrameCPCache().getAddressAt(0));
    }

    public abstract JavaCallWrapper getEntryFrameCallWrapper();

    public boolean entryFrameIsFirst() {
        return (getEntryFrameCallWrapper().getLastJavaSP() == null);
    }

    protected abstract Address addressOfSavedOopResult();

    protected abstract Address addressOfSavedReceiver();

    public OopHandle getSavedOopResult() {
        return addressOfSavedOopResult().getOopHandleAt(0);
    }

    public OopHandle getSavedReceiver() {
        return addressOfSavedReceiver().getOopHandleAt(0);
    }

    public void oopsInterpretedArgumentsDo(Symbol signature, boolean isStatic, AddressVisitor f) {
        ArgumentOopFinder finder = new ArgumentOopFinder(signature, isStatic, this, f);
        finder.oopsDo();
    }

    public Address oopMapRegToLocation(VMReg reg, RegisterMap regMap) {
        VMReg stack0 = VM.getVM().getVMRegImplInfo().getStack0();
        if (reg.lessThan(stack0)) {
            return regMap.getLocation(reg);
        } else {
            long spOffset = VM.getVM().getAddressSize() * reg.minus(stack0);
            return getUnextendedSP().addOffsetTo(spOffset);
        }
    }

    public void oopsDo(AddressVisitor oopVisitor, RegisterMap map) {
        if (isInterpretedFrame()) {
            oopsInterpretedDo(oopVisitor, map);
        } else if (isEntryFrame()) {
            oopsEntryDo(oopVisitor, map);
        } else if (VM.getVM().getCodeCache().contains(getPC())) {
            oopsCodeBlobDo(oopVisitor, map);
        } else {
            Assert.that(false, "should not reach here");
        }
    }

    public void printValue() {
        printValueOn(System.out);
    }

    public void printValueOn(PrintStream tty) {
    }

    public void print() {
        printOn(System.out);
    }

    public void printOn(PrintStream tty) {
    }

    public void interpreterFramePrintOn(PrintStream tty) {
    }

    private static class InterpVisitor implements OopMapVisitor {

        private AddressVisitor addressVisitor;

        public InterpVisitor(AddressVisitor oopVisitor) {
            setAddressVisitor(oopVisitor);
        }

        public void setAddressVisitor(AddressVisitor addressVisitor) {
            this.addressVisitor = addressVisitor;
        }

        public void visitOopLocation(Address oopAddr) {
            addressVisitor.visitAddress(oopAddr);
        }

        public void visitDerivedOopLocation(Address baseOopAddr, Address derivedOopAddr) {
            if (VM.getVM().isClientCompiler()) {
                Assert.that(false, "should not reach here");
            } else if (VM.getVM().isServerCompiler() && VM.getVM().useDerivedPointerTable()) {
                Assert.that(false, "FIXME: add derived pointer table");
            }
        }

        public void visitNarrowOopLocation(Address compOopAddr) {
            addressVisitor.visitCompOopAddress(compOopAddr);
        }
    }

    private void oopsInterpretedDo(AddressVisitor oopVisitor, RegisterMap map) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map != null, "map must be set");
        }
        Method m = getInterpreterFrameMethod();
        int bci = getInterpreterFrameBCI();
        if (VM.getVM().isDebugging()) {
            if (bci < 0 || bci >= m.getCodeSize())
                return;
        }
        if (Assert.ASSERTS_ENABLED) {
            Assert.that((m.isNative() && (bci == 0)) || ((bci >= 0) && (bci < m.getCodeSize())), "invalid bci value");
        }
        int maxLocals = (int) (m.isNative() ? m.getSizeOfParameters() : m.getMaxLocals());
        InterpreterFrameClosure blk = new InterpreterFrameClosure(this, maxLocals, (int) m.getMaxStack(), oopVisitor);
        OopMapCacheEntry mask = m.getMaskFor(bci);
        mask.iterateOop(blk);
        if (map.getIncludeArgumentOops() && !m.isNative()) {
            BytecodeInvoke call = BytecodeInvoke.atCheck(m, bci);
            if (call != null && getInterpreterFrameExpressionStackSize() > 0) {
                oopsInterpretedArgumentsDo(call.signature(), call.isInvokestatic(), oopVisitor);
            }
        }
    }

    private void oopsEntryDo(AddressVisitor oopVisitor, RegisterMap regMap) {
    }

    private void oopsCodeBlobDo(AddressVisitor oopVisitor, RegisterMap regMap) {
        CodeBlob cb = VM.getVM().getCodeCache().findBlob(getPC());
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(cb != null, "sanity check");
        }
        if (cb.getOopMaps() != null) {
            ImmutableOopMapSet.oopsDo(this, cb, regMap, oopVisitor, VM.getVM().isDebugging());
        }
    }
}

class InterpreterFrameClosure implements OffsetClosure {

    private static final boolean DEBUG = false;

    private Frame fr;

    private AddressVisitor f;

    private int maxLocals;

    private int maxStack;

    InterpreterFrameClosure(Frame fr, int maxLocals, int maxStack, AddressVisitor f) {
        this.fr = fr;
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.f = f;
    }

    public void offsetDo(int offset) {
        if (DEBUG) {
            System.err.println("Visiting offset " + offset + ", maxLocals = " + maxLocals + " for frame " + fr + ", method " + fr.getInterpreterFrameMethod().getMethodHolder().getName().asString() + fr.getInterpreterFrameMethod().getName().asString());
        }
        Address addr;
        if (offset < maxLocals) {
            addr = fr.addressOfInterpreterFrameLocal(offset);
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(AddressOps.gte(addr, fr.getSP()), "must be inside the frame");
            }
            if (DEBUG) {
                System.err.println("  Visiting local at addr " + addr);
            }
            f.visitAddress(addr);
        } else {
            addr = fr.addressOfInterpreterFrameExpressionStackSlot(offset - maxLocals);
            if (DEBUG) {
                System.err.println("  Address of expression stack slot: " + addr + ", TOS = " + fr.addressOfInterpreterFrameTOS());
            }
            boolean inStack;
            if (fr.getInterpreterFrameExpressionStackDirection() > 0) {
                inStack = AddressOps.lte(addr, fr.addressOfInterpreterFrameTOS());
            } else {
                inStack = AddressOps.gte(addr, fr.addressOfInterpreterFrameTOS());
            }
            if (inStack) {
                if (DEBUG) {
                    System.err.println("  In stack; visiting location.");
                }
                f.visitAddress(addr);
            } else if (DEBUG) {
                System.err.println("  *** WARNING: Address is out of bounds");
            }
        }
    }
}

class ArgumentOopFinder extends SignatureInfo {

    private AddressVisitor f;

    private int offset;

    private boolean isStatic;

    private Frame fr;

    protected void set(int size, int type) {
        offset -= size;
        if (type == BasicType.getTObject() || type == BasicType.getTArray())
            oopOffsetDo();
    }

    private void oopOffsetDo() {
        f.visitAddress(fr.addressOfInterpreterFrameTOSAt(offset));
    }

    public ArgumentOopFinder(Symbol signature, boolean isStatic, Frame fr, AddressVisitor f) {
        super(signature);
        int argsSize = new ArgumentSizeComputer(signature).size() + (isStatic ? 0 : 1);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!fr.isInterpretedFrame() || argsSize <= fr.getInterpreterFrameExpressionStackSize(), "args cannot be on stack anymore");
        }
        this.f = f;
        this.fr = fr;
        this.offset = argsSize;
        this.isStatic = isStatic;
    }

    public void oopsDo() {
        if (!isStatic) {
            --offset;
            oopOffsetDo();
        }
        iterateParameters();
    }
}
