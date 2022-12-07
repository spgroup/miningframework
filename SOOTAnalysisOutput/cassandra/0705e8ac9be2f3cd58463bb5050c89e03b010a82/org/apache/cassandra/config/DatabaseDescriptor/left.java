package org.apache.cassandra.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Longs;
import org.apache.cassandra.io.sstable.format.SSTableFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.auth.AllowAllAuthenticator;
import org.apache.cassandra.auth.AllowAllAuthorizer;
import org.apache.cassandra.auth.AllowAllInternodeAuthenticator;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.IAuthorizer;
import org.apache.cassandra.auth.IInternodeAuthenticator;
import org.apache.cassandra.config.Config.RequestSchedulerId;
import org.apache.cassandra.config.EncryptionOptions.ClientEncryptionOptions;
import org.apache.cassandra.config.EncryptionOptions.ServerEncryptionOptions;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.DefsTables;
import org.apache.cassandra.db.SystemKeyspace;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.io.FSWriteError;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.io.util.IAllocator;
import org.apache.cassandra.locator.DynamicEndpointSnitch;
import org.apache.cassandra.locator.EndpointSnitchInfo;
import org.apache.cassandra.locator.IEndpointSnitch;
import org.apache.cassandra.locator.SeedProvider;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.scheduler.IRequestScheduler;
import org.apache.cassandra.scheduler.NoScheduler;
import org.apache.cassandra.service.CacheService;
import org.apache.cassandra.thrift.ThriftServer;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.memory.HeapPool;
import org.apache.cassandra.utils.memory.NativePool;
import org.apache.cassandra.utils.memory.MemtablePool;
import org.apache.cassandra.utils.memory.SlabPool;

