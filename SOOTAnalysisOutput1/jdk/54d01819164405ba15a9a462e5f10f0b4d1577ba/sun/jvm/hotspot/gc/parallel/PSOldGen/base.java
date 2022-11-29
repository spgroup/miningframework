package sun.jvm.hotspot.gc.parallel;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class PSOldGen extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("PSOldGen");
        objectSpaceField = type.getAddressField("_object_space");
    }

    public PSOldGen(Address addr) {
        super(addr);
    }

    private static AddressField objectSpaceField;

    public MutableSpace objectSpace() {
        return (MutableSpace) VMObjectFactory.newObject(MutableSpace.class, objectSpaceField.getValue(addr));
    }

    public long capacity() {
        return objectSpace().capacity();
    }

    public long used() {
        return objectSpace().used();
    }

    public boolean isIn(Address a) {
        return objectSpace().contains(a);
    }

    public void printOn(PrintStream tty) {
        tty.print("PSOldGen [ ");
        objectSpace().printOn(tty);
        tty.print(" ] ");
    }
}
