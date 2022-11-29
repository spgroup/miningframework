package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.PrintStream;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class MachIfNode extends MachNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("MachIfNode");
        probField = type.getJFloatField("_prob");
        fcntField = type.getJFloatField("_fcnt");
    }

    private static JFloatField probField;

    private static JFloatField fcntField;

    float prob() {
        return probField.getValue(getAddress());
    }

    float cnt() {
        return fcntField.getValue(getAddress());
    }

    public MachIfNode(Address addr) {
        super(addr);
    }

    public void dumpSpec(PrintStream out) {
        out.print("P=" + prob() + ", C=" + cnt());
    }
}
