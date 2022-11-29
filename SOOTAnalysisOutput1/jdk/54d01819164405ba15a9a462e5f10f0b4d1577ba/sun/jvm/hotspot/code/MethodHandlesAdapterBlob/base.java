package sun.jvm.hotspot.code;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class MethodHandlesAdapterBlob extends AdapterBlob {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("MethodHandlesAdapterBlob");
    }

    public MethodHandlesAdapterBlob(Address addr) {
        super(addr);
    }

    public boolean isMethodHandlesAdapterBlob() {
        return true;
    }

    public String getName() {
        return "MethodHandlesAdapterBlob: " + super.getName();
    }
}
