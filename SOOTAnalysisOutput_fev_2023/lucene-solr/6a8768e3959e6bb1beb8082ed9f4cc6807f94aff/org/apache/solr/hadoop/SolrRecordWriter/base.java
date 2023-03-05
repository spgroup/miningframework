package org.apache.solr.hadoop;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.DirectoryFactory;
import org.apache.solr.core.HdfsDirectoryFactory;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SolrRecordWriter<K, V> extends RecordWriter<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public final static List<String> allowedConfigDirectories = new ArrayList<>(Arrays.asList(new String[] { "conf", "lib", "solr.xml", "core1" }));

    public final static Set<String> requiredConfigDirectories = new HashSet<>();

    static {
        requiredConfigDirectories.add("conf");
    }

    public static List<String> getAllowedConfigDirectories() {
        return Collections.unmodifiableList(allowedConfigDirectories);
    }

    public static boolean isRequiredConfigDirectory(final String directory) {
        return requiredConfigDirectories.contains(directory);
    }

    private final HeartBeater heartBeater;

    private final BatchWriter batchWriter;

    private final List<SolrInputDocument> batch;

    private final int batchSize;

    private long numDocsWritten = 0;

    private long nextLogTime = System.nanoTime();

    private static HashMap<TaskID, Reducer<?, ?, ?, ?>.Context> contextMap = new HashMap<>();

    public SolrRecordWriter(TaskAttemptContext context, Path outputShardDir, int batchSize) {
        this.batchSize = batchSize;
        this.batch = new ArrayList<>(batchSize);
        Configuration conf = context.getConfiguration();
        heartBeater = new HeartBeater(context);
        try {
            heartBeater.needHeartBeat();
            Path solrHomeDir = SolrRecordWriter.findSolrConfig(conf);
            FileSystem fs = outputShardDir.getFileSystem(conf);
            EmbeddedSolrServer solr = createEmbeddedSolrServer(solrHomeDir, fs, outputShardDir);
            batchWriter = new BatchWriter(solr, batchSize, context.getTaskAttemptID().getTaskID(), SolrOutputFormat.getSolrWriterThreadCount(conf), SolrOutputFormat.getSolrWriterQueueSize(conf));
        } catch (Exception e) {
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Failed to initialize record writer for %s, %s", context.getJobName(), conf.get("mapred.task.id")), e);
        } finally {
            heartBeater.cancelHeartBeat();
        }
    }

    public static EmbeddedSolrServer createEmbeddedSolrServer(Path solrHomeDir, FileSystem fs, Path outputShardDir) throws IOException {
        LOG.info("Creating embedded Solr server with solrHomeDir: " + solrHomeDir + ", fs: " + fs + ", outputShardDir: " + outputShardDir);
        Path solrDataDir = new Path(outputShardDir, "data");
        String dataDirStr = solrDataDir.toUri().toString();
        SolrResourceLoader loader = new SolrResourceLoader(Paths.get(solrHomeDir.toString()), null, null);
        LOG.info(String.format(Locale.ENGLISH, "Constructed instance information solr.home %s (%s), instance dir %s, conf dir %s, writing index to solr.data.dir %s, with permdir %s", solrHomeDir, solrHomeDir.toUri(), loader.getInstancePath(), loader.getConfigDir(), dataDirStr, outputShardDir));
        System.setProperty("solr.directoryFactory", HdfsDirectoryFactory.class.getName());
        System.setProperty("solr.lock.type", DirectoryFactory.LOCK_TYPE_HDFS);
        System.setProperty("solr.hdfs.nrtcachingdirectory", "false");
        System.setProperty("solr.hdfs.blockcache.enabled", "false");
        System.setProperty("solr.autoCommit.maxTime", "600000");
        System.setProperty("solr.autoSoftCommit.maxTime", "-1");
        CoreContainer container = new CoreContainer(loader);
        container.load();
        SolrCore core = container.create("", ImmutableMap.of(CoreDescriptor.CORE_DATADIR, dataDirStr));
        if (!(core.getDirectoryFactory() instanceof HdfsDirectoryFactory)) {
            throw new UnsupportedOperationException("Invalid configuration. Currently, the only DirectoryFactory supported is " + HdfsDirectoryFactory.class.getSimpleName());
        }
        EmbeddedSolrServer solr = new EmbeddedSolrServer(container, "");
        return solr;
    }

    public static void incrementCounter(TaskID taskId, String groupName, String counterName, long incr) {
        Reducer<?, ?, ?, ?>.Context context = contextMap.get(taskId);
        if (context != null) {
            context.getCounter(groupName, counterName).increment(incr);
        }
    }

    public static void incrementCounter(TaskID taskId, Enum<?> counterName, long incr) {
        Reducer<?, ?, ?, ?>.Context context = contextMap.get(taskId);
        if (context != null) {
            context.getCounter(counterName).increment(incr);
        }
    }

    public static void addReducerContext(Reducer<?, ?, ?, ?>.Context context) {
        TaskID taskID = context.getTaskAttemptID().getTaskID();
        contextMap.put(taskID, context);
    }

    public static Path findSolrConfig(Configuration conf) throws IOException {
        Path[] localArchives = DistributedCache.getLocalCacheArchives(conf);
        for (Path unpackedDir : localArchives) {
            if (unpackedDir.getName().equals(SolrOutputFormat.getZipName(conf))) {
                LOG.info("Using this unpacked directory as solr home: {}", unpackedDir);
                return unpackedDir;
            }
        }
        throw new IOException(String.format(Locale.ENGLISH, "No local cache archives, where is %s:%s", SolrOutputFormat.getSetupOk(), SolrOutputFormat.getZipName(conf)));
    }

    @Override
    public void write(K key, V value) throws IOException {
        heartBeater.needHeartBeat();
        try {
            try {
                SolrInputDocumentWritable sidw = (SolrInputDocumentWritable) value;
                batch.add(sidw.getSolrInputDocument());
                if (batch.size() >= batchSize) {
                    batchWriter.queueBatch(batch);
                    numDocsWritten += batch.size();
                    if (System.nanoTime() >= nextLogTime) {
                        LOG.info("docsWritten: {}", numDocsWritten);
                        nextLogTime += TimeUnit.NANOSECONDS.convert(10, TimeUnit.SECONDS);
                    }
                    batch.clear();
                }
            } catch (SolrServerException e) {
                throw new IOException(e);
            }
        } finally {
            heartBeater.cancelHeartBeat();
        }
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        if (context != null) {
            heartBeater.setProgress(context);
        }
        try {
            heartBeater.needHeartBeat();
            if (batch.size() > 0) {
                batchWriter.queueBatch(batch);
                numDocsWritten += batch.size();
                batch.clear();
            }
            LOG.info("docsWritten: {}", numDocsWritten);
            batchWriter.close(context);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        } finally {
            heartBeater.cancelHeartBeat();
            heartBeater.close();
        }
        context.setStatus("Done");
    }
}
