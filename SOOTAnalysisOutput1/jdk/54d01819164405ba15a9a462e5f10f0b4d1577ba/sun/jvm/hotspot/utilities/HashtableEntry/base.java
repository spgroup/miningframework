package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;

public class HashtableEntry extends BasicHashtableEntry {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("IntptrHashtableEntry");
        literalField = type.getAddressField("_literal");
    }

    private static AddressField literalField;

    public Address literalValue() {
        return literalField.getValue(addr);
    }

    public HashtableEntry(Address addr) {
        super(addr);
    }
}
