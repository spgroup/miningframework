package com.hazelcast.test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.PartitionService;
import com.hazelcast.instance.Node;
import com.hazelcast.instance.TestUtil;
import org.junit.After;
import org.junit.runner.RunWith;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastJUnit4ClassRunner.class)
public abstract class HazelcastTestSupport {

    private static final int ASSERT_TRUE_EVENTUALLY_TIMEOUT;

    static {
        System.setProperty("hazelcast.repmap.hooks.allowed", "true");
        ASSERT_TRUE_EVENTUALLY_TIMEOUT = Integer.parseInt(System.getProperty("hazelcast.assertTrueEventually.timeout", "120"));
        System.out.println("ASSERT_TRUE_EVENTUALLY_TIMEOUT = " + ASSERT_TRUE_EVENTUALLY_TIMEOUT);
    }

    private TestHazelcastInstanceFactory factory;

    protected final TestHazelcastInstanceFactory createHazelcastInstanceFactory(int nodeCount) {
        if (factory != null) {
            throw new IllegalStateException("Node factory is already created!");
        }
        return factory = new TestHazelcastInstanceFactory(nodeCount);
    }

    public static void assertTrueAllTheTime(AssertTask task, long durationSeconds) {
        for (int k = 0; k < durationSeconds; k++) {
            task.run();
            sleepSeconds(1);
        }
    }

    @After
    public final void shutdownNodeFactory() {
        final TestHazelcastInstanceFactory f = factory;
        if (f != null) {
            factory = null;
            f.shutdownAll();
        }
    }

    public static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }

    public static void assertTrueEventually(AssertTask task) {
        AssertionError error = null;
        for (int k = 0; k < ASSERT_TRUE_EVENTUALLY_TIMEOUT; k++) {
            try {
                task.run();
                return;
            } catch (AssertionError e) {
                error = e;
            }
            sleepSeconds(1);
        }
        throw error;
    }

    public static void assertTrueDelayed5sec(AssertTask task) {
        assertTrueDelayed(5, task);
    }

    public static void assertTrueDelayed(int delaySeconds, AssertTask task) {
        sleepSeconds(delaySeconds);
        task.run();
    }

    public HazelcastInstance createHazelcastInstance() {
        return createHazelcastInstanceFactory(1).newHazelcastInstance();
    }

    public static Node getNode(HazelcastInstance hz) {
        return TestUtil.getNode(hz);
    }

    protected static void warmUpPartitions(HazelcastInstance... instances) throws InterruptedException {
        TestUtil.warmUpPartitions(instances);
    }

    protected static void waitForShutdown(HazelcastInstance instance, int expectedMemberSize) throws InterruptedException {
        while (instance.getCluster().getMembers().size() > expectedMemberSize) {
            Thread.sleep(10);
        }
    }

    protected static String generateKeyOwnedBy(HazelcastInstance instance) throws InterruptedException {
        final Member localMember = instance.getCluster().getLocalMember();
        final PartitionService partitionService = instance.getPartitionService();
        int k = (int) (Math.random() * 1000);
        while (!localMember.equals(partitionService.getPartition(String.valueOf(k)).getOwner())) {
            k++;
            Thread.sleep(10);
        }
        return String.valueOf(k);
    }

    protected static String generateKeyNotOwnedBy(HazelcastInstance instance) throws InterruptedException {
        final Member localMember = instance.getCluster().getLocalMember();
        final PartitionService partitionService = instance.getPartitionService();
        int k = (int) (Math.random() * 1000);
        while (localMember.equals(partitionService.getPartition(String.valueOf(k)).getOwner())) {
            k++;
            Thread.sleep(10);
        }
        return String.valueOf(k);
    }

    public final class DummyUncheckedHazelcastTestException extends RuntimeException {
    }

    public static void printAllStackTraces() {
        Map liveThreads = Thread.getAllStackTraces();
        for (Iterator i = liveThreads.keySet().iterator(); i.hasNext(); ) {
            Thread key = (Thread) i.next();
            System.err.println("Thread " + key.getName());
            StackTraceElement[] trace = (StackTraceElement[]) liveThreads.get(key);
            for (int j = 0; j < trace.length; j++) {
                System.err.println("\tat " + trace[j]);
            }
        }
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }
}
