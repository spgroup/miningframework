package org.apache.cassandra.db;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.management.*;
import javax.management.openmbean.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.*;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.cache.*;
import org.apache.cassandra.concurrent.*;
import org.apache.cassandra.config.*;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.commitlog.CommitLogPosition;
import org.apache.cassandra.db.compaction.*;
import org.apache.cassandra.db.filter.ClusteringIndexFilter;
import org.apache.cassandra.db.filter.DataLimits;
import org.apache.cassandra.db.streaming.CassandraStreamManager;
import org.apache.cassandra.db.repair.CassandraTableRepairManager;
import org.apache.cassandra.db.view.TableViews;
import org.apache.cassandra.db.lifecycle.*;
import org.apache.cassandra.db.partitions.CachedPartition;
import org.apache.cassandra.db.partitions.PartitionUpdate;
import org.apache.cassandra.db.rows.CellPath;
import org.apache.cassandra.dht.*;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.exceptions.StartupException;
import org.apache.cassandra.index.SecondaryIndexManager;
import org.apache.cassandra.index.internal.CassandraIndex;
import org.apache.cassandra.index.transactions.UpdateTransaction;
import org.apache.cassandra.io.FSReadError;
import org.apache.cassandra.io.FSWriteError;
import org.apache.cassandra.io.sstable.Component;
import org.apache.cassandra.io.sstable.Descriptor;
import org.apache.cassandra.io.sstable.SSTableMultiWriter;
import org.apache.cassandra.io.sstable.format.*;
import org.apache.cassandra.io.sstable.metadata.MetadataCollector;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.metrics.Sampler;
import org.apache.cassandra.metrics.Sampler.Sample;
import org.apache.cassandra.metrics.Sampler.SamplerType;
import org.apache.cassandra.metrics.TableMetrics;
import org.apache.cassandra.repair.TableRepairManager;
import org.apache.cassandra.repair.consistent.admin.CleanupSummary;
import org.apache.cassandra.repair.consistent.admin.PendingStat;
import org.apache.cassandra.schema.*;
import org.apache.cassandra.schema.CompactionParams.TombstoneOption;
import org.apache.cassandra.service.ActiveRepairService;
import org.apache.cassandra.service.CacheService;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.streaming.TableStreamManager;
import org.apache.cassandra.utils.*;
import org.apache.cassandra.utils.concurrent.OpOrder;
import org.apache.cassandra.utils.concurrent.Refs;
import org.apache.cassandra.utils.memory.MemtableAllocator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.cassandra.utils.Throwables.maybeFail;
import static org.apache.cassandra.utils.Throwables.merge;

