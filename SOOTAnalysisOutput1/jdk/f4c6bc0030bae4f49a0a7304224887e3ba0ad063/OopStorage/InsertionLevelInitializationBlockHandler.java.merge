package sun.jvm.hotspot.gc.shared;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class OopStorage extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("OopStorage");
    }

    public OopStorage(Address addr) {
        super(addr);
    }

    public boolean findOop(Address handle) {
        return false;
    }

    public void oopsDo(AddressVisitor visitor) {
    }
}