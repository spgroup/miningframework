/*
 * The MIT License
 * 
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Stephen Connolly, Tom Huybrechts, InfraDNA, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.BulkChange;
import hudson.CopyOnWrite;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.Initializer;
import static hudson.init.InitMilestone.JOB_LOADED;
import static hudson.util.Iterators.reverse;

import hudson.cli.declarative.CLIMethod;
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
import jenkins.util.Timer;
import hudson.triggers.SafeTimerTask;
import hudson.util.TimeUnit2;
import hudson.util.XStream2;
import hudson.util.ConsistentHash;
import hudson.util.ConsistentHash.Hash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import jenkins.security.QueueItemAuthenticator;
import jenkins.security.QueueItemAuthenticatorConfiguration;
import jenkins.util.AtmostOneTaskExecutor;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import javax.annotation.CheckForNull;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

/**
 * Build queue.
 *
 * <p>
 * This class implements the core scheduling logic. {@link Task} represents the executable
 * task that are placed in the queue. While in the queue, it's wrapped into {@link Item}
 * so that we can keep track of additional data used for deciding what to execute when.
 *
 * <p>
 * Items in queue goes through several stages, as depicted below:
 * <pre>
 * (enter) --> waitingList --+--> blockedProjects
 *                           |        ^
 *                           |        |
 *                           |        v
 *                           +--> buildables ---> pending ---> left
 * </pre>
 *
 * <p>
 * In addition, at any stage, an item can be removed from the queue (for example, when the user
 * cancels a job in the queue.) See the corresponding field for their exact meanings.
 *
 * @author Kohsuke Kawaguchi
 * @see QueueListener
 * @see QueueTaskDispatcher
 */
@ExportedBean
public class Queue extends ResourceController implements Saveable {

    /**
     * Defines the refresh period for of the internal cache ({@link #itemsView}).
     * Data should be defined in milliseconds, default value - 1000;
     * @since 1.577
     */
    private static int CACHE_REFRESH_PERIOD = Integer.getInteger(Queue.class.getName() + ".cacheRefreshPeriod", 1000);
    
    /**
     * Items that are waiting for its quiet period to pass.
     *
     * <p>
     * This consists of {@link Item}s that cannot be run yet
     * because its time has not yet come.
     */
    private final Set<WaitingItem> waitingList = new TreeSet<WaitingItem>();

    /**
     * {@link Task}s that can be built immediately
     * but blocked because another build is in progress,
     * required {@link Resource}s are not available,
     * blocked via {@link QueueTaskDispatcher#canRun(Item)},
     * or otherwise blocked by {@link Task#isBuildBlocked()}.
     */
    private final ItemList<BlockedItem> blockedProjects = new ItemList<BlockedItem>();

    /**
     * {@link Task}s that can be built immediately
     * that are waiting for available {@link Executor}.
     * This list is sorted in such a way that earlier items are built earlier.
     */
    private final ItemList<BuildableItem> buildables = new ItemList<BuildableItem>();

    /**
     * {@link Task}s that are being handed over to the executor, but execution
     * has not started yet.
     */
    private final ItemList<BuildableItem> pendings = new ItemList<BuildableItem>();

    /**
     * Items that left queue would stay here for a while to enable tracking via {@link Item#id}.
     *
     * This map is forgetful, since we can't remember everything that executed in the past.
     */
    private final Cache<Integer,LeftItem> leftItems = CacheBuilder.newBuilder().expireAfterWrite(5*60, TimeUnit.SECONDS).build();

    private final CachedItemList itemsView = new CachedItemList();

    /**
     * Maintains a copy of {@link Queue#getItems()}
     *
     * @see Queue#getApproximateItemsQuickly()
     */
    private class CachedItemList {
        /**
         * The current cached value.
         */
        @CopyOnWrite
        private volatile List<Item> itemsView = Collections.emptyList();
        /**
         * When does the cache info expire?
         */
        private final AtomicLong expires = new AtomicLong();

        List<Item> get() {
            long t = System.currentTimeMillis();
            long d = expires.get();
            if (t>d) {// need to refresh the cache
                long next = t+CACHE_REFRESH_PERIOD;
                if (expires.compareAndSet(d,next)) {
                    // avoid concurrent cache update via CAS.
                    // if the getItems() lock is contended,
                    // some threads will end up serving stale data,
                    // but that's OK.
                    itemsView = ImmutableList.copyOf(getItems());
                }
            }
            return itemsView;
        }
    }

    /**
     * Data structure created for each idle {@link Executor}.
     * This is a job offer from the queue to an executor.
     *
     * <p>
     * For each idle executor, this gets created to allow the scheduling logic
     * to assign a work. Once a work is assigned, the executor actually gets
     * started to carry out the task in question.
     */
    public class JobOffer extends MappingWorksheet.ExecutorSlot {
        public final Executor executor;

        /**
         * The work unit that this {@link Executor} is going to handle.
         */
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
            // LOGGER.info("Starting "+executor.getName());
        }

        @Override
        public Executor getExecutor() {
            return executor;
        }

        /**
         * Verifies that the {@link Executor} represented by this object is capable of executing the given task.
         */
        public boolean canTake(BuildableItem item) {
            Node node = getNode();
            if (node==null)     return false;   // this executor is about to die

            if(node.canTake(item)!=null)
                return false;   // this node is not able to take the task

            for (QueueTaskDispatcher d : QueueTaskDispatcher.all())
                if (d.canTake(node,item)!=null)
                    return false;

            return isAvailable();
        }

