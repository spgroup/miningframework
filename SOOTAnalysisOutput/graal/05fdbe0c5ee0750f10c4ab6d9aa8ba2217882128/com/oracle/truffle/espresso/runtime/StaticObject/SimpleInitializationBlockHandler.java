package com.oracle.truffle.espresso.runtime;

import static com.oracle.truffle.api.CompilerDirectives.castExact;
import static com.oracle.truffle.espresso.vm.InterpreterToVM.instanceOf;
import java.lang.reflect.Array;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.espresso.EspressoLanguage;
import com.oracle.truffle.espresso.descriptors.Symbol;
import com.oracle.truffle.espresso.impl.ArrayKlass;
import com.oracle.truffle.espresso.impl.Field;
import com.oracle.truffle.espresso.impl.Klass;
import com.oracle.truffle.espresso.impl.ObjectKlass;
import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.espresso.meta.JavaKind;
import com.oracle.truffle.espresso.meta.Meta;
import sun.misc.Unsafe;

@ExportLibrary(InteropLibrary.class)
public final class StaticObject implements TruffleObject {

    @ExportMessage
    public static boolean isNull(StaticObject object) {
        assert object != null;
        return object == StaticObject.NULL;
    }

    public static boolean isEspressoNull(Object object) {
        return object == StaticObject.NULL;
    }

    @ExportMessage
    public boolean isString() {
        return StaticObject.notNull(this) && getKlass() == getKlass().getMeta().String;
    }

    @ExportMessage
    public String asString() {
        return Meta.toHostString(this);
    }

    private static final Unsafe U;

    public static final StaticObject[] EMPTY_ARRAY = new StaticObject[0];

    public static final StaticObject VOID = new StaticObject();

    public static final StaticObject NULL = new StaticObject();

    static {
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw EspressoError.shouldNotReachHere(e);
        }
    }

    private final Object fields;

    private final byte[] primitiveFields;

    public byte[] cloneFields() {
        return primitiveFields.clone();
    }

    private StaticObject() {
        this.klass = null;
        this.fields = null;
        this.primitiveFields = null;
    }

    StaticObject(ObjectKlass klass, Object[] fields, byte[] primitiveFields) {
        this.klass = klass;
        this.fields = fields;
        this.primitiveFields = primitiveFields;
    }

    public StaticObject(ObjectKlass klass) {
        this(klass, false);
    }

    public StaticObject(ObjectKlass guestClass, Klass thisKlass) {
        assert thisKlass != null;
        assert guestClass == guestClass.getMeta().Class;
        this.klass = guestClass;
        int primitiveFieldCount = guestClass.getPrimitiveFieldTotalByteCount();
        this.fields = guestClass.getObjectFieldsCount() > 0 ? new Object[guestClass.getObjectFieldsCount()] : null;
        this.primitiveFields = primitiveFieldCount > 0 ? new byte[primitiveFieldCount] : null;
        initFields(guestClass, false);
        setHiddenField(thisKlass.getMeta().HIDDEN_MIRROR_KLASS, thisKlass);
    }

    public StaticObject(ObjectKlass klass, boolean isStatic) {
        assert klass != klass.getMeta().Class || isStatic;
        this.klass = klass;
        if (isStatic) {
            this.fields = klass.getStaticObjectFieldsCount() > 0 ? new Object[klass.getStaticObjectFieldsCount()] : null;
            this.primitiveFields = klass.getPrimitiveStaticFieldTotalByteCount() > 0 ? new byte[klass.getPrimitiveStaticFieldTotalByteCount()] : null;
        } else {
            this.fields = klass.getObjectFieldsCount() > 0 ? new Object[klass.getObjectFieldsCount()] : null;
            this.primitiveFields = klass.getPrimitiveFieldTotalByteCount() > 0 ? new byte[klass.getPrimitiveFieldTotalByteCount()] : null;
        }
        initFields(klass, isStatic);
    }

    public static StaticObject createArray(ArrayKlass klass, Object array) {
        return new StaticObject(klass, array);
    }

    private StaticObject(ArrayKlass klass, Object array) {
        this.klass = klass;
        assert klass.isArray();
        assert array != null;
        assert !(array instanceof StaticObject);
        assert array.getClass().isArray();
        this.fields = array;
        this.primitiveFields = null;
    }

    private final Klass klass;

    public final Klass getKlass() {
        return klass;
    }

    public static boolean notNull(StaticObject object) {
        return !isNull(object);
    }

    public final boolean isStaticStorage() {
        return this == getKlass().getStatics();
    }

    public boolean isStatic() {
        return this == getKlass().getStatics();
    }

    public StaticObject copy() {
        if (isNull(this)) {
            return NULL;
        }
        if (getKlass().isArray()) {
            return new StaticObject((ArrayKlass) getKlass(), cloneWrapped());
        } else {
            return new StaticObject((ObjectKlass) getKlass(), fields == null ? null : ((Object[]) fields).clone(), primitiveFields == null ? null : primitiveFields.clone());
        }
    }

    @ExplodeLoop
    private void initFields(ObjectKlass thisKlass, boolean isStatic) {
        CompilerAsserts.partialEvaluationConstant(thisKlass);
        if (isStatic) {
            for (Field f : thisKlass.getStaticFieldTable()) {
                assert f.isStatic();
                if (f.getKind() == JavaKind.Object) {
                    setUnsafeField(f.getFieldIndex(), StaticObject.NULL);
                }
            }
        } else {
            for (Field f : thisKlass.getFieldTable()) {
                assert !f.isStatic();
                if (!f.isHidden()) {
                    if (f.getKind() == JavaKind.Object) {
                        setUnsafeField(f.getFieldIndex(), StaticObject.NULL);
                    }
                }
            }
        }
    }

    private static long getObjectFieldIndex(int index) {
        return Unsafe.ARRAY_OBJECT_BASE_OFFSET + Unsafe.ARRAY_OBJECT_INDEX_SCALE * (long) index;
    }

    @TruffleBoundary
