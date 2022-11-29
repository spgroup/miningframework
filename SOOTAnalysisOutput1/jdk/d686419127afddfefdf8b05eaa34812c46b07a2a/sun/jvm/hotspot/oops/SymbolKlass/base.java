package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class SymbolKlass extends Klass {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("symbolKlass");
        headerSize = type.getSize() + Oop.getHeaderSize();
    }

    SymbolKlass(OopHandle handle, ObjectHeap heap) {
        super(handle, heap);
    }

    private static long headerSize;

    public long getObjectSize() {
        return alignObjectSize(headerSize);
    }

    public void printValueOn(PrintStream tty) {
        tty.print("SymbolKlass");
    }
}
