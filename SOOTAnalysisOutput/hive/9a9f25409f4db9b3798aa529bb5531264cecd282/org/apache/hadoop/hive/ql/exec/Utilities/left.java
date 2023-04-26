package org.apache.hadoop.hive.ql.exec;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.Statement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTransientException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hive.common.FileUtils;
import org.apache.hadoop.hive.common.HiveInterruptCallback;
import org.apache.hadoop.hive.common.HiveInterruptUtils;
import org.apache.hadoop.hive.common.HiveStatsUtils;
import org.apache.hadoop.hive.common.JavaUtils;
import org.apache.hadoop.hive.common.StatsSetupConst;
import org.apache.hadoop.hive.common.StringInternUtils;
import org.apache.hadoop.hive.common.ValidTxnList;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.Warehouse;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Order;
import org.apache.hadoop.hive.metastore.api.hive_metastoreConstants;
import org.apache.hadoop.hive.ql.Context;
import org.apache.hadoop.hive.ql.Driver.DriverState;
import org.apache.hadoop.hive.ql.Driver.LockedDriverState;
import org.apache.hadoop.hive.ql.ErrorMsg;
import org.apache.hadoop.hive.ql.QueryPlan;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.hive.ql.exec.mr.ExecDriver;
import org.apache.hadoop.hive.ql.exec.mr.ExecMapper;
import org.apache.hadoop.hive.ql.exec.mr.ExecReducer;
import org.apache.hadoop.hive.ql.exec.mr.MapRedTask;
import org.apache.hadoop.hive.ql.exec.spark.SparkTask;
import org.apache.hadoop.hive.ql.exec.tez.DagUtils;
import org.apache.hadoop.hive.ql.exec.tez.TezTask;
import org.apache.hadoop.hive.ql.exec.vector.VectorExpressionDescriptor;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedInputFormatInterface;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatchCtx;
import org.apache.hadoop.hive.ql.io.AcidUtils;
import org.apache.hadoop.hive.ql.io.ContentSummaryInputFormat;
import org.apache.hadoop.hive.ql.io.HiveFileFormatUtils;
import org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat;
import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.hive.ql.io.HiveOutputFormat;
import org.apache.hadoop.hive.ql.io.HiveSequenceFileOutputFormat;
import org.apache.hadoop.hive.ql.io.IOConstants;
import org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat;
import org.apache.hadoop.hive.ql.io.OneNullRowInputFormat;
import org.apache.hadoop.hive.ql.io.RCFile;
import org.apache.hadoop.hive.ql.io.ReworkMapredInputFormat;
import org.apache.hadoop.hive.ql.io.SelfDescribingInputFormatInterface;
import org.apache.hadoop.hive.ql.io.merge.MergeFileMapper;
import org.apache.hadoop.hive.ql.io.merge.MergeFileWork;
import org.apache.hadoop.hive.ql.io.rcfile.stats.PartialScanMapper;
import org.apache.hadoop.hive.ql.io.rcfile.stats.PartialScanWork;
import org.apache.hadoop.hive.ql.io.rcfile.truncate.ColumnTruncateMapper;
import org.apache.hadoop.hive.ql.io.rcfile.truncate.ColumnTruncateWork;
import org.apache.hadoop.hive.ql.log.PerfLogger;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.metadata.HiveStorageHandler;
import org.apache.hadoop.hive.ql.metadata.HiveUtils;
import org.apache.hadoop.hive.ql.metadata.InputEstimator;
import org.apache.hadoop.hive.ql.metadata.Partition;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.hadoop.hive.ql.optimizer.physical.Vectorizer;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.plan.BaseWork;
import org.apache.hadoop.hive.ql.plan.DynamicPartitionCtx;
import org.apache.hadoop.hive.ql.plan.FileSinkDesc;
import org.apache.hadoop.hive.ql.plan.MapWork;
import org.apache.hadoop.hive.ql.plan.MapredWork;
import org.apache.hadoop.hive.ql.plan.MergeJoinWork;
import org.apache.hadoop.hive.ql.plan.OperatorDesc;
import org.apache.hadoop.hive.ql.plan.PartitionDesc;
import org.apache.hadoop.hive.ql.plan.PlanUtils;
import org.apache.hadoop.hive.ql.plan.ReduceWork;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.ql.plan.TableScanDesc;
import org.apache.hadoop.hive.ql.plan.api.Adjacency;
import org.apache.hadoop.hive.ql.plan.api.Graph;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.stats.StatsFactory;
import org.apache.hadoop.hive.ql.stats.StatsPublisher;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.ColumnProjectionUtils;
import org.apache.hadoop.hive.serde2.MetadataTypedColumnsetSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.Serializer;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.Progressable;
import org.apache.hive.common.util.ACLConfigurationParser;
import org.apache.hive.common.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({ "nls", "deprecation" })
public final class Utilities {

    public static final Logger LOG14535 = LoggerFactory.getLogger("Log14535");

    public static final String HADOOP_LOCAL_FS = "file:///";

    public static final String HADOOP_LOCAL_FS_SCHEME = "file";

    public static final String MAP_PLAN_NAME = "map.xml";

    public static final String REDUCE_PLAN_NAME = "reduce.xml";

    public static final String MERGE_PLAN_NAME = "merge.xml";

    public static final String INPUT_NAME = "iocontext.input.name";

    public static final String HAS_MAP_WORK = "has.map.work";

    public static final String HAS_REDUCE_WORK = "has.reduce.work";

    public static final String MAPRED_MAPPER_CLASS = "mapred.mapper.class";

    public static final String MAPRED_REDUCER_CLASS = "mapred.reducer.class";

    public static final String HIVE_ADDED_JARS = "hive.added.jars";

    public static final String VECTOR_MODE = "VECTOR_MODE";

    public static final String USE_VECTORIZED_INPUT_FILE_FORMAT = "USE_VECTORIZED_INPUT_FILE_FORMAT";

    public static final String MAPNAME = "Map ";

    public static final String REDUCENAME = "Reducer ";

    @Deprecated
    protected static final String DEPRECATED_MAPRED_DFSCLIENT_PARALLELISM_MAX = "mapred.dfsclient.parallelism.max";

    public static enum ReduceField {

        KEY, VALUE
    }

    public static List<String> reduceFieldNameList;

    static {
        reduceFieldNameList = new ArrayList<String>();
        for (ReduceField r : ReduceField.values()) {
            reduceFieldNameList.add(r.toString());
        }
    }

    public static String removeValueTag(String column) {
        if (column.startsWith(ReduceField.VALUE + ".")) {
            return column.substring(6);
        }
        return column;
    }

    private Utilities() {
    }

    private static GlobalWorkMapFactory gWorkMap = new GlobalWorkMapFactory();

    private static final String CLASS_NAME = Utilities.class.getName();

    private static final Logger LOG = LoggerFactory.getLogger(CLASS_NAME);

    public static void clearWork(Configuration conf) {
        Path mapPath = getPlanPath(conf, MAP_PLAN_NAME);
        Path reducePath = getPlanPath(conf, REDUCE_PLAN_NAME);
        if (mapPath == null && reducePath == null) {
            return;
        }
        try {
            FileSystem fs = mapPath.getFileSystem(conf);
            if (fs.exists(mapPath)) {
                fs.delete(mapPath, true);
            }
            if (fs.exists(reducePath)) {
                fs.delete(reducePath, true);
            }
        } catch (Exception e) {
            LOG.warn("Failed to clean-up tmp directories.", e);
        } finally {
            clearWorkMapForConf(conf);
        }
    }

    public static MapredWork getMapRedWork(Configuration conf) {
        MapredWork w = new MapredWork();
        w.setMapWork(getMapWork(conf));
        w.setReduceWork(getReduceWork(conf));
        return w;
    }

    public static void cacheMapWork(Configuration conf, MapWork work, Path hiveScratchDir) {
        cacheBaseWork(conf, MAP_PLAN_NAME, work, hiveScratchDir);
    }

    public static void setMapWork(Configuration conf, MapWork work) {
        setBaseWork(conf, MAP_PLAN_NAME, work);
    }

    public static MapWork getMapWork(Configuration conf) {
        if (!conf.getBoolean(HAS_MAP_WORK, false)) {
            return null;
        }
        return (MapWork) getBaseWork(conf, MAP_PLAN_NAME);
    }

    public static void setReduceWork(Configuration conf, ReduceWork work) {
        setBaseWork(conf, REDUCE_PLAN_NAME, work);
    }

    public static ReduceWork getReduceWork(Configuration conf) {
        if (!conf.getBoolean(HAS_REDUCE_WORK, false)) {
            return null;
        }
        return (ReduceWork) getBaseWork(conf, REDUCE_PLAN_NAME);
    }

    public static Path setMergeWork(JobConf conf, MergeJoinWork mergeJoinWork, Path mrScratchDir, boolean useCache) {
        for (BaseWork baseWork : mergeJoinWork.getBaseWorkList()) {
            setBaseWork(conf, baseWork, mrScratchDir, baseWork.getName() + MERGE_PLAN_NAME, useCache);
            String prefixes = conf.get(DagUtils.TEZ_MERGE_WORK_FILE_PREFIXES);
            if (prefixes == null) {
                prefixes = baseWork.getName();
            } else {
                prefixes = prefixes + "," + baseWork.getName();
            }
            conf.set(DagUtils.TEZ_MERGE_WORK_FILE_PREFIXES, prefixes);
        }
        return null;
    }

