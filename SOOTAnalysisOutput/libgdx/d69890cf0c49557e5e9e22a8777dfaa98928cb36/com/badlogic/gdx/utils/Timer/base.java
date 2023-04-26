package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;

public class Timer {

    static public final Timer instance = new Timer();

    static private final int CANCELLED = -1;

    static private final int FOREVER = -2;

    private final Array<Task> tasks = new Array(false, 8);

    private boolean stopped, posted;

    private final Runnable timerRunnable = new Runnable() {

        public void run() {
            update();
        }
    };

    public void postTask(Task task) {
        scheduleTask(task, 0, 0, 0);
    }

    public void scheduleTask(Task task) {
        scheduleTask(task, 0, 0, FOREVER);
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
        task.delaySeconds = delaySeconds;
        task.intervalSeconds = intervalSeconds;
        task.repeatCount = repeatCount;
        tasks.add(task);
        postRunnable();
    }

    public void stop() {
        stopped = true;
    }

    public void start() {
        stopped = false;
        postRunnable();
    }

    public void clear() {
        for (int i = 0, n = tasks.size; i < n; i++) tasks.get(i).cancel();
        tasks.clear();
    }

    private void postRunnable() {
        if (stopped || posted)
            return;
        posted = true;
        Gdx.app.postRunnable(timerRunnable);
    }

    void update() {
        if (stopped) {
            posted = false;
            return;
        }
        float delta = Gdx.graphics.getDeltaTime();
        for (int i = 0, n = tasks.size; i < n; i++) {
            Task task = tasks.get(i);
            task.delaySeconds -= delta;
            if (task.delaySeconds > 0)
                continue;
            if (task.repeatCount != CANCELLED) {
                if (task.repeatCount == 0)
                    task.repeatCount = CANCELLED;
                task.run();
            }
            if (task.repeatCount == CANCELLED) {
                tasks.removeIndex(i);
                i--;
                n--;
            } else {
                task.delaySeconds = task.intervalSeconds;
                if (task.repeatCount > 0)
                    task.repeatCount--;
            }
        }
        if (tasks.size == 0)
            posted = false;
        else
            Gdx.app.postRunnable(timerRunnable);
    }

    static public void post(Task task) {
        instance.postTask(task);
    }

    static public void schedule(Task task) {
        instance.scheduleTask(task);
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

        float delaySeconds;

        float intervalSeconds;

        int repeatCount = CANCELLED;

        abstract public void run();

        public void cancel() {
            delaySeconds = 0;
            repeatCount = CANCELLED;
        }

        public boolean isScheduled() {
            return repeatCount != CANCELLED;
        }
    }
}
