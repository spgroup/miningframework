package sun.invoke.util;

public enum Wrapper {

    BOOLEAN(Boolean.class, boolean.class, 'Z', Boolean.FALSE, new boolean[0], Format.unsigned(1)),
    BYTE(Byte.class, byte.class, 'B', new Byte((byte) 0), new byte[0], Format.signed(8)),
    SHORT(Short.class, short.class, 'S', new Short((short) 0), new short[0], Format.signed(16)),
    CHAR(Character.class, char.class, 'C', new Character((char) 0), new char[0], Format.unsigned(16)),
    INT(Integer.class, int.class, 'I', new Integer(0), new int[0], Format.signed(32)),
    LONG(Long.class, long.class, 'J', new Long(0), new long[0], Format.signed(64)),
    FLOAT(Float.class, float.class, 'F', (Float) (float) 0, new float[0], Format.floating(32)),
    DOUBLE(Double.class, double.class, 'D', (Double) (double) 0, new double[0], Format.floating(64)),
    OBJECT(Object.class, Object.class, 'L', null, new Object[0], Format.other(1)),
    VOID(Void.class, void.class, 'V', null, null, Format.other(0));

    private final Class<?> wrapperType;

    private final Class<?> primitiveType;

    private final char basicTypeChar;

    private final Object zero;

    private final Object emptyArray;

    private final int format;

    private final String wrapperSimpleName;

    private final String primitiveSimpleName;

    private Wrapper(Class<?> wtype, Class<?> ptype, char tchar, Object zero, Object emptyArray, int format) {
        this.wrapperType = wtype;
        this.primitiveType = ptype;
        this.basicTypeChar = tchar;
        this.zero = zero;
        this.emptyArray = emptyArray;
        this.format = format;
        this.wrapperSimpleName = wtype.getSimpleName();
        this.primitiveSimpleName = ptype.getSimpleName();
    }

    public String detailString() {
        return wrapperSimpleName + java.util.Arrays.asList(wrapperType, primitiveType, basicTypeChar, zero, "0x" + Integer.toHexString(format));
    }

    private abstract static class Format {

        static final int SLOT_SHIFT = 0, SIZE_SHIFT = 2, KIND_SHIFT = 12;

        static final int SIGNED = (-1) << KIND_SHIFT, UNSIGNED = 0 << KIND_SHIFT, FLOATING = 1 << KIND_SHIFT;

        static final int SLOT_MASK = ((1 << (SIZE_SHIFT - SLOT_SHIFT)) - 1), SIZE_MASK = ((1 << (KIND_SHIFT - SIZE_SHIFT)) - 1);

        static int format(int kind, int size, int slots) {
            assert (((kind >> KIND_SHIFT) << KIND_SHIFT) == kind);
            assert ((size & (size - 1)) == 0);
            assert ((kind == SIGNED) ? (size > 0) : (kind == UNSIGNED) ? (size > 0) : (kind == FLOATING) ? (size == 32 || size == 64) : false);
            assert ((slots == 2) ? (size == 64) : (slots == 1) ? (size <= 32) : false);
            return kind | (size << SIZE_SHIFT) | (slots << SLOT_SHIFT);
        }

        static final int INT = SIGNED | (32 << SIZE_SHIFT) | (1 << SLOT_SHIFT), SHORT = SIGNED | (16 << SIZE_SHIFT) | (1 << SLOT_SHIFT), BOOLEAN = UNSIGNED | (1 << SIZE_SHIFT) | (1 << SLOT_SHIFT), CHAR = UNSIGNED | (16 << SIZE_SHIFT) | (1 << SLOT_SHIFT), FLOAT = FLOATING | (32 << SIZE_SHIFT) | (1 << SLOT_SHIFT), VOID = UNSIGNED | (0 << SIZE_SHIFT) | (0 << SLOT_SHIFT), NUM_MASK = (-1) << SIZE_SHIFT;

        static int signed(int size) {
            return format(SIGNED, size, (size > 32 ? 2 : 1));
        }

        static int unsigned(int size) {
            return format(UNSIGNED, size, (size > 32 ? 2 : 1));
        }

        static int floating(int size) {
            return format(FLOATING, size, (size > 32 ? 2 : 1));
        }

