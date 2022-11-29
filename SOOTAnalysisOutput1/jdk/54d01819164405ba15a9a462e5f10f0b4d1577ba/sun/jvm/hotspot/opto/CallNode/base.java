package sun.jvm.hotspot.opto;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class CallNode extends SafePointNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("CallNode");
        entryPointField = type.getAddressField("_entry_point");
    }

    private static AddressField entryPointField;

    public Address entryPoint() {
        return entryPointField.getValue(getAddress());
    }

    public CallNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream out) {
        out.print(" ");
        JVMState jvms = jvms();
        if (jvms != null)
            jvms.dumpSpec(out);
    }
}