public final StaticObject getFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return (StaticObject) U.getObjectVolatile(fields, getObjectFieldIndex(field.getFieldIndex()));
    }

    public final StaticObject getField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert !field.getKind().isSubWord();
        Object result;
        if (field.isVolatile()) {
            result = getFieldVolatile(field);
        } else {
            result = getUnsafeField(field.getFieldIndex());
        }
        assert result != null;
        return (StaticObject) result;
    }

    public final Object getUnsafeField(int fieldIndex) {
        return U.getObject(fields, getObjectFieldIndex(fieldIndex));
    }

    @TruffleBoundary
public final void setFieldVolatile(Field field, Object value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        U.putObjectVolatile(fields, getObjectFieldIndex(field.getFieldIndex()), value);
    }

    public final void setField(Field field, Object value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert !field.getKind().isSubWord();
        if (field.isVolatile()) {
            setFieldVolatile(field, value);
        } else {
            Object[] fieldArray = castExact(fields, Object[].class);
            fieldArray[field.getFieldIndex()] = value;
        }
    }

    private void setUnsafeField(int index, Object value) {
        U.putObject(fields, getObjectFieldIndex(index), value);
    }

    public boolean compareAndSwapField(Field field, Object before, Object after) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.compareAndSwapObject(fields, getObjectFieldIndex(field.getFieldIndex()), before, after);
    }

    private static long getPrimitiveFieldIndex(int index) {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + Unsafe.ARRAY_BYTE_INDEX_SCALE * (long) index;
    }

    public final boolean getBooleanField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Boolean;
        if (field.isVolatile()) {
            return getByteFieldVolatile(field) != 0;
        } else {
            return U.getByte(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex())) != 0;
        }
    }

    @TruffleBoundary
