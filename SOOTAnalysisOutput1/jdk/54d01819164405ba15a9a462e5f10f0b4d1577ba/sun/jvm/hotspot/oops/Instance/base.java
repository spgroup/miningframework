package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class Instance extends Oop {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static long typeSize;

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("instanceOopDesc");
        typeSize = type.getSize();
    }

    Instance(OopHandle handle, ObjectHeap heap) {
        super(handle, heap);
    }

    public static long getHeaderSize() {
        if (VM.getVM().isCompressedKlassPointersEnabled()) {
            return typeSize - VM.getVM().getIntSize();
        } else {
            return typeSize;
        }
    }

    public boolean isInstance() {
        return true;
    }

    public void iterateFields(OopVisitor visitor, boolean doVMFields) {
        super.iterateFields(visitor, doVMFields);
        ((InstanceKlass) getKlass()).iterateNonStaticFields(visitor, this);
    }

    public void printValueOn(PrintStream tty) {
        if (getKlass().getName().asString().equals("java/lang/String")) {
            tty.print("\"" + OopUtilities.stringOopToString(this) + "\"");
        } else {
            super.printValueOn(tty);
        }
    }
}
