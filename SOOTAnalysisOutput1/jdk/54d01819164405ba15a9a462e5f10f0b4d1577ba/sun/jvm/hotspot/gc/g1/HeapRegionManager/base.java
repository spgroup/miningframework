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

public class HeapRegionManager extends VMObject {

    static private long regionsFieldOffset;

    static private CIntegerField numCommittedField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    static private synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("HeapRegionManager");
        regionsFieldOffset = type.getField("_regions").getOffset();
        numCommittedField = type.getCIntegerField("_num_committed");
    }

    private G1HeapRegionTable regions() {
        Address regionsAddr = addr.addOffsetTo(regionsFieldOffset);
        return (G1HeapRegionTable) VMObjectFactory.newObject(G1HeapRegionTable.class, regionsAddr);
    }

    public long capacity() {
        return length() * HeapRegion.grainBytes();
    }

    public long length() {
        return regions().length();
    }

    public long committedLength() {
        return numCommittedField.getValue(addr);
    }

    public Iterator<HeapRegion> heapRegionIterator() {
        return regions().heapRegionIterator(length());
    }

    public HeapRegionManager(Address addr) {
        super(addr);
    }
}
