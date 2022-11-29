package sun.jvm.hotspot.opto;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class PhaseRegAlloc extends Phase {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("PhaseRegAlloc");
        nodeRegsField = type.getAddressField("_node_regs");
        nodeRegsMaxIndexField = new CIntField(type.getCIntegerField("_node_regs_max_index"), 0);
        framesizeField = new CIntField(type.getCIntegerField("_framesize"), 0);
        maxRegField = new CIntField(type.getCIntegerField("_max_reg"), 0);
    }

    private static AddressField nodeRegsField;

    private static CIntField nodeRegsMaxIndexField;

    private static CIntField framesizeField;

    private static CIntField maxRegField;

    public PhaseRegAlloc(Address addr) {
        super(addr);
    }
}
