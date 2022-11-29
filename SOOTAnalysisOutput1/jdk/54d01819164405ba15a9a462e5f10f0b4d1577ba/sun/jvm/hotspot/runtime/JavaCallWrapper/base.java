package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class JavaCallWrapper extends VMObject {

    protected static AddressField anchorField;

    private static AddressField lastJavaSPField;

    private static AddressField lastJavaPCField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("JavaCallWrapper");
        Type anchorType = db.lookupType("JavaFrameAnchor");
        anchorField = type.getAddressField("_anchor");
        lastJavaSPField = anchorType.getAddressField("_last_Java_sp");
        lastJavaPCField = anchorType.getAddressField("_last_Java_pc");
    }

    public JavaCallWrapper(Address addr) {
        super(addr);
    }

    public Address getLastJavaSP() {
        return lastJavaSPField.getValue(addr.addOffsetTo(anchorField.getOffset()));
    }

    public Address getLastJavaPC() {
        return lastJavaPCField.getValue(addr.addOffsetTo(anchorField.getOffset()));
    }
}