        static int other(int slots) {
            return slots << SLOT_SHIFT;
        }
    }

    public int bitWidth() {
        return (format >> Format.SIZE_SHIFT) & Format.SIZE_MASK;
    }

    public int stackSlots() {
        return (format >> Format.SLOT_SHIFT) & Format.SLOT_MASK;
    }

    public boolean isSingleWord() {
        return (format & (1 << Format.SLOT_SHIFT)) != 0;
    }

    public boolean isDoubleWord() {
        return (format & (2 << Format.SLOT_SHIFT)) != 0;
    }

    public boolean isNumeric() {
        return (format & Format.NUM_MASK) != 0;
    }

    public boolean isIntegral() {
        return isNumeric() && format < Format.FLOAT;
    }

    public boolean isSubwordOrInt() {
        return isIntegral() && isSingleWord();
    }

    public boolean isSigned() {
        return format < Format.VOID;
    }

    public boolean isUnsigned() {
        return format >= Format.BOOLEAN && format < Format.FLOAT;
    }

    public boolean isFloating() {
        return format >= Format.FLOAT;
    }

    public boolean isOther() {
        return (format & ~Format.SLOT_MASK) == 0;
    }

    public boolean isConvertibleFrom(Wrapper source) {
        if (this == source)
            return true;
        if (this.compareTo(source) < 0) {
            return false;
        }
        boolean floatOrSigned = (((this.format & source.format) & Format.SIGNED) != 0);
        if (!floatOrSigned) {
            if (this.isOther())
                return true;
            if (source.format == Format.CHAR)
                return true;
            return false;
        }
        assert (this.isFloating() || this.isSigned());
        assert (source.isFloating() || source.isSigned());
        return true;
    }

    static {
        assert (checkConvertibleFrom());
    }

    private static boolean checkConvertibleFrom() {
        for (Wrapper w : values()) {
            assert (w.isConvertibleFrom(w));
            assert (VOID.isConvertibleFrom(w));
            if (w != VOID) {
                assert (OBJECT.isConvertibleFrom(w));
                assert (!w.isConvertibleFrom(VOID));
            }
            if (w != CHAR) {
                assert (!CHAR.isConvertibleFrom(w));
                if (!w.isConvertibleFrom(INT))
                    assert (!w.isConvertibleFrom(CHAR));
            }
            if (w != BOOLEAN) {
                assert (!BOOLEAN.isConvertibleFrom(w));
                if (w != VOID && w != OBJECT)
                    assert (!w.isConvertibleFrom(BOOLEAN));
            }
            if (w.isSigned()) {
                for (Wrapper x : values()) {
                    if (w == x)
                        continue;
                    if (x.isFloating())
                        assert (!w.isConvertibleFrom(x));
                    else if (x.isSigned()) {
                        if (w.compareTo(x) < 0)
                            assert (!w.isConvertibleFrom(x));
                        else
                            assert (w.isConvertibleFrom(x));
                    }
                }
            }
            if (w.isFloating()) {
                for (Wrapper x : values()) {
                    if (w == x)
                        continue;
                    if (x.isSigned())
                        assert (w.isConvertibleFrom(x));
                    else if (x.isFloating()) {
                        if (w.compareTo(x) < 0)
                            assert (!w.isConvertibleFrom(x));
                        else
                            assert (w.isConvertibleFrom(x));
                    }
                }
            }
        }
        return true;
    }

    public Object zero() {
        return zero;
    }

    public <T> T zero(Class<T> type) {
        return convert(zero, type);
    }

    public static Wrapper forPrimitiveType(Class<?> type) {
        Wrapper w = findPrimitiveType(type);
        if (w != null)
            return w;
        if (type.isPrimitive())
            throw new InternalError();
        throw newIllegalArgumentException("not primitive: " + type);
    }

    static Wrapper findPrimitiveType(Class<?> type) {
        Wrapper w = FROM_PRIM[hashPrim(type)];
        if (w != null && w.primitiveType == type) {
            return w;
        }
        return null;
    }

    public static Wrapper forWrapperType(Class<?> type) {
        Wrapper w = findWrapperType(type);
        if (w != null)
            return w;
        for (Wrapper x : values()) if (x.wrapperType == type)
            throw new InternalError();
        throw newIllegalArgumentException("not wrapper: " + type);
    }

