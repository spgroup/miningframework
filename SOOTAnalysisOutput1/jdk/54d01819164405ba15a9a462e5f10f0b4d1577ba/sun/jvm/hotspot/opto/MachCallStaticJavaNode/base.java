package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.PrintStream;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.CStringUtilities;

public class MachCallStaticJavaNode extends MachCallJavaNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("MachCallStaticJavaNode");
        nameField = type.getAddressField("_name");
    }

    private static AddressField nameField;

    public String name() {
        return CStringUtilities.getString(nameField.getValue(getAddress()));
    }

    public MachCallStaticJavaNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream st) {
        st.print("Static ");
        String n = name();
        if (n != null) {
            st.printf("wrapper for: %s", n);
            st.print(" ");
        }
        super.dumpSpec(st);
    }
}
