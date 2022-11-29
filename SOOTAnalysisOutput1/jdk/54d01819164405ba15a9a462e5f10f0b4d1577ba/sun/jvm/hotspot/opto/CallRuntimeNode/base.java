package sun.jvm.hotspot.opto;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.utilities.CStringUtilities;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class CallRuntimeNode extends CallNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("CallRuntimeNode");
        nameField = type.getAddressField("_name");
    }

    static private AddressField nameField;

    public String name() {
        return CStringUtilities.getString(nameField.getValue(getAddress()));
    }

    public CallRuntimeNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream out) {
        out.print(" #");
        out.print(name());
        super.dumpSpec(out);
    }
}
