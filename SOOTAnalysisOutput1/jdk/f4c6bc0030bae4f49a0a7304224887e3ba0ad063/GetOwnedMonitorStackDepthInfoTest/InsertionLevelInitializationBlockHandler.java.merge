public class GetOwnedMonitorStackDepthInfoTest {

    static {
        try {
            System.loadLibrary("GetOwnedMonitorStackDepthInfoTest");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Could not load GetOwnedMonitorStackDepthInfoTest library");
            System.err.println("java.library.path: " + System.getProperty("java.library.path"));
            throw ule;
        }
    }

    private static native int verifyOwnedMonitors();

    private static volatile int results = -1;

    public static void main(String[] args) throws Exception {
        new GetOwnedMonitorStackDepthInfoTest().runTest();
    }

    public void runTest() throws Exception {
        final Object lock1 = new Lock1();
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread in sync section 1: " + Thread.currentThread().getName());
                test1();
            }
        });
        t1.start();
        t1.join();
        if (results != 0) {
            throw new RuntimeException("FAILED status returned from the agent");
        }
    }

    private synchronized void test1() {
        test2();
    }

    private void test2() {
        Object lock2 = new Lock2();
        synchronized (lock2) {
            System.out.println("Thread in sync section 2: " + Thread.currentThread().getName());
            results = verifyOwnedMonitors();
        }
    }

    private static class Lock1 {
    }

    private static class Lock2 {
    }
}