    static Wrapper findWrapperType(Class<?> type) {
        Wrapper w = FROM_WRAP[hashWrap(type)];
        if (w != null && w.wrapperType == type) {
            return w;
        }
        return null;
    }

    public static Wrapper forBasicType(char type) {
        Wrapper w = FROM_CHAR[hashChar(type)];
        if (w != null && w.basicTypeChar == type) {
            return w;
        }
        for (Wrapper x : values()) if (w.basicTypeChar == type)
            throw new InternalError();
        throw newIllegalArgumentException("not basic type char: " + type);
    }

    public static Wrapper forBasicType(Class<?> type) {
        if (type.isPrimitive())
            return forPrimitiveType(type);
        return OBJECT;
    }

    private static final Wrapper[] FROM_PRIM = new Wrapper[16];

    private static final Wrapper[] FROM_WRAP = new Wrapper[16];

    private static final Wrapper[] FROM_CHAR = new Wrapper[16];

    private static int hashPrim(Class<?> x) {
        String xn = x.getName();
        if (xn.length() < 3)
            return 0;
        return (xn.charAt(0) + xn.charAt(2)) % 16;
    }

    private static int hashWrap(Class<?> x) {
        String xn = x.getName();
        final int offset = 10;
        assert (offset == "java.lang.".length());
        if (xn.length() < offset + 3)
            return 0;
        return (3 * xn.charAt(offset + 1) + xn.charAt(offset + 2)) % 16;
    }

    private static int hashChar(char x) {
        return (x + (x >> 1)) % 16;
    }

    static {
        for (Wrapper w : values()) {
            int pi = hashPrim(w.primitiveType);
            int wi = hashWrap(w.wrapperType);
            int ci = hashChar(w.basicTypeChar);
            assert (FROM_PRIM[pi] == null);
            assert (FROM_WRAP[wi] == null);
            assert (FROM_CHAR[ci] == null);
            FROM_PRIM[pi] = w;
            FROM_WRAP[wi] = w;
            FROM_CHAR[ci] = w;
        }
    }

    public Class<?> primitiveType() {
        return primitiveType;
    }

    public Class<?> wrapperType() {
        return wrapperType;
    }

    public <T> Class<T> wrapperType(Class<T> exampleType) {
        if (exampleType == wrapperType) {
            return exampleType;
        } else if (exampleType == primitiveType || wrapperType == Object.class || exampleType.isInterface()) {
            return forceType(wrapperType, exampleType);
        }
        throw newClassCastException(exampleType, primitiveType);
    }

    private static ClassCastException newClassCastException(Class<?> actual, Class<?> expected) {
        return new ClassCastException(actual + " is not compatible with " + expected);
    }

    public static <T> Class<T> asWrapperType(Class<T> type) {
        if (type.isPrimitive()) {
            return forPrimitiveType(type).wrapperType(type);
        }
        return type;
    }

    public static <T> Class<T> asPrimitiveType(Class<T> type) {
        Wrapper w = findWrapperType(type);
        if (w != null) {
            return forceType(w.primitiveType(), type);
        }
        return type;
    }

    public static boolean isWrapperType(Class<?> type) {
        return findWrapperType(type) != null;
    }

    public static boolean isPrimitiveType(Class<?> type) {
        return type.isPrimitive();
    }

    public static char basicTypeChar(Class<?> type) {
        if (!type.isPrimitive())
            return 'L';
        else
            return forPrimitiveType(type).basicTypeChar();
    }

    public char basicTypeChar() {
        return basicTypeChar;
    }

    public String wrapperSimpleName() {
        return wrapperSimpleName;
    }

    public String primitiveSimpleName() {
        return primitiveSimpleName;
    }

    public <T> T cast(Object x, Class<T> type) {
        return convert(x, type, true);
    }

    public <T> T convert(Object x, Class<T> type) {
        return convert(x, type, false);
    }

