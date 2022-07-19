package org.apache.accumulo.tserver;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.accumulo.fate.util.UtilWaitThread.sleepUninterruptibly;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.Durability;
import org.apache.accumulo.core.clientImpl.DurabilityImpl;
import org.apache.accumulo.core.clientImpl.TabletLocator;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.data.TableId;
import org.apache.accumulo.core.dataImpl.KeyExtent;
import org.apache.accumulo.core.master.thrift.BulkImportState;
import org.apache.accumulo.core.master.thrift.Compacting;
import org.apache.accumulo.core.master.thrift.MasterClientService;
import org.apache.accumulo.core.master.thrift.TableInfo;
import org.apache.accumulo.core.master.thrift.TabletServerStatus;
import org.apache.accumulo.core.metadata.MetadataTable;
import org.apache.accumulo.core.metadata.RootTable;
import org.apache.accumulo.core.metadata.TServerInstance;
import org.apache.accumulo.core.metadata.schema.TabletMetadata;
import org.apache.accumulo.core.metadata.schema.TabletMetadata.Location;
import org.apache.accumulo.core.metadata.schema.TabletMetadata.LocationType;
import org.apache.accumulo.core.replication.ReplicationConstants;
import org.apache.accumulo.core.replication.thrift.ReplicationServicer;
import org.apache.accumulo.core.rpc.ThriftUtil;
import org.apache.accumulo.core.tabletserver.log.LogEntry;
import org.apache.accumulo.core.tabletserver.thrift.TabletClientService.Iface;
import org.apache.accumulo.core.tabletserver.thrift.TabletClientService.Processor;
import org.apache.accumulo.core.trace.TraceUtil;
import org.apache.accumulo.core.util.ComparablePair;
import org.apache.accumulo.core.util.Halt;
import org.apache.accumulo.core.util.HostAndPort;
import org.apache.accumulo.core.util.MapCounter;
import org.apache.accumulo.core.util.Pair;
import org.apache.accumulo.core.util.ServerServices;
import org.apache.accumulo.core.util.ServerServices.Service;
import org.apache.accumulo.core.util.threads.ThreadPools;
import org.apache.accumulo.core.util.threads.Threads;
import org.apache.accumulo.fate.util.Retry;
import org.apache.accumulo.fate.util.Retry.RetryFactory;
import org.apache.accumulo.fate.util.UtilWaitThread;
import org.apache.accumulo.fate.zookeeper.ZooCache;
import org.apache.accumulo.fate.zookeeper.ZooLock;
import org.apache.accumulo.fate.zookeeper.ZooLock.LockLossReason;
import org.apache.accumulo.fate.zookeeper.ZooLock.LockWatcher;
import org.apache.accumulo.fate.zookeeper.ZooReaderWriter;
import org.apache.accumulo.fate.zookeeper.ZooUtil.NodeExistsPolicy;
import org.apache.accumulo.server.AbstractServer;
import org.apache.accumulo.server.GarbageCollectionLogger;
import org.apache.accumulo.server.ServerConstants;
import org.apache.accumulo.server.ServerContext;
import org.apache.accumulo.server.ServerOpts;
import org.apache.accumulo.server.TabletLevel;
import org.apache.accumulo.server.conf.TableConfiguration;
import org.apache.accumulo.server.fs.VolumeChooserEnvironment;
import org.apache.accumulo.server.fs.VolumeChooserEnvironmentImpl;
import org.apache.accumulo.server.fs.VolumeManager;
import org.apache.accumulo.server.log.SortedLogState;
import org.apache.accumulo.server.log.WalStateManager;
import org.apache.accumulo.server.log.WalStateManager.WalMarkerException;
import org.apache.accumulo.server.master.recovery.RecoveryPath;
import org.apache.accumulo.server.replication.ZooKeeperInitialization;
import org.apache.accumulo.server.rpc.ServerAddress;
import org.apache.accumulo.server.rpc.TCredentialsUpdatingWrapper;
import org.apache.accumulo.server.rpc.TServerUtils;
import org.apache.accumulo.server.rpc.ThriftServerType;
import org.apache.accumulo.server.security.AuditedSecurityOperation;
import org.apache.accumulo.server.security.SecurityOperation;
import org.apache.accumulo.server.security.SecurityUtil;
import org.apache.accumulo.server.security.delegation.AuthenticationTokenSecretManager;
import org.apache.accumulo.server.security.delegation.ZooAuthenticationKeyWatcher;
import org.apache.accumulo.server.util.FileSystemMonitor;
import org.apache.accumulo.server.util.ServerBulkImportStatus;
import org.apache.accumulo.server.util.time.RelativeTime;
import org.apache.accumulo.server.zookeeper.DistributedWorkQueue;
import org.apache.accumulo.tserver.TabletServerResourceManager.TabletResourceManager;
import org.apache.accumulo.tserver.TabletStatsKeeper.Operation;
import org.apache.accumulo.tserver.compactions.Compactable;
import org.apache.accumulo.tserver.compactions.CompactionManager;
import org.apache.accumulo.tserver.log.DfsLogger;
import org.apache.accumulo.tserver.log.LogSorter;
import org.apache.accumulo.tserver.log.MutationReceiver;
import org.apache.accumulo.tserver.log.TabletServerLogger;
import org.apache.accumulo.tserver.mastermessage.MasterMessage;
import org.apache.accumulo.tserver.mastermessage.SplitReportMessage;
import org.apache.accumulo.tserver.metrics.CompactionExecutorsMetrics;
import org.apache.accumulo.tserver.metrics.TabletServerMetrics;
import org.apache.accumulo.tserver.metrics.TabletServerMinCMetrics;
import org.apache.accumulo.tserver.metrics.TabletServerScanMetrics;
import org.apache.accumulo.tserver.metrics.TabletServerUpdateMetrics;
import org.apache.accumulo.tserver.replication.ReplicationServicerHandler;
import org.apache.accumulo.tserver.replication.ReplicationWorker;
import org.apache.accumulo.tserver.scan.ScanRunState;
import org.apache.accumulo.tserver.session.Session;
import org.apache.accumulo.tserver.session.SessionManager;
import org.apache.accumulo.tserver.tablet.BulkImportCacheCleaner;
import org.apache.accumulo.tserver.tablet.CommitSession;
import org.apache.accumulo.tserver.tablet.CompactionWatcher;
import org.apache.accumulo.tserver.tablet.Tablet;
import org.apache.accumulo.tserver.tablet.TabletData;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.metrics2.MetricsSystem;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.server.TServer;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;

