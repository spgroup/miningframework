package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class InstanceMirrorKlass extends InstanceKlass {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("InstanceMirrorKlass");
    }

    public InstanceMirrorKlass(Address addr) {
        super(addr);
    }

    public long getObjectSize(Oop o) {
        return java_lang_Class.getOopSize(o) * VM.getVM().getAddressSize();
    }

    public void iterateNonStaticFields(OopVisitor visitor, Oop obj) {
        super.iterateNonStaticFields(visitor, obj);
        Klass klass = java_lang_Class.asKlass(obj);
        if (klass instanceof InstanceKlass) {
            ((InstanceKlass) klass).iterateStaticFields(visitor);
        }
    }
}
