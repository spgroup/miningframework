package sun.jvm.hotspot.gc.parallel;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class MutableSpace extends ImmutableSpace {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("MutableSpace");
        topField = type.getAddressField("_top");
    }

    public MutableSpace(Address addr) {
        super(addr);
    }

    private static AddressField topField;

    public Address top() {
        return topField.getValue(addr);
    }

    public long used() {
        return top().minus(bottom());
    }

    public List getLiveRegions() {
        List res = new ArrayList();
        res.add(new MemRegion(bottom(), top()));
        return res;
    }

    public void printOn(PrintStream tty) {
        tty.print(" [" + bottom() + "," + top() + "," + end() + "] ");
    }
}
