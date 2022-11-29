package sun.jvm.hotspot.ci;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ciBaseObject extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ciBaseObject");
        identField = new CIntField(type.getCIntegerField("_ident"), 0);
    }

    private static CIntField identField;

    public ciBaseObject(Address addr) {
        super(addr);
    }

    public void dumpReplayData(PrintStream out) {
        out.println("# Unknown ci type " + getAddress().getAddressAt(0));
    }
}
