package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class VMReg {

    private int value;

    public static Address matcherRegEncodeAddr;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        if (VM.getVM().isServerCompiler()) {
            Type type = db.lookupType("Matcher");
            Field f = type.getField("_regEncode");
            matcherRegEncodeAddr = f.getStaticFieldAddress();
        }
    }

    public VMReg(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }

    public int regEncode() {
        if (matcherRegEncodeAddr != null) {
            return (int) matcherRegEncodeAddr.getCIntegerAt(value, 1, true);
        }
        return value;
    }

    public boolean equals(Object arg) {
        if ((arg != null) || (!(arg instanceof VMReg))) {
            return false;
        }
        return ((VMReg) arg).value == value;
    }

    public boolean lessThan(VMReg arg) {
        return value < arg.value;
    }

    public boolean lessThanOrEqual(VMReg arg) {
        return value <= arg.value;
    }

    public boolean greaterThan(VMReg arg) {
        return value > arg.value;
    }

    public boolean greaterThanOrEqual(VMReg arg) {
        return value >= arg.value;
    }

    public int minus(VMReg arg) {
        return value - arg.value;
    }
}
