package sun.jvm.hotspot.compiler;

import java.util.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class ImmutableOopMap extends VMObject {

    private static CIntegerField countField;

    private static long classSize;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("ImmutableOopMap");
        countField = type.getCIntegerField("_count");
        classSize = type.getSize();
    }

    public ImmutableOopMap(Address addr) {
        super(addr);
    }

    long getCount() {
        return countField.getValue(addr);
    }

    public Address getData() {
        return addr.addOffsetTo(classSize);
    }
}
