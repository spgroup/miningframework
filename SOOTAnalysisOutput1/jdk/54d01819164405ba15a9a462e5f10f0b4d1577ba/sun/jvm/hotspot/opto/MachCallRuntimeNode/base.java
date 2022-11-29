package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.PrintStream;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.CStringUtilities;

public class MachCallRuntimeNode extends MachCallJavaNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("MachCallRuntimeNode");
        nameField = type.getAddressField("_name");
    }

    private static AddressField nameField;

    public String name() {
        return CStringUtilities.getString(nameField.getValue(getAddress()));
    }

    public MachCallRuntimeNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream out) {
        out.printf("%s ", name());
        super.dumpSpec(out);
    }
}
