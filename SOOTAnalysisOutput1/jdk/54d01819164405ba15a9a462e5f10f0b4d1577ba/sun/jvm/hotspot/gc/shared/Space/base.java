package sun.jvm.hotspot.gc.shared;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public abstract class Space extends VMObject {

    private static AddressField bottomField;

    private static AddressField endField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("Space");
        bottomField = type.getAddressField("_bottom");
        endField = type.getAddressField("_end");
    }

    public Space(Address addr) {
        super(addr);
    }

    public Address bottom() {
        return bottomField.getValue(addr);
    }

    public Address end() {
        return endField.getValue(addr);
    }

    public MemRegion usedRegion() {
        return new MemRegion(bottom(), end());
    }

    public OopHandle bottomAsOopHandle() {
        return bottomField.getOopHandle(addr);
    }

    public OopHandle nextOopHandle(OopHandle handle, long size) {
        return handle.addOffsetToAsOopHandle(size);
    }

    public abstract List getLiveRegions();

    public long capacity() {
        return end().minus(bottom());
    }

    public abstract long used();

    public abstract long free();

    public boolean contains(Address p) {
        return (bottom().lessThanOrEqual(p) && end().greaterThan(p));
    }

    public void print() {
        printOn(System.out);
    }

    public void printOn(PrintStream tty) {
        tty.print(" space capacity = ");
        tty.print(capacity());
        tty.print(", ");
        tty.print((double) used() * 100.0 / capacity());
        tty.print(" used");
    }
}
