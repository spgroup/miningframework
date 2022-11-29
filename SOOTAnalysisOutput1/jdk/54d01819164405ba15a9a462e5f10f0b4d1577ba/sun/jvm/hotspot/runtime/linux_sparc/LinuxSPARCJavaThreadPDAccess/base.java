package sun.jvm.hotspot.runtime.linux_sparc;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.sparc.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.runtime.sparc.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class LinuxSPARCJavaThreadPDAccess implements JavaThreadPDAccess {

    private static AddressField baseOfStackPointerField;

    private static AddressField postJavaStateField;

    private static AddressField osThreadField;

    private static int isPC;

    private static int hasFlushed;

    private static CIntegerField osThreadThreadIDField;

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
        osThreadField = type.getAddressField("_osthread");
        hasFlushed = db.lookupIntConstant("JavaFrameAnchor::flushed").intValue();
        type = db.lookupType("OSThread");
        osThreadThreadIDField = type.getCIntegerField("_thread_id");
    }

    public Address getLastJavaFP(Address addr) {
        return null;
    }

    public Address getLastJavaPC(Address addr) {
        return null;
    }

    public Address getBaseOfStackPointer(Address addr) {
        return baseOfStackPointerField.getValue(addr);
    }

    public Frame getLastFramePD(JavaThread thread, Address addr) {
        if (thread.getLastJavaSP() == null) {
            return null;
        }
        if (thread.getLastJavaPC() != null) {
            return new SPARCFrame(SPARCFrame.biasSP(thread.getLastJavaSP()), thread.getLastJavaPC());
        } else {
            Frame top = getCurrentFrameGuess(thread, addr);
            return new SPARCFrame(SPARCFrame.biasSP(thread.getLastJavaSP()), SPARCFrame.biasSP(SPARCFrame.findYoungerSP(top.getSP(), thread.getLastJavaSP())), false);
        }
    }

    public RegisterMap newRegisterMap(JavaThread thread, boolean updateMap) {
        return new SPARCRegisterMap(thread, updateMap);
    }

    public Frame getCurrentFrameGuess(JavaThread thread, Address addr) {
        ThreadProxy t = getThreadProxy(addr);
        SPARCThreadContext context = (SPARCThreadContext) t.getContext();
        Address sp = context.getRegisterAsAddress(SPARCThreadContext.R_SP);
        Address pc = context.getRegisterAsAddress(SPARCThreadContext.R_PC);
        if ((sp == null) || (pc == null)) {
            return null;
        }
        return new SPARCFrame(sp, pc);
    }

    public void printThreadIDOn(Address addr, PrintStream tty) {
        tty.print(getThreadProxy(addr));
    }

    public Address getLastSP(Address addr) {
        ThreadProxy t = getThreadProxy(addr);
        SPARCThreadContext context = (SPARCThreadContext) t.getContext();
        return SPARCFrame.unBiasSP(context.getRegisterAsAddress(SPARCThreadContext.R_SP));
    }

    public void printInfoOn(Address threadAddr, PrintStream tty) {
    }

    public ThreadProxy getThreadProxy(Address addr) {
        Address osThreadAddr = osThreadField.getValue(addr);
        Address tidAddr = osThreadAddr.addOffsetTo(osThreadThreadIDField.getOffset());
        JVMDebugger debugger = VM.getVM().getDebugger();
        return debugger.getThreadForIdentifierAddress(tidAddr);
    }
}