    private <T> T convert(Object x, Class<T> type, boolean isCast) {
        if (this == OBJECT) {
            assert (!type.isPrimitive());
            if (!type.isInterface())
                type.cast(x);
            @SuppressWarnings("unchecked")
            T result = (T) x;
            return result;
        }
        Class<T> wtype = wrapperType(type);
        if (wtype.isInstance(x)) {
            return wtype.cast(x);
        }
        if (!isCast) {
            Class<?> sourceType = x.getClass();
            Wrapper source = findWrapperType(sourceType);
            if (source == null || !this.isConvertibleFrom(source)) {
                throw newClassCastException(wtype, sourceType);
            }
        } else if (x == null) {
            @SuppressWarnings("unchecked")
            T z = (T) zero;
            return z;
        }
        @SuppressWarnings("unchecked")
        T result = (T) wrap(x);
        assert (result == null ? Void.class : result.getClass()) == wtype;
        return result;
    }

    static <T> Class<T> forceType(Class<?> type, Class<T> exampleType) {
        boolean z = (type == exampleType || type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType) || exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type) || type == Object.class && !exampleType.isPrimitive());
        if (!z)
            System.out.println(type + " <= " + exampleType);
        assert (type == exampleType || type.isPrimitive() && forPrimitiveType(type) == findWrapperType(exampleType) || exampleType.isPrimitive() && forPrimitiveType(exampleType) == findWrapperType(type) || type == Object.class && !exampleType.isPrimitive());
        @SuppressWarnings("unchecked")
        Class<T> result = (Class<T>) type;
        return result;
    }

    public Object wrap(Object x) {
        switch(basicTypeChar) {
            case 'L':
                return x;
            case 'V':
                return null;
        }
        Number xn = numberValue(x);
        switch(basicTypeChar) {
            case 'I':
                return Integer.valueOf(xn.intValue());
            case 'J':
                return Long.valueOf(xn.longValue());
            case 'F':
                return Float.valueOf(xn.floatValue());
            case 'D':
                return Double.valueOf(xn.doubleValue());
            case 'S':
                return Short.valueOf((short) xn.intValue());
            case 'B':
                return Byte.valueOf((byte) xn.intValue());
            case 'C':
                return Character.valueOf((char) xn.intValue());
            case 'Z':
                return Boolean.valueOf(boolValue(xn.byteValue()));
        }
        throw new InternalError("bad wrapper");
    }

    public Object wrap(int x) {
        if (basicTypeChar == 'L')
            return (Integer) x;
        switch(basicTypeChar) {
            case 'L':
                throw newIllegalArgumentException("cannot wrap to object type");
            case 'V':
                return null;
            case 'I':
                return Integer.valueOf(x);
            case 'J':
                return Long.valueOf(x);
            case 'F':
                return Float.valueOf(x);
            case 'D':
                return Double.valueOf(x);
            case 'S':
                return Short.valueOf((short) x);
            case 'B':
                return Byte.valueOf((byte) x);
            case 'C':
                return Character.valueOf((char) x);
            case 'Z':
                return Boolean.valueOf(boolValue((byte) x));
        }
        throw new InternalError("bad wrapper");
    }

    private static Number numberValue(Object x) {
        if (x instanceof Number)
            return (Number) x;
        if (x instanceof Character)
            return (int) (Character) x;
        if (x instanceof Boolean)
            return (Boolean) x ? 1 : 0;
        return (Number) x;
    }

    private static boolean boolValue(byte bits) {
        bits &= 1;
        return (bits != 0);
    }

    private static RuntimeException newIllegalArgumentException(String message, Object x) {
        return newIllegalArgumentException(message + x);
    }

    private static RuntimeException newIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    public Object makeArray(int len) {
        return java.lang.reflect.Array.newInstance(primitiveType, len);
    }

    public Class<?> arrayType() {
        return emptyArray.getClass();
    }

    public void copyArrayUnboxing(Object[] values, int vpos, Object a, int apos, int length) {
        if (a.getClass() != arrayType())
            arrayType().cast(a);
        for (int i = 0; i < length; i++) {
            Object value = values[i + vpos];
            value = convert(value, primitiveType);
            java.lang.reflect.Array.set(a, i + apos, value);
        }
    }

    public void copyArrayBoxing(Object a, int apos, Object[] values, int vpos, int length) {
        if (a.getClass() != arrayType())
            arrayType().cast(a);
        for (int i = 0; i < length; i++) {
            Object value = java.lang.reflect.Array.get(a, i + apos);
            assert (value.getClass() == wrapperType);
            values[i + vpos] = value;
        }
    }
}
