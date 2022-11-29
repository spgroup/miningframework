package sun.jvm.hotspot.oops;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.gc.cms.*;
import sun.jvm.hotspot.gc.shared.*;
import sun.jvm.hotspot.gc.g1.*;
import sun.jvm.hotspot.gc.parallel.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class ObjectHeap {

    private static final boolean DEBUG;

    static {
        DEBUG = System.getProperty("sun.jvm.hotspot.oops.ObjectHeap.DEBUG") != null;
    }

    private Address boolArrayKlassHandle;

    private Address byteArrayKlassHandle;

    private Address charArrayKlassHandle;

    private Address intArrayKlassHandle;

    private Address shortArrayKlassHandle;

    private Address longArrayKlassHandle;

    private Address singleArrayKlassHandle;

    private Address doubleArrayKlassHandle;

    private TypeArrayKlass boolArrayKlassObj;

    private TypeArrayKlass byteArrayKlassObj;

    private TypeArrayKlass charArrayKlassObj;

    private TypeArrayKlass intArrayKlassObj;

    private TypeArrayKlass shortArrayKlassObj;

    private TypeArrayKlass longArrayKlassObj;

    private TypeArrayKlass singleArrayKlassObj;

    private TypeArrayKlass doubleArrayKlassObj;

    public void initialize(TypeDataBase db) throws WrongTypeException {
        Type universeType = db.lookupType("Universe");
        boolArrayKlassHandle = universeType.getAddressField("_boolArrayKlassObj").getValue();
        boolArrayKlassObj = new TypeArrayKlass(boolArrayKlassHandle);
        byteArrayKlassHandle = universeType.getAddressField("_byteArrayKlassObj").getValue();
        byteArrayKlassObj = new TypeArrayKlass(byteArrayKlassHandle);
        charArrayKlassHandle = universeType.getAddressField("_charArrayKlassObj").getValue();
        charArrayKlassObj = new TypeArrayKlass(charArrayKlassHandle);
        intArrayKlassHandle = universeType.getAddressField("_intArrayKlassObj").getValue();
        intArrayKlassObj = new TypeArrayKlass(intArrayKlassHandle);
        shortArrayKlassHandle = universeType.getAddressField("_shortArrayKlassObj").getValue();
        shortArrayKlassObj = new TypeArrayKlass(shortArrayKlassHandle);
        longArrayKlassHandle = universeType.getAddressField("_longArrayKlassObj").getValue();
        longArrayKlassObj = new TypeArrayKlass(longArrayKlassHandle);
        singleArrayKlassHandle = universeType.getAddressField("_singleArrayKlassObj").getValue();
        singleArrayKlassObj = new TypeArrayKlass(singleArrayKlassHandle);
        doubleArrayKlassHandle = universeType.getAddressField("_doubleArrayKlassObj").getValue();
        doubleArrayKlassObj = new TypeArrayKlass(doubleArrayKlassHandle);
    }

    public ObjectHeap(TypeDataBase db) throws WrongTypeException {
        oopSize = VM.getVM().getOopSize();
        byteSize = db.getJByteType().getSize();
        charSize = db.getJCharType().getSize();
        booleanSize = db.getJBooleanType().getSize();
        intSize = db.getJIntType().getSize();
        shortSize = db.getJShortType().getSize();
        longSize = db.getJLongType().getSize();
        floatSize = db.getJFloatType().getSize();
        doubleSize = db.getJDoubleType().getSize();
        initialize(db);
    }

    public boolean equal(Oop o1, Oop o2) {
        if (o1 != null)
            return o1.equals(o2);
        return (o2 == null);
    }

    private long oopSize;

    private long byteSize;

    private long charSize;

    private long booleanSize;

    private long intSize;

    private long shortSize;

    private long longSize;

    private long floatSize;

    private long doubleSize;

    public long getOopSize() {
        return oopSize;
    }

    public long getByteSize() {
        return byteSize;
    }

    public long getCharSize() {
        return charSize;
    }

    public long getBooleanSize() {
        return booleanSize;
    }

    public long getIntSize() {
        return intSize;
    }

    public long getShortSize() {
        return shortSize;
    }

    public long getLongSize() {
        return longSize;
    }

    public long getFloatSize() {
        return floatSize;
    }

    public long getDoubleSize() {
        return doubleSize;
    }

    public TypeArrayKlass getBoolArrayKlassObj() {
        return boolArrayKlassObj;
    }

    public TypeArrayKlass getByteArrayKlassObj() {
        return byteArrayKlassObj;
    }

    public TypeArrayKlass getCharArrayKlassObj() {
        return charArrayKlassObj;
    }

    public TypeArrayKlass getIntArrayKlassObj() {
        return intArrayKlassObj;
    }

    public TypeArrayKlass getShortArrayKlassObj() {
        return shortArrayKlassObj;
    }

    public TypeArrayKlass getLongArrayKlassObj() {
        return longArrayKlassObj;
    }

    public TypeArrayKlass getSingleArrayKlassObj() {
        return singleArrayKlassObj;
    }

    public TypeArrayKlass getDoubleArrayKlassObj() {
        return doubleArrayKlassObj;
    }

    public Klass typeArrayKlassObj(int t) {
        if (t == BasicType.getTBoolean())
            return getBoolArrayKlassObj();
        if (t == BasicType.getTChar())
            return getCharArrayKlassObj();
        if (t == BasicType.getTFloat())
            return getSingleArrayKlassObj();
        if (t == BasicType.getTDouble())
            return getDoubleArrayKlassObj();
        if (t == BasicType.getTByte())
            return getByteArrayKlassObj();
        if (t == BasicType.getTShort())
            return getShortArrayKlassObj();
        if (t == BasicType.getTInt())
            return getIntArrayKlassObj();
        if (t == BasicType.getTLong())
            return getLongArrayKlassObj();
        throw new RuntimeException("Illegal basic type " + t);
    }

    public static interface ObjectFilter {

        public boolean canInclude(Oop obj);
    }

    public void iterate(HeapVisitor visitor) {
        iterateLiveRegions(collectLiveRegions(), visitor, null);
    }

    public void iterate(HeapVisitor visitor, ObjectFilter of) {
        iterateLiveRegions(collectLiveRegions(), visitor, of);
    }

    public void iterateObjectsOfKlass(HeapVisitor visitor, final Klass k, boolean includeSubtypes) {
        if (includeSubtypes) {
            if (k.isFinal()) {
                iterateExact(visitor, k);
            } else {
                iterateSubtypes(visitor, k);
            }
        } else {
            if (!k.isAbstract() && !k.isInterface()) {
                iterateExact(visitor, k);
            }
        }
    }

    public void iterateObjectsOfKlass(HeapVisitor visitor, final Klass k) {
        iterateObjectsOfKlass(visitor, k, true);
    }

    public void iterateRaw(RawHeapVisitor visitor) {
        List liveRegions = collectLiveRegions();
        long totalSize = 0;
        for (int i = 0; i < liveRegions.size(); i += 2) {
            Address bottom = (Address) liveRegions.get(i);
            Address top = (Address) liveRegions.get(i + 1);
            totalSize += top.minus(bottom);
        }
        visitor.prologue(totalSize);
        for (int i = 0; i < liveRegions.size(); i += 2) {
            Address bottom = (Address) liveRegions.get(i);
            Address top = (Address) liveRegions.get(i + 1);
            while (bottom.lessThan(top)) {
                visitor.visitAddress(bottom);
                bottom = bottom.addOffsetTo(VM.getVM().getAddressSize());
            }
        }
        visitor.epilogue();
    }

    public boolean isValidMethod(Address handle) {
        try {
            Method m = (Method) Metadata.instantiateWrapperFor(handle);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Oop newOop(OopHandle handle) {
        if (handle == null)
            return null;
        Klass klass = Oop.getKlassForOopHandle(handle);
        if (klass != null) {
            if (klass instanceof TypeArrayKlass)
                return new TypeArray(handle, this);
            if (klass instanceof ObjArrayKlass)
                return new ObjArray(handle, this);
            if (klass instanceof InstanceKlass)
                return new Instance(handle, this);
        }
        if (DEBUG) {
            System.err.println("Unknown oop at " + handle);
            System.err.println("Oop's klass is " + klass);
        }
        throw new UnknownOopException();
    }

    public void print() {
        HeapPrinter printer = new HeapPrinter(System.out);
        iterate(printer);
    }

    private void iterateExact(HeapVisitor visitor, final Klass k) {
        iterateLiveRegions(collectLiveRegions(), visitor, new ObjectFilter() {

            public boolean canInclude(Oop obj) {
                Klass tk = obj.getKlass();
                return (tk != null && tk.equals(k));
            }
        });
    }

    private void iterateSubtypes(HeapVisitor visitor, final Klass k) {
        iterateLiveRegions(collectLiveRegions(), visitor, new ObjectFilter() {

            public boolean canInclude(Oop obj) {
                Klass tk = obj.getKlass();
                return (tk != null && tk.isSubtypeOf(k));
            }
        });
    }

    private void iterateLiveRegions(List liveRegions, HeapVisitor visitor, ObjectFilter of) {
        long totalSize = 0;
        for (int i = 0; i < liveRegions.size(); i += 2) {
            Address bottom = (Address) liveRegions.get(i);
            Address top = (Address) liveRegions.get(i + 1);
            totalSize += top.minus(bottom);
        }
        visitor.prologue(totalSize);
        CompactibleFreeListSpace cmsSpaceOld = null;
        CollectedHeap heap = VM.getVM().getUniverse().heap();
        if (heap instanceof GenCollectedHeap) {
            GenCollectedHeap genHeap = (GenCollectedHeap) heap;
            Generation genOld = genHeap.getGen(1);
            if (genOld instanceof ConcurrentMarkSweepGeneration) {
                ConcurrentMarkSweepGeneration concGen = (ConcurrentMarkSweepGeneration) genOld;
                cmsSpaceOld = concGen.cmsSpace();
            }
        }
        for (int i = 0; i < liveRegions.size(); i += 2) {
            Address bottom = (Address) liveRegions.get(i);
            Address top = (Address) liveRegions.get(i + 1);
            try {
                OopHandle handle = bottom.addOffsetToAsOopHandle(0);
                while (handle.lessThan(top)) {
                    Oop obj = null;
                    try {
                        obj = newOop(handle);
                    } catch (UnknownOopException exp) {
                        if (DEBUG) {
                            throw new RuntimeException(" UnknownOopException  " + exp);
                        }
                    }
                    if (obj == null) {
                        long size = 0;
                        if ((cmsSpaceOld != null) && cmsSpaceOld.contains(handle)) {
                            size = cmsSpaceOld.collector().blockSizeUsingPrintezisBits(handle);
                        }
                        if (size <= 0) {
                            throw new UnknownOopException();
                        }
                        handle = handle.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(size));
                        continue;
                    }
                    if (of == null || of.canInclude(obj)) {
                        if (visitor.doObj(obj)) {
                            break;
                        }
                    }
                    if ((cmsSpaceOld != null) && cmsSpaceOld.contains(handle)) {
                        handle = handle.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(obj.getObjectSize()));
                    } else {
                        handle = handle.addOffsetToAsOopHandle(obj.getObjectSize());
                    }
                }
            } catch (AddressException e) {
            } catch (UnknownOopException e) {
            }
        }
        visitor.epilogue();
    }

    private void addLiveRegions(String name, List input, List output) {
        for (Iterator itr = input.iterator(); itr.hasNext(); ) {
            MemRegion reg = (MemRegion) itr.next();
            Address top = reg.end();
            Address bottom = reg.start();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(top != null, "top address in a live region should not be null");
            }
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(bottom != null, "bottom address in a live region should not be null");
            }
            output.add(top);
            output.add(bottom);
            if (DEBUG) {
                System.err.println("Live region: " + name + ": " + bottom + ", " + top);
            }
        }
    }

    private class LiveRegionsCollector implements SpaceClosure {

        LiveRegionsCollector(List l) {
            liveRegions = l;
        }

        public void doSpace(Space s) {
            addLiveRegions(s.toString(), s.getLiveRegions(), liveRegions);
        }

        private List liveRegions;
    }

    private List collectLiveRegions() {
        List liveRegions = new ArrayList();
        LiveRegionsCollector lrc = new LiveRegionsCollector(liveRegions);
        CollectedHeap heap = VM.getVM().getUniverse().heap();
        if (heap instanceof GenCollectedHeap) {
            GenCollectedHeap genHeap = (GenCollectedHeap) heap;
            for (int i = 0; i < genHeap.nGens(); i++) {
                Generation gen = genHeap.getGen(i);
                gen.spaceIterate(lrc, true);
            }
        } else if (heap instanceof ParallelScavengeHeap) {
            ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
            PSYoungGen youngGen = psh.youngGen();
            addLiveRegions("eden", youngGen.edenSpace().getLiveRegions(), liveRegions);
            addLiveRegions("from", youngGen.fromSpace().getLiveRegions(), liveRegions);
            PSOldGen oldGen = psh.oldGen();
            addLiveRegions("old ", oldGen.objectSpace().getLiveRegions(), liveRegions);
        } else if (heap instanceof G1CollectedHeap) {
            G1CollectedHeap g1h = (G1CollectedHeap) heap;
            g1h.heapRegionIterate(lrc);
        } else {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(false, "Expecting GenCollectedHeap, G1CollectedHeap, " + "or ParallelScavengeHeap, but got " + heap.getClass().getName());
            }
        }
        if (VM.getVM().getUseTLAB()) {
            for (JavaThread thread = VM.getVM().getThreads().first(); thread != null; thread = thread.next()) {
                ThreadLocalAllocBuffer tlab = thread.tlab();
                if (tlab.start() != null) {
                    if ((tlab.top() == null) || (tlab.end() == null)) {
                        System.err.print("Warning: skipping invalid TLAB for thread ");
                        thread.printThreadIDOn(System.err);
                        System.err.println();
                    } else {
                        if (DEBUG) {
                            System.err.print("TLAB for " + thread.getThreadName() + ", #");
                            thread.printThreadIDOn(System.err);
                            System.err.print(": ");
                            tlab.printOn(System.err);
                        }
                        liveRegions.add(tlab.start());
                        liveRegions.add(tlab.start());
                        liveRegions.add(tlab.top());
                        liveRegions.add(tlab.hardEnd());
                    }
                }
            }
        }
        sortLiveRegions(liveRegions);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(liveRegions.size() % 2 == 0, "Must have even number of region boundaries");
        }
        if (DEBUG) {
            System.err.println("liveRegions:");
            for (int i = 0; i < liveRegions.size(); i += 2) {
                Address bottom = (Address) liveRegions.get(i);
                Address top = (Address) liveRegions.get(i + 1);
                System.err.println(" " + bottom + " - " + top);
            }
        }
        return liveRegions;
    }

    private void sortLiveRegions(List liveRegions) {
        Collections.sort(liveRegions, new Comparator() {

            public int compare(Object o1, Object o2) {
                Address a1 = (Address) o1;
                Address a2 = (Address) o2;
                if (AddressOps.lt(a1, a2)) {
                    return -1;
                } else if (AddressOps.gt(a1, a2)) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
