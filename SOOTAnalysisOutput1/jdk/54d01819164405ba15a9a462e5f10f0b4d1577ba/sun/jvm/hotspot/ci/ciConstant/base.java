package sun.jvm.hotspot.ci;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ciConstant extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ciConstant");
        valueObjectField = type.getAddressField("_value._object");
        valueDoubleField = type.getJDoubleField("_value._double");
        valueFloatField = type.getJFloatField("_value._float");
        valueLongField = type.getJLongField("_value._long");
        valueIntField = type.getJIntField("_value._int");
        typeField = new CIntField(type.getCIntegerField("_type"), 0);
    }

    private static AddressField valueObjectField;

    private static JDoubleField valueDoubleField;

    private static JFloatField valueFloatField;

    private static JLongField valueLongField;

    private static JIntField valueIntField;

    private static CIntField typeField;

    public ciConstant(Address addr) {
        super(addr);
    }

    public void dumpReplayData(PrintStream out) {
    }
}
