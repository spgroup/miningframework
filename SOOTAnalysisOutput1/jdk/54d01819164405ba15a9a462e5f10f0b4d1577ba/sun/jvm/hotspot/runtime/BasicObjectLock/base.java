package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class BasicObjectLock extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("BasicObjectLock");
        lockField = type.getField("_lock");
        objField = type.getOopField("_obj");
        size = (int) type.getSize();
    }

    private static sun.jvm.hotspot.types.Field lockField;

    private static sun.jvm.hotspot.types.OopField objField;

    private static int size;

    public BasicObjectLock(Address addr) {
        super(addr);
    }

    public OopHandle obj() {
        return objField.getValue(addr);
    }

    public BasicLock lock() {
        return new BasicLock(addr.addOffsetTo(lockField.getOffset()));
    }

    public static int size() {
        return size;
    }

    public Address address() {
        return addr;
    }
}
