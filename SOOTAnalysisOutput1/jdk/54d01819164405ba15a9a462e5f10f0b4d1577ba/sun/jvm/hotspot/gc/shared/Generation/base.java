package sun.jvm.hotspot.gc.shared;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public abstract class Generation extends VMObject {

    private static long reservedFieldOffset;

    private static long virtualSpaceFieldOffset;

    protected static final int K = 1024;

    private static Field statRecordField;

    private static CIntegerField invocationField;

    private static int NAME_DEF_NEW;

    private static int NAME_PAR_NEW;

    private static int NAME_MARK_SWEEP_COMPACT;

    private static int NAME_CONCURRENT_MARK_SWEEP;

    private static int NAME_OTHER;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("Generation");
        reservedFieldOffset = type.getField("_reserved").getOffset();
        virtualSpaceFieldOffset = type.getField("_virtual_space").getOffset();
        statRecordField = type.getField("_stat_record");
        type = db.lookupType("Generation::StatRecord");
        invocationField = type.getCIntegerField("invocations");
        NAME_DEF_NEW = db.lookupIntConstant("Generation::DefNew").intValue();
        NAME_PAR_NEW = db.lookupIntConstant("Generation::ParNew").intValue();
        NAME_MARK_SWEEP_COMPACT = db.lookupIntConstant("Generation::MarkSweepCompact").intValue();
        NAME_CONCURRENT_MARK_SWEEP = db.lookupIntConstant("Generation::ConcurrentMarkSweep").intValue();
        NAME_OTHER = db.lookupIntConstant("Generation::Other").intValue();
    }

    public Generation(Address addr) {
        super(addr);
    }

    public static class Name {

        public static final Name DEF_NEW = new Name("DefNew");

        public static final Name PAR_NEW = new Name("ParNew");

        public static final Name MARK_SWEEP_COMPACT = new Name("MarkSweepCompact");

        public static final Name CONCURRENT_MARK_SWEEP = new Name("ConcurrentMarkSweep");

        public static final Name OTHER = new Name("Other");

        private Name(String value) {
            this.value = value;
        }

        private String value;

        public String toString() {
            return value;
        }
    }

    public Generation.Name kind() {
        return Generation.Name.OTHER;
    }

    static Generation.Name nameForEnum(int value) {
        if (value == NAME_DEF_NEW) {
            return Name.DEF_NEW;
        } else if (value == NAME_PAR_NEW) {
            return Name.PAR_NEW;
        } else if (value == NAME_MARK_SWEEP_COMPACT) {
            return Name.MARK_SWEEP_COMPACT;
        } else if (value == NAME_CONCURRENT_MARK_SWEEP) {
            return Name.CONCURRENT_MARK_SWEEP;
        } else if (value == NAME_OTHER) {
            return Name.OTHER;
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public int invocations() {
        return getStatRecord().getInvocations();
    }

    public abstract long capacity();

    public abstract long used();

    public abstract long free();

    public abstract long contiguousAvailable();

    public MemRegion reserved() {
        return new MemRegion(addr.addOffsetTo(reservedFieldOffset));
    }

    public MemRegion usedRegion() {
        return reserved();
    }

    public boolean isIn(Address p) {
        GenerationIsInClosure blk = new GenerationIsInClosure(p);
        spaceIterate(blk);
        return (blk.space() != null);
    }

    public boolean isInReserved(Address p) {
        return reserved().contains(p);
    }

    protected VirtualSpace virtualSpace() {
        return (VirtualSpace) VMObjectFactory.newObject(VirtualSpace.class, addr.addOffsetTo(virtualSpaceFieldOffset));
    }

    public abstract String name();

    public void spaceIterate(SpaceClosure blk) {
        spaceIterate(blk, false);
    }

    public abstract void spaceIterate(SpaceClosure blk, boolean usedOnly);

    public void print() {
        printOn(System.out);
    }

    public abstract void printOn(PrintStream tty);

    public static class StatRecord extends VMObject {

        public StatRecord(Address addr) {
            super(addr);
        }

        public int getInvocations() {
            return (int) invocationField.getValue(addr);
        }
    }

    private StatRecord getStatRecord() {
        return (StatRecord) VMObjectFactory.newObject(Generation.StatRecord.class, addr.addOffsetTo(statRecordField.getOffset()));
    }
}
