package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class ObjectSynchronizer {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type;
        try {
            type = db.lookupType("ObjectSynchronizer");
            AddressField blockListField;
            blockListField = type.getAddressField("gBlockList");
            gBlockListAddr = blockListField.getValue();
            blockSize = db.lookupIntConstant("ObjectSynchronizer::_BLOCKSIZE").intValue();
            defaultCacheLineSize = db.lookupIntConstant("DEFAULT_CACHE_LINE_SIZE").intValue();
        } catch (RuntimeException e) {
        }
        type = db.lookupType("ObjectMonitor");
        objectMonitorTypeSize = type.getSize();
        if ((objectMonitorTypeSize % defaultCacheLineSize) != 0) {
            int needLines = ((int) objectMonitorTypeSize / defaultCacheLineSize) + 1;
            objectMonitorTypeSize = needLines * defaultCacheLineSize;
        }
    }

    public long identityHashValueFor(Oop obj) {
        Mark mark = obj.getMark();
        if (mark.isUnlocked()) {
            return mark.hash();
        } else if (mark.hasMonitor()) {
            ObjectMonitor monitor = mark.monitor();
            Mark temp = monitor.header();
            return temp.hash();
        } else {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(VM.getVM().isDebugging(), "Can not access displaced header otherwise");
            }
            if (mark.hasDisplacedMarkHelper()) {
                Mark temp = mark.displacedMarkHelper();
                return temp.hash();
            }
            return 0;
        }
    }

    public static Iterator objectMonitorIterator() {
        if (gBlockListAddr != null) {
            return new ObjectMonitorIterator();
        } else {
            return null;
        }
    }

    private static class ObjectMonitorIterator implements Iterator {

        ObjectMonitorIterator() {
            blockAddr = gBlockListAddr;
            index = blockSize - 1;
            block = new ObjectMonitor(blockAddr);
        }

        public boolean hasNext() {
            return (index > 0 || block.freeNext() != null);
        }

        public Object next() {
            Address addr;
            if (index > 0) {
                addr = blockAddr.addOffsetTo(index * objectMonitorTypeSize);
            } else {
                blockAddr = block.freeNext();
                index = blockSize - 1;
                addr = blockAddr.addOffsetTo(index * objectMonitorTypeSize);
            }
            index--;
            return new ObjectMonitor(addr);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private ObjectMonitor block;

        private int index;

        private Address blockAddr;
    }

    private static Address gBlockListAddr;

    private static int blockSize;

    private static int defaultCacheLineSize;

    private static long objectMonitorTypeSize;
}
