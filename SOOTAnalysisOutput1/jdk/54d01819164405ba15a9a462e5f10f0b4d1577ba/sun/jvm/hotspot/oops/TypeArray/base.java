package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class TypeArray extends Array {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("typeArrayOop");
    }

    TypeArray(OopHandle handle, ObjectHeap heap) {
        super(handle, heap);
    }

    public boolean isTypeArray() {
        return true;
    }

    public byte getByteAt(long index) {
        if (index < 0 || index >= getLength()) {
            throw new ArrayIndexOutOfBoundsException(index + " " + getLength());
        }
        long offset = baseOffsetInBytes(BasicType.T_BYTE) + index * getHeap().getByteSize();
        return getHandle().getJByteAt(offset);
    }

    public boolean getBooleanAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_BOOLEAN) + index * getHeap().getBooleanSize();
        return getHandle().getJBooleanAt(offset);
    }

    public char getCharAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_CHAR) + index * getHeap().getCharSize();
        return getHandle().getJCharAt(offset);
    }

    public int getIntAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_INT) + index * getHeap().getIntSize();
        return getHandle().getJIntAt(offset);
    }

    public short getShortAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_SHORT) + index * getHeap().getShortSize();
        return getHandle().getJShortAt(offset);
    }

    public long getLongAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_LONG) + index * getHeap().getLongSize();
        return getHandle().getJLongAt(offset);
    }

    public float getFloatAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_FLOAT) + index * getHeap().getFloatSize();
        return getHandle().getJFloatAt(offset);
    }

    public double getDoubleAt(long index) {
        long offset = baseOffsetInBytes(BasicType.T_DOUBLE) + index * getHeap().getDoubleSize();
        return getHandle().getJDoubleAt(offset);
    }

    public void printValueOn(PrintStream tty) {
        TypeArrayKlass klass = (TypeArrayKlass) getKlass();
        tty.print(klass.getTypeName());
    }

    public void iterateFields(OopVisitor visitor, boolean doVMFields) {
        super.iterateFields(visitor, doVMFields);
        TypeArrayKlass klass = (TypeArrayKlass) getKlass();
        int length = (int) getLength();
        int type = (int) klass.getElementType();
        for (int index = 0; index < length; index++) {
            FieldIdentifier id = new IndexableFieldIdentifier(index);
            switch(type) {
                case TypeArrayKlass.T_BOOLEAN:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_BOOLEAN) + index * getHeap().getBooleanSize();
                        visitor.doBoolean(new BooleanField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_CHAR:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_CHAR) + index * getHeap().getCharSize();
                        visitor.doChar(new CharField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_FLOAT:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_FLOAT) + index * getHeap().getFloatSize();
                        visitor.doFloat(new FloatField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_DOUBLE:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_DOUBLE) + index * getHeap().getDoubleSize();
                        visitor.doDouble(new DoubleField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_BYTE:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_BYTE) + index * getHeap().getByteSize();
                        visitor.doByte(new ByteField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_SHORT:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_SHORT) + index * getHeap().getShortSize();
                        visitor.doShort(new ShortField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_INT:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_INT) + index * getHeap().getIntSize();
                        visitor.doInt(new IntField(id, offset, false), false);
                        break;
                    }
                case TypeArrayKlass.T_LONG:
                    {
                        long offset = baseOffsetInBytes(BasicType.T_LONG) + index * getHeap().getLongSize();
                        visitor.doLong(new LongField(id, offset, false), false);
                        break;
                    }
            }
        }
    }
}
