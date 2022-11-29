package sun.jvm.hotspot.gc.parallel;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public abstract class ImmutableSpace extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("ImmutableSpace");
        bottomField = type.getAddressField("_bottom");
        endField = type.getAddressField("_end");
    }

    public ImmutableSpace(Address addr) {
        super(addr);
    }

    private static AddressField bottomField;

    private static AddressField endField;

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

    public abstract List getLiveRegions();

    public long capacity() {
        return end().minus(bottom());
    }

    public abstract long used();

    public boolean contains(Address p) {
        return (bottom().lessThanOrEqual(p) && end().greaterThan(p));
    }

    public void print() {
        printOn(System.out);
    }

    public abstract void printOn(PrintStream tty);
}