        /**
         * Is this executor ready to accept some tasks?
         */
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
            return String.format("JobOffer[%s #%d]",executor.getOwner().getName(), executor.getNumber());
        }
    }

    private volatile transient LoadBalancer loadBalancer;

    private volatile transient QueueSorter sorter;

    private transient final AtmostOneTaskExecutor<Void> maintainerThread = new AtmostOneTaskExecutor<Void>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            maintain();
            return null;
        }
    });

    public Queue(LoadBalancer loadBalancer) {
        this.loadBalancer =  loadBalancer.sanitize();
        // if all the executors are busy doing something, then the queue won't be maintained in
        // timely fashion, so use another thread to make sure it happens.
        new MaintainTask(this).periodic();
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        if(loadBalancer==null)  throw new IllegalArgumentException();
        this.loadBalancer = loadBalancer.sanitize();
    }

    public QueueSorter getSorter() {
        return sorter;
    }

    public void setSorter(QueueSorter sorter) {
        this.sorter = sorter;
    }

    /**
     * Loads the queue contents that was {@link #save() saved}.
     */
    public synchronized void load() {
        try {
            // first try the old format
            File queueFile = getQueueFile();
            if (queueFile.exists()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(queueFile)));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        AbstractProject j = Jenkins.getInstance().getItemByFullName(line, AbstractProject.class);
                        if (j != null)
                            j.scheduleBuild();
                    }
                } finally {
                    in.close();
                }
                // discard the queue file now that we are done
                queueFile.delete();
            } else {
                queueFile = getXMLQueueFile();
                if (queueFile.exists()) {
                    List list = (List) new XmlFile(XSTREAM, queueFile).read();
                    int maxId = 0;
                    for (Object o : list) {
                        if (o instanceof Task) {
                            // backward compatibility
                            schedule((Task)o, 0);
                        } else if (o instanceof Item) {
                            Item item = (Item)o;
                            if(item.task==null)
                                continue;   // botched persistence. throw this one away

                            maxId = Math.max(maxId, item.id);
                            if (item instanceof WaitingItem) {
                                item.enter(this);
                            } else if (item instanceof BlockedItem) {
                                item.enter(this);
                            } else if (item instanceof BuildableItem) {
                                item.enter(this);
                            } else {
                                throw new IllegalStateException("Unknown item type! " + item);
                            }
                        } // this conveniently ignores null
                    }
                    WaitingItem.COUNTER.set(maxId);

                    // I just had an incident where all the executors are dead at AbstractProject._getRuns()
                    // because runs is null. Debugger revealed that this is caused by a MatrixConfiguration
                    // object that doesn't appear to be de-serialized properly.
                    // I don't know how this problem happened, but to diagnose this problem better
                    // when it happens again, save the old queue file for introspection.
                    File bk = new File(queueFile.getPath() + ".bak");
                    bk.delete();
                    queueFile.renameTo(bk);
                    queueFile.delete();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load the queue file " + getXMLQueueFile(), e);
        }
    }

    /**
     * Persists the queue contents to the disk.
     */
    public synchronized void save() {
        if(BulkChange.contains(this))  return;
        
        // write out the tasks on the queue
    	ArrayList<Queue.Item> items = new ArrayList<Queue.Item>();
    	for (Item item: getItems()) {
            if(item.task instanceof TransientTask)  continue;
    	    items.add(item);
    	}

        try {
            XmlFile queueFile = new XmlFile(XSTREAM, getXMLQueueFile());
            queueFile.write(items);
            SaveableListener.fireOnChange(this, queueFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write out the queue file " + getXMLQueueFile(), e);
        }
    }

    /**
     * Wipes out all the items currently in the queue, as if all of them are cancelled at once.
     */
    @CLIMethod(name="clear-queue")
    public synchronized void clear() {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        for (WaitingItem i : new ArrayList<WaitingItem>(waitingList))   // copy the list as we'll modify it in the loop
            i.cancel(this);
        blockedProjects.cancelAll();
        pendings.cancelAll();
        buildables.cancelAll();
        scheduleMaintenance();
    }

    private File getQueueFile() {
        return new File(Jenkins.getInstance().getRootDir(), "queue.txt");
    }

    /*package*/ File getXMLQueueFile() {
        return new File(Jenkins.getInstance().getRootDir(), "queue.xml");
    }

    /**
     * @deprecated as of 1.311
     *      Use {@link #schedule(AbstractProject)}
     */
    public boolean add(AbstractProject p) {
        return schedule(p)!=null;
    }

    /**
     * Schedule a new build for this project.
     * @see #schedule(Task, int)
     */
    public @CheckForNull WaitingItem schedule(AbstractProject p) {
        return schedule(p, p.getQuietPeriod());
    }

    /**
     * Schedules a new build with a custom quiet period.
     *
     * <p>
     * Left for backward compatibility with &lt;1.114.
     *
     * @since 1.105
     * @deprecated as of 1.311
     *      Use {@link #schedule(Task, int)}
     */
    public boolean add(AbstractProject p, int quietPeriod) {
        return schedule(p, quietPeriod)!=null;
    }

    /**
     * @deprecated as of 1.521
     *  Use {@link #schedule2(Task, int, List)}
     */
    public synchronized WaitingItem schedule(Task p, int quietPeriod, List<Action> actions) {
        return schedule2(p, quietPeriod, actions).getCreateItem();
    }

    /**
     * Schedules an execution of a task.
     *
     * @param actions
     *      These actions can be used for associating information scoped to a particular build, to
     *      the task being queued. Upon the start of the build, these {@link Action}s will be automatically
     *      added to the {@link Run} object, and hence avaialable to everyone.
     *      For the convenience of the caller, this list can contain null, and those will be silently ignored.
     * @since 1.311
     * @return
     *      {@link hudson.model.queue.ScheduleResult.Refused} if Jenkins refused to add this task into the queue (for example because the system
     *      is about to shutdown.) Otherwise the task is either merged into existing items in the queue
     *      (in which case you get {@link hudson.model.queue.ScheduleResult.Existing} instance back), or a new item
     *      gets created in the queue (in which case you get {@link Created}.
     *
     *      Note the nature of the queue
     *      is that such {@link Item} only captures the state of the item at a particular moment,
     *      and by the time you inspect the object, some of its information can be already stale.
     *
     *      That said, one can still look at {@link Queue.Item#future}, {@link Queue.Item#id}, etc.
     */
    public synchronized @Nonnull ScheduleResult schedule2(Task p, int quietPeriod, List<Action> actions) {
        // remove nulls
        actions = new ArrayList<Action>(actions);
        for (Iterator<Action> itr = actions.iterator(); itr.hasNext();) {
            Action a =  itr.next();
            if (a==null)    itr.remove();
        }

    	for(QueueDecisionHandler h : QueueDecisionHandler.all())
    		if (!h.shouldSchedule(p, actions))
                return ScheduleResult.refused();    // veto

        return scheduleInternal(p, quietPeriod, actions);
    }

    /**
     * Schedules an execution of a task.
     *
     * @since 1.311
     * @return
     *      null if this task is already in the queue and therefore the add operation was no-op.
     *      Otherwise indicates the {@link WaitingItem} object added, although the nature of the queue
     *      is that such {@link Item} only captures the state of the item at a particular moment,
     *      and by the time you inspect the object, some of its information can be already stale.
     *
     *      That said, one can still look at {@link WaitingItem#future}, {@link WaitingItem#id}, etc.
     */
    private synchronized @Nonnull ScheduleResult scheduleInternal(Task p, int quietPeriod, List<Action> actions) {
        Calendar due = new GregorianCalendar();
    	due.add(Calendar.SECOND, quietPeriod);

        // Do we already have this task in the queue? Because if so, we won't schedule a new one.
    	List<Item> duplicatesInQueue = new ArrayList<Item>();
    	for(Item item : getItems(p)) {
    		boolean shouldScheduleItem = false;
    		for (QueueAction action: item.getActions(QueueAction.class)) {
                shouldScheduleItem |= action.shouldSchedule(actions);
    		}
    		for (QueueAction action: Util.filter(actions,QueueAction.class)) {
                shouldScheduleItem |= action.shouldSchedule((new ArrayList<Action>(item.getAllActions())));
    		}
    		if(!shouldScheduleItem) {
    			duplicatesInQueue.add(item);
    		}
    	}
    	if (duplicatesInQueue.isEmpty()) {
    		LOGGER.log(Level.FINE, "{0} added to queue", p);

    		// put the item in the queue
            WaitingItem added = new WaitingItem(due,p,actions);
            added.enter(this);
            scheduleMaintenance();   // let an executor know that a new item is in the queue.
            return ScheduleResult.created(added);
    	}

        LOGGER.log(Level.FINE, "{0} is already in the queue", p);

        // but let the actions affect the existing stuff.
        for(Item item : duplicatesInQueue) {
            for(FoldableAction a : Util.filter(actions,FoldableAction.class)) {
                a.foldIntoExisting(item, p, actions);
            }
        }

        boolean queueUpdated = false;
        for(WaitingItem wi : Util.filter(duplicatesInQueue,WaitingItem.class)) {
            if(quietPeriod<=0) {
                // the user really wants to build now, and they mean NOW.
                // so let's pull in the timestamp if we can.
                if (wi.timestamp.before(due))
                    continue;
            } else {
                // otherwise we do the normal quiet period implementation
                if (wi.timestamp.after(due))
                    continue;
                // quiet period timer reset. start the period over again
            }

            // waitingList is sorted, so when we change a timestamp we need to maintain order
            wi.leave(this);
            wi.timestamp = due;
            wi.enter(this);
            queueUpdated=true;
        }

        if (queueUpdated)   scheduleMaintenance();

        // REVISIT: when there are multiple existing items in the queue that matches the incoming one,
        // whether the new one should affect all existing ones or not is debateable. I for myself
        // thought this would only affect one, so the code was bit of surprise, but I'm keeping the current
        // behaviour.
        return ScheduleResult.existing(duplicatesInQueue.get(0));
    }


    /**
     * @deprecated as of 1.311
     *      Use {@link #schedule(Task, int)} 
     */
    public synchronized boolean add(Task p, int quietPeriod) {
    	return schedule(p, quietPeriod)!=null;
    }

    public synchronized @CheckForNull WaitingItem schedule(Task p, int quietPeriod) {
    	return schedule(p, quietPeriod, new Action[0]);
    }

    /**
     * @deprecated as of 1.311
     *      Use {@link #schedule(Task, int, Action...)} 
     */
    public synchronized boolean add(Task p, int quietPeriod, Action... actions) {
    	return schedule(p, quietPeriod, actions)!=null;
    }

    /**
     * Convenience wrapper method around {@link #schedule(Task, int, List)}
     */
    public synchronized @CheckForNull WaitingItem schedule(Task p, int quietPeriod, Action... actions) {
    	return schedule2(p, quietPeriod, actions).getCreateItem();
    }

    /**
     * Convenience wrapper method around {@link #schedule2(Task, int, List)}
     */
    public synchronized @Nonnull ScheduleResult schedule2(Task p, int quietPeriod, Action... actions) {
    	return schedule2(p, quietPeriod, Arrays.asList(actions));
    }

    /**
     * Cancels the item in the queue. If the item is scheduled more than once, cancels the first occurrence.
     *
     * @return true if the project was indeed in the queue and was removed.
     *         false if this was no-op.
     */
    public synchronized boolean cancel(Task p) {
        LOGGER.log(Level.FINE, "Cancelling {0}", p);
        for (WaitingItem item : waitingList) {
            if (item.task.equals(p)) {
                return item.cancel(this);
            }
        }
        // use bitwise-OR to make sure that both branches get evaluated all the time
        return blockedProjects.cancel(p)!=null | buildables.cancel(p)!=null;
    }
    
    public synchronized boolean cancel(Item item) {
        LOGGER.log(Level.FINE, "Cancelling {0} item#{1}", new Object[] {item.task, item.id});
        boolean r = item.cancel(this);

        LeftItem li = new LeftItem(item);
        li.enter(this);

        return r;
    }

    /**
     * Called from {@code queue.jelly} and {@code entries.jelly}.
     */
    @RequirePOST
    public HttpResponse doCancelItem(@QueryParameter int id) throws IOException, ServletException {
        Item item = getItem(id);
        if (item != null) {
            cancel(item);
        } // else too late, ignore (JENKINS-14813)
        return HttpResponses.forwardToPreviousPage();
    }

    public synchronized boolean isEmpty() {
        return waitingList.isEmpty() && blockedProjects.isEmpty() && buildables.isEmpty() && pendings.isEmpty();
    }

    private synchronized WaitingItem peek() {
        return waitingList.iterator().next();
    }

    /**
     * Gets a snapshot of items in the queue.
     *
     * Generally speaking the array is sorted such that the items that are most likely built sooner are
     * at the end.
     */
    @Exported(inline=true)
    public synchronized Item[] getItems() {
        List<Item> r = new ArrayList<Item>();

        for(WaitingItem p : waitingList) {
            r = filterItemListBasedOnPermissions(r, p);
        }
        for (BlockedItem p : blockedProjects.values()){
            r = filterItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(buildables.values())) {
            r = filterItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(pendings.values())) {
            r= filterItemListBasedOnPermissions(r, p);
        }
        Item[] items = new Item[r.size()];
        r.toArray(items);
        return items;
    }

    private List<Item> filterItemListBasedOnPermissions(List<Item> r, Item t) {
        if (t.task instanceof hudson.model.Item) {
            if (((hudson.model.Item)t.task).hasPermission(hudson.model.Item.READ)) {
                r.add(t);
            }
        }
        return r;
    }

    /**
     * Returns an array of Item for which it is only visible the name of the task.
     *
     * Generally speaking the array is sorted such that the items that are most likely built sooner are
     * at the end.
     */
    @Restricted(NoExternalUse.class)
    @Exported(inline=true)
    public synchronized StubItem[] getDiscoverableItems() {
        List<StubItem> r = new ArrayList<StubItem>();

        for(WaitingItem p : waitingList) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BlockedItem p : blockedProjects.values()){
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(buildables.values())) {
            r = filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        for (BuildableItem p : reverse(pendings.values())) {
            r= filterDiscoverableItemListBasedOnPermissions(r, p);
        }
        StubItem[] items = new StubItem[r.size()];
        r.toArray(items);
        return items;
    }

    private List<StubItem> filterDiscoverableItemListBasedOnPermissions(List<StubItem> r, Item t) {
        if (t.task instanceof hudson.model.Item) {
            if (!((hudson.model.Item)t.task).hasPermission(hudson.model.Item.READ) && ((hudson.model.Item)t.task).hasPermission(hudson.model.Item.DISCOVER)) {
                r.add(new StubItem(new StubTask(t.task)));
            }
        }
        return r;
    }

    /**
     * Like {@link #getItems()}, but returns an approximation that might not be completely up-to-date.
     *
     * <p>
     * At the expense of accuracy, this method does not usually lock {@link Queue} and therefore is faster
     * in a highly concurrent situation.
     *
     * <p>
     * The list obtained is an accurate snapshot of the queue at some point in the past. The snapshot
     * is updated and normally no more than one second old, but this is a soft commitment that might
     * get violated when the lock on {@link Queue} is highly contended.
     *
     * <p>
     * This method is primarily added to make UI threads run faster.
     *
     * @since 1.483
     */
    public List<Item> getApproximateItemsQuickly() {
        return itemsView.get();
    }
    
    public synchronized Item getItem(int id) {
    	for (Item item: waitingList) if (item.id == id) return item;
    	for (Item item: blockedProjects) if (item.id == id) return item;
    	for (Item item: buildables) if (item.id == id) return item;
        for (Item item: pendings) if (item.id == id) return item;

        return leftItems.getIfPresent(id);
    }

    /**
     * Gets all the {@link BuildableItem}s that are waiting for an executor in the given {@link Computer}.
     */
    public synchronized List<BuildableItem> getBuildableItems(Computer c) {
        List<BuildableItem> result = new ArrayList<BuildableItem>();
        _getBuildableItems(c, buildables, result);
        _getBuildableItems(c, pendings, result);
        return result;
    }

    private void _getBuildableItems(Computer c, ItemList<BuildableItem> col, List<BuildableItem> result) {
        Node node = c.getNode();
        if (node == null)   // Deleted computers cannot take build items... 
            return;
        for (BuildableItem p : col.values()) {
            if (node.canTake(p) == null)
                result.add(p);
        }
    }

    /**
     * Gets the snapshot of all {@link BuildableItem}s.
     */
    public synchronized List<BuildableItem> getBuildableItems() {
        ArrayList<BuildableItem> r = new ArrayList<BuildableItem>(buildables.values());
        r.addAll(pendings.values());
        return r;
    }

    /**
     * Gets the snapshot of all {@link BuildableItem}s.
     */
    public synchronized List<BuildableItem> getPendingItems() {
        return new ArrayList<BuildableItem>(pendings.values());
    }

    /**
     * Returns the snapshot of all {@link LeftItem}s.
     *
     * @since 1.519
     */
    public Collection<LeftItem> getLeftItems() {
        return Collections.unmodifiableCollection(leftItems.asMap().values());
    }

    /**
     * Immediately clear the {@link #getLeftItems} cache.
     * Useful for tests which need to verify that no links to a build remain.
     * @since 1.519
     */
    public void clearLeftItems() {
        leftItems.invalidateAll();
    }

    /**
     * Gets all items that are in the queue but not blocked
     *
     * @since 1.402
     */
    public synchronized List<Item> getUnblockedItems() {
    	List<Item> queuedNotBlocked = new ArrayList<Item>();
        queuedNotBlocked.addAll(waitingList);
        queuedNotBlocked.addAll(buildables);
        queuedNotBlocked.addAll(pendings);
        // but not 'blockedProjects'
        return queuedNotBlocked;
    }

    /**
     * Works just like {@link #getUnblockedItems()} but return tasks.
     *
     * @since 1.402
     */
    public synchronized Set<Task> getUnblockedTasks() {
        List<Item> items = getUnblockedItems();
        Set<Task> unblockedTasks = new HashSet<Task>(items.size());
        for (Queue.Item t : items)
            unblockedTasks.add(t.task);
        return unblockedTasks;
    }

    /**
     * Is the given task currently pending execution?
     */
    public synchronized boolean isPending(Task t) {
        for (BuildableItem i : pendings)
            if (i.task.equals(t))
                return true;
        return false;
    }

    /**
     * How many {@link BuildableItem}s are assigned for the given label?
     */
    public synchronized int countBuildableItemsFor(Label l) {
        int r = 0;
        for (BuildableItem bi : buildables.values())
            for (SubTask st : bi.task.getSubTasks())
                if (null==l || st.getAssignedLabel()==l)
                    r++;
        for (BuildableItem bi : pendings.values())
            for (SubTask st : bi.task.getSubTasks())
                if (null==l || st.getAssignedLabel()==l)
                    r++;
        return r;
    }

    /**
     * Counts all the {@link BuildableItem}s currently in the queue.
     */
    public synchronized int countBuildableItems() {
        return countBuildableItemsFor(null);
    }

    /**
     * Gets the information about the queue item for the given project.
     *
     * @return null if the project is not in the queue.
     */
    public synchronized Item getItem(Task t) {
        BlockedItem bp = blockedProjects.get(t);
        if (bp!=null)
            return bp;
        BuildableItem bi = buildables.get(t);
        if(bi!=null)
            return bi;
        bi = pendings.get(t);
        if(bi!=null)
            return bi;

        for (Item item : waitingList) {
            if (item.task.equals(t)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the information about the queue item for the given project.
     *
     * @return null if the project is not in the queue.
     */
    public synchronized List<Item> getItems(Task t) {
    	List<Item> result =new ArrayList<Item>();
    	result.addAll(blockedProjects.getAll(t));
    	result.addAll(buildables.getAll(t));
        result.addAll(pendings.getAll(t));
        for (Item item : waitingList) {
            if (item.task.equals(t)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Left for backward compatibility.
     *
     * @see #getItem(Task)
    public synchronized Item getItem(AbstractProject p) {
        return getItem((Task) p);
    }
     */

    /**
     * Returns true if this queue contains the said project.
     */
    public synchronized boolean contains(Task t) {
        if (blockedProjects.containsKey(t) || buildables.containsKey(t) || pendings.containsKey(t))
            return true;
        for (Item item : waitingList) {
            if (item.task.equals(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the executor actually starts executing the assigned work unit.
     *
     * This moves the task from the pending state to the "left the queue" state.
     */
    /*package*/ synchronized void onStartExecuting(Executor exec) throws InterruptedException {
        final WorkUnit wu = exec.getCurrentWorkUnit();
        pendings.remove(wu.context.item);

        LeftItem li = new LeftItem(wu.context);
        li.enter(this);
    }

    /**
     * Checks the queue and runs anything that can be run.
     *
     * <p>
     * When conditions are changed, this method should be invoked.
     * <p>
     * This wakes up one {@link Executor} so that it will maintain a queue.
     */
    @WithBridgeMethods(void.class)
    public Future<?> scheduleMaintenance() {
        // LOGGER.info("Scheduling maintenance");
        return maintainerThread.submit();
    }

    /**
     * Checks if the given item should be prevented from entering into the {@link #buildables} state
     * and instead stay in the {@link #blockedProjects} state.
     */
    private boolean isBuildBlocked(Item i) {
        if (i.task.isBuildBlocked() || !canRun(i.task.getResourceList()))
            return true;

        for (QueueTaskDispatcher d : QueueTaskDispatcher.all()) {
            if (d.canRun(i)!=null)
                return true;
        }

        return false;
    }

    /**
     * Make sure we don't queue two tasks of the same project to be built
     * unless that project allows concurrent builds.
     */
    private boolean allowNewBuildableTask(Task t) {
        try {
            if (t.isConcurrentBuild())
                return true;
        } catch (AbstractMethodError e) {
            // earlier versions don't have the "isConcurrentBuild" method, so fall back gracefully
        }
        return !buildables.containsKey(t) && !pendings.containsKey(t);
    }

    /**
     * Queue maintenance.
     *
     * <p>
     * Move projects between {@link #waitingList}, {@link #blockedProjects}, {@link #buildables}, and {@link #pendings}
     * appropriately.
     *
     * <p>
     * Jenkins internally invokes this method by itself whenever there's a change that can affect
     * the scheduling (such as new node becoming online, # of executors change, a task completes execution, etc.),
     * and it also gets invoked periodically (see {@link Queue.MaintainTask}.)
     */
    public synchronized void maintain() {
        LOGGER.log(Level.FINE, "Queue maintenance started {0}", this);

        // The executors that are currently waiting for a job to run.
        Map<Executor,JobOffer> parked = new HashMap<Executor,JobOffer>();

        {// update parked
            for (Computer c : Jenkins.getInstance().getComputers()) {
                for (Executor e : c.getExecutors()) {
                    if (e.isParking()) {
                        parked.put(e,new JobOffer(e));
                    }
                }
            }
        }


        {// blocked -> buildable
            for (BlockedItem p : new ArrayList<BlockedItem>(blockedProjects.values())) {// copy as we'll mutate the list
                if (!isBuildBlocked(p) && allowNewBuildableTask(p.task)) {
                    // ready to be executed
                    p.leave(this);
                    makeBuildable(new BuildableItem(p));
                }
            }
        }

        // waitingList -> buildable/blocked
        while (!waitingList.isEmpty()) {
            WaitingItem top = peek();

            if (top.timestamp.compareTo(new GregorianCalendar())>0)
                break; // finished moving all ready items from queue

            top.leave(this);
            Task p = top.task;
            if (!isBuildBlocked(top) && allowNewBuildableTask(p)) {
                // ready to be executed immediately
                makeBuildable(new BuildableItem(top));
            } else {
                // this can't be built now because another build is in progress
                // set this project aside.
                new BlockedItem(top).enter(this);
            }
        }

        final QueueSorter s = sorter;
        if (s != null)
        	s.sortBuildableItems(buildables);

        // allocate buildable jobs to executors
        for (BuildableItem p : new ArrayList<BuildableItem>(buildables)) {// copy as we'll mutate the list in the loop
            // one last check to make sure this build is not blocked.
            if (isBuildBlocked(p)) {
                p.leave(this);
                new BlockedItem(p).enter(this);
                LOGGER.log(Level.FINE, "Catching that {0} is blocked in the last minute", p);
                continue;
            }

            List<JobOffer> candidates = new ArrayList<JobOffer>(parked.size());
            for (JobOffer j : parked.values())
                if (j.canTake(p))
                    candidates.add(j);

            MappingWorksheet ws = new MappingWorksheet(p, candidates);
            Mapping m = loadBalancer.map(p.task, ws);
            if (m == null) {
                // if we couldn't find the executor that fits,
                // just leave it in the buildables list and
                // check if we can execute other projects
                LOGGER.log(Level.FINER, "Failed to map {0} to executors. candidates={1} parked={2}", new Object[]{p, candidates, parked.values()});
                continue;
            }

            // found a matching executor. use it.
            WorkUnitContext wuc = new WorkUnitContext(p);
            m.execute(wuc);

            p.leave(this);
            if (!wuc.getWorkUnits().isEmpty())
                makePending(p);
            else
                LOGGER.log(Level.FINE, "BuildableItem {0} with empty work units!?", p);
        }
    }

    private void makeBuildable(BuildableItem p) {
        if(Jenkins.FLYWEIGHT_SUPPORT && p.task instanceof FlyweightTask && !ifBlockedByHudsonShutdown(p.task)) {


            Jenkins h = Jenkins.getInstance();
            Map<Node,Integer> hashSource = new HashMap<Node, Integer>(h.getNodes().size());

            // Even if master is configured with zero executors, we may need to run a flyweight task like MatrixProject on it.
            hashSource.put(h, Math.max(h.getNumExecutors() * 100, 1));

            for (Node n : h.getNodes()) {
                hashSource.put(n, n.getNumExecutors() * 100);
            }

            ConsistentHash<Node> hash = new ConsistentHash<Node>(NODE_HASH);
            hash.addAll(hashSource);

            Label lbl = p.getAssignedLabel();
            for (Node n : hash.list(p.task.getFullDisplayName())) {
                Computer c = n.toComputer();
                if (c==null || c.isOffline())    continue;
                if (lbl!=null && !lbl.contains(n))  continue;
                if (n.canTake(p) != null) continue;
                c.startFlyWeightTask(new WorkUnitContext(p).createWorkUnit(p.task));
                makePending(p);
                return;
            }
            // if the execution get here, it means we couldn't schedule it anywhere.
            // so do the scheduling like other normal jobs.
        }

        p.enter(this);
    }


    private static Hash<Node> NODE_HASH = new Hash<Node>() {
        public String hash(Node node) {
            return node.getNodeName();
        }
    };

    private boolean makePending(BuildableItem p) {
        // LOGGER.info("Making "+p.task+" pending"); // REMOVE
        p.isPending = true;
        return pendings.add(p);
    }

    public static boolean ifBlockedByHudsonShutdown(Task task) {
        return Jenkins.getInstance().isQuietingDown() && !(task instanceof NonBlockingTask);
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Marks {@link Task}s that are not persisted.
     * @since 1.311
     */
    public interface TransientTask extends Task {}

    /**
     * Marks {@link Task}s that do not consume {@link Executor}.
     * @see OneOffExecutor
     * @since 1.318
     */
    public interface FlyweightTask extends Task {}

    /**
     * Marks {@link Task}s that are not affected by the {@linkplain Jenkins#isQuietingDown()}  quieting down},
     * because these tasks keep other tasks executing.
     *
     * @since 1.336 
     */
    public interface NonBlockingTask extends Task {}

    /**
     * Task whose execution is controlled by the queue.
     *
     * <p>
     * {@link #equals(Object) Value equality} of {@link Task}s is used
     * to collapse two tasks into one. This is used to avoid infinite
     * queue backlog.
     *
     * <p>
     * Pending {@link Task}s are persisted when Hudson shuts down, so
     * it needs to be persistable via XStream. To create a non-persisted
     * transient Task, extend {@link TransientTask} marker interface.
     *
     * <p>
     * Plugins are encouraged to extend from {@link AbstractQueueTask}
     * instead of implementing this interface directly, to maintain
     * compatibility with future changes to this interface.
     *
     * <p>
     * For historical reasons, {@link Task} object by itself
     * also represents the "primary" sub-task (and as implied by this
     * design, a {@link Task} must have at least one sub-task.)
     * Most of the time, the primary subtask is the only sub task.
     */
    public interface Task extends ModelObject, SubTask {
        /**
         * Returns true if the execution should be blocked
         * for temporary reasons.
         *
         * <p>
         * Short-hand for {@code getCauseOfBlockage()!=null}.
         */
        boolean isBuildBlocked();

        /**
         * @deprecated as of 1.330
         *      Use {@link CauseOfBlockage#getShortDescription()} instead.
         */
        String getWhyBlocked();

        /**
         * If the execution of this task should be blocked for temporary reasons,
         * this method returns a non-null object explaining why.
         *
         * <p>
         * Otherwise this method returns null, indicating that the build can proceed right away.
         *
         * <p>
         * This can be used to define mutual exclusion that goes beyond
         * {@link #getResourceList()}.
         */
        CauseOfBlockage getCauseOfBlockage();

        /**
         * Unique name of this task.
         *
         * <p>
         * This method is no longer used, left here for compatibility. Just return {@link #getDisplayName()}.
         */
        String getName();

        /**
         * @see hudson.model.Item#getFullDisplayName()
         */
        String getFullDisplayName();

        /**
         * Checks the permission to see if the current user can abort this executable.
         * Returns normally from this method if it's OK.
         *
         * @throws AccessDeniedException if the permission is not granted.
         */
        void checkAbortPermission();

        /**
         * Works just like {@link #checkAbortPermission()} except it indicates the status by a return value,
         * instead of exception.
         * Also used by default for {@link hudson.model.Queue.Item#hasCancelPermission}.
         */
        boolean hasAbortPermission();
        
        /**
         * Returns the URL of this task relative to the context root of the application.
         *
         * <p>
         * When the user clicks an item in the queue, this is the page where the user is taken to.
         * Hudson expects the current instance to be bound to the URL returned by this method.
         *
         * @return
         *      URL that ends with '/'.
         */
        String getUrl();
        
        /**
         * True if the task allows concurrent builds, where the same {@link Task} is executed
         * by multiple executors concurrently on the same or different nodes.
         *
         * @since 1.338
         */
        boolean isConcurrentBuild();

        /**
         * Obtains the {@link SubTask}s that constitute this task.
         *
         * <p>
         * The collection returned by this method must also contain the primary {@link SubTask}
         * represented by this {@link Task} object itself as the first element.
         * The returned value is read-only.
         *
         * <p>
         * At least size 1.
         *
         * <p>
         * Since this is a newly added method, the invocation may results in {@link AbstractMethodError}.
         * Use {@link Tasks#getSubTasksOf(Queue.Task)} that avoids this.
         *
         * @since 1.377
         */
        Collection<? extends SubTask> getSubTasks();

        /**
         * This method allows the task to provide the default fallback authentication object to be used
         * when {@link QueueItemAuthenticator} fails to authenticate the build.
         *
         * <p>
         * When the task execution touches other objects inside Jenkins, the access control is performed
         * based on whether this {@link Authentication} is allowed to use them.
         *
         * <p>
         * This method was added to an interface after it was created, so plugins built against
         * older versions of Jenkins may not have this method implemented. Called private method _getDefaultAuthenticationOf(Task) on {@link Tasks}
         * to avoid {@link AbstractMethodError}.
         *
         * @since 1.520
         * @see QueueItemAuthenticator
         * @see Tasks#getDefaultAuthenticationOf(Queue.Task)
         */
        @Nonnull Authentication getDefaultAuthentication();
    }

    /**
     * Represents the real meat of the computation run by {@link Executor}.
     *
     * <h2>Views</h2>
     * <p>
     * Implementation must have <tt>executorCell.jelly</tt>, which is
     * used to render the HTML that indicates this executable is executing.
     */
    public interface Executable extends Runnable {
        /**
         * Task from which this executable was created.
         *
         * <p>
         * Since this method went through a signature change in 1.377, the invocation may results in
         * {@link AbstractMethodError}.
         * Use {@link Executables#getParentOf(Queue.Executable)} that avoids this.
         */
        @Nonnull SubTask getParent();

        /**
         * Called by {@link Executor} to perform the task
         */
        void run();
        
        /**
         * Estimate of how long will it take to execute this executable.
         * Measured in milliseconds.
         * 
         * Please, consider using {@link Executables#getEstimatedDurationFor(Queue.Executable)}
         * to protected against AbstractMethodErrors!
         *
         * @return -1 if it's impossible to estimate.
         * @since 1.383
         */
        long getEstimatedDuration();

        /**
         * Used to render the HTML. Should be a human readable text of what this executable is.
         */
        @Override String toString();
    }

    /**
     * Item in a queue.
     */
    @ExportedBean(defaultVisibility = 999)
    public static abstract class Item extends Actionable {
        /**
         * VM-wide unique ID that tracks the {@link Task} as it moves through different stages
         * in the queue (each represented by different subtypes of {@link Item}.
         */
        @Exported
    	public final int id;
    	
		/**
         * Project to be built.
         */
        @Exported
        public final Task task;

        private /*almost final*/ transient FutureImpl future;
        
        private final long inQueueSince;

        /**
         * Build is blocked because another build is in progress,
         * required {@link Resource}s are not available, or otherwise blocked
         * by {@link Task#isBuildBlocked()}.
         */
        @Exported
        public boolean isBlocked() { return this instanceof BlockedItem; }

        /**
         * Build is waiting the executor to become available.
         * This flag is only used in {@link Queue#getItems()} for
         * 'pseudo' items that are actually not really in the queue.
         */
        @Exported
        public boolean isBuildable() { return this instanceof BuildableItem; }

        /**
         * True if the item is starving for an executor for too long.
         */
        @Exported
        public boolean isStuck() { return false; }
        
        /**
         * Since when is this item in the queue.
         * @return Unix timestamp
         */
        @Exported
        public long getInQueueSince() {
            return this.inQueueSince;
        }
        
        /**
         * Returns a human readable presentation of how long this item is already in the queue.
         * E.g. something like '3 minutes 40 seconds'
         */
        public String getInQueueForString() {
            long duration = System.currentTimeMillis() - this.inQueueSince;
            return Util.getTimeSpanString(duration);
        }

        /**
         * Can be used to wait for the completion (either normal, abnormal, or cancellation) of the {@link Task}.
         * <p>
         * Just like {@link #id}, the same object tracks various stages of the queue.
         */
        @WithBridgeMethods(Future.class)
        public QueueTaskFuture<Executable> getFuture() { return future; }

        /**
         * If this task needs to be run on a node with a particular label,
         * return that {@link Label}. Otherwise null, indicating
         * it can run on anywhere.
         * 
         * <p>
         * This code takes {@link LabelAssignmentAction} into account, then fall back to {@link SubTask#getAssignedLabel()}
         */
        public Label getAssignedLabel() {
            for (LabelAssignmentAction laa : getActions(LabelAssignmentAction.class)) {
                Label l = laa.getAssignedLabel(task);
                if (l!=null)    return l;
            }
            return task.getAssignedLabel();
        }

        /**
         * Convenience method that returns a read only view of the {@link Cause}s associated with this item in the queue.
         *
         * @return can be empty but never null
         * @since 1.343
         */
        public final List<Cause> getCauses() {
            CauseAction ca = getAction(CauseAction.class);
            if (ca!=null)
                return Collections.unmodifiableList(ca.getCauses());
            return Collections.emptyList();
        }

        @Restricted(DoNotUse.class) // used from Jelly
        public String getCausesDescription() {
            List<Cause> causes = getCauses();
            StringBuilder s = new StringBuilder();
            for (Cause c : causes) {
                s.append(c.getShortDescription()).append('\n');
            }
            return s.toString();
        }

        protected Item(Task task, List<Action> actions, int id, FutureImpl future) {
            this.task = task;
            this.id = id;
            this.future = future;
            this.inQueueSince = System.currentTimeMillis();
            for (Action action: actions) addAction(action);
        }
        
        protected Item(Task task, List<Action> actions, int id, FutureImpl future, long inQueueSince) {
            this.task = task;
            this.id = id;
            this.future = future;
            this.inQueueSince = inQueueSince;
            for (Action action: actions) addAction(action);
        }
        
        protected Item(Item item) {
        	this(item.task, new ArrayList<Action>(item.getAllActions()), item.id, item.future, item.inQueueSince);
        }

        /**
         * Returns the URL of this {@link Item} relative to the context path of Jenkins
         *
         * @return
         *      URL that ends with '/'.
         * @since 1.519
         */
        @Exported
        public String getUrl() {
            return "queue/item/"+id+'/';
        }

        /**
         * Gets a human-readable status message describing why it's in the queue.
         */
        @Exported
        public final String getWhy() {
            CauseOfBlockage cob = getCauseOfBlockage();
            return cob!=null ? cob.getShortDescription() : null;
        }

        /**
         * Gets an object that describes why this item is in the queue.
         */
        public abstract CauseOfBlockage getCauseOfBlockage();

        /**
         * Gets a human-readable message about the parameters of this item
         * @return String
         */
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

        /**
         * Checks whether a scheduled item may be canceled.
         * @return by default, the same as {@link hudson.model.Queue.Task#hasAbortPermission}
         */
        public boolean hasCancelPermission() {
            return task.hasAbortPermission();
        }
        
        public String getDisplayName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getSearchUrl() {
			// TODO Auto-generated method stub
			return null;
		}

        /** @deprecated Use {@link #doCancelItem} instead. */
        @Deprecated
        @RequirePOST
        public HttpResponse doCancelQueue() throws IOException, ServletException {
        	Jenkins.getInstance().getQueue().cancel(this);
            return HttpResponses.forwardToPreviousPage();
        }

        /**
         * Returns the identity that this task carries when it runs, for the purpose of access control.
         *
         * When the task execution touches other objects inside Jenkins, the access control is performed
         * based on whether this {@link Authentication} is allowed to use them. Implementers, if you are unsure,
         * return the identity of the user who queued the task, or {@link ACL#SYSTEM} to bypass the access control
         * and run as the super user.
         *
         * @since 1.520
         */
        @Nonnull
        public Authentication authenticate() {
            for (QueueItemAuthenticator auth : QueueItemAuthenticatorConfiguration.get().getAuthenticators()) {
                Authentication a = auth.authenticate(this);
                if (a!=null)
                    return a;
            }
            return Tasks.getDefaultAuthenticationOf(task);
        }


        public Api getApi() {
            return new Api(this);
        }

        private Object readResolve() {
            this.future = new FutureImpl(task);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getName() + ':' + task + ':' + id;
        }

        /**
         * Enters the appropriate queue for this type of item.
         */
        /*package*/ abstract void enter(Queue q);

        /**
         * Leaves the appropriate queue for this type of item.
         */
        /*package*/ abstract boolean leave(Queue q);

        /**
         * Cancels this item, which updates {@link #future} to notify the listener, and
         * also leaves the queue.
         */
        /*package*/ boolean cancel(Queue q) {
            boolean r = leave(q);
            if (r)  future.setAsCancelled();
            return r;
        }

    }

    /**
     * A Stub class for {@link Task} which exposes only the name of the Task to be displayed when the user
     * has DISCOVERY permissions only.
     */
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

    /**
     * A Stub class for {@link Item} which exposes only the name of the Task to be displayed when the user
     * has DISCOVERY permissions only.
     */
    @Restricted(NoExternalUse.class)
    @ExportedBean(defaultVisibility = 999)
    public class StubItem {

        @Exported public StubTask task;

        public StubItem(StubTask task) {
            this.task = task;
        }

    }
    
    /**
     * An optional interface for actions on Queue.Item.
     * Lets the action cooperate in queue management.
     * 
     * @since 1.300-ish.
     */
    public interface QueueAction extends Action {
    	/**
    	 * Returns whether the new item should be scheduled. 
    	 * An action should return true if the associated task is 'different enough' to warrant a separate execution.
    	 */
	    boolean shouldSchedule(List<Action> actions);
    }

    /**
     * Extension point for deciding if particular job should be scheduled or not.
     *
     * <p>
     * This handler is consulted every time someone tries to submit a task to the queue.
     * If any of the registered handlers returns false, the task will not be added
     * to the queue, and the task will never get executed. 
     *
     * <p>
     * The other use case is to add additional {@link Action}s to the task
     * (for example {@link LabelAssignmentAction}) to tasks that are submitted to the queue.
     *
     * @since 1.316
     */
    public static abstract class QueueDecisionHandler implements ExtensionPoint {
    	/**
    	 * Returns whether the new item should be scheduled.
         *
         * @param actions
         *      List of actions that are to be made available as {@link AbstractBuild#getActions()}
         *      upon the start of the build. This list is live, and can be mutated.
    	 */
    	public abstract boolean shouldSchedule(Task p, List<Action> actions);
    	    	
    	/**
    	 * All registered {@link QueueDecisionHandler}s
    	 */
    	public static ExtensionList<QueueDecisionHandler> all() {
    		return ExtensionList.lookup(QueueDecisionHandler.class);
    	}
    }
    
    /**
     * {@link Item} in the {@link Queue#waitingList} stage.
     */
    public static final class WaitingItem extends Item implements Comparable<WaitingItem> {
    	private static final AtomicInteger COUNTER = new AtomicInteger(0);
    	
        /**
         * This item can be run after this time.
         */
        @Exported
        public Calendar timestamp;

        public WaitingItem(Calendar timestamp, Task project, List<Action> actions) {
            super(project, actions, COUNTER.incrementAndGet(), new FutureImpl(project));
            this.timestamp = timestamp;
        }
        
        public int compareTo(WaitingItem that) {
            int r = this.timestamp.getTime().compareTo(that.timestamp.getTime());
            if (r != 0) return r;

            return this.id - that.id;
        }

        public CauseOfBlockage getCauseOfBlockage() {
            long diff = timestamp.getTimeInMillis() - System.currentTimeMillis();
            if (diff > 0)
                return CauseOfBlockage.fromMessage(Messages._Queue_InQuietPeriod(Util.getTimeSpanString(diff)));
            else
                return CauseOfBlockage.fromMessage(Messages._Queue_Unknown());
        }

        @Override
        /*package*/ void enter(Queue q) {
            if (q.waitingList.add(this)) {
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onEnterWaiting(this);
                    } catch (Throwable e) {
                        // don't let this kill the queue
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                    }
                }
            }
        }

        @Override
        /*package*/ boolean leave(Queue q) {
            boolean r = q.waitingList.remove(this);
            if (r) {
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveWaiting(this);
                    } catch (Throwable e) {
                        // don't let this kill the queue
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                    }
                }
            }
            return r;
        }


    }

    /**
     * Common part between {@link BlockedItem} and {@link BuildableItem}.
     */
    public static abstract class NotWaitingItem extends Item {
        /**
         * When did this job exit the {@link Queue#waitingList} phase?
         */
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

    /**
     * {@link Item} in the {@link Queue#blockedProjects} stage.
     */
    public final class BlockedItem extends NotWaitingItem {
        public BlockedItem(WaitingItem wi) {
            super(wi);
        }

        public BlockedItem(NotWaitingItem ni) {
            super(ni);
        }

        public CauseOfBlockage getCauseOfBlockage() {
            ResourceActivity r = getBlockingActivity(task);
            if (r != null) {
                if (r == task) // blocked by itself, meaning another build is in progress
                    return CauseOfBlockage.fromMessage(Messages._Queue_InProgress());
                return CauseOfBlockage.fromMessage(Messages._Queue_BlockedBy(r.getDisplayName()));
            }
            
            for (QueueTaskDispatcher d : QueueTaskDispatcher.all()) {
                CauseOfBlockage cause = d.canRun(this);
                if (cause != null)
                    return cause;
            }
            
            return task.getCauseOfBlockage();
        }

        /*package*/ void enter(Queue q) {
            LOGGER.log(Level.FINE, "{0} is blocked", this);
            blockedProjects.add(this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onEnterBlocked(this);
                } catch (Throwable e) {
                    // don't let this kill the queue
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                }
            }
        }

        /*package*/ boolean leave(Queue q) {
            boolean r = blockedProjects.remove(this);
            if (r) {
                LOGGER.log(Level.FINE, "{0} no longer blocked", this);
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveBlocked(this);
                    } catch (Throwable e) {
                        // don't let this kill the queue
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                    }
                }
            }
            return r;
        }
    }

    /**
     * {@link Item} in the {@link Queue#buildables} stage.
     */
    public final static class BuildableItem extends NotWaitingItem {
        /**
         * Set to true when this is added to the {@link Queue#pendings} list.
         */
        private boolean isPending;

        public BuildableItem(WaitingItem wi) {
            super(wi);
        }

        public BuildableItem(NotWaitingItem ni) {
            super(ni);
        }

        public CauseOfBlockage getCauseOfBlockage() {
            Jenkins jenkins = Jenkins.getInstance();
            if(ifBlockedByHudsonShutdown(task))
                return CauseOfBlockage.fromMessage(Messages._Queue_HudsonIsAboutToShutDown());

            Label label = getAssignedLabel();
            List<Node> allNodes = jenkins.getNodes();
            if (allNodes.isEmpty())
                label = null;    // no master/slave. pointless to talk about nodes

            if (label != null) {
                Set<Node> nodes = label.getNodes();
                if (label.isOffline()) {
                    if (nodes.size() != 1)      return new BecauseLabelIsOffline(label);
                    else                        return new BecauseNodeIsOffline(nodes.iterator().next());
                } else {
                    if (nodes.size() != 1)      return new BecauseLabelIsBusy(label);
                    else                        return new BecauseNodeIsBusy(nodes.iterator().next());
                }
            } else {
                return CauseOfBlockage.createNeedsMoreExecutor(Messages._Queue_WaitingForNextAvailableExecutor());
            }
        }

        @Override
        public boolean isStuck() {
            Label label = getAssignedLabel();
            if(label!=null && label.isOffline())
                // no executor online to process this job. definitely stuck.
                return true;

            long d = task.getEstimatedDuration();
            long elapsed = System.currentTimeMillis()-buildableStartMilliseconds;
            if(d>=0) {
                // if we were running elsewhere, we would have done this build ten times.
                return elapsed > Math.max(d,60000L)*10;
            } else {
                // more than a day in the queue
                return TimeUnit2.MILLISECONDS.toHours(elapsed)>24;
            }
        }

        @Exported
        public boolean isPending() {
            return isPending;
        }

        @Override
        /*package*/ void enter(Queue q) {
            q.buildables.add(this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onEnterBuildable(this);
                } catch (Throwable e) {
                    // don't let this kill the queue
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                }
            }
        }

        @Override
        /*package*/ boolean leave(Queue q) {
            boolean r = q.buildables.remove(this);
            if (r) {
                LOGGER.log(Level.FINE, "{0} no longer blocked", this);
                for (QueueListener ql : QueueListener.all()) {
                    try {
                        ql.onLeaveBuildable(this);
                    } catch (Throwable e) {
                        // don't let this kill the queue
                        LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                    }
                }
            }
            return r;
        }
    }

    /**
     * {@link Item} in the {@link Queue#leftItems} stage. These are items that had left the queue
     * by either began executing or by getting cancelled.
     *
     * @since 1.519
     */
    public final static class LeftItem extends Item {
        public final WorkUnitContext outcome;

        /**
         * When item has left the queue and begin executing.
         */
        public LeftItem(WorkUnitContext wuc) {
            super(wuc.item);
            this.outcome = wuc;
        }

        /**
         * When item is cancelled.
         */
        public LeftItem(Item cancelled) {
            super(cancelled);
            this.outcome = null;
        }

        @Override
        public CauseOfBlockage getCauseOfBlockage() {
            return null;
        }

        /**
         * If this is representing an item that started executing, this property returns
         * the primary executable (such as {@link AbstractBuild}) that created out of it.
         */
        @Exported
        public @CheckForNull Executable getExecutable() {
            return outcome!=null ? outcome.getPrimaryWorkUnit().getExecutable() : null;
        }

        /**
         * Is this representing a cancelled item?
         */
        @Exported
        public boolean isCancelled() {
            return outcome==null;
        }

        @Override
        void enter(Queue q) {
            q.leftItems.put(id,this);
            for (QueueListener ql : QueueListener.all()) {
                try {
                    ql.onLeft(this);
                } catch (Throwable e) {
                    // don't let this kill the queue
                    LOGGER.log(Level.WARNING, "QueueListener failed while processing "+this,e);
                }
            }
        }

        @Override
        boolean leave(Queue q) {
            // there's no leave operation
            return false;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Queue.class.getName());

    /**
     * This {@link XStream} instance is used to persist {@link Task}s.
     */
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
                Object item = Jenkins.getInstance().getItemByFullName(string);
                if(item==null)  throw new NoSuchElementException("No such job exists: "+string);
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
				Job<?,?> job = (Job<?,?>) Jenkins.getInstance().getItemByFullName(projectName);
                if(job==null)  throw new NoSuchElementException("No such job exists: "+projectName);
				Run<?,?> run = job.getBuildByNumber(buildNumber);
                if(run==null)  throw new NoSuchElementException("No such build: "+string);
				return run;
			}

			@Override
			public String toString(Object object) {
				Run<?,?> run = (Run<?,?>) object;
				return run.getParent().getFullName() + "#" + run.getNumber();
			}
        });

        /**
         * Reconnect every reference to {@link Queue} by the singleton.
         */
        XSTREAM.registerConverter(new AbstractSingleValueConverter() {
			@Override
			public boolean canConvert(Class klazz) {
				return Queue.class.isAssignableFrom(klazz);
			}

			@Override
			public Object fromString(String string) {
                return Jenkins.getInstance().getQueue();
			}

			@Override
			public String toString(Object item) {
                return "queue";
			}
        });
    }

    /**
     * Regularly invokes {@link Queue#maintain()} and clean itself up when
     * {@link Queue} gets GC-ed.
     */
    private static class MaintainTask extends SafeTimerTask {
        private final WeakReference<Queue> queue;

        MaintainTask(Queue queue) {
            this.queue = new WeakReference<Queue>(queue);
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
    
    /**
     * {@link ArrayList} of {@link Item} with more convenience methods.
     */
    private class ItemList<T extends Item> extends ArrayList<T> {
    	public T get(Task task) {
    		for (T item: this) {
    			if (item.task.equals(task)) {
    				return item;
    			}
    		}
    		return null;
    	}
    	
    	public List<T> getAll(Task task) {
    		List<T> result = new ArrayList<T>();
    		for (T item: this) {
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

        /**
         * Works like {@link #remove(Task)} but also marks the {@link Item} as cancelled.
         */
        public T cancel(Task p) {
            T x = get(p);
            if(x!=null) x.cancel(Queue.this);
            return x;
        }

        public void cancelAll() {
            for (T t : new ArrayList<T>(this))
                t.cancel(Queue.this);

            clear();    // just to be sure
        }
    }

    @CLIResolver
    public static Queue getInstance() {
        return Jenkins.getInstance().getQueue();
    }

    /**
     * Restores the queue content during the start up.
     */
    @Initializer(after=JOB_LOADED)
    public static void init(Jenkins h) {
        h.getQueue().load();
    }
}
