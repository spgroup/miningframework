package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class MethodCounters extends Metadata {

    public MethodCounters(Address addr) {
        super(addr);
    }

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("MethodCounters");
        interpreterInvocationCountField = new CIntField(type.getCIntegerField("_interpreter_invocation_count"), 0);
        interpreterThrowoutCountField = new CIntField(type.getCIntegerField("_interpreter_throwout_count"), 0);
        if (!VM.getVM().isCore()) {
            invocationCounter = new CIntField(type.getCIntegerField("_invocation_counter"), 0);
            backedgeCounter = new CIntField(type.getCIntegerField("_backedge_counter"), 0);
        }
    }

    private static CIntField interpreterInvocationCountField;

    private static CIntField interpreterThrowoutCountField;

    private static CIntField invocationCounter;

    private static CIntField backedgeCounter;

    public int interpreterInvocationCount() {
        return (int) interpreterInvocationCountField.getValue(this);
    }

    public int interpreterThrowoutCount() {
        return (int) interpreterThrowoutCountField.getValue(this);
    }

    public long getInvocationCounter() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!VM.getVM().isCore(), "must not be used in core build");
        }
        return invocationCounter.getValue(this);
    }

    public long getBackedgeCounter() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!VM.getVM().isCore(), "must not be used in core build");
        }
        return backedgeCounter.getValue(this);
    }

    public void printValueOn(PrintStream tty) {
    }
}