public byte getByteFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getByteVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public final byte getByteField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Byte;
        if (field.isVolatile()) {
            return getByteFieldVolatile(field);
        } else {
            return U.getByte(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    public final char getCharField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Char;
        if (field.isVolatile()) {
            return getCharFieldVolatile(field);
        } else {
            return U.getChar(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public char getCharFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getCharVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public final short getShortField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Short;
        if (field.isVolatile()) {
            return getShortFieldVolatile(field);
        } else {
            return U.getShort(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public short getShortFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getShortVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public final int getIntField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Int;
        if (field.isVolatile()) {
            return getIntFieldVolatile(field);
        } else {
            return U.getInt(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public int getIntFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getIntVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public float getFloatField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Float;
        if (field.isVolatile()) {
            return getFloatFieldVolatile(field);
        } else {
            return U.getFloat(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public float getFloatFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getFloatVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public double getDoubleField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Double;
        if (field.isVolatile()) {
            return getDoubleFieldVolatile(field);
        } else {
            return U.getDouble(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public double getDoubleFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getDoubleVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public final void setBooleanField(Field field, boolean value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Boolean;
        if (field.isVolatile()) {
            setBooleanFieldVolatile(field, value);
        } else {
            U.putByte(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), (byte) (value ? 1 : 0));
        }
    }

    @TruffleBoundary
public void setBooleanFieldVolatile(Field field, boolean value) {
        setByteFieldVolatile(field, (byte) (value ? 1 : 0));
    }

    public final void setByteField(Field field, byte value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Byte;
        if (field.isVolatile()) {
            setByteFieldVolatile(field, value);
        } else {
            U.putByte(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setByteFieldVolatile(Field field, byte value) {
        U.putByteVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public final void setCharField(Field field, char value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Char;
        if (field.isVolatile()) {
            setCharFieldVolatile(field, value);
        } else {
            U.putChar(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setCharFieldVolatile(Field field, char value) {
        U.putCharVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public final void setShortField(Field field, short value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Short;
        if (field.isVolatile()) {
            setShortFieldVolatile(field, value);
        } else {
            U.putShort(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setShortFieldVolatile(Field field, short value) {
        U.putShortVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public final void setIntField(Field field, int value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Int || field.getKind() == JavaKind.Float;
        if (field.isVolatile()) {
            setIntFieldVolatile(field, value);
        } else {
            U.putInt(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setIntFieldVolatile(Field field, int value) {
        U.putIntVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public void setFloatField(Field field, float value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Float;
        if (field.isVolatile()) {
            setFloatFieldVolatile(field, value);
        } else {
            U.putFloat(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setDoubleFieldVolatile(Field field, double value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        U.putDoubleVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public void setDoubleField(Field field, double value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind() == JavaKind.Double;
        if (field.isVolatile()) {
            setDoubleFieldVolatile(field, value);
        } else {
            U.putDouble(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    @TruffleBoundary
public void setFloatFieldVolatile(Field field, float value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        U.putFloatVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public boolean compareAndSwapIntField(Field field, int before, int after) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.compareAndSwapInt(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), before, after);
    }

    @TruffleBoundary
public final long getLongFieldVolatile(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.getLongVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
    }

    public final long getLongField(Field field) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind().needsTwoSlots();
        if (field.isVolatile()) {
            return getLongFieldVolatile(field);
        } else {
            return U.getLong(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()));
        }
    }

    @TruffleBoundary
public final void setLongFieldVolatile(Field field, long value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        U.putLongVolatile(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
    }

    public final void setLongField(Field field, long value) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        assert field.getKind().needsTwoSlots();
        if (field.isVolatile()) {
            setLongFieldVolatile(field, value);
        } else {
            U.putLong(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), value);
        }
    }

    public boolean compareAndSwapLongField(Field field, long before, long after) {
        assert field.getDeclaringKlass().isAssignableFrom(getKlass());
        return U.compareAndSwapLong(primitiveFields, getPrimitiveFieldIndex(field.getFieldIndex()), before, after);
    }

    public final Klass getMirrorKlass() {
        assert getKlass().getType() == Symbol.Type.Class;
        Klass result = (Klass) getHiddenField(getKlass().getMeta().HIDDEN_MIRROR_KLASS);
        if (result == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw EspressoError.shouldNotReachHere("Uninitialized mirror class");
        }
        return result;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        if (this == VOID) {
            return "void";
        }
        if (this == NULL) {
            return "null";
        }
        if (getKlass() == getKlass().getMeta().String) {
            return Meta.toHostString(this);
        }
        if (isArray()) {
            return unwrap().toString();
        }
        if (getKlass() == getKlass().getMeta().Class) {
            return "mirror: " + getMirrorKlass().toString();
        }
        return getKlass().getType().toString();
    }

    @TruffleBoundary
    public String toVerboseString() {
        if (this == VOID) {
            return "void";
        }
        if (this == NULL) {
            return "null";
        }
        if (getKlass() == getKlass().getMeta().String) {
            return Meta.toHostString(this);
        }
        if (isArray()) {
            return unwrap().toString();
        }
        if (getKlass() == getKlass().getMeta().Class) {
            return "mirror: " + getMirrorKlass().toString();
        }
        StringBuilder str = new StringBuilder(getKlass().getType().toString());
        for (Field f : ((ObjectKlass) getKlass()).getFieldTable()) {
            str.append("\n    ").append(f.getName()).append(": ").append(f.get(this).toString());
        }
        return str.toString();
    }

    public void setHiddenField(Field hiddenField, Object value) {
        assert hiddenField.isHidden();
        setUnsafeField(hiddenField.getFieldIndex(), value);
    }

    public Object getHiddenField(Field hiddenField) {
        assert hiddenField.isHidden();
        return getUnsafeField(hiddenField.getFieldIndex());
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap() {
        assert isArray();
        return (T) fields;
    }

    public <T> T get(int index) {
        assert isArray();
        return this.<T[]>unwrap()[index];
    }

    public void putObject(StaticObject value, int index, Meta meta) {
        assert isArray();
        if (index >= 0 && index < length()) {
            U.putObject(fields, getObjectFieldIndex(index), arrayStoreExCheck(value, ((ArrayKlass) klass).getComponentType(), meta));
        } else {
            CompilerDirectives.transferToInterpreter();
            throw meta.throwEx(ArrayIndexOutOfBoundsException.class);
        }
    }

    private static Object arrayStoreExCheck(StaticObject value, Klass componentType, Meta meta) {
        if (StaticObject.isNull(value) || instanceOf(value, componentType)) {
            return value;
        } else {
            throw meta.throwEx(ArrayStoreException.class);
        }
    }

    public int length() {
        assert isArray();
        return Array.getLength(fields);
    }

    private Object cloneWrapped() {
        assert isArray();
        if (fields instanceof boolean[]) {
            return this.<boolean[]>unwrap().clone();
        }
        if (fields instanceof byte[]) {
            return this.<byte[]>unwrap().clone();
        }
        if (fields instanceof char[]) {
            return this.<char[]>unwrap().clone();
        }
        if (fields instanceof short[]) {
            return this.<short[]>unwrap().clone();
        }
        if (fields instanceof int[]) {
            return this.<int[]>unwrap().clone();
        }
        if (fields instanceof float[]) {
            return this.<float[]>unwrap().clone();
        }
        if (fields instanceof double[]) {
            return this.<double[]>unwrap().clone();
        }
        if (fields instanceof long[]) {
            return this.<long[]>unwrap().clone();
        }
        return this.<StaticObject[]>unwrap().clone();
    }

    public static StaticObject wrap(StaticObject[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta.Object_array, array);
    }

    public static StaticObject wrap(byte[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._byte_array, array);
    }

    public static StaticObject wrap(boolean[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._boolean_array, array);
    }

    public static StaticObject wrap(char[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._char_array, array);
    }

    public static StaticObject wrap(short[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._short_array, array);
    }

    public static StaticObject wrap(int[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._int_array, array);
    }

    public static StaticObject wrap(float[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._float_array, array);
    }

    public static StaticObject wrap(double[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._double_array, array);
    }

    public static StaticObject wrap(long[] array) {
        Meta meta = EspressoLanguage.getCurrentContext().getMeta();
        return new StaticObject(meta._long_array, array);
    }

    public static StaticObject wrapPrimitiveArray(Object array) {
        assert array != null;
        assert array.getClass().isArray() && array.getClass().getComponentType().isPrimitive();
        if (array instanceof boolean[]) {
            return wrap((boolean[]) array);
        }
        if (array instanceof byte[]) {
            return wrap((byte[]) array);
        }
        if (array instanceof char[]) {
            return wrap((char[]) array);
        }
        if (array instanceof short[]) {
            return wrap((short[]) array);
        }
        if (array instanceof int[]) {
            return wrap((int[]) array);
        }
        if (array instanceof float[]) {
            return wrap((float[]) array);
        }
        if (array instanceof double[]) {
            return wrap((double[]) array);
        }
        if (array instanceof long[]) {
            return wrap((long[]) array);
        }
        throw EspressoError.shouldNotReachHere("Not a primitive array " + array);
    }

    public boolean isArray() {
        return getKlass().isArray();
    }

    public static long getArrayByteOffset(int index) {
        return Unsafe.ARRAY_BYTE_BASE_OFFSET + Unsafe.ARRAY_BYTE_INDEX_SCALE * (long) index;
    }

    static {
        assert (Unsafe.ARRAY_BYTE_BASE_OFFSET == Unsafe.ARRAY_BOOLEAN_BASE_OFFSET && Unsafe.ARRAY_BYTE_INDEX_SCALE == Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);
    }

    public void setArrayByte(byte value, int index, Meta meta) {
        assert isArray() && (fields instanceof byte[] || fields instanceof boolean[]);
        if (index >= 0 && index < length()) {
            U.putByte(fields, getArrayByteOffset(index), value);
        } else {
            throw meta.throwEx(ArrayIndexOutOfBoundsException.class);
        }
    }

    public byte getArrayByte(int index, Meta meta) {
        assert isArray() && (fields instanceof byte[] || fields instanceof boolean[]);
        if (index >= 0 && index < length()) {
            return U.getByte(fields, getArrayByteOffset(index));
        } else {
            throw meta.throwEx(ArrayIndexOutOfBoundsException.class);
        }
    }

    public StaticObject getAndSetObject(Field field, StaticObject value) {
        return (StaticObject) U.getAndSetObject(fields, getObjectFieldIndex(field.getFieldIndex()), value);
    }
}