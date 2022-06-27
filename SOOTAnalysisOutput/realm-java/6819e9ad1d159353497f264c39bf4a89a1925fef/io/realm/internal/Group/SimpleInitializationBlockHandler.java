package io.realm.internal;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Group implements Closeable {

    public static final int MODE_READONLY = 0;

    public static final int MODE_READWRITE = 1;

    public static final int MODE_READWRITE_NOCREATE = 2;

    protected long nativePtr;

    protected boolean immutable;

    private final Context context;

    static {
        RealmCore.loadLibrary();
    }

    private void checkNativePtrNotZero() {
        if (this.nativePtr == 0)
            throw new OutOfMemoryError("Out of native memory.");
    }

    public Group() {
        this.immutable = false;
        this.context = new Context();
        this.nativePtr = createNative();
        checkNativePtrNotZero();
    }

    public Group(String filepath, int mode) {
        this.immutable = (mode == MODE_READONLY);
        this.context = new Context();
        this.nativePtr = createNative(filepath, mode);
        checkNativePtrNotZero();
    }

    public Group(String filepath) {
        this(filepath, MODE_READONLY);
    }

    public Group(File file) {
        this(file.getAbsolutePath(), file.canWrite() ? MODE_READWRITE : MODE_READONLY);
    }

    public Group(byte[] data) {
        this.immutable = false;
        this.context = new Context();
        if (data != null) {
            this.nativePtr = createNative(data);
            checkNativePtrNotZero();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Group(ByteBuffer buffer) {
        this.immutable = false;
        this.context = new Context();
        if (buffer != null) {
            this.nativePtr = createNative(buffer);
            checkNativePtrNotZero();
        } else {
            throw new IllegalArgumentException();
        }
    }

    Group(Context context, long nativePointer, boolean immutable) {
        this.context = context;
        this.nativePtr = nativePointer;
        this.immutable = immutable;
    }

    public void close() {
        synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                nativePtr = 0;
            }
        }
    }

    boolean isClosed() {
        return nativePtr == 0;
    }

    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeGroup(nativePtr);
                nativePtr = 0;
            }
        }
    }

    private void verifyGroupIsValid() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Illegal to call methods on a closed Group.");
        }
    }

    public long size() {
        verifyGroupIsValid();
        return nativeSize(nativePtr);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean hasTable(String name) {
        verifyGroupIsValid();
        return name != null && nativeHasTable(nativePtr, name);
    }

    public String getTableName(int index) {
        verifyGroupIsValid();
        long cnt = size();
        if (index < 0 || index >= cnt) {
            throw new IndexOutOfBoundsException("Table index argument is out of range. possible range is [0, " + (cnt - 1) + "]");
        }
        return nativeGetTableName(nativePtr, index);
    }

    public void removeTable(String name) {
        nativeRemoveTable(nativePtr, name);
    }

    native void nativeRemoveTable(long nativeGroupPtr, String tableName);

    public void renameTable(String oldName, String newName) {
        nativeRenameTable(nativePtr, oldName, newName);
    }

    native void nativeRenameTable(long nativeGroupPtr, String oldName, String newName);

    public Table getTable(String name) {
        verifyGroupIsValid();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Invalid name. Name must be a non-empty String.");
        }
        if (immutable && !hasTable(name)) {
                throw new IllegalStateException("Requested table is not in this Realm. " + "Creating it requires a transaction: " + name);
            }
        context.executeDelayedDisposal();
        long nativeTablePointer = nativeGetTableNativePtr(nativePtr, name);
        try {
            return new Table(context, this, nativeTablePointer);
        } catch (RuntimeException e) {
            Table.nativeClose(nativeTablePointer);
            throw e;
        }
    }

    public void writeToFile(File file, byte[] key) throws IOException {
        verifyGroupIsValid();
        if (file.isFile() && file.exists()) {
            throw new IllegalArgumentException("The destination file must not exist");
        }
        if (key != null && key.length != 64) {
            throw new IllegalArgumentException("Realm AES keys must be 64 bytes long");
        }
        nativeWriteToFile(nativePtr, file.getAbsolutePath(), key);
    }

    public byte[] writeToMem() {
        verifyGroupIsValid();
        return nativeWriteToMem(nativePtr);
    }

    public boolean isObjectTablesEmpty() {
        return nativeIsEmpty(nativePtr);
    }

    public void commit() {
        verifyGroupIsValid();
        nativeCommit(nativePtr);
    }

    public String toJson() {
        return nativeToJson(nativePtr);
    }

    public String toString() {
        return nativeToString(nativePtr);
    }

    protected native long createNative();

    protected native long createNative(String filepath, int value);

    protected native long createNative(byte[] data);

    protected native long createNative(ByteBuffer buffer);

    protected static native void nativeClose(long nativeGroupPtr);

    protected native long nativeSize(long nativeGroupPtr);

    protected native String nativeGetTableName(long nativeGroupPtr, int index);

    protected native boolean nativeHasTable(long nativeGroupPtr, String name);

    protected native void nativeWriteToFile(long nativeGroupPtr, String fileName, byte[] keyArray) throws IOException;

    protected native long nativeGetTableNativePtr(long nativeGroupPtr, String name);

    protected native long nativeLoadFromMem(byte[] buffer);

    protected native byte[] nativeWriteToMem(long nativeGroupPtr);

    protected native String nativeToJson(long nativeGroupPtr);

    protected native void nativeCommit(long nativeGroupPtr);

    protected native String nativeToString(long nativeGroupPtr);

    protected native boolean nativeIsEmpty(long nativeGroupPtr);
}