public class TabletServer extends AbstractServer {<<<<<<< MINE

=======


  private class UnloadTabletHandler implements Runnable {

    private final KeyExtent extent;

    private final TUnloadTabletGoal goalState;

    private final long requestTimeSkew;

    public UnloadTabletHandler(KeyExtent extent, TUnloadTabletGoal goalState, long requestTime) {
      this.extent = extent;
      this.goalState = goalState;
      this.requestTimeSkew = requestTime - MILLISECONDS.convert(System.nanoTime(), NANOSECONDS);
    }

    @Override
    public void run() {
      Tablet t = null;
      synchronized (unopenedTablets) {
        if (unopenedTablets.contains(extent)) {
          unopenedTablets.remove(extent);
          return;
        }
      }
      synchronized (openingTablets) {
        while (openingTablets.contains(extent)) {
          try {
            log.info("Waiting for tablet {} to finish opening before unloading.", extent);
            openingTablets.wait();
          } catch (InterruptedException e) {
          }
        }
      }
      synchronized (onlineTablets) {
        if (onlineTablets.containsKey(extent)) {
          t = onlineTablets.get(extent);
        }
      }
      if (t == null) {
        if (!recentlyUnloadedCache.containsKey(extent)) {
          log.info("told to unload tablet that was not being served " + extent);
          enqueueMasterMessage(new TabletStatusMessage(TabletLoadState.UNLOAD_FAILURE_NOT_SERVING, extent));
        }
        return;
      }
      try {
        t.close(!goalState.equals(TUnloadTabletGoal.DELETED));
      } catch (Throwable e) {
        if ((t.isClosing() || t.isClosed()) && e instanceof IllegalStateException) {
          log.debug("Failed to unload tablet {} ... it was already closing or closed : {}", extent, e.getMessage());
        } else {
          log.error("Failed to close tablet {}... Aborting migration", extent, e);
          enqueueMasterMessage(new TabletStatusMessage(TabletLoadState.UNLOAD_ERROR, extent));
        }
        return;
      }
      recentlyUnloadedCache.put(extent, System.currentTimeMillis());
      onlineTablets.remove(extent);
      try {
        TServerInstance instance = new TServerInstance(clientAddress, getLock().getSessionId());
        TabletLocationState tls = null;
        try {
          tls = new TabletLocationState(extent, null, instance, null, null, null, false);
        } catch (BadLocationStateException e) {
          log.error("Unexpected error ", e);
        }
        if (!goalState.equals(TUnloadTabletGoal.SUSPENDED) || extent.isRootTablet() || (extent.isMeta() && !getConfiguration().getBoolean(Property.MASTER_METADATA_SUSPENDABLE))) {
          log.debug("Unassigning " + tls);
          TabletStateStore.unassign(TabletServer.this, tls, null);
        } else {
          log.debug("Suspending " + tls);
          TabletStateStore.suspend(TabletServer.this, tls, null, requestTimeSkew + MILLISECONDS.convert(System.nanoTime(), NANOSECONDS));
        }
      } catch (DistributedStoreException ex) {
        log.warn("Unable to update storage", ex);
      } catch (KeeperException e) {
        log.warn("Unable determine our zookeeper session information", e);
      } catch (InterruptedException e) {
        log.warn("Interrupted while getting our zookeeper session information", e);
      }
      enqueueMasterMessage(new TabletStatusMessage(TabletLoadState.UNLOADED, extent));
      statsKeeper.saveMajorMinorTimes(t.getTabletStats());
      log.info("unloaded " + extent);
    }
  }
>>>>>>> YOURS

    private static final Logger log = LoggerFactory.getLogger(TabletServer.class);

    private static final long TIME_BETWEEN_GC_CHECKS = 5000;

    private static final long TIME_BETWEEN_LOCATOR_CACHE_CLEARS = 60 * 60 * 1000;

    final GarbageCollectionLogger gcLogger = new GarbageCollectionLogger();

    final ZooCache masterLockCache;

    final TabletServerLogger logger;

    final TabletServerUpdateMetrics updateMetrics;

    final TabletServerScanMetrics scanMetrics;

    final TabletServerMinCMetrics mincMetrics;

    final CompactionExecutorsMetrics ceMetrics;

    public TabletServerScanMetrics getScanMetrics() {
        return scanMetrics;
    }

    public TabletServerMinCMetrics getMinCMetrics() {
        return mincMetrics;
    }

    private final LogSorter logSorter;

    private ReplicationWorker replWorker = null;

    final TabletStatsKeeper statsKeeper;

    private final AtomicInteger logIdGenerator = new AtomicInteger();

    private final AtomicLong flushCounter = new AtomicLong(0);

    private final AtomicLong syncCounter = new AtomicLong(0);

    final OnlineTablets onlineTablets = new OnlineTablets();

    final SortedSet<KeyExtent> unopenedTablets = Collections.synchronizedSortedSet(new TreeSet<>());

    final SortedSet<KeyExtent> openingTablets = Collections.synchronizedSortedSet(new TreeSet<>());

    final Map<KeyExtent, Long> recentlyUnloadedCache = Collections.synchronizedMap(new LRUMap<>(1000));

    final TabletServerResourceManager resourceManager;

    private final SecurityOperation security;

    private final BlockingDeque<MasterMessage> masterMessages = new LinkedBlockingDeque<>();

    HostAndPort clientAddress;

    private volatile boolean serverStopRequested = false;

    private volatile boolean shutdownComplete = false;

    private ZooLock tabletServerLock;

    private TServer server;

    private volatile TServer replServer;

    private DistributedWorkQueue bulkFailedCopyQ;

    private String lockID;

    public static final AtomicLong seekCount = new AtomicLong(0);

    private final AtomicLong totalMinorCompactions = new AtomicLong(0);

    private final ZooAuthenticationKeyWatcher authKeyWatcher;

    private final WalStateManager walMarker;

    final SessionManager sessionManager;

    private final AtomicLong totalQueuedMutationSize = new AtomicLong(0);

    private final ReentrantLock recoveryLock = new ReentrantLock(true);