public class DatabaseDescriptor {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDescriptor.class);

    private static final int MAX_NUM_TOKENS = 1536;

    private static IEndpointSnitch snitch;

    private static InetAddress listenAddress;

    private static InetAddress broadcastAddress;

    private static InetAddress rpcAddress;

    private static InetAddress broadcastRpcAddress;

    private static SeedProvider seedProvider;

    private static IInternodeAuthenticator internodeAuthenticator;

    private static IPartitioner<?> partitioner;

    private static String paritionerName;

    private static Config.DiskAccessMode indexAccessMode;

    private static Config conf;

    private static SSTableFormat.Type sstable_format = SSTableFormat.Type.BIG;

    private static IAuthenticator authenticator = new AllowAllAuthenticator();

    private static IAuthorizer authorizer = new AllowAllAuthorizer();

    private static IRequestScheduler requestScheduler;

    private static RequestSchedulerId requestSchedulerId;

    private static RequestSchedulerOptions requestSchedulerOptions;

    private static long keyCacheSizeInMB;

    private static long counterCacheSizeInMB;

    private static IAllocator memoryAllocator;

    private static long indexSummaryCapacityInMB;

    private static String localDC;

    private static Comparator<InetAddress> localComparator;

    static {
        try {
            applyConfig(loadConfig());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e.getMessage() + "\nFatal configuration error; unable to start. See log for stacktrace.");
        }
    }

    @VisibleForTesting
    public static Config loadConfig() throws ConfigurationException {
        String loaderClass = System.getProperty("cassandra.config.loader");
        ConfigurationLoader loader = loaderClass == null ? new YamlConfigurationLoader() : FBUtilities.<ConfigurationLoader>construct(loaderClass, "configuration loading");
        return loader.loadConfig();
    }

    private static void applyConfig(Config config) throws ConfigurationException {
        conf = config;
        if (conf.commitlog_sync == null) {
            throw new ConfigurationException("Missing required directive CommitLogSync");
        }
        if (conf.commitlog_sync == Config.CommitLogSync.batch) {
            if (conf.commitlog_sync_batch_window_in_ms == null) {
                throw new ConfigurationException("Missing value for commitlog_sync_batch_window_in_ms: Double expected.");
            } else if (conf.commitlog_sync_period_in_ms != null) {
                throw new ConfigurationException("Batch sync specified, but commitlog_sync_period_in_ms found. Only specify commitlog_sync_batch_window_in_ms when using batch sync");
            }
            logger.debug("Syncing log with a batch window of {}", conf.commitlog_sync_batch_window_in_ms);
        } else {
            if (conf.commitlog_sync_period_in_ms == null) {
                throw new ConfigurationException("Missing value for commitlog_sync_period_in_ms: Integer expected");
            } else if (conf.commitlog_sync_batch_window_in_ms != null) {
                throw new ConfigurationException("commitlog_sync_period_in_ms specified, but commitlog_sync_batch_window_in_ms found.  Only specify commitlog_sync_period_in_ms when using periodic sync.");
            }
            logger.debug("Syncing log with a period of {}", conf.commitlog_sync_period_in_ms);
        }
        if (conf.commitlog_total_space_in_mb == null)
            conf.commitlog_total_space_in_mb = hasLargeAddressSpace() ? 8192 : 32;
        if (FBUtilities.isUnix()) {
            if (conf.disk_access_mode == Config.DiskAccessMode.auto) {
                conf.disk_access_mode = hasLargeAddressSpace() ? Config.DiskAccessMode.mmap : Config.DiskAccessMode.standard;
                indexAccessMode = conf.disk_access_mode;
                logger.info("DiskAccessMode 'auto' determined to be {}, indexAccessMode is {}", conf.disk_access_mode, indexAccessMode);
            } else if (conf.disk_access_mode == Config.DiskAccessMode.mmap_index_only) {
                conf.disk_access_mode = Config.DiskAccessMode.standard;
                indexAccessMode = Config.DiskAccessMode.mmap;
                logger.info("DiskAccessMode is {}, indexAccessMode is {}", conf.disk_access_mode, indexAccessMode);
            } else {
                indexAccessMode = conf.disk_access_mode;
                logger.info("DiskAccessMode is {}, indexAccessMode is {}", conf.disk_access_mode, indexAccessMode);
            }
        } else {
            conf.disk_access_mode = Config.DiskAccessMode.standard;
            indexAccessMode = conf.disk_access_mode;
            logger.info("Non-unix environment detected.  DiskAccessMode set to {}, indexAccessMode {}", conf.disk_access_mode, indexAccessMode);
        }
        if (conf.authenticator != null)
            authenticator = FBUtilities.newAuthenticator(conf.authenticator);
        if (conf.authorizer != null)
            authorizer = FBUtilities.newAuthorizer(conf.authorizer);
        if (authenticator instanceof AllowAllAuthenticator && !(authorizer instanceof AllowAllAuthorizer))
            throw new ConfigurationException("AllowAllAuthenticator can't be used with " + conf.authorizer);
        if (conf.internode_authenticator != null)
            internodeAuthenticator = FBUtilities.construct(conf.internode_authenticator, "internode_authenticator");
        else
            internodeAuthenticator = new AllowAllInternodeAuthenticator();
        authenticator.validateConfiguration();
        authorizer.validateConfiguration();
        internodeAuthenticator.validateConfiguration();
        if (conf.partitioner == null) {
            throw new ConfigurationException("Missing directive: partitioner");
        }
        try {
            partitioner = FBUtilities.newPartitioner(System.getProperty("cassandra.partitioner", conf.partitioner));
        } catch (Exception e) {
            throw new ConfigurationException("Invalid partitioner class " + conf.partitioner);
        }
        paritionerName = partitioner.getClass().getCanonicalName();
        if (conf.max_hint_window_in_ms == null) {
            throw new ConfigurationException("max_hint_window_in_ms cannot be set to null");
        }
        if (conf.phi_convict_threshold < 5 || conf.phi_convict_threshold > 16) {
            throw new ConfigurationException("phi_convict_threshold must be between 5 and 16");
        }
        if (conf.concurrent_reads != null && conf.concurrent_reads < 2) {
            throw new ConfigurationException("concurrent_reads must be at least 2");
        }
        if (conf.concurrent_writes != null && conf.concurrent_writes < 2) {
            throw new ConfigurationException("concurrent_writes must be at least 2");
        }
        if (conf.concurrent_counter_writes != null && conf.concurrent_counter_writes < 2)
            throw new ConfigurationException("concurrent_counter_writes must be at least 2");
        if (conf.concurrent_replicates != null)
            logger.warn("concurrent_replicates has been deprecated and should be removed from cassandra.yaml");
        if (conf.file_cache_size_in_mb == null)
            conf.file_cache_size_in_mb = Math.min(512, (int) (Runtime.getRuntime().maxMemory() / (4 * 1048576)));
        if (conf.memtable_offheap_space_in_mb == null)
            conf.memtable_offheap_space_in_mb = (int) (Runtime.getRuntime().maxMemory() / (4 * 1048576));
        if (conf.memtable_offheap_space_in_mb < 0)
            throw new ConfigurationException("memtable_offheap_space_in_mb must be positive");
        if (conf.memtable_heap_space_in_mb == null)
            conf.memtable_heap_space_in_mb = (int) (Runtime.getRuntime().maxMemory() / (4 * 1048576));
        if (conf.memtable_heap_space_in_mb <= 0)
            throw new ConfigurationException("memtable_heap_space_in_mb must be positive");
        logger.info("Global memtable on-heap threshold is enabled at {}MB", conf.memtable_heap_space_in_mb);
        if (conf.memtable_offheap_space_in_mb == 0)
            logger.info("Global memtable off-heap threshold is disabled, HeapAllocator will be used instead");
        else
            logger.info("Global memtable off-heap threshold is enabled at {}MB", conf.memtable_offheap_space_in_mb);
        if (conf.listen_address != null && conf.listen_interface != null) {
            throw new ConfigurationException("Set listen_address OR listen_interface, not both");
        } else if (conf.listen_address != null) {
            try {
                listenAddress = InetAddress.getByName(conf.listen_address);
            } catch (UnknownHostException e) {
                throw new ConfigurationException("Unknown listen_address '" + conf.listen_address + "'");
            }
            if (listenAddress.isAnyLocalAddress())
                throw new ConfigurationException("listen_address cannot be a wildcard address (" + conf.listen_address + ")!");
        } else if (conf.listen_interface != null) {
            try {
                Enumeration<InetAddress> addrs = NetworkInterface.getByName(conf.listen_interface).getInetAddresses();
                listenAddress = addrs.nextElement();
                if (addrs.hasMoreElements())
                    throw new ConfigurationException("Interface " + conf.listen_interface + " can't have more than one address");
            } catch (SocketException e) {
                throw new ConfigurationException("Unknown network interface in listen_interface " + conf.listen_interface);
            }
        }
        if (conf.broadcast_address != null) {
            try {
                broadcastAddress = InetAddress.getByName(conf.broadcast_address);
            } catch (UnknownHostException e) {
                throw new ConfigurationException("Unknown broadcast_address '" + conf.broadcast_address + "'");
            }
            if (broadcastAddress.isAnyLocalAddress())
                throw new ConfigurationException("broadcast_address cannot be a wildcard address (" + conf.broadcast_address + ")!");
        }
        if (conf.rpc_address != null && conf.rpc_interface != null) {
            throw new ConfigurationException("Set rpc_address OR rpc_interface, not both");
        } else if (conf.rpc_address != null) {
            try {
                rpcAddress = InetAddress.getByName(conf.rpc_address);
            } catch (UnknownHostException e) {
                throw new ConfigurationException("Unknown host in rpc_address " + conf.rpc_address);
            }
        } else if (conf.rpc_interface != null) {
            try {
                Enumeration<InetAddress> addrs = NetworkInterface.getByName(conf.rpc_interface).getInetAddresses();
                rpcAddress = addrs.nextElement();
                if (addrs.hasMoreElements())
                    throw new ConfigurationException("Interface " + conf.rpc_interface + " can't have more than one address");
            } catch (SocketException e) {
                throw new ConfigurationException("Unknown network interface in rpc_interface " + conf.rpc_interface);
            }
        } else {
            rpcAddress = FBUtilities.getLocalAddress();
        }
        if (conf.broadcast_rpc_address != null) {
            try {
                broadcastRpcAddress = InetAddress.getByName(conf.broadcast_rpc_address);
            } catch (UnknownHostException e) {
                throw new ConfigurationException("Unknown broadcast_rpc_address '" + conf.broadcast_rpc_address + "'");
            }
            if (broadcastRpcAddress.isAnyLocalAddress())
                throw new ConfigurationException("broadcast_rpc_address cannot be a wildcard address (" + conf.broadcast_rpc_address + ")!");
        } else {
            if (rpcAddress.isAnyLocalAddress())
                throw new ConfigurationException("If rpc_address is set to a wildcard address (" + conf.rpc_address + "), then " + "you must set broadcast_rpc_address to a value other than " + conf.rpc_address);
            broadcastRpcAddress = rpcAddress;
        }
        if (conf.thrift_framed_transport_size_in_mb <= 0)
            throw new ConfigurationException("thrift_framed_transport_size_in_mb must be positive");
        if (conf.native_transport_max_frame_size_in_mb <= 0)
            throw new ConfigurationException("native_transport_max_frame_size_in_mb must be positive");
        if (ThriftServer.HSHA.equals(conf.rpc_server_type) && conf.rpc_max_threads == Integer.MAX_VALUE)
            throw new ConfigurationException("The hsha rpc_server_type is not compatible with an rpc_max_threads " + "setting of 'unlimited'.  Please see the comments in cassandra.yaml " + "for rpc_server_type and rpc_max_threads.");
        if (conf.endpoint_snitch == null) {
            throw new ConfigurationException("Missing endpoint_snitch directive");
        }
        snitch = createEndpointSnitch(conf.endpoint_snitch);
        EndpointSnitchInfo.create();
        localDC = snitch.getDatacenter(FBUtilities.getBroadcastAddress());
        localComparator = new Comparator<InetAddress>() {

            public int compare(InetAddress endpoint1, InetAddress endpoint2) {
                boolean local1 = localDC.equals(snitch.getDatacenter(endpoint1));
                boolean local2 = localDC.equals(snitch.getDatacenter(endpoint2));
                if (local1 && !local2)
                    return -1;
                if (local2 && !local1)
                    return 1;
                return 0;
            }
        };
        requestSchedulerOptions = conf.request_scheduler_options;
        if (conf.request_scheduler != null) {
            try {
                if (requestSchedulerOptions == null) {
                    requestSchedulerOptions = new RequestSchedulerOptions();
                }
                Class<?> cls = Class.forName(conf.request_scheduler);
                requestScheduler = (IRequestScheduler) cls.getConstructor(RequestSchedulerOptions.class).newInstance(requestSchedulerOptions);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Invalid Request Scheduler class " + conf.request_scheduler);
            } catch (Exception e) {
                throw new ConfigurationException("Unable to instantiate request scheduler", e);
            }
        } else {
            requestScheduler = new NoScheduler();
        }
        if (conf.request_scheduler_id == RequestSchedulerId.keyspace) {
            requestSchedulerId = conf.request_scheduler_id;
        } else {
            requestSchedulerId = RequestSchedulerId.keyspace;
        }
        if (conf.commitlog_directory == null) {
            conf.commitlog_directory = System.getProperty("cassandra.storagedir", null);
            if (conf.commitlog_directory == null)
                throw new ConfigurationException("commitlog_directory is missing and -Dcassandra.storagedir is not set");
            conf.commitlog_directory += File.separator + "commitlog";
        }
        if (conf.saved_caches_directory == null) {
            conf.saved_caches_directory = System.getProperty("cassandra.storagedir", null);
            if (conf.saved_caches_directory == null)
                throw new ConfigurationException("saved_caches_directory is missing and -Dcassandra.storagedir is not set");
            conf.saved_caches_directory += File.separator + "saved_caches";
        }
        if (conf.data_file_directories == null) {
            String defaultDataDir = System.getProperty("cassandra.storagedir", null);
            if (defaultDataDir == null)
                throw new ConfigurationException("data_file_directories is not missing and -Dcassandra.storagedir is not set");
            conf.data_file_directories = new String[] { defaultDataDir + File.separator + "data" };
        }
        for (String datadir : conf.data_file_directories) {
            if (datadir.equals(conf.commitlog_directory))
                throw new ConfigurationException("commitlog_directory must not be the same as any data_file_directories");
            if (datadir.equals(conf.saved_caches_directory))
                throw new ConfigurationException("saved_caches_directory must not be the same as any data_file_directories");
        }
        if (conf.commitlog_directory.equals(conf.saved_caches_directory))
            throw new ConfigurationException("saved_caches_directory must not be the same as the commitlog_directory");
        if (conf.memtable_flush_writers == null)
            conf.memtable_flush_writers = Math.min(8, Math.max(2, Math.min(FBUtilities.getAvailableProcessors(), conf.data_file_directories.length)));
        if (conf.memtable_flush_writers < 1)
            throw new ConfigurationException("memtable_flush_writers must be at least 1");
        if (conf.memtable_cleanup_threshold == null)
            conf.memtable_cleanup_threshold = (float) (1.0 / (1 + conf.memtable_flush_writers));
        if (conf.memtable_cleanup_threshold < 0.01f)
            throw new ConfigurationException("memtable_cleanup_threshold must be >= 0.01");
        if (conf.memtable_cleanup_threshold > 0.99f)
            throw new ConfigurationException("memtable_cleanup_threshold must be <= 0.99");
        if (conf.memtable_cleanup_threshold < 0.1f)
            logger.warn("memtable_cleanup_threshold is set very low, which may cause performance degradation");
        if (conf.concurrent_compactors == null)
            conf.concurrent_compactors = Math.min(8, Math.max(2, Math.min(FBUtilities.getAvailableProcessors(), conf.data_file_directories.length)));
        if (conf.concurrent_compactors <= 0)
            throw new ConfigurationException("concurrent_compactors should be strictly greater than 0");
        if (conf.initial_token != null)
            for (String token : tokensFromString(conf.initial_token)) partitioner.getTokenFactory().validate(token);
        if (conf.num_tokens == null)
            conf.num_tokens = 1;
        else if (conf.num_tokens > MAX_NUM_TOKENS)
            throw new ConfigurationException(String.format("A maximum number of %d tokens per node is supported", MAX_NUM_TOKENS));
        try {
            keyCacheSizeInMB = (conf.key_cache_size_in_mb == null) ? Math.min(Math.max(1, (int) (Runtime.getRuntime().totalMemory() * 0.05 / 1024 / 1024)), 100) : conf.key_cache_size_in_mb;
            if (keyCacheSizeInMB < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ConfigurationException("key_cache_size_in_mb option was set incorrectly to '" + conf.key_cache_size_in_mb + "', supported values are <integer> >= 0.");
        }
        try {
            counterCacheSizeInMB = (conf.counter_cache_size_in_mb == null) ? Math.min(Math.max(1, (int) (Runtime.getRuntime().totalMemory() * 0.025 / 1024 / 1024)), 50) : conf.counter_cache_size_in_mb;
            if (counterCacheSizeInMB < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new ConfigurationException("counter_cache_size_in_mb option was set incorrectly to '" + conf.counter_cache_size_in_mb + "', supported values are <integer> >= 0.");
        }
        indexSummaryCapacityInMB = (conf.index_summary_capacity_in_mb == null) ? Math.max(1, (int) (Runtime.getRuntime().totalMemory() * 0.05 / 1024 / 1024)) : conf.index_summary_capacity_in_mb;
        if (indexSummaryCapacityInMB < 0)
            throw new ConfigurationException("index_summary_capacity_in_mb option was set incorrectly to '" + conf.index_summary_capacity_in_mb + "', it should be a non-negative integer.");
        memoryAllocator = FBUtilities.newOffHeapAllocator(conf.memory_allocator);
        if (conf.encryption_options != null) {
            logger.warn("Please rename encryption_options as server_encryption_options in the yaml");
            conf.server_encryption_options = conf.encryption_options;
        }
        List<KSMetaData> systemKeyspaces = Arrays.asList(KSMetaData.systemKeyspace());
        assert systemKeyspaces.size() == Schema.systemKeyspaceNames.size();
        for (KSMetaData ksmd : systemKeyspaces) Schema.instance.load(ksmd);
        if (conf.seed_provider == null) {
            throw new ConfigurationException("seeds configuration is missing; a minimum of one seed is required.");
        }
        try {
            Class<?> seedProviderClass = Class.forName(conf.seed_provider.class_name);
            seedProvider = (SeedProvider) seedProviderClass.getConstructor(Map.class).newInstance(conf.seed_provider.parameters);
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage() + "\nFatal configuration error; unable to start server.  See log for stacktrace.");
        }
        if (seedProvider.getSeeds().size() == 0)
            throw new ConfigurationException("The seed provider lists no seeds.");
        if (conf.batch_size_fail_threshold_in_kb == null) {
            conf.batch_size_fail_threshold_in_kb = conf.batch_size_warn_threshold_in_kb * 10;
        }
    }

    private static IEndpointSnitch createEndpointSnitch(String snitchClassName) throws ConfigurationException {
        if (!snitchClassName.contains("."))
            snitchClassName = "org.apache.cassandra.locator." + snitchClassName;
        IEndpointSnitch snitch = FBUtilities.construct(snitchClassName, "snitch");
        return conf.dynamic_snitch ? new DynamicEndpointSnitch(snitch) : snitch;
    }

    public static void loadSchemas() {
        ColumnFamilyStore schemaCFS = SystemKeyspace.schemaCFS(SystemKeyspace.SCHEMA_KEYSPACES_CF);
        if (schemaCFS.estimateKeys() == 0) {
            logger.info("Couldn't detect any schema definitions in local storage.");
            if (hasExistingNoSystemTables())
                logger.info("Found keyspace data in data directories. Consider using cqlsh to define your schema.");
            else
                logger.info("To create keyspaces and column families, see 'help create' in cqlsh.");
        } else {
            Schema.instance.load(DefsTables.loadFromKeyspace());
        }
        Schema.instance.updateVersion();
    }

    private static boolean hasExistingNoSystemTables() {
        for (String dataDir : getAllDataFileLocations()) {
            File dataPath = new File(dataDir);
            if (dataPath.exists() && dataPath.isDirectory()) {
                int dirCount = dataPath.listFiles(new FileFilter() {

                    public boolean accept(File pathname) {
                        return (pathname.isDirectory() && !Schema.systemKeyspaceNames.contains(pathname.getName()));
                    }
                }).length;
                if (dirCount > 0)
                    return true;
            }
        }
        return false;
    }

    public static IAuthenticator getAuthenticator() {
        return authenticator;
    }

    public static IAuthorizer getAuthorizer() {
        return authorizer;
    }

    public static int getPermissionsValidity() {
        return conf.permissions_validity_in_ms;
    }

    public static void setPermissionsValidity(int timeout) {
        conf.permissions_validity_in_ms = timeout;
    }

    public static int getThriftFramedTransportSize() {
        return conf.thrift_framed_transport_size_in_mb * 1024 * 1024;
    }

    public static void createAllDirectories() {
        try {
            if (conf.data_file_directories.length == 0)
                throw new ConfigurationException("At least one DataFileDirectory must be specified");
            for (String dataFileDirectory : conf.data_file_directories) {
                FileUtils.createDirectory(dataFileDirectory);
            }
            if (conf.commitlog_directory == null)
                throw new ConfigurationException("commitlog_directory must be specified");
            FileUtils.createDirectory(conf.commitlog_directory);
            if (conf.saved_caches_directory == null)
                throw new ConfigurationException("saved_caches_directory must be specified");
            FileUtils.createDirectory(conf.saved_caches_directory);
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException("Bad configuration; unable to start server: " + e.getMessage());
        } catch (FSWriteError e) {
            throw new IllegalStateException(e.getCause().getMessage() + "; unable to start server");
        }
    }

    public static IPartitioner<?> getPartitioner() {
        return partitioner;
    }

    public static String getPartitionerName() {
        return paritionerName;
    }

    public static void setPartitioner(IPartitioner<?> newPartitioner) {
        partitioner = newPartitioner;
    }

    public static IEndpointSnitch getEndpointSnitch() {
        return snitch;
    }

    public static void setEndpointSnitch(IEndpointSnitch eps) {
        snitch = eps;
    }

    public static IRequestScheduler getRequestScheduler() {
        return requestScheduler;
    }

    public static RequestSchedulerOptions getRequestSchedulerOptions() {
        return requestSchedulerOptions;
    }

    public static RequestSchedulerId getRequestSchedulerId() {
        return requestSchedulerId;
    }

    public static int getColumnIndexSize() {
        return conf.column_index_size_in_kb * 1024;
    }

    public static int getBatchSizeWarnThreshold() {
        return conf.batch_size_warn_threshold_in_kb * 1024;
    }

    public static long getBatchSizeFailThreshold() {
        return conf.batch_size_fail_threshold_in_kb * 1024L;
    }

    public static int getBatchSizeFailThresholdInKB() {
        return conf.batch_size_fail_threshold_in_kb;
    }

    public static void setBatchSizeFailThresholdInKB(int threshold) {
        conf.batch_size_fail_threshold_in_kb = threshold;
    }

    public static Collection<String> getInitialTokens() {
        return tokensFromString(System.getProperty("cassandra.initial_token", conf.initial_token));
    }

    public static Collection<String> tokensFromString(String tokenString) {
        List<String> tokens = new ArrayList<String>();
        if (tokenString != null)
            for (String token : tokenString.split(",")) tokens.add(token.replaceAll("^\\s+", "").replaceAll("\\s+$", ""));
        return tokens;
    }

    public static Integer getNumTokens() {
        return conf.num_tokens;
    }

    public static InetAddress getReplaceAddress() {
        try {
            if (System.getProperty("cassandra.replace_address", null) != null)
                return InetAddress.getByName(System.getProperty("cassandra.replace_address", null));
            else if (System.getProperty("cassandra.replace_address_first_boot", null) != null)
                return InetAddress.getByName(System.getProperty("cassandra.replace_address_first_boot", null));
            return null;
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static Collection<String> getReplaceTokens() {
        return tokensFromString(System.getProperty("cassandra.replace_token", null));
    }

    public static UUID getReplaceNode() {
        try {
            return UUID.fromString(System.getProperty("cassandra.replace_node", null));
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static boolean isReplacing() {
        if (System.getProperty("cassandra.replace_address_first_boot", null) != null && SystemKeyspace.bootstrapComplete()) {
            logger.info("Replace address on first boot requested; this node is already bootstrapped");
            return false;
        }
        return getReplaceAddress() != null;
    }

    public static String getClusterName() {
        return conf.cluster_name;
    }

    public static int getMaxStreamingRetries() {
        return conf.max_streaming_retries;
    }

    public static int getStoragePort() {
        return Integer.parseInt(System.getProperty("cassandra.storage_port", conf.storage_port.toString()));
    }

    public static int getSSLStoragePort() {
        return Integer.parseInt(System.getProperty("cassandra.ssl_storage_port", conf.ssl_storage_port.toString()));
    }

    public static int getRpcPort() {
        return Integer.parseInt(System.getProperty("cassandra.rpc_port", conf.rpc_port.toString()));
    }

    public static int getRpcListenBacklog() {
        return conf.rpc_listen_backlog;
    }

    public static long getRpcTimeout() {
        return conf.request_timeout_in_ms;
    }

    public static void setRpcTimeout(Long timeOutInMillis) {
        conf.request_timeout_in_ms = timeOutInMillis;
    }

    public static long getReadRpcTimeout() {
        return conf.read_request_timeout_in_ms;
    }

    public static void setReadRpcTimeout(Long timeOutInMillis) {
        conf.read_request_timeout_in_ms = timeOutInMillis;
    }

    public static long getRangeRpcTimeout() {
        return conf.range_request_timeout_in_ms;
    }

    public static void setRangeRpcTimeout(Long timeOutInMillis) {
        conf.range_request_timeout_in_ms = timeOutInMillis;
    }

    public static long getWriteRpcTimeout() {
        return conf.write_request_timeout_in_ms;
    }

    public static void setWriteRpcTimeout(Long timeOutInMillis) {
        conf.write_request_timeout_in_ms = timeOutInMillis;
    }

    public static long getCounterWriteRpcTimeout() {
        return conf.counter_write_request_timeout_in_ms;
    }

    public static void setCounterWriteRpcTimeout(Long timeOutInMillis) {
        conf.counter_write_request_timeout_in_ms = timeOutInMillis;
    }

    public static long getCasContentionTimeout() {
        return conf.cas_contention_timeout_in_ms;
    }

    public static void setCasContentionTimeout(Long timeOutInMillis) {
        conf.cas_contention_timeout_in_ms = timeOutInMillis;
    }

    public static long getTruncateRpcTimeout() {
        return conf.truncate_request_timeout_in_ms;
    }

    public static void setTruncateRpcTimeout(Long timeOutInMillis) {
        conf.truncate_request_timeout_in_ms = timeOutInMillis;
    }

    public static boolean hasCrossNodeTimeout() {
        return conf.cross_node_timeout;
    }

    public static long getTimeout(MessagingService.Verb verb) {
        switch(verb) {
            case READ:
                return getReadRpcTimeout();
            case RANGE_SLICE:
                return getRangeRpcTimeout();
            case TRUNCATE:
                return getTruncateRpcTimeout();
            case READ_REPAIR:
            case MUTATION:
            case PAXOS_COMMIT:
            case PAXOS_PREPARE:
            case PAXOS_PROPOSE:
                return getWriteRpcTimeout();
            case COUNTER_MUTATION:
                return getCounterWriteRpcTimeout();
            default:
                return getRpcTimeout();
        }
    }

    public static long getMinRpcTimeout() {
        return Longs.min(getRpcTimeout(), getReadRpcTimeout(), getRangeRpcTimeout(), getWriteRpcTimeout(), getCounterWriteRpcTimeout(), getTruncateRpcTimeout());
    }

    public static double getPhiConvictThreshold() {
        return conf.phi_convict_threshold;
    }

    public static void setPhiConvictThreshold(double phiConvictThreshold) {
        conf.phi_convict_threshold = phiConvictThreshold;
    }

    public static int getConcurrentReaders() {
        return conf.concurrent_reads;
    }

    public static int getConcurrentWriters() {
        return conf.concurrent_writes;
    }

    public static int getConcurrentCounterWriters() {
        return conf.concurrent_counter_writes;
    }

    public static int getFlushWriters() {
        return conf.memtable_flush_writers;
    }

    public static int getConcurrentCompactors() {
        return conf.concurrent_compactors;
    }

    public static int getCompactionThroughputMbPerSec() {
        return conf.compaction_throughput_mb_per_sec;
    }

    public static void setCompactionThroughputMbPerSec(int value) {
        conf.compaction_throughput_mb_per_sec = value;
    }

    public static boolean getDisableSTCSInL0() {
        return Boolean.getBoolean("cassandra.disable_stcs_in_l0");
    }

    public static int getStreamThroughputOutboundMegabitsPerSec() {
        return conf.stream_throughput_outbound_megabits_per_sec;
    }

    public static void setStreamThroughputOutboundMegabitsPerSec(int value) {
        conf.stream_throughput_outbound_megabits_per_sec = value;
    }

    public static int getInterDCStreamThroughputOutboundMegabitsPerSec() {
        return conf.inter_dc_stream_throughput_outbound_megabits_per_sec;
    }

    public static void setInterDCStreamThroughputOutboundMegabitsPerSec(int value) {
        conf.inter_dc_stream_throughput_outbound_megabits_per_sec = value;
    }

    public static String[] getAllDataFileLocations() {
        return conf.data_file_directories;
    }

    public static String getCommitLogLocation() {
        return conf.commitlog_directory;
    }

    public static int getTombstoneWarnThreshold() {
        return conf.tombstone_warn_threshold;
    }

    public static void setTombstoneWarnThreshold(int threshold) {
        conf.tombstone_warn_threshold = threshold;
    }

    public static int getTombstoneFailureThreshold() {
        return conf.tombstone_failure_threshold;
    }

    public static void setTombstoneFailureThreshold(int threshold) {
        conf.tombstone_failure_threshold = threshold;
    }

    public static int getCommitLogSegmentSize() {
        return conf.commitlog_segment_size_in_mb * 1024 * 1024;
    }

    public static String getSavedCachesLocation() {
        return conf.saved_caches_directory;
    }

    public static Set<InetAddress> getSeeds() {
        return ImmutableSet.<InetAddress>builder().addAll(seedProvider.getSeeds()).build();
    }

    public static InetAddress getListenAddress() {
        return listenAddress;
    }

    public static InetAddress getBroadcastAddress() {
        return broadcastAddress;
    }

    public static IInternodeAuthenticator getInternodeAuthenticator() {
        return internodeAuthenticator;
    }

    public static void setBroadcastAddress(InetAddress broadcastAdd) {
        broadcastAddress = broadcastAdd;
    }

    public static boolean startRpc() {
        return conf.start_rpc;
    }

    public static InetAddress getRpcAddress() {
        return rpcAddress;
    }

    public static void setBroadcastRpcAddress(InetAddress broadcastRPCAddr) {
        broadcastRpcAddress = broadcastRPCAddr;
    }

    public static InetAddress getBroadcastRpcAddress() {
        return broadcastRpcAddress;
    }

    public static String getRpcServerType() {
        return conf.rpc_server_type;
    }

    public static boolean getRpcKeepAlive() {
        return conf.rpc_keepalive;
    }

    public static Integer getRpcMinThreads() {
        return conf.rpc_min_threads;
    }

    public static Integer getRpcMaxThreads() {
        return conf.rpc_max_threads;
    }

    public static Integer getRpcSendBufferSize() {
        return conf.rpc_send_buff_size_in_bytes;
    }

    public static Integer getRpcRecvBufferSize() {
        return conf.rpc_recv_buff_size_in_bytes;
    }

    public static Integer getInternodeSendBufferSize() {
        return conf.internode_send_buff_size_in_bytes;
    }

    public static Integer getInternodeRecvBufferSize() {
        return conf.internode_recv_buff_size_in_bytes;
    }

    public static boolean startNativeTransport() {
        return conf.start_native_transport;
    }

    public static int getNativeTransportPort() {
        return Integer.parseInt(System.getProperty("cassandra.native_transport_port", conf.native_transport_port.toString()));
    }

    public static Integer getNativeTransportMaxThreads() {
        return conf.native_transport_max_threads;
    }

    public static int getNativeTransportMaxFrameSize() {
        return conf.native_transport_max_frame_size_in_mb * 1024 * 1024;
    }

    public static double getCommitLogSyncBatchWindow() {
        return conf.commitlog_sync_batch_window_in_ms;
    }

    public static int getCommitLogSyncPeriod() {
        return conf.commitlog_sync_period_in_ms;
    }

    public static int getCommitLogPeriodicQueueSize() {
        return conf.commitlog_periodic_queue_size;
    }

    public static Config.CommitLogSync getCommitLogSync() {
        return conf.commitlog_sync;
    }

    public static Config.DiskAccessMode getDiskAccessMode() {
        return conf.disk_access_mode;
    }

    public static Config.DiskAccessMode getIndexAccessMode() {
        return indexAccessMode;
    }

    public static void setDiskFailurePolicy(Config.DiskFailurePolicy policy) {
        conf.disk_failure_policy = policy;
    }

    public static Config.DiskFailurePolicy getDiskFailurePolicy() {
        return conf.disk_failure_policy;
    }

    public static void setCommitFailurePolicy(Config.CommitFailurePolicy policy) {
        conf.commit_failure_policy = policy;
    }

    public static Config.CommitFailurePolicy getCommitFailurePolicy() {
        return conf.commit_failure_policy;
    }

    public static boolean isSnapshotBeforeCompaction() {
        return conf.snapshot_before_compaction;
    }

    public static boolean isAutoSnapshot() {
        return conf.auto_snapshot;
    }

    @VisibleForTesting
    public static void setAutoSnapshot(boolean autoSnapshot) {
        conf.auto_snapshot = autoSnapshot;
    }

    public static boolean isAutoBootstrap() {
        return Boolean.parseBoolean(System.getProperty("cassandra.auto_bootstrap", conf.auto_bootstrap.toString()));
    }

    public static void setHintedHandoffEnabled(boolean hintedHandoffEnabled) {
        conf.hinted_handoff_enabled_global = hintedHandoffEnabled;
        conf.hinted_handoff_enabled_by_dc.clear();
    }

    public static void setHintedHandoffEnabled(final String dcNames) {
        List<String> dcNameList;
        try {
            dcNameList = Config.parseHintedHandoffEnabledDCs(dcNames);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read csv of dcs for hinted handoff enable. " + dcNames, e);
        }
        if (dcNameList.isEmpty())
            throw new IllegalArgumentException("Empty list of Dcs for hinted handoff enable");
        conf.hinted_handoff_enabled_by_dc.clear();
        conf.hinted_handoff_enabled_by_dc.addAll(dcNameList);
    }

    public static boolean hintedHandoffEnabled() {
        return conf.hinted_handoff_enabled_global;
    }

    public static Set<String> hintedHandoffEnabledByDC() {
        return Collections.unmodifiableSet(conf.hinted_handoff_enabled_by_dc);
    }

    public static boolean shouldHintByDC() {
        return !conf.hinted_handoff_enabled_by_dc.isEmpty();
    }

    public static boolean hintedHandoffEnabled(final String dcName) {
        return conf.hinted_handoff_enabled_by_dc.contains(dcName);
    }

    public static void setMaxHintWindow(int ms) {
        conf.max_hint_window_in_ms = ms;
    }

    public static int getMaxHintWindow() {
        return conf.max_hint_window_in_ms;
    }

    public static File getSerializedCachePath(String ksName, String cfName, UUID cfId, CacheService.CacheType cacheType, String version) {
        StringBuilder builder = new StringBuilder();
        builder.append(ksName).append('-');
        builder.append(cfName).append('-');
        builder.append(ByteBufferUtil.bytesToHex(ByteBufferUtil.bytes(cfId))).append('-');
        builder.append(cacheType);
        builder.append((version == null ? "" : "-" + version + ".db"));
        return new File(conf.saved_caches_directory, builder.toString());
    }

    public static int getDynamicUpdateInterval() {
        return conf.dynamic_snitch_update_interval_in_ms;
    }

    public static void setDynamicUpdateInterval(Integer dynamicUpdateInterval) {
        conf.dynamic_snitch_update_interval_in_ms = dynamicUpdateInterval;
    }

    public static int getDynamicResetInterval() {
        return conf.dynamic_snitch_reset_interval_in_ms;
    }

    public static void setDynamicResetInterval(Integer dynamicResetInterval) {
        conf.dynamic_snitch_reset_interval_in_ms = dynamicResetInterval;
    }

    public static double getDynamicBadnessThreshold() {
        return conf.dynamic_snitch_badness_threshold;
    }

    public static void setDynamicBadnessThreshold(Double dynamicBadnessThreshold) {
        conf.dynamic_snitch_badness_threshold = dynamicBadnessThreshold;
    }

    public static ServerEncryptionOptions getServerEncryptionOptions() {
        return conf.server_encryption_options;
    }

    public static ClientEncryptionOptions getClientEncryptionOptions() {
        return conf.client_encryption_options;
    }

    public static int getHintedHandoffThrottleInKB() {
        return conf.hinted_handoff_throttle_in_kb;
    }

    public static int getBatchlogReplayThrottleInKB() {
        return conf.batchlog_replay_throttle_in_kb;
    }

    public static void setHintedHandoffThrottleInKB(Integer throttleInKB) {
        conf.hinted_handoff_throttle_in_kb = throttleInKB;
    }

    public static int getMaxHintsThread() {
        return conf.max_hints_delivery_threads;
    }

    public static boolean isIncrementalBackupsEnabled() {
        return conf.incremental_backups;
    }

    public static void setIncrementalBackupsEnabled(boolean value) {
        conf.incremental_backups = value;
    }

    public static int getFileCacheSizeInMB() {
        return conf.file_cache_size_in_mb;
    }

    public static long getTotalCommitlogSpaceInMB() {
        return conf.commitlog_total_space_in_mb;
    }

    public static int getSSTablePreempiveOpenIntervalInMB() {
        return conf.sstable_preemptive_open_interval_in_mb;
    }

    public static boolean getTrickleFsync() {
        return conf.trickle_fsync;
    }

    public static int getTrickleFsyncIntervalInKb() {
        return conf.trickle_fsync_interval_in_kb;
    }

    public static long getKeyCacheSizeInMB() {
        return keyCacheSizeInMB;
    }

    public static long getIndexSummaryCapacityInMB() {
        return indexSummaryCapacityInMB;
    }

    public static int getKeyCacheSavePeriod() {
        return conf.key_cache_save_period;
    }

    public static void setKeyCacheSavePeriod(int keyCacheSavePeriod) {
        conf.key_cache_save_period = keyCacheSavePeriod;
    }

    public static int getKeyCacheKeysToSave() {
        return conf.key_cache_keys_to_save;
    }

    public static void setKeyCacheKeysToSave(int keyCacheKeysToSave) {
        conf.key_cache_keys_to_save = keyCacheKeysToSave;
    }

    public static long getRowCacheSizeInMB() {
        return conf.row_cache_size_in_mb;
    }

    public static int getRowCacheSavePeriod() {
        return conf.row_cache_save_period;
    }

    public static void setRowCacheSavePeriod(int rowCacheSavePeriod) {
        conf.row_cache_save_period = rowCacheSavePeriod;
    }

    public static int getRowCacheKeysToSave() {
        return conf.row_cache_keys_to_save;
    }

    public static long getCounterCacheSizeInMB() {
        return counterCacheSizeInMB;
    }

    public static int getCounterCacheSavePeriod() {
        return conf.counter_cache_save_period;
    }

    public static void setCounterCacheSavePeriod(int counterCacheSavePeriod) {
        conf.counter_cache_save_period = counterCacheSavePeriod;
    }

    public static int getCounterCacheKeysToSave() {
        return conf.counter_cache_keys_to_save;
    }

    public static void setCounterCacheKeysToSave(int counterCacheKeysToSave) {
        conf.counter_cache_keys_to_save = counterCacheKeysToSave;
    }

    public static IAllocator getoffHeapMemoryAllocator() {
        return memoryAllocator;
    }

    public static void setRowCacheKeysToSave(int rowCacheKeysToSave) {
        conf.row_cache_keys_to_save = rowCacheKeysToSave;
    }

    public static int getStreamingSocketTimeout() {
        return conf.streaming_socket_timeout_in_ms;
    }

    public static String getLocalDataCenter() {
        return localDC;
    }

    public static Comparator<InetAddress> getLocalComparator() {
        return localComparator;
    }

    public static Config.InternodeCompression internodeCompression() {
        return conf.internode_compression;
    }

    public static boolean getInterDCTcpNoDelay() {
        return conf.inter_dc_tcp_nodelay;
    }

    public static SSTableFormat.Type getSSTableFormat() {
        return sstable_format;
    }

    public static MemtablePool getMemtableAllocatorPool() {
        long heapLimit = ((long) conf.memtable_heap_space_in_mb) << 20;
        long offHeapLimit = ((long) conf.memtable_offheap_space_in_mb) << 20;
        switch(conf.memtable_allocation_type) {
            case unslabbed_heap_buffers:
                return new HeapPool(heapLimit, conf.memtable_cleanup_threshold, new ColumnFamilyStore.FlushLargestColumnFamily());
            case heap_buffers:
                return new SlabPool(heapLimit, 0, conf.memtable_cleanup_threshold, new ColumnFamilyStore.FlushLargestColumnFamily());
            case offheap_buffers:
                if (!FileUtils.isCleanerAvailable()) {
                    throw new IllegalStateException("Could not free direct byte buffer: offheap_buffers is not a safe memtable_allocation_type without this ability, please adjust your config. This feature is only guaranteed to work on an Oracle JVM. Refusing to start.");
                }
                return new SlabPool(heapLimit, offHeapLimit, conf.memtable_cleanup_threshold, new ColumnFamilyStore.FlushLargestColumnFamily());
            case offheap_objects:
                return new NativePool(heapLimit, offHeapLimit, conf.memtable_cleanup_threshold, new ColumnFamilyStore.FlushLargestColumnFamily());
            default:
                throw new AssertionError();
        }
    }

    public static int getIndexSummaryResizeIntervalInMinutes() {
        return conf.index_summary_resize_interval_in_minutes;
    }

    public static boolean hasLargeAddressSpace() {
        String datamodel = System.getProperty("sun.arch.data.model");
        if (datamodel != null) {
            switch(datamodel) {
                case "64":
                    return true;
                case "32":
                    return false;
            }
        }
        String arch = System.getProperty("os.arch");
        return arch.contains("64") || arch.contains("sparcv9");
    }
}
