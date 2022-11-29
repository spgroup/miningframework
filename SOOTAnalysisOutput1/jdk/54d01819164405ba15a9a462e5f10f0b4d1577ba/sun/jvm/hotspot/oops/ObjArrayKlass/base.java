package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class ObjArrayKlass extends ArrayKlass {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ObjArrayKlass");
        elementKlass = new MetadataField(type.getAddressField("_element_klass"), 0);
        bottomKlass = new MetadataField(type.getAddressField("_bottom_klass"), 0);
    }

    public ObjArrayKlass(Address addr) {
        super(addr);
    }

    private static MetadataField elementKlass;

    private static MetadataField bottomKlass;

    public Klass getElementKlass() {
        return (Klass) elementKlass.getValue(this);
    }

    public Klass getBottomKlass() {
        return (Klass) bottomKlass.getValue(this);
    }

    public long computeModifierFlags() {
        long elementFlags = getElementKlass().computeModifierFlags();
        long arrayFlags = 0L;
        if ((elementFlags & (JVM_ACC_PUBLIC | JVM_ACC_PROTECTED)) != 0) {
            arrayFlags = JVM_ACC_ABSTRACT | JVM_ACC_FINAL | JVM_ACC_PUBLIC;
        } else {
            arrayFlags = JVM_ACC_ABSTRACT | JVM_ACC_FINAL;
        }
        return arrayFlags;
    }

    public void iterateFields(MetadataVisitor visitor) {
        super.iterateFields(visitor);
        visitor.doMetadata(elementKlass, true);
        visitor.doMetadata(bottomKlass, true);
    }

    public Klass arrayKlassImpl(boolean orNull, int n) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(getDimension() <= n, "check order of chain");
        }
        int dimension = (int) getDimension();
        if (dimension == n) {
            return this;
        }
        ObjArrayKlass ak = (ObjArrayKlass) getHigherDimension();
        if (ak == null) {
            if (orNull)
                return null;
            throw new RuntimeException("Can not allocate array klasses in debugging system");
        }
        if (orNull) {
            return ak.arrayKlassOrNull(n);
        }
        return ak.arrayKlass(n);
    }

    public Klass arrayKlassImpl(boolean orNull) {
        return arrayKlassImpl(orNull, (int) (getDimension() + 1));
    }

    public void printValueOn(PrintStream tty) {
        tty.print("ObjArrayKlass for ");
        getElementKlass().printValueOn(tty);
    }
}
