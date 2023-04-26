package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

public class Timer {

    static final Array<Timer> instances = new Array(1);

    static {
        Thread thread = new Thread("Timer") {

            public void run() {
                while (true) {
                    synchronized (instances) {
                        float time = System.nanoTime() * MathUtils.nanoToSec;
                        float wait = Float.MAX_VALUE;
                        for (int i = 0, n = instances.size; i < n; i++) wait = Math.min(wait, instances.get(i).update(time));
                        long waitMillis = (long) (wait * 1000);
                        try {
                            if (waitMillis > 0)
                                instances.wait(waitMillis);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    static public final Timer instance = new Timer();

    static private final int CANCELLED = -1;

    static private final int FOREVER = -2;

    private final Array<Task> tasks = new Array(false, 8);

    public Timer() {
        start();
    }

    public void postTask(Task task) {
        scheduleTask(task, 0, 0, 0);
    }

    public void scheduleTask(Task task, float delaySeconds) {
        scheduleTask(task, delaySeconds, 0, 0);
    }

    public void scheduleTask(Task task, float delaySeconds, float intervalSeconds) {
        scheduleTask(task, delaySeconds, intervalSeconds, FOREVER);
    }

    public void scheduleTask(Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        if (task.repeatCount != CANCELLED)
            throw new IllegalArgumentException("The same task may not be scheduled twice.");
        task.executeTime = System.nanoTime() * MathUtils.nanoToSec + delaySeconds;
        task.intervalSeconds = intervalSeconds;
        task.repeatCount = repeatCount;
        tasks.add(task);
        wake();
    }

    public void stop() {
        synchronized (instances) {
            instances.removeValue(this, true);
        }
    }

    public void start() {
        synchronized (instances) {
            if (instances.contains(this, true))
                return;
            instances.add(this);
            wake();
        }
    }

    public void clear() {
        for (int i = 0, n = tasks.size; i < n; i++) tasks.get(i).cancel();
        tasks.clear();
    }

    float update(float time) {
        float wait = Float.MAX_VALUE;
        for (int i = 0, n = tasks.size; i < n; i++) {
            Task task = tasks.get(i);
            if (task.executeTime > time) {
                wait = Math.min(wait, task.executeTime - time);
                continue;
            }
            if (task.repeatCount != CANCELLED) {
                if (task.repeatCount == 0)
                    task.repeatCount = CANCELLED;
                Gdx.app.postRunnable(task);
            }
            if (task.repeatCount == CANCELLED) {
                tasks.removeIndex(i);
                i--;
                n--;
            } else {
                task.executeTime = time + task.intervalSeconds;
                wait = Math.min(wait, task.executeTime - time);
                if (task.repeatCount > 0)
                    task.repeatCount--;
            }
        }
        return wait;
    }

    static private void wake() {
        synchronized (instances) {
            instances.notifyAll();
        }
    }

    static public void post(Task task) {
        instance.postTask(task);
    }

    static public void schedule(Task task, float delaySeconds) {
        instance.scheduleTask(task, delaySeconds);
    }

    static public void schedule(Task task, float delaySeconds, float intervalSeconds) {
        instance.scheduleTask(task, delaySeconds, intervalSeconds);
    }

    static public void schedule(Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        instance.scheduleTask(task, delaySeconds, intervalSeconds, repeatCount);
    }

    static abstract public class Task implements Runnable {

        float executeTime;

        float intervalSeconds;

        int repeatCount = CANCELLED;

        abstract public void run();

        public void cancel() {
            executeTime = 0;
            repeatCount = CANCELLED;
        }

        public boolean isScheduled() {
            return repeatCount != CANCELLED;
        }
    }
}