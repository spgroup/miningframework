public class TestCSLocker extends Thread {

    static int timeout = 5000;

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        GarbageProducer garbageProducer = new GarbageProducer(1000000, 10);
        garbageProducer.start();
        CSLocker csLocker = new CSLocker();
        csLocker.start();
        while (System.currentTimeMillis() < startTime + timeout) {
            System.out.println("sleeping...");
            sleep(1000);
        }
        csLocker.unlock();
        garbageProducer.interrupt();
    }
}

class GarbageProducer extends Thread {

    private int size;

    private int sleepTime;

    GarbageProducer(int size, int sleepTime) {
        this.size = size;
        this.sleepTime = sleepTime;
    }

    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            try {
                int[] arr = null;
                arr = new int[size];
                sleep(sleepTime);
            } catch (InterruptedException e) {
                isRunning = false;
            }
        }
    }
}

class CSLocker extends Thread {

    static {
        System.loadLibrary("TestCSLocker");
    }

    public void run() {
        int[] a = new int[10];
        a[0] = 1;
        if (!lock(a)) {
            throw new RuntimeException("failed to acquire CSLock");
        }
    }

    native boolean lock(int[] array);

    native void unlock();
}
