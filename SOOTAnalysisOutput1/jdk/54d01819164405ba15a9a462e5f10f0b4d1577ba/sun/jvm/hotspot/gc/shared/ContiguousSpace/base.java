package sun.jvm.hotspot.gc.shared;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class ContiguousSpace extends CompactibleSpace {

    private static AddressField topField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("ContiguousSpace");
        topField = type.getAddressField("_top");
    }

    public ContiguousSpace(Address addr) {
        super(addr);
    }

    public Address top() {
        return topField.getValue(addr);
    }

    public long capacity() {
        return end().minus(bottom());
    }

    public long used() {
        return top().minus(bottom());
    }

    public long free() {
        return end().minus(top());
    }

    public MemRegion usedRegion() {
        return new MemRegion(bottom(), top());
    }

    public List getLiveRegions() {
        List res = new ArrayList();
        res.add(new MemRegion(bottom(), top()));
        return res;
    }

    public boolean contains(Address p) {
        return (bottom().lessThanOrEqual(p) && top().greaterThan(p));
    }

    public void printOn(PrintStream tty) {
        tty.print(" [" + bottom() + "," + top() + "," + end() + ")");
        super.printOn(tty);
    }
}
