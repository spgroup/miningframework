package sun.jvm.hotspot.oops;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class Array extends Oop {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    Array(OopHandle handle, ObjectHeap heap) {
        super(handle, heap);
    }

    private static void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("arrayOopDesc");
        typeSize = (int) type.getSize();
    }

    private static long headerSize = 0;

    private static long lengthOffsetInBytes = 0;

    private static long typeSize;

    private static long headerSizeInBytes() {
        if (headerSize != 0) {
            return headerSize;
        }
        if (VM.getVM().isCompressedKlassPointersEnabled()) {
            headerSize = typeSize;
        } else {
            headerSize = VM.getVM().alignUp(typeSize + VM.getVM().getIntSize(), VM.getVM().getHeapWordSize());
        }
        return headerSize;
    }

    private static long headerSize(BasicType type) {
        if (Universe.elementTypeShouldBeAligned(type)) {
            return alignObjectSize(headerSizeInBytes()) / VM.getVM().getHeapWordSize();
        } else {
            return headerSizeInBytes() / VM.getVM().getHeapWordSize();
        }
    }

    private long lengthOffsetInBytes() {
        if (lengthOffsetInBytes != 0) {
            return lengthOffsetInBytes;
        }
        if (VM.getVM().isCompressedKlassPointersEnabled()) {
            lengthOffsetInBytes = typeSize - VM.getVM().getIntSize();
        } else {
            lengthOffsetInBytes = typeSize;
        }
        return lengthOffsetInBytes;
    }

    public long getLength() {
        boolean isUnsigned = true;
        return this.getHandle().getCIntegerAt(lengthOffsetInBytes(), VM.getVM().getIntSize(), isUnsigned);
    }

    public long getObjectSize() {
        ArrayKlass klass = (ArrayKlass) getKlass();
        long s = getLength() << klass.getLog2ElementSize();
        s += klass.getArrayHeaderInBytes();
        s = Oop.alignObjectSize(s);
        return s;
    }

    public static long baseOffsetInBytes(BasicType type) {
        return headerSize(type) * VM.getVM().getHeapWordSize();
    }

    public boolean isArray() {
        return true;
    }

    public void iterateFields(OopVisitor visitor, boolean doVMFields) {
        super.iterateFields(visitor, doVMFields);
    }
}
