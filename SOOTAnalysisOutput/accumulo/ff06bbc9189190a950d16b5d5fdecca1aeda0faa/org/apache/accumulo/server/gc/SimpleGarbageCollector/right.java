package org.apache.accumulo.server.gc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.accumulo.cloudtrace.instrument.CountSampler;
import org.apache.accumulo.cloudtrace.instrument.Sampler;
import org.apache.accumulo.cloudtrace.instrument.Span;
import org.apache.accumulo.cloudtrace.instrument.Trace;
import org.apache.accumulo.cloudtrace.instrument.thrift.TraceWrap;
import org.apache.accumulo.cloudtrace.thrift.TInfo;
import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.cli.Help;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.IsolatedScanner;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.impl.Tables;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.KeyExtent;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.FileOperations;
import org.apache.accumulo.core.file.FileUtil;
import org.apache.accumulo.core.gc.thrift.GCMonitorService.Iface;
import org.apache.accumulo.core.gc.thrift.GCMonitorService.Processor;
import org.apache.accumulo.core.gc.thrift.GCStatus;
import org.apache.accumulo.core.gc.thrift.GcCycleStats;
import org.apache.accumulo.core.master.state.tables.TableState;
import org.apache.accumulo.core.security.thrift.AuthInfo;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.accumulo.core.util.NamingThreadFactory;
import org.apache.accumulo.core.util.ServerServices;
import org.apache.accumulo.core.util.ServerServices.Service;
import org.apache.accumulo.core.util.UtilWaitThread;
import org.apache.accumulo.core.zookeeper.ZooUtil;
import org.apache.accumulo.fate.zookeeper.ZooLock.LockLossReason;
import org.apache.accumulo.fate.zookeeper.ZooLock.LockWatcher;
import org.apache.accumulo.server.Accumulo;
import org.apache.accumulo.server.ServerConstants;
import org.apache.accumulo.server.client.HdfsZooInstance;
import org.apache.accumulo.server.conf.ServerConfiguration;
import org.apache.accumulo.server.master.state.tables.TableManager;
import org.apache.accumulo.server.security.SecurityConstants;
import org.apache.accumulo.server.security.SecurityUtil;
import org.apache.accumulo.server.trace.TraceFileSystem;
import org.apache.accumulo.server.util.Halt;
import org.apache.accumulo.server.util.OfflineMetadataScanner;
import org.apache.accumulo.server.util.TServerUtils;
import org.apache.accumulo.server.util.TabletIterator;
import org.apache.accumulo.server.zookeeper.ZooLock;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Trash;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import com.beust.jcommander.Parameter;

public class SimpleGarbageCollector implements Iface {

    private static final Text EMPTY_TEXT = new Text();

    static class Opts extends Help {

        @Parameter(names = { "-v", "--verbose" }, description = "extra information will get printed to stdout also")
        boolean verbose = false;

        @Parameter(names = { "-s", "--safemode" }, description = "safe mode will not delete files")
        boolean safeMode = false;

        @Parameter(names = { "-o", "--offline" }, description = "offline mode will run once and check data files directly; this is dangerous if accumulo is running or not shut down properly")
        boolean offline = false;

        @Parameter(names = { "-a", "--address" }, description = "specify our local address")
        String address = null;

        @Parameter(names = { "--no-trash" }, description = "do not use the Trash, even if it is configured")
        boolean noTrash = false;
    }

    private static final float CANDIDATE_MEMORY_PERCENTAGE = 0.75f;

    private boolean candidateMemExceeded;

    private static final Logger log = Logger.getLogger(SimpleGarbageCollector.class);

    private AuthInfo credentials;

    private long gcStartDelay;

    private boolean checkForBulkProcessingFiles;

    private FileSystem fs;

    private Trash trash = null;

    private boolean safemode = false, offline = false, verbose = false;

    private String address = "localhost";

    private ZooLock lock;

    private Key continueKey = null;

    private GCStatus status = new GCStatus(new GcCycleStats(), new GcCycleStats(), new GcCycleStats(), new GcCycleStats());

    private int numDeleteThreads;

    private Instance instance;

