package sun.jvm.hotspot.oops;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.AddressField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.types.TypeDataBase;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.jdi.JVMTIThreadState;

public class java_lang_Class {

    static int klassOffset;

    static IntField oopSizeField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type jlc = db.lookupType("java_lang_Class");
        klassOffset = (int) jlc.getCIntegerField("_klass_offset").getValue();
        int oopSizeOffset = (int) jlc.getCIntegerField("_oop_size_offset").getValue();
        oopSizeField = new IntField(new NamedFieldIdentifier("oop_size"), oopSizeOffset, true);
    }

    public static Klass asKlass(Oop aClass) {
        return (Klass) Metadata.instantiateWrapperFor(aClass.getHandle().getAddressAt(klassOffset));
    }

    public static long getOopSize(Oop aClass) {
        return java_lang_Class.oopSizeField.getValue(aClass);
    }
}
