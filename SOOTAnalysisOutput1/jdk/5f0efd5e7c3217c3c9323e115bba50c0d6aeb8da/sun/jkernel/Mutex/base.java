package sun.jkernel;

public class Mutex {

    static {
        try {
            System.loadLibrary("jkernel");
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private String uniqueId;

    private long handle;

    public static Mutex create(String uniqueId) {
        return new Mutex(uniqueId);
    }

    private Mutex(String uniqueId) {
        this.uniqueId = uniqueId;
        this.handle = createNativeMutex(uniqueId);
    }

    private static native long createNativeMutex(String uniqueId);

    public native void acquire();

    public native boolean acquire(int timeout);

    public native void release();

    public native void destroyNativeMutex();

    public void dispose() {
        destroyNativeMutex();
        handle = 0;
    }

    public void finalize() {
        dispose();
    }

    public String toString() {
        return "Mutex[" + uniqueId + "]";
    }
}
