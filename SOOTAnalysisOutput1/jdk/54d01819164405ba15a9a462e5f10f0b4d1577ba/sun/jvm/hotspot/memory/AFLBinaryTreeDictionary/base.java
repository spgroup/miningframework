package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;

public class AFLBinaryTreeDictionary extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("AFLBinaryTreeDictionary");
        totalSizeField = type.getCIntegerField("_total_size");
    }

    private static CIntegerField totalSizeField;

    public long size() {
        return totalSizeField.getValue(addr);
    }

    public AFLBinaryTreeDictionary(Address addr) {
        super(addr);
    }
}
