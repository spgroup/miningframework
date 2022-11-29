package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.PrintStream;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class MachCallNode extends MachSafePointNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("MachCallNode");
    }

    public MachCallNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream st) {
        st.print("# ");
        if (jvms() != null)
            jvms().dumpSpec(st);
    }
}
