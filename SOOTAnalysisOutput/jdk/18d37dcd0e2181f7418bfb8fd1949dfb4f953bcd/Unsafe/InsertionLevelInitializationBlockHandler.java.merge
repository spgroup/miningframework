package jdk.internal.misc;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.misc.VM;
import jdk.internal.HotSpotIntrinsicCandidate;

public final class Unsafe {

    private static native void registerNatives();

    static {
        registerNatives();
        sun.reflect.Reflection.registerMethodsToFilter(Unsafe.class, "getUnsafe");
    }

    private Unsafe() {
    }

    private static final Unsafe theUnsafe = new Unsafe();

    @CallerSensitive
    public static Unsafe getUnsafe() {
        Class<?> caller = Reflection.getCallerClass();
        if (!VM.isSystemDomainLoader(caller.getClassLoader()))
            throw new SecurityException("Unsafe");
        return theUnsafe;
    }

    @HotSpotIntrinsicCandidate
    public native int getInt(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putInt(Object o, long offset, int x);

    @HotSpotIntrinsicCandidate
    public native Object getObject(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putObject(Object o, long offset, Object x);

    @HotSpotIntrinsicCandidate
    public native boolean getBoolean(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putBoolean(Object o, long offset, boolean x);

    @HotSpotIntrinsicCandidate
    public native byte getByte(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putByte(Object o, long offset, byte x);

    @HotSpotIntrinsicCandidate
    public native short getShort(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putShort(Object o, long offset, short x);

    @HotSpotIntrinsicCandidate
    public native char getChar(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putChar(Object o, long offset, char x);

    @HotSpotIntrinsicCandidate
    public native long getLong(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putLong(Object o, long offset, long x);

    @HotSpotIntrinsicCandidate
    public native float getFloat(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putFloat(Object o, long offset, float x);

    @HotSpotIntrinsicCandidate
    public native double getDouble(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putDouble(Object o, long offset, double x);

    public native Object getUncompressedObject(long address);

    public native Class<?> getJavaMirror(long metaspaceKlass);

    public native long getKlassPointer(Object o);

    @HotSpotIntrinsicCandidate
    public native byte getByte(long address);

    @HotSpotIntrinsicCandidate
    public native void putByte(long address, byte x);

    @HotSpotIntrinsicCandidate
    public native short getShort(long address);

    @HotSpotIntrinsicCandidate
    public native void putShort(long address, short x);

    @HotSpotIntrinsicCandidate
    public native char getChar(long address);

    @HotSpotIntrinsicCandidate
    public native void putChar(long address, char x);

    @HotSpotIntrinsicCandidate
    public native int getInt(long address);

    @HotSpotIntrinsicCandidate
    public native void putInt(long address, int x);

    @HotSpotIntrinsicCandidate
    public native long getLong(long address);

    @HotSpotIntrinsicCandidate
    public native void putLong(long address, long x);

    @HotSpotIntrinsicCandidate
    public native float getFloat(long address);

    @HotSpotIntrinsicCandidate
    public native void putFloat(long address, float x);

    @HotSpotIntrinsicCandidate
    public native double getDouble(long address);

    @HotSpotIntrinsicCandidate
    public native void putDouble(long address, double x);

    @HotSpotIntrinsicCandidate
    public native long getAddress(long address);

    @HotSpotIntrinsicCandidate
    public native void putAddress(long address, long x);

    public native long allocateMemory(long bytes);

    public native long reallocateMemory(long address, long bytes);

    public native void setMemory(Object o, long offset, long bytes, byte value);

    public void setMemory(long address, long bytes, byte value) {
        setMemory(null, address, bytes, value);
    }

    @HotSpotIntrinsicCandidate
    public native void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes);

    public void copyMemory(long srcAddress, long destAddress, long bytes) {
        copyMemory(null, srcAddress, null, destAddress, bytes);
    }

    public native void freeMemory(long address);

    public static final int INVALID_FIELD_OFFSET = -1;

    public native long objectFieldOffset(Field f);

    public native long staticFieldOffset(Field f);

    public native Object staticFieldBase(Field f);

    public native boolean shouldBeInitialized(Class<?> c);

    public native void ensureClassInitialized(Class<?> c);

    public native int arrayBaseOffset(Class<?> arrayClass);

    public static final int ARRAY_BOOLEAN_BASE_OFFSET = theUnsafe.arrayBaseOffset(boolean[].class);

    public static final int ARRAY_BYTE_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);

    public static final int ARRAY_SHORT_BASE_OFFSET = theUnsafe.arrayBaseOffset(short[].class);

    public static final int ARRAY_CHAR_BASE_OFFSET = theUnsafe.arrayBaseOffset(char[].class);

    public static final int ARRAY_INT_BASE_OFFSET = theUnsafe.arrayBaseOffset(int[].class);

    public static final int ARRAY_LONG_BASE_OFFSET = theUnsafe.arrayBaseOffset(long[].class);

    public static final int ARRAY_FLOAT_BASE_OFFSET = theUnsafe.arrayBaseOffset(float[].class);

    public static final int ARRAY_DOUBLE_BASE_OFFSET = theUnsafe.arrayBaseOffset(double[].class);

    public static final int ARRAY_OBJECT_BASE_OFFSET = theUnsafe.arrayBaseOffset(Object[].class);

    public native int arrayIndexScale(Class<?> arrayClass);

    public static final int ARRAY_BOOLEAN_INDEX_SCALE = theUnsafe.arrayIndexScale(boolean[].class);

    public static final int ARRAY_BYTE_INDEX_SCALE = theUnsafe.arrayIndexScale(byte[].class);

    public static final int ARRAY_SHORT_INDEX_SCALE = theUnsafe.arrayIndexScale(short[].class);

    public static final int ARRAY_CHAR_INDEX_SCALE = theUnsafe.arrayIndexScale(char[].class);

    public static final int ARRAY_INT_INDEX_SCALE = theUnsafe.arrayIndexScale(int[].class);

    public static final int ARRAY_LONG_INDEX_SCALE = theUnsafe.arrayIndexScale(long[].class);

    public static final int ARRAY_FLOAT_INDEX_SCALE = theUnsafe.arrayIndexScale(float[].class);

    public static final int ARRAY_DOUBLE_INDEX_SCALE = theUnsafe.arrayIndexScale(double[].class);

    public static final int ARRAY_OBJECT_INDEX_SCALE = theUnsafe.arrayIndexScale(Object[].class);

    public native int addressSize();

    public static final int ADDRESS_SIZE = theUnsafe.addressSize();

    public native int pageSize();

    public native Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);

    public native Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches);

    @HotSpotIntrinsicCandidate
    public native Object allocateInstance(Class<?> cls) throws InstantiationException;

    public native void throwException(Throwable ee);

    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSwapObject(Object o, long offset, Object expected, Object x);

    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSwapInt(Object o, long offset, int expected, int x);

    @HotSpotIntrinsicCandidate
    public final native boolean compareAndSwapLong(Object o, long offset, long expected, long x);

    @HotSpotIntrinsicCandidate
    public native Object getObjectVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putObjectVolatile(Object o, long offset, Object x);

    @HotSpotIntrinsicCandidate
    public native int getIntVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putIntVolatile(Object o, long offset, int x);

    @HotSpotIntrinsicCandidate
    public native boolean getBooleanVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putBooleanVolatile(Object o, long offset, boolean x);

    @HotSpotIntrinsicCandidate
    public native byte getByteVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putByteVolatile(Object o, long offset, byte x);

    @HotSpotIntrinsicCandidate
    public native short getShortVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putShortVolatile(Object o, long offset, short x);

    @HotSpotIntrinsicCandidate
    public native char getCharVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putCharVolatile(Object o, long offset, char x);

    @HotSpotIntrinsicCandidate
    public native long getLongVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putLongVolatile(Object o, long offset, long x);

    @HotSpotIntrinsicCandidate
    public native float getFloatVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putFloatVolatile(Object o, long offset, float x);

    @HotSpotIntrinsicCandidate
    public native double getDoubleVolatile(Object o, long offset);

    @HotSpotIntrinsicCandidate
    public native void putDoubleVolatile(Object o, long offset, double x);

    @HotSpotIntrinsicCandidate
    public native void putOrderedObject(Object o, long offset, Object x);

    @HotSpotIntrinsicCandidate
    public native void putOrderedInt(Object o, long offset, int x);

    @HotSpotIntrinsicCandidate
    public native void putOrderedLong(Object o, long offset, long x);

    @HotSpotIntrinsicCandidate
    public native void unpark(Object thread);

    @HotSpotIntrinsicCandidate
    public native void park(boolean isAbsolute, long time);

    public native int getLoadAverage(double[] loadavg, int nelems);

    @HotSpotIntrinsicCandidate
    public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, v + delta));
        return v;
    }

    @HotSpotIntrinsicCandidate
    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, v + delta));
        return v;
    }

    @HotSpotIntrinsicCandidate
    public final int getAndSetInt(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, newValue));
        return v;
    }

    @HotSpotIntrinsicCandidate
    public final long getAndSetLong(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, newValue));
        return v;
    }

    @HotSpotIntrinsicCandidate
    public final Object getAndSetObject(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectVolatile(o, offset);
        } while (!compareAndSwapObject(o, offset, v, newValue));
        return v;
    }

    @HotSpotIntrinsicCandidate
    public native void loadFence();

    @HotSpotIntrinsicCandidate
    public native void storeFence();

    @HotSpotIntrinsicCandidate
    public native void fullFence();

    private static void throwIllegalAccessError() {
        throw new IllegalAccessError();
    }

    public final boolean isBigEndian() {
        return BE;
    }

    public final boolean unalignedAccess() {
        return unalignedAccess;
    }

    @HotSpotIntrinsicCandidate
    public final long getLongUnaligned(Object o, long offset) {
        if ((offset & 7) == 0) {
            return getLong(o, offset);
        } else if ((offset & 3) == 0) {
            return makeLong(getInt(o, offset), getInt(o, offset + 4));
        } else if ((offset & 1) == 0) {
            return makeLong(getShort(o, offset), getShort(o, offset + 2), getShort(o, offset + 4), getShort(o, offset + 6));
        } else {
            return makeLong(getByte(o, offset), getByte(o, offset + 1), getByte(o, offset + 2), getByte(o, offset + 3), getByte(o, offset + 4), getByte(o, offset + 5), getByte(o, offset + 6), getByte(o, offset + 7));
        }
    }

    public final long getLongUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getLongUnaligned(o, offset));
    }

    @HotSpotIntrinsicCandidate
    public final int getIntUnaligned(Object o, long offset) {
        if ((offset & 3) == 0) {
            return getInt(o, offset);
        } else if ((offset & 1) == 0) {
            return makeInt(getShort(o, offset), getShort(o, offset + 2));
        } else {
            return makeInt(getByte(o, offset), getByte(o, offset + 1), getByte(o, offset + 2), getByte(o, offset + 3));
        }
    }

    public final int getIntUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getIntUnaligned(o, offset));
    }

    @HotSpotIntrinsicCandidate
    public final short getShortUnaligned(Object o, long offset) {
        if ((offset & 1) == 0) {
            return getShort(o, offset);
        } else {
            return makeShort(getByte(o, offset), getByte(o, offset + 1));
        }
    }

    public final short getShortUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getShortUnaligned(o, offset));
    }

    @HotSpotIntrinsicCandidate
    public final char getCharUnaligned(Object o, long offset) {
        return (char) getShortUnaligned(o, offset);
    }

    public final char getCharUnaligned(Object o, long offset, boolean bigEndian) {
        return convEndian(bigEndian, getCharUnaligned(o, offset));
    }

    @HotSpotIntrinsicCandidate
    public final void putLongUnaligned(Object o, long offset, long x) {
        if ((offset & 7) == 0) {
            putLong(o, offset, x);
        } else if ((offset & 3) == 0) {
            putLongParts(o, offset, (int) (x >> 0), (int) (x >>> 32));
        } else if ((offset & 1) == 0) {
            putLongParts(o, offset, (short) (x >>> 0), (short) (x >>> 16), (short) (x >>> 32), (short) (x >>> 48));
        } else {
            putLongParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24), (byte) (x >>> 32), (byte) (x >>> 40), (byte) (x >>> 48), (byte) (x >>> 56));
        }
    }

    public final void putLongUnaligned(Object o, long offset, long x, boolean bigEndian) {
        putLongUnaligned(o, offset, convEndian(bigEndian, x));
    }

    @HotSpotIntrinsicCandidate
    public final void putIntUnaligned(Object o, long offset, int x) {
        if ((offset & 3) == 0) {
            putInt(o, offset, x);
        } else if ((offset & 1) == 0) {
            putIntParts(o, offset, (short) (x >> 0), (short) (x >>> 16));
        } else {
            putIntParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8), (byte) (x >>> 16), (byte) (x >>> 24));
        }
    }

    public final void putIntUnaligned(Object o, long offset, int x, boolean bigEndian) {
        putIntUnaligned(o, offset, convEndian(bigEndian, x));
    }

    @HotSpotIntrinsicCandidate
    public final void putShortUnaligned(Object o, long offset, short x) {
        if ((offset & 1) == 0) {
            putShort(o, offset, x);
        } else {
            putShortParts(o, offset, (byte) (x >>> 0), (byte) (x >>> 8));
        }
    }

    public final void putShortUnaligned(Object o, long offset, short x, boolean bigEndian) {
        putShortUnaligned(o, offset, convEndian(bigEndian, x));
    }

    @HotSpotIntrinsicCandidate
    public final void putCharUnaligned(Object o, long offset, char x) {
        putShortUnaligned(o, offset, (short) x);
    }

    public final void putCharUnaligned(Object o, long offset, char x, boolean bigEndian) {
        putCharUnaligned(o, offset, convEndian(bigEndian, x));
    }

    private native boolean unalignedAccess0();

    private native boolean isBigEndian0();

    private static final boolean BE = theUnsafe.isBigEndian0();

    private static final boolean unalignedAccess = theUnsafe.unalignedAccess0();

    private static int pickPos(int top, int pos) {
        return BE ? top - pos : pos;
    }

    private static long makeLong(byte i0, byte i1, byte i2, byte i3, byte i4, byte i5, byte i6, byte i7) {
        return ((toUnsignedLong(i0) << pickPos(56, 0)) | (toUnsignedLong(i1) << pickPos(56, 8)) | (toUnsignedLong(i2) << pickPos(56, 16)) | (toUnsignedLong(i3) << pickPos(56, 24)) | (toUnsignedLong(i4) << pickPos(56, 32)) | (toUnsignedLong(i5) << pickPos(56, 40)) | (toUnsignedLong(i6) << pickPos(56, 48)) | (toUnsignedLong(i7) << pickPos(56, 56)));
    }

    private static long makeLong(short i0, short i1, short i2, short i3) {
        return ((toUnsignedLong(i0) << pickPos(48, 0)) | (toUnsignedLong(i1) << pickPos(48, 16)) | (toUnsignedLong(i2) << pickPos(48, 32)) | (toUnsignedLong(i3) << pickPos(48, 48)));
    }

    private static long makeLong(int i0, int i1) {
        return (toUnsignedLong(i0) << pickPos(32, 0)) | (toUnsignedLong(i1) << pickPos(32, 32));
    }

    private static int makeInt(short i0, short i1) {
        return (toUnsignedInt(i0) << pickPos(16, 0)) | (toUnsignedInt(i1) << pickPos(16, 16));
    }

    private static int makeInt(byte i0, byte i1, byte i2, byte i3) {
        return ((toUnsignedInt(i0) << pickPos(24, 0)) | (toUnsignedInt(i1) << pickPos(24, 8)) | (toUnsignedInt(i2) << pickPos(24, 16)) | (toUnsignedInt(i3) << pickPos(24, 24)));
    }

    private static short makeShort(byte i0, byte i1) {
        return (short) ((toUnsignedInt(i0) << pickPos(8, 0)) | (toUnsignedInt(i1) << pickPos(8, 8)));
    }

    private static byte pick(byte le, byte be) {
        return BE ? be : le;
    }

    private static short pick(short le, short be) {
        return BE ? be : le;
    }

    private static int pick(int le, int be) {
        return BE ? be : le;
    }

    private void putLongParts(Object o, long offset, byte i0, byte i1, byte i2, byte i3, byte i4, byte i5, byte i6, byte i7) {
        putByte(o, offset + 0, pick(i0, i7));
        putByte(o, offset + 1, pick(i1, i6));
        putByte(o, offset + 2, pick(i2, i5));
        putByte(o, offset + 3, pick(i3, i4));
        putByte(o, offset + 4, pick(i4, i3));
        putByte(o, offset + 5, pick(i5, i2));
        putByte(o, offset + 6, pick(i6, i1));
        putByte(o, offset + 7, pick(i7, i0));
    }

    private void putLongParts(Object o, long offset, short i0, short i1, short i2, short i3) {
        putShort(o, offset + 0, pick(i0, i3));
        putShort(o, offset + 2, pick(i1, i2));
        putShort(o, offset + 4, pick(i2, i1));
        putShort(o, offset + 6, pick(i3, i0));
    }

    private void putLongParts(Object o, long offset, int i0, int i1) {
        putInt(o, offset + 0, pick(i0, i1));
        putInt(o, offset + 4, pick(i1, i0));
    }

    private void putIntParts(Object o, long offset, short i0, short i1) {
        putShort(o, offset + 0, pick(i0, i1));
        putShort(o, offset + 2, pick(i1, i0));
    }

    private void putIntParts(Object o, long offset, byte i0, byte i1, byte i2, byte i3) {
        putByte(o, offset + 0, pick(i0, i3));
        putByte(o, offset + 1, pick(i1, i2));
        putByte(o, offset + 2, pick(i2, i1));
        putByte(o, offset + 3, pick(i3, i0));
    }

    private void putShortParts(Object o, long offset, byte i0, byte i1) {
        putByte(o, offset + 0, pick(i0, i1));
        putByte(o, offset + 1, pick(i1, i0));
    }

    private static int toUnsignedInt(byte n) {
        return n & 0xff;
    }

    private static int toUnsignedInt(short n) {
        return n & 0xffff;
    }

    private static long toUnsignedLong(byte n) {
        return n & 0xffl;
    }

    private static long toUnsignedLong(short n) {
        return n & 0xffffl;
    }

    private static long toUnsignedLong(int n) {
        return n & 0xffffffffl;
    }

    private static char convEndian(boolean big, char n) {
        return big == BE ? n : Character.reverseBytes(n);
    }

    private static short convEndian(boolean big, short n) {
        return big == BE ? n : Short.reverseBytes(n);
    }

    private static int convEndian(boolean big, int n) {
        return big == BE ? n : Integer.reverseBytes(n);
    }

    private static long convEndian(boolean big, long n) {
        return big == BE ? n : Long.reverseBytes(n);
    }
}