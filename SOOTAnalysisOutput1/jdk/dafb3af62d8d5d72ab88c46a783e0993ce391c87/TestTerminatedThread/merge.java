import java.lang.management.*;

public class TestTerminatedThread {

    static native Thread createTerminatedThread();

    static {
        System.loadLibrary("terminatedThread");
    }

    private static ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) throws Throwable {
        Thread t = createTerminatedThread();
        if (!t.isAlive())
            throw new Error("Thread is only supposed to terminate at native layer!");
        System.out.println("Working with thread: " + t + ",  in state: " + t.getState());
        System.out.println("Calling suspend ...");
        t.suspend();
        System.out.println("Calling resume ...");
        t.resume();
        System.out.println("Calling getStackTrace ...");
        StackTraceElement[] stack = t.getStackTrace();
        System.out.println(java.util.Arrays.toString(stack));
        if (stack.length != 0)
            throw new Error("Terminated thread should have empty java stack trace");
        System.out.println("Calling setName(\"NewName\") ...");
        t.setName("NewName");
        System.out.println("Calling interrupt ...");
        t.interrupt();
        System.out.println("Calling stop ...");
        t.stop();
        if (mbean.isThreadCpuTimeSupported() && mbean.isThreadCpuTimeEnabled()) {
            System.out.println("Calling getThreadCpuTime ...");
            long t1 = mbean.getThreadCpuTime(t.getId());
            if (t1 != -1) {
                throw new RuntimeException("Invalid ThreadCpuTime returned = " + t1 + " expected = -1");
            }
            System.out.println("Okay: getThreadCpuTime() reported -1 as expected");
        } else {
            System.out.println("Skipping Thread CPU time test as it's not supported");
        }
        System.out.println("Calling getThreadUserTime ...");
        long t1 = mbean.getThreadUserTime(t.getId());
        if (t1 != -1) {
            throw new RuntimeException("Invalid ThreadUserTime returned = " + t1 + " expected = -1");
        }
        System.out.println("Okay: getThreadUserTime() reported -1 as expected");
        System.out.println("Calling getThreadInfo ...");
        ThreadInfo info = mbean.getThreadInfo(t.getId());
        System.out.println(info);
        System.out.println("Calling getThreadInfo with stack ...");
        info = mbean.getThreadInfo(t.getId(), Integer.MAX_VALUE);
        System.out.println(info);
    }
}
