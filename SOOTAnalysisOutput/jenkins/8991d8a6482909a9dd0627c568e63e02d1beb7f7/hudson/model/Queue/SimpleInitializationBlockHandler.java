package hudson.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.BulkChange;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.Initializer;
import static hudson.init.InitMilestone.JOB_LOADED;
import static hudson.util.Iterators.reverse;
import hudson.cli.declarative.CLIResolver;
import hudson.model.labels.LabelAssignmentAction;
import hudson.model.queue.AbstractQueueTask;
import hudson.model.queue.Executables;
import hudson.model.queue.QueueListener;
import hudson.model.queue.QueueTaskFuture;
import hudson.model.queue.ScheduleResult;
import hudson.model.queue.ScheduleResult.Created;
import hudson.model.queue.SubTask;
import hudson.model.queue.FutureImpl;
import hudson.model.queue.MappingWorksheet;
import hudson.model.queue.MappingWorksheet.Mapping;
import hudson.model.queue.QueueSorter;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.model.queue.Tasks;
import hudson.model.queue.WorkUnit;
import hudson.model.Node.Mode;
import hudson.model.listeners.SaveableListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.FoldableAction;
import hudson.model.queue.CauseOfBlockage.BecauseLabelIsBusy;
import hudson.model.queue.CauseOfBlockage.BecauseNodeIsOffline;
import hudson.model.queue.CauseOfBlockage.BecauseLabelIsOffline;
import hudson.model.queue.CauseOfBlockage.BecauseNodeIsBusy;
import hudson.model.queue.WorkUnitContext;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import java.nio.file.Files;
import hudson.util.Futures;
import jenkins.security.QueueItemAuthenticatorProvider;
import jenkins.security.stapler.StaplerAccessibleType;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import hudson.triggers.SafeTimerTask;
import java.util.concurrent.TimeUnit;
import hudson.util.XStream2;
import hudson.util.ConsistentHash;
import hudson.util.ConsistentHash.Hash;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.security.QueueItemAuthenticator;
import jenkins.util.AtmostOneTaskExecutor;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.jenkinsci.bytecode.AdaptField;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import jenkins.model.queue.AsynchronousExecution;
import jenkins.model.queue.CompositeCauseOfBlockage;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
public class Queue extends ResourceController implements Saveable {

    private final Set<WaitingItem> waitingList = new TreeSet<>();

    private final ItemList<BlockedItem> blockedProjects = new ItemList<>();

    private final ItemList<BuildableItem> buildables = new ItemList<>();

    private final ItemList<BuildableItem> pendings = new ItemList<>();

    private transient volatile Snapshot snapshot = new Snapshot(waitingList, blockedProjects, buildables, pendings);

    private final Cache<Long, LeftItem> leftItems = CacheBuilder.newBuilder().expireAfterWrite(5 * 60, TimeUnit.SECONDS).build();

    public static class JobOffer extends MappingWorksheet.ExecutorSlot {

        public final Executor executor;

        private WorkUnit workUnit;

        private JobOffer(Executor executor) {
            this.executor = executor;
        }

        @Override
        protected void set(WorkUnit p) {
            assert this.workUnit == null;
            this.workUnit = p;
            assert executor.isParking();
            executor.start(workUnit);
        }

        @Override
        public Executor getExecutor() {
            return executor;
        }

        @Deprecated
        public boolean canTake(BuildableItem item) {
            return getCauseOfBlockage(item) == null;
        }

        @CheckForNull
        public CauseOfBlockage getCauseOfBlockage(BuildableItem item) {
            Node node = getNode();
            if (node == null) {
                return CauseOfBlockage.fromMessage(Messages._Queue_node_has_been_removed_from_configuration(executor.getOwner().getDisplayName()));
            }
            CauseOfBlockage reason = node.canTake(item);
            if (reason != null) {
                return reason;
            }
            for (QueueTaskDispatcher d : QueueTaskDispatcher.all()) {
                try {
                reason = d.canTake(node, item);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, t, () -> String.format("Exception evaluating if the node '%s' can take the task '%s'", node.getNodeName(), item.task.getName()));
                    reason = CauseOfBlockage.fromMessage(Messages._Queue_ExceptionCanTake());
                }
                if (reason != null) {
                    return reason;
                }
            }
            if (workUnit != null) {
                return CauseOfBlockage.fromMessage(Messages._Queue_executor_slot_already_in_use());
            }
            if (executor.getOwner().isOffline()) {
                return new CauseOfBlockage.BecauseNodeIsOffline(node);
            }
            if (!executor.getOwner().isAcceptingTasks()) {
                return new CauseOfBlockage.BecauseNodeIsNotAcceptingTasks(node);
            }
            return null;
        }

        public boolean isAvailable() {
            return workUnit == null && !executor.getOwner().isOffline() && executor.getOwner().isAcceptingTasks();
        }

        @CheckForNull
        public Node getNode() {
            return executor.getOwner().getNode();
        }

        public boolean isNotExclusive() {
            return getNode().getMode() == Mode.NORMAL;
        }

