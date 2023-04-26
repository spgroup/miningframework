package compiler.codecache.stress;

import jdk.test.lib.Platform;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

public class OverloadCompileQueueTest implements Runnable {

    private static final int MAX_SLEEP = 10000;

    private static final String METHOD_TO_ENQUEUE = "method";

    private static final int LEVEL_SIMPLE = 1;

    private static final int LEVEL_FULL_OPTIMIZATION = 4;

    private static final boolean TIERED_COMPILATION = Helper.WHITE_BOX.getBooleanVMFlag("TieredCompilation");

    private static final int TIERED_STOP_AT_LEVEL = Helper.WHITE_BOX.getIntxVMFlag("TieredStopAtLevel").intValue();

    private static final int[] AVAILABLE_LEVELS;

    static {
        if (TIERED_COMPILATION) {
            AVAILABLE_LEVELS = IntStream.rangeClosed(LEVEL_SIMPLE, TIERED_STOP_AT_LEVEL).toArray();
        } else if (Platform.isServer() && !Platform.isEmulatedClient()) {
            AVAILABLE_LEVELS = new int[] { LEVEL_FULL_OPTIMIZATION };
        } else if (Platform.isClient() || Platform.isMinimal() || Platform.isEmulatedClient()) {
            AVAILABLE_LEVELS = new int[] { LEVEL_SIMPLE };
        } else {
            throw new Error("TESTBUG: unknown VM: " + Platform.vmName);
        }
    }

    public static void main(String[] args) {
        if (Platform.isInt()) {
            throw new Error("TESTBUG: test can not be run in interpreter");
        }
        new CodeCacheStressRunner(new OverloadCompileQueueTest()).runTest();
    }

    public OverloadCompileQueueTest() {
        Helper.startInfiniteLoopThread(this::lockUnlock, 100L);
    }

    @Override
    public void run() {
        Helper.TestCase obj = Helper.TestCase.get();
        Class clazz = obj.getClass();
        Method mEnqueue;
        try {
            mEnqueue = clazz.getMethod(METHOD_TO_ENQUEUE);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new Error(String.format("TESTBUG: cannot get method '%s' of class %s", METHOD_TO_ENQUEUE, clazz.getName()), e);
        }
        for (int compLevel : AVAILABLE_LEVELS) {
            Helper.WHITE_BOX.enqueueMethodForCompilation(mEnqueue, compLevel);
        }
    }

    private void lockUnlock() {
        try {
            int sleep = Helper.RNG.nextInt(MAX_SLEEP);
            Helper.WHITE_BOX.lockCompilation();
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new Error("TESTBUG: lockUnlocker thread was unexpectedly interrupted", e);
        } finally {
            Helper.WHITE_BOX.unlockCompilation();
        }
    }
}