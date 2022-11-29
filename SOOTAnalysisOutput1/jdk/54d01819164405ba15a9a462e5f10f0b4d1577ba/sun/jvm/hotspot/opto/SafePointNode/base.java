package sun.jvm.hotspot.opto;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class SafePointNode extends MultiNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("SafePointNode");
        jvmsField = type.getAddressField("_jvms");
    }

    private static AddressField jvmsField;

    public SafePointNode(Address addr) {
        super(addr);
    }

    public JVMState jvms() {
        return JVMState.create(jvmsField.getValue(getAddress()));
    }

    public void dumpSpec(PrintStream out) {
        JVMState jvms = jvms();
        if (jvms != null)
            out.print(" !");
        while (jvms != null) {
            Method m = jvms.method().method();
            int bci = jvms.bci();
            out.print(" " + m.getMethodHolder().getName().asString().replace('/', '.') + "::" + m.getName().asString() + " @ bci:" + bci);
            jvms = jvms.caller();
        }
    }
}
