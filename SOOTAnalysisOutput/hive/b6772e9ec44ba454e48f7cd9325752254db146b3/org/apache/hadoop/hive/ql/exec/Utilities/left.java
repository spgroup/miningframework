package org.apache.hadoop.hive.ql.exec;

import static com.google.common.base.Preconditions.checkNotNull;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTransientException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.antlr.runtime.CommonToken;
import org.apache.calcite.util.ChunkList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hive.common.FileUtils;
import org.apache.hadoop.hive.common.HiveInterruptCallback;
import org.apache.hadoop.hive.common.HiveInterruptUtils;
import org.apache.hadoop.hive.common.HiveStatsUtils;
import org.apache.hadoop.hive.common.JavaUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;
import org.apache.hadoop.hive.metastore.Warehouse;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Order;
import org.apache.hadoop.hive.metastore.api.hive_metastoreConstants;
import org.apache.hadoop.hive.ql.Context;
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
import org.apache.hadoop.hive.ql.io.ContentSummaryInputFormat;
import org.apache.hadoop.hive.ql.io.HiveFileFormatUtils;
import org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat;
import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.hive.ql.io.HiveOutputFormat;
import org.apache.hadoop.hive.ql.io.HiveSequenceFileOutputFormat;
import org.apache.hadoop.hive.ql.io.OneNullRowInputFormat;
import org.apache.hadoop.hive.ql.io.RCFile;
import org.apache.hadoop.hive.ql.io.ReworkMapredInputFormat;
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
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.plan.AbstractOperatorDesc;
import org.apache.hadoop.hive.ql.plan.BaseWork;
import org.apache.hadoop.hive.ql.plan.DynamicPartitionCtx;
import org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc;
import org.apache.hadoop.hive.ql.plan.FileSinkDesc;
import org.apache.hadoop.hive.ql.plan.GroupByDesc;
import org.apache.hadoop.hive.ql.plan.MapWork;
import org.apache.hadoop.hive.ql.plan.MapredWork;
import org.apache.hadoop.hive.ql.plan.MergeJoinWork;
import org.apache.hadoop.hive.ql.plan.OperatorDesc;
import org.apache.hadoop.hive.ql.plan.PartitionDesc;
import org.apache.hadoop.hive.ql.plan.PlanUtils;
import org.apache.hadoop.hive.ql.plan.PlanUtils.ExpressionTypes;
import org.apache.hadoop.hive.ql.plan.ReduceWork;
import org.apache.hadoop.hive.ql.plan.SparkEdgeProperty;
import org.apache.hadoop.hive.ql.plan.SparkWork;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.ql.plan.api.Adjacency;
import org.apache.hadoop.hive.ql.plan.api.Graph;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.stats.StatsFactory;
import org.apache.hadoop.hive.ql.stats.StatsPublisher;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.Serializer;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
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
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.Shell;
import org.apache.hive.common.util.ReflectionUtil;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy;
import com.google.common.base.Preconditions;

@SuppressWarnings("nls")
public final class Utilities {

    public static String HADOOP_LOCAL_FS = "file:///";

    public static String MAP_PLAN_NAME = "map.xml";

    public static String REDUCE_PLAN_NAME = "reduce.xml";

    public static String MERGE_PLAN_NAME = "merge.xml";

    public static final String INPUT_NAME = "iocontext.input.name";

    public static final String MAPRED_MAPPER_CLASS = "mapred.mapper.class";

    public static final String MAPRED_REDUCER_CLASS = "mapred.reducer.class";

    public static final String HIVE_ADDED_JARS = "hive.added.jars";

    public static String MAPNAME = "Map ";

    public static String REDUCENAME = "Reducer ";

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