    public static void main(String[] args) throws UnknownHostException, IOException {
        SecurityUtil.serverLogin();
        Instance instance = HdfsZooInstance.getInstance();
        ServerConfiguration serverConf = new ServerConfiguration(instance);
        final FileSystem fs = FileUtil.getFileSystem(CachedConfiguration.getInstance(), serverConf.getConfiguration());
        Accumulo.init(fs, serverConf, "gc");
        String address = "localhost";
        SimpleGarbageCollector gc = new SimpleGarbageCollector();
        Opts opts = new Opts();
        opts.parseArgs(SimpleGarbageCollector.class.getName(), args);
        if (opts.safeMode)
            gc.setSafeMode();
        if (opts.offline)
            gc.setOffline();
        if (opts.verbose)
            gc.setVerbose();
        if (opts.address != null)
            gc.useAddress(address);
        gc.init(fs, instance, SecurityConstants.getSystemCredentials(), opts.noTrash);
        Accumulo.enableTracing(address, "gc");
        gc.run();
    }

    public SimpleGarbageCollector() {
    }

    public void setSafeMode() {
        this.safemode = true;
    }

    public void setOffline() {
        this.offline = true;
    }

    public void setVerbose() {
        this.verbose = true;
    }

    public void useAddress(String address) {
        this.address = address;
    }

    public void init(FileSystem fs, Instance instance, AuthInfo credentials, boolean noTrash) throws IOException {
        this.fs = TraceFileSystem.wrap(fs);
        this.credentials = credentials;
        this.instance = instance;
        gcStartDelay = instance.getConfiguration().getTimeInMillis(Property.GC_CYCLE_START);
        long gcDelay = instance.getConfiguration().getTimeInMillis(Property.GC_CYCLE_DELAY);
        numDeleteThreads = instance.getConfiguration().getCount(Property.GC_DELETE_THREADS);
        log.info("start delay: " + (offline ? 0 + " sec (offline)" : gcStartDelay + " milliseconds"));
        log.info("time delay: " + gcDelay + " milliseconds");
        log.info("safemode: " + safemode);
        log.info("offline: " + offline);
        log.info("verbose: " + verbose);
        log.info("memory threshold: " + CANDIDATE_MEMORY_PERCENTAGE + " of " + Runtime.getRuntime().maxMemory() + " bytes");
        log.info("delete threads: " + numDeleteThreads);
        if (!noTrash) {
            this.trash = new Trash(fs, fs.getConf());
        }
    }

    private void run() {
        long tStart, tStop;
        if (!offline) {
            try {
                getZooLock(startStatsService());
            } catch (Exception ex) {
                log.error(ex, ex);
                System.exit(1);
            }
            try {
                log.debug("Sleeping for " + gcStartDelay + " milliseconds before beginning garbage collection cycles");
                Thread.sleep(gcStartDelay);
            } catch (InterruptedException e) {
                log.warn(e, e);
                return;
            }
        }
        Sampler sampler = new CountSampler(100);
        while (true) {
            if (sampler.next())
                Trace.on("gc");
            Span gcSpan = Trace.start("loop");
            tStart = System.currentTimeMillis();
            try {
                System.gc();
                candidateMemExceeded = false;
                checkForBulkProcessingFiles = false;
                Span candidatesSpan = Trace.start("getCandidates");
                status.current.started = System.currentTimeMillis();
                SortedSet<String> candidates = getCandidates();
                status.current.candidates = candidates.size();
                candidatesSpan.stop();
                Span confirmDeletesSpan = Trace.start("confirmDeletes");
                confirmDeletes(candidates);
                status.current.inUse = status.current.candidates - candidates.size();
                confirmDeletesSpan.stop();
                if (safemode) {
                    if (verbose)
                        System.out.println("SAFEMODE: There are " + candidates.size() + " data file candidates marked for deletion.%n" + "          Examine the log files to identify them.%n" + "          They can be removed by executing: bin/accumulo gc --offline%n" + "WARNING:  Do not run the garbage collector in offline mode unless you are positive%n" + "          that the accumulo METADATA table is in a clean state, or that accumulo%n" + "          has not yet been run, in the case of an upgrade.");
                    log.info("SAFEMODE: Listing all data file candidates for deletion");
                    for (String s : candidates) log.info("SAFEMODE: " + s);
                    log.info("SAFEMODE: End candidates for deletion");
                } else {
                    Span deleteSpan = Trace.start("deleteFiles");
                    deleteFiles(candidates);
                    log.info("Number of data file candidates for deletion: " + status.current.candidates);
                    log.info("Number of data file candidates still in use: " + status.current.inUse);
                    log.info("Number of successfully deleted data files: " + status.current.deleted);
                    log.info("Number of data files delete failures: " + status.current.errors);
                    deleteSpan.stop();
                    cleanUpDeletedTableDirs(candidates);
                }
                status.current.finished = System.currentTimeMillis();
                status.last = status.current;
                status.current = new GcCycleStats();
            } catch (Exception e) {
                log.error(e, e);
            }
            tStop = System.currentTimeMillis();
            log.info(String.format("Collect cycle took %.2f seconds", ((tStop - tStart) / 1000.0)));
            if (offline)
                break;
            if (candidateMemExceeded) {
                log.info("Gathering of candidates was interrupted due to memory shortage. Bypassing cycle delay to collect the remaining candidates.");
                continue;
            }
            Span waLogs = Trace.start("walogs");
            GarbageCollectWriteAheadLogs walogCollector = new GarbageCollectWriteAheadLogs(instance, fs);
            try {
                log.info("Beginning garbage collection of write-ahead logs");
                walogCollector.collect(status);
            } catch (Exception e) {
                log.error(e, e);
            }
            waLogs.stop();
            gcSpan.stop();
            Trace.offNoFlush();
            try {
                long gcDelay = instance.getConfiguration().getTimeInMillis(Property.GC_CYCLE_DELAY);
                log.debug("Sleeping for " + gcDelay + " milliseconds");
                Thread.sleep(gcDelay);
            } catch (InterruptedException e) {
                log.warn(e, e);
                return;
            }
        }
    }

