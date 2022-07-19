package hudson.model;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.SortedMap;
import hudson.model.Descriptor.FormException;

public abstract class ViewJob<JobT extends ViewJob<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Job<JobT, RunT> {

    private transient long nextUpdate = 0;

    protected transient RunMap<RunT> runs = new RunMap<RunT>();

    private transient boolean notLoaded = true;

    private transient volatile boolean reloadingInProgress;

    private static final LinkedHashSet<ViewJob> reloadQueue = new LinkedHashSet<ViewJob>();

    static final Thread reloadThread = new ReloadThread();

    static {
        reloadThread.start();
    }

    protected ViewJob(Jenkins parent, String name) {
        super(parent, name);
    }

    protected ViewJob(ItemGroup parent, String name) {
        super(parent, name);
    }

    public boolean isBuildable() {
        return false;
    }

    @Override
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        super.onLoad(parent, name);
        notLoaded = true;
    }

    protected SortedMap<Integer, RunT> _getRuns() {
        if (notLoaded || runs == null) {
            synchronized (this) {
                if (runs == null)
                    runs = new RunMap<RunT>();
                if (notLoaded) {
                    notLoaded = false;
                    _reload();
                }
            }
        }
        if (nextUpdate < System.currentTimeMillis()) {
            if (!reloadingInProgress) {
                reloadingInProgress = true;
                synchronized (reloadQueue) {
                    reloadQueue.add(this);
                    reloadQueue.notify();
                }
            }
        }
        return runs;
    }

    public void removeRun(RunT run) {
        nextUpdate = 0;
    }

    private void _reload() {
        try {
            reload();
        } finally {
            reloadingInProgress = false;
            nextUpdate = reloadPeriodically ? System.currentTimeMillis() + 1000 * 60 : Long.MAX_VALUE;
        }
    }

    protected abstract void reload();

    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        super.submit(req, rsp);
        nextUpdate = 0;
    }

    private static final class ReloadThread extends Thread {

        private ReloadThread() {
            setName("ViewJob reload thread");
        }

        private ViewJob getNext() throws InterruptedException {
            synchronized (reloadQueue) {
                while (reloadQueue.isEmpty() && !terminating()) reloadQueue.wait(60 * 1000);
                if (terminating())
                    throw new InterruptedException();
                ViewJob job = reloadQueue.iterator().next();
                reloadQueue.remove(job);
                return job;
            }
        }

        private boolean terminating() {
            return Jenkins.getInstance().isTerminating();
        }

        @Override
        public void run() {
            while (!terminating()) {
                try {
                    getNext()._reload();
                } catch (InterruptedException e) {
                    return;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public static boolean reloadPeriodically = Boolean.getBoolean(ViewJob.class.getName() + ".reloadPeriodically");
}