    private static final Log LOG = LogFactory.getLog(CLASS_NAME);

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
        return (MapWork) getBaseWork(conf, MAP_PLAN_NAME);
    }

    public static void setReduceWork(Configuration conf, ReduceWork work) {
        setBaseWork(conf, REDUCE_PLAN_NAME, work);
    }

    public static ReduceWork getReduceWork(Configuration conf) {
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

    public static BaseWork getMergeWork(JobConf jconf) {
        if ((jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX) == null) || (jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX).isEmpty())) {
            return null;
        }
        return getMergeWork(jconf, jconf.get(DagUtils.TEZ_MERGE_CURRENT_MERGE_FILE_PREFIX));
    }

    public static BaseWork getMergeWork(JobConf jconf, String prefix) {
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
        gWorkMap.get(conf).put(path, work);
    }

    private static BaseWork getBaseWork(Configuration conf, String name) {
        Path path = null;
        InputStream in = null;
        try {
            String engine = HiveConf.getVar(conf, ConfVars.HIVE_EXECUTION_ENGINE);
            if (engine.equals("spark")) {
                String addedJars = conf.get(HIVE_ADDED_JARS);
                if (addedJars != null && !addedJars.isEmpty()) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    ClassLoader newLoader = addToClassPath(loader, addedJars.split(";"));
                    Thread.currentThread().setContextClassLoader(newLoader);
                    runtimeSerializationKryo.get().setClassLoader(newLoader);
                }
            }
            path = getPlanPath(conf, name);
            LOG.info("PLAN PATH = " + path);
            assert path != null;
            BaseWork gWork = gWorkMap.get(conf).get(path);
            if (gWork == null) {
                Path localPath;
                if (conf.getBoolean("mapreduce.task.uberized", false) && name.equals(REDUCE_PLAN_NAME)) {
                    localPath = new Path(name);
                } else if (ShimLoader.getHadoopShims().isLocalMode(conf)) {
                    localPath = path;
                } else {
                    LOG.info("***************non-local mode***************");
                    localPath = new Path(name);
                }
                localPath = path;
                LOG.info("local path = " + localPath);
                if (HiveConf.getBoolVar(conf, ConfVars.HIVE_RPC_QUERY_PLAN)) {
                    LOG.debug("Loading plan from string: " + path.toUri().getPath());
                    String planString = conf.getRaw(path.toUri().getPath());
                    if (planString == null) {
                        LOG.info("Could not find plan string in conf");
                        return null;
                    }
                    byte[] planBytes = Base64.decodeBase64(planString);
                    in = new ByteArrayInputStream(planBytes);
                    in = new InflaterInputStream(in);
                } else {
                    LOG.info("Open file to read in plan: " + localPath);
                    in = localPath.getFileSystem(conf).open(localPath);
                }
                if (MAP_PLAN_NAME.equals(name)) {
                    if (ExecMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = deserializePlan(in, MapWork.class, conf);
                    } else if (MergeFileMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = deserializePlan(in, MergeFileWork.class, conf);
                    } else if (ColumnTruncateMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = deserializePlan(in, ColumnTruncateWork.class, conf);
                    } else if (PartialScanMapper.class.getName().equals(conf.get(MAPRED_MAPPER_CLASS))) {
                        gWork = deserializePlan(in, PartialScanWork.class, conf);
                    } else {
                        throw new RuntimeException("unable to determine work from configuration ." + MAPRED_MAPPER_CLASS + " was " + conf.get(MAPRED_MAPPER_CLASS));
                    }
                } else if (REDUCE_PLAN_NAME.equals(name)) {
                    if (ExecReducer.class.getName().equals(conf.get(MAPRED_REDUCER_CLASS))) {
                        gWork = deserializePlan(in, ReduceWork.class, conf);
                    } else {
                        throw new RuntimeException("unable to determine work from configuration ." + MAPRED_REDUCER_CLASS + " was " + conf.get(MAPRED_REDUCER_CLASS));
                    }
                } else if (name.contains(MERGE_PLAN_NAME)) {
                    if (name.startsWith(MAPNAME)) {
                        gWork = deserializePlan(in, MapWork.class, conf);
                    } else if (name.startsWith(REDUCENAME)) {
                        gWork = deserializePlan(in, ReduceWork.class, conf);
                    } else {
                        throw new RuntimeException("Unknown work type: " + name);
                    }
                }
                gWorkMap.get(conf).put(path, gWork);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Found plan in cache for name: " + name);
            }
            return gWork;
        } catch (FileNotFoundException fnf) {
            LOG.debug("File not found: " + fnf.getMessage());
            LOG.info("No plan file found: " + path);
            return null;
        } catch (Exception e) {
            String msg = "Failed to load plan: " + path + ": " + e;
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException cantBlameMeForTrying) {
                }
            }
        }
    }

    public static Map<Integer, String> getMapWorkVectorScratchColumnTypeMap(Configuration hiveConf) {
        MapWork mapWork = getMapWork(hiveConf);
        return mapWork.getVectorScratchColumnTypeMap();
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

    public static class EnumDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(Enum.class, "valueOf", new Object[] { oldInstance.getClass(), ((Enum<?>) oldInstance).name() });
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return oldInstance == newInstance;
        }
    }

    public static class MapDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Map oldMap = (Map) oldInstance;
            HashMap newMap = new HashMap(oldMap);
            return new Expression(newMap, HashMap.class, "new", new Object[] {});
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return false;
        }

        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
            java.util.Collection oldO = (java.util.Collection) oldInstance;
            java.util.Collection newO = (java.util.Collection) newInstance;
            if (newO.size() != 0) {
                out.writeStatement(new Statement(oldInstance, "clear", new Object[] {}));
            }
            for (Iterator i = oldO.iterator(); i.hasNext(); ) {
                out.writeStatement(new Statement(oldInstance, "add", new Object[] { i.next() }));
            }
        }
    }

    public static class SetDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Set oldSet = (Set) oldInstance;
            HashSet newSet = new HashSet(oldSet);
            return new Expression(newSet, HashSet.class, "new", new Object[] {});
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return false;
        }

        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
            java.util.Collection oldO = (java.util.Collection) oldInstance;
            java.util.Collection newO = (java.util.Collection) newInstance;
            if (newO.size() != 0) {
                out.writeStatement(new Statement(oldInstance, "clear", new Object[] {}));
            }
            for (Iterator i = oldO.iterator(); i.hasNext(); ) {
                out.writeStatement(new Statement(oldInstance, "add", new Object[] { i.next() }));
            }
        }
    }

    public static class ListDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            List oldList = (List) oldInstance;
            ArrayList newList = new ArrayList(oldList);
            return new Expression(newList, ArrayList.class, "new", new Object[] {});
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            return false;
        }

        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
            java.util.Collection oldO = (java.util.Collection) oldInstance;
            java.util.Collection newO = (java.util.Collection) newInstance;
            if (newO.size() != 0) {
                out.writeStatement(new Statement(oldInstance, "clear", new Object[] {}));
            }
            for (Iterator i = oldO.iterator(); i.hasNext(); ) {
                out.writeStatement(new Statement(oldInstance, "add", new Object[] { i.next() }));
            }
        }
    }

    public static class DatePersistenceDelegate extends PersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Date dateVal = (Date) oldInstance;
            Object[] args = { dateVal.getTime() };
            return new Expression(dateVal, dateVal.getClass(), "new", args);
        }

        @Override
        protected boolean mutatesTo(Object oldInstance, Object newInstance) {
            if (oldInstance == null || newInstance == null) {
                return false;
            }
            return oldInstance.getClass() == newInstance.getClass();
        }
    }

    public static class TimestampPersistenceDelegate extends DatePersistenceDelegate {

        @Override
        protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
            Timestamp ts = (Timestamp) oldInstance;
            Object[] args = { ts.getNanos() };
            Statement stmt = new Statement(oldInstance, "setNanos", args);
            out.writeStatement(stmt);
        }
    }

    public static class CommonTokenDelegate extends PersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            CommonToken ct = (CommonToken) oldInstance;
            Object[] args = { ct.getType(), ct.getText() };
            return new Expression(ct, ct.getClass(), "new", args);
        }
    }

    public static class PathDelegate extends PersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Path p = (Path) oldInstance;
            Object[] args = { p.toString() };
            return new Expression(p, p.getClass(), "new", args);
        }
    }

    public static void setMapRedWork(Configuration conf, MapredWork w, Path hiveScratchDir) {
        String useName = conf.get(INPUT_NAME);
        if (useName == null) {
            useName = "mapreduce";
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
        try {
            setPlanPath(conf, hiveScratchDir);
            Path planPath = getPlanPath(conf, name);
            OutputStream out = null;
            if (HiveConf.getBoolVar(conf, ConfVars.HIVE_RPC_QUERY_PLAN)) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                try {
                    out = new DeflaterOutputStream(byteOut, new Deflater(Deflater.BEST_SPEED));
                    serializePlan(w, out, conf);
                    out.close();
                    out = null;
                } finally {
                    IOUtils.closeStream(out);
                }
                LOG.info("Setting plan: " + planPath.toUri().getPath());
                conf.set(planPath.toUri().getPath(), Base64.encodeBase64String(byteOut.toByteArray()));
            } else {
                FileSystem fs = planPath.getFileSystem(conf);
                try {
                    out = fs.create(planPath);
                    serializePlan(w, out, conf);
                    out.close();
                    out = null;
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
            gWorkMap.get(conf).put(planPath, w);
            return planPath;
        } catch (Exception e) {
            String msg = "Error caching " + name + ": " + e;
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
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

    public static byte[] serializeExpressionToKryo(ExprNodeGenericFuncDesc expr) {
        return serializeObjectToKryo(expr);
    }

    public static ExprNodeGenericFuncDesc deserializeExpressionFromKryo(byte[] bytes) {
        return deserializeObjectFromKryo(bytes, ExprNodeGenericFuncDesc.class);
    }

    public static String serializeExpression(ExprNodeGenericFuncDesc expr) {
        try {
            return new String(Base64.encodeBase64(serializeExpressionToKryo(expr)), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 support required", ex);
        }
    }

    public static ExprNodeGenericFuncDesc deserializeExpression(String s) {
        byte[] bytes;
        try {
            bytes = Base64.decodeBase64(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 support required", ex);
        }
        return deserializeExpressionFromKryo(bytes);
    }

    private static byte[] serializeObjectToKryo(Serializable object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        runtimeSerializationKryo.get().writeObject(output, object);
        output.close();
        return baos.toByteArray();
    }

    private static <T extends Serializable> T deserializeObjectFromKryo(byte[] bytes, Class<T> clazz) {
        Input inp = new Input(new ByteArrayInputStream(bytes));
        T func = runtimeSerializationKryo.get().readObject(inp, clazz);
        inp.close();
        return func;
    }

    public static String serializeObject(Serializable expr) {
        try {
            return new String(Base64.encodeBase64(serializeObjectToKryo(expr)), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 support required", ex);
        }
    }

    public static <T extends Serializable> T deserializeObject(String s, Class<T> clazz) {
        try {
            return deserializeObjectFromKryo(Base64.decodeBase64(s.getBytes("UTF-8")), clazz);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 support required", ex);
        }
    }

    public static class CollectionPersistenceDelegate extends DefaultPersistenceDelegate {

        @Override
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, oldInstance.getClass(), "new", null);
        }

        @Override
        protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
            Iterator ite = ((Collection) oldInstance).iterator();
            while (ite.hasNext()) {
                out.writeStatement(new Statement(oldInstance, "add", new Object[] { ite.next() }));
            }
        }
    }

    private static class TimestampSerializer extends com.esotericsoftware.kryo.Serializer<Timestamp> {

        @Override
        public Timestamp read(Kryo kryo, Input input, Class<Timestamp> clazz) {
            Timestamp ts = new Timestamp(input.readLong());
            ts.setNanos(input.readInt());
            return ts;
        }

        @Override
        public void write(Kryo kryo, Output output, Timestamp ts) {
            output.writeLong(ts.getTime());
            output.writeInt(ts.getNanos());
        }
    }

    private static class SqlDateSerializer extends com.esotericsoftware.kryo.Serializer<java.sql.Date> {

        @Override
        public java.sql.Date read(Kryo kryo, Input input, Class<java.sql.Date> clazz) {
            return new java.sql.Date(input.readLong());
        }

        @Override
        public void write(Kryo kryo, Output output, java.sql.Date sqlDate) {
            output.writeLong(sqlDate.getTime());
        }
    }

    private static class CommonTokenSerializer extends com.esotericsoftware.kryo.Serializer<CommonToken> {

        @Override
        public CommonToken read(Kryo kryo, Input input, Class<CommonToken> clazz) {
            return new CommonToken(input.readInt(), input.readString());
        }

        @Override
        public void write(Kryo kryo, Output output, CommonToken token) {
            output.writeInt(token.getType());
            output.writeString(token.getText());
        }
    }

    private static class PathSerializer extends com.esotericsoftware.kryo.Serializer<Path> {

        @Override
        public void write(Kryo kryo, Output output, Path path) {
            output.writeString(path.toUri().toString());
        }

        @Override
        public Path read(Kryo kryo, Input input, Class<Path> type) {
            return new Path(URI.create(input.readString()));
        }
    }

    public static List<Operator<?>> cloneOperatorTree(Configuration conf, List<Operator<?>> roots) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        serializePlan(roots, baos, conf, true);
        @SuppressWarnings("unchecked")
        List<Operator<?>> result = deserializePlan(new ByteArrayInputStream(baos.toByteArray()), roots.getClass(), conf, true);
        return result;
    }

    private static void serializePlan(Object plan, OutputStream out, Configuration conf, boolean cloningPlan) {
        PerfLogger perfLogger = PerfLogger.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.SERIALIZE_PLAN);
        String serializationType = conf.get(HiveConf.ConfVars.PLAN_SERIALIZATION.varname, "kryo");
        LOG.info("Serializing " + plan.getClass().getSimpleName() + " via " + serializationType);
        if ("javaXML".equalsIgnoreCase(serializationType)) {
            serializeObjectByJavaXML(plan, out);
        } else {
            if (cloningPlan) {
                serializeObjectByKryo(cloningQueryPlanKryo.get(), plan, out);
            } else {
                serializeObjectByKryo(runtimeSerializationKryo.get(), plan, out);
            }
        }
        perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.SERIALIZE_PLAN);
    }

    public static void serializePlan(Object plan, OutputStream out, Configuration conf) {
        serializePlan(plan, out, conf, false);
    }

    private static <T> T deserializePlan(InputStream in, Class<T> planClass, Configuration conf, boolean cloningPlan) {
        PerfLogger perfLogger = PerfLogger.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.DESERIALIZE_PLAN);
        T plan;
        String serializationType = conf.get(HiveConf.ConfVars.PLAN_SERIALIZATION.varname, "kryo");
        LOG.info("Deserializing " + planClass.getSimpleName() + " via " + serializationType);
        if ("javaXML".equalsIgnoreCase(serializationType)) {
            plan = deserializeObjectByJavaXML(in);
        } else {
            if (cloningPlan) {
                plan = deserializeObjectByKryo(cloningQueryPlanKryo.get(), in, planClass);
            } else {
                plan = deserializeObjectByKryo(runtimeSerializationKryo.get(), in, planClass);
            }
        }
        perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.DESERIALIZE_PLAN);
        return plan;
    }

    public static <T> T deserializePlan(InputStream in, Class<T> planClass, Configuration conf) {
        return deserializePlan(in, planClass, conf, false);
    }

    public static MapredWork clonePlan(MapredWork plan) {
        PerfLogger perfLogger = PerfLogger.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.CLONE_PLAN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        Configuration conf = new HiveConf();
        serializePlan(plan, baos, conf, true);
        MapredWork newPlan = deserializePlan(new ByteArrayInputStream(baos.toByteArray()), MapredWork.class, conf, true);
        perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.CLONE_PLAN);
        return newPlan;
    }

    public static BaseWork cloneBaseWork(BaseWork plan) {
        PerfLogger perfLogger = PerfLogger.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.CLONE_PLAN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        Configuration conf = new HiveConf();
        serializePlan(plan, baos, conf, true);
        BaseWork newPlan = deserializePlan(new ByteArrayInputStream(baos.toByteArray()), plan.getClass(), conf, true);
        perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.CLONE_PLAN);
        return newPlan;
    }

    private static void serializeObjectByJavaXML(Object plan, OutputStream out) {
        XMLEncoder e = new XMLEncoder(out);
        e.setExceptionListener(new ExceptionListener() {

            @Override
            public void exceptionThrown(Exception e) {
                LOG.warn(org.apache.hadoop.util.StringUtils.stringifyException(e));
                throw new RuntimeException("Cannot serialize object", e);
            }
        });
        e.setPersistenceDelegate(ExpressionTypes.class, new EnumDelegate());
        e.setPersistenceDelegate(GroupByDesc.Mode.class, new EnumDelegate());
        e.setPersistenceDelegate(java.sql.Date.class, new DatePersistenceDelegate());
        e.setPersistenceDelegate(Timestamp.class, new TimestampPersistenceDelegate());
        e.setPersistenceDelegate(org.datanucleus.store.types.backed.Map.class, new MapDelegate());
        e.setPersistenceDelegate(org.datanucleus.store.types.backed.List.class, new ListDelegate());
        e.setPersistenceDelegate(CommonToken.class, new CommonTokenDelegate());
        e.setPersistenceDelegate(Path.class, new PathDelegate());
        e.writeObject(plan);
        e.close();
    }

    private static void serializeObjectByKryo(Kryo kryo, Object plan, OutputStream out) {
        Output output = new Output(out);
        kryo.writeObject(output, plan);
        output.close();
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeObjectByJavaXML(InputStream in) {
        XMLDecoder d = null;
        try {
            d = new XMLDecoder(in, null, null);
            return (T) d.readObject();
        } finally {
            if (null != d) {
                d.close();
            }
        }
    }

    private static <T> T deserializeObjectByKryo(Kryo kryo, InputStream in, Class<T> clazz) {
        Input inp = new Input(in);
        T t = kryo.readObject(inp, clazz);
        inp.close();
        return t;
    }

    public static ThreadLocal<Kryo> runtimeSerializationKryo = new ThreadLocal<Kryo>() {

        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.register(java.sql.Date.class, new SqlDateSerializer());
            kryo.register(java.sql.Timestamp.class, new TimestampSerializer());
            kryo.register(Path.class, new PathSerializer());
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            removeField(kryo, Operator.class, "colExprMap");
            removeField(kryo, ColumnInfo.class, "objectInspector");
            removeField(kryo, AbstractOperatorDesc.class, "statistics");
            return kryo;
        }
    };

    @SuppressWarnings("rawtypes")
    protected static void removeField(Kryo kryo, Class type, String fieldName) {
        FieldSerializer fld = new FieldSerializer(kryo, type);
        fld.removeField(fieldName);
        kryo.register(type, fld);
    }

    public static ThreadLocal<Kryo> sparkSerializationKryo = new ThreadLocal<Kryo>() {

        @Override
        protected synchronized Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.register(java.sql.Date.class, new SqlDateSerializer());
            kryo.register(java.sql.Timestamp.class, new TimestampSerializer());
            kryo.register(Path.class, new PathSerializer());
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            removeField(kryo, Operator.class, "colExprMap");
            removeField(kryo, ColumnInfo.class, "objectInspector");
            kryo.register(SparkEdgeProperty.class);
            kryo.register(MapWork.class);
            kryo.register(ReduceWork.class);
            kryo.register(SparkWork.class);
            kryo.register(TableDesc.class);
            kryo.register(Pair.class);
            return kryo;
        }
    };

    private static ThreadLocal<Kryo> cloningQueryPlanKryo = new ThreadLocal<Kryo>() {

        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.register(CommonToken.class, new CommonTokenSerializer());
            kryo.register(java.sql.Date.class, new SqlDateSerializer());
            kryo.register(java.sql.Timestamp.class, new TimestampSerializer());
            kryo.register(Path.class, new PathSerializer());
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    public static TableDesc defaultTd;

    static {
        defaultTd = PlanUtils.getDefaultTableDesc("" + Utilities.ctrlaCode);
    }

    public static final int carriageReturnCode = 13;

    public static final int newLineCode = 10;

    public static final int tabCode = 9;

    public static final int ctrlaCode = 1;

    public static final String INDENT = "  ";

    public static String nullStringStorage = "\\N";

    public static String nullStringOutput = "NULL";

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
        return (new PartitionDesc(part));
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
            if (Shell.WINDOWS) {
                if (foundCrChar && b != Utilities.newLineCode) {
                    out.write(Utilities.carriageReturnCode);
                    foundCrChar = false;
                }
                if (b == Utilities.carriageReturnCode) {
                    foundCrChar = true;
                    continue;
                }
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

    public static void renameOrMoveFiles(FileSystem fs, Path src, Path dst) throws IOException, HiveException {
        if (!fs.exists(dst)) {
            if (!fs.rename(src, dst)) {
                throw new HiveException("Unable to move: " + src + " to: " + dst);
            }
        } else {
            FileStatus[] files = fs.listStatus(src);
            for (FileStatus file : files) {
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
                        throw new HiveException("Unable to move: " + src + " to: " + dst);
                    }
                }
            }
        }
    }

    private static final Pattern FILE_NAME_TO_TASK_ID_REGEX = Pattern.compile("^.*?([0-9]+)(_[0-9]{1,6})?(\\..*)?$");

    private static final String COPY_KEYWORD = "_copy_";

    private static final Pattern COPY_FILE_NAME_TO_TASK_ID_REGEX = Pattern.compile("^.*?" + "([0-9]+)" + "(_)" + "([0-9]{1,6})?" + "((_)(\\Bcopy\\B)(_)" + "([0-9]{1,6})$)?" + "(\\..*)?$");

    private static final Pattern FILE_NAME_PREFIXED_TASK_ID_REGEX = Pattern.compile("^.*?((\\(.*\\))?[0-9]+)(_[0-9]{1,6})?(\\..*)?$");

    private static final Pattern PREFIXED_TASK_ID_REGEX = Pattern.compile("^(.*?\\(.*\\))?([0-9]+)$");

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

    public static void mvFileToFinalPath(Path specPath, Configuration hconf, boolean success, Log log, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Reporter reporter) throws IOException, HiveException {
        FileSystem fs = specPath.getFileSystem(hconf);
        Path tmpPath = Utilities.toTempPath(specPath);
        Path taskTmpPath = Utilities.toTaskTempPath(specPath);
        if (success) {
            if (fs.exists(tmpPath)) {
                ArrayList<String> emptyBuckets = Utilities.removeTempOrDuplicateFiles(fs, tmpPath, dpCtx, conf, hconf);
                if (emptyBuckets.size() > 0) {
                    createEmptyBuckets(hconf, emptyBuckets, conf, reporter);
                }
                log.info("Moving tmp dir: " + tmpPath + " to: " + specPath);
                Utilities.renameOrMoveFiles(fs, tmpPath, specPath);
            }
        } else {
            fs.delete(tmpPath, true);
        }
        fs.delete(taskTmpPath, true);
    }

    private static void createEmptyBuckets(Configuration hconf, ArrayList<String> paths, FileSinkDesc conf, Reporter reporter) throws HiveException, IOException {
        JobConf jc;
        if (hconf instanceof JobConf) {
            jc = new JobConf(hconf);
        } else {
            jc = new JobConf(hconf);
        }
        HiveOutputFormat<?, ?> hiveOutputFormat = null;
        Class<? extends Writable> outputClass = null;
        boolean isCompressed = conf.getCompressed();
        TableDesc tableInfo = conf.getTableInfo();
        try {
            Serializer serializer = (Serializer) tableInfo.getDeserializerClass().newInstance();
            serializer.initialize(null, tableInfo.getProperties());
            outputClass = serializer.getSerializedClass();
            hiveOutputFormat = HiveFileFormatUtils.getHiveOutputFormat(hconf, conf.getTableInfo());
        } catch (SerDeException e) {
            throw new HiveException(e);
        } catch (InstantiationException e) {
            throw new HiveException(e);
        } catch (IllegalAccessException e) {
            throw new HiveException(e);
        }
        for (String p : paths) {
            Path path = new Path(p);
            RecordWriter writer = HiveFileFormatUtils.getRecordWriter(jc, hiveOutputFormat, outputClass, isCompressed, tableInfo.getProperties(), path, reporter);
            writer.close(false);
            LOG.info("created empty bucket for enforcing bucketing at " + path);
        }
    }

    public static void removeTempOrDuplicateFiles(FileSystem fs, Path path) throws IOException {
        removeTempOrDuplicateFiles(fs, path, null, null, null);
    }

    public static ArrayList<String> removeTempOrDuplicateFiles(FileSystem fs, Path path, DynamicPartitionCtx dpCtx, FileSinkDesc conf, Configuration hconf) throws IOException {
        if (path == null) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>();
        HashMap<String, FileStatus> taskIDToFile = null;
        if (dpCtx != null) {
            FileStatus[] parts = HiveStatsUtils.getFileStatusRecurse(path, dpCtx.getNumDPCols(), fs);
            for (int i = 0; i < parts.length; ++i) {
                assert parts[i].isDir() : "dynamic partition " + parts[i].getPath() + " is not a directory";
                FileStatus[] items = fs.listStatus(parts[i].getPath());
                if (items.length == 0) {
                    if (!fs.delete(parts[i].getPath(), true)) {
                        LOG.error("Cannot delete empty directory " + parts[i].getPath());
                        throw new IOException("Cannot delete empty directory " + parts[i].getPath());
                    }
                }
                taskIDToFile = removeTempOrDuplicateFiles(items, fs);
                if (dpCtx.getNumBuckets() > 0 && taskIDToFile != null) {
                    items = fs.listStatus(parts[i].getPath());
                    String taskID1 = taskIDToFile.keySet().iterator().next();
                    Path bucketPath = taskIDToFile.values().iterator().next().getPath();
                    for (int j = 0; j < dpCtx.getNumBuckets(); ++j) {
                        String taskID2 = replaceTaskId(taskID1, j);
                        if (!taskIDToFile.containsKey(taskID2)) {
                            String path2 = replaceTaskIdFromFilename(bucketPath.toUri().getPath().toString(), j);
                            result.add(path2);
                        }
                    }
                }
            }
        } else {
            FileStatus[] items = fs.listStatus(path);
            taskIDToFile = removeTempOrDuplicateFiles(items, fs);
            if (taskIDToFile != null && taskIDToFile.size() > 0 && conf != null && conf.getTable() != null && (conf.getTable().getNumBuckets() > taskIDToFile.size()) && (HiveConf.getBoolVar(hconf, HiveConf.ConfVars.HIVEENFORCEBUCKETING))) {
                String taskID1 = taskIDToFile.keySet().iterator().next();
                Path bucketPath = taskIDToFile.values().iterator().next().getPath();
                for (int j = 0; j < conf.getTable().getNumBuckets(); ++j) {
                    String taskID2 = replaceTaskId(taskID1, j);
                    if (!taskIDToFile.containsKey(taskID2)) {
                        String path2 = replaceTaskIdFromFilename(bucketPath.toUri().getPath().toString(), j);
                        result.add(path2);
                    }
                }
            }
        }
        return result;
    }

    public static HashMap<String, FileStatus> removeTempOrDuplicateFiles(FileStatus[] items, FileSystem fs) throws IOException {
        if (items == null || fs == null) {
            return null;
        }
        HashMap<String, FileStatus> taskIdToFile = new HashMap<String, FileStatus>();
        for (FileStatus one : items) {
            if (isTempPath(one)) {
                if (!fs.delete(one.getPath(), true)) {
                    throw new IOException("Unable to delete tmp file: " + one.getPath());
                }
            } else {
                String taskId = getPrefixedTaskIdFromFilename(one.getPath().getName());
                FileStatus otherFile = taskIdToFile.get(taskId);
                if (otherFile == null) {
                    taskIdToFile.put(taskId, one);
                } else {
                    FileStatus toDelete = null;
                    if (!isCopyFile(one.getPath().getName())) {
                        if (otherFile.getLen() >= one.getLen()) {
                            toDelete = one;
                        } else {
                            toDelete = otherFile;
                            taskIdToFile.put(taskId, one);
                        }
                        long len1 = toDelete.getLen();
                        long len2 = taskIdToFile.get(taskId).getLen();
                        if (!fs.delete(toDelete.getPath(), true)) {
                            throw new IOException("Unable to delete duplicate file: " + toDelete.getPath() + ". Existing file: " + taskIdToFile.get(taskId).getPath());
                        } else {
                            LOG.warn("Duplicate taskid file removed: " + toDelete.getPath() + " with length " + len1 + ". Existing file: " + taskIdToFile.get(taskId).getPath() + " with length " + len2);
                        }
                    } else {
                        LOG.info(one.getPath() + " file identified as duplicate. This file is" + " not deleted as it has copySuffix.");
                    }
                }
            }
        }
        return taskIdToFile;
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

    public static String getNameMessage(Exception e) {
        return e.getClass().getName() + "(" + e.getMessage() + ")";
    }

    public static String getResourceFiles(Configuration conf, SessionState.ResourceType t) {
        SessionState ss = SessionState.get();
        Set<String> files = (ss == null) ? null : ss.list_resource(t, null);
        if (files != null) {
            List<String> realFiles = new ArrayList<String>(files.size());
            for (String one : files) {
                try {
                    realFiles.add(realFile(one, conf));
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

    public static Set<String> getJarFilesByPath(String path) {
        Set<String> result = new HashSet<String>();
        if (path == null || path.isEmpty()) {
            return result;
        }
        File paths = new File(path);
        if (paths.exists() && paths.isDirectory()) {
            Set<File> jarFiles = new HashSet<File>();
            jarFiles.addAll(org.apache.commons.io.FileUtils.listFiles(paths, new String[] { "jar" }, true));
            for (File f : jarFiles) {
                result.add(f.getAbsolutePath());
            }
        } else {
            String[] files = path.split(",");
            Collections.addAll(result, files);
        }
        return result;
    }

    public static ClassLoader addToClassPath(ClassLoader cloader, String[] newPaths) throws Exception {
        URLClassLoader loader = (URLClassLoader) cloader;
        List<URL> curPath = Arrays.asList(loader.getURLs());
        ArrayList<URL> newPath = new ArrayList<URL>();
        for (URL onePath : curPath) {
            newPath.add(onePath);
        }
        curPath = newPath;
        for (String onestr : newPaths) {
            URL oneurl = urlFromPathString(onestr);
            if (oneurl != null && !curPath.contains(oneurl)) {
                curPath.add(oneurl);
            }
        }
        return new URLClassLoader(curPath.toArray(new URL[0]), loader);
    }

    public static void removeFromClassPath(String[] pathsToRemove) throws Exception {
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
        loader = new URLClassLoader(newPath.toArray(new URL[0]));
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

    public static void copyTableJobPropertiesToConf(TableDesc tbl, Configuration job) {
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
        if (jobProperties == null) {
            return;
        }
        for (Map.Entry<String, String> entry : jobProperties.entrySet()) {
            job.set(entry.getKey(), entry.getValue());
        }
    }

    public static void copyTablePropertiesToConf(TableDesc tbl, JobConf job) {
        Properties tblProperties = tbl.getProperties();
        for (String name : tblProperties.stringPropertyNames()) {
            String val = (String) tblProperties.get(name);
            if (val != null) {
                job.set(name, StringEscapeUtils.escapeJava(val));
            }
        }
        Map<String, String> jobProperties = tbl.getJobProperties();
        if (jobProperties == null) {
            return;
        }
        for (Map.Entry<String, String> entry : jobProperties.entrySet()) {
            job.set(entry.getKey(), entry.getValue());
        }
    }

    private static final Object INPUT_SUMMARY_LOCK = new Object();

    public static ContentSummary getInputSummary(final Context ctx, MapWork work, PathFilter filter) throws IOException {
        PerfLogger perfLogger = PerfLogger.getPerfLogger();
        perfLogger.PerfLogBegin(CLASS_NAME, PerfLogger.INPUT_SUMMARY);
        long[] summary = { 0, 0, 0 };
        final List<String> pathNeedProcess = new ArrayList<String>();
        synchronized (INPUT_SUMMARY_LOCK) {
            for (String path : work.getPathToAliases().keySet()) {
                Path p = new Path(path);
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
            ArrayList<Future<?>> results = new ArrayList<Future<?>>();
            final ThreadPoolExecutor executor;
            int maxThreads = ctx.getConf().getInt("mapred.dfsclient.parallelism.max", 0);
            if (pathNeedProcess.size() > 1 && maxThreads > 1) {
                int numExecutors = Math.min(pathNeedProcess.size(), maxThreads);
                LOG.info("Using " + numExecutors + " threads for getContentSummary");
                executor = new ThreadPoolExecutor(numExecutors, numExecutors, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            } else {
                executor = null;
            }
            HiveInterruptCallback interrup = HiveInterruptUtils.add(new HiveInterruptCallback() {

                @Override
                public void interrupt() {
                    for (String path : pathNeedProcess) {
                        try {
                            new Path(path).getFileSystem(ctx.getConf()).close();
                        } catch (IOException ignore) {
                            LOG.debug(ignore);
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
                for (String path : pathNeedProcess) {
                    final Path p = new Path(path);
                    final String pathStr = path;
                    final Configuration myConf = conf;
                    final JobConf myJobConf = jobConf;
                    final Map<String, Operator<?>> aliasToWork = work.getAliasToWork();
                    final Map<String, ArrayList<String>> pathToAlias = work.getPathToAliases();
                    final PartitionDesc partDesc = work.getPathToPartitionInfo().get(p.toString());
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
                                HiveStorageHandler handler = HiveUtils.getStorageHandler(myConf, SerDeUtils.createOverlayedProperties(partDesc.getTableDesc().getProperties(), partDesc.getProperties()).getProperty(hive_metastoreConstants.META_TABLE_STORAGE));
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
                                        total += estimator.estimate(myJobConf, scanOp, -1).getTotalLength();
                                    }
                                    resultMap.put(pathStr, new ContentSummary(total, -1, -1));
                                }
                                FileSystem fs = p.getFileSystem(myConf);
                                resultMap.put(pathStr, fs.getContentSummary(p));
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
                perfLogger.PerfLogEnd(CLASS_NAME, PerfLogger.INPUT_SUMMARY);
                return new ContentSummary(summary[0], summary[1], summary[2]);
            } finally {
                HiveInterruptUtils.remove(interrup);
            }
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

    public static boolean isEmptyPath(JobConf job, Path dirPath) throws Exception {
        FileSystem inpFs = dirPath.getFileSystem(job);
        if (inpFs.exists(dirPath)) {
            FileStatus[] fStats = inpFs.listStatus(dirPath, FileUtils.HIDDEN_FILES_PATH_FILTER);
            if (fStats.length > 0) {
                return false;
            }
        }
        return true;
    }

    public static List<TezTask> getTezTasks(List<Task<? extends Serializable>> tasks) {
        List<TezTask> tezTasks = new ArrayList<TezTask>();
        if (tasks != null) {
            getTezTasks(tasks, tezTasks);
        }
        return tezTasks;
    }

    private static void getTezTasks(List<Task<? extends Serializable>> tasks, List<TezTask> tezTasks) {
        for (Task<? extends Serializable> task : tasks) {
            if (task instanceof TezTask && !tezTasks.contains(task)) {
                tezTasks.add((TezTask) task);
            }
            if (task.getDependentTasks() != null) {
                getTezTasks(task.getDependentTasks(), tezTasks);
            }
        }
    }

    public static List<SparkTask> getSparkTasks(List<Task<? extends Serializable>> tasks) {
        List<SparkTask> sparkTasks = new ArrayList<SparkTask>();
        if (tasks != null) {
            getSparkTasks(tasks, sparkTasks);
        }
        return sparkTasks;
    }

    private static void getSparkTasks(List<Task<? extends Serializable>> tasks, List<SparkTask> sparkTasks) {
        for (Task<? extends Serializable> task : tasks) {
            if (task instanceof SparkTask && !sparkTasks.contains(task)) {
                sparkTasks.add((SparkTask) task);
            }
            if (task.getDependentTasks() != null) {
                getSparkTasks(task.getDependentTasks(), sparkTasks);
            }
        }
    }

    public static List<ExecDriver> getMRTasks(List<Task<? extends Serializable>> tasks) {
        List<ExecDriver> mrTasks = new ArrayList<ExecDriver>();
        if (tasks != null) {
            getMRTasks(tasks, mrTasks);
        }
        return mrTasks;
    }

    private static void getMRTasks(List<Task<? extends Serializable>> tasks, List<ExecDriver> mrTasks) {
        for (Task<? extends Serializable> task : tasks) {
            if (task instanceof ExecDriver && !mrTasks.contains(task)) {
                mrTasks.add((ExecDriver) task);
            }
            if (task.getDependentTasks() != null) {
                getMRTasks(task.getDependentTasks(), mrTasks);
            }
        }
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
                Warehouse.makeSpecFromName(fullPartSpec, partPath);
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

    public static String getHashedStatsPrefix(String statsPrefix, int maxPrefixLength) {
        if (maxPrefixLength >= 0 && statsPrefix.length() > maxPrefixLength) {
            try {
                MessageDigest digester = MessageDigest.getInstance("MD5");
                digester.update(statsPrefix.getBytes());
                return new String(digester.digest()) + Path.SEPARATOR;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return statsPrefix.endsWith(Path.SEPARATOR) ? statsPrefix : statsPrefix + Path.SEPARATOR;
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

    public static void setColumnNameList(JobConf jobConf, Operator op) {
        setColumnNameList(jobConf, op, false);
    }

    public static void setColumnNameList(JobConf jobConf, Operator op, boolean excludeVCs) {
        RowSchema rowSchema = op.getSchema();
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

    public static void setColumnTypeList(JobConf jobConf, Operator op) {
        setColumnTypeList(jobConf, op, false);
    }

    public static void setColumnTypeList(JobConf jobConf, Operator op, boolean excludeVCs) {
        RowSchema rowSchema = op.getSchema();
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

    public static String suffix = ".hashtable";

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
        List<Path> paths = getInputPaths(job, work, new Path(scratchDir), null, !work.isUseOneNullRowInputFormat());
        return paths;
    }

    public static List<Path> getInputPaths(JobConf job, MapWork work, Path hiveScratchDir, Context ctx, boolean skipDummy) throws Exception {
        int sequenceNumber = 0;
        Set<Path> pathsProcessed = new HashSet<Path>();
        List<Path> pathsToAdd = new LinkedList<Path>();
        for (String alias : work.getAliasToWork().keySet()) {
            LOG.info("Processing alias " + alias);
            Path path = null;
            for (String file : new LinkedList<String>(work.getPathToAliases().keySet())) {
                List<String> aliases = work.getPathToAliases().get(file);
                if (aliases.contains(alias)) {
                    path = new Path(file);
                    if (pathsProcessed.contains(path)) {
                        continue;
                    }
                    pathsProcessed.add(path);
                    LOG.info("Adding input file " + path);
                    if (!skipDummy && isEmptyPath(job, path, ctx)) {
                        path = createDummyFileForEmptyPartition(path, job, work, hiveScratchDir, alias, sequenceNumber++);
                    }
                    pathsToAdd.add(path);
                }
            }
            if (path == null && !skipDummy) {
                path = createDummyFileForEmptyTable(job, work, hiveScratchDir, alias, sequenceNumber++);
                pathsToAdd.add(path);
            }
        }
        return pathsToAdd;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Path createEmptyFile(Path hiveScratchDir, HiveOutputFormat outFileFormat, JobConf job, int sequenceNumber, Properties props, boolean dummyRow) throws IOException, InstantiationException, IllegalAccessException {
        String newDir = hiveScratchDir + Path.SEPARATOR + sequenceNumber;
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
        return newPath;
    }

    @SuppressWarnings("rawtypes")
    private static Path createDummyFileForEmptyPartition(Path path, JobConf job, MapWork work, Path hiveScratchDir, String alias, int sequenceNumber) throws Exception {
        String strPath = path.toString();
        PartitionDesc partDesc = work.getPathToPartitionInfo().get(strPath);
        if (partDesc.getTableDesc().isNonNative()) {
            return path;
        }
        Properties props = SerDeUtils.createOverlayedProperties(partDesc.getTableDesc().getProperties(), partDesc.getProperties());
        HiveOutputFormat outFileFormat = HiveFileFormatUtils.getHiveOutputFormat(job, partDesc);
        boolean oneRow = partDesc.getInputFileFormatClass() == OneNullRowInputFormat.class;
        Path newPath = createEmptyFile(hiveScratchDir, outFileFormat, job, sequenceNumber, props, oneRow);
        if (LOG.isInfoEnabled()) {
            LOG.info("Changed input file " + strPath + " to empty file " + newPath);
        }
        String strNewPath = newPath.toString();
        LinkedHashMap<String, ArrayList<String>> pathToAliases = work.getPathToAliases();
        pathToAliases.put(strNewPath, pathToAliases.get(strPath));
        pathToAliases.remove(strPath);
        work.setPathToAliases(pathToAliases);
        LinkedHashMap<String, PartitionDesc> pathToPartitionInfo = work.getPathToPartitionInfo();
        pathToPartitionInfo.put(strNewPath, pathToPartitionInfo.get(strPath));
        pathToPartitionInfo.remove(strPath);
        work.setPathToPartitionInfo(pathToPartitionInfo);
        return newPath;
    }

    @SuppressWarnings("rawtypes")
    private static Path createDummyFileForEmptyTable(JobConf job, MapWork work, Path hiveScratchDir, String alias, int sequenceNumber) throws Exception {
        TableDesc tableDesc = work.getAliasToPartnInfo().get(alias).getTableDesc();
        if (tableDesc.isNonNative()) {
            return null;
        }
        Properties props = tableDesc.getProperties();
        HiveOutputFormat outFileFormat = HiveFileFormatUtils.getHiveOutputFormat(job, tableDesc);
        Path newPath = createEmptyFile(hiveScratchDir, outFileFormat, job, sequenceNumber, props, false);
        if (LOG.isInfoEnabled()) {
            LOG.info("Changed input file for alias " + alias + " to " + newPath);
        }
        LinkedHashMap<String, ArrayList<String>> pathToAliases = work.getPathToAliases();
        ArrayList<String> newList = new ArrayList<String>();
        newList.add(alias);
        pathToAliases.put(newPath.toUri().toString(), newList);
        work.setPathToAliases(pathToAliases);
        LinkedHashMap<String, PartitionDesc> pathToPartitionInfo = work.getPathToPartitionInfo();
        PartitionDesc pDesc = work.getAliasToPartnInfo().get(alias).clone();
        pathToPartitionInfo.put(newPath.toUri().toString(), pDesc);
        work.setPathToPartitionInfo(pathToPartitionInfo);
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
        Map<String, ArrayList<String>> pa = mWork.getPathToAliases();
        if (pa != null) {
            List<Operator<? extends OperatorDesc>> ops = new ArrayList<Operator<? extends OperatorDesc>>();
            for (List<String> ls : pa.values()) {
                for (String a : ls) {
                    ops.add(mWork.getAliasToWork().get(a));
                }
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

    public static boolean isVectorMode(Configuration conf) {
        if (HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_VECTORIZATION_ENABLED) && Utilities.getPlanPath(conf) != null && Utilities.getMapWork(conf).getVectorMode()) {
            return true;
        }
        return false;
    }

    public static boolean isVectorMode(Configuration conf, MapWork mapWork) {
        return HiveConf.getBoolVar(conf, HiveConf.ConfVars.HIVE_VECTORIZATION_ENABLED) && mapWork.getVectorMode();
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
        throw new IllegalStateException("Failed to create a temp dir under " + baseDir + " Giving up after " + MAX_ATTEMPS + " attemps");
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
        return !conf.getChangedProperties().containsKey(HiveConf.ConfVars.HADOOPFS.varname);
    }

    public static boolean isPerfOrAboveLogging(HiveConf conf) {
        String loggingLevel = conf.getVar(HiveConf.ConfVars.HIVE_SERVER2_LOGGING_OPERATION_LEVEL);
        return conf.getBoolVar(HiveConf.ConfVars.HIVE_SERVER2_LOGGING_OPERATION_ENABLED) && (loggingLevel.equalsIgnoreCase("PERFORMANCE") || loggingLevel.equalsIgnoreCase("VERBOSE"));
    }

    public static void stripHivePasswordDetails(Configuration conf) {
        if (HiveConf.getVar(conf, HiveConf.ConfVars.METASTOREPWD) != null) {
            HiveConf.setVar(conf, HiveConf.ConfVars.METASTOREPWD, "");
        }
        if (HiveConf.getVar(conf, HiveConf.ConfVars.HIVE_SERVER2_SSL_KEYSTORE_PASSWORD) != null) {
            HiveConf.setVar(conf, HiveConf.ConfVars.HIVE_SERVER2_SSL_KEYSTORE_PASSWORD, "");
        }
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
}
