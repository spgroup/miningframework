package sun.jvm.hotspot.gc.g1;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.runtime.VMObject;
import sun.jvm.hotspot.runtime.VMObjectFactory;
import sun.jvm.hotspot.types.AddressField;
import sun.jvm.hotspot.types.CIntegerField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.types.TypeDataBase;

public class HeapRegionSetBase extends VMObject {

    static private CIntegerField lengthField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    static private synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("HeapRegionSetBase");
        lengthField = type.getCIntegerField("_length");
    }

    public long length() {
        return lengthField.getValue(addr);
    }

    public HeapRegionSetBase(Address addr) {
        super(addr);
    }
}
