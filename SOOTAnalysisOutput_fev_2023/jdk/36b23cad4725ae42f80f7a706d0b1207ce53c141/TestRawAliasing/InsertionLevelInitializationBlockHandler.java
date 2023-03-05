package compiler.unsafe;

import java.lang.reflect.Field;

public class TestRawAliasing {

    static private final jdk.internal.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = jdk.internal.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (jdk.internal.misc.Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Unsafe instance.", e);
        }
    }

    static private final int OFFSET_X = 50;

    static private final int OFFSET_Y = 100;

    private static int test(long base_plus_offset_x, long base_plus_offset_y, int magic_value) {
        UNSAFE.putByte(base_plus_offset_x - OFFSET_X, (byte) 0);
        UNSAFE.putByte(base_plus_offset_y - OFFSET_Y, (byte) magic_value);
        return UNSAFE.getByte(base_plus_offset_x - OFFSET_X);
    }

    private static final int OFF_HEAP_AREA_SIZE = 128;

    private static final byte MAGIC = 123;

    public static void main(String... args) {
        long base = UNSAFE.allocateMemory(OFF_HEAP_AREA_SIZE);
        for (int i = 0; i < 100_000; i++) {
            if (test(base + OFFSET_X, base + OFFSET_Y, MAGIC) != MAGIC) {
                throw new RuntimeException("Unexpected magic value");
            }
        }
    }
}