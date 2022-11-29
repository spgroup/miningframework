package sun.jvm.hotspot.interpreter;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class InterpreterCodelet extends Stub {

    private static long instanceSize;

    private static CIntegerField sizeField;

    private static AddressField descriptionField;

    private static CIntegerField bytecodeField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("InterpreterCodelet");
        sizeField = type.getCIntegerField("_size");
        descriptionField = type.getAddressField("_description");
        bytecodeField = type.getCIntegerField("_bytecode");
        instanceSize = type.getSize();
    }

    public InterpreterCodelet(Address addr) {
        super(addr);
    }

    public long getSize() {
        return sizeField.getValue(addr);
    }

    public Address codeBegin() {
        return addr.addOffsetTo(instanceSize);
    }

    public Address codeEnd() {
        return addr.addOffsetTo(getSize());
    }

    public long codeSize() {
        return codeEnd().minus(codeBegin());
    }

    public String getDescription() {
        return CStringUtilities.getString(descriptionField.getValue(addr));
    }

    public void verify() {
    }

    public void printOn(PrintStream tty) {
        String desc = getDescription();
        if (desc != null) {
            tty.print(desc);
        }
        tty.println(" [" + codeBegin() + ", " + codeEnd() + ")  " + codeSize() + " bytes  ");
    }
}
