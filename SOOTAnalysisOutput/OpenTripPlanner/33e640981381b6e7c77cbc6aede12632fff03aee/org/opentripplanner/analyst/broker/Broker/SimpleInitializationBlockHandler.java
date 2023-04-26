package org.opentripplanner.analyst.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.opentripplanner.analyst.cluster.AnalystClusterRequest;
import org.opentripplanner.api.model.AgencyAndIdSerializer;
import org.opentripplanner.api.model.JodaLocalDateSerializer;
import org.opentripplanner.api.model.QualifiedModeSetSerializer;
import org.opentripplanner.api.model.TraverseModeSetSerializer;
import org.opentripplanner.analyst.cluster.AnalystWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class Broker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Broker.class);

    public final CircularList<Job> jobs = new CircularList<>();

    private int nUndeliveredTasks = 0;

    private int nWaitingConsumers = 0;

    private int nextTaskId = 0;

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(AgencyAndIdSerializer.makeModule());
        mapper.registerModule(QualifiedModeSetSerializer.makeModule());
        mapper.registerModule(JodaLocalDateSerializer.makeModule());
        mapper.registerModule(TraverseModeSetSerializer.makeModule());
    }

    private WorkerCatalog workerCatalog = new WorkerCatalog();

    TIntObjectMap<AnalystClusterRequest> deliveredTasks = new TIntObjectHashMap<>();

    TIntIntMap deliveryTimes = new TIntIntHashMap();

    private Queue<AnalystClusterRequest> highPriorityTasks = new ArrayDeque<>();

    private TIntObjectMap<Response> highPriorityResponses = new TIntObjectHashMap<>();

    Map<String, Deque<Response>> consumersByGraph = new HashMap<>();

    public synchronized void enqueuePriorityTask(AnalystClusterRequest task, Response response) {
        task.taskId = nextTaskId++;
        highPriorityTasks.add(task);
        highPriorityResponses.put(task.taskId, response);
        nUndeliveredTasks += 1;
        notify();
    }

    public synchronized void enqueueTasks(List<AnalystClusterRequest> tasks) {
        Job job = findJob(tasks.get(0));
        for (AnalystClusterRequest task : tasks) {
            task.taskId = nextTaskId++;
            job.addTask(task);
            nUndeliveredTasks += 1;
            LOG.debug("Enqueued task id {} in job {}", task.taskId, job.jobId);
            if (!task.graphId.equals(job.graphId)) {
                LOG.warn("Task graph ID {} does not match job graph ID {}.", task.graphId, job.graphId);
            }
        }
        notify();
    }

    public synchronized void registerSuspendedResponse(String graphId, Response response) {
        String workerId = response.getRequest().getHeader(AnalystWorker.WORKER_ID_HEADER);
        if (workerId != null && !workerId.isEmpty()) {
            workerCatalog.catalog(workerId, graphId);
        } else {
            LOG.error("Worker did not supply a unique ID for itself . Ignoring it.");
            return;
        }
        Deque<Response> deque = consumersByGraph.get(graphId);
        if (deque == null) {
            deque = new ArrayDeque<>();
            consumersByGraph.put(graphId, deque);
        }
        deque.addLast(response);
        nWaitingConsumers += 1;
        notify();
    }

    public synchronized boolean removeSuspendedResponse(String graphId, Response response) {
        Deque<Response> deque = consumersByGraph.get(graphId);
        if (deque == null) {
            return false;
        }
        if (deque.remove(response)) {
            nWaitingConsumers -= 1;
            LOG.debug("Removed closed connection from queue.");
            logQueueStatus();
            return true;
        }
        return false;
    }

    private void logQueueStatus() {
        LOG.info("{} undelivered, of which {} high-priority", nUndeliveredTasks, highPriorityTasks.size());
        LOG.info("{} producers waiting, {} consumers waiting", highPriorityResponses.size(), nWaitingConsumers);
        LOG.info("{} total workers", workerCatalog.size());
    }

    public synchronized void deliverTasks() throws InterruptedException {
        while (nUndeliveredTasks == 0) {
            LOG.debug("Task delivery thread is going to sleep, there are no tasks waiting for delivery.");
            logQueueStatus();
            wait();
        }
        LOG.debug("Task delivery thread is awake and there are some undelivered tasks.");
        logQueueStatus();
        Job job;
        if (highPriorityTasks.size() > 0) {
            AnalystClusterRequest task = highPriorityTasks.remove();
            job = new Job("HIGH PRIORITY");
            job.graphId = task.graphId;
            job.addTask(task);
        } else {
            job = jobs.advanceToElement(e -> e.visibleTasks.size() > 0);
        }
        LOG.debug("Task delivery thread has found undelivered tasks in job {}.", job.jobId);
        while (true) {
            while (nWaitingConsumers == 0) {
                LOG.debug("Task delivery thread is going to sleep, there are no consumers waiting.");
                wait();
            }
            LOG.debug("Task delivery thread is awake, and some consumers are waiting.");
            logQueueStatus();
            LOG.debug("Looking for an eligible consumer, respecting graph affinity.");
            Deque<Response> deque = consumersByGraph.get(job.graphId);
            while (deque != null && !deque.isEmpty()) {
                Response response = deque.pop();
                nWaitingConsumers -= 1;
                if (deliver(job, response)) {
                    return;
                }
            }
            LOG.debug("No consumers with the right affinity. Looking for any consumer.");
            List<Deque<Response>> deques = new ArrayList<>(consumersByGraph.values());
            deques.sort((d1, d2) -> Integer.compare(d2.size(), d1.size()));
            for (Deque<Response> d : deques) {
                while (!d.isEmpty()) {
                    Response response = d.pop();
                    nWaitingConsumers -= 1;
                    if (deliver(job, response)) {
                        return;
                    }
                }
            }
            LOG.debug("No consumer was available. They all must have closed their connections.");
            if (nWaitingConsumers != 0) {
                throw new AssertionError("There should be no waiting consumers here, something is wrong.");
            }
        }
    }

    public synchronized boolean deliver(Job job, Response response) {
        if (!response.getRequest().getRequest().getConnection().isOpen()) {
            LOG.debug("Consumer connection was closed. It will be removed.");
            return false;
        }
        List<AnalystClusterRequest> tasks = new ArrayList<>();
        while (tasks.size() < 4 && !job.visibleTasks.isEmpty()) {
            tasks.add(job.visibleTasks.poll());
        }
        try {
            response.setStatus(HttpStatus.OK_200);
            OutputStream out = response.getOutputStream();
            mapper.writeValue(out, tasks);
            response.resume();
        } catch (IOException e) {
            LOG.debug("Consumer connection caused IO error, it will be removed.");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            response.resume();
            job.visibleTasks.addAll(tasks);
            return false;
        }
        LOG.debug("Delivery of {} tasks succeeded.", tasks.size());
        nUndeliveredTasks -= tasks.size();
        job.markTasksDelivered(tasks);
        for (AnalystClusterRequest task : tasks) {
            deliveredTasks.put(task.taskId, task);
        }
        return true;
    }

    public synchronized boolean deleteJobTask(int taskId) {
        AnalystClusterRequest task = deliveredTasks.remove(taskId);
        if (task == null)
            return false;
        Job job = findJob(task.jobId);
        if (job == null)
            return true;
        job.markTasksCompleted(task);
        return true;
    }

    public synchronized Response deletePriorityTask(int taskId) {
        return highPriorityResponses.remove(taskId);
    }

    @Override
    public void run() {
        while (true) {
            try {
                deliverTasks();
            } catch (InterruptedException e) {
                LOG.warn("Task pump thread was interrupted.");
                return;
            }
        }
    }

    public Job findJob(AnalystClusterRequest task) {
        Job job = findJob(task.jobId);
        if (job != null)
                return job;
        job = new Job(task.jobId);
        job.graphId = task.graphId;
        jobs.insertAtTail(job);
        return job;
    }

    public Job findJob(String jobId) {
        for (Job job : jobs) {
            if (job.jobId.equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    public synchronized boolean deleteJob(String jobId) {
        Job job = findJob(jobId);
        if (job == null)
            return false;
        nUndeliveredTasks -= job.visibleTasks.size();
        return jobs.remove(job);
    }

    private Multimap<String, String> activeJobsPerGraph = HashMultimap.create();

    void activateJob(Job job) {
        activeJobsPerGraph.put(job.graphId, job.jobId);
    }

    void deactivateJob(Job job) {
        activeJobsPerGraph.remove(job.graphId, job.jobId);
    }
}