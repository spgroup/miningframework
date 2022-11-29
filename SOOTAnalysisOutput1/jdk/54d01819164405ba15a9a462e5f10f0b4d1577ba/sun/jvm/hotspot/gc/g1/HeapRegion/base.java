package sun.jvm.hotspot.gc.g1;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.gc.shared.CompactibleSpace;
import sun.jvm.hotspot.memory.MemRegion;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.AddressField;
import sun.jvm.hotspot.types.CIntegerField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.types.TypeDataBase;

public class HeapRegion extends CompactibleSpace {

    static private CIntegerField grainBytesField;

    static private AddressField topField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    static private synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("HeapRegion");
        grainBytesField = type.getCIntegerField("GrainBytes");
        topField = type.getAddressField("_top");
    }

    static public long grainBytes() {
        return grainBytesField.getValue();
    }

    public HeapRegion(Address addr) {
        super(addr);
    }

    public Address top() {
        return topField.getValue(addr);
    }

    @Override
    public List getLiveRegions() {
        List res = new ArrayList();
        res.add(new MemRegion(bottom(), top()));
        return res;
    }

    @Override
    public long used() {
        return top().minus(bottom());
    }

    @Override
    public long free() {
        return end().minus(top());
    }
}