    private boolean moveToTrash(Path path) throws IOException {
        if (trash == null)
            return false;
        try {
            return trash.moveToTrash(path);
        } catch (FileNotFoundException ex) {
            return false;
        }
    }

    private void cleanUpDeletedTableDirs(SortedSet<String> candidates) throws Exception {
        HashSet<String> tableIdsWithDeletes = new HashSet<String>();
        for (String delete : candidates) {
            if (isDir(delete)) {
                String tableId = delete.split("/")[1];
                tableIdsWithDeletes.add(tableId);
            }
        }
        Tables.clearCache(instance);
        Set<String> tableIdsInZookeeper = Tables.getIdToNameMap(instance).keySet();
        tableIdsWithDeletes.removeAll(tableIdsInZookeeper);
        for (String delTableId : tableIdsWithDeletes) {
            FileStatus[] tabletDirs = fs.listStatus(new Path(ServerConstants.getTablesDir() + "/" + delTableId));
            if (tabletDirs == null)
                continue;
            if (tabletDirs.length == 0) {
                Path p = new Path(ServerConstants.getTablesDir() + "/" + delTableId);
                if (!moveToTrash(p))
                    fs.delete(p, false);
            }
        }
    }

    private void getZooLock(InetSocketAddress addr) throws KeeperException, InterruptedException {
        String address = addr.getHostName() + ":" + addr.getPort();
        String path = ZooUtil.getRoot(HdfsZooInstance.getInstance()) + Constants.ZGC_LOCK;
        LockWatcher lockWatcher = new LockWatcher() {

            public void lostLock(LockLossReason reason) {
                Halt.halt("GC lock in zookeeper lost (reason = " + reason + "), exiting!");
            }
        };
        while (true) {
            lock = new ZooLock(path);
            if (lock.tryLock(lockWatcher, new ServerServices(address, Service.GC_CLIENT).toString().getBytes())) {
                break;
            }
            UtilWaitThread.sleep(1000);
        }
    }

    private InetSocketAddress startStatsService() throws UnknownHostException {
        Processor<Iface> processor = new Processor<Iface>(TraceWrap.service(this));
        int port = instance.getConfiguration().getPort(Property.GC_PORT);
        long maxMessageSize = instance.getConfiguration().getMemoryInBytes(Property.GENERAL_MAX_MESSAGE_SIZE);
        try {
            TServerUtils.startTServer(port, processor, this.getClass().getSimpleName(), "GC Monitor Service", 2, 1000, maxMessageSize);
        } catch (Exception ex) {
            log.fatal(ex, ex);
            throw new RuntimeException(ex);
        }
        return new InetSocketAddress(Accumulo.getLocalAddress(new String[] { "--address", address }), port);
    }

