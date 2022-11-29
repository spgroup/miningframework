package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class PerfMemory {

    private static AddressField startField;

    private static AddressField endField;

    private static AddressField topField;

    private static CIntegerField capacityField;

    private static AddressField prologueField;

    private static JIntField initializedField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("PerfMemory");
        startField = type.getAddressField("_start");
        endField = type.getAddressField("_end");
        topField = type.getAddressField("_top");
        capacityField = type.getCIntegerField("_capacity");
        prologueField = type.getAddressField("_prologue");
        initializedField = type.getJIntField("_initialized");
    }

    public static Address start() {
        return startField.getValue();
    }

    public static Address end() {
        return endField.getValue();
    }

    public static Address top() {
        return topField.getValue();
    }

    public static long capacity() {
        return capacityField.getValue();
    }

    public static boolean initialized() {
        return ((int) initializedField.getValue()) != 0;
    }

    public static PerfDataPrologue prologue() {
        return (PerfDataPrologue) VMObjectFactory.newObject(PerfDataPrologue.class, prologueField.getValue());
    }

    public static boolean contains(Address addr) {
        return start() != null && addr.minus(start()) >= 0 && end().minus(addr) > 0;
    }

    public static interface PerfDataEntryVisitor {

        public boolean visit(PerfDataEntry pde);
    }

    public static void iterate(PerfDataEntryVisitor visitor) {
        PerfDataPrologue header = prologue();
        int off = header.entryOffset();
        int num = header.numEntries();
        Address addr = header.getAddress();
        for (int i = 0; i < num; i++) {
            PerfDataEntry pde = (PerfDataEntry) VMObjectFactory.newObject(PerfDataEntry.class, addr.addOffsetTo(off));
            off += pde.entryLength();
            if (visitor.visit(pde) == false)
                return;
        }
    }
}
