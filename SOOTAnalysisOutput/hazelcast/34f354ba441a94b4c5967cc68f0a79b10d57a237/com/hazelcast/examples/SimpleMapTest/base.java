package com.hazelcast.examples;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.logging.ILogger;
import com.hazelcast.partition.Partition;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class SimpleMapTest {

    public static final int STATS_SECONDS = 10;

    public static int THREAD_COUNT = 40;

    public static int ENTRY_COUNT = 10 * 1000;

    public static int VALUE_SIZE = 1000;

    public static int GET_PERCENTAGE = 40;

    public static int PUT_PERCENTAGE = 40;

    private static final HazelcastInstance INSTANCE;

    private static final ILogger logger;

    private static final String NAMESPACE = "default";

    final static Stats stats = new Stats();

    static {
        INSTANCE = Hazelcast.newHazelcastInstance(null);
        logger = INSTANCE.getLoggingService().getLogger("SimpleMapTest");
    }

    public static boolean parse(String... input) {
        boolean load = false;
        if (input != null && input.length > 0) {
            for (String arg : input) {
                arg = arg.trim();
                if (arg.startsWith("t")) {
                    THREAD_COUNT = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("c")) {
                    ENTRY_COUNT = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("v")) {
                    VALUE_SIZE = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("g")) {
                    GET_PERCENTAGE = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("p")) {
                    PUT_PERCENTAGE = Integer.parseInt(arg.substring(1));
                } else if (arg.startsWith("load")) {
                    load = true;
                }
            }
        } else {
            logger.log(Level.INFO, "Help: sh test.sh t200 v130 p10 g85 ");
            logger.log(Level.INFO, "    // means 200 threads, value-size 130 bytes, 10% put, 85% get");
            logger.log(Level.INFO, "");
        }
        return load;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean load = parse(args);
        logger.log(Level.INFO, "Starting Test with ");
        printVariables();
        ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
        startPrintStats();
        if (load)
            load(es);
        run(es);
    }

    private static void run(ExecutorService es) {
        final IMap<String, byte[]> map = INSTANCE.getMap(NAMESPACE);
        for (int i = 0; i < THREAD_COUNT; i++) {
            es.execute(new Runnable() {

                public void run() {
                    try {
                        while (true) {
                            int key = (int) (Math.random() * ENTRY_COUNT);
                            int operation = ((int) (Math.random() * 100));
                            if (operation < GET_PERCENTAGE) {
                                map.get(String.valueOf(key));
                                stats.gets.incrementAndGet();
                            } else if (operation < GET_PERCENTAGE + PUT_PERCENTAGE) {
                                map.put(String.valueOf(key), new byte[VALUE_SIZE]);
                                stats.puts.incrementAndGet();
                            } else {
                                map.remove(String.valueOf(key));
                                stats.removes.incrementAndGet();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    private static void load(ExecutorService es) throws InterruptedException {
        final IMap<String, byte[]> map = INSTANCE.getMap("default");
        final Member thisMember = INSTANCE.getCluster().getLocalMember();
        List<String> lsOwnedEntries = new LinkedList<String>();
        for (int i = 0; i < ENTRY_COUNT; i++) {
            final String key = String.valueOf(i);
            Partition partition = INSTANCE.getPartitionService().getPartition(key);
            if (thisMember.equals(partition.getOwner())) {
                lsOwnedEntries.add(key);
            }
        }
        final CountDownLatch latch = new CountDownLatch(lsOwnedEntries.size());
        for (final String ownedKey : lsOwnedEntries) {
            es.execute(new Runnable() {

                public void run() {
                    map.put(ownedKey, new byte[VALUE_SIZE]);
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    private static void startPrintStats() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(STATS_SECONDS * 1000);
                        Stats statsNow = stats.getAndReset();
                        System.out.println(statsNow);
                        System.out.println("Operations per Second : " + statsNow.total() / STATS_SECONDS);
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
            }
        });
    }

    public static class Stats {

        public AtomicLong gets = new AtomicLong();

        public AtomicLong puts = new AtomicLong();

        public AtomicLong removes = new AtomicLong();

        public Stats getAndReset() {
            long getsNow = gets.getAndSet(0);
            long putsNow = puts.getAndSet(0);
            long removesNow = removes.getAndSet(0);
            Stats newOne = new Stats();
            newOne.gets.set(getsNow);
            newOne.puts.set(putsNow);
            newOne.removes.set(removesNow);
            return newOne;
        }

        public long total() {
            return gets.get() + puts.get() + removes.get();
        }

        public String toString() {
            return "total= " + total() + ", gets:" + gets.get() + ", puts:" + puts.get() + ", removes:" + removes.get();
        }
    }

    private static void printVariables() {
        logger.log(Level.INFO, "      Thread Count: " + THREAD_COUNT);
        logger.log(Level.INFO, "       Entry Count: " + ENTRY_COUNT);
        logger.log(Level.INFO, "        Value Size: " + VALUE_SIZE);
        logger.log(Level.INFO, "    Get Percentage: " + GET_PERCENTAGE);
        logger.log(Level.INFO, "    Put Percentage: " + PUT_PERCENTAGE);
        logger.log(Level.INFO, " Remove Percentage: " + (100 - (PUT_PERCENTAGE + GET_PERCENTAGE)));
    }
}
