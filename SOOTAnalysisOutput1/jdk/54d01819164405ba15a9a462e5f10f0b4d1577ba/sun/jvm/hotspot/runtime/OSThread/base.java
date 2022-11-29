package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class OSThread extends VMObject {

    private static JIntField interruptedField;

    private static Field threadIdField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("OSThread");
        interruptedField = type.getJIntField("_interrupted");
        threadIdField = type.getField("_thread_id");
    }

    public OSThread(Address addr) {
        super(addr);
    }

    public boolean interrupted() {
        return ((int) interruptedField.getValue(addr)) != 0;
    }

    public int threadId() {
        return threadIdField.getJInt(addr);
    }
}
