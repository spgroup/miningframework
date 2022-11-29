import java.io.PrintStream;

public class GetOwnedMonitorInfoTest {

    static {
        try {
            System.loadLibrary("GetOwnedMonitorInfoTest");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Could not load GetOwnedMonitorInfoTest library");
            System.err.println("java.library.path: " + System.getProperty("java.library.path"));
            throw ule;
        }
    }

    private static native int check();

    private static native boolean hasEventPosted();

    public static void main(String[] args) throws Exception {
        final GetOwnedMonitorInfoTest lock = new GetOwnedMonitorInfoTest();
        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Thread in sync section: " + Thread.currentThread().getName());
            }
        });
        synchronized (lock) {
            System.out.println("Main starting worker thread.");
            t1.start();
            while (!hasEventPosted()) {
                System.out.println("Main waiting for event.");
                Thread.sleep(100);
            }
        }
        t1.join();
        if (check() != 0) {
            throw new RuntimeException("FAILED status returned from the agent");
        }
    }
}