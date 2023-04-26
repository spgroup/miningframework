package org.robolectric.nativeruntime;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class SQLiteConnectionNatives {

    static {
        NativeRuntimeLoader.ensureLoaded();
    }

    private SQLiteConnectionNatives() {
    }

    public static native long nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile, int lookasideSlotSize, int lookasideSlotCount);

    public static native void nativeClose(long connectionPtr);

    public static native void nativeRegisterCustomScalarFunction(long connectionPtr, String name, UnaryOperator<String> function);

    public static native void nativeRegisterCustomAggregateFunction(long connectionPtr, String name, BinaryOperator<String> function);

    public static native void nativeRegisterLocalizedCollators(long connectionPtr, String locale);

    public static native long nativePrepareStatement(long connectionPtr, String sql);

    public static native void nativeFinalizeStatement(long connectionPtr, long statementPtr);

    public static native int nativeGetParameterCount(long connectionPtr, long statementPtr);

    public static native boolean nativeIsReadOnly(long connectionPtr, long statementPtr);

    public static native int nativeGetColumnCount(long connectionPtr, long statementPtr);

    public static native String nativeGetColumnName(long connectionPtr, long statementPtr, int index);

    public static native void nativeBindNull(long connectionPtr, long statementPtr, int index);

    public static native void nativeBindLong(long connectionPtr, long statementPtr, int index, long value);

    public static native void nativeBindDouble(long connectionPtr, long statementPtr, int index, double value);

    public static native void nativeBindString(long connectionPtr, long statementPtr, int index, String value);

    public static native void nativeBindBlob(long connectionPtr, long statementPtr, int index, byte[] value);

    public static native void nativeResetStatementAndClearBindings(long connectionPtr, long statementPtr);

    public static native void nativeExecute(long connectionPtr, long statementPtr);

    public static native long nativeExecuteForLong(long connectionPtr, long statementPtr);

    public static native String nativeExecuteForString(long connectionPtr, long statementPtr);

    public static native int nativeExecuteForBlobFileDescriptor(long connectionPtr, long statementPtr);

    public static native int nativeExecuteForChangedRowCount(long connectionPtr, long statementPtr);

    public static native long nativeExecuteForLastInsertedRowId(long connectionPtr, long statementPtr);

    public static native long nativeExecuteForCursorWindow(long connectionPtr, long statementPtr, long windowPtr, int startPos, int requiredPos, boolean countAllRows);

    public static native int nativeGetDbLookaside(long connectionPtr);

    public static native void nativeCancel(long connectionPtr);

    public static native void nativeResetCancel(long connectionPtr, boolean cancelable);
}
