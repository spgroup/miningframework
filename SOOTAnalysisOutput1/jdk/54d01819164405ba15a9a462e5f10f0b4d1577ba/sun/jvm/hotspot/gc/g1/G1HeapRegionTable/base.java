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

public class G1HeapRegionTable extends VMObject {

    static private AddressField baseField;

    static private CIntegerField lengthField;

    static private AddressField biasedBaseField;

    static private CIntegerField biasField;

    static private CIntegerField shiftByField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    static private synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("G1HeapRegionTable");
        baseField = type.getAddressField("_base");
        lengthField = type.getCIntegerField("_length");
        biasedBaseField = type.getAddressField("_biased_base");
        biasField = type.getCIntegerField("_bias");
        shiftByField = type.getCIntegerField("_shift_by");
    }

    private HeapRegion at(long index) {
        Address arrayAddr = baseField.getValue(addr);
        long offset = index * VM.getVM().getAddressSize();
        Address regionAddr = arrayAddr.getAddressAt(offset);
        return (HeapRegion) VMObjectFactory.newObject(HeapRegion.class, regionAddr);
    }

    public long length() {
        return lengthField.getValue(addr);
    }

    public long bias() {
        return biasField.getValue(addr);
    }

    public long shiftBy() {
        return shiftByField.getValue(addr);
    }

    private class HeapRegionIterator implements Iterator<HeapRegion> {

        private long index;

        private long length;

        private HeapRegion next;

        public HeapRegion positionToNext() {
            HeapRegion result = next;
            while (index < length && at(index) == null) {
                index++;
            }
            if (index < length) {
                next = at(index);
                index++;
            } else {
                next = null;
            }
            return result;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public HeapRegion next() {
            return positionToNext();
        }

        @Override
        public void remove() {
        }

        HeapRegionIterator(long totalLength) {
            index = 0;
            length = totalLength;
            positionToNext();
        }
    }

    public Iterator<HeapRegion> heapRegionIterator(long committedLength) {
        return new HeapRegionIterator(committedLength);
    }

    public G1HeapRegionTable(Address addr) {
        super(addr);
    }
}
