package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class CodeHeap extends VMObject {

    private static Field memoryField;

    private static Field segmapField;

    private static CIntegerField log2SegmentSizeField;

    private VirtualSpace memory;

    private VirtualSpace segmentMap;

    private int log2SegmentSize;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("CodeHeap");
        memoryField = type.getField("_memory");
        segmapField = type.getField("_segmap");
        log2SegmentSizeField = type.getCIntegerField("_log2_segment_size");
    }

    public CodeHeap(Address addr) {
        super(addr);
        log2SegmentSize = (int) log2SegmentSizeField.getValue(addr);
        segmentMap = new VirtualSpace(addr.addOffsetTo(segmapField.getOffset()));
        memory = new VirtualSpace(addr.addOffsetTo(memoryField.getOffset()));
    }

    public Address begin() {
        return getMemory().low();
    }

    public Address end() {
        return getMemory().high();
    }

    public boolean contains(Address p) {
        return (begin().lessThanOrEqual(p) && end().greaterThan(p));
    }

    public Address findStart(Address p) {
        if (!contains(p))
            return null;
        HeapBlock h = blockStart(p);
        if (h == null || h.isFree()) {
            return null;
        }
        return h.getAllocatedSpace();
    }

    private Address nextBlock(Address ptr) {
        Address base = blockBase(ptr);
        if (base == null) {
            return null;
        }
        HeapBlock block = getBlockAt(base);
        return base.addOffsetTo(block.getLength() * (1 << getLog2SegmentSize()));
    }

    public void iterate(CodeCacheVisitor visitor, CodeCache cache) {
        CodeBlob lastBlob = null;
        Address ptr = begin();
        while (ptr != null && ptr.lessThan(end())) {
            try {
                CodeBlob blob = cache.createCodeBlobWrapper(findStart(ptr));
                if (blob != null) {
                    visitor.visit(blob);
                    if (blob == lastBlob) {
                        throw new InternalError("saw same blob twice");
                    }
                    lastBlob = blob;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            Address next = nextBlock(ptr);
            if (next != null && next.lessThan(ptr)) {
                throw new InternalError("pointer moved backwards");
            }
            ptr = next;
        }
    }

    private VirtualSpace getMemory() {
        return memory;
    }

    private VirtualSpace getSegmentMap() {
        return segmentMap;
    }

    private long segmentFor(Address p) {
        return p.minus(getMemory().low()) >> getLog2SegmentSize();
    }

    private int getLog2SegmentSize() {
        return log2SegmentSize;
    }

    private HeapBlock getBlockAt(Address addr) {
        return (HeapBlock) VMObjectFactory.newObject(HeapBlock.class, addr);
    }

    private HeapBlock blockStart(Address p) {
        Address base = blockBase(p);
        if (base == null)
            return null;
        return getBlockAt(base);
    }

    private Address blockBase(Address p) {
        long i = segmentFor(p);
        Address b = getSegmentMap().low();
        if (b.getCIntegerAt(i, 1, true) == 0xFF) {
            return null;
        }
        while (b.getCIntegerAt(i, 1, true) > 0) {
            i -= b.getCIntegerAt(i, 1, true);
        }
        return getMemory().low().addOffsetTo(i << getLog2SegmentSize());
    }
}
