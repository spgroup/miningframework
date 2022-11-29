package sun.jvm.hotspot.classfile;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ClassLoaderData extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ClassLoaderData");
        classLoaderField = type.getOopField("_class_loader");
        nextField = type.getAddressField("_next");
    }

    private static sun.jvm.hotspot.types.OopField classLoaderField;

    private static AddressField nextField;

    public ClassLoaderData(Address addr) {
        super(addr);
    }

    public static ClassLoaderData instantiateWrapperFor(Address addr) {
        if (addr == null) {
            return null;
        }
        return new ClassLoaderData(addr);
    }

    public Oop getClassLoader() {
        return VM.getVM().getObjectHeap().newOop(classLoaderField.getValue(getAddress()));
    }
}
