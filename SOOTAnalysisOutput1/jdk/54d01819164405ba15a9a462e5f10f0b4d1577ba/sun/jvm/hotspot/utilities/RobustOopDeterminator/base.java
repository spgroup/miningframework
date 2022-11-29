package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.oops.Metadata;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class RobustOopDeterminator {

    private static AddressField klassField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("oopDesc");
        if (VM.getVM().isCompressedKlassPointersEnabled()) {
            klassField = type.getAddressField("_metadata._compressed_klass");
        } else {
            klassField = type.getAddressField("_metadata._klass");
        }
    }

    public static boolean oopLooksValid(OopHandle oop) {
        if (oop == null) {
            return false;
        }
        if (!VM.getVM().getUniverse().isIn(oop)) {
            return false;
        }
        try {
            if (VM.getVM().isCompressedKlassPointersEnabled()) {
                Metadata.instantiateWrapperFor(oop.getCompKlassAddressAt(klassField.getOffset()));
            } else {
                Metadata.instantiateWrapperFor(klassField.getValue(oop));
            }
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