public class ColumnFamilyStore implements ColumnFamilyStoreMBean {

    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilyStore.class);

    private static final ExecutorService flushExecutor = new JMXEnabledThreadPoolExecutor(DatabaseDescriptor.getFlushWriters(), Stage.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MemtableFlushWriter"), "internal");

    private static final ExecutorService[] perDiskflushExecutors = new ExecutorService[DatabaseDescriptor.getAllDataFileLocations().length];

    static {
        for (int i = 0; i < DatabaseDescriptor.getAllDataFileLocations().length; i++) {
<<<<<<< MINE
            perDiskflushExecutors[i] = new JMXEnabledThreadPoolExecutor(DatabaseDescriptor.getFlushWriters(), Stage.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("PerDiskMemtableFlushWriter_" + i), "internal");
=======
            perDiskflushExecutors[i] = new JMXEnabledThreadPoolExecutor(DatabaseDescriptor.getFlushWriters(), StageManager.KEEPALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("PerDiskMemtableFlushWriter_" + i), "internal");
>>>>>>> YOURS
        }
    }

    private static final ExecutorService postFlushExecutor = new JMXEnabledThreadPoolExecutor(1, Stage.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MemtablePostFlush"), "internal");

    private static final ExecutorService reclaimExecutor = new JMXEnabledThreadPoolExecutor(1, Stage.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MemtableReclaimMemory"), "internal");

    private static final String[] COUNTER_NAMES = new String[] { "table", "count", "error", "value" };

    private static final String[] COUNTER_DESCS = new String[] { "keyspace.tablename", "number of occurances", "error bounds", "value" };

    private static final CompositeType COUNTER_COMPOSITE_TYPE;

    private static final String SAMPLING_RESULTS_NAME = "SAMPLING_RESULTS";

    public static final String SNAPSHOT_TRUNCATE_PREFIX = "truncated";

    public static final String SNAPSHOT_DROP_PREFIX = "dropped";

    static {
        try {
            OpenType<?>[] counterTypes = new OpenType[] { SimpleType.STRING, SimpleType.LONG, SimpleType.LONG, SimpleType.STRING };
            COUNTER_COMPOSITE_TYPE = new CompositeType(SAMPLING_RESULTS_NAME, SAMPLING_RESULTS_NAME, COUNTER_NAMES, COUNTER_DESCS, counterTypes);
        } catch (OpenDataException e) {
            throw new RuntimeException(e);
        }
    }

    public final Keyspace keyspace;

    public final String name;

    public final TableMetadataRef metadata;

    private final String mbeanName;

    @Deprecated
    private final String oldMBeanName;

    private volatile boolean valid = true;

    private final Tracker data;

    public final OpOrder readOrdering = new OpOrder();

    private final AtomicInteger fileIndexGenerator = new AtomicInteger(0);

    public final SecondaryIndexManager indexManager;

    public final TableViews viewManager;

    private volatile DefaultValue<Integer> minCompactionThreshold;

    private volatile DefaultValue<Integer> maxCompactionThreshold;

    private volatile DefaultValue<Double> crcCheckChance;

    private final CompactionStrategyManager compactionStrategyManager;

    private final Directories directories;

    public final TableMetrics metric;

    public volatile long sampleReadLatencyNanos;

    public volatile long additionalWriteLatencyNanos;

    private final CassandraTableWriteHandler writeHandler;

    private final CassandraStreamManager streamManager;

    private final TableRepairManager repairManager;

    private final SSTableImporter sstableImporter;

    private volatile boolean compactionSpaceCheck = true;

    @VisibleForTesting
    final DiskBoundaryManager diskBoundaryManager = new DiskBoundaryManager();

    private volatile boolean neverPurgeTombstones = false;

    public static void shutdownFlushExecutor() throws InterruptedException {
        flushExecutor.shutdown();
        flushExecutor.awaitTermination(60, TimeUnit.SECONDS);
    }

    public static void shutdownPostFlushExecutor() throws InterruptedException {
        postFlushExecutor.shutdown();
        postFlushExecutor.awaitTermination(60, TimeUnit.SECONDS);
    }

    public static void shutdownExecutorsAndWait(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        List<ExecutorService> executors = new ArrayList<>(perDiskflushExecutors.length + 3);
        Collections.addAll(executors, reclaimExecutor, postFlushExecutor, flushExecutor);
        Collections.addAll(executors, perDiskflushExecutors);
        ExecutorUtils.shutdownAndWait(timeout, unit, executors);
    }

    public void reload() {
        if (!minCompactionThreshold.isModified())
            for (ColumnFamilyStore cfs : concatWithIndexes()) cfs.minCompactionThreshold = new DefaultValue(metadata().params.compaction.minCompactionThreshold());
        if (!maxCompactionThreshold.isModified())
            for (ColumnFamilyStore cfs : concatWithIndexes()) cfs.maxCompactionThreshold = new DefaultValue(metadata().params.compaction.maxCompactionThreshold());
        if (!crcCheckChance.isModified())
            for (ColumnFamilyStore cfs : concatWithIndexes()) cfs.crcCheckChance = new DefaultValue(metadata().params.crcCheckChance);
        compactionStrategyManager.maybeReload(metadata());
        scheduleFlush();
        indexManager.reload();
        if (data.getView().getCurrentMemtable().initialComparator != metadata().comparator)
            switchMemtable();
    }

    void scheduleFlush() {
        int period = metadata().params.memtableFlushPeriodInMs;
        if (period > 0) {
            logger.trace("scheduling flush in {} ms", period);
            WrappedRunnable runnable = new WrappedRunnable() {

                protected void runMayThrow() {
                    synchronized (data) {
                        Memtable current = data.getView().getCurrentMemtable();
                        if (current.isExpired()) {
                            if (current.isClean()) {
                                scheduleFlush();
                            } else {
                                forceFlush();
                            }
                        }
                    }
                }
            };
            ScheduledExecutors.scheduledTasks.schedule(runnable, period, TimeUnit.MILLISECONDS);
        }
    }

    public static Runnable getBackgroundCompactionTaskSubmitter() {
        return new Runnable() {

            public void run() {
                for (Keyspace keyspace : Keyspace.all()) for (ColumnFamilyStore cfs : keyspace.getColumnFamilyStores()) CompactionManager.instance.submitBackground(cfs);
            }
        };
    }

    public void setCompactionParametersJson(String options) {
        setCompactionParameters(FBUtilities.fromJsonMap(options));
    }

    public String getCompactionParametersJson() {
        return FBUtilities.json(getCompactionParameters());
    }

    public void setCompactionParameters(Map<String, String> options) {
        try {
            CompactionParams compactionParams = CompactionParams.fromMap(options);
            compactionParams.validate();
            compactionStrategyManager.setNewLocalCompactionStrategy(compactionParams);
        } catch (Throwable t) {
            logger.error("Could not set new local compaction strategy", t);
            throw new IllegalArgumentException("Could not set new local compaction strategy: " + t.getMessage());
        }
    }

    public Map<String, String> getCompactionParameters() {
        return compactionStrategyManager.getCompactionParams().asMap();
    }

    public Map<String, String> getCompressionParameters() {
        return metadata().params.compression.asMap();
    }

    public String getCompressionParametersJson() {
        return FBUtilities.json(getCompressionParameters());
    }

    public void setCompressionParameters(Map<String, String> opts) {
        try {
            CompressionParams params = CompressionParams.fromMap(opts);
            params.validate();
            throw new UnsupportedOperationException();
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void setCompressionParametersJson(String options) {
        setCompressionParameters(FBUtilities.fromJsonMap(options));
    }

    @VisibleForTesting
    public ColumnFamilyStore(Keyspace keyspace, String columnFamilyName, int generation, TableMetadataRef metadata, Directories directories, boolean loadSSTables, boolean registerBookeeping, boolean offline) {
        assert directories != null;
        assert metadata != null : "null metadata for " + keyspace + ":" + columnFamilyName;
        this.keyspace = keyspace;
        this.metadata = metadata;
        this.directories = directories;
        name = columnFamilyName;
        minCompactionThreshold = new DefaultValue<>(metadata.get().params.compaction.minCompactionThreshold());
        maxCompactionThreshold = new DefaultValue<>(metadata.get().params.compaction.maxCompactionThreshold());
        crcCheckChance = new DefaultValue<>(metadata.get().params.crcCheckChance);
        viewManager = keyspace.viewManager.forTable(metadata.id);
        metric = new TableMetrics(this);
        fileIndexGenerator.set(generation);
        sampleReadLatencyNanos = DatabaseDescriptor.getReadRpcTimeout(NANOSECONDS) / 2;
        additionalWriteLatencyNanos = DatabaseDescriptor.getWriteRpcTimeout(NANOSECONDS) / 2;
        logger.info("Initializing {}.{}", keyspace.getName(), name);
        Memtable initialMemtable = null;
        if (DatabaseDescriptor.isDaemonInitialized())
            initialMemtable = new Memtable(new AtomicReference<>(CommitLog.instance.getCurrentPosition()), this);
        data = new Tracker(initialMemtable, loadSSTables);
        if (data.loadsstables) {
            Directories.SSTableLister sstableFiles = directories.sstableLister(Directories.OnTxnErr.IGNORE).skipTemporary(true);
            Collection<SSTableReader> sstables = SSTableReader.openAll(sstableFiles.list().entrySet(), metadata);
            data.addInitialSSTables(sstables);
        }
        compactionStrategyManager = new CompactionStrategyManager(this);
        if (maxCompactionThreshold.value() <= 0 || minCompactionThreshold.value() <= 0) {
            logger.warn("Disabling compaction strategy by setting compaction thresholds to 0 is deprecated, set the compaction option 'enabled' to 'false' instead.");
            this.compactionStrategyManager.disable();
        }
        indexManager = new SecondaryIndexManager(this);
        for (IndexMetadata info : metadata.get().indexes) {
            indexManager.addIndex(info, true);
        }
        if (registerBookeeping) {
            mbeanName = String.format("org.apache.cassandra.db:type=%s,keyspace=%s,table=%s", isIndex() ? "IndexTables" : "Tables", keyspace.getName(), name);
            oldMBeanName = String.format("org.apache.cassandra.db:type=%s,keyspace=%s,columnfamily=%s", isIndex() ? "IndexColumnFamilies" : "ColumnFamilies", keyspace.getName(), name);
            String[] objectNames = { mbeanName, oldMBeanName };
            for (String objectName : objectNames) MBeanWrapper.instance.registerMBean(this, objectName);
        } else {
            mbeanName = null;
            oldMBeanName = null;
        }
        writeHandler = new CassandraTableWriteHandler(this);
        streamManager = new CassandraStreamManager(this);
        repairManager = new CassandraTableRepairManager(this);
        sstableImporter = new SSTableImporter(this);
    }

    public void updateSpeculationThreshold() {
        try {
            sampleReadLatencyNanos = metadata().params.speculativeRetry.calculateThreshold(metric.coordinatorReadLatency.getSnapshot(), sampleReadLatencyNanos);
            additionalWriteLatencyNanos = metadata().params.additionalWritePolicy.calculateThreshold(metric.coordinatorWriteLatency.getSnapshot(), additionalWriteLatencyNanos);
        } catch (Throwable e) {
            logger.error("Exception caught while calculating speculative retry threshold for {}: {}", metadata(), e);
        }
    }

    public TableWriteHandler getWriteHandler() {
        return writeHandler;
    }

    public TableStreamManager getStreamManager() {
        return streamManager;
    }

    public TableRepairManager getRepairManager() {
        return repairManager;
    }

    public TableMetadata metadata() {
        return metadata.get();
    }

<<<<<<< MINE

=======
    @VisibleForTesting
    public ColumnFamilyStore(Keyspace keyspace, String columnFamilyName, int generation, CFMetaData metadata, Directories directories, boolean loadSSTables, boolean registerBookeeping, boolean offline) {
        assert directories != null;
        assert metadata != null : "null metadata for " + keyspace + ":" + columnFamilyName;
        this.keyspace = keyspace;
        this.metadata = metadata;
        name = columnFamilyName;
        minCompactionThreshold = new DefaultValue<>(metadata.params.compaction.minCompactionThreshold());
        maxCompactionThreshold = new DefaultValue<>(metadata.params.compaction.maxCompactionThreshold());
        crcCheckChance = new DefaultValue<>(metadata.params.crcCheckChance);
        indexManager = new SecondaryIndexManager(this);
        viewManager = keyspace.viewManager.forTable(metadata);
        metric = new TableMetrics(this);
        fileIndexGenerator.set(generation);
        sampleLatencyNanos = TimeUnit.MILLISECONDS.toNanos(DatabaseDescriptor.getReadRpcTimeout() / 2);
        logger.info("Initializing {}.{}", keyspace.getName(), name);
        Memtable initialMemtable = null;
        if (DatabaseDescriptor.isDaemonInitialized())
            initialMemtable = new Memtable(new AtomicReference<>(CommitLog.instance.getCurrentPosition()), this);
        data = new Tracker(initialMemtable, loadSSTables);
        if (data.loadsstables) {
            Directories.SSTableLister sstableFiles = directories.sstableLister(Directories.OnTxnErr.IGNORE).skipTemporary(true);
            Collection<SSTableReader> sstables = SSTableReader.openAll(sstableFiles.list().entrySet(), metadata);
            data.addInitialSSTables(sstables);
        }
        if (offline)
            this.directories = directories;
        else
            this.directories = new Directories(metadata, Directories.dataDirectories);
        compactionStrategyManager = new CompactionStrategyManager(this);
        this.directories = compactionStrategyManager.getDirectories();
        if (maxCompactionThreshold.value() <= 0 || minCompactionThreshold.value() <= 0) {
            logger.warn("Disabling compaction strategy by setting compaction thresholds to 0 is deprecated, set the compaction option 'enabled' to 'false' instead.");
            this.compactionStrategyManager.disable();
        }
        for (IndexMetadata info : metadata.getIndexes()) indexManager.addIndex(info);
        if (registerBookeeping) {
            mbeanName = String.format("org.apache.cassandra.db:type=%s,keyspace=%s,table=%s", isIndex() ? "IndexTables" : "Tables", keyspace.getName(), name);
            oldMBeanName = String.format("org.apache.cassandra.db:type=%s,keyspace=%s,columnfamily=%s", isIndex() ? "IndexColumnFamilies" : "ColumnFamilies", keyspace.getName(), name);
            try {
                ObjectName[] objectNames = { new ObjectName(mbeanName), new ObjectName(oldMBeanName) };
                for (ObjectName objectName : objectNames) {
                    MBeanWrapper.instance.registerMBean(this, objectName);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            logger.trace("retryPolicy for {} is {}", name, this.metadata.params.speculativeRetry);
            latencyCalculator = ScheduledExecutors.optionalTasks.scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    SpeculativeRetryParam retryPolicy = ColumnFamilyStore.this.metadata.params.speculativeRetry;
                    switch(retryPolicy.kind()) {
                        case PERCENTILE:
                            sampleLatencyNanos = (long) (metric.coordinatorReadLatency.getSnapshot().getValue(retryPolicy.threshold()));
                            break;
                        case CUSTOM:
                            sampleLatencyNanos = (long) retryPolicy.threshold();
                            break;
                        default:
                            sampleLatencyNanos = Long.MAX_VALUE;
                            break;
                    }
                }
            }, DatabaseDescriptor.getReadRpcTimeout(), DatabaseDescriptor.getReadRpcTimeout(), TimeUnit.MILLISECONDS);
        } else {
            latencyCalculator = ScheduledExecutors.optionalTasks.schedule(Runnables.doNothing(), 0, TimeUnit.NANOSECONDS);
            mbeanName = null;
            oldMBeanName = null;
        }
    }
>>>>>>> YOURS

    public Directories getDirectories() {
        return directories;
    }

    public SSTableMultiWriter createSSTableMultiWriter(Descriptor descriptor, long keyCount, long repairedAt, UUID pendingRepair, boolean isTransient, int sstableLevel, SerializationHeader header, LifecycleNewTracker lifecycleNewTracker) {
        MetadataCollector collector = new MetadataCollector(metadata().comparator).sstableLevel(sstableLevel);
        return createSSTableMultiWriter(descriptor, keyCount, repairedAt, pendingRepair, isTransient, collector, header, lifecycleNewTracker);
    }

<<<<<<< MINE
public SSTableMultiWriter createSSTableMultiWriter(Descriptor descriptor, long keyCount, long repairedAt, UUID pendingRepair, boolean isTransient, MetadataCollector metadataCollector, SerializationHeader header, LifecycleNewTracker lifecycleNewTracker) {
        return getCompactionStrategyManager().createSSTableMultiWriter(descriptor, keyCount, repairedAt, pendingRepair, isTransient, metadataCollector, header, indexManager.listIndexes(), lifecycleNewTracker);
=======
public SSTableMultiWriter createSSTableMultiWriter(Descriptor descriptor, long keyCount, long repairedAt, MetadataCollector metadataCollector, SerializationHeader header, LifecycleNewTracker lifecycleNewTracker) {
        return getCompactionStrategyManager().createSSTableMultiWriter(descriptor, keyCount, repairedAt, metadataCollector, header, indexManager.listIndexes(), lifecycleNewTracker);
>>>>>>> YOURS
    }

    public boolean supportsEarlyOpen() {
        return compactionStrategyManager.supportsEarlyOpen();
    }

    public void invalidate() {
        invalidate(true);
    }

    public void invalidate(boolean expectMBean) {
        valid = false;
        try {
            unregisterMBean();
        } catch (Exception e) {
            if (expectMBean) {
                JVMStabilityInspector.inspectThrowable(e);
                logger.warn("Failed unregistering mbean: {}", mbeanName, e);
            }
        }
        compactionStrategyManager.shutdown();
        SystemKeyspace.removeTruncationRecord(metadata.id);
        data.dropSSTables();
        LifecycleTransaction.waitForDeletions();
        indexManager.dropAllIndexes();
        invalidateCaches();
    }

    void maybeRemoveUnreadableSSTables(File directory) {
        data.removeUnreadableSSTables(directory);
    }

    void unregisterMBean() throws MalformedObjectNameException {
        ObjectName[] objectNames = { new ObjectName(mbeanName), new ObjectName(oldMBeanName) };
        for (ObjectName objectName : objectNames) {
            if (MBeanWrapper.instance.isRegistered(objectName))
                MBeanWrapper.instance.unregisterMBean(objectName);
        }
        metric.release();
    }

    public static ColumnFamilyStore createColumnFamilyStore(Keyspace keyspace, TableMetadataRef metadata, boolean loadSSTables) {
        return createColumnFamilyStore(keyspace, metadata.name, metadata, loadSSTables);
    }

    public static synchronized ColumnFamilyStore createColumnFamilyStore(Keyspace keyspace, String columnFamily, TableMetadataRef metadata, boolean loadSSTables) {
        Directories directories = new Directories(metadata.get());
        return createColumnFamilyStore(keyspace, columnFamily, metadata, directories, loadSSTables, true, false);
    }

<<<<<<< MINE
public static synchronized ColumnFamilyStore createColumnFamilyStore(Keyspace keyspace, String columnFamily, TableMetadataRef metadata, Directories directories, boolean loadSSTables, boolean registerBookkeeping, boolean offline) {
        Directories.SSTableLister lister = directories.sstableLister(Directories.OnTxnErr.IGNORE).includeBackups(true);
        List<Integer> generations = new ArrayList<Integer>();
        for (Map.Entry<Descriptor, Set<Component>> entry : lister.list().entrySet()) {
            Descriptor desc = entry.getKey();
            generations.add(desc.generation);
            if (!desc.isCompatible())
                throw new RuntimeException(String.format("Incompatible SSTable found. Current version %s is unable to read file: %s. Please run upgradesstables.", desc.getFormat().getLatestVersion(), desc));
        }
        Collections.sort(generations);
        int value = (generations.size() > 0) ? (generations.get(generations.size() - 1)) : 0;
        return new ColumnFamilyStore(keyspace, columnFamily, value, metadata, directories, loadSSTables, registerBookkeeping, offline);
=======
public static synchronized ColumnFamilyStore createColumnFamilyStore(Keyspace keyspace, String columnFamily, CFMetaData metadata, boolean loadSSTables) {
        Directories directories = new Directories(metadata, initialDirectories);
        return createColumnFamilyStore(keyspace, columnFamily, metadata, directories, loadSSTables, true, false);
>>>>>>> YOURS
    }

<<<<<<< MINE
public static void scrubDataDirectories(TableMetadata metadata) throws StartupException {
        Directories directories = new Directories(metadata);
=======
public static void scrubDataDirectories(CFMetaData metadata) throws StartupException {
        Directories directories = new Directories(metadata, initialDirectories);
>>>>>>> YOURS
        Set<File> cleanedDirectories = new HashSet<>();
        clearEphemeralSnapshots(directories);
        directories.removeTemporaryDirectories();
<<<<<<< MINE
        logger.trace("Removing temporary or obsoleted files from unfinished operations for table {}", metadata.name);
        if (!LifecycleTransaction.removeUnfinishedLeftovers(metadata))
            throw new StartupException(StartupException.ERR_WRONG_DISK_STATE, String.format("Cannot remove temporary or obsoleted files for %s due to a problem with transaction " + "log files. Please check records with problems in the log messages above and fix them. " + "Refer to the 3.0 upgrading instructions in NEWS.txt " + "for a description of transaction log files.", metadata.toString()));
        logger.trace("Further extra check for orphan sstable files for {}", metadata.name);
=======
        logger.trace("Removing temporary or obsoleted files from unfinished operations for table {}", metadata.cfName);
        if (!LifecycleTransaction.removeUnfinishedLeftovers(metadata))
            throw new StartupException(StartupException.ERR_WRONG_DISK_STATE, String.format("Cannot remove temporary or obsoleted files for %s.%s due to a problem with transaction " + "log files. Please check records with problems in the log messages above and fix them. " + "Refer to the 3.0 upgrading instructions in NEWS.txt " + "for a description of transaction log files.", metadata.ksName, metadata.cfName));
        logger.trace("Further extra check for orphan sstable files for {}", metadata.cfName);
>>>>>>> YOURS
        for (Map.Entry<Descriptor, Set<Component>> sstableFiles : directories.sstableLister(Directories.OnTxnErr.IGNORE).list().entrySet()) {
            Descriptor desc = sstableFiles.getKey();
            File directory = desc.directory;
            Set<Component> components = sstableFiles.getValue();
            if (!cleanedDirectories.contains(directory)) {
                cleanedDirectories.add(directory);
                for (File tmpFile : desc.getTemporaryFiles()) {
                    logger.info("Removing unfinished temporary file {}", tmpFile);
                    tmpFile.delete();
                }
            }
            File dataFile = new File(desc.filenameFor(Component.DATA));
            if (components.contains(Component.DATA) && dataFile.length() > 0)
                continue;
            logger.warn("Removing orphans for {}: {}", desc, components);
            for (Component component : components) {
                File file = new File(desc.filenameFor(component));
                if (file.exists())
                    FileUtils.deleteWithConfirm(desc.filenameFor(component));
            }
        }
        Pattern tmpCacheFilePattern = Pattern.compile(metadata.keyspace + "-" + metadata.name + "-(Key|Row)Cache.*\\.tmp$");
        File dir = new File(DatabaseDescriptor.getSavedCachesLocation());
        if (dir.exists()) {
            assert dir.isDirectory();
            for (File file : dir.listFiles()) if (tmpCacheFilePattern.matcher(file.getName()).matches())
                if (!file.delete())
                    logger.warn("could not delete {}", file.getAbsolutePath());
        }
        for (IndexMetadata index : metadata.indexes) if (!index.isCustom()) {
            TableMetadata indexMetadata = CassandraIndex.indexCfsMetadata(metadata, index);
            scrubDataDirectories(indexMetadata);
        }
    }

    public static synchronized ColumnFamilyStore createColumnFamilyStore(Keyspace keyspace, String columnFamily, CFMetaData metadata, Directories directories, boolean loadSSTables, boolean registerBookkeeping, boolean offline) {
        Directories.SSTableLister lister = directories.sstableLister(Directories.OnTxnErr.IGNORE).includeBackups(true);
        List<Integer> generations = new ArrayList<Integer>();
        for (Map.Entry<Descriptor, Set<Component>> entry : lister.list().entrySet()) {
            Descriptor desc = entry.getKey();
            generations.add(desc.generation);
            if (!desc.isCompatible())
                throw new RuntimeException(String.format("Incompatible SSTable found. Current version %s is unable to read file: %s. Please run upgradesstables.", desc.getFormat().getLatestVersion(), desc));
        }
        Collections.sort(generations);
        int value = (generations.size() > 0) ? (generations.get(generations.size() - 1)) : 0;
        return new ColumnFamilyStore(keyspace, columnFamily, value, metadata, directories, loadSSTables, registerBookkeeping, offline);
    }

    public static void loadNewSSTables(String ksName, String cfName) {
        Keyspace keyspace = Keyspace.open(ksName);
        keyspace.getColumnFamilyStore(cfName).loadNewSSTables();
    }

    @Deprecated
    public void loadNewSSTables() {
        SSTableImporter.Options options = SSTableImporter.Options.options().resetLevel(true).build();
        sstableImporter.importNewSSTables(options);
    }

    public synchronized List<String> importNewSSTables(Set<String> srcPaths, boolean resetLevel, boolean clearRepaired, boolean verifySSTables, boolean verifyTokens, boolean invalidateCaches, boolean extendedVerify) {
        SSTableImporter.Options options = SSTableImporter.Options.options(srcPaths).resetLevel(resetLevel).clearRepaired(clearRepaired).verifySSTables(verifySSTables).verifyTokens(verifyTokens).invalidateCaches(invalidateCaches).extendedVerify(extendedVerify).build();
        return sstableImporter.importNewSSTables(options);
    }

    Descriptor getUniqueDescriptorFor(Descriptor descriptor, File targetDirectory) {
        Descriptor newDescriptor;
        do {
            newDescriptor = new Descriptor(descriptor.version, targetDirectory, descriptor.ksname, descriptor.cfname, fileIndexGenerator.incrementAndGet(), descriptor.formatType);
        } while (new File(newDescriptor.filenameFor(Component.DATA)).exists());
        return newDescriptor;
    }

    public void rebuildSecondaryIndex(String idxName) {
        rebuildSecondaryIndex(keyspace.getName(), metadata.name, idxName);
    }

    public static void rebuildSecondaryIndex(String ksName, String cfName, String... idxNames) {
        ColumnFamilyStore cfs = Keyspace.open(ksName).getColumnFamilyStore(cfName);
            logger.info("User Requested secondary index re-build for {}/{} indexes: {}", ksName, cfName, Joiner.on(',').join(idxNames));
        cfs.indexManager.rebuildIndexesBlocking(Sets.newHashSet(Arrays.asList(idxNames)));
    }

    public AbstractCompactionStrategy createCompactionStrategyInstance(CompactionParams compactionParams) {
        try {
            Constructor<? extends AbstractCompactionStrategy> constructor = compactionParams.klass().getConstructor(ColumnFamilyStore.class, Map.class);
            return constructor.newInstance(this, compactionParams.options());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public String getColumnFamilyName() {
        return getTableName();
    }

    public String getTableName() {
        return name;
    }

    public Descriptor newSSTableDescriptor(File directory) {
        return newSSTableDescriptor(directory, SSTableFormat.Type.current().info.getLatestVersion(), SSTableFormat.Type.current());
    }

    public Descriptor newSSTableDescriptor(File directory, SSTableFormat.Type format) {
        return newSSTableDescriptor(directory, format.info.getLatestVersion(), format);
    }

    public Descriptor newSSTableDescriptor(File directory, Version version, SSTableFormat.Type format) {
        return new Descriptor(version, directory, keyspace.getName(), name, fileIndexGenerator.incrementAndGet(), format);
    }<<<<<<< MINE
=======
public String getSSTablePath(File directory) {
        return getSSTablePath(directory, SSTableFormat.Type.current().info.getLatestVersion(), SSTableFormat.Type.current());
    }
>>>>>>> YOURS


    public ListenableFuture<CommitLogPosition> switchMemtableIfCurrent(Memtable memtable) {
        synchronized (data) {
            if (data.getView().getCurrentMemtable() == memtable)
                return switchMemtable();
        }
        return waitForFlushes();
    }

    public ListenableFuture<CommitLogPosition> switchMemtable() {
        synchronized (data) {
            logFlush();
            Flush flush = new Flush(false);
            flushExecutor.execute(flush);
            postFlushExecutor.execute(flush.postFlushTask);
            return flush.postFlushTask;
        }
    }

    private void logFlush() {
        float onHeapRatio = 0, offHeapRatio = 0;
        long onHeapTotal = 0, offHeapTotal = 0;
        Memtable memtable = getTracker().getView().getCurrentMemtable();
        onHeapRatio += memtable.getAllocator().onHeap().ownershipRatio();
        offHeapRatio += memtable.getAllocator().offHeap().ownershipRatio();
        onHeapTotal += memtable.getAllocator().onHeap().owns();
        offHeapTotal += memtable.getAllocator().offHeap().owns();
        for (ColumnFamilyStore indexCfs : indexManager.getAllIndexColumnFamilyStores()) {
            MemtableAllocator allocator = indexCfs.getTracker().getView().getCurrentMemtable().getAllocator();
            onHeapRatio += allocator.onHeap().ownershipRatio();
            offHeapRatio += allocator.offHeap().ownershipRatio();
            onHeapTotal += allocator.onHeap().owns();
            offHeapTotal += allocator.offHeap().owns();
        }
<<<<<<< MINE
        logger.info("Enqueuing flush of {}: {}", name, String.format("%s (%.0f%%) on-heap, %s (%.0f%%) off-heap", FBUtilities.prettyPrintMemory(onHeapTotal), onHeapRatio * 100, FBUtilities.prettyPrintMemory(offHeapTotal), offHeapRatio * 100));
=======
        logger.debug("Enqueuing flush of {}: {}", name, String.format("%s (%.0f%%) on-heap, %s (%.0f%%) off-heap", FBUtilities.prettyPrintMemory(onHeapTotal), onHeapRatio * 100, FBUtilities.prettyPrintMemory(offHeapTotal), offHeapRatio * 100));
>>>>>>> YOURS
    }

    public ListenableFuture<CommitLogPosition> forceFlush() {
        synchronized (data) {
            Memtable current = data.getView().getCurrentMemtable();
            for (ColumnFamilyStore cfs : concatWithIndexes()) if (!cfs.data.getView().getCurrentMemtable().isClean())
                return switchMemtableIfCurrent(current);
            return waitForFlushes();
        }
    }

    public ListenableFuture<?> forceFlush(CommitLogPosition flushIfDirtyBefore) {
        Memtable current = data.getView().getCurrentMemtable();
        if (current.mayContainDataBefore(flushIfDirtyBefore))
            return switchMemtableIfCurrent(current);
        return waitForFlushes();
    }

    private ListenableFuture<CommitLogPosition> waitForFlushes() {
        final Memtable current = data.getView().getCurrentMemtable();
        ListenableFutureTask<CommitLogPosition> task = ListenableFutureTask.create(() -> {
                logger.debug("forceFlush requested but everything is clean in {}", name);
                return current.getCommitLogLowerBound();
        });
        postFlushExecutor.execute(task);
        return task;
    }

    public CommitLogPosition forceBlockingFlush() {
        return FBUtilities.waitOnFuture(forceFlush());
    }

    private final class PostFlush implements Callable<CommitLogPosition> {

        final CountDownLatch latch = new CountDownLatch(1);

        final List<Memtable> memtables;

        volatile Throwable flushFailure = null;

        private PostFlush(List<Memtable> memtables) {
            this.memtables = memtables;
        }

        public CommitLogPosition call() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException();
            }
            CommitLogPosition commitLogUpperBound = CommitLogPosition.NONE;
            if (flushFailure == null && !memtables.isEmpty()) {
                Memtable memtable = memtables.get(0);
                commitLogUpperBound = memtable.getCommitLogUpperBound();
                CommitLog.instance.discardCompletedSegments(metadata.id, memtable.getCommitLogLowerBound(), commitLogUpperBound);
            }
            metric.pendingFlushes.dec();
            if (flushFailure != null)
                throw Throwables.propagate(flushFailure);
            return commitLogUpperBound;
        }
    }

    private final class Flush implements Runnable {

        final OpOrder.Barrier writeBarrier;

        final List<Memtable> memtables = new ArrayList<>();

        final ListenableFutureTask<CommitLogPosition> postFlushTask;

        final PostFlush postFlush;

        final boolean truncate;

        private Flush(boolean truncate) {
            this.truncate = truncate;
            metric.pendingFlushes.inc();
<<<<<<< MINE
            writeBarrier = Keyspace.writeOrder.newBarrier();
=======
            writeBarrier = keyspace.writeOrder.newBarrier();
>>>>>>> YOURS
            AtomicReference<CommitLogPosition> commitLogUpperBound = new AtomicReference<>();
            for (ColumnFamilyStore cfs : concatWithIndexes()) {
                Memtable newMemtable = new Memtable(commitLogUpperBound, cfs);
                Memtable oldMemtable = cfs.data.switchMemtable(truncate, newMemtable);
                oldMemtable.setDiscarding(writeBarrier, commitLogUpperBound);
                memtables.add(oldMemtable);
            }
            setCommitLogUpperBound(commitLogUpperBound);
            writeBarrier.issue();
            postFlush = new PostFlush(memtables);
            postFlushTask = ListenableFutureTask.create(postFlush);
        }

        public void run() {
            writeBarrier.markBlocking();
            writeBarrier.await();
            for (Memtable memtable : memtables) memtable.cfs.data.markFlushing(memtable);
            metric.memtableSwitchCount.inc();
            try {
                flushMemtable(memtables.get(0), true);
                for (int i = 1; i < memtables.size(); i++) flushMemtable(memtables.get(i), false);
            } catch (Throwable t) {
                JVMStabilityInspector.inspectThrowable(t);
                postFlush.flushFailure = t;
                        }
                postFlush.latch.countDown();
            }

        public Collection<SSTableReader> flushMemtable(Memtable memtable, boolean flushNonCf2i) {
            if (memtable.isClean() || truncate) {
                memtable.cfs.replaceFlushed(memtable, Collections.emptyList());
                reclaim(memtable);
                return Collections.emptyList();
            }
            List<Future<SSTableMultiWriter>> futures = new ArrayList<>();
            long totalBytesOnDisk = 0;
            long maxBytesOnDisk = 0;
            long minBytesOnDisk = Long.MAX_VALUE;
            List<SSTableReader> sstables = new ArrayList<>();
            try (LifecycleTransaction txn = LifecycleTransaction.offline(OperationType.FLUSH)) {
                List<Memtable.FlushRunnable> flushRunnables = null;
                List<SSTableMultiWriter> flushResults = null;
                try {
                    flushRunnables = memtable.flushRunnables(txn);
                    for (int i = 0; i < flushRunnables.size(); i++) futures.add(perDiskflushExecutors[i].submit(flushRunnables.get(i)));
                    if (flushNonCf2i)
                        indexManager.flushAllNonCFSBackedIndexesBlocking();
                    flushResults = Lists.newArrayList(FBUtilities.waitOnFutures(futures));
                } catch (Throwable t) {
                    t = memtable.abortRunnables(flushRunnables, t);
                    t = txn.abort(t);
                    throw Throwables.propagate(t);
                }
                try {
                    Iterator<SSTableMultiWriter> writerIterator = flushResults.iterator();
                    while (writerIterator.hasNext()) {
                        @SuppressWarnings("resource")
                        SSTableMultiWriter writer = writerIterator.next();
                        if (writer.getFilePointer() > 0) {
                            writer.setOpenResult(true).prepareToCommit();
                        } else {
                            maybeFail(writer.abort(null));
                            writerIterator.remove();
                        }
                    }
                } catch (Throwable t) {
                    for (SSTableMultiWriter writer : flushResults) t = writer.abort(t);
                    t = txn.abort(t);
                    Throwables.propagate(t);
                }
                txn.prepareToCommit();
                Throwable accumulate = null;
                for (SSTableMultiWriter writer : flushResults) accumulate = writer.commit(accumulate);
                maybeFail(txn.commit(accumulate));
                for (SSTableMultiWriter writer : flushResults) {
                    Collection<SSTableReader> flushedSSTables = writer.finished();
                    for (SSTableReader sstable : flushedSSTables) {
                        if (sstable != null) {
                            sstables.add(sstable);
                            long size = sstable.bytesOnDisk();
                            totalBytesOnDisk += size;
                            maxBytesOnDisk = Math.max(maxBytesOnDisk, size);
                            minBytesOnDisk = Math.min(minBytesOnDisk, size);
                        }
                    }
                }
            }
            memtable.cfs.replaceFlushed(memtable, sstables);
            reclaim(memtable);
            memtable.cfs.compactionStrategyManager.compactionLogger.flush(sstables);
            logger.debug("Flushed to {} ({} sstables, {}), biggest {}, smallest {}", sstables, sstables.size(), FBUtilities.prettyPrintMemory(totalBytesOnDisk), FBUtilities.prettyPrintMemory(maxBytesOnDisk), FBUtilities.prettyPrintMemory(minBytesOnDisk));
            return sstables;
        }

        private void reclaim(final Memtable memtable) {
            final OpOrder.Barrier readBarrier = readOrdering.newBarrier();
            readBarrier.issue();
            postFlushTask.addListener(new WrappedRunnable() {

                public void runMayThrow() {
                    readBarrier.await();
                    memtable.setDiscarded();
                }
            }, reclaimExecutor);
        }
    }

    private static void setCommitLogUpperBound(AtomicReference<CommitLogPosition> commitLogUpperBound) {
        CommitLogPosition lastReplayPosition;
        while (true) {
            lastReplayPosition = new Memtable.LastCommitLogPosition((CommitLog.instance.getCurrentPosition()));
            CommitLogPosition currentLast = commitLogUpperBound.get();
            if ((currentLast == null || currentLast.compareTo(lastReplayPosition) <= 0) && commitLogUpperBound.compareAndSet(currentLast, lastReplayPosition))
                break;
        }
    }

    public static class FlushLargestColumnFamily implements Runnable {

        public void run() {
            float largestRatio = 0f;
            Memtable largest = null;
            float liveOnHeap = 0, liveOffHeap = 0;
            for (ColumnFamilyStore cfs : ColumnFamilyStore.all()) {
                Memtable current = cfs.getTracker().getView().getCurrentMemtable();
                float onHeap = 0f, offHeap = 0f;
                onHeap += current.getAllocator().onHeap().ownershipRatio();
                offHeap += current.getAllocator().offHeap().ownershipRatio();
                for (ColumnFamilyStore indexCfs : cfs.indexManager.getAllIndexColumnFamilyStores()) {
                    MemtableAllocator allocator = indexCfs.getTracker().getView().getCurrentMemtable().getAllocator();
                    onHeap += allocator.onHeap().ownershipRatio();
                    offHeap += allocator.offHeap().ownershipRatio();
                }
                float ratio = Math.max(onHeap, offHeap);
                if (ratio > largestRatio) {
                    largest = current;
                    largestRatio = ratio;
                }
                liveOnHeap += onHeap;
                liveOffHeap += offHeap;
            }
            if (largest != null) {
                float usedOnHeap = Memtable.MEMORY_POOL.onHeap.usedRatio();
                float usedOffHeap = Memtable.MEMORY_POOL.offHeap.usedRatio();
                float flushingOnHeap = Memtable.MEMORY_POOL.onHeap.reclaimingRatio();
                float flushingOffHeap = Memtable.MEMORY_POOL.offHeap.reclaimingRatio();
                float thisOnHeap = largest.getAllocator().onHeap().ownershipRatio();
                float thisOffHeap = largest.getAllocator().offHeap().ownershipRatio();
                logger.info("Flushing largest {} to free up room. Used total: {}, live: {}, flushing: {}, this: {}", largest.cfs, ratio(usedOnHeap, usedOffHeap), ratio(liveOnHeap, liveOffHeap), ratio(flushingOnHeap, flushingOffHeap), ratio(thisOnHeap, thisOffHeap));
                largest.cfs.switchMemtableIfCurrent(largest);
            }
        }
    }

    private static String ratio(float onHeap, float offHeap) {
        return String.format("%.2f/%.2f", onHeap, offHeap);
    }

    public void apply(PartitionUpdate update, UpdateTransaction indexer, OpOrder.Group opGroup, CommitLogPosition commitLogPosition) {
        long start = System.nanoTime();
        try {
            Memtable mt = data.getMemtableFor(opGroup, commitLogPosition);
            long timeDelta = mt.put(update, indexer, opGroup);
            DecoratedKey key = update.partitionKey();
            invalidateCachedPartition(key);
<<<<<<< MINE
            metric.topWritePartitionFrequency.addSample(key.getKey(), 1);
            if (metric.topWritePartitionSize.isEnabled())
                metric.topWritePartitionSize.addSample(key.getKey(), update.dataSize());
            StorageHook.instance.reportWrite(metadata.id, update);
=======
            metric.samplers.get(Sampler.WRITES).addSample(key.getKey(), key.hashCode(), 1);
            StorageHook.instance.reportWrite(metadata.cfId, update);
>>>>>>> YOURS
            metric.writeLatency.addNano(System.nanoTime() - start);
            if (timeDelta < Long.MAX_VALUE)
                metric.colUpdateTimeDeltaHistogram.update(Math.min(18165375903306L, timeDelta));
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage() + " for ks: " + keyspace.getName() + ", table: " + name, e);
        }
    }

    public Collection<SSTableReader> getOverlappingLiveSSTables(Iterable<SSTableReader> sstables) {
        logger.trace("Checking for sstables overlapping {}", sstables);
        if (!sstables.iterator().hasNext())
            return ImmutableSet.of();
        View view = data.getView();
        List<SSTableReader> sortedByFirst = Lists.newArrayList(sstables);
        Collections.sort(sortedByFirst, (o1, o2) -> o1.first.compareTo(o2.first));
        List<AbstractBounds<PartitionPosition>> bounds = new ArrayList<>();
        DecoratedKey first = null, last = null;
        for (SSTableReader sstable : sortedByFirst) {
            if (first == null) {
                first = sstable.first;
                last = sstable.last;
            } else {
                if (sstable.first.compareTo(last) <= 0) {
                    if (sstable.last.compareTo(last) > 0)
                        last = sstable.last;
                } else {
                    bounds.add(AbstractBounds.bounds(first, true, last, true));
                    first = sstable.first;
                    last = sstable.last;
                }
            }
        }
        bounds.add(AbstractBounds.bounds(first, true, last, true));
        Set<SSTableReader> results = new HashSet<>();
        for (AbstractBounds<PartitionPosition> bound : bounds) Iterables.addAll(results, view.liveSSTablesInBounds(bound.left, bound.right));
        return Sets.difference(results, ImmutableSet.copyOf(sstables));
    }

    public Refs<SSTableReader> getAndReferenceOverlappingLiveSSTables(Iterable<SSTableReader> sstables) {
        while (true) {
            Iterable<SSTableReader> overlapped = getOverlappingLiveSSTables(sstables);
            Refs<SSTableReader> refs = Refs.tryRef(overlapped);
            if (refs != null)
                return refs;
        }
    }

    public void addSSTable(SSTableReader sstable) {
        assert sstable.getColumnFamilyName().equals(name);
        addSSTables(Collections.singletonList(sstable));
    }

    public void addSSTables(Collection<SSTableReader> sstables) {
        data.addSSTables(sstables);
        CompactionManager.instance.submitBackground(this);
    }

    public long getExpectedCompactedFileSize(Iterable<SSTableReader> sstables, OperationType operation) {
        if (operation != OperationType.CLEANUP || isIndex()) {
            return SSTableReader.getTotalBytes(sstables);
        }
        long expectedFileSize = 0;
        Collection<Range<Token>> ranges = StorageService.instance.getLocalReplicas(keyspace.getName()).ranges();
        for (SSTableReader sstable : sstables) {
            List<SSTableReader.PartitionPositionBounds> positions = sstable.getPositionsForRanges(ranges);
            for (SSTableReader.PartitionPositionBounds position : positions) expectedFileSize += position.upperPosition - position.lowerPosition;
        }
        double compressionRatio = metric.compressionRatio.getValue();
        if (compressionRatio > 0d)
            expectedFileSize *= compressionRatio;
        return expectedFileSize;
    }

    public SSTableReader getMaxSizeFile(Iterable<SSTableReader> sstables) {
        long maxSize = 0L;
        SSTableReader maxFile = null;
        for (SSTableReader sstable : sstables) {
            if (sstable.onDiskLength() > maxSize) {
                maxSize = sstable.onDiskLength();
                maxFile = sstable;
            }
        }
        return maxFile;
    }

    public CompactionManager.AllSSTableOpStatus forceCleanup(int jobs) throws ExecutionException, InterruptedException {
        return CompactionManager.instance.performCleanup(ColumnFamilyStore.this, jobs);
    }

    public CompactionManager.AllSSTableOpStatus scrub(boolean disableSnapshot, boolean skipCorrupted, boolean checkData, boolean reinsertOverflowedTTL, int jobs) throws ExecutionException, InterruptedException {
        return scrub(disableSnapshot, skipCorrupted, reinsertOverflowedTTL, false, checkData, jobs);
    }

    @VisibleForTesting
    public CompactionManager.AllSSTableOpStatus scrub(boolean disableSnapshot, boolean skipCorrupted, boolean reinsertOverflowedTTL, boolean alwaysFail, boolean checkData, int jobs) throws ExecutionException, InterruptedException {
        if (!disableSnapshot)
            snapshotWithoutFlush("pre-scrub-" + System.currentTimeMillis());
        try {
            return CompactionManager.instance.performScrub(ColumnFamilyStore.this, skipCorrupted, checkData, reinsertOverflowedTTL, jobs);
        } catch (Throwable t) {
            if (!rebuildOnFailedScrub(t))
                throw t;
            return alwaysFail ? CompactionManager.AllSSTableOpStatus.ABORTED : CompactionManager.AllSSTableOpStatus.SUCCESSFUL;
        }
    }

    public boolean rebuildOnFailedScrub(Throwable failure) {
        if (!isIndex() || !SecondaryIndexManager.isIndexColumnFamilyStore(this))
            return false;
        truncateBlocking();
        logger.warn("Rebuilding index for {} because of <{}>", name, failure.getMessage());
        ColumnFamilyStore parentCfs = SecondaryIndexManager.getParentCfs(this);
        assert parentCfs.indexManager.getAllIndexColumnFamilyStores().contains(this);
        String indexName = SecondaryIndexManager.getIndexName(this);
        parentCfs.rebuildSecondaryIndex(indexName);
        return true;
    }

    public CompactionManager.AllSSTableOpStatus verify(Verifier.Options options) throws ExecutionException, InterruptedException {
        return CompactionManager.instance.performVerify(ColumnFamilyStore.this, options);
    }

    public CompactionManager.AllSSTableOpStatus sstablesRewrite(boolean excludeCurrentVersion, int jobs) throws ExecutionException, InterruptedException {
        return CompactionManager.instance.performSSTableRewrite(ColumnFamilyStore.this, excludeCurrentVersion, jobs);
    }

    public CompactionManager.AllSSTableOpStatus relocateSSTables(int jobs) throws ExecutionException, InterruptedException {
        return CompactionManager.instance.relocateSSTables(this, jobs);
    }

    public CompactionManager.AllSSTableOpStatus garbageCollect(TombstoneOption tombstoneOption, int jobs) throws ExecutionException, InterruptedException {
        return CompactionManager.instance.performGarbageCollection(this, tombstoneOption, jobs);
    }

    public void markObsolete(Collection<SSTableReader> sstables, OperationType compactionType) {
        assert !sstables.isEmpty();
        maybeFail(data.dropSSTables(Predicates.in(sstables), compactionType, null));
    }

    void replaceFlushed(Memtable memtable, Collection<SSTableReader> sstables) {
        data.replaceFlushed(memtable, sstables);
        if (sstables != null && !sstables.isEmpty())
            CompactionManager.instance.submitBackground(this);
    }

    public boolean isValid() {
        return valid;
    }

    public Tracker getTracker() {
        return data;
    }

    public Set<SSTableReader> getLiveSSTables() {
        return data.getView().liveSSTables();
    }

    public Iterable<SSTableReader> getSSTables(SSTableSet sstableSet) {
        return data.getView().select(sstableSet);
    }

    public Iterable<SSTableReader> getUncompactingSSTables() {
        return data.getUncompacting();
    }

    public Map<UUID, PendingStat> getPendingRepairStats() {
        Map<UUID, PendingStat.Builder> builders = new HashMap<>();
        for (SSTableReader sstable : getLiveSSTables()) {
            UUID session = sstable.getPendingRepair();
            if (session == null)
                continue;
            if (!builders.containsKey(session))
                builders.put(session, new PendingStat.Builder());
            builders.get(session).addSSTable(sstable);
        }
        Map<UUID, PendingStat> stats = new HashMap<>();
        for (Map.Entry<UUID, PendingStat.Builder> entry : builders.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().build());
        }
        return stats;
    }

    public CleanupSummary releaseRepairData(Collection<UUID> sessions, boolean force) {
        if (force) {
            Predicate<SSTableReader> predicate = sst -> {
                UUID session = sst.getPendingRepair();
                return session != null && sessions.contains(session);
            };
            return runWithCompactionsDisabled(() -> compactionStrategyManager.releaseRepairData(sessions), predicate, false, true, true);
        } else {
            return compactionStrategyManager.releaseRepairData(sessions);
        }
    }

    public boolean isFilterFullyCoveredBy(ClusteringIndexFilter filter, DataLimits limits, CachedPartition cached, int nowInSec, boolean enforceStrictLiveness) {
        if (cached.cachedLiveRows() < metadata().params.caching.rowsPerPartitionToCache())
            return true;
        return (filter.isHeadFilter() && limits.hasEnoughLiveData(cached, nowInSec, filter.selectsAllPartition(), enforceStrictLiveness)) || filter.isFullyCoveredBy(cached);
    }

    public int gcBefore(int nowInSec) {
        return nowInSec - metadata().params.gcGraceSeconds;
    }

    @SuppressWarnings("resource")
    public RefViewFragment selectAndReference(Function<View, Iterable<SSTableReader>> filter) {
        long failingSince = -1L;
        while (true) {
            ViewFragment view = select(filter);
            Refs<SSTableReader> refs = Refs.tryRef(view.sstables);
            if (refs != null)
                return new RefViewFragment(view.sstables, view.memtables, refs);
            if (failingSince <= 0) {
                failingSince = System.nanoTime();
            } else if (System.nanoTime() - failingSince > TimeUnit.MILLISECONDS.toNanos(100)) {
                List<SSTableReader> released = new ArrayList<>();
                for (SSTableReader reader : view.sstables) if (reader.selfRef().globalCount() == 0)
                    released.add(reader);
                NoSpamLogger.log(logger, NoSpamLogger.Level.WARN, 1, TimeUnit.SECONDS, "Spinning trying to capture readers {}, released: {}, ", view.sstables, released);
                failingSince = System.nanoTime();
            }
        }
    }

    public ViewFragment select(Function<View, Iterable<SSTableReader>> filter) {
        View view = data.getView();
        List<SSTableReader> sstables = Lists.newArrayList(filter.apply(view));
        return new ViewFragment(sstables, view.getAllMemtables());
    }

    public List<String> getSSTablesForKey(String key) {
        return getSSTablesForKey(key, false);
    }

    public List<String> getSSTablesForKey(String key, boolean hexFormat) {
<<<<<<< MINE
        ByteBuffer keyBuffer = hexFormat ? ByteBufferUtil.hexToBytes(key) : metadata().partitionKeyType.fromString(key);
=======
        ByteBuffer keyBuffer = hexFormat ? ByteBufferUtil.hexToBytes(key) : metadata.getKeyValidator().fromString(key);
>>>>>>> YOURS
        DecoratedKey dk = decorateKey(keyBuffer);
        try (OpOrder.Group op = readOrdering.start()) {
            List<String> files = new ArrayList<>();
            for (SSTableReader sstr : select(View.select(SSTableSet.LIVE, dk)).sstables) {
                if (sstr.getPosition(dk, SSTableReader.Operator.EQ, false) != null)
                    files.add(sstr.getFilename());
            }
            return files;
        }
    }

    public void beginLocalSampling(String sampler, int capacity, int durationMillis) {
        metric.samplers.get(SamplerType.valueOf(sampler)).beginSampling(capacity, durationMillis);
    }

<<<<<<< MINE
@SuppressWarnings({ "rawtypes", "unchecked" })
    public List<CompositeData> finishLocalSampling(String sampler, int count) throws OpenDataException {
        Sampler samplerImpl = metric.samplers.get(SamplerType.valueOf(sampler));
        List<Sample> samplerResults = samplerImpl.finishSampling(count);
        List<CompositeData> result = new ArrayList<>(count);
        for (Sample counter : samplerResults) {
            result.add(new CompositeDataSupport(COUNTER_COMPOSITE_TYPE, COUNTER_NAMES, new Object[] { keyspace.getName() + "." + name, counter.count, counter.error, samplerImpl.toString(counter.value) }));
=======
public CompositeData finishLocalSampling(String sampler, int count) throws OpenDataException {
        SamplerResult<ByteBuffer> samplerResults = metric.samplers.get(Sampler.valueOf(sampler)).finishSampling(count);
        TabularDataSupport result = new TabularDataSupport(COUNTER_TYPE);
        for (Counter<ByteBuffer> counter : samplerResults.topK) {
            ByteBuffer key = counter.getItem();
            result.put(new CompositeDataSupport(COUNTER_COMPOSITE_TYPE, COUNTER_NAMES, new Object[] { ByteBufferUtil.bytesToHex(key), counter.getCount(), counter.getError(), metadata.getKeyValidator().getString(key) }));
>>>>>>> YOURS
        }
        return result;
    }

    public boolean isCompactionDiskSpaceCheckEnabled() {
        return compactionSpaceCheck;
    }

    public void compactionDiskSpaceCheck(boolean enable) {
        compactionSpaceCheck = enable;
    }

    public void cleanupCache() {
        Collection<Range<Token>> ranges = StorageService.instance.getLocalReplicas(keyspace.getName()).ranges();
        for (Iterator<RowCacheKey> keyIter = CacheService.instance.rowCache.keyIterator(); keyIter.hasNext(); ) {
            RowCacheKey key = keyIter.next();
            DecoratedKey dk = decorateKey(ByteBuffer.wrap(key.key));
            if (key.sameTable(metadata()) && !Range.isInRanges(dk.getToken(), ranges))
                invalidateCachedPartition(dk);
        }
        if (metadata().isCounter()) {
            for (Iterator<CounterCacheKey> keyIter = CacheService.instance.counterCache.keyIterator(); keyIter.hasNext(); ) {
                CounterCacheKey key = keyIter.next();
                DecoratedKey dk = decorateKey(key.partitionKey());
                if (key.sameTable(metadata()) && !Range.isInRanges(dk.getToken(), ranges))
                    CacheService.instance.counterCache.remove(key);
            }
        }
    }

    public ClusteringComparator getComparator() {
        return metadata().comparator;
    }

    public void snapshotWithoutFlush(String snapshotName) {
        snapshotWithoutFlush(snapshotName, null, false);
    }

    public Set<SSTableReader> snapshotWithoutFlush(String snapshotName, Predicate<SSTableReader> predicate, boolean ephemeral) {
        Set<SSTableReader> snapshottedSSTables = new HashSet<>();
        final JSONArray filesJSONArr = new JSONArray();
        for (ColumnFamilyStore cfs : concatWithIndexes()) {
            try (RefViewFragment currentView = cfs.selectAndReference(View.select(SSTableSet.CANONICAL, (x) -> predicate == null || predicate.apply(x)))) {
                for (SSTableReader ssTable : currentView.sstables) {
                    File snapshotDirectory = Directories.getSnapshotDirectory(ssTable.descriptor, snapshotName);
                    ssTable.createLinks(snapshotDirectory.getPath());
                    filesJSONArr.add(ssTable.descriptor.relativeFilenameFor(Component.DATA));
                    if (logger.isTraceEnabled())
                        logger.trace("Snapshot for {} keyspace data file {} created in {}", keyspace, ssTable.getFilename(), snapshotDirectory);
                    snapshottedSSTables.add(ssTable);
                }
            }
        }
        writeSnapshotManifest(filesJSONArr, snapshotName);
<<<<<<< MINE
        if (!SchemaConstants.isLocalSystemKeyspace(metadata.keyspace) && !SchemaConstants.isReplicatedSystemKeyspace(metadata.keyspace))
=======
        if (!SchemaConstants.isLocalSystemKeyspace(metadata.ksName) && !SchemaConstants.isReplicatedSystemKeyspace(metadata.ksName))
>>>>>>> YOURS
            writeSnapshotSchema(snapshotName);
        if (ephemeral)
            createEphemeralSnapshotMarkerFile(snapshotName);
        return snapshottedSSTables;
    }

    private void writeSnapshotManifest(final JSONArray filesJSONArr, final String snapshotName) {
        final File manifestFile = getDirectories().getSnapshotManifestFile(snapshotName);
        try {
            if (!manifestFile.getParentFile().exists())
                manifestFile.getParentFile().mkdirs();
            try (PrintStream out = new PrintStream(manifestFile)) {
                final JSONObject manifestJSON = new JSONObject();
                manifestJSON.put("files", filesJSONArr);
                out.println(manifestJSON.toJSONString());
            }
        } catch (IOException e) {
            throw new FSWriteError(e, manifestFile);
        }
    }

    private void writeSnapshotSchema(final String snapshotName) {
        final File schemaFile = getDirectories().getSnapshotSchemaFile(snapshotName);
        try {
            if (!schemaFile.getParentFile().exists())
                schemaFile.getParentFile().mkdirs();
            try (PrintStream out = new PrintStream(schemaFile)) {
                SchemaCQLHelper.reCreateStatementsForSchemaCql(metadata(), keyspace.getMetadata().types).forEach(out::println);
            }
        } catch (IOException e) {
            throw new FSWriteError(e, schemaFile);
        }
    }

    private void createEphemeralSnapshotMarkerFile(final String snapshot) {
        final File ephemeralSnapshotMarker = getDirectories().getNewEphemeralSnapshotMarkerFile(snapshot);
        try {
            if (!ephemeralSnapshotMarker.getParentFile().exists())
                ephemeralSnapshotMarker.getParentFile().mkdirs();
            Files.createFile(ephemeralSnapshotMarker.toPath());
            if (logger.isTraceEnabled())
            logger.trace("Created ephemeral snapshot marker file on {}.", ephemeralSnapshotMarker.getAbsolutePath());
        } catch (IOException e) {
            logger.warn(String.format("Could not create marker file %s for ephemeral snapshot %s. " + "In case there is a failure in the operation that created " + "this snapshot, you may need to clean it manually afterwards.", ephemeralSnapshotMarker.getAbsolutePath(), snapshot), e);
        }
    }

    protected static void clearEphemeralSnapshots(Directories directories) {
        for (String ephemeralSnapshot : directories.listEphemeralSnapshots()) {
            logger.trace("Clearing ephemeral snapshot {} leftover from previous session.", ephemeralSnapshot);
            Directories.clearSnapshot(ephemeralSnapshot, directories.getCFDirectories());
        }
    }

    public Refs<SSTableReader> getSnapshotSSTableReaders(String tag) throws IOException {
        Map<Integer, SSTableReader> active = new HashMap<>();
        for (SSTableReader sstable : getSSTables(SSTableSet.CANONICAL)) active.put(sstable.descriptor.generation, sstable);
        Map<Descriptor, Set<Component>> snapshots = getDirectories().sstableLister(Directories.OnTxnErr.IGNORE).snapshots(tag).list();
        Refs<SSTableReader> refs = new Refs<>();
        try {
            for (Map.Entry<Descriptor, Set<Component>> entries : snapshots.entrySet()) {
                SSTableReader sstable = active.get(entries.getKey().generation);
                if (sstable == null || !refs.tryRef(sstable)) {
                    if (logger.isTraceEnabled())
                        logger.trace("using snapshot sstable {}", entries.getKey());
                    sstable = SSTableReader.open(entries.getKey(), entries.getValue(), metadata, true, true);
                    refs.tryRef(sstable);
                    sstable.selfRef().release();
                } else if (logger.isTraceEnabled()) {
                    logger.trace("using active sstable {}", entries.getKey());
                }
            }
        } catch (FSReadError | RuntimeException e) {
            refs.release();
            throw e;
        }
        return refs;
    }

    public Set<SSTableReader> snapshot(String snapshotName) {
        return snapshot(snapshotName, false);
    }

    public Set<SSTableReader> snapshot(String snapshotName, boolean skipFlush) {
        return snapshot(snapshotName, null, false, skipFlush);
    }

    public Set<SSTableReader> snapshot(String snapshotName, Predicate<SSTableReader> predicate, boolean ephemeral, boolean skipFlush) {
        if (!skipFlush) {
            forceBlockingFlush();
        }
        return snapshotWithoutFlush(snapshotName, predicate, ephemeral);
    }

    public boolean snapshotExists(String snapshotName) {
        return getDirectories().snapshotExists(snapshotName);
    }

    public long getSnapshotCreationTime(String snapshotName) {
        return getDirectories().snapshotCreationTime(snapshotName);
    }

    public void clearSnapshot(String snapshotName) {
        List<File> snapshotDirs = getDirectories().getCFDirectories();
        Directories.clearSnapshot(snapshotName, snapshotDirs);
    }

    public Map<String, Directories.SnapshotSizeDetails> getSnapshotDetails() {
        return getDirectories().getSnapshotDetails();
    }

    public CachedPartition getRawCachedPartition(DecoratedKey key) {
        if (!isRowCacheEnabled())
            return null;
        IRowCacheEntry cached = CacheService.instance.rowCache.getInternal(new RowCacheKey(metadata(), key));
        return cached == null || cached instanceof RowCacheSentinel ? null : (CachedPartition) cached;
    }

    private void invalidateCaches() {
        CacheService.instance.invalidateKeyCacheForCf(metadata());
        CacheService.instance.invalidateRowCacheForCf(metadata());
        if (metadata().isCounter())
            CacheService.instance.invalidateCounterCacheForCf(metadata());
    }

    public int invalidateRowCache(Collection<Bounds<Token>> boundsToInvalidate) {
        int invalidatedKeys = 0;
        for (Iterator<RowCacheKey> keyIter = CacheService.instance.rowCache.keyIterator(); keyIter.hasNext(); ) {
            RowCacheKey key = keyIter.next();
            DecoratedKey dk = decorateKey(ByteBuffer.wrap(key.key));
            if (key.sameTable(metadata()) && Bounds.isInBounds(dk.getToken(), boundsToInvalidate)) {
                invalidateCachedPartition(dk);
                invalidatedKeys++;
            }
        }
        return invalidatedKeys;
    }

    public int invalidateCounterCache(Collection<Bounds<Token>> boundsToInvalidate) {
        int invalidatedKeys = 0;
        for (Iterator<CounterCacheKey> keyIter = CacheService.instance.counterCache.keyIterator(); keyIter.hasNext(); ) {
            CounterCacheKey key = keyIter.next();
            DecoratedKey dk = decorateKey(key.partitionKey());
            if (key.sameTable(metadata()) && Bounds.isInBounds(dk.getToken(), boundsToInvalidate)) {
                CacheService.instance.counterCache.remove(key);
                invalidatedKeys++;
            }
        }
        return invalidatedKeys;
    }

    public boolean containsCachedParition(DecoratedKey key) {
        return CacheService.instance.rowCache.getCapacity() != 0 && CacheService.instance.rowCache.containsKey(new RowCacheKey(metadata(), key));
    }

    public void invalidateCachedPartition(RowCacheKey key) {
        CacheService.instance.rowCache.remove(key);
    }

    public void invalidateCachedPartition(DecoratedKey key) {
        if (!isRowCacheEnabled())
            return;
        invalidateCachedPartition(new RowCacheKey(metadata(), key));
    }

    public ClockAndCount getCachedCounter(ByteBuffer partitionKey, Clustering<?> clustering, ColumnMetadata column, CellPath path) {
        if (CacheService.instance.counterCache.getCapacity() == 0L)
            return null;
        return CacheService.instance.counterCache.get(CounterCacheKey.create(metadata(), partitionKey, clustering, column, path));
    }

    public void putCachedCounter(ByteBuffer partitionKey, Clustering<?> clustering, ColumnMetadata column, CellPath path, ClockAndCount clockAndCount) {
        if (CacheService.instance.counterCache.getCapacity() == 0L)
            return;
        CacheService.instance.counterCache.put(CounterCacheKey.create(metadata(), partitionKey, clustering, column, path), clockAndCount);
    }

    public void forceMajorCompaction() throws InterruptedException, ExecutionException {
        forceMajorCompaction(false);
    }

    public void forceMajorCompaction(boolean splitOutput) throws InterruptedException, ExecutionException {
        CompactionManager.instance.performMaximal(this, splitOutput);
    }

    public void forceCompactionForTokenRange(Collection<Range<Token>> tokenRanges) throws ExecutionException, InterruptedException {
        CompactionManager.instance.forceCompactionForTokenRange(this, tokenRanges);
    }

    public static Iterable<ColumnFamilyStore> all() {
        List<Iterable<ColumnFamilyStore>> stores = new ArrayList<Iterable<ColumnFamilyStore>>(Schema.instance.getKeyspaces().size());
        for (Keyspace keyspace : Keyspace.all()) {
            stores.add(keyspace.getColumnFamilyStores());
        }
        return Iterables.concat(stores);
    }

    public Iterable<DecoratedKey> keySamples(Range<Token> range) {
        try (RefViewFragment view = selectAndReference(View.selectFunction(SSTableSet.CANONICAL))) {
            Iterable<DecoratedKey>[] samples = new Iterable[view.sstables.size()];
            int i = 0;
            for (SSTableReader sstable : view.sstables) {
                samples[i++] = sstable.getKeySamples(range);
            }
            return Iterables.concat(samples);
        }
    }

    public long estimatedKeysForRange(Range<Token> range) {
        try (RefViewFragment view = selectAndReference(View.selectFunction(SSTableSet.CANONICAL))) {
            long count = 0;
            for (SSTableReader sstable : view.sstables) count += sstable.estimatedKeysForRanges(Collections.singleton(range));
            return count;
        }
    }

    @VisibleForTesting
    public void clearUnsafe() {
        for (final ColumnFamilyStore cfs : concatWithIndexes()) {
            cfs.runWithCompactionsDisabled(new Callable<Void>() {

                public Void call() {
                    cfs.data.reset(new Memtable(new AtomicReference<>(CommitLogPosition.NONE), cfs));
                    return null;
                }
            }, true, false);
        }
    }

    public void truncateBlocking() {
        logger.info("Truncating {}.{}", keyspace.getName(), name);
        final long truncatedAt;
        final CommitLogPosition replayAfter;
        if (keyspace.getMetadata().params.durableWrites || DatabaseDescriptor.isAutoSnapshot()) {
            replayAfter = forceBlockingFlush();
            viewManager.forceBlockingFlush();
        } else {
            viewManager.dumpMemtables();
            try {
                replayAfter = dumpMemtable().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        long now = System.currentTimeMillis();
        for (ColumnFamilyStore cfs : concatWithIndexes()) for (SSTableReader sstable : cfs.getLiveSSTables()) now = Math.max(now, sstable.maxDataAge);
        truncatedAt = now;
        Runnable truncateRunnable = new Runnable() {

            public void run() {
                logger.info("Truncating {}.{} with truncatedAt={}", keyspace.getName(), getTableName(), truncatedAt);
                ActiveRepairService.instance.abort((prs) -> prs.getTableIds().contains(metadata.id), "Stopping parent sessions {} due to truncation of tableId=" + metadata.id);
                data.notifyTruncated(truncatedAt);
                if (DatabaseDescriptor.isAutoSnapshot())
                    snapshot(Keyspace.getTimestampedSnapshotNameWithPrefix(name, SNAPSHOT_TRUNCATE_PREFIX));
                discardSSTables(truncatedAt);
                indexManager.truncateAllIndexesBlocking(truncatedAt);
                viewManager.truncateBlocking(replayAfter, truncatedAt);
                SystemKeyspace.saveTruncationRecord(ColumnFamilyStore.this, truncatedAt, replayAfter);
                logger.trace("cleaning out row cache");
                invalidateCaches();
            }
        };
        runWithCompactionsDisabled(Executors.callable(truncateRunnable), true, true);
        logger.info("Truncate of {}.{} is complete", keyspace.getName(), name);
    }

    public Future<CommitLogPosition> dumpMemtable() {
        synchronized (data) {
            final Flush flush = new Flush(true);
            flushExecutor.execute(flush);
            postFlushExecutor.execute(flush.postFlushTask);
            return flush.postFlushTask;
        }
    }

    public <V> V runWithCompactionsDisabled(Callable<V> callable, boolean interruptValidation, boolean interruptViews) {
        return runWithCompactionsDisabled(callable, (sstable) -> true, interruptValidation, interruptViews, true);
    }

    public <V> V runWithCompactionsDisabled(Callable<V> callable, Predicate<SSTableReader> sstablesPredicate, boolean interruptValidation, boolean interruptViews, boolean interruptIndexes) {
        synchronized (this) {
            logger.trace("Cancelling in-progress compactions for {}", metadata.name);
            Iterable<ColumnFamilyStore> toInterruptFor = interruptIndexes ? concatWithIndexes() : Collections.singleton(this);
            toInterruptFor = interruptViews ? Iterables.concat(toInterruptFor, viewManager.allViewsCfs()) : toInterruptFor;
            try (CompactionManager.CompactionPauser pause = CompactionManager.instance.pauseGlobalCompaction();
                CompactionManager.CompactionPauser pausedStrategies = pauseCompactionStrategies(toInterruptFor)) {
                CompactionManager.instance.interruptCompactionForCFs(toInterruptFor, sstablesPredicate, interruptValidation);
                CompactionManager.instance.waitForCessation(toInterruptFor, sstablesPredicate);
                for (ColumnFamilyStore cfs : toInterruptFor) {
                    if (cfs.getTracker().getCompacting().stream().anyMatch(sstablesPredicate)) {
                        logger.warn("Unable to cancel in-progress compactions for {}.  Perhaps there is an unusually large row in progress somewhere, or the system is simply overloaded.", metadata.name);
                        return null;
                    }
                }
                logger.trace("Compactions successfully cancelled");
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static CompactionManager.CompactionPauser pauseCompactionStrategies(Iterable<ColumnFamilyStore> toPause) {
        ArrayList<ColumnFamilyStore> successfullyPaused = new ArrayList<>();
        try {
            for (ColumnFamilyStore cfs : toPause) {
                successfullyPaused.ensureCapacity(successfullyPaused.size() + 1);
                cfs.getCompactionStrategyManager().pause();
                successfullyPaused.add(cfs);
            }
            return () -> maybeFail(resumeAll(null, toPause));
        } catch (Throwable t) {
            resumeAll(t, successfullyPaused);
            throw t;
        }
    }

    private static Throwable resumeAll(Throwable accumulate, Iterable<ColumnFamilyStore> cfss) {
        for (ColumnFamilyStore cfs : cfss) {
            try {
                cfs.getCompactionStrategyManager().resume();
            } catch (Throwable t) {
                accumulate = merge(accumulate, t);
            }
        }
        return accumulate;
    }

    public LifecycleTransaction markAllCompacting(final OperationType operationType) {
        Callable<LifecycleTransaction> callable = new Callable<LifecycleTransaction>() {

            public LifecycleTransaction call() {
                assert data.getCompacting().isEmpty() : data.getCompacting();
                Iterable<SSTableReader> sstables = getLiveSSTables();
                sstables = AbstractCompactionStrategy.filterSuspectSSTables(sstables);
                LifecycleTransaction modifier = data.tryModify(sstables, operationType);
                assert modifier != null : "something marked things compacting while compactions are disabled";
                return modifier;
            }
        };
        return runWithCompactionsDisabled(callable, false, false);
    }

    @Override
    public String toString() {
        return "CFS(" + "Keyspace='" + keyspace.getName() + '\'' + ", ColumnFamily='" + name + '\'' + ')';
    }

    public void disableAutoCompaction() {
        compactionStrategyManager.disable();
    }

    public void enableAutoCompaction() {
        enableAutoCompaction(false);
    }

    @VisibleForTesting
    public void enableAutoCompaction(boolean waitForFutures) {
        compactionStrategyManager.enable();
        List<Future<?>> futures = CompactionManager.instance.submitBackground(this);
        if (waitForFutures)
            FBUtilities.waitOnFutures(futures);
    }

    public boolean isAutoCompactionDisabled() {
        return !this.compactionStrategyManager.isEnabled();
    }

    public CompactionStrategyManager getCompactionStrategyManager() {
        return compactionStrategyManager;
    }

    public void setCrcCheckChance(double crcCheckChance) {
        try {
            TableParams.builder().crcCheckChance(crcCheckChance).build().validate();
            for (ColumnFamilyStore cfs : concatWithIndexes()) {
                cfs.crcCheckChance.set(crcCheckChance);
                for (SSTableReader sstable : cfs.getSSTables(SSTableSet.LIVE)) sstable.setCrcCheckChance(crcCheckChance);
            }
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Double getCrcCheckChance() {
        return crcCheckChance.value();
    }

    public void setCompactionThresholds(int minThreshold, int maxThreshold) {
        validateCompactionThresholds(minThreshold, maxThreshold);
        minCompactionThreshold.set(minThreshold);
        maxCompactionThreshold.set(maxThreshold);
        CompactionManager.instance.submitBackground(this);
    }

    public int getMinimumCompactionThreshold() {
        return minCompactionThreshold.value();
    }

    public void setMinimumCompactionThreshold(int minCompactionThreshold) {
        validateCompactionThresholds(minCompactionThreshold, maxCompactionThreshold.value());
        this.minCompactionThreshold.set(minCompactionThreshold);
    }

    public int getMaximumCompactionThreshold() {
        return maxCompactionThreshold.value();
    }

    public void setMaximumCompactionThreshold(int maxCompactionThreshold) {
        validateCompactionThresholds(minCompactionThreshold.value(), maxCompactionThreshold);
        this.maxCompactionThreshold.set(maxCompactionThreshold);
    }

    private void validateCompactionThresholds(int minThreshold, int maxThreshold) {
        if (minThreshold > maxThreshold)
            throw new RuntimeException(String.format("The min_compaction_threshold cannot be larger than the max_compaction_threshold. " + "Min is '%d', Max is '%d'.", minThreshold, maxThreshold));
        if (maxThreshold == 0 || minThreshold == 0)
            throw new RuntimeException("Disabling compaction by setting min_compaction_threshold or max_compaction_threshold to 0 " + "is deprecated, set the compaction strategy option 'enabled' to 'false' instead or use the nodetool command 'disableautocompaction'.");
    }

    public int getMeanEstimatedCellPerPartitionCount() {
        long sum = 0;
        long count = 0;
        for (SSTableReader sstable : getSSTables(SSTableSet.CANONICAL)) {
            long n = sstable.getEstimatedCellPerPartitionCount().count();
            sum += sstable.getEstimatedCellPerPartitionCount().mean() * n;
            count += n;
        }
        return count > 0 ? (int) (sum / count) : 0;
    }

    public double getMeanPartitionSize() {
        long sum = 0;
        long count = 0;
        for (SSTableReader sstable : getSSTables(SSTableSet.CANONICAL)) {
            long n = sstable.getEstimatedPartitionSize().count();
            sum += sstable.getEstimatedPartitionSize().mean() * n;
            count += n;
        }
        return count > 0 ? sum * 1.0 / count : 0;
    }

    public int getMeanRowCount() {
        long totalRows = 0;
        long totalPartitions = 0;
        for (SSTableReader sstable : getSSTables(SSTableSet.CANONICAL)) {
            totalPartitions += sstable.getEstimatedPartitionSize().count();
            totalRows += sstable.getTotalRows();
        }
        return totalPartitions > 0 ? (int) (totalRows / totalPartitions) : 0;
    }

    public long estimateKeys() {
        long n = 0;
        for (SSTableReader sstable : getSSTables(SSTableSet.CANONICAL)) n += sstable.estimatedKeys();
        return n;
    }

    public IPartitioner getPartitioner() {
        return metadata().partitioner;
    }

    public DecoratedKey decorateKey(ByteBuffer key) {
        return getPartitioner().decorateKey(key);
    }

    public boolean isIndex() {
        return metadata().isIndex();
    }

    public Iterable<ColumnFamilyStore> concatWithIndexes() {
        return Iterables.concat(Collections.singleton(this), indexManager.getAllIndexColumnFamilyStores());
    }

    public List<String> getBuiltIndexes() {
        return indexManager.getBuiltIndexNames();
    }

    public int getUnleveledSSTables() {
        return compactionStrategyManager.getUnleveledSSTables();
    }

    public int[] getSSTableCountPerLevel() {
        return compactionStrategyManager.getSSTableCountPerLevel();
    }

    public int getLevelFanoutSize() {
        return compactionStrategyManager.getLevelFanoutSize();
    }

    public static class ViewFragment {

        public final List<SSTableReader> sstables;

        public final Iterable<Memtable> memtables;

        public ViewFragment(List<SSTableReader> sstables, Iterable<Memtable> memtables) {
            this.sstables = sstables;
            this.memtables = memtables;
        }
    }

    public static class RefViewFragment extends ViewFragment implements AutoCloseable {

        public final Refs<SSTableReader> refs;

        public RefViewFragment(List<SSTableReader> sstables, Iterable<Memtable> memtables, Refs<SSTableReader> refs) {
            super(sstables, memtables);
            this.refs = refs;
        }

        public void release() {
            refs.release();
        }

        public void close() {
            refs.release();
        }
    }

    public boolean isEmpty() {
        return data.getView().isEmpty();
    }

    public boolean isRowCacheEnabled() {
        boolean retval = metadata().params.caching.cacheRows() && CacheService.instance.rowCache.getCapacity() > 0;
        assert (!retval || !isIndex());
        return retval;
    }

    public boolean isCounterCacheEnabled() {
        return metadata().isCounter() && CacheService.instance.counterCache.getCapacity() > 0;
    }

    public boolean isKeyCacheEnabled() {
        return metadata().params.caching.cacheKeys() && CacheService.instance.keyCache.getCapacity() > 0;
    }

    public void discardSSTables(long truncatedAt) {
        assert data.getCompacting().isEmpty() : data.getCompacting();
        List<SSTableReader> truncatedSSTables = new ArrayList<>();
        int keptSSTables = 0;
        for (SSTableReader sstable : getSSTables(SSTableSet.LIVE)) {
            if (!sstable.newSince(truncatedAt)) {
                truncatedSSTables.add(sstable);
            } else {
                keptSSTables++;
                logger.info("Truncation is keeping {} maxDataAge={} truncatedAt={}", sstable, sstable.maxDataAge, truncatedAt);
        }
        }
        if (!truncatedSSTables.isEmpty()) {
            logger.info("Truncation is dropping {} sstables and keeping {} due to sstable.maxDataAge > truncatedAt", truncatedSSTables.size(), keptSSTables);
            markObsolete(truncatedSSTables, OperationType.UNKNOWN);
    }
    }

    public double getDroppableTombstoneRatio() {
        double allDroppable = 0;
        long allColumns = 0;
        int localTime = (int) (System.currentTimeMillis() / 1000);
        for (SSTableReader sstable : getSSTables(SSTableSet.LIVE)) {
            allDroppable += sstable.getDroppableTombstonesBefore(localTime - metadata().params.gcGraceSeconds);
            allColumns += sstable.getEstimatedCellPerPartitionCount().mean() * sstable.getEstimatedCellPerPartitionCount().count();
        }
        return allColumns > 0 ? allDroppable / allColumns : 0;
    }

    public long trueSnapshotsSize() {
        return getDirectories().trueSnapshotsSize();
    }

    public static ColumnFamilyStore getIfExists(TableId id) {
        TableMetadata metadata = Schema.instance.getTableMetadata(id);
        if (metadata == null)
            return null;
        Keyspace keyspace = Keyspace.open(metadata.keyspace);
        if (keyspace == null)
            return null;
        return keyspace.hasColumnFamilyStore(id) ? keyspace.getColumnFamilyStore(id) : null;
    }

    public static ColumnFamilyStore getIfExists(String ksName, String cfName) {
        if (ksName == null || cfName == null)
            return null;
        Keyspace keyspace = Keyspace.open(ksName);
        if (keyspace == null)
            return null;
        TableMetadata table = Schema.instance.getTableMetadata(ksName, cfName);
        if (table == null)
            return null;
        return keyspace.getColumnFamilyStore(table.id);
    }

    public static TableMetrics metricsFor(TableId tableId) {
        return getIfExists(tableId).metric;
    }

    public DiskBoundaries getDiskBoundaries() {
        return diskBoundaryManager.getDiskBoundaries(this);
    }

    public void invalidateDiskBoundaries() {
        diskBoundaryManager.invalidate();
    }

    @Override
    public void setNeverPurgeTombstones(boolean value) {
        if (neverPurgeTombstones != value)
            logger.info("Changing neverPurgeTombstones for {}.{} from {} to {}", keyspace.getName(), getTableName(), neverPurgeTombstones, value);
        else
            logger.info("Not changing neverPurgeTombstones for {}.{}, it is {}", keyspace.getName(), getTableName(), neverPurgeTombstones);
        neverPurgeTombstones = value;
    }

    @Override
    public boolean getNeverPurgeTombstones() {
        return neverPurgeTombstones;
    }
}