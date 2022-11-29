package sun.jvm.hotspot.gc.serial;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.gc.shared.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class TenuredGeneration extends CardGeneration {

    private static AddressField theSpaceField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("TenuredGeneration");
        theSpaceField = type.getAddressField("_the_space");
    }

    public TenuredGeneration(Address addr) {
        super(addr);
    }

    public ContiguousSpace theSpace() {
        return (ContiguousSpace) VMObjectFactory.newObject(ContiguousSpace.class, theSpaceField.getValue(addr));
    }

    public boolean isIn(Address p) {
        return theSpace().contains(p);
    }

    public long capacity() {
        return theSpace().capacity();
    }

    public long used() {
        return theSpace().used();
    }

    public long free() {
        return theSpace().free();
    }

    public long contiguousAvailable() {
        return theSpace().free() + virtualSpace().uncommittedSize();
    }

    public void spaceIterate(SpaceClosure blk, boolean usedOnly) {
        blk.doSpace(theSpace());
    }

    public void printOn(PrintStream tty) {
        tty.print("  old ");
        theSpace().printOn(tty);
    }

    public Generation.Name kind() {
        return Generation.Name.MARK_SWEEP_COMPACT;
    }

    public String name() {
        return "tenured generation";
    }
}
