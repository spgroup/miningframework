package sun.jvm.hotspot.oops;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class BreakpointInfo extends VMObject {

    private static CIntegerField origBytecodeField;

    private static CIntegerField bciField;

    private static CIntegerField nameIndexField;

    private static CIntegerField signatureIndexField;

    private static AddressField nextField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("BreakpointInfo");
        origBytecodeField = type.getCIntegerField("_orig_bytecode");
        bciField = type.getCIntegerField("_bci");
        nameIndexField = type.getCIntegerField("_name_index");
        signatureIndexField = type.getCIntegerField("_signature_index");
        nextField = type.getAddressField("_next");
    }

    public BreakpointInfo(Address addr) {
        super(addr);
    }

    public int getOrigBytecode() {
        return (int) origBytecodeField.getValue(addr);
    }

    public int getBCI() {
        return (int) bciField.getValue(addr);
    }

    public long getNameIndex() {
        return nameIndexField.getValue(addr);
    }

    public long getSignatureIndex() {
        return signatureIndexField.getValue(addr);
    }

    public BreakpointInfo getNext() {
        return (BreakpointInfo) VMObjectFactory.newObject(BreakpointInfo.class, nextField.getValue(addr));
    }

    public boolean match(Method m, int bci) {
        return (bci == getBCI() && match(m));
    }

    public boolean match(Method m) {
        return (getNameIndex() == m.getNameIndex() && getSignatureIndex() == m.getSignatureIndex());
    }
}