    SortedSet<String> getCandidates() throws Exception {
        TreeSet<String> candidates = new TreeSet<String>();
        if (offline) {
            checkForBulkProcessingFiles = true;
            try {
                for (String validExtension : FileOperations.getValidExtensions()) {
                    for (FileStatus stat : fs.globStatus(new Path(ServerConstants.getTablesDir() + "/*/*/*." + validExtension))) {
                        String cand = stat.getPath().toUri().getPath();
                        if (!cand.contains(ServerConstants.getRootTabletDir())) {
                            candidates.add(cand.substring(ServerConstants.getTablesDir().length()));
                            log.debug("Offline candidate: " + cand);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Unable to check the filesystem for offline candidates. Removing all candidates for deletion to be safe.", e);
                candidates.clear();
            }
            return candidates;
        }
        Scanner scanner = instance.getConnector(credentials).createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
        if (continueKey != null) {
            scanner.setRange(new Range(continueKey, true, Constants.METADATA_DELETES_KEYSPACE.getEndKey(), Constants.METADATA_DELETES_KEYSPACE.isEndKeyInclusive()));
            continueKey = null;
        } else {
            scanner.setRange(Constants.METADATA_DELETES_KEYSPACE);
        }
        checkForBulkProcessingFiles = false;
        for (Entry<Key, Value> entry : scanner) {
            String cand = entry.getKey().getRow().toString().substring(Constants.METADATA_DELETE_FLAG_PREFIX.length());
            candidates.add(cand);
            checkForBulkProcessingFiles |= cand.toLowerCase(Locale.ENGLISH).contains(Constants.BULK_PREFIX);
            if (almostOutOfMemory()) {
                candidateMemExceeded = true;
                log.info("List of delete candidates has exceeded the memory threshold. Attempting to delete what has been gathered so far.");
                continueKey = entry.getKey();
                break;
            }
        }
        return candidates;
    }

    static public boolean almostOutOfMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory() > CANDIDATE_MEMORY_PERCENTAGE * runtime.maxMemory();
    }

    public void confirmDeletes(SortedSet<String> candidates) throws AccumuloException {
        Scanner scanner;
        if (offline) {
            try {
                scanner = new OfflineMetadataScanner(instance.getConfiguration(), fs);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create offline metadata scanner", e);
            }
        } else {
            try {
                scanner = new IsolatedScanner(instance.getConnector(credentials).createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS));
            } catch (AccumuloSecurityException ex) {
                throw new AccumuloException(ex);
            } catch (TableNotFoundException ex) {
                throw new AccumuloException(ex);
            }
        }
        if (checkForBulkProcessingFiles) {
            log.debug("Checking for bulk processing flags");
            scanner.setRange(Constants.METADATA_BLIP_KEYSPACE);
            for (Entry<Key, Value> entry : scanner) {
                String blipPath = entry.getKey().getRow().toString().substring(Constants.METADATA_BLIP_FLAG_PREFIX.length());
                Iterator<String> tailIter = candidates.tailSet(blipPath).iterator();
                int count = 0;
                while (tailIter.hasNext()) {
                    if (tailIter.next().startsWith(blipPath)) {
                        count++;
                        tailIter.remove();
                    } else {
                        break;
                    }
                }
                if (count > 0)
                    log.debug("Folder has bulk processing flag: " + blipPath);
            }
        }
        scanner.clearColumns();
        scanner.fetchColumnFamily(Constants.METADATA_DATAFILE_COLUMN_FAMILY);
        scanner.fetchColumnFamily(Constants.METADATA_SCANFILE_COLUMN_FAMILY);
        Constants.METADATA_DIRECTORY_COLUMN.fetch(scanner);
        TabletIterator tabletIterator = new TabletIterator(scanner, Constants.METADATA_KEYSPACE, false, true);
        while (tabletIterator.hasNext()) {
            Map<Key, Value> tabletKeyValues = tabletIterator.next();
            for (Entry<Key, Value> entry : tabletKeyValues.entrySet()) {
                if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY) || entry.getKey().getColumnFamily().equals(Constants.METADATA_SCANFILE_COLUMN_FAMILY)) {
                    String cf = entry.getKey().getColumnQualifier().toString();
                    String delete;
                    if (cf.startsWith("../")) {
                        delete = cf.substring(2);
                    } else {
                        String table = new String(KeyExtent.tableOfMetadataRow(entry.getKey().getRow()));
                        delete = "/" + table + cf;
                    }
                    if (candidates.remove(delete))
                        log.debug("Candidate was still in use in the METADATA table: " + delete);
                    String path = delete.substring(0, delete.lastIndexOf('/'));
                    if (candidates.remove(path))
                        log.debug("Candidate was still in use in the METADATA table: " + path);
                } else if (Constants.METADATA_DIRECTORY_COLUMN.hasColumns(entry.getKey())) {
                    String table = new String(KeyExtent.tableOfMetadataRow(entry.getKey().getRow()));
                    String delete = "/" + table + entry.getValue().toString();
                    if (candidates.remove(delete))
                        log.debug("Candidate was still in use in the METADATA table: " + delete);
                } else
                    throw new AccumuloException("Scanner over metadata table returned unexpected column : " + entry.getKey());
            }
        }
    }

    private void deleteFiles(SortedSet<String> confirmedDeletes) {
        BatchWriter writer = null;
        if (!offline) {
            Connector c;
            try {
                c = instance.getConnector(SecurityConstants.getSystemCredentials());
                writer = c.createBatchWriter(Constants.METADATA_TABLE_NAME, new BatchWriterConfig());
            } catch (Exception e) {
                log.error("Unable to create writer to remove file from the !METADATA table", e);
            }
        }
        Iterator<String> cdIter = confirmedDeletes.iterator();
        String lastDir = null;
        while (cdIter.hasNext()) {
            String delete = cdIter.next();
            if (isDir(delete)) {
                lastDir = delete;
            } else if (lastDir != null) {
                if (delete.startsWith(lastDir)) {
                    log.debug("Ignoring " + delete + " because " + lastDir + " exist");
                    Mutation m = new Mutation(new Text(Constants.METADATA_DELETE_FLAG_PREFIX + delete));
                    m.putDelete(EMPTY_TEXT, EMPTY_TEXT);
                    try {
                        writer.addMutation(m);
                    } catch (MutationsRejectedException e) {
                        throw new RuntimeException(e);
                    }
                    cdIter.remove();
                } else {
                    lastDir = null;
                }
            }
        }
        final BatchWriter finalWriter = writer;
        ExecutorService deleteThreadPool = Executors.newFixedThreadPool(numDeleteThreads, new NamingThreadFactory("deleting"));
        for (final String delete : confirmedDeletes) {
            Runnable deleteTask = new Runnable() {

                @Override
                public void run() {
                    boolean removeFlag;
                    log.debug("Deleting " + ServerConstants.getTablesDir() + delete);
                    try {
                        Path p = new Path(ServerConstants.getTablesDir() + delete);
                        if (moveToTrash(p) || fs.delete(p, true)) {
                            removeFlag = true;
                            synchronized (SimpleGarbageCollector.this) {
                                ++status.current.deleted;
                            }
                        } else if (fs.exists(p)) {
                            removeFlag = false;
                            synchronized (SimpleGarbageCollector.this) {
                                ++status.current.errors;
                            }
                            log.warn("File exists, but was not deleted for an unknown reason: " + p);
                        } else {
                            removeFlag = true;
                            synchronized (SimpleGarbageCollector.this) {
                                ++status.current.errors;
                            }
                            String[] parts = delete.split("/");
                            if (parts.length > 1) {
                                String tableId = parts[1];
                                TableManager.getInstance().updateTableStateCache(tableId);
                                TableState tableState = TableManager.getInstance().getTableState(tableId);
                                if (tableState != null && tableState != TableState.DELETING)
                                    log.warn("File doesn't exist: " + p);
                            } else {
                                log.warn("Very strange path name: " + delete);
                            }
                        }
                        if (removeFlag && finalWriter != null) {
                            Mutation m = new Mutation(new Text(Constants.METADATA_DELETE_FLAG_PREFIX + delete));
                            m.putDelete(EMPTY_TEXT, EMPTY_TEXT);
                            finalWriter.addMutation(m);
                        }
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                }
            };
            deleteThreadPool.execute(deleteTask);
        }
        deleteThreadPool.shutdown();
        try {
            while (!deleteThreadPool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
            }
        } catch (InterruptedException e1) {
            log.error(e1, e1);
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (MutationsRejectedException e) {
                log.error("Problem removing entries from the metadata table: ", e);
            }
        }
    }

    private boolean isDir(String delete) {
        int slashCount = 0;
        for (int i = 0; i < delete.length(); i++) if (delete.charAt(i) == '/')
            slashCount++;
        return slashCount == 2;
    }

    @Override
    public GCStatus getStatus(TInfo info, AuthInfo credentials) {
        return status;
    }
}
