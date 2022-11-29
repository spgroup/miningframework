package sun.jvm.hotspot.runtime.win32_amd64;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.amd64.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.runtime.amd64.*;
import sun.jvm.hotspot.runtime.x86.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class Win32AMD64JavaThreadPDAccess implements JavaThreadPDAccess {

    private static AddressField lastJavaFPField;

    private static AddressField osThreadField;

    private static Field osThreadThreadIdField;

    private static final long GUESS_SCAN_RANGE = 128 * 1024;

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
        lastJavaFPField = anchorType.getAddressField("_last_Java_fp");
        osThreadField = type.getAddressField("_osthread");
        type = db.lookupType("OSThread");
        osThreadThreadIdField = type.getField("_thread_id");
    }

    public Address getLastJavaFP(Address addr) {
        return lastJavaFPField.getValue(addr.addOffsetTo(sun.jvm.hotspot.runtime.JavaThread.getAnchorField().getOffset()));
    }

    public Address getLastJavaPC(Address addr) {
        return null;
    }

    public Address getBaseOfStackPointer(Address addr) {
        return null;
    }

    public Frame getLastFramePD(JavaThread thread, Address addr) {
        Address fp = thread.getLastJavaFP();
        if (fp == null) {
            return null;
        }
        Address pc = thread.getLastJavaPC();
        if (pc != null) {
            return new X86Frame(thread.getLastJavaSP(), fp, pc);
        } else {
            return new X86Frame(thread.getLastJavaSP(), fp);
        }
    }

    public RegisterMap newRegisterMap(JavaThread thread, boolean updateMap) {
        return new X86RegisterMap(thread, updateMap);
    }

    public Frame getCurrentFrameGuess(JavaThread thread, Address addr) {
        ThreadProxy t = getThreadProxy(addr);
        AMD64ThreadContext context = (AMD64ThreadContext) t.getContext();
        AMD64CurrentFrameGuess guesser = new AMD64CurrentFrameGuess(context, thread);
        if (!guesser.run(GUESS_SCAN_RANGE)) {
            return null;
        }
        if (guesser.getPC() == null) {
            return new X86Frame(guesser.getSP(), guesser.getFP());
        } else {
            return new X86Frame(guesser.getSP(), guesser.getFP(), guesser.getPC());
        }
    }

    public void printThreadIDOn(Address addr, PrintStream tty) {
        tty.print(getThreadProxy(addr));
    }

    public void printInfoOn(Address threadAddr, PrintStream tty) {
    }

    public Address getLastSP(Address addr) {
        ThreadProxy t = getThreadProxy(addr);
        AMD64ThreadContext context = (AMD64ThreadContext) t.getContext();
        return context.getRegisterAsAddress(AMD64ThreadContext.RSP);
    }

    public ThreadProxy getThreadProxy(Address addr) {
        Address osThreadAddr = osThreadField.getValue(addr);
        Address threadIdAddr = osThreadAddr.addOffsetTo(osThreadThreadIdField.getOffset());
        JVMDebugger debugger = VM.getVM().getDebugger();
        return debugger.getThreadForIdentifierAddress(threadIdAddr);
    }
}
