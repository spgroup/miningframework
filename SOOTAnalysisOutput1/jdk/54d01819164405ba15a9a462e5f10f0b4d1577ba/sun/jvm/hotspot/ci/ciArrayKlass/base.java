package sun.jvm.hotspot.ci;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ciArrayKlass extends ciKlass {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ciArrayKlass");
        dimensionField = new IntField(type.getJIntField("_dimension"), 0);
    }

    private static IntField dimensionField;

    public ciArrayKlass(Address addr) {
        super(addr);
    }
}