        @Override
        public String toString() {
            return String.format("JobOffer[%s #%d]", executor.getOwner().getName(), executor.getNumber());
        }
    }

    private volatile transient LoadBalancer loadBalancer;

    private volatile transient QueueSorter sorter;

    private transient final AtmostOneTaskExecutor<Void> maintainerThread = new AtmostOneTaskExecutor<>(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
            maintain();
            return null;
        }

        @Override
        public String toString() {
            return "Periodic Jenkins queue maintenance";
        }
    });

    private transient final ReentrantLock lock = new ReentrantLock();

    private transient final Condition condition = lock.newCondition();

    public Queue(@Nonnull LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer.sanitize();
        new MaintainTask(this).periodic();
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(@Nonnull LoadBalancer loadBalancer) {
        if (loadBalancer == null)
            throw new IllegalArgumentException();
        this.loadBalancer = loadBalancer.sanitize();
    }

    public QueueSorter getSorter() {
        return sorter;
    }

    public void setSorter(QueueSorter sorter) {
        this.sorter = sorter;
    }

    static class State {

        public long counter;

        public List<Item> items = new ArrayList<>();
    }

    public void load() {
        lock.lock();
        try {
            try {
                waitingList.clear();
                blockedProjects.clear();
                buildables.clear();
                pendings.clear();
                File queueFile = getQueueFile();
                if (queueFile.exists()) {
                    try (BufferedReader in = Files.newBufferedReader(Util.fileToPath(queueFile), Charset.defaultCharset())) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            AbstractProject j = Jenkins.get().getItemByFullName(line, AbstractProject.class);
                            if (j != null)
                                j.scheduleBuild();
                        }
                    }
                    queueFile.delete();
                } else {
                    queueFile = getXMLQueueFile();
                    if (queueFile.exists()) {
                        Object unmarshaledObj = new XmlFile(XSTREAM, queueFile).read();
                        List items;
                        if (unmarshaledObj instanceof State) {
                            State state = (State) unmarshaledObj;
                            items = state.items;
                            WaitingItem.COUNTER.set(state.counter);
                        } else {
                            items = (List) unmarshaledObj;
                            long maxId = 0;
                            for (Object o : items) {
                                if (o instanceof Item) {
                                    maxId = Math.max(maxId, ((Item) o).id);
                                }
                            }
                            WaitingItem.COUNTER.set(maxId);
                        }
                        for (Object o : items) {
                            if (o instanceof Task) {
                                schedule((Task) o, 0);
                            } else if (o instanceof Item) {
                                Item item = (Item) o;
                                if (item.task == null) {
                                    continue;
                                }
                                if (item instanceof WaitingItem) {
                                    item.enter(this);
                                } else if (item instanceof BlockedItem) {
                                    item.enter(this);
                                } else if (item instanceof BuildableItem) {
                                    item.enter(this);
                                } else {
                                    throw new IllegalStateException("Unknown item type! " + item);
                                }
                            }
                        }
                        File bk = new File(queueFile.getPath() + ".bak");
                        bk.delete();
                        queueFile.renameTo(bk);
                        queueFile.delete();
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load the queue file " + getXMLQueueFile(), e);
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    public void save() {
        if (BulkChange.contains(this))
            return;
        if (Jenkins.getInstanceOrNull() == null) {
            return;
        }
        XmlFile queueFile = new XmlFile(XSTREAM, getXMLQueueFile());
        lock.lock();
        try {
            State state = new State();
            state.counter = WaitingItem.COUNTER.longValue();
            for (Item item : getItems()) {
                if (item.task instanceof TransientTask)
                    continue;
                state.items.add(item);
            }
            try {
                queueFile.write(state);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to write out the queue file " + getXMLQueueFile(), e);
            }
        } finally {
            lock.unlock();
        }
        SaveableListener.fireOnChange(this, queueFile);
    }

    public void clear() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        lock.lock();
        try {
            try {
                for (WaitingItem i : new ArrayList<>(waitingList)) i.cancel(this);
                blockedProjects.cancelAll();
                pendings.cancelAll();
                buildables.cancelAll();
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
        scheduleMaintenance();
    }

    private File getQueueFile() {
        return new File(Jenkins.get().getRootDir(), "queue.txt");
    }

    File getXMLQueueFile() {
        return new File(Jenkins.get().getRootDir(), "queue.xml");
    }

    @Deprecated
    public boolean add(AbstractProject p) {
        return schedule(p) != null;
    }

    @CheckForNull
    public WaitingItem schedule(AbstractProject p) {
        return schedule(p, p.getQuietPeriod());
    }

    @Deprecated
    public boolean add(AbstractProject p, int quietPeriod) {
        return schedule(p, quietPeriod) != null;
    }

    @Deprecated
    public WaitingItem schedule(Task p, int quietPeriod, List<Action> actions) {
        return schedule2(p, quietPeriod, actions).getCreateItem();
    }

    @Nonnull
    public ScheduleResult schedule2(Task p, int quietPeriod, List<Action> actions) {
        actions = new ArrayList<>(actions);
        actions.removeIf(Objects::isNull);
        lock.lock();
        try {
            try {
                for (QueueDecisionHandler h : QueueDecisionHandler.all()) if (!h.shouldSchedule(p, actions))
                    return ScheduleResult.refused();
                return scheduleInternal(p, quietPeriod, actions);
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    private ScheduleResult scheduleInternal(Task p, int quietPeriod, List<Action> actions) {
        lock.lock();
        try {
            try {
                Calendar due = new GregorianCalendar();
                due.add(Calendar.SECOND, quietPeriod);
                List<Item> duplicatesInQueue = new ArrayList<>();
                for (Item item : liveGetItems(p)) {
                    boolean shouldScheduleItem = false;
                    for (QueueAction action : item.getActions(QueueAction.class)) {
                        shouldScheduleItem |= action.shouldSchedule(actions);
                    }
                    for (QueueAction action : Util.filter(actions, QueueAction.class)) {
                        shouldScheduleItem |= action.shouldSchedule((new ArrayList<>(item.getAllActions())));
                    }
                    if (!shouldScheduleItem) {
                        duplicatesInQueue.add(item);
                    }
                }
                if (duplicatesInQueue.isEmpty()) {
                    LOGGER.log(Level.FINE, "{0} added to queue", p);
                    WaitingItem added = new WaitingItem(due, p, actions);
                    added.enter(this);
                    scheduleMaintenance();
                    return ScheduleResult.created(added);
                }
                LOGGER.log(Level.FINE, "{0} is already in the queue", p);
                for (Item item : duplicatesInQueue) {
                    for (FoldableAction a : Util.filter(actions, FoldableAction.class)) {
                        a.foldIntoExisting(item, p, actions);
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "after folding {0}, {1} includes {2}", new Object[] { a, item, item.getAllActions() });
                        }
                    }
                }
                boolean queueUpdated = false;
                for (WaitingItem wi : Util.filter(duplicatesInQueue, WaitingItem.class)) {
                    if (wi.timestamp.before(due))
                        continue;
                    wi.leave(this);
                    wi.timestamp = due;
                    wi.enter(this);
                    queueUpdated = true;
                }
                if (queueUpdated)
                    scheduleMaintenance();
                return ScheduleResult.existing(duplicatesInQueue.get(0));
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    @Deprecated
    public boolean add(Task p, int quietPeriod) {
        return schedule(p, quietPeriod) != null;
    }

    @CheckForNull
    public WaitingItem schedule(Task p, int quietPeriod) {
        return schedule(p, quietPeriod, new Action[0]);
    }

    @Deprecated
    public boolean add(Task p, int quietPeriod, Action... actions) {
        return schedule(p, quietPeriod, actions) != null;
    }

    @CheckForNull
    public WaitingItem schedule(Task p, int quietPeriod, Action... actions) {
        return schedule2(p, quietPeriod, actions).getCreateItem();
    }

    @Nonnull
    public ScheduleResult schedule2(Task p, int quietPeriod, Action... actions) {
        return schedule2(p, quietPeriod, Arrays.asList(actions));
    }

    public boolean cancel(Task p) {
        lock.lock();
        try {
            try {
                LOGGER.log(Level.FINE, "Cancelling {0}", p);
                for (WaitingItem item : waitingList) {
                    if (item.task.equals(p)) {
                        return item.cancel(this);
                    }
                }
                return blockedProjects.cancel(p) != null | buildables.cancel(p) != null;
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateSnapshot() {
        Snapshot revised = new Snapshot(waitingList, blockedProjects, buildables, pendings);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "{0} ? {1}; leftItems={2}", new Object[] { snapshot, revised, leftItems.asMap() });
        }
        snapshot = revised;
    }

    public boolean cancel(Item item) {
        LOGGER.log(Level.FINE, "Cancelling {0} item#{1}", new Object[] { item.task, item.id });
        lock.lock();
        try {
            try {
                return item.cancel(this);
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    @RequirePOST
    public HttpResponse doCancelItem(@QueryParameter long id) throws IOException, ServletException {
        Item item = getItem(id);
        if (item != null) {
            if (item.hasCancelPermission()) {
                cancel(item);
            }
        }
        return HttpResponses.forwardToPreviousPage();
    }

    public boolean isEmpty() {
        Snapshot snapshot = this.snapshot;
        return snapshot.waitingList.isEmpty() && snapshot.blockedProjects.isEmpty() && snapshot.buildables.isEmpty() && snapshot.pendings.isEmpty();
    }

    private WaitingItem peek() {
        return waitingList.iterator().next();
    }

    @Exported(inline = true)
    public Item[] getItems() {
        Snapshot s = this.snapshot;
        List<Item> r = new ArrayList<>();
        for (WaitingItem p : s.waitingList) {
            r = checkPermissionsAndAddToList(r, p);
        }
        for (BlockedItem p : s.blockedProjects) {
            r = checkPermissionsAndAddToList(r, p);
        }
        for (BuildableItem p : reverse(s.buildables)) {
            r = checkPermissionsAndAddToList(r, p);
        }
        for (BuildableItem p : reverse(s.pendings)) {
            r = checkPermissionsAndAddToList(r, p);
        }
        Item[] items = new Item[r.size()];
        r.toArray(items);
        return items;
    }

    private List<Item> checkPermissionsAndAddToList(List<Item> r, Item t) {
        if (t.task instanceof hudson.security.AccessControlled) {
            if (((hudson.security.AccessControlled) t.task).hasPermission(hudson.model.Item.READ) || ((hudson.security.AccessControlled) t.task).hasPermission(hudson.security.Permission.READ)) {
                r.add(t);
            }
        }
        return r;
    }

    @Restricted(NoExternalUse.class)
    @Exported(inline = true)
    public StubItem[] getDiscoverableItems() {
        Snapshot s = this.snapshot;
        List<StubItem> r = new ArrayList<>();
        for (WaitingItem p : s.waitingList) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BlockedItem p : s.blockedProjects) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(s.buildables)) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(s.pendings)) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        StubItem[] items = new StubItem[r.size()];
        r.toArray(items);
        return items;
    }

    private List<StubItem> filterDiscoverableItemListBasedOnPermissions(List<StubItem> r, Item t) {
        if (t.task instanceof hudson.model.Item) {
            if (!((hudson.model.Item) t.task).hasPermission(hudson.model.Item.READ) && ((hudson.model.Item) t.task).hasPermission(hudson.model.Item.DISCOVER)) {
                r.add(new StubItem(new StubTask(t.task)));
            }
        }
        return r;
    }

    @Deprecated
    public List<Item> getApproximateItemsQuickly() {
        return Arrays.asList(getItems());
    }

    public Item getItem(long id) {
        Snapshot snapshot = this.snapshot;
        for (Item item : snapshot.blockedProjects) {
            if (item.id == id)
                return item;
        }
        for (Item item : snapshot.buildables) {
            if (item.id == id)
                return item;
        }
        for (Item item : snapshot.pendings) {
            if (item.id == id)
                return item;
        }
        for (Item item : snapshot.waitingList) {
            if (item.id == id) {
                return item;
            }
        }
        return leftItems.getIfPresent(id);
    }

    public List<BuildableItem> getBuildableItems(Computer c) {
        Snapshot snapshot = this.snapshot;
        List<BuildableItem> result = new ArrayList<>();
        _getBuildableItems(c, snapshot.buildables, result);
        _getBuildableItems(c, snapshot.pendings, result);
        return result;
    }

    private void _getBuildableItems(Computer c, List<BuildableItem> col, List<BuildableItem> result) {
        Node node = c.getNode();
        if (node == null)
            return;
        for (BuildableItem p : col) {
            if (node.canTake(p) == null)
                result.add(p);
        }
    }

    public List<BuildableItem> getBuildableItems() {
        Snapshot snapshot = this.snapshot;
        ArrayList<BuildableItem> r = new ArrayList<>(snapshot.buildables);
        r.addAll(snapshot.pendings);
        return r;
    }

    public List<BuildableItem> getPendingItems() {
        return new ArrayList<>(snapshot.pendings);
    }

    protected List<BlockedItem> getBlockedItems() {
        return new ArrayList<>(snapshot.blockedProjects);
    }

    public Collection<LeftItem> getLeftItems() {
        return Collections.unmodifiableCollection(leftItems.asMap().values());
    }

    public void clearLeftItems() {
        leftItems.invalidateAll();
    }

    public List<Item> getUnblockedItems() {
        Snapshot snapshot = this.snapshot;
        List<Item> queuedNotBlocked = new ArrayList<>();
        queuedNotBlocked.addAll(snapshot.waitingList);
        queuedNotBlocked.addAll(snapshot.buildables);
        queuedNotBlocked.addAll(snapshot.pendings);
        return queuedNotBlocked;
    }

    public Set<Task> getUnblockedTasks() {
        List<Item> items = getUnblockedItems();
        Set<Task> unblockedTasks = new HashSet<>(items.size());
        for (Queue.Item t : items) unblockedTasks.add(t.task);
        return unblockedTasks;
    }

    public boolean isPending(Task t) {
        Snapshot snapshot = this.snapshot;
        for (BuildableItem i : snapshot.pendings) if (i.task.equals(t))
            return true;
        return false;
    }

    @Nonnegative
    public int countBuildableItemsFor(@CheckForNull Label l) {
        Snapshot snapshot = this.snapshot;
        int r = 0;
        for (BuildableItem bi : snapshot.buildables) for (SubTask st : bi.task.getSubTasks()) if (null == l || bi.getAssignedLabelFor(st) == l)
            r++;
        for (BuildableItem bi : snapshot.pendings) for (SubTask st : bi.task.getSubTasks()) if (null == l || bi.getAssignedLabelFor(st) == l)
            r++;
        return r;
    }

    @Nonnegative
    public int strictCountBuildableItemsFor(@CheckForNull Label l) {
        Snapshot _snapshot = this.snapshot;
        int r = 0;
        for (BuildableItem bi : _snapshot.buildables) for (SubTask st : bi.task.getSubTasks()) if (bi.getAssignedLabelFor(st) == l)
            r++;
        for (BuildableItem bi : _snapshot.pendings) for (SubTask st : bi.task.getSubTasks()) if (bi.getAssignedLabelFor(st) == l)
            r++;
        return r;
    }

    public int countBuildableItems() {
        return countBuildableItemsFor(null);
    }

    public Item getItem(Task t) {
        Snapshot snapshot = this.snapshot;
        for (Item item : snapshot.blockedProjects) {
            if (item.task.equals(t))
                return item;
        }
        for (Item item : snapshot.buildables) {
            if (item.task.equals(t))
                return item;
        }
        for (Item item : snapshot.pendings) {
            if (item.task.equals(t))
                return item;
        }
        for (Item item : snapshot.waitingList) {
            if (item.task.equals(t)) {
                return item;
            }
        }
        return null;
    }

    private List<Item> liveGetItems(Task t) {
        lock.lock();
        try {
            List<Item> result = new ArrayList<>();
            result.addAll(blockedProjects.getAll(t));
            result.addAll(buildables.getAll(t));
            if (LOGGER.isLoggable(Level.FINE)) {
                List<BuildableItem> thePendings = pendings.getAll(t);
                if (!thePendings.isEmpty()) {
                    LOGGER.log(Level.FINE, "ignoring {0} during scheduleInternal", thePendings);
                }
            }
            for (Item item : waitingList) {
                if (item.task.equals(t)) {
                    result.add(item);
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public List<Item> getItems(Task t) {
        Snapshot snapshot = this.snapshot;
        List<Item> result = new ArrayList<>();
        for (Item item : snapshot.blockedProjects) {
            if (item.task.equals(t)) {
                result.add(item);
            }
        }
        for (Item item : snapshot.buildables) {
            if (item.task.equals(t)) {
                result.add(item);
            }
        }
        for (Item item : snapshot.pendings) {
            if (item.task.equals(t)) {
                result.add(item);
            }
        }
        for (Item item : snapshot.waitingList) {
            if (item.task.equals(t)) {
                result.add(item);
            }
        }
        return result;
    }

    public boolean contains(Task t) {
        return getItem(t) != null;
    }

    void onStartExecuting(Executor exec) throws InterruptedException {
        lock.lock();
        try {
            try {
                final WorkUnit wu = exec.getCurrentWorkUnit();
                pendings.remove(wu.context.item);
                LeftItem li = new LeftItem(wu.context);
                li.enter(this);
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    @WithBridgeMethods(void.class)
    public Future<?> scheduleMaintenance() {
        return maintainerThread.submit();
    }

    @CheckForNull
    private CauseOfBlockage getCauseOfBlockageForItem(Item i) {
        CauseOfBlockage causeOfBlockage = getCauseOfBlockageForTask(i.task);
        if (causeOfBlockage != null) {
            return causeOfBlockage;
        }
        for (QueueTaskDispatcher d : QueueTaskDispatcher.all()) {
            try {
            causeOfBlockage = d.canRun(i);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, t, () -> String.format("Exception evaluating if the queue can run the task '%s'", i.task.getName()));
                causeOfBlockage = CauseOfBlockage.fromMessage(Messages._Queue_ExceptionCanRun());
            }
            if (causeOfBlockage != null)
                return causeOfBlockage;
        }
        if (!(i instanceof BuildableItem)) {
            if (!i.task.isConcurrentBuild() && (buildables.containsKey(i.task) || pendings.containsKey(i.task))) {
                return CauseOfBlockage.fromMessage(Messages._Queue_InProgress());
            }
        }
        return null;
    }

    @CheckForNull
    private CauseOfBlockage getCauseOfBlockageForTask(Task task) {
        CauseOfBlockage causeOfBlockage = task.getCauseOfBlockage();
        if (causeOfBlockage != null) {
            return task.getCauseOfBlockage();
        }
        if (!canRun(task.getResourceList())) {
            ResourceActivity r = getBlockingActivity(task);
            if (r != null) {
                if (r == task)
                    return CauseOfBlockage.fromMessage(Messages._Queue_InProgress());
                return CauseOfBlockage.fromMessage(Messages._Queue_BlockedBy(r.getDisplayName()));
            }
        }
        return null;
    }

    public static void withLock(Runnable runnable) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        if (queue == null) {
            runnable.run();
        } else {
            queue._withLock(runnable);
        }
    }

    public static <V, T extends Throwable> V withLock(hudson.remoting.Callable<V, T> callable) throws T {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        if (queue == null) {
            return callable.call();
        } else {
            return queue._withLock(callable);
        }
    }

    public static <V> V withLock(java.util.concurrent.Callable<V> callable) throws Exception {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        if (queue == null) {
            return callable.call();
        } else {
            return queue._withLock(callable);
        }
    }

    public static boolean tryWithLock(Runnable runnable) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        if (queue == null) {
            runnable.run();
            return true;
        } else {
            return queue._tryWithLock(runnable);
        }
    }

    public static Runnable wrapWithLock(Runnable runnable) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        return queue == null ? runnable : new LockedRunnable(runnable);
    }

    public static <V, T extends Throwable> hudson.remoting.Callable<V, T> wrapWithLock(hudson.remoting.Callable<V, T> callable) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        return queue == null ? callable : new LockedHRCallable<>(callable);
    }

    public static <V> java.util.concurrent.Callable<V> wrapWithLock(java.util.concurrent.Callable<V> callable) {
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        final Queue queue = jenkins == null ? null : jenkins.getQueue();
        return queue == null ? callable : new LockedJUCCallable<>(callable);
    }

    @Override
    protected void _await() throws InterruptedException {
        condition.await();
    }

    @Override
    protected void _signalAll() {
        condition.signalAll();
    }

    protected void _withLock(Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    protected boolean _tryWithLock(Runnable runnable) {
        if (lock.tryLock()) {
            try {
                runnable.run();
            } finally {
                lock.unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    protected <V, T extends Throwable> V _withLock(hudson.remoting.Callable<V, T> callable) throws T {
        lock.lock();
        try {
            return callable.call();
        } finally {
            lock.unlock();
        }
    }

    protected <V> V _withLock(java.util.concurrent.Callable<V> callable) throws Exception {
        lock.lock();
        try {
            return callable.call();
        } finally {
            lock.unlock();
        }
    }

    public void maintain() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return;
        }
        lock.lock();
        try {
            try {
                LOGGER.log(Level.FINE, "Queue maintenance started on {0} with {1}", new Object[] { this, snapshot });
                Map<Executor, JobOffer> parked = new HashMap<>();
                {
                    List<BuildableItem> lostPendings = new ArrayList<>(pendings);
                    for (Computer c : jenkins.getComputers()) {
                        for (Executor e : c.getAllExecutors()) {
                            if (e.isInterrupted()) {
                                lostPendings.clear();
                                LOGGER.log(Level.FINEST, "Interrupt thread for executor {0} is set and we do not know what work unit was on the executor.", e.getDisplayName());
                                continue;
                            }
                            if (e.isParking()) {
                                LOGGER.log(Level.FINEST, "{0} is parking and is waiting for a job to execute.", e.getDisplayName());
                                parked.put(e, new JobOffer(e));
                            }
                            final WorkUnit workUnit = e.getCurrentWorkUnit();
                            if (workUnit != null) {
                                lostPendings.remove(workUnit.context.item);
                            }
                        }
                    }
                    for (BuildableItem p : lostPendings) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "BuildableItem {0}: pending -> buildable as the assigned executor disappeared", p.task.getFullDisplayName());
                        }
                        p.isPending = false;
                        pendings.remove(p);
                        makeBuildable(p);
                    }
                }
                final QueueSorter s = sorter;
                {
                    List<BlockedItem> blockedItems = new ArrayList<>(blockedProjects.values());
                    if (s != null) {
                        s.sortBlockedItems(blockedItems);
                    } else {
                        blockedItems.sort(QueueSorter.DEFAULT_BLOCKED_ITEM_COMPARATOR);
                    }
                    for (BlockedItem p : blockedItems) {
                        String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
                        LOGGER.log(Level.FINEST, "Current blocked item: {0}", taskDisplayName);
                        CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(p);
                        if (causeOfBlockage == null) {
                            LOGGER.log(Level.FINEST, "BlockedItem {0}: blocked -> buildable as the build is not blocked and new tasks are allowed", taskDisplayName);
                            Runnable r = makeBuildable(new BuildableItem(p));
                            if (r != null) {
                                p.leave(this);
                                r.run();
                                updateSnapshot();
                            }
                        } else {
                            p.setCauseOfBlockage(causeOfBlockage);
                        }
                    }
                }
                while (!waitingList.isEmpty()) {
                    WaitingItem top = peek();
                    if (top.timestamp.compareTo(new GregorianCalendar()) > 0) {
                        LOGGER.log(Level.FINEST, "Finished moving all ready items from queue.");
                        break;
                    }
                    top.leave(this);
                    CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(top);
                    if (causeOfBlockage == null) {
                        Runnable r = makeBuildable(new BuildableItem(top));
                        String topTaskDisplayName = LOGGER.isLoggable(Level.FINEST) ? top.task.getFullDisplayName() : null;
                        if (r != null) {
                            LOGGER.log(Level.FINEST, "Executing runnable {0}", topTaskDisplayName);
                            r.run();
                        } else {
                            LOGGER.log(Level.FINEST, "Item {0} was unable to be made a buildable and is now a blocked item.", topTaskDisplayName);
                            new BlockedItem(top, CauseOfBlockage.fromMessage(Messages._Queue_HudsonIsAboutToShutDown())).enter(this);
                        }
                    } else {
                        new BlockedItem(top, causeOfBlockage).enter(this);
                    }
                }
                if (s != null) {
                    try {
                        s.sortBuildableItems(buildables);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "s.sortBuildableItems() threw Throwable: {0}", e);
                    }
                }
                updateSnapshot();
                for (BuildableItem p : new ArrayList<>(buildables)) {
                    CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(p);
                    if (causeOfBlockage != null) {
                        p.leave(this);
                        new BlockedItem(p, causeOfBlockage).enter(this);
                        LOGGER.log(Level.FINE, "Catching that {0} is blocked in the last minute", p);
                        updateSnapshot();
                        continue;
                    }
                    String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
                    if (p.task instanceof FlyweightTask) {
                        Runnable r = makeFlyWeightTaskBuildable(new BuildableItem(p));
                        if (r != null) {
                            p.leave(this);
                            LOGGER.log(Level.FINEST, "Executing flyweight task {0}", taskDisplayName);
                            r.run();
                            updateSnapshot();
                        }
                    } else {
                        List<JobOffer> candidates = new ArrayList<>(parked.size());
                        List<CauseOfBlockage> reasons = new ArrayList<>(parked.size());
                        for (JobOffer j : parked.values()) {
                            CauseOfBlockage reason = j.getCauseOfBlockage(p);
                            if (reason == null) {
                                LOGGER.log(Level.FINEST, "{0} is a potential candidate for task {1}", new Object[] { j, taskDisplayName });
                                candidates.add(j);
                            } else {
                                LOGGER.log(Level.FINEST, "{0} rejected {1}: {2}", new Object[] { j, taskDisplayName, reason });
                                reasons.add(reason);
                            }
                        }
                        MappingWorksheet ws = new MappingWorksheet(p, candidates);
                        Mapping m = loadBalancer.map(p.task, ws);
                        if (m == null) {
                            LOGGER.log(Level.FINER, "Failed to map {0} to executors. candidates={1} parked={2}", new Object[] { p, candidates, parked.values() });
                            p.transientCausesOfBlockage = reasons.isEmpty() ? null : reasons;
                            continue;
                        }
                        WorkUnitContext wuc = new WorkUnitContext(p);
                        LOGGER.log(Level.FINEST, "Found a matching executor for {0}. Using it.", taskDisplayName);
                        m.execute(wuc);
                        p.leave(this);
                        if (!wuc.getWorkUnits().isEmpty()) {
                            LOGGER.log(Level.FINEST, "BuildableItem {0} marked as pending.", taskDisplayName);
                            makePending(p);
                        } else
                            LOGGER.log(Level.FINEST, "BuildableItem {0} with empty work units!?", p);
                        updateSnapshot();
                    }
                }
            } finally {
                updateSnapshot();
            }
        } finally {
            lock.unlock();
        }
    }

    @CheckForNull
    private Runnable makeBuildable(final BuildableItem p) {
        if (p.task instanceof FlyweightTask) {
            String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
            if (!isBlockedByShutdown(p.task)) {
                Runnable runnable = makeFlyWeightTaskBuildable(p);
                LOGGER.log(Level.FINEST, "Converting flyweight task: {0} into a BuildableRunnable", taskDisplayName);
                if (runnable != null) {
                    return runnable;
                }
                LOGGER.log(Level.FINEST, "Flyweight task {0} is entering as buildable to provision a node.", taskDisplayName);
                return new BuildableRunnable(p);
            }
            LOGGER.log(Level.FINEST, "Task {0} is blocked by shutdown.", taskDisplayName);
            return null;
        } else {
            return new BuildableRunnable(p);
        }
    }

    @CheckForNull
    private Runnable makeFlyWeightTaskBuildable(final BuildableItem p) {
        if (p.task instanceof FlyweightTask) {
            Jenkins h = Jenkins.get();
            Label lbl = p.getAssignedLabel();
            Computer masterComputer = h.toComputer();
            if (lbl != null && lbl.equals(h.getSelfLabel())) {
                if (h.canTake(p) == null) {
                    return createFlyWeightTaskRunnable(p, masterComputer);
                } else {
                    return null;
                }
            }
            if (lbl == null && h.canTake(p) == null && masterComputer.isOnline() && masterComputer.isAcceptingTasks()) {
                return createFlyWeightTaskRunnable(p, masterComputer);
            }
            Map<Node, Integer> hashSource = new HashMap<>(h.getNodes().size());
            for (Node n : h.getNodes()) {
                hashSource.put(n, n.getNumExecutors() * 100);
            }
            ConsistentHash<Node> hash = new ConsistentHash<>(NODE_HASH);
            hash.addAll(hashSource);
            String fullDisplayName = p.task.getFullDisplayName();
            for (Node n : hash.list(fullDisplayName)) {
                final Computer c = n.toComputer();
                if (c == null || c.isOffline()) {
                    continue;
                }
                if (lbl != null && !lbl.contains(n)) {
                    continue;
                }
                if (n.canTake(p) != null) {
                    continue;
                }
                return createFlyWeightTaskRunnable(p, c);
            }
        }
        return null;
    }

    private Runnable createFlyWeightTaskRunnable(final BuildableItem p, final Computer c) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Creating flyweight task {0} for computer {1}", new Object[] { p.task.getFullDisplayName(), c.getName() });
        }
        return new Runnable() {

            @Override
            public void run() {
                c.startFlyWeightTask(new WorkUnitContext(p).createWorkUnit(p.task));
                makePending(p);
            }
        };
    }

    private static Hash<Node> NODE_HASH = new Hash<Node>() {

        public String hash(Node node) {
            return node.getNodeName();
        }
    };

    private boolean makePending(BuildableItem p) {
        p.isPending = true;
        return pendings.add(p);
    }

    @Deprecated
    public static boolean ifBlockedByHudsonShutdown(Task task) {
        return isBlockedByShutdown(task);
    }

    public static boolean isBlockedByShutdown(Task task) {
        return Jenkins.get().isQuietingDown() && !(task instanceof NonBlockingTask);
    }

    public Api getApi() {
        return new Api(this);
    }

    public interface TransientTask extends Task {
    }

    public interface FlyweightTask extends Task {
    }

    public interface NonBlockingTask extends Task {
    }

    public interface Task extends ModelObject, SubTask {

        @Deprecated
        default boolean isBuildBlocked() {
            return getCauseOfBlockage() != null;
        }

        @Deprecated
        default String getWhyBlocked() {
            CauseOfBlockage cause = getCauseOfBlockage();
            return cause != null ? cause.getShortDescription() : null;
        }

        @CheckForNull
        default CauseOfBlockage getCauseOfBlockage() {
            return null;
        }

        String getName();

        String getFullDisplayName();

        default String getAffinityKey() {
            return getFullDisplayName();
        }

        void checkAbortPermission();

        boolean hasAbortPermission();

        String getUrl();

        default boolean isConcurrentBuild() {
            return false;
        }

        default Collection<? extends SubTask> getSubTasks() {
            return Collections.singleton(this);
        }

        @Nonnull
        default Authentication getDefaultAuthentication() {
            return ACL.SYSTEM;
        }

        @Nonnull
        default Authentication getDefaultAuthentication(Queue.Item item) {
            return getDefaultAuthentication();
        }
    }

    @StaplerAccessibleType
    public interface Executable extends Runnable {

        @Nonnull
        SubTask getParent();

        @Override
        void run() throws AsynchronousExecution;

        default long getEstimatedDuration() {
            return Executables.getParentOf(this).getEstimatedDuration();
        }

        @Override
        String toString();
    }

    @ExportedBean(defaultVisibility = 999)
    public static abstract class Item extends Actionable {

        private final long id;

        @Exported
        public long getId() {
            return id;
        }

        @AdaptField(was = int.class, name = "id")
        @Deprecated
        public int getIdLegacy() {
            if (id > Integer.MAX_VALUE) {
                throw new IllegalStateException("Sorry, you need to update any Plugins attempting to " + "assign 'Queue.Item.id' to an int value. 'Queue.Item.id' is now a long value and " + "has incremented to a value greater than Integer.MAX_VALUE (2^31 - 1).");
            }
            return (int) id;
        }

        @Exported
        public final Task task;

        private transient FutureImpl future;

        private final long inQueueSince;

        @Exported
        public boolean isBlocked() {
            return this instanceof BlockedItem;
        }

        @Exported
        public boolean isBuildable() {
            return this instanceof BuildableItem;
        }

        @Exported
        public boolean isStuck() {
            return false;
        }

        @Exported
        public long getInQueueSince() {
            return this.inQueueSince;
        }

        public String getInQueueForString() {
            long duration = System.currentTimeMillis() - this.inQueueSince;
            return Util.getTimeSpanString(duration);
        }

        @WithBridgeMethods(Future.class)
        public QueueTaskFuture<Executable> getFuture() {
            return future;
        }

        @CheckForNull
        public Label getAssignedLabel() {
            for (LabelAssignmentAction laa : getActions(LabelAssignmentAction.class)) {
                Label l = laa.getAssignedLabel(task);
                if (l != null)
                    return l;
            }
            return task.getAssignedLabel();
        }

        @CheckForNull
        public Label getAssignedLabelFor(@Nonnull SubTask st) {
            for (LabelAssignmentAction laa : getActions(LabelAssignmentAction.class)) {
                Label l = laa.getAssignedLabel(st);
                if (l != null)
                    return l;
            }
            return st.getAssignedLabel();
        }

        public final List<Cause> getCauses() {
            CauseAction ca = getAction(CauseAction.class);
            if (ca != null)
                return Collections.unmodifiableList(ca.getCauses());
            return Collections.emptyList();
        }

        @Restricted(DoNotUse.class)
        public String getCausesDescription() {
            List<Cause> causes = getCauses();
            StringBuilder s = new StringBuilder();
            for (Cause c : causes) {
                s.append(c.getShortDescription()).append('\n');
            }
            return s.toString();
        }

        protected Item(Task task, List<Action> actions, long id, FutureImpl future) {
            this.task = task;
            this.id = id;
            this.future = future;
            this.inQueueSince = System.currentTimeMillis();
            for (Action action : actions) addAction(action);
        }

        protected Item(Task task, List<Action> actions, long id, FutureImpl future, long inQueueSince) {
            this.task = task;
            this.id = id;
            this.future = future;
            this.inQueueSince = inQueueSince;
            for (Action action : actions) addAction(action);
        }

        @SuppressWarnings("deprecation")
        protected Item(Item item) {
            this(item.task, new ArrayList<>(item.getActions()), item.id, item.future, item.inQueueSince);
        }

        @Exported
        public String getUrl() {
            return "queue/item/" + id + '/';
        }

        @Exported
        public final String getWhy() {
            CauseOfBlockage cob = getCauseOfBlockage();
            return cob != null ? cob.getShortDescription() : null;
        }

        public abstract CauseOfBlockage getCauseOfBlockage();

        @Exported
        public String getParams() {
            StringBuilder s = new StringBuilder();
            for (ParametersAction pa : getActions(ParametersAction.class)) {
                for (ParameterValue p : pa.getParameters()) {
                    s.append('\n').append(p.getShortDescription());
                }
            }
            return s.toString();
        }

        public boolean hasCancelPermission() {
            return task.hasAbortPermission();
        }

        public String getDisplayName() {
            return null;
        }

        public String getSearchUrl() {
            return null;
        }

        @Deprecated
        @RequirePOST
        public HttpResponse doCancelQueue() throws IOException, ServletException {
            if (hasCancelPermission()) {
                Jenkins.get().getQueue().cancel(this);
            }
            return HttpResponses.forwardToPreviousPage();
        }

        @Nonnull
        public Authentication authenticate() {
            for (QueueItemAuthenticator auth : QueueItemAuthenticatorProvider.authenticators()) {
                Authentication a = auth.authenticate(this);
                if (a != null)
                    return a;
            }
            return task.getDefaultAuthentication(this);
        }

        @Restricted(DoNotUse.class)
        public Api getApi() throws AccessDeniedException {
            if (task instanceof AccessControlled) {
                AccessControlled ac = (AccessControlled) task;
                if (!ac.hasPermission(hudson.model.Item.DISCOVER)) {
                    return null;
                } else if (!ac.hasPermission(hudson.model.Item.READ)) {
                    throw new AccessDeniedException("Please log in to access " + task.getUrl());
                } else {
                    return new Api(this);
                }
            } else {
                return null;
            }
        }

        private Object readResolve() {
            this.future = new FutureImpl(task);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getName() + ':' + task + ':' + id;
        }

        abstract void enter(Queue q);

        abstract boolean leave(Queue q);

        boolean cancel(Queue q) {
            boolean r = leave(q);
            if (r) {
                future.setAsCancelled();
                LeftItem li = new LeftItem(this);
                li.enter(q);
            }
            return r;
        }
    }

    @Restricted(NoExternalUse.class)
    @ExportedBean(defaultVisibility = 999)
    public static class StubTask {

        private String name;

        public StubTask(@Nonnull Queue.Task base) {
            this.name = base.getName();
        }

        @Exported
        public String getName() {
            return name;
        }
    }

    @Restricted(NoExternalUse.class)
    @ExportedBean(defaultVisibility = 999)
    public class StubItem {

        @Exported
        public StubTask task;

        public StubItem(StubTask task) {
            this.task = task;
        }
    }

    public interface QueueAction extends Action {

        boolean shouldSchedule(List<Action> actions);
    }

    public static abstract class QueueDecisionHandler implements ExtensionPoint {

        public abstract boolean shouldSchedule(Task p, List<Action> actions);

        public static ExtensionList<QueueDecisionHandler> all() {
            return ExtensionList.lookup(QueueDecisionHandler.class);
        }
    }

    public static final class WaitingItem extends Item implements Comparable<WaitingItem> {

        private static final AtomicLong COUNTER = new AtomicLong(0);

        @Exported
        public Calendar timestamp;

        public WaitingItem(Calendar timestamp, Task project, List<Action> actions) {
            super(project, actions, COUNTER.incrementAndGet(), new FutureImpl(project));
            this.timestamp = timestamp;
        }

        static int getCurrentCounterValue() {
            return COUNTER.intValue();
        }

        public int compareTo(WaitingItem that) {
            int r = this.timestamp.getTime().compareTo(that.timestamp.getTime());
            if (r != 0)
                return r;
            if (this.getId() < that.getId()) {
                return -1;
            } else if (this.getId() == that.getId()) {
                return 0;
            } else {
                return 1;
            }
        }

        public CauseOfBlockage getCauseOfBlockage() {
            long diff = timestamp.getTimeInMillis() - System.currentTimeMillis();
            if (diff >= 0)
                return CauseOfBlockage.fromMessage(Messages._Queue_InQuietPeriod(Util.getTimeSpanString(diff)));
            else
                return CauseOfBlockage.fromMessage(Messages._Queue_FinishedWaiting());
        }

        @Override
        void enter(Queue q) {
            if (q.waitingList.add(this)) {
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onEnterWaiting(this);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                    }
                }
            }
        }

        @Override
        boolean leave(Queue q) {
            boolean r = q.waitingList.remove(this);
            if (r) {
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveWaiting(this);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                    }
                }
            }
            return r;
        }
    }

    public static abstract class NotWaitingItem extends Item {

        @Exported
        public final long buildableStartMilliseconds;

        protected NotWaitingItem(WaitingItem wi) {
            super(wi);
            buildableStartMilliseconds = System.currentTimeMillis();
        }

        protected NotWaitingItem(NotWaitingItem ni) {
            super(ni);
            buildableStartMilliseconds = ni.buildableStartMilliseconds;
        }
    }

    public final class BlockedItem extends NotWaitingItem {

        private transient CauseOfBlockage causeOfBlockage = null;

        public BlockedItem(WaitingItem wi) {
            this(wi, null);
        }

        public BlockedItem(NotWaitingItem ni) {
            this(ni, null);
        }

        BlockedItem(WaitingItem wi, CauseOfBlockage causeOfBlockage) {
            super(wi);
            this.causeOfBlockage = causeOfBlockage;
        }

        BlockedItem(NotWaitingItem ni, CauseOfBlockage causeOfBlockage) {
            super(ni);
            this.causeOfBlockage = causeOfBlockage;
        }

        void setCauseOfBlockage(CauseOfBlockage causeOfBlockage) {
            this.causeOfBlockage = causeOfBlockage;
        }

        public CauseOfBlockage getCauseOfBlockage() {
            if (causeOfBlockage != null) {
                return causeOfBlockage;
            }
            return getCauseOfBlockageForItem(this);
        }

        void enter(Queue q) {
            LOGGER.log(Level.FINE, "{0} is blocked", this);
            blockedProjects.add(this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onEnterBlocked(this);
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                }
            }
        }

        boolean leave(Queue q) {
            boolean r = blockedProjects.remove(this);
            if (r) {
                LOGGER.log(Level.FINE, "{0} no longer blocked", this);
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveBlocked(this);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                    }
                }
            }
            return r;
        }
    }

    public final static class BuildableItem extends NotWaitingItem {

        private boolean isPending;

        @CheckForNull
        private transient volatile List<CauseOfBlockage> transientCausesOfBlockage;

        public BuildableItem(WaitingItem wi) {
            super(wi);
        }

        public BuildableItem(NotWaitingItem ni) {
            super(ni);
        }

        public CauseOfBlockage getCauseOfBlockage() {
            Jenkins jenkins = Jenkins.get();
            if (isBlockedByShutdown(task))
                return CauseOfBlockage.fromMessage(Messages._Queue_HudsonIsAboutToShutDown());
            List<CauseOfBlockage> causesOfBlockage = transientCausesOfBlockage;
            Label label = getAssignedLabel();
            List<Node> allNodes = jenkins.getNodes();
            if (allNodes.isEmpty())
                label = null;
            if (label != null) {
                Set<Node> nodes = label.getNodes();
                if (label.isOffline()) {
                    if (nodes.size() != 1)
                        return new BecauseLabelIsOffline(label);
                    else
                        return new BecauseNodeIsOffline(nodes.iterator().next());
                } else {
                    if (causesOfBlockage != null && label.getIdleExecutors() > 0) {
                        return new CompositeCauseOfBlockage(causesOfBlockage);
                    }
                    if (nodes.size() != 1)
                        return new BecauseLabelIsBusy(label);
                    else
                        return new BecauseNodeIsBusy(nodes.iterator().next());
                }
            } else if (causesOfBlockage != null && new ComputerSet().getIdleExecutors() > 0) {
                return new CompositeCauseOfBlockage(causesOfBlockage);
            } else {
                return CauseOfBlockage.createNeedsMoreExecutor(Messages._Queue_WaitingForNextAvailableExecutor());
            }
        }

        @Override
        public boolean isStuck() {
            Label label = getAssignedLabel();
            if (label != null && label.isOffline())
                return true;
            long d = task.getEstimatedDuration();
            long elapsed = System.currentTimeMillis() - buildableStartMilliseconds;
            if (d >= 0) {
                return elapsed > Math.max(d, 60000L) * 10;
            } else {
                return TimeUnit.MILLISECONDS.toHours(elapsed) > 24;
            }
        }

        @Exported
        public boolean isPending() {
            return isPending;
        }

        @Override
        void enter(Queue q) {
            q.buildables.add(this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onEnterBuildable(this);
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                }
            }
        }

        @Override
        boolean leave(Queue q) {
            boolean r = q.buildables.remove(this);
            if (r) {
                LOGGER.log(Level.FINE, "{0} no longer blocked", this);
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveBuildable(this);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                    }
                }
            }
            return r;
        }
    }

    public final static class LeftItem extends Item {

        public final WorkUnitContext outcome;

        public LeftItem(WorkUnitContext wuc) {
            super(wuc.item);
            this.outcome = wuc;
        }

        public LeftItem(Item cancelled) {
            super(cancelled);
            this.outcome = null;
        }

        @Override
        public CauseOfBlockage getCauseOfBlockage() {
            return null;
        }

        @Exported
        @CheckForNull
        public Executable getExecutable() {
            return outcome != null ? outcome.getPrimaryWorkUnit().getExecutable() : null;
        }

        @Exported
        public boolean isCancelled() {
            return outcome == null;
        }

        @Override
        void enter(Queue q) {
            q.leftItems.put(getId(), this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onLeft(this);
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing " + this, e);
                }
            }
        }

        @Override
        boolean leave(Queue q) {
            return false;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Queue.class.getName());

    public static final XStream XSTREAM = new XStream2();

    static {
        XSTREAM.registerConverter(new AbstractSingleValueConverter() {

            @Override
            @SuppressWarnings("unchecked")
            public boolean canConvert(Class klazz) {
                return hudson.model.Item.class.isAssignableFrom(klazz);
            }

            @Override
            public Object fromString(String string) {
                Object item = Jenkins.get().getItemByFullName(string);
                if (item == null)
                    throw new NoSuchElementException("No such job exists: " + string);
                return item;
            }

            @Override
            public String toString(Object item) {
                return ((hudson.model.Item) item).getFullName();
            }
        });
        XSTREAM.registerConverter(new AbstractSingleValueConverter() {

            @SuppressWarnings("unchecked")
            @Override
            public boolean canConvert(Class klazz) {
                return Run.class.isAssignableFrom(klazz);
            }

            @Override
            public Object fromString(String string) {
                String[] split = string.split("#");
                String projectName = split[0];
                int buildNumber = Integer.parseInt(split[1]);
                Job<?, ?> job = (Job<?, ?>) Jenkins.get().getItemByFullName(projectName);
                if (job == null)
                    throw new NoSuchElementException("No such job exists: " + projectName);
                Run<?, ?> run = job.getBuildByNumber(buildNumber);
                if (run == null)
                    throw new NoSuchElementException("No such build: " + string);
                return run;
            }

            @Override
            public String toString(Object object) {
                Run<?, ?> run = (Run<?, ?>) object;
                return run.getParent().getFullName() + "#" + run.getNumber();
            }
        });
        XSTREAM.registerConverter(new AbstractSingleValueConverter() {

            @Override
            public boolean canConvert(Class klazz) {
                return Queue.class.isAssignableFrom(klazz);
            }

            @Override
            public Object fromString(String string) {
                return Jenkins.get().getQueue();
            }

            @Override
            public String toString(Object item) {
                return "queue";
            }
        });
    }

    private static class MaintainTask extends SafeTimerTask {

        private final WeakReference<Queue> queue;

        MaintainTask(Queue queue) {
            this.queue = new WeakReference<>(queue);
        }

        private void periodic() {
            long interval = 5000;
            Timer.get().scheduleWithFixedDelay(this, interval, interval, TimeUnit.MILLISECONDS);
        }

        protected void doRun() {
            Queue q = queue.get();
            if (q != null)
                q.maintain();
            else
                cancel();
        }
    }

    private class ItemList<T extends Item> extends ArrayList<T> {

        public T get(Task task) {
            for (T item : this) {
                if (item.task.equals(task)) {
                    return item;
                }
            }
            return null;
        }

        public List<T> getAll(Task task) {
            List<T> result = new ArrayList<>();
            for (T item : this) {
                if (item.task.equals(task)) {
                    result.add(item);
                }
            }
            return result;
        }

        public boolean containsKey(Task task) {
            return get(task) != null;
        }

        public T remove(Task task) {
            Iterator<T> it = iterator();
            while (it.hasNext()) {
                T t = it.next();
                if (t.task.equals(task)) {
                    it.remove();
                    return t;
                }
            }
            return null;
        }

        public void put(Task task, T item) {
            assert item.task.equals(task);
            add(item);
        }

        public ItemList<T> values() {
            return this;
        }

        public T cancel(Task p) {
            T x = get(p);
            if (x != null)
                x.cancel(Queue.this);
            return x;
        }

        @SuppressFBWarnings(value = "IA_AMBIGUOUS_INVOCATION_OF_INHERITED_OR_OUTER_METHOD", justification = "It will invoke the inherited clear() method according to Java semantics. " + "FindBugs recommends suppressing warnings in such case")
        public void cancelAll() {
            for (T t : new ArrayList<>(this)) t.cancel(Queue.this);
            clear();
        }
    }

    private static class Snapshot {

        private final Set<WaitingItem> waitingList;

        private final List<BlockedItem> blockedProjects;

        private final List<BuildableItem> buildables;

        private final List<BuildableItem> pendings;

        public Snapshot(Set<WaitingItem> waitingList, List<BlockedItem> blockedProjects, List<BuildableItem> buildables, List<BuildableItem> pendings) {
            this.waitingList = new LinkedHashSet<>(waitingList);
            this.blockedProjects = new ArrayList<>(blockedProjects);
            this.buildables = new ArrayList<>(buildables);
            this.pendings = new ArrayList<>(pendings);
        }

        @Override
        public String toString() {
            return "Queue.Snapshot{waitingList=" + waitingList + ";blockedProjects=" + blockedProjects + ";buildables=" + buildables + ";pendings=" + pendings + "}";
        }
    }

    private static class LockedRunnable implements Runnable {

        private final Runnable delegate;

        private LockedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            withLock(delegate);
        }
    }

    private class BuildableRunnable implements Runnable {

        private final BuildableItem buildableItem;

        private BuildableRunnable(BuildableItem p) {
            this.buildableItem = p;
        }

        @Override
        public void run() {
            buildableItem.enter(Queue.this);
        }
    }

    private static class LockedJUCCallable<V> implements java.util.concurrent.Callable<V> {

        private final java.util.concurrent.Callable<V> delegate;

        private LockedJUCCallable(java.util.concurrent.Callable<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V call() throws Exception {
            return withLock(delegate);
        }
    }

    private static class LockedHRCallable<V, T extends Throwable> implements hudson.remoting.Callable<V, T> {

        private static final long serialVersionUID = 1L;

        private final hudson.remoting.Callable<V, T> delegate;

        private LockedHRCallable(hudson.remoting.Callable<V, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public V call() throws T {
            return withLock(delegate);
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            delegate.checkRoles(checker);
        }
    }

    @CLIResolver
    public static Queue getInstance() {
        return Jenkins.get().getQueue();
    }

    @Initializer(after = JOB_LOADED)
    public static void init(Jenkins h) {
        h.getQueue().load();
    }

    @Extension
    @Restricted(NoExternalUse.class)
    public static final class Saver extends QueueListener implements Runnable {

        @VisibleForTesting
        static int DELAY_SECONDS = SystemProperties.getInteger("hudson.model.Queue.Saver.DELAY_SECONDS", 60);

        private final Object lock = new Object();

        @GuardedBy("lock")
        private Future<?> nextSave;

        @Override
        public void onEnterWaiting(WaitingItem wi) {
            push();
        }

        @Override
        public void onLeft(Queue.LeftItem li) {
            push();
        }

        private void push() {
            if (DELAY_SECONDS < 0)
                return;
            synchronized (lock) {
                if (nextSave != null && !(nextSave.isDone() || nextSave.isCancelled()))
                    return;
                nextSave = Timer.get().schedule(this, DELAY_SECONDS, TimeUnit.SECONDS);
            }
        }

        @Override
        public void run() {
            try {
                Jenkins j = Jenkins.getInstanceOrNull();
                if (j != null) {
                    j.getQueue().save();
                }
            } finally {
                synchronized (lock) {
                    nextSave = null;
                }
            }
        }

        @VisibleForTesting
        @Restricted(NoExternalUse.class)
        @Nonnull
        Future<?> getNextSave() {
            synchronized (lock) {
                return nextSave == null ? Futures.precomputed(null) : nextSave;
            }
        }
    }
}