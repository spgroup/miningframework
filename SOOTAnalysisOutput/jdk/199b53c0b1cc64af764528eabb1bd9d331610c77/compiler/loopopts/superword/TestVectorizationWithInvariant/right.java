package compiler.loopopts.superword;

import jdk.internal.misc.Unsafe;
import jdk.test.lib.unsafe.UnsafeHelper;

public class TestVectorizationWithInvariant {

    private static Unsafe unsafe;

    private static final long BYTE_ARRAY_OFFSET;

    private static final long CHAR_ARRAY_OFFSET;

    static {
        unsafe = UnsafeHelper.getUnsafe();
        BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        CHAR_ARRAY_OFFSET = unsafe.arrayBaseOffset(char[].class);
    }

    public static void main(String[] args) throws Exception {
        byte[] byte_array1 = new byte[1000];
        byte[] byte_array2 = new byte[1000];
        char[] char_array = new char[1000];
        for (int i = 0; i < 20_000; ++i) {
            copyByteToChar(byte_array1, byte_array2, char_array, 1);
            copyCharToByte(char_array, byte_array1, 1);
            copyCharToByteAligned(char_array, byte_array1);
            copyCharToByteUnaligned(char_array, byte_array1);
        }
    }

    public static void copyByteToChar(byte[] src1, byte[] src2, char[] dst, int off) {
        off = (int) BYTE_ARRAY_OFFSET + (off << 1);
        byte[] src = src1;
        for (int i = (int) CHAR_ARRAY_OFFSET; i < 100; i = i + 8) {
            unsafe.putChar(dst, i + 0, unsafe.getChar(src, off + 0));
            unsafe.putChar(dst, i + 2, unsafe.getChar(src, off + 2));
            unsafe.putChar(dst, i + 4, unsafe.getChar(src, off + 4));
            unsafe.putChar(dst, i + 6, unsafe.getChar(src, off + 6));
            unsafe.putChar(dst, i + 8, unsafe.getChar(src, off + 8));
            unsafe.putChar(dst, i + 10, unsafe.getChar(src, off + 10));
            unsafe.putChar(dst, i + 12, unsafe.getChar(src, off + 12));
            unsafe.putChar(dst, i + 14, unsafe.getChar(src, off + 14));
            src = (src == src1) ? src2 : src1;
        }
    }

    public static void copyCharToByte(char[] src, byte[] dst, int off) {
        off = (int) BYTE_ARRAY_OFFSET + (off << 1);
        for (int i = 0; i < 100; i = i + 8) {
            unsafe.putChar(dst, off + 0, src[i + 0]);
            unsafe.putChar(dst, off + 2, src[i + 1]);
            unsafe.putChar(dst, off + 4, src[i + 2]);
            unsafe.putChar(dst, off + 6, src[i + 3]);
            unsafe.putChar(dst, off + 8, src[i + 4]);
            unsafe.putChar(dst, off + 10, src[i + 5]);
            unsafe.putChar(dst, off + 12, src[i + 6]);
            unsafe.putChar(dst, off + 14, src[i + 7]);
        }
    }

    public static void copyCharToByteAligned(char[] src, byte[] dst) {
        final int off = (int) BYTE_ARRAY_OFFSET;
        for (int i = 8; i < 100; i = i + 8) {
            unsafe.putChar(dst, off + 0, src[i + 0]);
            unsafe.putChar(dst, off + 2, src[i + 1]);
            unsafe.putChar(dst, off + 4, src[i + 2]);
            unsafe.putChar(dst, off + 6, src[i + 3]);
            unsafe.putChar(dst, off + 8, src[i + 4]);
            unsafe.putChar(dst, off + 10, src[i + 5]);
            unsafe.putChar(dst, off + 12, src[i + 6]);
            unsafe.putChar(dst, off + 14, src[i + 7]);
        }
    }

    public static void copyCharToByteUnaligned(char[] src, byte[] dst) {
        final int off = (int) BYTE_ARRAY_OFFSET + 2;
        for (int i = 0; i < 100; i = i + 8) {
            unsafe.putChar(dst, off + 0, src[i + 0]);
            unsafe.putChar(dst, off + 2, src[i + 1]);
            unsafe.putChar(dst, off + 4, src[i + 2]);
            unsafe.putChar(dst, off + 6, src[i + 3]);
            unsafe.putChar(dst, off + 8, src[i + 4]);
            unsafe.putChar(dst, off + 10, src[i + 5]);
            unsafe.putChar(dst, off + 12, src[i + 6]);
            unsafe.putChar(dst, off + 14, src[i + 7]);
        }
    }
}
