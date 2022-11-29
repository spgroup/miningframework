package sun.jvm.hotspot.runtime.ppc64;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.ppc64.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.interpreter.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.runtime.ppc64.*;

public class PPC64CurrentFrameGuess {

    private PPC64ThreadContext context;

    private JavaThread thread;

    private Address spFound;

    private Address fpFound;

    private Address pcFound;

    private static final boolean DEBUG;

    static {
        DEBUG = System.getProperty("sun.jvm.hotspot.runtime.ppc64.PPC64Frame.DEBUG") != null;
    }

    public PPC64CurrentFrameGuess(PPC64ThreadContext context, JavaThread thread) {
        this.context = context;
        this.thread = thread;
    }

    public boolean run(long regionInBytesToSearch) {
        Address sp = context.getRegisterAsAddress(PPC64ThreadContext.SP);
        Address pc = context.getRegisterAsAddress(PPC64ThreadContext.PC);
        if (sp == null) {
            if (thread.getLastJavaSP() != null) {
                Address javaSP = thread.getLastJavaSP();
                Address javaFP = javaSP.getAddressAt(0);
                setValues(javaSP, javaFP, null);
                return true;
            }
            return false;
        }
        Address fp = sp.getAddressAt(0);
        setValues(null, null, null);
        VM vm = VM.getVM();
        if (vm.isJavaPCDbg(pc)) {
            if (vm.isClientCompiler()) {
                if (DEBUG) {
                    System.out.println("CurrentFrameGuess: choosing compiler frame: sp = " + sp + ", fp = " + fp + ", pc = " + pc);
                }
                setValues(sp, fp, pc);
                return true;
            } else {
                if (vm.getInterpreter().contains(pc)) {
                    if (DEBUG) {
                        System.out.println("CurrentFrameGuess: choosing interpreter frame: sp = " + sp + ", fp = " + fp + ", pc = " + pc);
                    }
                    setValues(sp, fp, pc);
                    return true;
                }
                for (long offset = 0; offset < regionInBytesToSearch; offset += vm.getAddressSize()) {
                    try {
                        Address curSP = sp.addOffsetTo(offset);
                        fp = curSP.getAddressAt(0);
                        Frame frame = new PPC64Frame(curSP, fp, pc);
                        RegisterMap map = thread.newRegisterMap(false);
                        while (frame != null) {
                            if (frame.isEntryFrame() && frame.entryFrameIsFirst()) {
                                if (DEBUG) {
                                    System.out.println("CurrentFrameGuess: Choosing sp = " + curSP + ", pc = " + pc);
                                }
                                setValues(curSP, fp, pc);
                                return true;
                            }
                            frame = frame.sender(map);
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            System.out.println("CurrentFrameGuess: Exception " + e + " at offset " + offset);
                        }
                    }
                }
                return false;
            }
        } else {
            if (thread.getLastJavaSP() == null) {
                if (DEBUG) {
                    System.out.println("CurrentFrameGuess: last java sp is null");
                }
                return false;
            }
            Address javaSP = thread.getLastJavaSP();
            Address javaFP = javaSP.getAddressAt(0);
            Address javaPC = thread.getLastJavaPC();
            if (DEBUG) {
                System.out.println("CurrentFrameGuess: choosing last Java frame: sp = " + javaSP + ", fp = " + javaFP + ", pc = " + javaPC);
            }
            setValues(javaSP, javaFP, javaPC);
            return true;
        }
    }

    public Address getSP() {
        return spFound;
    }

    public Address getFP() {
        return fpFound;
    }

    public Address getPC() {
        return pcFound;
    }

    private void setValues(Address sp, Address fp, Address pc) {
        spFound = sp;
        fpFound = fp;
        pcFound = pc;
    }
}