    public static BaseWork getMergeWork(Configuration jconf) {
        if ((jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX) == null) || (jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX).isEmpty())) {
            return null;
        }
        return getMergeWork(jconf, jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX));
    }

    public static BaseWork getMergeWork(Configuration jconf, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        return getBaseWork(jconf, prefix + MERGE_PLAN_NAME);
    }

    public static void cacheBaseWork(Configuration conf, String name, BaseWork work, Path hiveScratchDir) {
        try {
            setPlanPath(conf, hiveScratchDir);
            setBaseWork(conf, name, work);
        } catch (IOException e) {
            LOG.error("Failed to cache plan", e);
            throw new RuntimeException(e);
        }
    }

    public static void setBaseWork(Configuration conf, String name, BaseWork work) {
        Path path = getPlanPath(conf, name);
        setHasWork(conf, name);
        gWorkMap.get(conf).put(path, work);
    }

    private static BaseWork getBaseWork(Configuration conf, String name) {
        Path path = null;
        InputStream in = null;
        Kryo kryo = SerializationUtilities.borrowKryo();
        try {
            String engine = HiveConf.getVar(conf, ConfVars.HIVE_EXECUTION_ENGINE);
            if (engine.equals("spark")) {
                String addedJars = conf.get(HIVE_ADDED_JARS);
                if (addedJars != null && !addedJars.isEmpty()) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    ClassLoader newLoader = addToClassPath(loader, addedJars.split(";"));
                    Thread.currentThread().setContextClassLoader(newLoader);
                    kryo.setClassLoader(newLoader);
                }
            }
            path = getPlanPath(conf, name);
            LOG.info("PLAN PATH = " + path);
            if (path == null) {
                return null;
            }
            BaseWork gWork = gWorkMap.get(conf).get(path);
            if (gWork == null) {
                Path localPath = path;
                LOG.debug("local path = " + localPath);
                final long serializedSize;
                final String planMode;
                if (HiveConf.getBoolVar(conf, ConfVars.HIVE_RPC_QUERY_PLAN)) {
                    LOG.debug("Loading plan from string: " + path.toUri().getPath());
                    String planString = conf.getRaw(path.toUri().getPath());
                    if (planString == null) {
                        LOG.info("Could not find plan string in conf");
                        return null;
                    }
                    serializedSize = planString.length();
                    planMode = "RPC";
                    byte[] planBytes = Base64.decodeBase64(planString);
                    in = new ByteArrayInputStream(planBytes);
                    in = new InflaterInputStream(in);
                } else {
                    LOG.debug("Open file to read in plan: " + localPath);
                    FileSystem fs = localPath.getFileSystem(conf);
                    in = fs.open(localPath);
                    serializedSize = fs.getFileStatus(localPath).getLen();
                    planMode = "FILE";
                }
                if (MAP_PLAN_NAME.equals(name)) {
                    if (ExecMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, MapWork.class);
                    } else if (MergeFileMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, MergeFileWork.class);
                    } else if (ColumnTruncateMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, ColumnTruncateWork.class);
                    } else if (PartialScanMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, PartialScanWork.class);
                    } else {
                        throw new RuntimeException("unable to determine work from configuration ." + MAPRED_MAPPER_CLASS + " was " + conf.get(MAPRED_MAPPER_CLASS));
                    }
                } else if (REDUCE_PLAN_NAME.equals(name)) {
                    if (ExecReducer.class.getName().equals(conf.get(MAPRED_REDUCER_CLASS))) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, ReduceWork.class);
                    } else {
                        throw new RuntimeException("unable to determine work from configuration ." + MAPRED_REDUCER_CLASS + " was " + conf.get(MAPRED_REDUCER_CLASS));
                    }
                } else if (name.contains(MERGE_PLAN_NAME)) {
                    if (name.startsWith(MAPNAME)) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, MapWork.class);
                    } else if (name.startsWith(REDUCENAME)) {
                        gWork = SerializationUtilities.deserializePlan(kryo, in, ReduceWork.class);
                    } else {
                        throw new RuntimeException("Unknown work type: " + name);
                    }
                }
                LOG.info("Deserialized plan (via {}) - name: {} size: {}", planMode, gWork.getName(), humanReadableByteCount(serializedSize));
                gWorkMap.get(conf).put(path, gWork);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Found plan in cache for name: " + name);
            }
            return gWork;
        } catch (FileNotFoundException fnf) {
            LOG.debug("No plan file found: " + path + "; " + fnf.getMessage());
            return null;
        } catch (Exception e) {
            String msg = "Failed to load plan: " + path;
            LOG.error("Failed to load plan: " + path, e);
            throw new RuntimeException(msg, e);
        } finally {
            SerializationUtilities.releaseKryo(kryo);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException cantBlameMeForTrying) {
                }
            }
        }
    }

    private static void setHasWork(Configuration conf, String name) {
        if (MAP_PLAN_NAME.equals(name)) {
            conf.setBoolean(HAS_MAP_WORK, true);
        } else if (REDUCE_PLAN_NAME.equals(name)) {
            conf.setBoolean(HAS_REDUCE_WORK, true);
        }
    }

    public static void setWorkflowAdjacencies(Configuration conf, QueryPlan plan) {
        try {
            Graph stageGraph = plan.getQueryPlan().getStageGraph();
            if (stageGraph == null) {
                return;
            }
            List<Adjacency> adjList = stageGraph.getAdjacencyList();
            if (adjList == null) {
                return;
            }
            for (Adjacency adj : adjList) {
                List<String> children = adj.getChildren();
                if (children == null || children.isEmpty()) {
                    return;
                }
                conf.setStrings("mapreduce.workflow.adjacency." + adj.getNode(), children.toArray(new String[children.size()]));
            }
        } catch (IOException e) {
        }
    }

    public static List<String> getFieldSchemaString(List<FieldSchema> fl) {
        if (fl == null) {
            return null;
        }
        ArrayList<String> ret = new ArrayList<String>();
        for (FieldSchema f : fl) {
            ret.add(f.getName() + " " + f.getType() + (f.getComment() != null ? (" " + f.getComment()) : ""));
        }
        return ret;
    }

    public static void setMapRedWork(Configuration conf, MapredWork w, Path hiveScratchDir) {
        String useName = conf.get(INPUT_NAME);
        if (useName == null) {
            useName = "mapreduce:" + hiveScratchDir;
        }
        conf.set(INPUT_NAME, useName);
        setMapWork(conf, w.getMapWork(), hiveScratchDir, true);
        if (w.getReduceWork() != null) {
            conf.set(INPUT_NAME, useName);
            setReduceWork(conf, w.getReduceWork(), hiveScratchDir, true);
        }
    }

    public static Path setMapWork(Configuration conf, MapWork w, Path hiveScratchDir, boolean useCache) {
        return setBaseWork(conf, w, hiveScratchDir, MAP_PLAN_NAME, useCache);
    }

    public static Path setReduceWork(Configuration conf, ReduceWork w, Path hiveScratchDir, boolean useCache) {
        return setBaseWork(conf, w, hiveScratchDir, REDUCE_PLAN_NAME, useCache);
    }

    private static Path setBaseWork(Configuration conf, BaseWork w, Path hiveScratchDir, String name, boolean useCache) {
        Kryo kryo = SerializationUtilities.borrowKryo();
        try {
            setPlanPath(conf, hiveScratchDir);
            Path planPath = getPlanPath(conf, name);
            setHasWork(conf, name);
            OutputStream out = null;
            final long serializedSize;
            final String planMode;
            if (HiveConf.getBoolVar(conf, ConfVars.HIVE_RPC_QUERY_PLAN)) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                try {
                    out = new DeflaterOutputStream(byteOut, new Deflater(Deflater.BEST_SPEED));
                    SerializationUtilities.serializePlan(kryo, w, out);
                    out.close();
                    out = null;
                } finally {
                    IOUtils.closeStream(out);
                }
                final String serializedPlan = Base64.encodeBase64String(byteOut.toByteArray());
                serializedSize = serializedPlan.length();
                planMode = "RPC";
                conf.set(planPath.toUri().getPath(), serializedPlan);
            } else {
                FileSystem fs = planPath.getFileSystem(conf);
                try {
                    out = fs.create(planPath);
                    SerializationUtilities.serializePlan(kryo, w, out);
                    out.close();
                    out = null;
                    long fileLen = fs.getFileStatus(planPath).getLen();
                    serializedSize = fileLen;
                    planMode = "FILE";
                } finally {
                    IOUtils.closeStream(out);
                }
                if (useCache && !ShimLoader.getHadoopShims().isLocalMode(conf)) {
                    if (!DistributedCache.getSymlink(conf)) {
                        DistributedCache.createSymlink(conf);
                    }
                    String uriWithLink = planPath.toUri().toString() + "#" + name;
                    DistributedCache.addCacheFile(new URI(uriWithLink), conf);
                    short replication = (short) conf.getInt("mapred.submit.replication", 10);
                    fs.setReplication(planPath, replication);
                }
            }
            LOG.info("Serialized plan (via {}) - name: {} size: {}", planMode, w.getName(), humanReadableByteCount(serializedSize));
            gWorkMap.get(conf).put(planPath, w);
            return planPath;
        } catch (Exception e) {
            String msg = "Error caching " + name + ": " + e;
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            SerializationUtilities.releaseKryo(kryo);
        }
    }

    private static Path getPlanPath(Configuration conf, String name) {
        Path planPath = getPlanPath(conf);
        if (planPath == null) {
            return null;
        }
        return new Path(planPath, name);
    }

    private static void setPlanPath(Configuration conf, Path hiveScratchDir) throws IOException {
        if (getPlanPath(conf) == null) {
            String jobID = UUID.randomUUID().toString();
            Path planPath = new Path(hiveScratchDir, jobID);
            FileSystem fs = planPath.getFileSystem(conf);
            fs.mkdirs(planPath);
            HiveConf.setVar(conf, HiveConf.ConfVars.PLAN, planPath.toUri().toString());
        }
    }

    public static Path getPlanPath(Configuration conf) {
        String plan = HiveConf.getVar(conf, HiveConf.ConfVars.PLAN);
        if (plan != null && !plan.isEmpty()) {
            return new Path(plan);
        }
        return null;
    }

    public static class CollectionPersistenceDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, oldInstance.getClass(), "new", null);
        }

        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
            Iterator<?> ite = ((Collection<?>) oldInstance).iterator();
            while (ite.hasNext()) {
                out.writeStatement(new Statement(oldInstance, "add", new Object[] { ite.next() }));
            }
        }
    }

    @VisibleForTesting
    public static TableDesc defaultTd;

    static {
        defaultTd = new TableDesc(TextInputFormat.class, IgnoreKeyTextOutputFormat.class, Utilities.makeProperties(org.apache.hadoop.hive.serde.serdeConstants.SERIALIZATION_FORMAT, "" + Utilities.ctrlaCode, serdeConstants.SERIALIZATION_LIB, MetadataTypedColumnsetSerDe.class.getName()));
    }

    public static final int carriageReturnCode = 13;

    public static final int newLineCode = 10;

    public static final int tabCode = 9;

    public static final int ctrlaCode = 1;

    public static final String INDENT = "  ";

    public static final String nullStringStorage = "\\N";

    public static final String nullStringOutput = "NULL";

    public static Random randGen = new Random();

    public static String getTaskId(Configuration hconf) {
        String taskid = (hconf == null) ? null : hconf.get("mapred.task.id");
        if ((taskid == null) || taskid.equals("")) {
            return ("" + Math.abs(randGen.nextInt()));
        } else {
            String ret = taskid.replaceAll(".*_[mr]_", "").replaceAll(".*_(map|reduce)_", "");
            return (ret);
        }
    }

    public static HashMap makeMap(Object... olist) {
        HashMap ret = new HashMap();
        for (int i = 0; i < olist.length; i += 2) {
            ret.put(olist[i], olist[i + 1]);
        }
        return (ret);
    }

    public static Properties makeProperties(String... olist) {
        Properties ret = new Properties();
        for (int i = 0; i < olist.length; i += 2) {
            ret.setProperty(olist[i], olist[i + 1]);
        }
        return (ret);
    }

    public static ArrayList makeList(Object... olist) {
        ArrayList ret = new ArrayList();
        for (Object element : olist) {
            ret.add(element);
        }
        return (ret);
    }

    public static TableDesc getTableDesc(Table tbl) {
        Properties props = tbl.getMetadata();
        props.put(serdeConstants.SERIALIZATION_LIB, tbl.getDeserializer().getClass().getName());
        return (new TableDesc(tbl.getInputFormatClass(), tbl.getOutputFormatClass(), props));
    }

    public static TableDesc getTableDesc(String cols, String colTypes) {
        return (new TableDesc(SequenceFileInputFormat.class, HiveSequenceFileOutputFormat.class, Utilities.makeProperties(serdeConstants.SERIALIZATION_FORMAT, "" + Utilities.ctrlaCode, serdeConstants.LIST_COLUMNS, cols, serdeConstants.LIST_COLUMN_TYPES, colTypes, serdeConstants.SERIALIZATION_LIB, LazySimpleSerDe.class.getName())));
    }

    public static PartitionDesc getPartitionDesc(Partition part) throws HiveException {
        return new PartitionDesc(part);
    }

    public static PartitionDesc getPartitionDescFromTableDesc(TableDesc tblDesc, Partition part, boolean usePartSchemaProperties) throws HiveException {
        return new PartitionDesc(part, tblDesc, usePartSchemaProperties);
    }

    private static String getOpTreeSkel_helper(Operator<?> op, String indent) {
        if (op == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indent);
        sb.append(op.toString());
        sb.append("\n");
        if (op.getChildOperators() != null) {
            for (Object child : op.getChildOperators()) {
                sb.append(getOpTreeSkel_helper((Operator<?>) child, indent + "  "));
            }
        }
        return sb.toString();
    }

    public static String getOpTreeSkel(Operator<?> op) {
        return getOpTreeSkel_helper(op, "");
    }

    private static boolean isWhitespace(int c) {
        if (c == -1) {
            return false;
        }
        return Character.isWhitespace((char) c);
    }

    public static boolean contentsEqual(InputStream is1, InputStream is2, boolean ignoreWhitespace) throws IOException {
        try {
            if ((is1 == is2) || (is1 == null && is2 == null)) {
                return true;
            }
            if (is1 == null || is2 == null) {
                return false;
            }
            while (true) {
                int c1 = is1.read();
                while (ignoreWhitespace && isWhitespace(c1)) {
                    c1 = is1.read();
                }
                int c2 = is2.read();
                while (ignoreWhitespace && isWhitespace(c2)) {
                    c2 = is2.read();
                }
                if (c1 == -1 && c2 == -1) {
                    return true;
                }
                if (c1 != c2) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String abbreviate(String str, int max) {
        str = str.trim();
        int len = str.length();
        int suffixlength = 20;
        if (len <= max) {
            return str;
        }
        suffixlength = Math.min(suffixlength, (max - 3) / 2);
        String rev = StringUtils.reverse(str);
        String suffix = WordUtils.abbreviate(rev, 0, suffixlength, "");
        suffix = StringUtils.reverse(suffix);
        String prefix = StringUtils.abbreviate(str, max - suffix.length());
        return prefix + suffix;
    }

    public static final String NSTR = "";

    public static enum StreamStatus {

        EOF, TERMINATED
    }

    public static StreamStatus readColumn(DataInput in, OutputStream out) throws IOException {
        boolean foundCrChar = false;
        while (true) {
            int b;
            try {
                b = in.readByte();
            } catch (EOFException e) {
                return StreamStatus.EOF;
            }
            if (b == Utilities.newLineCode) {
                return StreamStatus.TERMINATED;
            }
            out.write(b);
        }
    }

    public static OutputStream createCompressedStream(JobConf jc, OutputStream out) throws IOException {
        boolean isCompressed = FileOutputFormat.getCompressOutput(jc);
        return createCompressedStream(jc, out, isCompressed);
    }

    public static OutputStream createCompressedStream(JobConf jc, OutputStream out, boolean isCompressed) throws IOException {
        if (isCompressed) {
            Class<? extends CompressionCodec> codecClass = FileOutputFormat.getOutputCompressorClass(jc, DefaultCodec.class);
            CompressionCodec codec = ReflectionUtil.newInstance(codecClass, jc);
            return codec.createOutputStream(out);
        } else {
            return (out);
        }
    }

    @Deprecated
    public static String getFileExtension(JobConf jc, boolean isCompressed) {
        return getFileExtension(jc, isCompressed, new HiveIgnoreKeyTextOutputFormat());
    }

    public static String getFileExtension(JobConf jc, boolean isCompressed, HiveOutputFormat<?, ?> hiveOutputFormat) {
        String extension = HiveConf.getVar(jc, HiveConf.ConfVars.OUTPUT_FILE_EXTENSION);
        if (!StringUtils.isEmpty(extension)) {
            return extension;
        }
        if ((hiveOutputFormat instanceof HiveIgnoreKeyTextOutputFormat) && isCompressed) {
            Class<? extends CompressionCodec> codecClass = FileOutputFormat.getOutputCompressorClass(jc, DefaultCodec.class);
            CompressionCodec codec = ReflectionUtil.newInstance(codecClass, jc);
            return codec.getDefaultExtension();
        }
        return "";
    }

    public static SequenceFile.Writer createSequenceWriter(JobConf jc, FileSystem fs, Path file, Class<?> keyClass, Class<?> valClass, Progressable progressable) throws IOException {
        boolean isCompressed = FileOutputFormat.getCompressOutput(jc);
        return createSequenceWriter(jc, fs, file, keyClass, valClass, isCompressed, progressable);
    }

    public static SequenceFile.Writer createSequenceWriter(JobConf jc, FileSystem fs, Path file, Class<?> keyClass, Class<?> valClass, boolean isCompressed, Progressable progressable) throws IOException {
        CompressionCodec codec = null;
        CompressionType compressionType = CompressionType.NONE;
        Class codecClass = null;
        if (isCompressed) {
            compressionType = SequenceFileOutputFormat.getOutputCompressionType(jc);
            codecClass = FileOutputFormat.getOutputCompressorClass(jc, DefaultCodec.class);
            codec = (CompressionCodec) ReflectionUtil.newInstance(codecClass, jc);
        }
        return SequenceFile.createWriter(fs, jc, file, keyClass, valClass, compressionType, codec, progressable);
    }

    public static RCFile.Writer createRCFileWriter(JobConf jc, FileSystem fs, Path file, boolean isCompressed, Progressable progressable) throws IOException {
        CompressionCodec codec = null;
        if (isCompressed) {
            Class<?> codecClass = FileOutputFormat.getOutputCompressorClass(jc, DefaultCodec.class);
            codec = (CompressionCodec) ReflectionUtil.newInstance(codecClass, jc);
        }
        return new RCFile.Writer(fs, jc, file, progressable, codec);
    }

    public static String realFile(String newFile, Configuration conf) throws IOException {
        Path path = new Path(newFile);
        URI pathURI = path.toUri();
        FileSystem fs;
        if (pathURI.getScheme() == null) {
            fs = FileSystem.getLocal(conf);
        } else {
            fs = path.getFileSystem(conf);
        }
        if (!fs.exists(path)) {
            return null;
        }
        String file = path.makeQualified(fs).toString();
        return file;
    }

    public static List<String> mergeUniqElems(List<String> src, List<String> dest) {
        if (dest == null) {
            return src;
        }
        if (src == null) {
            return dest;
        }
        int pos = 0;
        while (pos < dest.size()) {
            if (!src.contains(dest.get(pos))) {
                src.add(dest.get(pos));
            }
            pos++;
        }
        return src;
    }

    private static final String tmpPrefix = "_tmp.";

    private static final String taskTmpPrefix = "_task_tmp.";

    public static Path toTaskTempPath(Path orig) {
        if (orig.getName().indexOf(taskTmpPrefix) == 0) {
            return orig;
        }
        return new Path(orig.getParent(), taskTmpPrefix + orig.getName());
    }

    public static Path toTempPath(Path orig) {
        if (orig.getName().indexOf(tmpPrefix) == 0) {
            return orig;
        }
        if (orig.getName().contains("=1")) {
            LOG.error("TODO# creating tmp path from " + orig, new Exception());
        }
        return new Path(orig.getParent(), tmpPrefix + orig.getName());
    }

    public static Path toTempPath(String orig) {
        return toTempPath(new Path(orig));
    }

    public static boolean isTempPath(FileStatus file) {
        String name = file.getPath().getName();
        return (name.startsWith("_task") || name.startsWith(tmpPrefix));
    }

    public static void rename(FileSystem fs, Path src, Path dst) throws IOException, HiveException {
        if (!fs.rename(src, dst)) {
            throw new HiveException("Unable to move: " + src + " to: " + dst);
        }
    }

    private static void moveSpecifiedFiles(FileSystem fs, Path src, Path dst, Set<Path> filesToMove) throws IOException, HiveException {
        if (!fs.exists(dst)) {
            fs.mkdirs(dst);
        }
        FileStatus[] files = fs.listStatus(src);
        for (FileStatus file : files) {
            if (filesToMove.contains(file.getPath())) {
                Utilities.moveFile(fs, file, dst);
            }
        }
    }

    private static void moveFile(FileSystem fs, FileStatus file, Path dst) throws IOException, HiveException {
        Path srcFilePath = file.getPath();
        String fileName = srcFilePath.getName();
        Path dstFilePath = new Path(dst, fileName);
        if (file.isDir()) {
            renameOrMoveFiles(fs, srcFilePath, dstFilePath);
        } else {
            if (fs.exists(dstFilePath)) {
                int suffix = 0;
                do {
                    suffix++;
                    dstFilePath = new Path(dst, fileName + "_" + suffix);
                } while (fs.exists(dstFilePath));
            }
            if (!fs.rename(srcFilePath, dstFilePath)) {
                throw new HiveException("Unable to move: " + srcFilePath + " to: " + dst);
            }
        }
    }

    public static void renameOrMoveFiles(FileSystem fs, Path src, Path dst) throws IOException, HiveException {
        if (!fs.exists(dst)) {
            if (!fs.rename(src, dst)) {
                throw new HiveException("Unable to move: " + src + " to: " + dst);
            }
        } else {
            FileStatus[] files = fs.listStatus(src);
            for (FileStatus file : files) {
                Utilities.moveFile(fs, file, dst);
            }
        }
    }

    private static final Pattern FILE_NAME_TO_TASK_ID_REGEX = Pattern.compile("^.*?([0-9]+)(_[0-9]{1,6})?(\\..*)?$");

    public static final String COPY_KEYWORD = "_copy_";

    private static final Pattern COPY_FILE_NAME_TO_TASK_ID_REGEX = Pattern.compile("^.*?" + "([0-9]+)" + "(_)" + "([0-9]{1,6})?" + "((_)(\\Bcopy\\B)(_)" + "([0-9]{1,6})$)?" + "(\\..*)?$");

    private static final Pattern FILE_NAME_PREFIXED_TASK_ID_REGEX = Pattern.compile("^.*?((\\(.*\\))?[0-9]+)(_[0-9]{1,6})?(\\..*)?$");

    private static final Pattern PREFIXED_TASK_ID_REGEX = Pattern.compile("^(.*?\\(.*\\))?([0-9]+)$");

    private static final Pattern PREFIXED_BUCKET_ID_REGEX = Pattern.compile("^(0*([0-9]+))_([0-9]+).*");

    public static String getTaskIdFromFilename(String filename) {
        return getIdFromFilename(filename, FILE_NAME_TO_TASK_ID_REGEX);
    }

    public static String getPrefixedTaskIdFromFilename(String filename) {
        return getIdFromFilename(filename, FILE_NAME_PREFIXED_TASK_ID_REGEX);
    }

    private static String getIdFromFilename(String filename, Pattern pattern) {
        String taskId = filename;
        int dirEnd = filename.lastIndexOf(Path.SEPARATOR);
        if (dirEnd != -1) {
            taskId = filename.substring(dirEnd + 1);
        }
        Matcher m = pattern.matcher(taskId);
        if (!m.matches()) {
            LOG.warn("Unable to get task id from file name: " + filename + ". Using last component" + taskId + " as task id.");
        } else {
            taskId = m.group(1);
        }
        LOG.debug("TaskId for " + filename + " = " + taskId);
        return taskId;
    }

    public static String getFileNameFromDirName(String dirName) {
        int dirEnd = dirName.lastIndexOf(Path.SEPARATOR);
        if (dirEnd != -1) {
            return dirName.substring(dirEnd + 1);
        }
        return dirName;
    }

    public static String replaceTaskIdFromFilename(String filename, int bucketNum) {
        return replaceTaskIdFromFilename(filename, String.valueOf(bucketNum));
    }

    public static String replaceTaskIdFromFilename(String filename, String fileId) {
        String taskId = getTaskIdFromFilename(filename);
        String newTaskId = replaceTaskId(taskId, fileId);
        String ret = replaceTaskIdFromFilename(filename, taskId, newTaskId);
        return (ret);
    }

    public static String replaceTaskId(String taskId, int bucketNum) {
        String bucketNumStr = String.valueOf(bucketNum);
        Matcher m = PREFIXED_TASK_ID_REGEX.matcher(taskId);
        if (!m.matches()) {
            LOG.warn("Unable to determine bucket number from task id: " + taskId + ". Using " + "task ID as bucket number.");
            return adjustBucketNumLen(bucketNumStr, taskId);
        } else {
            String adjustedBucketNum = adjustBucketNumLen(bucketNumStr, m.group(2));
            return (m.group(1) == null ? "" : m.group(1)) + adjustedBucketNum;
        }
    }

    private static String replaceTaskId(String taskId, String strBucketNum) {
        Matcher m = PREFIXED_TASK_ID_REGEX.matcher(strBucketNum);
        if (!m.matches()) {
            LOG.warn("Unable to determine bucket number from file ID: " + strBucketNum + ". Using " + "file ID as bucket number.");
            return adjustBucketNumLen(strBucketNum, taskId);
        } else {
            String adjustedBucketNum = adjustBucketNumLen(m.group(2), taskId);
            return (m.group(1) == null ? "" : m.group(1)) + adjustedBucketNum;
        }
    }

    private static String adjustBucketNumLen(String bucketNum, String taskId) {
        int bucketNumLen = bucketNum.length();
        int taskIdLen = taskId.length();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < taskIdLen - bucketNumLen; i++) {
            s.append("0");
        }
        s.append(bucketNum);
        return s.toString();
    }

    private static String replaceTaskIdFromFilename(String filename, String oldTaskId, String newTaskId) {
        String[] spl = filename.split(oldTaskId);
        if ((spl.length == 0) || (spl.length == 1)) {
            return filename.replaceAll(oldTaskId, newTaskId);
        }
        StringBuilder snew = new StringBuilder();
        for (int idx = 0; idx < spl.length - 1; idx++) {
            if (idx > 0) {
                snew.append(oldTaskId);
            }
            snew.append(spl[idx]);
        }
        snew.append(newTaskId);
        snew.append(spl[spl.length - 1]);
        return snew.toString();
    }

    public static FileStatus[] listStatusIfExists(Path path, FileSystem fs) throws IOException {
        try {
            return fs.listStatus(path, FileUtils.HIDDEN_FILES_PATH_FILTER);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void mvFileToFinalPath(Path specPath, Configuration hconf, boolean success, Logger log, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Reporter reporter) throws IOException, HiveException {
        FileSystem fs = specPath.getFileSystem(hconf);
        Path tmpPath = Utilities.toTempPath(specPath);
        Path taskTmpPath = Utilities.toTaskTempPath(specPath);
        if (success) {
            FileStatus[] statuses = HiveStatsUtils.getFileStatusRecurse(tmpPath, ((dpCtx == null) ? 1 : dpCtx.getNumDPCols()), fs);
            if (statuses != null && statuses.length > 0) {
                PerfLogger perfLogger = SessionState.getPerfLogger();
                Set<Path> filesKept = new HashSet<Path>();
                perfLogger.PerfLogBegin("FileSinkOperator", "RemoveTempOrDuplicateFiles");
                List<Path> emptyBuckets = Utilities.removeTempOrDuplicateFiles(fs, statuses, dpCtx, conf, hconf, filesKept);
                perfLogger.PerfLogEnd("FileSinkOperator", "RemoveTempOrDuplicateFiles");
                if (emptyBuckets.size() > 0) {
                    perfLogger.PerfLogBegin("FileSinkOperator", "CreateEmptyBuckets");
                    createEmptyBuckets(hconf, emptyBuckets, conf.getCompressed(), conf.getTableInfo(), reporter);
                    filesKept.addAll(emptyBuckets);
                    perfLogger.PerfLogEnd("FileSinkOperator", "CreateEmptyBuckets");
                }
                Utilities.LOG14535.info("Moving tmp dir: " + tmpPath + " to: " + specPath);
                perfLogger.PerfLogBegin("FileSinkOperator", "RenameOrMoveFiles");
                if (HiveConf.getBoolVar(hconf, HiveConf.ConfVars.HIVE_EXEC_MOVE_FILES_FROM_SOURCE_DIR)) {
                    Utilities.moveSpecifiedFiles(fs, tmpPath, specPath, filesKept);
                } else {
                    Utilities.renameOrMoveFiles(fs, tmpPath, specPath);
                }
                perfLogger.PerfLogEnd("FileSinkOperator", "RenameOrMoveFiles");
            }
        } else {
            Utilities.LOG14535.info("deleting tmpPath " + tmpPath);
            fs.delete(tmpPath, true);
        }
        Utilities.LOG14535.info("deleting taskTmpPath " + taskTmpPath);
        fs.delete(taskTmpPath, true);
    }

    static void createEmptyBuckets(Configuration hconf, List<Path> paths, boolean isCompressed, TableDesc tableInfo, Reporter reporter) throws HiveException, IOException {
        JobConf jc;
        if (hconf instanceof JobConf) {
            jc = new JobConf(hconf);
        } else {
            jc = new JobConf(hconf);
        }
        HiveOutputFormat<?, ?> hiveOutputFormat = null;
        Class<? extends Writable> outputClass = null;
        try {
            Serializer serializer = (Serializer) tableInfo.getDeserializerClass().newInstance();
            serializer.initialize(null, tableInfo.getProperties());
            outputClass = serializer.getSerializedClass();
            hiveOutputFormat = HiveFileFormatUtils.getHiveOutputFormat(hconf, tableInfo);
        } catch (SerDeException e) {
            throw new HiveException(e);
        } catch (InstantiationException e) {
            throw new HiveException(e);
        } catch (IllegalAccessException e) {
            throw new HiveException(e);
        }
        for (Path path : paths) {
            Utilities.LOG14535.info("creating empty bucket for " + path);
            RecordWriter writer = HiveFileFormatUtils.getRecordWriter(jc, hiveOutputFormat, outputClass, isCompressed, tableInfo.getProperties(), path, reporter);
            writer.close(false);
            LOG.info("created empty bucket for enforcing bucketing at " + path);
        }
    }

    private static void addFilesToPathSet(Collection<FileStatus> files, Set<Path> fileSet) {
        for (FileStatus file : files) {
            fileSet.add(file.getPath());
        }
    }

    public static void removeTempOrDuplicateFiles(FileSystem fs, Path path) throws IOException {
        removeTempOrDuplicateFiles(fs, path, null, null, null);
    }

    public static List<Path> removeTempOrDuplicateFiles(FileSystem fs, Path path, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Configuration hconf) throws IOException {
        if (path == null) {
            return null;
        }
        FileStatus[] stats = HiveStatsUtils.getFileStatusRecurse(path, ((dpCtx == null) ? 1 : dpCtx.getNumDPCols()), fs);
        return removeTempOrDuplicateFiles(fs, stats, dpCtx, conf, hconf);
    }

    public static List<Path> removeTempOrDuplicateFiles(FileSystem fs, FileStatus[] fileStats, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Configuration hconf) throws IOException {
        return removeTempOrDuplicateFiles(fs, fileStats, dpCtx, conf, hconf, null);
    }

    public static List<Path> removeTempOrDuplicateFiles(FileSystem fs, FileStatus[] fileStats, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Configuration hconf, Set<Path> filesKept) throws IOException {
        int dpLevels = dpCtx == null ? 0 : dpCtx.getNumDPCols(), numBuckets = (conf != null && conf.getTable() != null) ? conf.getTable().getNumBuckets() : 0;
        return removeTempOrDuplicateFiles(fs, fileStats, dpLevels, numBuckets, hconf, null, 0, false);
    }

    private static boolean removeEmptyDpDirectory(FileSystem fs, Path path) throws IOException {
        FileStatus[] items = fs.listStatus(path);
        if (items.length != 0)
            return false;
        if (!fs.delete(path, true)) {
            LOG.error("Cannot delete empty directory " + path);
            throw new IOException("Cannot delete empty directory " + path);
        }
        return true;
    }

    public static List<Path> removeTempOrDuplicateFiles(FileSystem fs, FileStatus[] fileStats, int dpLevels, int numBuckets, Configuration hconf, Long txnId, int stmtId, boolean isMmTable) throws IOException {
        if (fileStats == null) {
            return null;
        }
        List<Path> result = new ArrayList<Path>();
        HashMap<String, FileStatus> taskIDToFile = null;
        if (dpLevels > 0) {
            FileStatus[] parts = fileStats;
            for (int i = 0; i < parts.length; ++i) {
                assert parts[i].isDirectory() : "dynamic partition " + parts[i].getPath() + " is not a directory";
                Path path = parts[i].getPath();
                Utilities.LOG14535.info("removeTempOrDuplicateFiles looking at DP " + path);
                if (removeEmptyDpDirectory(fs, path)) {
                    parts[i] = null;
                    continue;
                }
                FileStatus[] items = fs.listStatus(path);
                if (isMmTable) {
                    Path mmDir = parts[i].getPath();
                    if (!mmDir.getName().equals(AcidUtils.deltaSubdir(txnId, txnId, stmtId))) {
                        throw new IOException("Unexpected non-MM directory name " + mmDir);
                    }
                    Utilities.LOG14535.info("removeTempOrDuplicateFiles processing files in MM directory " + mmDir);
                }
                taskIDToFile = removeTempOrDuplicateFilesNonMm(items, fs);
                addBucketFileToResults(taskIDToFile, numBuckets, hconf, result);
            }
        } else {
            FileStatus[] items = fileStats;
            if (items.length == 0) {
                return result;
            }
            if (!isMmTable) {
                taskIDToFile = removeTempOrDuplicateFilesNonMm(items, fs);
            } else {
                if (items.length > 1) {
                    throw new IOException("Unexpected directories for non-DP MM: " + Arrays.toString(items));
                }
                Path mmDir = items[0].getPath();
                if (!mmDir.getName().equals(AcidUtils.deltaSubdir(txnId, txnId, stmtId))) {
                    throw new IOException("Unexpected non-MM directory " + mmDir);
                }
                Utilities.LOG14535.info("removeTempOrDuplicateFiles processing files in MM directory " + mmDir);
                taskIDToFile = removeTempOrDuplicateFilesNonMm(fs.listStatus(mmDir), fs);
            }
            addBucketFileToResults2(taskIDToFile, numBuckets, hconf, result);
        }
        return result;
    }

    private static void addBucketFileToResults2(HashMap<String, FileStatus> taskIDToFile, int numBuckets, Configuration hconf, List<Path> result) {
        if (taskIDToFile != null && taskIDToFile.size() > 0 && (numBuckets > taskIDToFile.size()) && !"tez".equalsIgnoreCase(hconf.get(ConfVars.HIVE_EXECUTION_ENGINE.varname))) {
            addBucketsToResultsCommon(taskIDToFile, numBuckets, result);
        }
    }

    private static void addBucketFileToResults(HashMap<String, FileStatus> taskIDToFile, int numBuckets, Configuration hconf, List<Path> result) {
        if (numBuckets > 0 && taskIDToFile != null && !"tez".equalsIgnoreCase(hconf.get(ConfVars.HIVE_EXECUTION_ENGINE.varname))) {
            addBucketsToResultsCommon(taskIDToFile, numBuckets, result);
        }
    }

    private static void addBucketsToResultsCommon(HashMap<String, FileStatus> taskIDToFile, int numBuckets, List<Path> result) {
        String taskID1 = taskIDToFile.keySet().iterator().next();
        Path bucketPath = taskIDToFile.values().iterator().next().getPath();
        Utilities.LOG14535.info("Bucket path " + bucketPath);
        for (int j = 0; j < numBuckets; ++j) {
            addBucketFileIfMissing(result, taskIDToFile, taskID1, bucketPath, j);
        }
    }

    private static void addBucketFileIfMissing(List<Path> result, HashMap<String, FileStatus> taskIDToFile, String taskID1, Path bucketPath, int j) {
        String taskID2 = replaceTaskId(taskID1, j);
        if (!taskIDToFile.containsKey(taskID2)) {
            URI bucketUri = bucketPath.toUri();
            String path2 = replaceTaskIdFromFilename(bucketUri.getPath().toString(), j);
            Utilities.LOG14535.info("Creating an empty bucket file " + path2);
            result.add(new Path(bucketUri.getScheme(), bucketUri.getAuthority(), path2));
        }
    }

    private static HashMap<String, FileStatus> removeTempOrDuplicateFilesNonMm(FileStatus[] files, FileSystem fs) throws IOException {
        if (files == null || fs == null) {
            return null;
        }
        HashMap<String, FileStatus> taskIdToFile = new HashMap<String, FileStatus>();
        for (FileStatus one : files) {
            if (isTempPath(one)) {
                Utilities.LOG14535.info("removeTempOrDuplicateFiles deleting " + one.getPath());
                if (!fs.delete(one.getPath(), true)) {
                    throw new IOException("Unable to delete tmp file: " + one.getPath());
                }
            } else {
                ponderRemovingTempOrDuplicateFile(fs, one, taskIdToFile);
            }
        }
        return taskIdToFile;
    }

    private static void ponderRemovingTempOrDuplicateFile(FileSystem fs, FileStatus file, HashMap<String, FileStatus> taskIdToFile) throws IOException {
        String taskId = getPrefixedTaskIdFromFilename(file.getPath().getName());
        Utilities.LOG14535.info("removeTempOrDuplicateFiles pondering " + file.getPath() + ", taskId " + taskId);
        FileStatus otherFile = taskIdToFile.get(taskId);
        taskIdToFile.put(taskId, (otherFile == null) ? file : compareTempOrDuplicateFiles(fs, file, otherFile));
    }

    private static FileStatus compareTempOrDuplicateFiles(FileSystem fs, FileStatus file, FileStatus existingFile) throws IOException {
        FileStatus toDelete = null, toRetain = null;
        if (isCopyFile(file.getPath().getName())) {
            LOG.info(file.getPath() + " file identified as duplicate. This file is" + " not deleted as it has copySuffix.");
            return existingFile;
        }
        if (existingFile.getLen() >= file.getLen()) {
            toDelete = file;
            toRetain = existingFile;
        } else {
            toDelete = existingFile;
            toRetain = file;
        }
        if (!fs.delete(toDelete.getPath(), true)) {
            throw new IOException("Unable to delete duplicate file: " + toDelete.getPath() + ". Existing file: " + toRetain.getPath());
        } else {
            LOG.warn("Duplicate taskid file removed: " + toDelete.getPath() + " with length " + toDelete.getLen() + ". Existing file: " + toRetain.getPath() + " with length " + toRetain.getLen());
        }
        return toRetain;
    }

    public static boolean isCopyFile(String filename) {
        String taskId = filename;
        String copyFileSuffix = null;
        int dirEnd = filename.lastIndexOf(Path.SEPARATOR);
        if (dirEnd != -1) {
            taskId = filename.substring(dirEnd + 1);
        }
        Matcher m = COPY_FILE_NAME_TO_TASK_ID_REGEX.matcher(taskId);
        if (!m.matches()) {
            LOG.warn("Unable to verify if file name " + filename + " has _copy_ suffix.");
        } else {
            taskId = m.group(1);
            copyFileSuffix = m.group(4);
        }
        LOG.debug("Filename: " + filename + " TaskId: " + taskId + " CopySuffix: " + copyFileSuffix);
        if (taskId != null && copyFileSuffix != null) {
            return true;
        }
        return false;
    }

    public static String getBucketFileNameFromPathSubString(String bucketName) {
        try {
            return bucketName.split(COPY_KEYWORD)[0];
        } catch (Exception e) {
            e.printStackTrace();
            return bucketName;
        }
    }

    public static int parseSplitBucket(InputSplit split) {
        if (split instanceof FileSplit) {
            return getBucketIdFromFile(((FileSplit) split).getPath().getName());
        }
        return -1;
    }

    public static int getBucketIdFromFile(String bucketName) {
        Matcher m = PREFIXED_BUCKET_ID_REGEX.matcher(bucketName);
        if (m.matches()) {
            if (m.group(2).isEmpty()) {
                return m.group(1).isEmpty() ? -1 : 0;
            }
            return Integer.parseInt(m.group(2));
        }
        if (bucketName.startsWith(AcidUtils.BUCKET_PREFIX)) {
            m = AcidUtils.BUCKET_DIGIT_PATTERN.matcher(bucketName);
            if (m.find()) {
                return Integer.parseInt(m.group());
            }
        }
        return -1;
    }

    public static String getNameMessage(Throwable e) {
        return e.getClass().getName() + "(" + e.getMessage() + ")";
    }

    public static String getResourceFiles(Configuration conf, SessionState.ResourceType t) {
        SessionState ss = SessionState.get();
        Set<String> files = (ss == null) ? null : ss.list_resource(t, null);
        if (files != null) {
            List<String> realFiles = new ArrayList<String>(files.size());
            for (String one : files) {
                try {
                    String onefile = realFile(one, conf);
                    if (onefile != null) {
                        realFiles.add(realFile(one, conf));
                    } else {
                        LOG.warn("The file " + one + " does not exist.");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Cannot validate file " + one + "due to exception: " + e.getMessage(), e);
                }
            }
            return StringUtils.join(realFiles, ",");
        } else {
            return "";
        }
    }

    public static ClassLoader getSessionSpecifiedClassLoader() {
        SessionState state = SessionState.get();
        if (state == null || state.getConf() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Hive Conf not found or Session not initiated, use thread based class loader instead");
            }
            return JavaUtils.getClassLoader();
        }
        ClassLoader sessionCL = state.getConf().getClassLoader();
        if (sessionCL != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Use session specified class loader");
            }
            return sessionCL;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Session specified class loader not found, use thread based class loader");
        }
        return JavaUtils.getClassLoader();
    }

    public static void restoreSessionSpecifiedClassLoader(ClassLoader prev) {
        SessionState state = SessionState.get();
        if (state != null && state.getConf() != null) {
            ClassLoader current = state.getConf().getClassLoader();
            if (current != prev && JavaUtils.closeClassLoadersTo(current, prev)) {
                Thread.currentThread().setContextClassLoader(prev);
                state.getConf().setClassLoader(prev);
            }
        }
    }

    private static URL urlFromPathString(String onestr) {
        URL oneurl = null;
        try {
            if (StringUtils.indexOf(onestr, "file:/") == 0) {
                oneurl = new URL(onestr);
            } else {
                oneurl = new File(onestr).toURL();
            }
        } catch (Exception err) {
            LOG.error("Bad URL " + onestr + ", ignoring path");
        }
        return oneurl;
    }

    private static boolean useExistingClassLoader(ClassLoader cl) {
        if (!(cl instanceof UDFClassLoader)) {
            return false;
        }
        final UDFClassLoader udfClassLoader = (UDFClassLoader) cl;
        if (udfClassLoader.isClosed()) {
            return false;
        }
        return true;
    }

    public static ClassLoader addToClassPath(ClassLoader cloader, String[] newPaths) {
        final URLClassLoader loader = (URLClassLoader) cloader;
        if (useExistingClassLoader(cloader)) {
            final UDFClassLoader udfClassLoader = (UDFClassLoader) loader;
            for (String path : newPaths) {
                udfClassLoader.addURL(urlFromPathString(path));
            }
            return udfClassLoader;
        } else {
            return createUDFClassLoader(loader, newPaths);
        }
    }

    public static ClassLoader createUDFClassLoader(URLClassLoader loader, String[] newPaths) {
        final Set<URL> curPathsSet = Sets.newHashSet(loader.getURLs());
        final List<URL> curPaths = Lists.newArrayList(curPathsSet);
        for (String onestr : newPaths) {
            final URL oneurl = urlFromPathString(onestr);
            if (oneurl != null && !curPathsSet.contains(oneurl)) {
                curPaths.add(oneurl);
            }
        }
        return new UDFClassLoader(curPaths.toArray(new URL[0]), loader);
    }

    public static void removeFromClassPath(String[] pathsToRemove) throws IOException {
        Thread curThread = Thread.currentThread();
        URLClassLoader loader = (URLClassLoader) curThread.getContextClassLoader();
        Set<URL> newPath = new HashSet<URL>(Arrays.asList(loader.getURLs()));
        for (String onestr : pathsToRemove) {
            URL oneurl = urlFromPathString(onestr);
            if (oneurl != null) {
                newPath.remove(oneurl);
            }
        }
        JavaUtils.closeClassLoader(loader);
        Registry reg = SessionState.getRegistry();
        if (reg != null) {
            reg.removeFromUDFLoaders(loader);
        }
        loader = new UDFClassLoader(newPath.toArray(new URL[0]));
        curThread.setContextClassLoader(loader);
        SessionState.get().getConf().setClassLoader(loader);
    }

    public static String formatBinaryString(byte[] array, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            sb.append("x");
            sb.append(array[i] < 0 ? array[i] + 256 : array[i] + 0);
        }
        return sb.toString();
    }

    public static List<String> getColumnNamesFromSortCols(List<Order> sortCols) {
        List<String> names = new ArrayList<String>();
        for (Order o : sortCols) {
            names.add(o.getCol());
        }
        return names;
    }

    public static List<String> getColumnNamesFromFieldSchema(List<FieldSchema> partCols) {
        List<String> names = new ArrayList<String>();
        for (FieldSchema o : partCols) {
            names.add(o.getName());
        }
        return names;
    }

    public static List<String> getInternalColumnNamesFromSignature(List<ColumnInfo> colInfos) {
        List<String> names = new ArrayList<String>();
        for (ColumnInfo ci : colInfos) {
            names.add(ci.getInternalName());
        }
        return names;
    }

    public static List<String> getColumnNames(Properties props) {
        List<String> names = new ArrayList<String>();
        String colNames = props.getProperty(serdeConstants.LIST_COLUMNS);
        String[] cols = colNames.trim().split(",");
        if (cols != null) {
            for (String col : cols) {
                if (col != null && !col.trim().equals("")) {
                    names.add(col);
                }
            }
        }
        return names;
    }

    public static List<String> getColumnTypes(Properties props) {
        List<String> names = new ArrayList<String>();
        String colNames = props.getProperty(serdeConstants.LIST_COLUMN_TYPES);
        ArrayList<TypeInfo> cols = TypeInfoUtils.getTypeInfosFromTypeString(colNames);
        for (TypeInfo col : cols) {
            names.add(col.getTypeName());
        }
        return names;
    }

    public static String[] getDbTableName(String dbtable) throws SemanticException {
        return getDbTableName(SessionState.get().getCurrentDatabase(), dbtable);
    }

    public static String[] getDbTableName(String defaultDb, String dbtable) throws SemanticException {
        if (dbtable == null) {
            return new String[2];
        }
        String[] names = dbtable.split("\\.");
        switch(names.length) {
            case 2:
                return names;
            case 1:
                return new String[] { defaultDb, dbtable };
            default:
                throw new SemanticException(ErrorMsg.INVALID_TABLE_NAME, dbtable);
        }
    }

    public static String getDatabaseName(String dbTableName) throws SemanticException {
        String[] split = dbTableName.split("\\.");
        if (split.length != 2) {
            throw new SemanticException(ErrorMsg.INVALID_TABLE_NAME, dbTableName);
        }
        return split[0];
    }

    public static String getTableName(String dbTableName) throws SemanticException {
        String[] split = dbTableName.split("\\.");
        if (split.length != 2) {
            throw new SemanticException(ErrorMsg.INVALID_TABLE_NAME, dbTableName);
        }
        return split[1];
    }

    public static void validateColumnNames(List<String> colNames, List<String> checkCols) throws SemanticException {
        Iterator<String> checkColsIter = checkCols.iterator();
        while (checkColsIter.hasNext()) {
            String toCheck = checkColsIter.next();
            boolean found = false;
            Iterator<String> colNamesIter = colNames.iterator();
            while (colNamesIter.hasNext()) {
                String colName = colNamesIter.next();
                if (toCheck.equalsIgnoreCase(colName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new SemanticException(ErrorMsg.INVALID_COLUMN.getMsg());
            }
        }
    }

    public static int getDefaultNotificationInterval(Configuration hconf) {
        int notificationInterval;
        Integer expInterval = Integer.decode(hconf.get("mapred.tasktracker.expiry.interval"));
        if (expInterval != null) {
            notificationInterval = expInterval.intValue() / 2;
        } else {
            notificationInterval = 5 * 60 * 1000;
        }
        return notificationInterval;
    }

    public static void copyTableJobPropertiesToConf(TableDesc tbl, JobConf job) throws HiveException {
        Properties tblProperties = tbl.getProperties();
        for (String name : tblProperties.stringPropertyNames()) {
            if (job.get(name) == null) {
                String val = (String) tblProperties.get(name);
                if (val != null) {
                    job.set(name, StringEscapeUtils.escapeJava(val));
                }
            }
        }
        Map<String, String> jobProperties = tbl.getJobProperties();
        if (jobProperties != null) {
            for (Map.Entry<String, String> entry : jobProperties.entrySet()) {
                job.set(entry.getKey(), entry.getValue());
            }
        }
        try {
            Map<String, String> jobSecrets = tbl.getJobSecrets();
            if (jobSecrets != null) {
                for (Map.Entry<String, String> entry : jobSecrets.entrySet()) {
                    job.getCredentials().addSecretKey(new Text(entry.getKey()), entry.getValue().getBytes());
                    UserGroupInformation.getCurrentUser().getCredentials().addSecretKey(new Text(entry.getKey()), entry.getValue().getBytes());
                }
            }
        } catch (IOException e) {
            throw new HiveException(e);
        }
    }

    public static void copyTablePropertiesToConf(TableDesc tbl, JobConf job) throws HiveException {
        Properties tblProperties = tbl.getProperties();
        for (String name : tblProperties.stringPropertyNames()) {
            String val = (String) tblProperties.get(name);
            if (val != null) {
                job.set(name, StringEscapeUtils.escapeJava(val));
            }
        }
        Map<String, String> jobProperties = tbl.getJobProperties();
        if (jobProperties != null) {
            for (Map.Entry<String, String> entry : jobProperties.entrySet()) {
                job.set(entry.getKey(), entry.getValue());
            }
        }
        try {
            Map<String, String> jobSecrets = tbl.getJobSecrets();
            if (jobSecrets != null) {
                for (Map.Entry<String, String> entry : jobSecrets.entrySet()) {
                    job.getCredentials().addSecretKey(new Text(entry.getKey()), entry.getValue().getBytes());
                    UserGroupInformation.getCurrentUser().getCredentials().addSecretKey(new Text(entry.getKey()), entry.getValue().getBytes());
                }
            }
        } catch (IOException e) {
            throw new HiveException(e);
        }
    }

    private static final Object INPUT_SUMMARY_LOCK = new Object();

    @VisibleForTesting
    static int getMaxExecutorsForInputListing(final Configuration conf, int inputLocationListSize) {
        if (inputLocationListSize < 1)
            return 0;
        int maxExecutors = 1;
        if (inputLocationListSize > 1) {
            int listingMaxThreads = HiveConf.getIntVar(conf, ConfVars.HIVE_EXEC_INPUT_LISTING_MAX_THREADS);
            if (listingMaxThreads <= 0) {
                listingMaxThreads = conf.getInt(DEPRECATED_MAPRED_DFSCLIENT_PARALLELISM_MAX, 0);
                if (listingMaxThreads > 0) {
                    LOG.warn("Deprecated configuration is used: " + DEPRECATED_MAPRED_DFSCLIENT_PARALLELISM_MAX + ". Please use " + ConfVars.HIVE_EXEC_INPUT_LISTING_MAX_THREADS.varname);
                }
            }
            if (listingMaxThreads > 1) {
                maxExecutors = Math.min(inputLocationListSize, listingMaxThreads);
            }
        }
        return maxExecutors;
    }

    public static ContentSummary getInputSummary(final Context ctx, MapWork work, PathFilter filter) throws IOException {
        PerfLogger perfLogger = SessionState.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.INPUT_SUMMARY);
        long[] summary = { 0, 0, 0 };
        final Set<Path> pathNeedProcess = new HashSet<>();
        synchronized (INPUT_SUMMARY_LOCK) {
            for (Path path : work.getPathToAliases().keySet()) {
                Path p = path;
                if (filter != null && !filter.accept(p)) {
                    continue;
                }
                ContentSummary cs = ctx.getCS(path);
                if (cs == null) {
                    if (path == null) {
                        continue;
                    }
                    pathNeedProcess.add(path);
                } else {
                    summary[0] += cs.getLength();
                    summary[1] += cs.getFileCount();
                    summary[2] += cs.getDirectoryCount();
                }
            }
            final Map<String, ContentSummary> resultMap = new ConcurrentHashMap<String, ContentSummary>();
            final ExecutorService executor;
            int numExecutors = getMaxExecutorsForInputListing(ctx.getConf(), pathNeedProcess.size());
            if (numExecutors > 1) {
                LOG.info("Using " + numExecutors + " threads for getContentSummary");
                executor = Executors.newFixedThreadPool(numExecutors, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Get-Input-Summary-%d").build());
            } else {
                executor = null;
            }
            ContentSummary cs = getInputSummaryWithPool(ctx, pathNeedProcess, work, summary, executor);
            perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.INPUT_SUMMARY);
            return cs;
        }
    }

    @VisibleForTesting
    static ContentSummary getInputSummaryWithPool(final Context ctx, Set<Path> pathNeedProcess, MapWork work, long[] summary, ExecutorService executor) throws IOException {
        List<Future<?>> results = new ArrayList<Future<?>>();
        final Map<String, ContentSummary> resultMap = new ConcurrentHashMap<String, ContentSummary>();
        HiveInterruptCallback interrup = HiveInterruptUtils.add(new HiveInterruptCallback() {

            @Override
            public void interrupt() {
                for (Path path : pathNeedProcess) {
                    try {
                        path.getFileSystem(ctx.getConf()).close();
                    } catch (IOException ignore) {
                        LOG.debug("Failed to close filesystem", ignore);
                    }
                }
                if (executor != null) {
                    executor.shutdownNow();
                }
            }
        });
        try {
            Configuration conf = ctx.getConf();
            JobConf jobConf = new JobConf(conf);
            for (Path path : pathNeedProcess) {
                final Path p = path;
                final String pathStr = path.toString();
                final Configuration myConf = conf;
                final JobConf myJobConf = jobConf;
                final Map<String, Operator<?>> aliasToWork = work.getAliasToWork();
                final Map<Path, ArrayList<String>> pathToAlias = work.getPathToAliases();
                final PartitionDesc partDesc = work.getPathToPartitionInfo().get(p);
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Class<? extends InputFormat> inputFormatCls = partDesc.getInputFileFormatClass();
                            InputFormat inputFormatObj = HiveInputFormat.getInputFormatFromCache(inputFormatCls, myJobConf);
                            if (inputFormatObj instanceof ContentSummaryInputFormat) {
                                ContentSummaryInputFormat cs = (ContentSummaryInputFormat) inputFormatObj;
                                resultMap.put(pathStr, cs.getContentSummary(p, myJobConf));
                                return;
                            }
                            String metaTableStorage = null;
                            if (partDesc.getTableDesc() != null && partDesc.getTableDesc().getProperties() != null) {
                                metaTableStorage = partDesc.getTableDesc().getProperties().getProperty(hive_metastoreConstants.META_TABLE_STORAGE, null);
                            }
                            if (partDesc.getProperties() != null) {
                                metaTableStorage = partDesc.getProperties().getProperty(hive_metastoreConstants.META_TABLE_STORAGE, metaTableStorage);
                            }
                            HiveStorageHandler handler = HiveUtils.getStorageHandler(myConf, metaTableStorage);
                            if (handler instanceof InputEstimator) {
                                long total = 0;
                                TableDesc tableDesc = partDesc.getTableDesc();
                                InputEstimator estimator = (InputEstimator) handler;
                                for (String alias : HiveFileFormatUtils.doGetAliasesFromPath(pathToAlias, p)) {
                                    JobConf jobConf = new JobConf(myJobConf);
                                    TableScanOperator scanOp = (TableScanOperator) aliasToWork.get(alias);
                                    Utilities.setColumnNameList(jobConf, scanOp, true);
                                    Utilities.setColumnTypeList(jobConf, scanOp, true);
                                    PlanUtils.configureInputJobPropertiesForStorageHandler(tableDesc);
                                    Utilities.copyTableJobPropertiesToConf(tableDesc, jobConf);
                                    total += estimator.estimate(jobConf, scanOp, -1).getTotalLength();
                                }
                                resultMap.put(pathStr, new ContentSummary(total, -1, -1));
                            } else {
                                FileSystem fs = p.getFileSystem(myConf);
                                resultMap.put(pathStr, fs.getContentSummary(p));
                            }
                        } catch (Exception e) {
                            LOG.info("Cannot get size of " + pathStr + ". Safely ignored.");
                        }
                    }
                };
                if (executor == null) {
                    r.run();
                } else {
                    Future<?> result = executor.submit(r);
                    results.add(result);
                }
            }
            if (executor != null) {
                for (Future<?> result : results) {
                    boolean executorDone = false;
                    do {
                        try {
                            result.get();
                            executorDone = true;
                        } catch (InterruptedException e) {
                            LOG.info("Interrupted when waiting threads: ", e);
                            Thread.currentThread().interrupt();
                            break;
                        } catch (ExecutionException e) {
                            throw new IOException(e);
                        }
                    } while (!executorDone);
                }
                executor.shutdown();
            }
            HiveInterruptUtils.checkInterrupted();
            for (Map.Entry<String, ContentSummary> entry : resultMap.entrySet()) {
                ContentSummary cs = entry.getValue();
                summary[0] += cs.getLength();
                summary[1] += cs.getFileCount();
                summary[2] += cs.getDirectoryCount();
                ctx.addCS(entry.getKey(), cs);
                LOG.info("Cache Content Summary for " + entry.getKey() + " length: " + cs.getLength() + " file count: " + cs.getFileCount() + " directory count: " + cs.getDirectoryCount());
            }
            return new ContentSummary(summary[0], summary[1], summary[2]);
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
            HiveInterruptUtils.remove(interrup);
        }
    }

    public static long sumOf(Map<String, Long> aliasToSize, Set<String> aliases) {
        return sumOfExcept(aliasToSize, aliases, null);
    }

    public static long sumOfExcept(Map<String, Long> aliasToSize, Set<String> aliases, Set<String> excepts) {
        long total = 0;
        for (String alias : aliases) {
            if (excepts != null && excepts.contains(alias)) {
                continue;
            }
            Long size = aliasToSize.get(alias);
            if (size == null) {
                return -1;
            }
            total += size;
        }
        return total;
    }

    public static boolean isEmptyPath(JobConf job, Path dirPath, Context ctx) throws Exception {
        if (ctx != null) {
            ContentSummary cs = ctx.getCS(dirPath);
            if (cs != null) {
                LOG.info("Content Summary " + dirPath + "length: " + cs.getLength() + " num files: " + cs.getFileCount() + " num directories: " + cs.getDirectoryCount());
                return (cs.getLength() == 0 && cs.getFileCount() == 0 && cs.getDirectoryCount() <= 1);
            } else {
                LOG.info("Content Summary not cached for " + dirPath);
            }
        }
        return isEmptyPath(job, dirPath);
    }

    public static boolean isEmptyPath(Configuration job, Path dirPath) throws IOException {
        FileSystem inpFs = dirPath.getFileSystem(job);
        try {
            FileStatus[] fStats = inpFs.listStatus(dirPath, FileUtils.HIDDEN_FILES_PATH_FILTER);
            if (fStats.length > 0) {
                return false;
            }
        } catch (FileNotFoundException fnf) {
            return true;
        }
        return true;
    }

    public static List<TezTask> getTezTasks(List<Task<? extends Serializable>> tasks) {
        List<TezTask> tezTasks = new ArrayList<TezTask>();
        if (tasks != null) {
            Set<Task<? extends Serializable>> visited = new HashSet<Task<? extends Serializable>>();
            while (!tasks.isEmpty()) {
                tasks = getTezTasks(tasks, tezTasks, visited);
            }
        }
        return tezTasks;
    }

    private static List<Task<? extends Serializable>> getTezTasks(List<Task<? extends Serializable>> tasks, List<TezTask> tezTasks, Set<Task<? extends Serializable>> visited) {
        List<Task<? extends Serializable>> childTasks = new ArrayList<>();
        for (Task<? extends Serializable> task : tasks) {
            if (visited.contains(task)) {
                continue;
            }
            if (task instanceof TezTask && !tezTasks.contains(task)) {
                tezTasks.add((TezTask) task);
            }
            if (task.getDependentTasks() != null) {
                childTasks.addAll(task.getDependentTasks());
            }
            visited.add(task);
        }
        return childTasks;
    }

    public static List<SparkTask> getSparkTasks(List<Task<? extends Serializable>> tasks) {
        List<SparkTask> sparkTasks = new ArrayList<SparkTask>();
        if (tasks != null) {
            Set<Task<? extends Serializable>> visited = new HashSet<Task<? extends Serializable>>();
            while (!tasks.isEmpty()) {
                tasks = getSparkTasks(tasks, sparkTasks, visited);
            }
        }
        return sparkTasks;
    }

    private static List<Task<? extends Serializable>> getSparkTasks(List<Task<? extends Serializable>> tasks, List<SparkTask> sparkTasks, Set<Task<? extends Serializable>> visited) {
        List<Task<? extends Serializable>> childTasks = new ArrayList<>();
        for (Task<? extends Serializable> task : tasks) {
            if (visited.contains(task)) {
                continue;
            }
            if (task instanceof SparkTask && !sparkTasks.contains(task)) {
                sparkTasks.add((SparkTask) task);
            }
            if (task.getDependentTasks() != null) {
                childTasks.addAll(task.getDependentTasks());
            }
            visited.add(task);
        }
        return childTasks;
    }

    public static List<ExecDriver> getMRTasks(List<Task<? extends Serializable>> tasks) {
        List<ExecDriver> mrTasks = new ArrayList<ExecDriver>();
        if (tasks != null) {
            Set<Task<? extends Serializable>> visited = new HashSet<Task<? extends Serializable>>();
            while (!tasks.isEmpty()) {
                tasks = getMRTasks(tasks, mrTasks, visited);
            }
        }
        return mrTasks;
    }

    private static List<Task<? extends Serializable>> getMRTasks(List<Task<? extends Serializable>> tasks, List<ExecDriver> mrTasks, Set<Task<? extends Serializable>> visited) {
        List<Task<? extends Serializable>> childTasks = new ArrayList<>();
        for (Task<? extends Serializable> task : tasks) {
            if (visited.contains(task)) {
                continue;
            }
            if (task instanceof ExecDriver && !mrTasks.contains(task)) {
                mrTasks.add((ExecDriver) task);
            }
            if (task.getDependentTasks() != null) {
                childTasks.addAll(task.getDependentTasks());
            }
            visited.add(task);
        }
        return childTasks;
    }

    public static List<LinkedHashMap<String, String>> getFullDPSpecs(Configuration conf, DynamicPartitionCtx dpCtx) throws HiveException {
        try {
            Path loadPath = dpCtx.getRootPath();
            FileSystem fs = loadPath.getFileSystem(conf);
            int numDPCols = dpCtx.getNumDPCols();
            FileStatus[] status = HiveStatsUtils.getFileStatusRecurse(loadPath, numDPCols, fs);
            if (status.length == 0) {
                LOG.warn("No partition is generated by dynamic partitioning");
                return null;
            }
            Map<String, String> partSpec = dpCtx.getPartSpec();
            List<LinkedHashMap<String, String>> fullPartSpecs = new ArrayList<LinkedHashMap<String, String>>();
            for (int i = 0; i < status.length; ++i) {
                Path partPath = status[i].getPath();
                assert fs.getFileStatus(partPath).isDir() : "partitions " + partPath + " is not a directory !";
                LinkedHashMap<String, String> fullPartSpec = new LinkedHashMap<String, String>(partSpec);
                if (!Warehouse.makeSpecFromName(fullPartSpec, partPath, new HashSet<String>(partSpec.keySet()))) {
                    Utilities.LOG14535.warn("Ignoring invalid DP directory " + partPath);
                    continue;
                }
                Utilities.LOG14535.info("Adding partition spec from " + partPath + ": " + fullPartSpec);
                fullPartSpecs.add(fullPartSpec);
            }
            return fullPartSpecs;
        } catch (IOException e) {
            throw new HiveException(e);
        }
    }

    public static StatsPublisher getStatsPublisher(JobConf jc) {
        StatsFactory factory = StatsFactory.newFactory(jc);
        return factory == null ? null : factory.getStatsPublisher();
    }

    public static String join(String... elements) {
        StringBuilder builder = new StringBuilder();
        for (String element : elements) {
            if (element == null || element.isEmpty()) {
                continue;
            }
            builder.append(element);
            if (!element.endsWith(Path.SEPARATOR)) {
                builder.append(Path.SEPARATOR);
            }
        }
        return builder.toString();
    }

    public static void setColumnNameList(JobConf jobConf, RowSchema rowSchema) {
        setColumnNameList(jobConf, rowSchema, false);
    }

    public static void setColumnNameList(JobConf jobConf, RowSchema rowSchema, boolean excludeVCs) {
        if (rowSchema == null) {
            return;
        }
        StringBuilder columnNames = new StringBuilder();
        for (ColumnInfo colInfo : rowSchema.getSignature()) {
            if (excludeVCs && colInfo.getIsVirtualCol()) {
                continue;
            }
            if (columnNames.length() > 0) {
                columnNames.append(",");
            }
            columnNames.append(colInfo.getInternalName());
        }
        String columnNamesString = columnNames.toString();
        jobConf.set(serdeConstants.LIST_COLUMNS, columnNamesString);
    }

    public static void setColumnNameList(JobConf jobConf, Operator op) {
        setColumnNameList(jobConf, op, false);
    }

    public static void setColumnNameList(JobConf jobConf, Operator op, boolean excludeVCs) {
        RowSchema rowSchema = op.getSchema();
        setColumnNameList(jobConf, rowSchema, excludeVCs);
    }

    public static void setColumnTypeList(JobConf jobConf, RowSchema rowSchema) {
        setColumnTypeList(jobConf, rowSchema, false);
    }

    public static void setColumnTypeList(JobConf jobConf, RowSchema rowSchema, boolean excludeVCs) {
        if (rowSchema == null) {
            return;
        }
        StringBuilder columnTypes = new StringBuilder();
        for (ColumnInfo colInfo : rowSchema.getSignature()) {
            if (excludeVCs && colInfo.getIsVirtualCol()) {
                continue;
            }
            if (columnTypes.length() > 0) {
                columnTypes.append(",");
            }
            columnTypes.append(colInfo.getTypeName());
        }
        String columnTypesString = columnTypes.toString();
        jobConf.set(serdeConstants.LIST_COLUMN_TYPES, columnTypesString);
    }

    public static void setColumnTypeList(JobConf jobConf, Operator op) {
        setColumnTypeList(jobConf, op, false);
    }

    public static void setColumnTypeList(JobConf jobConf, Operator op, boolean excludeVCs) {
        RowSchema rowSchema = op.getSchema();
        setColumnTypeList(jobConf, rowSchema, excludeVCs);
    }

    public static final String suffix = ".hashtable";

    public static Path generatePath(Path basePath, String dumpFilePrefix, Byte tag, String bigBucketFileName) {
        return new Path(basePath, "MapJoin-" + dumpFilePrefix + tag + "-" + bigBucketFileName + suffix);
    }

    public static String generateFileName(Byte tag, String bigBucketFileName) {
        String fileName = new String("MapJoin-" + tag + "-" + bigBucketFileName + suffix);
        return fileName;
    }

    public static Path generateTmpPath(Path basePath, String id) {
        return new Path(basePath, "HashTable-" + id);
    }

    public static Path generateTarPath(Path basePath, String filename) {
        return new Path(basePath, filename + ".tar.gz");
    }

    public static String generateTarFileName(String name) {
        return name + ".tar.gz";
    }

    public static String generatePath(Path baseURI, String filename) {
        String path = new String(baseURI + Path.SEPARATOR + filename);
        return path;
    }

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    public static double showTime(long time) {
        double result = (double) time / (double) 1000;
        return result;
    }

    public static void reworkMapRedWork(Task<? extends Serializable> task, boolean reworkMapredWork, HiveConf conf) throws SemanticException {
        if (reworkMapredWork && (task instanceof MapRedTask)) {
            try {
                MapredWork mapredWork = ((MapRedTask) task).getWork();
                Set<Class<? extends InputFormat>> reworkInputFormats = new HashSet<Class<? extends InputFormat>>();
                for (PartitionDesc part : mapredWork.getMapWork().getPathToPartitionInfo().values()) {
                    Class<? extends InputFormat> inputFormatCls = part.getInputFileFormatClass();
                    if (ReworkMapredInputFormat.class.isAssignableFrom(inputFormatCls)) {
                        reworkInputFormats.add(inputFormatCls);
                    }
                }
                if (reworkInputFormats.size() > 0) {
                    for (Class<? extends InputFormat> inputFormatCls : reworkInputFormats) {
                        ReworkMapredInputFormat inst = (ReworkMapredInputFormat) ReflectionUtil.newInstance(inputFormatCls, null);
                        inst.rework(conf, mapredWork);
                    }
                }
            } catch (IOException e) {
                throw new SemanticException(e);
            }
        }
    }

    public static class SQLCommand<T> {

        public T run(PreparedStatement stmt) throws SQLException {
            return null;
        }
    }

    public static <T> T executeWithRetry(SQLCommand<T> cmd, PreparedStatement stmt, long baseWindow, int maxRetries) throws SQLException {
        Random r = new Random();
        T result = null;
        for (int failures = 0; ; failures++) {
            try {
                result = cmd.run(stmt);
                return result;
            } catch (SQLTransientException e) {
                LOG.warn("Failure and retry #" + failures + " with exception " + e.getMessage());
                if (failures >= maxRetries) {
                    throw e;
                }
                long waitTime = getRandomWaitTime(baseWindow, failures, r);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException iex) {
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    public static Connection connectWithRetry(String connectionString, long waitWindow, int maxRetries) throws SQLException {
        Random r = new Random();
        for (int failures = 0; ; failures++) {
            try {
                Connection conn = DriverManager.getConnection(connectionString);
                return conn;
            } catch (SQLTransientException e) {
                if (failures >= maxRetries) {
                    LOG.error("Error during JDBC connection. " + e);
                    throw e;
                }
                long waitTime = Utilities.getRandomWaitTime(waitWindow, failures, r);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e1) {
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    public static PreparedStatement prepareWithRetry(Connection conn, String stmt, long waitWindow, int maxRetries) throws SQLException {
        Random r = new Random();
        for (int failures = 0; ; failures++) {
            try {
                return conn.prepareStatement(stmt);
            } catch (SQLTransientException e) {
                if (failures >= maxRetries) {
                    LOG.error("Error preparing JDBC Statement " + stmt + " :" + e);
                    throw e;
                }
                long waitTime = Utilities.getRandomWaitTime(waitWindow, failures, r);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e1) {
                }
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    public static void setQueryTimeout(java.sql.Statement stmt, int timeout) throws SQLException {
        if (timeout < 0) {
            LOG.info("Invalid query timeout " + timeout);
            return;
        }
        try {
            stmt.setQueryTimeout(timeout);
        } catch (SQLException e) {
            String message = e.getMessage() == null ? null : e.getMessage().toLowerCase();
            if (e instanceof SQLFeatureNotSupportedException || (message != null && (message.contains("implemented") || message.contains("supported")))) {
                LOG.info("setQueryTimeout is not supported");
                return;
            }
            throw e;
        }
    }

    public static long getRandomWaitTime(long baseWindow, int failures, Random r) {
        return (long) (baseWindow * failures + baseWindow * (failures + 1) * r.nextDouble());
    }

    public static final char sqlEscapeChar = '\\';

    public static String escapeSqlLike(String key) {
        StringBuilder sb = new StringBuilder(key.length());
        for (char c : key.toCharArray()) {
            switch(c) {
                case '_':
                case '%':
                case sqlEscapeChar:
                    sb.append(sqlEscapeChar);
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String formatMsecToStr(long msec) {
        long day = -1, hour = -1, minute = -1, second = -1;
        long ms = msec % 1000;
        long timeLeft = msec / 1000;
        if (timeLeft > 0) {
            second = timeLeft % 60;
            timeLeft /= 60;
            if (timeLeft > 0) {
                minute = timeLeft % 60;
                timeLeft /= 60;
                if (timeLeft > 0) {
                    hour = timeLeft % 24;
                    day = timeLeft / 24;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        if (day != -1) {
            sb.append(day + " days ");
        }
        if (hour != -1) {
            sb.append(hour + " hours ");
        }
        if (minute != -1) {
            sb.append(minute + " minutes ");
        }
        if (second != -1) {
            sb.append(second + " seconds ");
        }
        sb.append(ms + " msec");
        return sb.toString();
    }

    public static int estimateNumberOfReducers(HiveConf conf, ContentSummary inputSummary, MapWork work, boolean finalMapRed) throws IOException {
        long bytesPerReducer = conf.getLongVar(HiveConf.ConfVars.BYTESPERREDUCER);
        int maxReducers = conf.getIntVar(HiveConf.ConfVars.MAXREDUCERS);
        double samplePercentage = getHighestSamplePercentage(work);
        long totalInputFileSize = getTotalInputFileSize(inputSummary, work, samplePercentage);
        if (totalInputFileSize != inputSummary.getLength()) {
            LOG.info("BytesPerReducer=" + bytesPerReducer + " maxReducers=" + maxReducers + " estimated totalInputFileSize=" + totalInputFileSize);
        } else {
            LOG.info("BytesPerReducer=" + bytesPerReducer + " maxReducers=" + maxReducers + " totalInputFileSize=" + totalInputFileSize);
        }
        boolean powersOfTwo = conf.getBoolVar(HiveConf.ConfVars.HIVE_INFER_BUCKET_SORT_NUM_BUCKETS_POWER_TWO) && finalMapRed && !work.getBucketedColsByDirectory().isEmpty();
        return estimateReducers(totalInputFileSize, bytesPerReducer, maxReducers, powersOfTwo);
    }

    public static int estimateReducers(long totalInputFileSize, long bytesPerReducer, int maxReducers, boolean powersOfTwo) {
        double bytes = Math.max(totalInputFileSize, bytesPerReducer);
        int reducers = (int) Math.ceil(bytes / bytesPerReducer);
        reducers = Math.max(1, reducers);
        reducers = Math.min(maxReducers, reducers);
        int reducersLog = (int) (Math.log(reducers) / Math.log(2)) + 1;
        int reducersPowerTwo = (int) Math.pow(2, reducersLog);
        if (powersOfTwo) {
            if (reducersPowerTwo / 2 == reducers) {
            } else if (reducersPowerTwo > maxReducers) {
                reducers = reducersPowerTwo / 2;
            } else {
                reducers = reducersPowerTwo;
            }
        }
        return reducers;
    }

    public static long getTotalInputFileSize(ContentSummary inputSummary, MapWork work, double highestSamplePercentage) {
        long totalInputFileSize = inputSummary.getLength();
        if (work.getNameToSplitSample() == null || work.getNameToSplitSample().isEmpty()) {
            return totalInputFileSize;
        }
        if (highestSamplePercentage >= 0) {
            totalInputFileSize = Math.min((long) (totalInputFileSize * (highestSamplePercentage / 100D)), totalInputFileSize);
        }
        return totalInputFileSize;
    }

    public static long getTotalInputNumFiles(ContentSummary inputSummary, MapWork work, double highestSamplePercentage) {
        long totalInputNumFiles = inputSummary.getFileCount();
        if (work.getNameToSplitSample() == null || work.getNameToSplitSample().isEmpty()) {
            return totalInputNumFiles;
        }
        if (highestSamplePercentage >= 0) {
            totalInputNumFiles = Math.min((long) (totalInputNumFiles * (highestSamplePercentage / 100D)), totalInputNumFiles);
        }
        return totalInputNumFiles;
    }

    public static double getHighestSamplePercentage(MapWork work) {
        double highestSamplePercentage = 0;
        for (String alias : work.getAliasToWork().keySet()) {
            if (work.getNameToSplitSample().containsKey(alias)) {
                Double rate = work.getNameToSplitSample().get(alias).getPercent();
                if (rate != null && rate > highestSamplePercentage) {
                    highestSamplePercentage = rate;
                }
            } else {
                highestSamplePercentage = -1;
                break;
            }
        }
        return highestSamplePercentage;
    }

    public static List<Path> getInputPathsTez(JobConf job, MapWork work) throws Exception {
        String scratchDir = job.get(DagUtils.TEZ_TMP_DIR_KEY);
        List<Path> paths = getInputPaths(job, work, new Path(scratchDir), null, true);
        return paths;
    }

    public static List<Path> getInputPaths(JobConf job, MapWork work, Path hiveScratchDir, Context ctx, boolean skipDummy) throws Exception {
        Set<Path> pathsProcessed = new HashSet<Path>();
        List<Path> pathsToAdd = new LinkedList<Path>();
        LockedDriverState lDrvStat = LockedDriverState.getLockedDriverState();
        Collection<String> aliasToWork = work.getAliasToWork().keySet();
        if (!skipDummy) {
            aliasToWork = new ArrayList<>(aliasToWork);
        }
        for (String alias : aliasToWork) {
            LOG.info("Processing alias " + alias);
            Collection<Map.Entry<Path, ArrayList<String>>> pathToAliases = work.getPathToAliases().entrySet();
            if (!skipDummy) {
                pathToAliases = new ArrayList<>(pathToAliases);
            }
            boolean isEmptyTable = true;
            boolean hasLogged = false;
            Path path = null;
            for (Map.Entry<Path, ArrayList<String>> e : pathToAliases) {
                if (lDrvStat != null && lDrvStat.driverState == DriverState.INTERRUPT)
                    throw new IOException("Operation is Canceled.");
                Path file = e.getKey();
                List<String> aliases = e.getValue();
                if (aliases.contains(alias)) {
                    if (file != null) {
                        isEmptyTable = false;
                    } else {
                        LOG.warn("Found a null path for alias " + alias);
                        continue;
                    }
                    if (pathsProcessed.contains(file)) {
                        continue;
                    }
                    StringInternUtils.internUriStringsInPath(file);
                    pathsProcessed.add(file);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding input file " + file);
                    } else if (!hasLogged) {
                        hasLogged = true;
                        LOG.info("Adding " + work.getPathToAliases().size() + " inputs; the first input is " + file);
                    }
                    pathsToAdd.add(file);
                }
            }
            if (isEmptyTable && !skipDummy) {
                pathsToAdd.add(createDummyFileForEmptyTable(job, work, hiveScratchDir, alias));
            }
        }
        List<Path> finalPathsToAdd = new LinkedList<>();
        int numExecutors = getMaxExecutorsForInputListing(job, pathsToAdd.size());
        if (numExecutors > 1) {
            ExecutorService pool = Executors.newFixedThreadPool(numExecutors, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Get-Input-Paths-%d").build());
            finalPathsToAdd.addAll(getInputPathsWithPool(job, work, hiveScratchDir, ctx, skipDummy, pathsToAdd, pool));
        } else {
            for (final Path path : pathsToAdd) {
                if (lDrvStat != null && lDrvStat.driverState == DriverState.INTERRUPT) {
                    throw new IOException("Operation is Canceled.");
                }
                Path newPath = new GetInputPathsCallable(path, job, work, hiveScratchDir, ctx, skipDummy).call();
                updatePathForMapWork(newPath, work, path);
                finalPathsToAdd.add(newPath);
            }
        }
        return finalPathsToAdd;
    }

    @VisibleForTesting
    static List<Path> getInputPathsWithPool(JobConf job, MapWork work, Path hiveScratchDir, Context ctx, boolean skipDummy, List<Path> pathsToAdd, ExecutorService pool) throws IOException, ExecutionException, InterruptedException {
        LockedDriverState lDrvStat = LockedDriverState.getLockedDriverState();
        List<Path> finalPathsToAdd = new ArrayList<>();
        try {
            Map<GetInputPathsCallable, Future<Path>> getPathsCallableToFuture = new LinkedHashMap<>();
            for (final Path path : pathsToAdd) {
                if (lDrvStat != null && lDrvStat.driverState == DriverState.INTERRUPT) {
                    throw new IOException("Operation is Canceled.");
                }
                GetInputPathsCallable callable = new GetInputPathsCallable(path, job, work, hiveScratchDir, ctx, skipDummy);
                getPathsCallableToFuture.put(callable, pool.submit(callable));
            }
            pool.shutdown();
            for (Map.Entry<GetInputPathsCallable, Future<Path>> future : getPathsCallableToFuture.entrySet()) {
                if (lDrvStat != null && lDrvStat.driverState == DriverState.INTERRUPT) {
                    throw new IOException("Operation is Canceled.");
                }
                Path newPath = future.getValue().get();
                updatePathForMapWork(newPath, work, future.getKey().path);
                finalPathsToAdd.add(newPath);
            }
        } finally {
            pool.shutdownNow();
        }
        return finalPathsToAdd;
    }

    private static class GetInputPathsCallable implements Callable<Path> {

        private final Path path;

        private final JobConf job;

        private final MapWork work;

        private final Path hiveScratchDir;

        private final Context ctx;

        private final boolean skipDummy;

        private GetInputPathsCallable(Path path, JobConf job, MapWork work, Path hiveScratchDir, Context ctx, boolean skipDummy) {
            this.path = path;
            this.job = job;
            this.work = work;
            this.hiveScratchDir = hiveScratchDir;
            this.ctx = ctx;
            this.skipDummy = skipDummy;
        }

        @Override
        public Path call() throws Exception {
            if (!this.skipDummy && isEmptyPath(this.job, this.path, this.ctx)) {
                return createDummyFileForEmptyPartition(this.path, this.job, this.work.getPathToPartitionInfo().get(this.path), this.hiveScratchDir);
            }
            return this.path;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Path createEmptyFile(Path hiveScratchDir, HiveOutputFormat outFileFormat, JobConf job, Properties props, boolean dummyRow) throws IOException, InstantiationException, IllegalAccessException {
        String newDir = hiveScratchDir + Path.SEPARATOR + UUID.randomUUID().toString();
        Path newPath = new Path(newDir);
        FileSystem fs = newPath.getFileSystem(job);
        fs.mkdirs(newPath);
        newPath = fs.makeQualified(newPath);
        String newFile = newDir + Path.SEPARATOR + "emptyFile";
        Path newFilePath = new Path(newFile);
        RecordWriter recWriter = outFileFormat.getHiveRecordWriter(job, newFilePath, Text.class, false, props, null);
        if (dummyRow) {
            recWriter.write(new Text("empty"));
        }
        recWriter.close(false);
        return StringInternUtils.internUriStringsInPath(newPath);
    }

    @SuppressWarnings("rawtypes")
    private static Path createDummyFileForEmptyPartition(Path path, JobConf job, PartitionDesc partDesc, Path hiveScratchDir) throws Exception {
        String strPath = path.toString();
        if (partDesc.getTableDesc().isNonNative()) {
            return path;
        }
        Properties props = SerDeUtils.createOverlayedProperties(partDesc.getTableDesc().getProperties(), partDesc.getProperties());
        HiveOutputFormat outFileFormat = HiveFileFormatUtils.getHiveOutputFormat(job, partDesc);
        boolean oneRow = partDesc.getInputFileFormatClass() == OneNullRowInputFormat.class;
        Path newPath = createEmptyFile(hiveScratchDir, outFileFormat, job, props, oneRow);
        if (LOG.isInfoEnabled()) {
            LOG.info("Changed input file " + strPath + " to empty file " + newPath + " (" + oneRow + ")");
        }
        return newPath;
    }

    private static void updatePathForMapWork(Path newPath, MapWork work, Path path) {
        if (!newPath.equals(path)) {
            PartitionDesc partDesc = work.getPathToPartitionInfo().get(path);
            work.addPathToAlias(newPath, work.getPathToAliases().get(path));
            work.removePathToAlias(path);
            work.removePathToPartitionInfo(path);
            work.addPathToPartitionInfo(newPath, partDesc);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Path createDummyFileForEmptyTable(JobConf job, MapWork work, Path hiveScratchDir, String alias) throws Exception {
        TableDesc tableDesc = work.getAliasToPartnInfo().get(alias).getTableDesc();
        if (tableDesc.isNonNative()) {
            return null;
        }
        Properties props = tableDesc.getProperties();
        HiveOutputFormat outFileFormat = HiveFileFormatUtils.getHiveOutputFormat(job, tableDesc);
        Path newPath = createEmptyFile(hiveScratchDir, outFileFormat, job, props, false);
        if (LOG.isInfoEnabled()) {
            LOG.info("Changed input file for alias " + alias + " to " + newPath);
        }
        LinkedHashMap<Path, ArrayList<String>> pathToAliases = work.getPathToAliases();
        ArrayList<String> newList = new ArrayList<String>();
        newList.add(alias);
        pathToAliases.put(newPath, newList);
        work.setPathToAliases(pathToAliases);
        PartitionDesc pDesc = work.getAliasToPartnInfo().get(alias).clone();
        work.addPathToPartitionInfo(newPath, pDesc);
        return newPath;
    }

    public static void setInputPaths(JobConf job, List<Path> pathsToAdd) {
        Path[] addedPaths = FileInputFormat.getInputPaths(job);
        if (addedPaths == null) {
            addedPaths = new Path[0];
        }
        Path[] combined = new Path[addedPaths.length + pathsToAdd.size()];
        System.arraycopy(addedPaths, 0, combined, 0, addedPaths.length);
        int i = 0;
        for (Path p : pathsToAdd) {
            combined[addedPaths.length + (i++)] = p;
        }
        FileInputFormat.setInputPaths(job, combined);
    }

    public static void setInputAttributes(Configuration conf, MapWork mWork) {
        HiveConf.ConfVars var = HiveConf.getVar(conf, HiveConf.ConfVars.HIVE_EXECUTION_ENGINE).equals("tez") ? HiveConf.ConfVars.HIVETEZINPUTFORMAT : HiveConf.ConfVars.HIVEINPUTFORMAT;
        if (mWork.getInputformat() != null) {
            HiveConf.setVar(conf, var, mWork.getInputformat());
        }
        if (mWork.getIndexIntermediateFile() != null) {
            conf.set(ConfVars.HIVE_INDEX_COMPACT_FILE.varname, mWork.getIndexIntermediateFile());
            conf.set(ConfVars.HIVE_INDEX_BLOCKFILTER_FILE.varname, mWork.getIndexIntermediateFile());
        }
        conf.setBoolean("hive.input.format.sorted", mWork.isInputFormatSorted());
    }

    public static void createTmpDirs(Configuration conf, MapWork mWork) throws IOException {
        Map<Path, ArrayList<String>> pa = mWork.getPathToAliases();
        if (pa != null) {
            HashSet<String> aliases = new HashSet<String>(1);
            List<Operator<? extends OperatorDesc>> ops = new ArrayList<Operator<? extends OperatorDesc>>();
            for (List<String> ls : pa.values()) {
                for (String a : ls) {
                    aliases.add(a);
                }
            }
            for (String a : aliases) {
                ops.add(mWork.getAliasToWork().get(a));
            }
            createTmpDirs(conf, ops);
        }
    }

    @SuppressWarnings("unchecked")
    public static void createTmpDirs(Configuration conf, ReduceWork rWork) throws IOException {
        if (rWork == null) {
            return;
        }
        List<Operator<? extends OperatorDesc>> ops = new LinkedList<Operator<? extends OperatorDesc>>();
        ops.add(rWork.getReducer());
        createTmpDirs(conf, ops);
    }

    private static void createTmpDirs(Configuration conf, List<Operator<? extends OperatorDesc>> ops) throws IOException {
        while (!ops.isEmpty()) {
            Operator<? extends OperatorDesc> op = ops.remove(0);
            if (op instanceof FileSinkOperator) {
                FileSinkDesc fdesc = ((FileSinkOperator) op).getConf();
                if (fdesc.isMmTable())
                    continue;
                Path tempDir = fdesc.getDirName();
                if (tempDir != null) {
                    Path tempPath = Utilities.toTempPath(tempDir);
                    FileSystem fs = tempPath.getFileSystem(conf);
                    fs.mkdirs(tempPath);
                }
            }
            if (op.getChildOperators() != null) {
                ops.addAll(op.getChildOperators());
            }
        }
    }

    public static boolean createDirsWithPermission(Configuration conf, Path mkdirPath, FsPermission fsPermission, boolean recursive) throws IOException {
        String origUmask = null;
        LOG.debug("Create dirs " + mkdirPath + " with permission " + fsPermission + " recursive " + recursive);
        if (recursive) {
            origUmask = conf.get(FsPermission.UMASK_LABEL);
            conf.set(FsPermission.UMASK_LABEL, "000");
        }
        FileSystem fs = ShimLoader.getHadoopShims().getNonCachedFileSystem(mkdirPath.toUri(), conf);
        boolean retval = false;
        try {
            retval = fs.mkdirs(mkdirPath, fsPermission);
            resetUmaskInConf(conf, recursive, origUmask);
        } catch (IOException ioe) {
            resetUmaskInConf(conf, recursive, origUmask);
            throw ioe;
        } finally {
            IOUtils.closeStream(fs);
        }
        return retval;
    }

    private static void resetUmaskInConf(Configuration conf, boolean unsetUmask, String origUmask) {
        if (unsetUmask) {
            if (origUmask != null) {
                conf.set(FsPermission.UMASK_LABEL, origUmask);
            } else {
                conf.unset(FsPermission.UMASK_LABEL);
            }
        }
    }

    public static boolean getUseVectorizedInputFileFormat(Configuration conf) {
        if (conf.get(VECTOR_MODE) != null) {
            return conf.getBoolean(VECTOR_MODE, false) && conf.getBoolean(USE_VECTORIZED_INPUT_FILE_FORMAT, false);
        } else {
            if (HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_VECTORIZATION_ENABLED) && Utilities.getPlanPath(conf) != null) {
                MapWork mapWork = Utilities.getMapWork(conf);
                return (mapWork.getVectorMode() && mapWork.getUseVectorizedInputFileFormat());
            } else {
                return false;
            }
        }
    }

    public static boolean getUseVectorizedInputFileFormat(Configuration conf, MapWork mapWork) {
        return HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_VECTORIZATION_ENABLED) && mapWork.getVectorMode() && mapWork.getUseVectorizedInputFileFormat();
    }

    public static VectorizedRowBatchCtx getVectorizedRowBatchCtx(Configuration conf) {
        VectorizedRowBatchCtx result = null;
        if (HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_VECTORIZATION_ENABLED) && Utilities.getPlanPath(conf) != null) {
            MapWork mapWork = Utilities.getMapWork(conf);
            if (mapWork != null && mapWork.getVectorMode()) {
                result = mapWork.getVectorizedRowBatchCtx();
            }
        }
        return result;
    }

    public static void clearWorkMapForConf(Configuration conf) {
        Path mapPath = getPlanPath(conf, MAP_PLAN_NAME);
        Path reducePath = getPlanPath(conf, REDUCE_PLAN_NAME);
        if (mapPath != null) {
            gWorkMap.get(conf).remove(mapPath);
        }
        if (reducePath != null) {
            gWorkMap.get(conf).remove(reducePath);
        }
    }

    public static void clearWorkMap(Configuration conf) {
        gWorkMap.get(conf).clear();
    }

    public static File createTempDir(String baseDir) {
        final int MAX_ATTEMPS = 30;
        for (int i = 0; i < MAX_ATTEMPS; i++) {
            String tempDirName = "tmp_" + ((int) (100000 * Math.random()));
            File tempDir = new File(baseDir, tempDirName);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create a temp dir under " + baseDir + " Giving up after " + MAX_ATTEMPS + " attempts");
    }

    public static boolean skipHeader(RecordReader<WritableComparable, Writable> currRecReader, int headerCount, WritableComparable key, Writable value) throws IOException {
        while (headerCount > 0) {
            if (!currRecReader.next(key, value))
                return false;
            headerCount--;
        }
        return true;
    }

    public static int getHeaderCount(TableDesc table) throws IOException {
        int headerCount;
        try {
            headerCount = Integer.parseInt(table.getProperties().getProperty(serdeConstants.HEADER_COUNT, "0"));
        } catch (NumberFormatException nfe) {
            throw new IOException(nfe);
        }
        return headerCount;
    }

    public static int getFooterCount(TableDesc table, JobConf job) throws IOException {
        int footerCount;
        try {
            footerCount = Integer.parseInt(table.getProperties().getProperty(serdeConstants.FOOTER_COUNT, "0"));
            if (footerCount > HiveConf.getIntVar(job, HiveConf.ConfVars.HIVE_FILE_MAX_FOOTER)) {
                throw new IOException("footer number exceeds the limit defined in hive.file.max.footer");
            }
        } catch (NumberFormatException nfe) {
            throw new IOException(nfe);
        }
        return footerCount;
    }

    public static String getQualifiedPath(HiveConf conf, Path path) throws HiveException {
        FileSystem fs;
        if (path == null) {
            return null;
        }
        try {
            fs = path.getFileSystem(conf);
            return fs.makeQualified(path).toString();
        } catch (IOException e) {
            throw new HiveException(e);
        }
    }

    public static boolean isDefaultNameNode(HiveConf conf) {
        return !conf.getChangedProperties().containsKey(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);
    }

    public static boolean isPerfOrAboveLogging(HiveConf conf) {
        String loggingLevel = conf.getVar(HiveConf.ConfVars.HIVE_SERVER2_LOGGING_OPERATION_LEVEL);
        return conf.getBoolVar(HiveConf.ConfVars.HIVE_SERVER2_LOGGING_OPERATION_ENABLED) && (loggingLevel.equalsIgnoreCase("PERFORMANCE") || loggingLevel.equalsIgnoreCase("VERBOSE"));
    }

    @SuppressWarnings("rawtypes")
    public static String jarFinderGetJar(Class klass) {
        Preconditions.checkNotNull(klass, "klass");
        ClassLoader loader = klass.getClassLoader();
        if (loader != null) {
            String class_file = klass.getName().replaceAll("\\.", "/") + ".class";
            try {
                for (Enumeration itr = loader.getResources(class_file); itr.hasMoreElements(); ) {
                    URL url = (URL) itr.nextElement();
                    String path = url.getPath();
                    if (path.startsWith("file:")) {
                        path = path.substring("file:".length());
                    }
                    path = URLDecoder.decode(path, "UTF-8");
                    if ("jar".equals(url.getProtocol())) {
                        path = URLDecoder.decode(path, "UTF-8");
                        return path.replaceAll("!.*$", "");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static int getDPColOffset(FileSinkDesc conf) {
        if (conf.getWriteType() == AcidUtils.Operation.DELETE) {
            return 1;
        } else if (conf.getWriteType() == AcidUtils.Operation.UPDATE) {
            return getColumnNames(conf.getTableInfo().getProperties()).size() + 1;
        } else {
            return getColumnNames(conf.getTableInfo().getProperties()).size();
        }
    }

    public static List<String> getStatsTmpDirs(BaseWork work, Configuration conf) {
        List<String> statsTmpDirs = new ArrayList<>();
        if (!StatsSetupConst.StatDB.fs.name().equalsIgnoreCase(HiveConf.getVar(conf, ConfVars.HIVESTATSDBCLASS))) {
            return statsTmpDirs;
        }
        Set<Operator<? extends OperatorDesc>> ops = work.getAllLeafOperators();
        if (work instanceof MapWork) {
            ops.addAll(work.getAllRootOperators());
        }
        for (Operator<? extends OperatorDesc> op : ops) {
            OperatorDesc desc = op.getConf();
            String statsTmpDir = null;
            if (desc instanceof FileSinkDesc) {
                statsTmpDir = ((FileSinkDesc) desc).getStatsTmpDir();
            } else if (desc instanceof TableScanDesc) {
                statsTmpDir = ((TableScanDesc) desc).getTmpStatsDir();
            }
            if (statsTmpDir != null && !statsTmpDir.isEmpty()) {
                statsTmpDirs.add(statsTmpDir);
            }
        }
        return statsTmpDirs;
    }

    public static boolean isSchemaEvolutionEnabled(Configuration conf, boolean isAcid) {
        return isAcid || HiveConf.getBoolVar(conf, ConfVars.HIVE_SCHEMA_EVOLUTION);
    }

    public static boolean isInputFileFormatSelfDescribing(PartitionDesc pd) {
        Class<?> inputFormatClass = pd.getInputFileFormatClass();
        return SelfDescribingInputFormatInterface.class.isAssignableFrom(inputFormatClass);
    }

    public static boolean isInputFileFormatVectorized(PartitionDesc pd) {
        Class<?> inputFormatClass = pd.getInputFileFormatClass();
        return VectorizedInputFormatInterface.class.isAssignableFrom(inputFormatClass);
    }

    public static void addSchemaEvolutionToTableScanOperator(Table table, TableScanOperator tableScanOp) {
        String colNames = MetaStoreUtils.getColumnNamesFromFieldSchema(table.getSd().getCols());
        String colTypes = MetaStoreUtils.getColumnTypesFromFieldSchema(table.getSd().getCols());
        tableScanOp.setSchemaEvolution(colNames, colTypes);
    }

    public static void addSchemaEvolutionToTableScanOperator(StructObjectInspector structOI, TableScanOperator tableScanOp) {
        String colNames = ObjectInspectorUtils.getFieldNames(structOI);
        String colTypes = ObjectInspectorUtils.getFieldTypes(structOI);
        tableScanOp.setSchemaEvolution(colNames, colTypes);
    }

    public static void unsetSchemaEvolution(Configuration conf) {
        conf.unset(IOConstants.SCHEMA_EVOLUTION_COLUMNS);
        conf.unset(IOConstants.SCHEMA_EVOLUTION_COLUMNS_TYPES);
    }

    public static void addTableSchemaToConf(Configuration conf, TableScanOperator tableScanOp) {
        String schemaEvolutionColumns = tableScanOp.getSchemaEvolutionColumns();
        if (schemaEvolutionColumns != null) {
            conf.set(IOConstants.SCHEMA_EVOLUTION_COLUMNS, tableScanOp.getSchemaEvolutionColumns());
            conf.set(IOConstants.SCHEMA_EVOLUTION_COLUMNS_TYPES, tableScanOp.getSchemaEvolutionColumnsTypes());
        } else {
            LOG.info("schema.evolution.columns and schema.evolution.columns.types not available");
        }
    }

    public static StandardStructObjectInspector constructVectorizedReduceRowOI(StructObjectInspector keyInspector, StructObjectInspector valueInspector) throws HiveException {
        ArrayList<String> colNames = new ArrayList<String>();
        ArrayList<ObjectInspector> ois = new ArrayList<ObjectInspector>();
        List<? extends StructField> fields = keyInspector.getAllStructFieldRefs();
        for (StructField field : fields) {
            colNames.add(Utilities.ReduceField.KEY.toString() + "." + field.getFieldName());
            ois.add(field.getFieldObjectInspector());
        }
        fields = valueInspector.getAllStructFieldRefs();
        for (StructField field : fields) {
            colNames.add(Utilities.ReduceField.VALUE.toString() + "." + field.getFieldName());
            ois.add(field.getFieldObjectInspector());
        }
        StandardStructObjectInspector rowObjectInspector = ObjectInspectorFactory.getStandardStructObjectInspector(colNames, ois);
        return rowObjectInspector;
    }

    private static String[] getReadColumnTypes(final List<String> readColumnNames, final List<String> allColumnNames, final List<String> allColumnTypes) {
        if (readColumnNames == null || allColumnNames == null || allColumnTypes == null || readColumnNames.isEmpty() || allColumnNames.isEmpty() || allColumnTypes.isEmpty()) {
            return null;
        }
        Map<String, String> columnNameToType = new HashMap<>();
        List<TypeInfo> types = TypeInfoUtils.typeInfosFromTypeNames(allColumnTypes);
        if (allColumnNames.size() != types.size()) {
            LOG.warn("Column names count does not match column types count." + " ColumnNames: {} [{}] ColumnTypes: {} [{}]", allColumnNames, allColumnNames.size(), allColumnTypes, types.size());
            return null;
        }
        for (int i = 0; i < allColumnNames.size(); i++) {
            columnNameToType.put(allColumnNames.get(i), types.get(i).toString());
        }
        String[] result = new String[readColumnNames.size()];
        for (int i = 0; i < readColumnNames.size(); i++) {
            result[i] = columnNameToType.get(readColumnNames.get(i));
        }
        return result;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String suffix = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f%sB", bytes / Math.pow(unit, exp), suffix);
    }

    private static final String MANIFEST_EXTENSION = ".manifest";

    private static void tryDelete(FileSystem fs, Path path) {
        try {
            fs.delete(path, true);
        } catch (IOException ex) {
            LOG.error("Failed to delete " + path, ex);
        }
    }

    public static Path[] getMmDirectoryCandidates(FileSystem fs, Path path, int dpLevels, int lbLevels, PathFilter filter, long txnId, int stmtId, Configuration conf) throws IOException {
        int skipLevels = dpLevels + lbLevels;
        if (filter == null) {
            filter = new JavaUtils.IdPathFilter(txnId, stmtId, true);
        }
        if (skipLevels == 0) {
            return statusToPath(fs.listStatus(path, filter));
        }
        if (HiveConf.getBoolVar(conf, ConfVars.HIVE_MM_AVOID_GLOBSTATUS_ON_S3) && isS3(fs)) {
            return getMmDirectoryCandidatesRecursive(fs, path, skipLevels, filter);
        }
        return getMmDirectoryCandidatesGlobStatus(fs, path, skipLevels, filter, txnId, stmtId);
    }

    private static boolean isS3(FileSystem fs) {
        try {
            return fs.getScheme().equalsIgnoreCase("s3a");
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    private static Path[] statusToPath(FileStatus[] statuses) {
        if (statuses == null)
            return null;
        Path[] paths = new Path[statuses.length];
        for (int i = 0; i < statuses.length; ++i) {
            paths[i] = statuses[i].getPath();
        }
        return paths;
    }

    private static Path[] getMmDirectoryCandidatesRecursive(FileSystem fs, Path path, int skipLevels, PathFilter filter) throws IOException {
        String lastRelDir = null;
        HashSet<Path> results = new HashSet<Path>();
        String relRoot = Path.getPathWithoutSchemeAndAuthority(path).toString();
        if (!relRoot.endsWith(Path.SEPARATOR)) {
            relRoot += Path.SEPARATOR;
        }
        RemoteIterator<LocatedFileStatus> allFiles = fs.listFiles(path, true);
        while (allFiles.hasNext()) {
            LocatedFileStatus lfs = allFiles.next();
            Path dirPath = Path.getPathWithoutSchemeAndAuthority(lfs.getPath());
            String dir = dirPath.toString();
            if (!dir.startsWith(relRoot)) {
                throw new IOException("Path " + lfs.getPath() + " is not under " + relRoot + " (when shortened to " + dir + ")");
            }
            String subDir = dir.substring(relRoot.length());
            Utilities.LOG14535.info("Looking at " + subDir + " from " + lfs.getPath());
            if (lastRelDir != null && subDir.startsWith(lastRelDir))
                continue;
            int startIx = skipLevels > 0 ? -1 : 0;
            for (int i = 0; i < skipLevels; ++i) {
                startIx = subDir.indexOf(Path.SEPARATOR_CHAR, startIx + 1);
                if (startIx == -1) {
                    Utilities.LOG14535.info("Expected level of nesting (" + skipLevels + ") is not " + " present in " + subDir + " (from " + lfs.getPath() + ")");
                    break;
                }
            }
            if (startIx == -1)
                continue;
            int endIx = subDir.indexOf(Path.SEPARATOR_CHAR, startIx + 1);
            if (endIx == -1) {
                Utilities.LOG14535.info("Expected level of nesting (" + (skipLevels + 1) + ") is not " + " present in " + subDir + " (from " + lfs.getPath() + ")");
                continue;
            }
            lastRelDir = subDir = subDir.substring(0, endIx);
            Path candidate = new Path(relRoot, subDir);
            Utilities.LOG14535.info("Considering MM directory candidate " + candidate);
            if (!filter.accept(candidate))
                continue;
            results.add(fs.makeQualified(candidate));
        }
        return results.toArray(new Path[results.size()]);
    }

    private static Path[] getMmDirectoryCandidatesGlobStatus(FileSystem fs, Path path, int skipLevels, PathFilter filter, long txnId, int stmtId) throws IOException {
        StringBuilder sb = new StringBuilder(path.toUri().getPath());
        for (int i = 0; i < skipLevels; i++) {
            sb.append(Path.SEPARATOR).append("*");
        }
        sb.append(Path.SEPARATOR).append(AcidUtils.deltaSubdir(txnId, txnId, stmtId));
        Path pathPattern = new Path(path, sb.toString());
        Utilities.LOG14535.info("Looking for files via: " + pathPattern);
        return statusToPath(fs.globStatus(pathPattern, filter));
    }

    private static void tryDeleteAllMmFiles(FileSystem fs, Path specPath, Path manifestDir, int dpLevels, int lbLevels, JavaUtils.IdPathFilter filter, long txnId, int stmtId, Configuration conf) throws IOException {
        Path[] files = getMmDirectoryCandidates(fs, specPath, dpLevels, lbLevels, filter, txnId, stmtId, conf);
        if (files != null) {
            for (Path path : files) {
                Utilities.LOG14535.info("Deleting " + path + " on failure");
                tryDelete(fs, path);
            }
        }
        Utilities.LOG14535.info("Deleting " + manifestDir + " on failure");
        fs.delete(manifestDir, true);
    }

    public static void writeMmCommitManifest(List<Path> commitPaths, Path specPath, FileSystem fs, String taskId, Long txnId, int stmtId, String unionSuffix) throws HiveException {
        if (commitPaths.isEmpty())
            return;
        Path manifestPath = getManifestDir(specPath, txnId, stmtId, unionSuffix);
        manifestPath = new Path(manifestPath, taskId + MANIFEST_EXTENSION);
        Utilities.LOG14535.info("Writing manifest to " + manifestPath + " with " + commitPaths);
        try {
            try (FSDataOutputStream out = fs.create(manifestPath, false)) {
                if (out == null) {
                    throw new HiveException("Failed to create manifest at " + manifestPath);
                }
                out.writeInt(commitPaths.size());
                for (Path path : commitPaths) {
                    out.writeUTF(path.toString());
                }
            }
        } catch (IOException e) {
            throw new HiveException(e);
        }
    }

    private static Path getManifestDir(Path specPath, long txnId, int stmtId, String unionSuffix) {
        Path manifestPath = new Path(specPath, "_tmp." + AcidUtils.deltaSubdir(txnId, txnId, stmtId));
        return (unionSuffix == null) ? manifestPath : new Path(manifestPath, unionSuffix);
    }

    public static final class MissingBucketsContext {

        public final TableDesc tableInfo;

        public final int numBuckets;

        public final boolean isCompressed;

        public MissingBucketsContext(TableDesc tableInfo, int numBuckets, boolean isCompressed) {
            this.tableInfo = tableInfo;
            this.numBuckets = numBuckets;
            this.isCompressed = isCompressed;
        }
    }

    public static void handleMmTableFinalPath(Path specPath, String unionSuffix, Configuration hconf, boolean success, int dpLevels, int lbLevels, MissingBucketsContext mbc, long txnId, int stmtId, Reporter reporter, boolean isMmTable, boolean isMmCtas) throws IOException, HiveException {
        FileSystem fs = specPath.getFileSystem(hconf);
        Path manifestDir = getManifestDir(specPath, txnId, stmtId, unionSuffix);
        if (!success) {
            JavaUtils.IdPathFilter filter = new JavaUtils.IdPathFilter(txnId, stmtId, true);
            tryDeleteAllMmFiles(fs, specPath, manifestDir, dpLevels, lbLevels, filter, txnId, stmtId, hconf);
            return;
        }
        Utilities.LOG14535.info("Looking for manifests in: " + manifestDir + " (" + txnId + ")");
        List<Path> manifests = new ArrayList<>();
        if (fs.exists(manifestDir)) {
            FileStatus[] manifestFiles = fs.listStatus(manifestDir);
            if (manifestFiles != null) {
                for (FileStatus status : manifestFiles) {
                    Path path = status.getPath();
                    if (path.getName().endsWith(MANIFEST_EXTENSION)) {
                        Utilities.LOG14535.info("Reading manifest " + path);
                        manifests.add(path);
                    }
                }
            }
        } else {
            Utilities.LOG14535.info("No manifests found - query produced no output");
            manifestDir = null;
        }
        Utilities.LOG14535.info("Looking for files in: " + specPath);
        JavaUtils.IdPathFilter filter = new JavaUtils.IdPathFilter(txnId, stmtId, true);
        if (isMmCtas && !fs.exists(specPath)) {
            Utilities.LOG14535.info("Creating table directory for CTAS with no output at " + specPath);
            FileUtils.mkdir(fs, specPath, hconf);
        }
        Path[] files = getMmDirectoryCandidates(fs, specPath, dpLevels, lbLevels, filter, txnId, stmtId, hconf);
        ArrayList<Path> mmDirectories = new ArrayList<>();
        if (files != null) {
            for (Path path : files) {
                Utilities.LOG14535.info("Looking at path: " + path);
                mmDirectories.add(path);
            }
        }
        HashSet<String> committed = new HashSet<>();
        for (Path mfp : manifests) {
            try (FSDataInputStream mdis = fs.open(mfp)) {
                int fileCount = mdis.readInt();
                for (int i = 0; i < fileCount; ++i) {
                    String nextFile = mdis.readUTF();
                    if (!committed.add(nextFile)) {
                        throw new HiveException(nextFile + " was specified in multiple manifests");
                    }
                }
            }
        }
        if (manifestDir != null) {
            Utilities.LOG14535.info("Deleting manifest directory " + manifestDir);
            tryDelete(fs, manifestDir);
            if (unionSuffix != null) {
                manifestDir = manifestDir.getParent();
                FileStatus[] remainingFiles = fs.listStatus(manifestDir);
                if (remainingFiles == null || remainingFiles.length == 0) {
                    Utilities.LOG14535.info("Deleting manifest directory " + manifestDir);
                    tryDelete(fs, manifestDir);
                }
            }
        }
        for (Path path : mmDirectories) {
            cleanMmDirectory(path, fs, unionSuffix, committed);
        }
        if (!committed.isEmpty()) {
            throw new HiveException("The following files were committed but not found: " + committed);
        }
        if (mmDirectories.isEmpty())
            return;
        if (lbLevels != 0)
            return;
        FileStatus[] finalResults = new FileStatus[mmDirectories.size()];
        for (int i = 0; i < mmDirectories.size(); ++i) {
            finalResults[i] = new PathOnlyFileStatus(mmDirectories.get(i));
        }
        List<Path> emptyBuckets = Utilities.removeTempOrDuplicateFiles(fs, finalResults, dpLevels, mbc == null ? 0 : mbc.numBuckets, hconf, txnId, stmtId, isMmTable);
        if (emptyBuckets.size() > 0) {
            assert mbc != null;
            Utilities.createEmptyBuckets(hconf, emptyBuckets, mbc.isCompressed, mbc.tableInfo, reporter);
        }
    }

    private static final class PathOnlyFileStatus extends FileStatus {

        public PathOnlyFileStatus(Path path) {
            super(0, true, 0, 0, 0, path);
        }
    }

    private static void cleanMmDirectory(Path dir, FileSystem fs, String unionSuffix, HashSet<String> committed) throws IOException, HiveException {
        for (FileStatus child : fs.listStatus(dir)) {
            Path childPath = child.getPath();
            if (unionSuffix == null) {
                if (committed.remove(childPath.toString()))
                    continue;
                deleteUncommitedFile(childPath, fs);
            } else if (!child.isDirectory()) {
                if (committed.contains(childPath.toString())) {
                    throw new HiveException("Union FSOP has commited " + childPath + " outside of union directory" + unionSuffix);
                }
                deleteUncommitedFile(childPath, fs);
            } else if (childPath.getName().equals(unionSuffix)) {
                cleanMmDirectory(childPath, fs, null, committed);
            } else {
                Utilities.LOG14535.info("FSOP for " + unionSuffix + " is ignoring the other side of the union " + childPath.getName());
            }
        }
    }

    private static void deleteUncommitedFile(Path childPath, FileSystem fs) throws IOException, HiveException {
        Utilities.LOG14535.info("Deleting " + childPath + " that was not committed");
        if (!fs.delete(childPath, true)) {
            throw new HiveException("Failed to delete an uncommitted path " + childPath);
        }
    }

    public static List<Path> getValidMmDirectoriesFromTableOrPart(Path path, Configuration conf, ValidTxnList validTxnList, int lbLevels) throws IOException {
        Utilities.LOG14535.info("Looking for valid MM paths under " + path);
        List<Path> result = null;
        FileSystem fs = path.getFileSystem(conf);
        FileStatus[] children = (lbLevels == 0) ? fs.listStatus(path) : fs.globStatus(new Path(path, StringUtils.repeat("*" + Path.SEPARATOR, lbLevels) + "*"));
        for (int i = 0; i < children.length; ++i) {
            FileStatus file = children[i];
            Path childPath = file.getPath();
            Long txnId = JavaUtils.extractTxnId(childPath);
            if (!file.isDirectory() || txnId == null || !validTxnList.isTxnValid(txnId)) {
                Utilities.LOG14535.info("Skipping path " + childPath);
                if (result == null) {
                    result = new ArrayList<>(children.length - 1);
                    for (int j = 0; j < i; ++j) {
                        result.add(children[j].getPath());
                    }
                }
            } else if (result != null) {
                result.add(childPath);
            }
        }
        return result;
    }

    public static String getAclStringWithHiveModification(Configuration tezConf, String propertyName, boolean addHs2User, String user, String hs2User) throws IOException {
        ACLConfigurationParser aclConf = new ACLConfigurationParser(tezConf, propertyName);
        aclConf.addAllowedUser(user);
        if (addHs2User && hs2User != null) {
            aclConf.addAllowedUser(hs2User);
        }
        return aclConf.toAclString();
    }
}
