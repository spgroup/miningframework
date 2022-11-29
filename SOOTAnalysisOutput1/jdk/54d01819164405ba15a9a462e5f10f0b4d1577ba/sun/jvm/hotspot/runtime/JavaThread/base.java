package sun.jvm.hotspot.runtime;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class JavaThread extends Thread {

    private static final boolean DEBUG = System.getProperty("sun.jvm.hotspot.runtime.JavaThread.DEBUG") != null;

    private static AddressField nextField;

    private static sun.jvm.hotspot.types.OopField threadObjField;

    private static AddressField anchorField;

    private static AddressField lastJavaSPField;

    private static AddressField lastJavaPCField;

    private static CIntegerField threadStateField;

    private static AddressField osThreadField;

    private static AddressField stackBaseField;

    private static CIntegerField stackSizeField;

    private static JavaThreadPDAccess access;

    private static int UNINITIALIZED;

    private static int NEW;

    private static int NEW_TRANS;

    private static int IN_NATIVE;

    private static int IN_NATIVE_TRANS;

    private static int IN_VM;

    private static int IN_VM_TRANS;

    private static int IN_JAVA;

    private static int IN_JAVA_TRANS;

    private static int BLOCKED;

    private static int BLOCKED_TRANS;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("JavaThread");
        Type anchorType = db.lookupType("JavaFrameAnchor");
        nextField = type.getAddressField("_next");
        threadObjField = type.getOopField("_threadObj");
        anchorField = type.getAddressField("_anchor");
        lastJavaSPField = anchorType.getAddressField("_last_Java_sp");
        lastJavaPCField = anchorType.getAddressField("_last_Java_pc");
        threadStateField = type.getCIntegerField("_thread_state");
        osThreadField = type.getAddressField("_osthread");
        stackBaseField = type.getAddressField("_stack_base");
        stackSizeField = type.getCIntegerField("_stack_size");
        UNINITIALIZED = db.lookupIntConstant("_thread_uninitialized").intValue();
        NEW = db.lookupIntConstant("_thread_new").intValue();
        NEW_TRANS = db.lookupIntConstant("_thread_new_trans").intValue();
        IN_NATIVE = db.lookupIntConstant("_thread_in_native").intValue();
        IN_NATIVE_TRANS = db.lookupIntConstant("_thread_in_native_trans").intValue();
        IN_VM = db.lookupIntConstant("_thread_in_vm").intValue();
        IN_VM_TRANS = db.lookupIntConstant("_thread_in_vm_trans").intValue();
        IN_JAVA = db.lookupIntConstant("_thread_in_Java").intValue();
        IN_JAVA_TRANS = db.lookupIntConstant("_thread_in_Java_trans").intValue();
        BLOCKED = db.lookupIntConstant("_thread_blocked").intValue();
        BLOCKED_TRANS = db.lookupIntConstant("_thread_blocked_trans").intValue();
    }

    public JavaThread(Address addr) {
        super(addr);
    }

    void setThreadPDAccess(JavaThreadPDAccess access) {
        this.access = access;
    }

    public JavaThread next() {
        Address threadAddr = nextField.getValue(addr);
        if (threadAddr == null) {
            return null;
        }
        return VM.getVM().getThreads().createJavaThreadWrapper(threadAddr);
    }

    public boolean isJavaThread() {
        return true;
    }

    public static AddressField getAnchorField() {
        return anchorField;
    }

    public Address getLastJavaSP() {
        Address sp = lastJavaSPField.getValue(addr.addOffsetTo(anchorField.getOffset()));
        return sp;
    }

    public Address getLastJavaPC() {
        Address pc = lastJavaPCField.getValue(addr.addOffsetTo(anchorField.getOffset()));
        return pc;
    }

    public Address getLastJavaFP() {
        return access.getLastJavaFP(addr);
    }

    public Address getBaseOfStackPointer() {
        return access.getBaseOfStackPointer(addr);
    }

    public boolean hasLastJavaFrame() {
        return (getLastJavaSP() != null);
    }

    public Frame getLastFrame() {
        return cookLastFrame(getLastFramePD());
    }

    protected Frame getLastFramePD() {
        return access.getLastFramePD(this, addr);
    }

    public JavaVFrame getLastJavaVFrame(RegisterMap regMap) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(regMap != null, "a map must be given");
        }
        Frame f = getLastFrame();
        if (f == null) {
            return null;
        }
        for (VFrame vf = VFrame.newVFrame(f, regMap, this); vf != null; vf = vf.sender()) {
            if (vf.isJavaFrame()) {
                return (JavaVFrame) vf;
            }
        }
        return null;
    }

    public JavaVFrame getLastJavaVFrameDbg() {
        RegisterMap regMap = newRegisterMap(true);
        sun.jvm.hotspot.runtime.Frame f = getCurrentFrameGuess();
        if (f == null)
            return null;
        boolean imprecise = true;
        if (f.isInterpretedFrame() && !f.isInterpretedFrameValid()) {
            if (DEBUG) {
                System.out.println("Correcting for invalid interpreter frame");
            }
            f = f.sender(regMap);
            imprecise = false;
        }
        VFrame vf = VFrame.newVFrame(f, regMap, this, true, imprecise);
        if (vf == null) {
            if (DEBUG) {
                System.out.println(" (Unable to create vframe for topmost frame guess)");
            }
            return null;
        }
        return vf.isJavaFrame() ? (JavaVFrame) vf : vf.javaSender();
    }

    public RegisterMap newRegisterMap(boolean updateMap) {
        return access.newRegisterMap(this, updateMap);
    }

    public Frame getCurrentFrameGuess() {
        return access.getCurrentFrameGuess(this, addr);
    }

    public void printThreadIDOn(PrintStream tty) {
        access.printThreadIDOn(addr, tty);
    }

    public void printThreadID() {
        printThreadIDOn(System.out);
    }

    public ThreadProxy getThreadProxy() {
        return access.getThreadProxy(addr);
    }

    public JavaThreadState getThreadState() {
        int val = (int) threadStateField.getValue(addr);
        if (val == UNINITIALIZED) {
            return JavaThreadState.UNINITIALIZED;
        } else if (val == NEW) {
            return JavaThreadState.NEW;
        } else if (val == NEW_TRANS) {
            return JavaThreadState.NEW_TRANS;
        } else if (val == IN_NATIVE) {
            return JavaThreadState.IN_NATIVE;
        } else if (val == IN_NATIVE_TRANS) {
            return JavaThreadState.IN_NATIVE_TRANS;
        } else if (val == IN_VM) {
            return JavaThreadState.IN_VM;
        } else if (val == IN_VM_TRANS) {
            return JavaThreadState.IN_VM_TRANS;
        } else if (val == IN_JAVA) {
            return JavaThreadState.IN_JAVA;
        } else if (val == IN_JAVA_TRANS) {
            return JavaThreadState.IN_JAVA_TRANS;
        } else if (val == BLOCKED) {
            return JavaThreadState.BLOCKED;
        } else if (val == BLOCKED_TRANS) {
            return JavaThreadState.BLOCKED_TRANS;
        } else {
            throw new RuntimeException("Illegal thread state " + val);
        }
    }

    public OSThread getOSThread() {
        return (OSThread) VMObjectFactory.newObject(OSThread.class, osThreadField.getValue(addr));
    }

    public Address getStackBase() {
        return stackBaseField.getValue(addr);
    }

    public long getStackBaseValue() {
        return VM.getVM().getAddressValue(getStackBase());
    }

    public long getStackSize() {
        return stackSizeField.getValue(addr);
    }

    public Oop getThreadObj() {
        Oop obj = null;
        try {
            obj = VM.getVM().getObjectHeap().newOop(threadObjField.getValue(addr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public String getThreadName() {
        Oop threadObj = getThreadObj();
        if (threadObj == null) {
            return "<null>";
        }
        return OopUtilities.threadOopGetName(threadObj);
    }

    public void oopsDo(AddressVisitor oopVisitor) {
        super.oopsDo(oopVisitor);
        for (StackFrameStream fst = new StackFrameStream(this); !fst.isDone(); fst.next()) {
            fst.getCurrent().oopsDo(oopVisitor, fst.getRegisterMap());
        }
    }

    public boolean isInStack(Address a) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(VM.getVM().isDebugging(), "Not yet implemented for non-debugging system");
        }
        Address sp = lastSPDbg();
        Address stackBase = getStackBase();
        if (sp == null)
            return false;
        return stackBase.greaterThanOrEqual(a) && sp.lessThanOrEqual(a);
    }

    public boolean isLockOwned(Address a) {
        Address stackBase = getStackBase();
        Address stackLimit = stackBase.addOffsetTo(-getStackSize());
        return stackBase.greaterThanOrEqual(a) && stackLimit.lessThanOrEqual(a);
    }

    public Oop getCurrentParkBlocker() {
        Oop threadObj = getThreadObj();
        if (threadObj != null) {
            return OopUtilities.threadOopGetParkBlocker(threadObj);
        }
        return null;
    }

    public void printInfoOn(PrintStream tty) {
        tty.println("State: " + getThreadState().toString());
        sun.jvm.hotspot.runtime.Frame tmpFrame = getCurrentFrameGuess();
        if (tmpFrame != null) {
            Address sp = tmpFrame.getSP();
            Address maxSP = sp;
            Address minSP = sp;
            RegisterMap tmpMap = newRegisterMap(false);
            while ((tmpFrame != null) && (!tmpFrame.isFirstFrame())) {
                tmpFrame = tmpFrame.sender(tmpMap);
                if (tmpFrame != null) {
                    sp = tmpFrame.getSP();
                    maxSP = AddressOps.max(maxSP, sp);
                    minSP = AddressOps.min(minSP, sp);
                }
            }
            tty.println("Stack in use by Java: " + minSP + " .. " + maxSP);
        } else {
            tty.println("No Java frames present");
        }
        tty.println("Base of Stack: " + getStackBase());
        tty.println("Last_Java_SP: " + getLastJavaSP());
        tty.println("Last_Java_FP: " + getLastJavaFP());
        tty.println("Last_Java_PC: " + getLastJavaPC());
        access.printInfoOn(addr, tty);
    }

    private Frame cookLastFrame(Frame fr) {
        if (fr == null) {
            return null;
        }
        Address pc = fr.getPC();
        if (Assert.ASSERTS_ENABLED) {
            if (pc == null) {
                Assert.that(VM.getVM().isDebugging(), "must have PC");
            }
        }
        return fr;
    }

    private Address lastSPDbg() {
        return access.getLastSP(addr);
    }
}
