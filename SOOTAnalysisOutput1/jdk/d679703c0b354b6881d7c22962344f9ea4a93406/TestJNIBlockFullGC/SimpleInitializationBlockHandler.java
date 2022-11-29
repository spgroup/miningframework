import java.lang.ref.SoftReference;

public class TestJNIBlockFullGC {

    static {
        System.loadLibrary("TestJNIBlockFullGC");
    }

    public static volatile Object tmp;

    public static volatile boolean hadError = false;

    private static native int TestCriticalArray0(int[] x);

    public static class Node {

        public SoftReference<Node> next;

        long payload1;

        long payload2;

        long payload3;

        long payload4;

        public Node(int load) {
            payload1 = payload2 = payload3 = payload4 = load;
        }
    }

    public static void warmUp(long warmupEndTime, int size) {
        Node[] roots = new Node[size];
        while (System.currentTimeMillis() < warmupEndTime) {
            int index = (int) (Math.random() * roots.length);
            roots[index] = new Node(1);
        }
        for (int i = 0; i < roots.length; ++i) {
            roots[i] = null;
        }
    }

    public static void runTest(long endTime, int size, double alive) {
        final int length = 10000;
        int[] array1 = new int[length];
        for (int x = 1; x < length; x++) {
            array1[x] = x;
        }
        Node[] roots = new Node[size];
        try {
            int index = 0;
            roots[0] = new Node(0);
            while (!hadError && (System.currentTimeMillis() < endTime)) {
                int test_val1 = TestCriticalArray0(array1);
                if (Math.random() > alive) {
                    tmp = new Node(test_val1);
                } else {
                    index = (int) (Math.random() * roots.length);
                    if (roots[index] != null) {
                        Node node = new Node(test_val1);
                        node.next = new SoftReference<Node>(roots[index]);
                        roots[index] = node;
                    } else {
                        roots[index] = new Node(test_val1);
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            hadError = true;
            e.printStackTrace();
        }
    }

    private static void joinThreads(Thread[] threads) throws Exception {
        for (int i = 0; i < threads.length; i++) {
            try {
                if (threads[i] != null) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            System.out.println("Usage: java TestJNIBlockFullGC <warmupThreads> <warmup-time-in-millis> <warmup iterations> <threads> <time-in-millis> <iterations> <aliveFrac>");
            System.exit(0);
        }
        int warmupThreads = Integer.parseInt(args[0]);
        System.out.println("# Warmup Threads = " + warmupThreads);
        int warmupDuration = Integer.parseInt(args[1]);
        System.out.println("WarmUp Duration = " + warmupDuration);
        int warmupIterations = Integer.parseInt(args[2]);
        System.out.println("# Warmup Iterations = " + warmupIterations);
        int mainThreads = Integer.parseInt(args[3]);
        System.out.println("# Main Threads = " + mainThreads);
        int mainDuration = Integer.parseInt(args[4]);
        System.out.println("Main Duration = " + mainDuration);
        int mainIterations = Integer.parseInt(args[5]);
        System.out.println("# Main Iterations = " + mainIterations);
        double liveFrac = Double.parseDouble(args[6]);
        System.out.println("Live Fraction = " + liveFrac);
        Thread[] threads = new Thread[Math.max(warmupThreads, mainThreads)];
        System.out.println("Start warm-up threads!");
        long warmupStartTime = System.currentTimeMillis();
        for (int i = 0; i < warmupThreads; i++) {
            threads[i] = new Thread() {

                public void run() {
                    warmUp(warmupStartTime + warmupDuration, warmupIterations);
                }
            };
            threads[i].start();
        }
        joinThreads(threads);
        System.gc();
        System.out.println("Keep alive a lot");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < mainThreads; i++) {
            threads[i] = new Thread() {

                public void run() {
                    runTest(startTime + mainDuration, mainIterations, liveFrac);
                }
            };
            threads[i].start();
        }
        System.out.println("All threads started");
        joinThreads(threads);
        if (hadError) {
            throw new RuntimeException("Experienced an OoME during execution.");
        }
    }
}