    private ThriftClientHandler clientHandler;

    private final ServerBulkImportStatus bulkImportStatus = new ServerBulkImportStatus();

    private CompactionManager compactionManager;

    String getLockID() {
        return lockID;
    }

    void requestStop() {
        serverStopRequested = true;
    }

    private class SplitRunner implements Runnable {

        private final Tablet tablet;

        public SplitRunner(Tablet tablet) {
            this.tablet = tablet;
        }

        @Override
        public void run() {
            splitTablet(tablet);
        }
    }

    public long updateTotalQueuedMutationSize(long additionalMutationSize) {
        return totalQueuedMutationSize.addAndGet(additionalMutationSize);
    }

    public Tablet getOnlineTablet(KeyExtent extent) {
        return onlineTablets.snapshot().get(extent);
    }

    public VolumeManager getVolumeManager() {
        return getContext().getVolumeManager();
    }

    public Session getSession(long sessionId) {
        return sessionManager.getSession(sessionId);
    }

    public void executeSplit(Tablet tablet) {
        resourceManager.executeSplit(tablet.getExtent(), new SplitRunner(tablet));
    }

    private class MajorCompactor implements Runnable {

        public MajorCompactor(AccumuloConfiguration config) {
            CompactionWatcher.startWatching(config);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleepUninterruptibly(getConfiguration().getTimeInMillis(Property.TSERV_MAJC_DELAY), TimeUnit.MILLISECONDS);
                    List<DfsLogger> closedCopy;
                    synchronized (closedLogs) {
                        closedCopy = copyClosedLogs(closedLogs);
                    }
                    for (Entry<KeyExtent, Tablet> entry : getOnlineTablets().entrySet()) {
                        Tablet tablet = entry.getValue();
                        if (tablet.needsSplit()) {
                            executeSplit(tablet);
                            continue;
                        }
                        tablet.checkIfMinorCompactionNeededForLogs(closedCopy);
                            }
                } catch (Exception t) {
                    log.error("Unexpected exception in {}", Thread.currentThread().getName(), t);
                    sleepUninterruptibly(1, TimeUnit.SECONDS);
                }
            }
        }
    }

    private void splitTablet(Tablet tablet) {
        try {
            splitTablet(tablet, null);
        } catch (IOException e) {
            statsKeeper.updateTime(Operation.SPLIT, 0, true);
            log.error("split failed: {} for tablet {}", e.getMessage(), tablet.getExtent(), e);
        } catch (Exception e) {
            statsKeeper.updateTime(Operation.SPLIT, 0, true);
            log.error("Unknown error on split:", e);
        }
    }

    TreeMap<KeyExtent, TabletData> splitTablet(Tablet tablet, byte[] splitPoint) throws IOException {
        long t1 = System.currentTimeMillis();
        TreeMap<KeyExtent, TabletData> tabletInfo = tablet.split(splitPoint);
        if (tabletInfo == null) {
            return null;
        }
        log.info("Starting split: {}", tablet.getExtent());
        statsKeeper.incrementStatusSplit();
        long start = System.currentTimeMillis();
        Tablet[] newTablets = new Tablet[2];
        Entry<KeyExtent, TabletData> first = tabletInfo.firstEntry();
        TabletResourceManager newTrm0 = resourceManager.createTabletResourceManager(first.getKey(), getTableConfiguration(first.getKey()));
        newTablets[0] = new Tablet(TabletServer.this, first.getKey(), newTrm0, first.getValue());
        Entry<KeyExtent, TabletData> last = tabletInfo.lastEntry();
        TabletResourceManager newTrm1 = resourceManager.createTabletResourceManager(last.getKey(), getTableConfiguration(last.getKey()));
        newTablets[1] = new Tablet(TabletServer.this, last.getKey(), newTrm1, last.getValue());
        statsKeeper.saveMajorMinorTimes(tablet.getTabletStats());
        onlineTablets.split(tablet.getExtent(), newTablets[0], newTablets[1]);
        enqueueMasterMessage(new SplitReportMessage(tablet.getExtent(), newTablets[0].getExtent(), new Text("/" + newTablets[0].getDirName()), newTablets[1].getExtent(), new Text("/" + newTablets[1].getDirName())));
        statsKeeper.updateTime(Operation.SPLIT, start, false);
        long t2 = System.currentTimeMillis();
        log.info("Tablet split: {} size0 {} size1 {} time {}ms", tablet.getExtent(), newTablets[0].estimateTabletSize(), newTablets[1].estimateTabletSize(), (t2 - t1));
        return tabletInfo;
    }

    public void enqueueMasterMessage(MasterMessage m) {
        masterMessages.addLast(m);
    }

    void acquireRecoveryMemory(KeyExtent extent) {
        if (!extent.isMeta()) {
            recoveryLock.lock();
        }
    }

    void releaseRecoveryMemory(KeyExtent extent) {
        if (!extent.isMeta()) {
            recoveryLock.unlock();
        }
    }

    private HostAndPort startServer(AccumuloConfiguration conf, String address, TProcessor processor) throws UnknownHostException {
        Property maxMessageSizeProperty = (conf.get(Property.TSERV_MAX_MESSAGE_SIZE) != null ? Property.TSERV_MAX_MESSAGE_SIZE : Property.GENERAL_MAX_MESSAGE_SIZE);
        ServerAddress sp = TServerUtils.startServer(getMetricsSystem(), getContext(), address, Property.TSERV_CLIENTPORT, processor, this.getClass().getSimpleName(), "Thrift Client Server", Property.TSERV_PORTSEARCH, Property.TSERV_MINTHREADS, Property.TSERV_MINTHREADS_TIMEOUT, Property.TSERV_THREADCHECK, maxMessageSizeProperty);
        this.server = sp.server;
        return sp.address;
    }

    private HostAndPort getMasterAddress() {
        try {
            List<String> locations = getContext().getMasterLocations();
            if (locations.isEmpty()) {
                return null;
            }
            return HostAndPort.fromString(locations.get(0));
        } catch (Exception e) {
            log.warn("Failed to obtain master host " + e);
        }
        return null;
    }

    private MasterClientService.Client masterConnection(HostAndPort address) {
        try {
            if (address == null) {
                return null;
            }
            return ThriftUtil.getClient(new MasterClientService.Client.Factory(), address, getContext());
        } catch (Exception e) {
            log.warn("Issue with masterConnection (" + address + ") " + e, e);
        }
        return null;
    }

    private void returnMasterConnection(MasterClientService.Client client) {
        ThriftUtil.returnClient(client);
    }

    private HostAndPort startTabletClientService() throws UnknownHostException {
        clientHandler = new ThriftClientHandler(this);
        Iface rpcProxy = TraceUtil.wrapService(clientHandler);
        final Processor<Iface> processor;
        if (getContext().getThriftServerType() == ThriftServerType.SASL) {
            Iface tcredProxy = TCredentialsUpdatingWrapper.service(rpcProxy, ThriftClientHandler.class, getConfiguration());
            processor = new Processor<>(tcredProxy);
        } else {
            processor = new Processor<>(rpcProxy);
        }
        HostAndPort address = startServer(getConfiguration(), clientAddress.getHost(), processor);
        log.info("address = {}", address);
        return address;
    }

    private void startReplicationService() throws UnknownHostException {
        final ReplicationServicerHandler handler = new ReplicationServicerHandler(this);
        ReplicationServicer.Iface rpcProxy = TraceUtil.wrapService(handler);
        ReplicationServicer.Iface repl = TCredentialsUpdatingWrapper.service(rpcProxy, handler.getClass(), getConfiguration());
        ReplicationServicer.Processor<ReplicationServicer.Iface> processor = new ReplicationServicer.Processor<>(repl);
        Property maxMessageSizeProperty = getConfiguration().get(Property.TSERV_MAX_MESSAGE_SIZE) != null ? Property.TSERV_MAX_MESSAGE_SIZE : Property.GENERAL_MAX_MESSAGE_SIZE;
        ServerAddress sp = TServerUtils.startServer(getMetricsSystem(), getContext(), clientAddress.getHost(), Property.REPLICATION_RECEIPT_SERVICE_PORT, processor, "ReplicationServicerHandler", "Replication Servicer", Property.TSERV_PORTSEARCH, Property.REPLICATION_MIN_THREADS, null, Property.REPLICATION_THREADCHECK, maxMessageSizeProperty);
        this.replServer = sp.server;
        log.info("Started replication service on {}", sp.address);
        try {
            getContext().getZooReaderWriter().putPersistentData(getContext().getZooKeeperRoot() + ReplicationConstants.ZOO_TSERVERS + "/" + clientAddress, sp.address.toString().getBytes(UTF_8), NodeExistsPolicy.OVERWRITE);
        } catch (Exception e) {
            log.error("Could not advertise replication service port", e);
            throw new RuntimeException(e);
        }
    }

    public ZooLock getLock() {
        return tabletServerLock;
    }

    private void announceExistence() {
        ZooReaderWriter zoo = getContext().getZooReaderWriter();
        try {
            String zPath = getContext().getZooKeeperRoot() + Constants.ZTSERVERS + "/" + getClientAddressString();
            try {
                zoo.putPersistentData(zPath, new byte[] {}, NodeExistsPolicy.SKIP);
            } catch (KeeperException e) {
                if (e.code() == KeeperException.Code.NOAUTH) {
                    log.error("Failed to write to ZooKeeper. Ensure that" + " accumulo.properties, specifically instance.secret, is consistent.");
                }
                throw e;
            }
            tabletServerLock = new ZooLock(zoo, zPath);
            LockWatcher lw = new LockWatcher() {

                @Override
                public void lostLock(final LockLossReason reason) {
                    Halt.halt(serverStopRequested ? 0 : 1, () -> {
                        if (!serverStopRequested) {
                            log.error("Lost tablet server lock (reason = {}), exiting.", reason);
                        }
                            gcLogger.logGCInfo(getConfiguration());
                    });
                }

                @Override
                public void unableToMonitorLockNode(final Exception e) {
                    Halt.halt(1, () -> log.error("Lost ability to monitor tablet server lock, exiting.", e));
                }
            };
            byte[] lockContent = new ServerServices(getClientAddressString(), Service.TSERV_CLIENT).toString().getBytes(UTF_8);
            for (int i = 0; i < 120 / 5; i++) {
                zoo.putPersistentData(zPath, new byte[0], NodeExistsPolicy.SKIP);
                if (tabletServerLock.tryLock(lw, lockContent)) {
                    log.debug("Obtained tablet server lock {}", tabletServerLock.getLockPath());
                    lockID = tabletServerLock.getLockID().serialize(getContext().getZooKeeperRoot() + Constants.ZTSERVERS + "/");
                    return;
                }
                log.info("Waiting for tablet server lock");
                sleepUninterruptibly(5, TimeUnit.SECONDS);
            }
            String msg = "Too many retries, exiting.";
            log.info(msg);
            throw new RuntimeException(msg);
        } catch (Exception e) {
            log.info("Could not obtain tablet server lock, exiting.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        SecurityUtil.serverLogin(getConfiguration());
        try {
            ZooKeeperInitialization.ensureZooKeeperInitialized(getContext().getZooReaderWriter(), getContext().getZooKeeperRoot());
        } catch (KeeperException | InterruptedException e) {
            log.error("Could not ensure that ZooKeeper is properly initialized", e);
            throw new RuntimeException(e);
        }
        try {
            MetricsSystem metricsSystem = getMetricsSystem();
            new TabletServerMetrics(this).register(metricsSystem);
            mincMetrics.register(metricsSystem);
            scanMetrics.register(metricsSystem);
            updateMetrics.register(metricsSystem);
            ceMetrics.register(metricsSystem);
        } catch (Exception e) {
            log.error("Error registering metrics", e);
        }
        if (authKeyWatcher != null) {
            log.info("Seeding ZooKeeper watcher for authentication keys");
            try {
                authKeyWatcher.updateAuthKeys();
            } catch (KeeperException | InterruptedException e) {
                log.error("Failed to perform initial check for authentication tokens in" + " ZooKeeper. Delegation token authentication will be unavailable.", e);
            }
        }
        this.compactionManager = new CompactionManager(new Iterable<Compactable>() {

            @Override
            public Iterator<Compactable> iterator() {
                return Iterators.transform(onlineTablets.snapshot().values().iterator(), Tablet::asCompactable);
            }
        }, getContext(), ceMetrics);
        compactionManager.start();
        try {
            clientAddress = startTabletClientService();
        } catch (UnknownHostException e1) {
            throw new RuntimeException("Failed to start the tablet client service", e1);
        }
        announceExistence();
        try {
            walMarker.initWalMarker(getTabletSession());
        } catch (Exception e) {
            log.error("Unable to create WAL marker node in zookeeper", e);
            throw new RuntimeException(e);
        }
        ThreadPoolExecutor distWorkQThreadPool = (ThreadPoolExecutor) ThreadPools.createExecutorService(getConfiguration(), Property.TSERV_WORKQ_THREADS);
        bulkFailedCopyQ = new DistributedWorkQueue(getContext().getZooKeeperRoot() + Constants.ZBULK_FAILED_COPYQ, getConfiguration());
        try {
            bulkFailedCopyQ.startProcessing(new BulkFailedCopyProcessor(getContext()), distWorkQThreadPool);
        } catch (Exception e1) {
            throw new RuntimeException("Failed to start distributed work queue for copying ", e1);
        }
        try {
            logSorter.startWatchingForRecoveryLogs(distWorkQThreadPool);
        } catch (Exception ex) {
            log.error("Error setting watches for recoveries");
            throw new RuntimeException(ex);
        }
        final AccumuloConfiguration aconf = getConfiguration();
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(() -> {
            if (this.replServer == null) {
                if (!getConfiguration().get(Property.REPLICATION_NAME).isEmpty()) {
                    log.info(Property.REPLICATION_NAME.getKey() + " was set, starting repl services.");
                    setupReplication(aconf);
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
        final long CLEANUP_BULK_LOADED_CACHE_MILLIS = 15 * 60 * 1000;
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(new BulkImportCacheCleaner(this), CLEANUP_BULK_LOADED_CACHE_MILLIS, CLEANUP_BULK_LOADED_CACHE_MILLIS, TimeUnit.MILLISECONDS);
        HostAndPort masterHost;
        while (!serverStopRequested) {
            try {
                MasterMessage mm = null;
                MasterClientService.Client iface = null;
                try {
                    while (mm == null && !serverStopRequested) {
                        mm = masterMessages.poll(1000, TimeUnit.MILLISECONDS);
                    }
                    masterHost = getMasterAddress();
                    iface = masterConnection(masterHost);
                    TServiceClient client = iface;
                    while (!serverStopRequested && mm != null && client != null && client.getOutputProtocol() != null && client.getOutputProtocol().getTransport() != null && client.getOutputProtocol().getTransport().isOpen()) {
                        try {
                            mm.send(getContext().rpcCreds(), getClientAddressString(), iface);
                            mm = null;
                        } catch (TException ex) {
                            log.warn("Error sending message: queuing message again");
                            masterMessages.putFirst(mm);
                            mm = null;
                            throw ex;
                        }
                        mm = masterMessages.poll();
                    }
                } finally {
                    if (mm != null) {
                        masterMessages.putFirst(mm);
                    }
                    returnMasterConnection(iface);
                    sleepUninterruptibly(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                log.info("Interrupt Exception received, shutting down");
                serverStopRequested = true;
            } catch (Exception e) {
                log.error(getClientAddressString() + ": TServerInfo: Exception. Master down?", e);
            }
        }
        synchronized (this) {
            while (!shutdownComplete) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }
            }
        }
        log.debug("Stopping Replication Server");
        TServerUtils.stopTServer(this.replServer);
        log.debug("Stopping Thrift Servers");
        TServerUtils.stopTServer(server);
        try {
            log.debug("Closing filesystems");
            getVolumeManager().close();
        } catch (IOException e) {
            log.warn("Failed to close filesystem : {}", e.getMessage(), e);
        }
        gcLogger.logGCInfo(getConfiguration());
        log.info("TServerInfo: stop requested. exiting ... ");
        try {
            tabletServerLock.unlock();
        } catch (Exception e) {
            log.warn("Failed to release tablet server lock", e);
        }
    }

    private void setupReplication(AccumuloConfiguration aconf) {
        try {
            startReplicationService();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to start replication service", e);
        }
        final ThreadPoolExecutor replicationThreadPool = (ThreadPoolExecutor) ThreadPools.createExecutorService(getConfiguration(), Property.REPLICATION_WORKER_THREADS);
        replWorker.setExecutor(replicationThreadPool);
        replWorker.run();
        Runnable replicationWorkThreadPoolResizer = () -> {
            int maxPoolSize = aconf.getCount(Property.REPLICATION_WORKER_THREADS);
            if (replicationThreadPool.getMaximumPoolSize() != maxPoolSize) {
                log.info("Resizing thread pool for sending replication work from {} to {}", replicationThreadPool.getMaximumPoolSize(), maxPoolSize);
                replicationThreadPool.setMaximumPoolSize(maxPoolSize);
            }
        };
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(replicationWorkThreadPoolResizer, 10000, 30000, TimeUnit.MILLISECONDS);
    }

    static boolean checkTabletMetadata(KeyExtent extent, TServerInstance instance, TabletMetadata meta) throws AccumuloException {
        if (!meta.sawPrevEndRow()) {
            throw new AccumuloException("Metadata entry does not have prev row (" + meta.getTableId() + " " + meta.getEndRow() + ")");
        }
        if (!extent.equals(meta.getExtent())) {
            log.info("Tablet extent mismatch {} {}", extent, meta.getExtent());
            return false;
        }
        if (meta.getDirName() == null) {
            throw new AccumuloException("Metadata entry does not have directory (" + meta.getExtent() + ")");
        }
        if (meta.getTime() == null && !extent.equals(RootTable.EXTENT)) {
            throw new AccumuloException("Metadata entry does not have time (" + meta.getExtent() + ")");
        }
        Location loc = meta.getLocation();
        if (loc == null || loc.getType() != LocationType.FUTURE || !instance.equals(loc)) {
            log.info("Unexpected location {} {}", extent, loc);
            return false;
        }
        return true;
    }

    public String getClientAddressString() {
        if (clientAddress == null) {
            return null;
        }
        return clientAddress.getHost() + ":" + clientAddress.getPort();
    }

    public TServerInstance getTabletSession() {
        String address = getClientAddressString();
        if (address == null) {
            return null;
        }
        try {
            return new TServerInstance(address, tabletServerLock.getSessionId());
        } catch (Exception ex) {
            log.warn("Unable to read session from tablet server lock" + ex);
            return null;
        }
    }

    private static void checkWalCanSync(ServerContext context) {
        VolumeChooserEnvironment chooserEnv = new VolumeChooserEnvironmentImpl(VolumeChooserEnvironment.ChooserScope.LOGGER, context);
        Set<String> prefixes;
        var options = ServerConstants.getBaseUris(context);
        try {
            prefixes = context.getVolumeManager().choosable(chooserEnv, options);
        } catch (RuntimeException e) {
            log.warn("Unable to determine if WAL directories ({}) support sync or flush. " + "Data loss may occur.", Arrays.asList(options), e);
            return;
        }
        boolean warned = false;
        for (String prefix : prefixes) {
            String logPath = prefix + Path.SEPARATOR + ServerConstants.WAL_DIR;
            if (!context.getVolumeManager().canSyncAndFlush(new Path(logPath))) {
                if (!warned) {
                    UtilWaitThread.sleep(5000);
                    warned = true;
                }
                log.warn("WAL directory ({}) implementation does not support sync or flush." + " Data loss may occur.", logPath);
            }
        }
    }

    private void config() {
        log.info("Tablet server starting on {}", getHostname());
        Threads.createThread("Split/MajC initiator", new MajorCompactor(getConfiguration())).start();
        clientAddress = HostAndPort.fromParts(getHostname(), 0);
        final AccumuloConfiguration aconf = getConfiguration();
        FileSystemMonitor.start(aconf, Property.TSERV_MONITOR_FS);
        Runnable gcDebugTask = () -> gcLogger.logGCInfo(getConfiguration());
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(gcDebugTask, 0, TIME_BETWEEN_GC_CHECKS, TimeUnit.MILLISECONDS);
    }

    public TabletServerStatus getStats(Map<TableId, MapCounter<ScanRunState>> scanCounts) {
        long start = System.currentTimeMillis();
        TabletServerStatus result = new TabletServerStatus();
        final Map<String, TableInfo> tables = new HashMap<>();
        getOnlineTablets().forEach((ke, tablet) -> {
            String tableId = ke.tableId().canonical();
            TableInfo table = tables.get(tableId);
            if (table == null) {
                table = new TableInfo();
                table.minors = new Compacting();
                table.majors = new Compacting();
                tables.put(tableId, table);
            }
            long recs = tablet.getNumEntries();
            table.tablets++;
            table.onlineTablets++;
            table.recs += recs;
            table.queryRate += tablet.queryRate();
            table.queryByteRate += tablet.queryByteRate();
            table.ingestRate += tablet.ingestRate();
            table.ingestByteRate += tablet.ingestByteRate();
            table.scanRate += tablet.scanRate();
            long recsInMemory = tablet.getNumEntriesInMemory();
            table.recsInMemory += recsInMemory;
            if (tablet.isMinorCompactionRunning()) {
                table.minors.running++;
            }
            if (tablet.isMinorCompactionQueued()) {
                table.minors.queued++;
            }
            if (tablet.isMajorCompactionRunning()) {
                table.majors.running++;
            }
            if (tablet.isMajorCompactionQueued()) {
                table.majors.queued++;
        }
        });
        scanCounts.forEach((tableId, mapCounter) -> {
            TableInfo table = tables.get(tableId.canonical());
            if (table == null) {
                table = new TableInfo();
                tables.put(tableId.canonical(), table);
            }
            if (table.scans == null) {
                table.scans = new Compacting();
        }
            table.scans.queued += mapCounter.getInt(ScanRunState.QUEUED);
            table.scans.running += mapCounter.getInt(ScanRunState.RUNNING);
        });
        ArrayList<KeyExtent> offlineTabletsCopy = new ArrayList<>();
        synchronized (this.unopenedTablets) {
            synchronized (this.openingTablets) {
                offlineTabletsCopy.addAll(this.unopenedTablets);
                offlineTabletsCopy.addAll(this.openingTablets);
            }
        }
        for (KeyExtent extent : offlineTabletsCopy) {
            String tableId = extent.tableId().canonical();
            TableInfo table = tables.get(tableId);
            if (table == null) {
                table = new TableInfo();
                tables.put(tableId, table);
            }
            table.tablets++;
        }
        result.lastContact = RelativeTime.currentTimeMillis();
        result.tableMap = tables;
        result.osLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        result.name = getClientAddressString();
        result.holdTime = resourceManager.holdTime();
        result.lookups = seekCount.get();
        result.indexCacheHits = resourceManager.getIndexCache().getStats().hitCount();
        result.indexCacheRequest = resourceManager.getIndexCache().getStats().requestCount();
        result.dataCacheHits = resourceManager.getDataCache().getStats().hitCount();
        result.dataCacheRequest = resourceManager.getDataCache().getStats().requestCount();
        result.logSorts = logSorter.getLogSorts();
        result.flushs = flushCounter.get();
        result.syncs = syncCounter.get();
        result.bulkImports = new ArrayList<>();
        result.bulkImports.addAll(clientHandler.getBulkLoadStatus());
        result.bulkImports.addAll(bulkImportStatus.getBulkLoadStatus());
        result.version = getVersion();
        result.responseTime = System.currentTimeMillis() - start;
        return result;
    }

    public static void main(String[] args) throws Exception {
        try (TabletServer tserver = new TabletServer(new ServerOpts(), args)) {
            tserver.runServer();
        }
    }

    TabletServer(ServerOpts opts, String[] args) {
        super("tserver", opts, args);
        ServerContext context = super.getContext();
        context.setupCrypto();
        this.masterLockCache = new ZooCache(context.getZooReaderWriter(), null);
        final AccumuloConfiguration aconf = getConfiguration();
        log.info("Version " + Constants.VERSION);
        log.info("Instance " + getInstanceID());
        this.sessionManager = new SessionManager(aconf);
        this.logSorter = new LogSorter(context, aconf);
        this.replWorker = new ReplicationWorker(context);
        this.statsKeeper = new TabletStatsKeeper();
        final int numBusyTabletsToLog = aconf.getCount(Property.TSERV_LOG_BUSY_TABLETS_COUNT);
        final long logBusyTabletsDelay = aconf.getTimeInMillis(Property.TSERV_LOG_BUSY_TABLETS_INTERVAL);
        checkWalCanSync(context);
        if (numBusyTabletsToLog > 0) {
            ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(Threads.createNamedRunnable("BusyTabletLogger", new Runnable() {

                private BusiestTracker ingestTracker = BusiestTracker.newBusiestIngestTracker(numBusyTabletsToLog);

                private BusiestTracker queryTracker = BusiestTracker.newBusiestQueryTracker(numBusyTabletsToLog);

                @Override
                public void run() {
                    Collection<Tablet> tablets = onlineTablets.snapshot().values();
                    logBusyTablets(ingestTracker.computeBusiest(tablets), "ingest count");
                    logBusyTablets(queryTracker.computeBusiest(tablets), "query count");
                }

                private void logBusyTablets(List<ComparablePair<Long, KeyExtent>> busyTablets, String label) {
                    int i = 1;
                    for (Pair<Long, KeyExtent> pair : busyTablets) {
                        log.debug("{} busiest tablet by {}: {} -- extent: {} ", i, label.toLowerCase(), pair.getFirst(), pair.getSecond());
                        i++;
                    }
                }
            }), logBusyTabletsDelay, logBusyTabletsDelay, TimeUnit.MILLISECONDS);
        }
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(Threads.createNamedRunnable("TabletRateUpdater", new Runnable() {

            @Override
            public void run() {
                    long now = System.currentTimeMillis();
                for (Tablet tablet : getOnlineTablets().values()) {
                    try {
                        tablet.updateRates(now);
                    } catch (Exception ex) {
                        log.error("Error updating rates for {}", tablet.getExtent(), ex);
                    }
                }
            }
        }), 5000, 5000, TimeUnit.MILLISECONDS);
        final long walogMaxSize = aconf.getAsBytes(Property.TSERV_WALOG_MAX_SIZE);
        final long walogMaxAge = aconf.getTimeInMillis(Property.TSERV_WALOG_MAX_AGE);
        final long minBlockSize = context.getHadoopConf().getLong("dfs.namenode.fs-limits.min-block-size", 0);
        if (minBlockSize != 0 && minBlockSize > walogMaxSize) {
            throw new RuntimeException("Unable to start TabletServer. Logger is set to use blocksize " + walogMaxSize + " but hdfs minimum block size is " + minBlockSize + ". Either increase the " + Property.TSERV_WALOG_MAX_SIZE + " or decrease dfs.namenode.fs-limits.min-block-size in hdfs-site.xml.");
        }
        final long toleratedWalCreationFailures = aconf.getCount(Property.TSERV_WALOG_TOLERATED_CREATION_FAILURES);
        final long walFailureRetryIncrement = aconf.getTimeInMillis(Property.TSERV_WALOG_TOLERATED_WAIT_INCREMENT);
        final long walFailureRetryMax = aconf.getTimeInMillis(Property.TSERV_WALOG_TOLERATED_MAXIMUM_WAIT_DURATION);
        final RetryFactory walCreationRetryFactory = Retry.builder().maxRetries(toleratedWalCreationFailures).retryAfter(walFailureRetryIncrement, TimeUnit.MILLISECONDS).incrementBy(walFailureRetryIncrement, TimeUnit.MILLISECONDS).maxWait(walFailureRetryMax, TimeUnit.MILLISECONDS).backOffFactor(1.5).logInterval(3, TimeUnit.MINUTES).createFactory();
        final RetryFactory walWritingRetryFactory = Retry.builder().infiniteRetries().retryAfter(walFailureRetryIncrement, TimeUnit.MILLISECONDS).incrementBy(walFailureRetryIncrement, TimeUnit.MILLISECONDS).maxWait(walFailureRetryMax, TimeUnit.MILLISECONDS).backOffFactor(1.5).logInterval(3, TimeUnit.MINUTES).createFactory();
        logger = new TabletServerLogger(this, walogMaxSize, syncCounter, flushCounter, walCreationRetryFactory, walWritingRetryFactory, walogMaxAge);
        this.resourceManager = new TabletServerResourceManager(context);
        this.security = AuditedSecurityOperation.getInstance(context);
        updateMetrics = new TabletServerUpdateMetrics();
        scanMetrics = new TabletServerScanMetrics();
        mincMetrics = new TabletServerMinCMetrics();
        ceMetrics = new CompactionExecutorsMetrics();
        ThreadPools.createGeneralScheduledExecutorService(aconf).scheduleWithFixedDelay(TabletLocator::clearLocators, jitter(), jitter(), TimeUnit.MILLISECONDS);
        walMarker = new WalStateManager(context);
        context.setSecretManager(new AuthenticationTokenSecretManager(context.getInstanceID(), aconf.getTimeInMillis(Property.GENERAL_DELEGATION_TOKEN_LIFETIME)));
        if (aconf.getBoolean(Property.INSTANCE_RPC_SASL_ENABLED)) {
            log.info("SASL is enabled, creating ZooKeeper watcher for AuthenticationKeys");
            authKeyWatcher = new ZooAuthenticationKeyWatcher(context.getSecretManager(), context.getZooReaderWriter(), context.getZooKeeperRoot() + Constants.ZDELEGATION_TOKEN_KEYS);
        } else {
            authKeyWatcher = null;
        }
        config();
    }

    public String getInstanceID() {
        return getContext().getInstanceID();
    }

    public String getVersion() {
        return Constants.VERSION;
    }

    private static long jitter() {
        Random r = new SecureRandom();
        return (long) ((1. + (r.nextDouble() / 10)) * TabletServer.TIME_BETWEEN_LOCATOR_CACHE_CLEARS);
    }

    private Durability getMincEventDurability(KeyExtent extent) {
        TableConfiguration conf;
        if (extent.isMeta()) {
            conf = getContext().getTableConfiguration(RootTable.ID);
        } else {
            conf = getContext().getTableConfiguration(MetadataTable.ID);
        }
        return DurabilityImpl.fromString(conf.get(Property.TABLE_DURABILITY));
    }

    public void minorCompactionFinished(CommitSession tablet, long walogSeq) throws IOException {
        Durability durability = getMincEventDurability(tablet.getExtent());
        totalMinorCompactions.incrementAndGet();
        logger.minorCompactionFinished(tablet, walogSeq, durability);
        markUnusedWALs();
    }

    public void minorCompactionStarted(CommitSession tablet, long lastUpdateSequence, String newMapfileLocation) throws IOException {
        Durability durability = getMincEventDurability(tablet.getExtent());
        logger.minorCompactionStarted(tablet, lastUpdateSequence, newMapfileLocation, durability);
    }

    public void recover(VolumeManager fs, KeyExtent extent, List<LogEntry> logEntries, Set<String> tabletFiles, MutationReceiver mutationReceiver) throws IOException {
        List<Path> recoveryLogs = new ArrayList<>();
        List<LogEntry> sorted = new ArrayList<>(logEntries);
        sorted.sort((e1, e2) -> (int) (e1.timestamp - e2.timestamp));
        for (LogEntry entry : sorted) {
            Path recovery = null;
            Path finished = RecoveryPath.getRecoveryPath(new Path(entry.filename));
            finished = SortedLogState.getFinishedMarkerPath(finished);
            TabletServer.log.debug("Looking for " + finished);
            if (fs.exists(finished)) {
                recovery = finished.getParent();
            }
            if (recovery == null) {
                throw new IOException("Unable to find recovery files for extent " + extent + " logEntry: " + entry);
            }
            recoveryLogs.add(recovery);
        }
        logger.recover(fs, extent, recoveryLogs, tabletFiles, mutationReceiver);
    }

    public int createLogId() {
        int logId = logIdGenerator.incrementAndGet();
        if (logId < 0) {
            throw new IllegalStateException("Log Id rolled");
        }
        return logId;
    }

    public TableConfiguration getTableConfiguration(KeyExtent extent) {
        return getContext().getTableConfiguration(extent.tableId());
    }

    public DfsLogger.ServerResources getServerConfig() {
        return new DfsLogger.ServerResources() {

            @Override
            public VolumeManager getVolumeManager() {
                return TabletServer.this.getVolumeManager();
            }

            @Override
            public AccumuloConfiguration getConfiguration() {
                return TabletServer.this.getConfiguration();
            }
        };
    }

    public SortedMap<KeyExtent, Tablet> getOnlineTablets() {
        return onlineTablets.snapshot();
    }

    public int getOpeningCount() {
        return openingTablets.size();
    }

    public int getUnopenedCount() {
        return unopenedTablets.size();
    }

    public long getTotalMinorCompactions() {
        return totalMinorCompactions.get();
    }

    public double getHoldTimeMillis() {
        return resourceManager.holdTime();
    }

    public SecurityOperation getSecurityOperation() {
        return security;
    }

    final ConcurrentHashMap<DfsLogger, EnumSet<TabletLevel>> metadataTableLogs = new ConcurrentHashMap<>();

    {
        for (int i = 0; i < levelLocks.length; i++) {
            levelLocks[i] = new Object();
        }
    }

    LinkedHashSet<DfsLogger> closedLogs = new LinkedHashSet<>();

    @VisibleForTesting
    interface ReferencedRemover {

        void removeInUse(Set<DfsLogger> candidates);
    }

    @VisibleForTesting
    static Set<DfsLogger> findOldestUnreferencedWals(List<DfsLogger> closedLogs, ReferencedRemover referencedRemover) {
        LinkedHashSet<DfsLogger> unreferenced = new LinkedHashSet<>(closedLogs);
        referencedRemover.removeInUse(unreferenced);
        Iterator<DfsLogger> closedIter = closedLogs.iterator();
        Iterator<DfsLogger> unrefIter = unreferenced.iterator();
        Set<DfsLogger> eligible = new HashSet<>();
        while (closedIter.hasNext() && unrefIter.hasNext()) {
            DfsLogger closed = closedIter.next();
            DfsLogger unref = unrefIter.next();
            if (closed.equals(unref)) {
                eligible.add(unref);
            } else {
                break;
            }
        }
        return eligible;
    }

    @VisibleForTesting
    static List<DfsLogger> copyClosedLogs(LinkedHashSet<DfsLogger> closedLogs) {
        List<DfsLogger> closedCopy = new ArrayList<>(closedLogs.size());
        for (DfsLogger dfsLogger : closedLogs) {
            closedCopy.add(dfsLogger);
        }
        return Collections.unmodifiableList(closedCopy);
    }

    private void markUnusedWALs() {
        List<DfsLogger> closedCopy;
        synchronized (closedLogs) {
            closedCopy = copyClosedLogs(closedLogs);
        }
        ReferencedRemover refRemover = candidates -> {
            for (Tablet tablet : getOnlineTablets().values()) {
                    tablet.removeInUseLogs(candidates);
                    if (candidates.isEmpty()) {
                        break;
                    }
                }
        };
        Set<DfsLogger> eligible = findOldestUnreferencedWals(closedCopy, refRemover);
        try {
            TServerInstance session = this.getTabletSession();
            for (DfsLogger candidate : eligible) {
                log.info("Marking " + candidate.getPath() + " as unreferenced");
                walMarker.walUnreferenced(session, candidate.getPath());
            }
            synchronized (closedLogs) {
                closedLogs.removeAll(eligible);
            }
        } catch (WalMarkerException ex) {
            log.info(ex.toString(), ex);
        }
    }

    public void addNewLogMarker(DfsLogger copy) throws WalMarkerException {
        log.info("Writing log marker for " + copy.getPath());
        walMarker.addNewWalMarker(getTabletSession(), copy.getPath());
    }

    public void walogClosed(DfsLogger currentLog) throws WalMarkerException {
        metadataTableLogs.remove(currentLog);
        if (currentLog.getWrites() > 0) {
            int clSize;
            synchronized (closedLogs) {
                closedLogs.add(currentLog);
                clSize = closedLogs.size();
            }
            log.info("Marking " + currentLog.getPath() + " as closed. Total closed logs " + clSize);
            walMarker.closeWal(getTabletSession(), currentLog.getPath());
        } else {
            log.info("Marking " + currentLog.getPath() + " as unreferenced (skipping closed writes == 0)");
            walMarker.walUnreferenced(getTabletSession(), currentLog.getPath());
        }
    }

    public void updateBulkImportState(List<String> files, BulkImportState state) {
        bulkImportStatus.updateBulkImportStatus(files, state);
    }

    public void removeBulkImportState(List<String> files) {
        bulkImportStatus.removeBulkImportStatus(files);
    }

    public CompactionManager getCompactionManager() {
        return compactionManager;
    }
}