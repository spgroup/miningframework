package org.robolectric.nativeruntime;

import android.database.CharArrayBuffer;
import android.database.CursorWindow;
import org.robolectric.pluginapi.NativeRuntimeLoader;
import org.robolectric.util.inject.Injector;

public final class CursorWindowNatives {

    static {
        Injector injector = new Injector.Builder(CursorWindow.class.getClassLoader()).build();
        NativeRuntimeLoader loader = injector.getInstance(NativeRuntimeLoader.class);
        loader.ensureLoaded();
    }

    private CursorWindowNatives() {
    }

    public static native long nativeCreate(String name, int cursorWindowSize);

    public static native void nativeDispose(long windowPtr);

    public static native String nativeGetName(long windowPtr);

    public static native byte[] nativeGetBlob(long windowPtr, int row, int column);

    public static native String nativeGetString(long windowPtr, int row, int column);

    public static native void nativeCopyStringToBuffer(long windowPtr, int row, int column, CharArrayBuffer buffer);

    public static native boolean nativePutBlob(long windowPtr, byte[] value, int row, int column);

    public static native boolean nativePutString(long windowPtr, String value, int row, int column);

    public static native void nativeClear(long windowPtr);

    public static native int nativeGetNumRows(long windowPtr);

    public static native boolean nativeSetNumColumns(long windowPtr, int columnNum);

    public static native boolean nativeAllocRow(long windowPtr);

    public static native void nativeFreeLastRow(long windowPtr);

    public static native int nativeGetType(long windowPtr, int row, int column);

    public static native long nativeGetLong(long windowPtr, int row, int column);

    public static native double nativeGetDouble(long windowPtr, int row, int column);

    public static native boolean nativePutLong(long windowPtr, long value, int row, int column);

    public static native boolean nativePutDouble(long windowPtr, double value, int row, int column);

    public static native boolean nativePutNull(long windowPtr, int row, int column);
}
