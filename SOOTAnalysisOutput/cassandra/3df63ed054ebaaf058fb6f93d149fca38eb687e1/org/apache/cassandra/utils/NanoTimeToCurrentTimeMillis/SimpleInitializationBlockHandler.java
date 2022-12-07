package org.apache.cassandra.utils;

import java.util.concurrent.TimeUnit;
import org.apache.cassandra.concurrent.ScheduledExecutors;
import org.apache.cassandra.config.Config;

public class NanoTimeToCurrentTimeMillis {

    private static final String TIMESTAMP_UPDATE_INTERVAL_PROPERTY = Config.PROPERTY_PREFIX + "NANOTIMETOMILLIS_TIMESTAMP_UPDATE_INTERVAL";

    private static final long TIMESTAMP_UPDATE_INTERVAL = Long.getLong(TIMESTAMP_UPDATE_INTERVAL_PROPERTY, 10000);

    private static volatile long[] TIMESTAMP_BASE = new long[] { System.currentTimeMillis(), System.nanoTime() };

    private static final Thread updater;

    public static long convert(long nanoTime) {
        final long[] timestampBase = TIMESTAMP_BASE;
        return timestampBase[0] + TimeUnit.NANOSECONDS.toMillis(nanoTime - timestampBase[1]);
    }

    public static void updateNow() {
        ScheduledExecutors.scheduledFastTasks.submit(NanoTimeToCurrentTimeMillis::updateTimestampBase);
    }

    static {
<<<<<<< MINE
        ScheduledExecutors.scheduledFastTasks.scheduleWithFixedDelay(NanoTimeToCurrentTimeMillis::updateTimestampBase, TIMESTAMP_UPDATE_INTERVAL, TIMESTAMP_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
=======
        updater = new Thread("NanoTimeToCurrentTimeMillis updater") {

            @Override
            public void run() {
                while (true) {
                    try {
                        synchronized (TIMESTAMP_UPDATE) {
                            TIMESTAMP_UPDATE.wait(TIMESTAMP_UPDATE_INTERVAL);
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                    TIMESTAMP_BASE = new long[] { Math.max(TIMESTAMP_BASE[0], System.currentTimeMillis()), Math.max(TIMESTAMP_BASE[1], System.nanoTime()) };
                }
            }
        };
        updater.setDaemon(true);
        updater.start();
>>>>>>> YOURS
    }

    private static void updateTimestampBase() {
        TIMESTAMP_BASE = new long[] { Math.max(TIMESTAMP_BASE[0], System.currentTimeMillis()), Math.max(TIMESTAMP_BASE[1], System.nanoTime()) };
    }

    public static void shutdown(long millis) throws InterruptedException {
        updater.interrupt();
        updater.join(millis);
    }
}