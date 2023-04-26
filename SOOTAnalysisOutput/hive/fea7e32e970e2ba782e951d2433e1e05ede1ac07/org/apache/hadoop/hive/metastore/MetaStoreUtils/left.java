package org.apache.hadoop.hive.metastore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hive.common.HiveStatsUtils;
import org.apache.hadoop.hive.common.JavaUtils;
import org.apache.hadoop.hive.common.StatsSetupConst;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStore.HMSHandler;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.InvalidOperationException;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.hive_metastoreConstants;
import org.apache.hadoop.hive.metastore.partition.spec.PartitionSpecProxy;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.Deserializer;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.hive.thrift.HadoopThriftAuthBridge;
import org.apache.hive.common.util.ReflectionUtil;
import javax.annotation.Nullable;

public class MetaStoreUtils {

    protected static final Log LOG = LogFactory.getLog("hive.log");

    public static final String DEFAULT_DATABASE_NAME = "default";

    public static final String DEFAULT_DATABASE_COMMENT = "Default Hive database";

    public static final String DATABASE_WAREHOUSE_SUFFIX = ".db";

    public static Table createColumnsetSchema(String name, List<String> columns, List<String> partCols, Configuration conf) throws MetaException {
        if (columns == null) {
            throw new MetaException("columns not specified for table " + name);
        }
        Table tTable = new Table();
        tTable.setTableName(name);
        tTable.setSd(new StorageDescriptor());
        StorageDescriptor sd = tTable.getSd();
        sd.setSerdeInfo(new SerDeInfo());
        SerDeInfo serdeInfo = sd.getSerdeInfo();
        serdeInfo.setSerializationLib(LazySimpleSerDe.class.getName());
        serdeInfo.setParameters(new HashMap<String, String>());
        serdeInfo.getParameters().put(org.apache.hadoop.hive.serde.serdeConstants.SERIALIZATION_FORMAT, "1");
        List<FieldSchema> fields = new ArrayList<FieldSchema>();
        sd.setCols(fields);
        for (String col : columns) {
            FieldSchema field = new FieldSchema(col, org.apache.hadoop.hive.serde.serdeConstants.STRING_TYPE_NAME, "'default'");
            fields.add(field);
        }
        tTable.setPartitionKeys(new ArrayList<FieldSchema>());
        for (String partCol : partCols) {
            FieldSchema part = new FieldSchema();
            part.setName(partCol);
            part.setType(org.apache.hadoop.hive.serde.serdeConstants.STRING_TYPE_NAME);
            tTable.getPartitionKeys().add(part);
        }
        sd.setNumBuckets(-1);
        return tTable;
    }

    static public void recursiveDelete(File f) throws IOException {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (File subf : fs) {
                recursiveDelete(subf);
            }
        }
        if (!f.delete()) {
            throw new IOException("could not delete: " + f.getPath());
        }
    }

    public static boolean containsAllFastStats(Map<String, String> partParams) {
        for (String stat : StatsSetupConst.fastStats) {
            if (!partParams.containsKey(stat)) {
                return false;
            }
        }
        return true;
    }

    public static boolean updateUnpartitionedTableStatsFast(Database db, Table tbl, Warehouse wh, boolean madeDir) throws MetaException {
        return updateUnpartitionedTableStatsFast(db, tbl, wh, madeDir, false);
    }

    public static boolean updateUnpartitionedTableStatsFast(Database db, Table tbl, Warehouse wh, boolean madeDir, boolean forceRecompute) throws MetaException {
        return updateUnpartitionedTableStatsFast(tbl, wh.getFileStatusesForUnpartitionedTable(db, tbl), madeDir, forceRecompute);
    }

    public static boolean updateUnpartitionedTableStatsFast(Table tbl, FileStatus[] fileStatus, boolean newDir, boolean forceRecompute) throws MetaException {
        Map<String, String> params = tbl.getParameters();
        boolean updated = false;
        if (forceRecompute || params == null || !containsAllFastStats(params)) {
            if (params == null) {
                params = new HashMap<String, String>();
            }
            if (!newDir) {
                LOG.info("Updating table stats fast for " + tbl.getTableName());
                populateQuickStats(fileStatus, params);
                LOG.info("Updated size of table " + tbl.getTableName() + " to " + params.get(StatsSetupConst.TOTAL_SIZE));
                if (!params.containsKey(StatsSetupConst.STATS_GENERATED_VIA_STATS_TASK)) {
                    for (String stat : StatsSetupConst.statsRequireCompute) {
                        params.put(stat, "-1");
                    }
                    params.put(StatsSetupConst.COLUMN_STATS_ACCURATE, StatsSetupConst.FALSE);
                } else {
                    params.remove(StatsSetupConst.STATS_GENERATED_VIA_STATS_TASK);
                    params.put(StatsSetupConst.COLUMN_STATS_ACCURATE, StatsSetupConst.TRUE);
                }
            }
            tbl.setParameters(params);
            updated = true;
        }
        return updated;
    }

    public static void populateQuickStats(FileStatus[] fileStatus, Map<String, String> params) {
        int numFiles = 0;
        long tableSize = 0L;
        for (FileStatus status : fileStatus) {
            if (!status.isDir()) {
                tableSize += status.getLen();
                numFiles += 1;
            }
        }
        params.put(StatsSetupConst.NUM_FILES, Integer.toString(numFiles));
        params.put(StatsSetupConst.TOTAL_SIZE, Long.toString(tableSize));
    }

    public static boolean requireCalStats(Configuration hiveConf, Partition oldPart, Partition newPart, Table tbl) {
        if (MetaStoreUtils.isView(tbl)) {
            return false;
        }
        if (oldPart == null && newPart == null) {
            return true;
        }
        if ((newPart == null) || (newPart.getParameters() == null) || !containsAllFastStats(newPart.getParameters())) {
            return true;
        }
        if (newPart.getParameters().containsKey(StatsSetupConst.STATS_GENERATED_VIA_STATS_TASK)) {
            return true;
        }
        if ((oldPart != null) && (oldPart.getParameters() != null)) {
            for (String stat : StatsSetupConst.fastStats) {
                if (oldPart.getParameters().containsKey(stat)) {
                    Long oldStat = Long.parseLong(oldPart.getParameters().get(stat));
                    Long newStat = Long.parseLong(newPart.getParameters().get(stat));
                    if (!oldStat.equals(newStat)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean updatePartitionStatsFast(Partition part, Warehouse wh) throws MetaException {
        return updatePartitionStatsFast(part, wh, false, false);
    }

    public static boolean updatePartitionStatsFast(Partition part, Warehouse wh, boolean madeDir) throws MetaException {
        return updatePartitionStatsFast(part, wh, madeDir, false);
    }

    public static boolean updatePartitionStatsFast(Partition part, Warehouse wh, boolean madeDir, boolean forceRecompute) throws MetaException {
        return updatePartitionStatsFast(new PartitionSpecProxy.SimplePartitionWrapperIterator(part), wh, madeDir, forceRecompute);
    }

    public static boolean updatePartitionStatsFast(PartitionSpecProxy.PartitionIterator part, Warehouse wh, boolean madeDir, boolean forceRecompute) throws MetaException {
        Map<String, String> params = part.getParameters();
        boolean updated = false;
        if (forceRecompute || params == null || !containsAllFastStats(params)) {
            if (params == null) {
                params = new HashMap<String, String>();
            }
            if (!madeDir) {
                LOG.warn("Updating partition stats fast for: " + part.getTableName());
                FileStatus[] fileStatus = wh.getFileStatusesForLocation(part.getLocation());
                populateQuickStats(fileStatus, params);
                LOG.warn("Updated size to " + params.get(StatsSetupConst.TOTAL_SIZE));
                if (!params.containsKey(StatsSetupConst.STATS_GENERATED_VIA_STATS_TASK)) {
                    for (String stat : StatsSetupConst.statsRequireCompute) {
                        params.put(stat, "-1");
                    }
                    params.put(StatsSetupConst.COLUMN_STATS_ACCURATE, StatsSetupConst.FALSE);
                } else {
                    params.remove(StatsSetupConst.STATS_GENERATED_VIA_STATS_TASK);
                    params.put(StatsSetupConst.COLUMN_STATS_ACCURATE, StatsSetupConst.TRUE);
                }
            }
            part.setParameters(params);
            updated = true;
        }
        return updated;
    }

    static public Deserializer getDeserializer(Configuration conf, org.apache.hadoop.hive.metastore.api.Table table, boolean skipConfError) throws MetaException {
        String lib = table.getSd().getSerdeInfo().getSerializationLib();
        if (lib == null) {
            return null;
        }
        try {
            Deserializer deserializer = ReflectionUtil.newInstance(conf.getClassByName(lib).asSubclass(Deserializer.class), conf);
            if (skipConfError) {
                SerDeUtils.initializeSerDeWithoutErrorCheck(deserializer, conf, MetaStoreUtils.getTableMetadata(table), null);
            } else {
                SerDeUtils.initializeSerDe(deserializer, conf, MetaStoreUtils.getTableMetadata(table), null);
            }
            return deserializer;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("error in initSerDe: " + e.getClass().getName() + " " + e.getMessage(), e);
            throw new MetaException(e.getClass().getName() + " " + e.getMessage());
        }
    }

    public static Class<? extends Deserializer> getDeserializerClass(Configuration conf, org.apache.hadoop.hive.metastore.api.Table table) throws Exception {
        String lib = table.getSd().getSerdeInfo().getSerializationLib();
        return lib == null ? null : conf.getClassByName(lib).asSubclass(Deserializer.class);
    }

    static public Deserializer getDeserializer(Configuration conf, org.apache.hadoop.hive.metastore.api.Partition part, org.apache.hadoop.hive.metastore.api.Table table) throws MetaException {
        String lib = part.getSd().getSerdeInfo().getSerializationLib();
        try {
            Deserializer deserializer = ReflectionUtil.newInstance(conf.getClassByName(lib).asSubclass(Deserializer.class), conf);
            SerDeUtils.initializeSerDe(deserializer, conf, MetaStoreUtils.getTableMetadata(table), MetaStoreUtils.getPartitionMetadata(part, table));
            return deserializer;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("error in initSerDe: " + e.getClass().getName() + " " + e.getMessage(), e);
            throw new MetaException(e.getClass().getName() + " " + e.getMessage());
        }
    }

    static public void deleteWHDirectory(Path path, Configuration conf, boolean use_trash) throws MetaException {
        try {
            if (!path.getFileSystem(conf).exists(path)) {
                LOG.warn("drop data called on table/partition with no directory: " + path);
                return;
            }
            if (use_trash) {
                int count = 0;
                Path newPath = new Path("/Trash/Current" + path.getParent().toUri().getPath());
                if (path.getFileSystem(conf).exists(newPath) == false) {
                    path.getFileSystem(conf).mkdirs(newPath);
                }
                do {
                    newPath = new Path("/Trash/Current" + path.toUri().getPath() + "." + count);
                    if (path.getFileSystem(conf).exists(newPath)) {
                        count++;
                        continue;
                    }
                    if (path.getFileSystem(conf).rename(path, newPath)) {
                        break;
                    }
                } while (++count < 50);
                if (count >= 50) {
                    throw new MetaException("Rename failed due to maxing out retries");
                }
            } else {
                path.getFileSystem(conf).delete(path, true);
            }
        } catch (IOException e) {
            LOG.error("Got exception trying to delete data dir: " + e);
            throw new MetaException(e.getMessage());
        } catch (MetaException e) {
            LOG.error("Got exception trying to delete data dir: " + e);
            throw e;
        }
    }

    public static List<String> getPvals(List<FieldSchema> partCols, Map<String, String> partSpec) {
        List<String> pvals = new ArrayList<String>();
        for (FieldSchema field : partCols) {
            String val = partSpec.get(field.getName());
            if (val == null) {
                val = "";
            }
            pvals.add(val);
        }
        return pvals;
    }

    static public boolean validateName(String name) {
        Pattern tpat = Pattern.compile("[\\w_]+");
        Matcher m = tpat.matcher(name);
        if (m.matches()) {
            return true;
        }
        return false;
    }

    public static final boolean validateColumnName(String name) {
        return true;
    }

    static public String validateTblColumns(List<FieldSchema> cols) {
        for (FieldSchema fieldSchema : cols) {
            if (!validateColumnName(fieldSchema.getName())) {
                return "name: " + fieldSchema.getName();
            }
            if (!validateColumnType(fieldSchema.getType())) {
                return "type: " + fieldSchema.getType();
            }
        }
        return null;
    }

    static void throwExceptionIfIncompatibleColTypeChange(List<FieldSchema> oldCols, List<FieldSchema> newCols) throws InvalidOperationException {
        List<String> incompatibleCols = new ArrayList<String>();
        int maxCols = Math.min(oldCols.size(), newCols.size());
        for (int i = 0; i < maxCols; i++) {
            if (!areColTypesCompatible(oldCols.get(i).getType(), newCols.get(i).getType())) {
                incompatibleCols.add(newCols.get(i).getName());
            }
        }
        if (!incompatibleCols.isEmpty()) {
            throw new InvalidOperationException("The following columns have types incompatible with the existing " + "columns in their respective positions :\n" + StringUtils.join(incompatibleCols, ','));
        }
    }

    static boolean isCascadeNeededInAlterTable(Table oldTable, Table newTable) {
        List<FieldSchema> oldCols = oldTable.getSd().getCols();
        List<FieldSchema> newCols = newTable.getSd().getCols();
        return !areSameColumns(oldCols, newCols);
    }

    static boolean areSameColumns(List<FieldSchema> oldCols, List<FieldSchema> newCols) {
        if (oldCols.size() != newCols.size()) {
            return false;
        } else {
            for (int i = 0; i < oldCols.size(); i++) {
                FieldSchema oldCol = oldCols.get(i);
                FieldSchema newCol = newCols.get(i);
                if (!oldCol.equals(newCol)) {
                    return false;
                }
            }
        }
        return true;
    }

    static private boolean areColTypesCompatible(String oldType, String newType) {
        if (oldType.equals(newType)) {
            return true;
        }
        if (serdeConstants.PrimitiveTypes.contains(oldType.toLowerCase()) && serdeConstants.PrimitiveTypes.contains(newType.toLowerCase())) {
            return true;
        }
        return false;
    }

    static public boolean validateColumnType(String type) {
        int last = 0;
        boolean lastAlphaDigit = Character.isLetterOrDigit(type.charAt(last));
        for (int i = 1; i <= type.length(); i++) {
            if (i == type.length() || Character.isLetterOrDigit(type.charAt(i)) != lastAlphaDigit) {
                String token = type.substring(last, i);
                last = i;
                if (!hiveThriftTypeMap.contains(token)) {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    public static String validateSkewedColNames(List<String> cols) {
        if (null == cols) {
            return null;
        }
        for (String col : cols) {
            if (!validateColumnName(col)) {
                return col;
            }
        }
        return null;
    }

    public static String validateSkewedColNamesSubsetCol(List<String> skewedColNames, List<FieldSchema> cols) {
        if (null == skewedColNames) {
            return null;
        }
        List<String> colNames = new ArrayList<String>();
        for (FieldSchema fieldSchema : cols) {
            colNames.add(fieldSchema.getName());
        }
        List<String> copySkewedColNames = new ArrayList<String>(skewedColNames);
        copySkewedColNames.removeAll(colNames);
        if (copySkewedColNames.isEmpty()) {
            return null;
        }
        return copySkewedColNames.toString();
    }

    public static String getListType(String t) {
        return "array<" + t + ">";
    }

    public static String getMapType(String k, String v) {
        return "map<" + k + "," + v + ">";
    }

    public static void setSerdeParam(SerDeInfo sdi, Properties schema, String param) {
        String val = schema.getProperty(param);
        if (org.apache.commons.lang.StringUtils.isNotBlank(val)) {
            sdi.getParameters().put(param, val);
        }
    }

    static HashMap<String, String> typeToThriftTypeMap;

    static {
        typeToThriftTypeMap = new HashMap<String, String>();
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.BOOLEAN_TYPE_NAME, "bool");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.TINYINT_TYPE_NAME, "byte");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.SMALLINT_TYPE_NAME, "i16");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.INT_TYPE_NAME, "i32");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.BIGINT_TYPE_NAME, "i64");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.DOUBLE_TYPE_NAME, "double");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.FLOAT_TYPE_NAME, "float");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.LIST_TYPE_NAME, "list");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.MAP_TYPE_NAME, "map");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.STRING_TYPE_NAME, "string");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.BINARY_TYPE_NAME, "binary");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.DATE_TYPE_NAME, "date");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.DATETIME_TYPE_NAME, "datetime");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.TIMESTAMP_TYPE_NAME, "timestamp");
        typeToThriftTypeMap.put(org.apache.hadoop.hive.serde.serdeConstants.DECIMAL_TYPE_NAME, "decimal");
    }

    static Set<String> hiveThriftTypeMap;

    static {
        hiveThriftTypeMap = new HashSet<String>();
        hiveThriftTypeMap.addAll(serdeConstants.PrimitiveTypes);
        hiveThriftTypeMap.addAll(org.apache.hadoop.hive.serde.serdeConstants.CollectionTypes);
        hiveThriftTypeMap.add(org.apache.hadoop.hive.serde.serdeConstants.UNION_TYPE_NAME);
        hiveThriftTypeMap.add(org.apache.hadoop.hive.serde.serdeConstants.STRUCT_TYPE_NAME);
    }

    public static String typeToThriftType(String type) {
        StringBuilder thriftType = new StringBuilder();
        int last = 0;
        boolean lastAlphaDigit = Character.isLetterOrDigit(type.charAt(last));
        for (int i = 1; i <= type.length(); i++) {
            if (i == type.length() || Character.isLetterOrDigit(type.charAt(i)) != lastAlphaDigit) {
                String token = type.substring(last, i);
                last = i;
                String thriftToken = typeToThriftTypeMap.get(token);
                thriftType.append(thriftToken == null ? token : thriftToken);
                lastAlphaDigit = !lastAlphaDigit;
            }
        }
        return thriftType.toString();
    }

    public static String getFullDDLFromFieldSchema(String structName, List<FieldSchema> fieldSchemas) {
        StringBuilder ddl = new StringBuilder();
        ddl.append(getDDLFromFieldSchema(structName, fieldSchemas));
        ddl.append('#');
        StringBuilder colnames = new StringBuilder();
        StringBuilder coltypes = new StringBuilder();
        boolean first = true;
        for (FieldSchema col : fieldSchemas) {
            if (first) {
                first = false;
            } else {
                colnames.append(',');
                coltypes.append(':');
            }
            colnames.append(col.getName());
            coltypes.append(col.getType());
        }
        ddl.append(colnames);
        ddl.append('#');
        ddl.append(coltypes);
        return ddl.toString();
    }

    public static String getDDLFromFieldSchema(String structName, List<FieldSchema> fieldSchemas) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("struct ");
        ddl.append(structName);
        ddl.append(" { ");
        boolean first = true;
        for (FieldSchema col : fieldSchemas) {
            if (first) {
                first = false;
            } else {
                ddl.append(", ");
            }
            ddl.append(typeToThriftType(col.getType()));
            ddl.append(' ');
            ddl.append(col.getName());
        }
        ddl.append("}");
        LOG.debug("DDL: " + ddl);
        return ddl.toString();
    }

    public static Properties getTableMetadata(org.apache.hadoop.hive.metastore.api.Table table) {
        return MetaStoreUtils.getSchema(table.getSd(), table.getSd(), table.getParameters(), table.getDbName(), table.getTableName(), table.getPartitionKeys());
    }

    public static Properties getPartitionMetadata(org.apache.hadoop.hive.metastore.api.Partition partition, org.apache.hadoop.hive.metastore.api.Table table) {
        return MetaStoreUtils.getSchema(partition.getSd(), partition.getSd(), partition.getParameters(), table.getDbName(), table.getTableName(), table.getPartitionKeys());
    }

    public static Properties getSchema(org.apache.hadoop.hive.metastore.api.Partition part, org.apache.hadoop.hive.metastore.api.Table table) {
        return MetaStoreUtils.getSchema(part.getSd(), table.getSd(), table.getParameters(), table.getDbName(), table.getTableName(), table.getPartitionKeys());
    }

    public static Properties getPartSchemaFromTableSchema(org.apache.hadoop.hive.metastore.api.StorageDescriptor sd, org.apache.hadoop.hive.metastore.api.StorageDescriptor tblsd, Map<String, String> parameters, String databaseName, String tableName, List<FieldSchema> partitionKeys, Properties tblSchema) {
        Properties schema = (Properties) tblSchema.clone();
        String inputFormat = sd.getInputFormat();
        if (inputFormat == null || inputFormat.length() == 0) {
            String tblInput = schema.getProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_INPUT_FORMAT);
            if (tblInput == null) {
                inputFormat = org.apache.hadoop.mapred.SequenceFileInputFormat.class.getName();
            } else {
                inputFormat = tblInput;
            }
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_INPUT_FORMAT, inputFormat);
        String outputFormat = sd.getOutputFormat();
        if (outputFormat == null || outputFormat.length() == 0) {
            String tblOutput = schema.getProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_OUTPUT_FORMAT);
            if (tblOutput == null) {
                outputFormat = org.apache.hadoop.mapred.SequenceFileOutputFormat.class.getName();
            } else {
                outputFormat = tblOutput;
            }
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_OUTPUT_FORMAT, outputFormat);
        if (sd.getLocation() != null) {
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_LOCATION, sd.getLocation());
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.BUCKET_COUNT, Integer.toString(sd.getNumBuckets()));
        if (sd.getBucketCols() != null && sd.getBucketCols().size() > 0) {
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.BUCKET_FIELD_NAME, sd.getBucketCols().get(0));
        }
        if (sd.getSerdeInfo() != null) {
            String cols = org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_COLUMNS;
            String colTypes = org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_COLUMN_TYPES;
            String parts = org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_PARTITION_COLUMNS;
            for (Map.Entry<String, String> param : sd.getSerdeInfo().getParameters().entrySet()) {
                String key = param.getKey();
                if (schema.get(key) != null && (key.equals(cols) || key.equals(colTypes) || key.equals(parts))) {
                    continue;
                }
                schema.put(key, (param.getValue() != null) ? param.getValue() : "");
            }
            if (sd.getSerdeInfo().getSerializationLib() != null) {
                schema.setProperty(org.apache.hadoop.hive.serde.serdeConstants.SERIALIZATION_LIB, sd.getSerdeInfo().getSerializationLib());
            }
        }
        if (parameters != null) {
            for (Entry<String, String> e : parameters.entrySet()) {
                schema.setProperty(e.getKey(), e.getValue());
            }
        }
        return schema;
    }

    public static Properties getSchema(org.apache.hadoop.hive.metastore.api.StorageDescriptor sd, org.apache.hadoop.hive.metastore.api.StorageDescriptor tblsd, Map<String, String> parameters, String databaseName, String tableName, List<FieldSchema> partitionKeys) {
        Properties schema = new Properties();
        String inputFormat = sd.getInputFormat();
        if (inputFormat == null || inputFormat.length() == 0) {
            inputFormat = org.apache.hadoop.mapred.SequenceFileInputFormat.class.getName();
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_INPUT_FORMAT, inputFormat);
        String outputFormat = sd.getOutputFormat();
        if (outputFormat == null || outputFormat.length() == 0) {
            outputFormat = org.apache.hadoop.mapred.SequenceFileOutputFormat.class.getName();
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.FILE_OUTPUT_FORMAT, outputFormat);
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_NAME, databaseName + "." + tableName);
        if (sd.getLocation() != null) {
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_LOCATION, sd.getLocation());
        }
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.BUCKET_COUNT, Integer.toString(sd.getNumBuckets()));
        if (sd.getBucketCols() != null && sd.getBucketCols().size() > 0) {
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.BUCKET_FIELD_NAME, sd.getBucketCols().get(0));
        }
        if (sd.getSerdeInfo() != null) {
            for (Map.Entry<String, String> param : sd.getSerdeInfo().getParameters().entrySet()) {
                schema.put(param.getKey(), (param.getValue() != null) ? param.getValue() : "");
            }
            if (sd.getSerdeInfo().getSerializationLib() != null) {
                schema.setProperty(org.apache.hadoop.hive.serde.serdeConstants.SERIALIZATION_LIB, sd.getSerdeInfo().getSerializationLib());
            }
        }
        StringBuilder colNameBuf = new StringBuilder();
        StringBuilder colTypeBuf = new StringBuilder();
        StringBuilder colComment = new StringBuilder();
        boolean first = true;
        for (FieldSchema col : tblsd.getCols()) {
            if (!first) {
                colNameBuf.append(",");
                colTypeBuf.append(":");
                colComment.append('\0');
            }
            colNameBuf.append(col.getName());
            colTypeBuf.append(col.getType());
            colComment.append((null != col.getComment()) ? col.getComment() : "");
            first = false;
        }
        String colNames = colNameBuf.toString();
        String colTypes = colTypeBuf.toString();
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_COLUMNS, colNames);
        schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_COLUMN_TYPES, colTypes);
        schema.setProperty("columns.comments", colComment.toString());
        if (sd.getCols() != null) {
            schema.setProperty(org.apache.hadoop.hive.serde.serdeConstants.SERIALIZATION_DDL, getDDLFromFieldSchema(tableName, sd.getCols()));
        }
        String partString = "";
        String partStringSep = "";
        String partTypesString = "";
        String partTypesStringSep = "";
        for (FieldSchema partKey : partitionKeys) {
            partString = partString.concat(partStringSep);
            partString = partString.concat(partKey.getName());
            partTypesString = partTypesString.concat(partTypesStringSep);
            partTypesString = partTypesString.concat(partKey.getType());
            if (partStringSep.length() == 0) {
                partStringSep = "/";
                partTypesStringSep = ":";
            }
        }
        if (partString.length() > 0) {
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_PARTITION_COLUMNS, partString);
            schema.setProperty(org.apache.hadoop.hive.metastore.api.hive_metastoreConstants.META_TABLE_PARTITION_COLUMN_TYPES, partTypesString);
        }
        if (parameters != null) {
            for (Entry<String, String> e : parameters.entrySet()) {
                if (e.getValue() != null) {
                    schema.setProperty(e.getKey(), e.getValue());
                }
            }
        }
        return schema;
    }

    public static String getColumnNamesFromFieldSchema(List<FieldSchema> fieldSchemas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldSchemas.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fieldSchemas.get(i).getName());
        }
        return sb.toString();
    }

    public static String getColumnTypesFromFieldSchema(List<FieldSchema> fieldSchemas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldSchemas.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fieldSchemas.get(i).getType());
        }
        return sb.toString();
    }

    public static void makeDir(Path path, HiveConf hiveConf) throws MetaException {
        FileSystem fs;
        try {
            fs = path.getFileSystem(hiveConf);
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (IOException e) {
            throw new MetaException("Unable to : " + path);
        }
    }

    public static void startMetaStore(final int port, final HadoopThriftAuthBridge bridge) throws Exception {
        startMetaStore(port, bridge, new HiveConf(HMSHandler.class));
    }

    public static void startMetaStore(final int port, final HadoopThriftAuthBridge bridge, final HiveConf hiveConf) throws Exception {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    HiveMetaStore.startMetaStore(port, bridge, hiveConf);
                } catch (Throwable e) {
                    LOG.error("Metastore Thrift Server threw an exception...", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        loopUntilHMSReady(port);
    }

    private static void loopUntilHMSReady(int port) throws Exception {
        int retries = 0;
        Exception exc = null;
        while (true) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(port), 5000);
                socket.close();
                return;
            } catch (Exception e) {
                if (retries++ > 6) {
                    exc = e;
                    break;
                }
                Thread.sleep(10000);
            }
        }
        throw exc;
    }

    public static int findFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    static void logAndThrowMetaException(Exception e) throws MetaException {
        String exInfo = "Got exception: " + e.getClass().getName() + " " + e.getMessage();
        LOG.error(exInfo, e);
        LOG.error("Converting exception to MetaException");
        throw new MetaException(exInfo);
    }

    public static List<FieldSchema> getFieldsFromDeserializer(String tableName, Deserializer deserializer) throws SerDeException, MetaException {
        ObjectInspector oi = deserializer.getObjectInspector();
        String[] names = tableName.split("\\.");
        String last_name = names[names.length - 1];
        for (int i = 1; i < names.length; i++) {
            if (oi instanceof StructObjectInspector) {
                StructObjectInspector soi = (StructObjectInspector) oi;
                StructField sf = soi.getStructFieldRef(names[i]);
                if (sf == null) {
                    throw new MetaException("Invalid Field " + names[i]);
                } else {
                    oi = sf.getFieldObjectInspector();
                }
            } else if (oi instanceof ListObjectInspector && names[i].equalsIgnoreCase("$elem$")) {
                ListObjectInspector loi = (ListObjectInspector) oi;
                oi = loi.getListElementObjectInspector();
            } else if (oi instanceof MapObjectInspector && names[i].equalsIgnoreCase("$key$")) {
                MapObjectInspector moi = (MapObjectInspector) oi;
                oi = moi.getMapKeyObjectInspector();
            } else if (oi instanceof MapObjectInspector && names[i].equalsIgnoreCase("$value$")) {
                MapObjectInspector moi = (MapObjectInspector) oi;
                oi = moi.getMapValueObjectInspector();
            } else {
                throw new MetaException("Unknown type for " + names[i]);
            }
        }
        ArrayList<FieldSchema> str_fields = new ArrayList<FieldSchema>();
        if (oi.getCategory() != Category.STRUCT) {
            str_fields.add(new FieldSchema(last_name, oi.getTypeName(), FROM_SERIALIZER));
        } else {
            List<? extends StructField> fields = ((StructObjectInspector) oi).getAllStructFieldRefs();
            for (int i = 0; i < fields.size(); i++) {
                StructField structField = fields.get(i);
                String fieldName = structField.getFieldName();
                String fieldTypeName = structField.getFieldObjectInspector().getTypeName();
                String fieldComment = determineFieldComment(structField.getFieldComment());
                str_fields.add(new FieldSchema(fieldName, fieldTypeName, fieldComment));
            }
        }
        return str_fields;
    }

    private static final String FROM_SERIALIZER = "from deserializer";

    private static String determineFieldComment(String comment) {
        return (comment == null) ? FROM_SERIALIZER : comment;
    }

    public static FieldSchema getFieldSchemaFromTypeInfo(String fieldName, TypeInfo typeInfo) {
        return new FieldSchema(fieldName, typeInfo.getTypeName(), "generated by TypeInfoUtils.getFieldSchemaFromTypeInfo");
    }

    public static boolean isExternalTable(Table table) {
        if (table == null) {
            return false;
        }
        Map<String, String> params = table.getParameters();
        if (params == null) {
            return false;
        }
        return "TRUE".equalsIgnoreCase(params.get("EXTERNAL"));
    }

    public static boolean isImmutableTable(Table table) {
        if (table == null) {
            return false;
        }
        Map<String, String> params = table.getParameters();
        if (params == null) {
            return false;
        }
        return "TRUE".equalsIgnoreCase(params.get(hive_metastoreConstants.IS_IMMUTABLE));
    }

    public static boolean isArchived(org.apache.hadoop.hive.metastore.api.Partition part) {
        Map<String, String> params = part.getParameters();
        if ("true".equalsIgnoreCase(params.get(hive_metastoreConstants.IS_ARCHIVED))) {
            return true;
        } else {
            return false;
        }
    }

    public static Path getOriginalLocation(org.apache.hadoop.hive.metastore.api.Partition part) {
        Map<String, String> params = part.getParameters();
        assert (isArchived(part));
        String originalLocation = params.get(hive_metastoreConstants.ORIGINAL_LOCATION);
        assert (originalLocation != null);
        return new Path(originalLocation);
    }

    public static boolean isNonNativeTable(Table table) {
        if (table == null) {
            return false;
        }
        return (table.getParameters().get(hive_metastoreConstants.META_TABLE_STORAGE) != null);
    }

    private static final PathFilter hiddenFileFilter = new PathFilter() {

        @Override
        public boolean accept(Path p) {
            String name = p.getName();
            return !name.startsWith("_") && !name.startsWith(".");
        }
    };

    public static boolean isDirEmpty(FileSystem fs, Path path) throws IOException {
        if (fs.exists(path)) {
            FileStatus[] status = fs.globStatus(new Path(path, "*"), hiddenFileFilter);
            if (status.length > 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean pvalMatches(List<String> partial, List<String> full) {
        if (partial.size() > full.size()) {
            return false;
        }
        Iterator<String> p = partial.iterator();
        Iterator<String> f = full.iterator();
        while (p.hasNext()) {
            String pval = p.next();
            String fval = f.next();
            if (pval.length() != 0 && !pval.equals(fval)) {
                return false;
            }
        }
        return true;
    }

    public static String getIndexTableName(String dbName, String baseTblName, String indexName) {
        return dbName + "__" + baseTblName + "_" + indexName + "__";
    }

    public static boolean isIndexTable(Table table) {
        if (table == null) {
            return false;
        }
        return TableType.INDEX_TABLE.toString().equals(table.getTableType());
    }

    public static String makeFilterStringFromMap(Map<String, String> m) {
        StringBuilder filter = new StringBuilder();
        for (Entry<String, String> e : m.entrySet()) {
            String col = e.getKey();
            String val = e.getValue();
            if (filter.length() == 0) {
                filter.append(col + "=\"" + val + "\"");
            } else {
                filter.append(" and " + col + "=\"" + val + "\"");
            }
        }
        return filter.toString();
    }

    public static boolean isView(Table table) {
        if (table == null) {
            return false;
        }
        return TableType.VIRTUAL_VIEW.toString().equals(table.getTableType());
    }

    static <T> List<T> getMetaStoreListeners(Class<T> clazz, HiveConf conf, String listenerImplList) throws MetaException {
        List<T> listeners = new ArrayList<T>();
        listenerImplList = listenerImplList.trim();
        if (listenerImplList.equals("")) {
            return listeners;
        }
        String[] listenerImpls = listenerImplList.split(",");
        for (String listenerImpl : listenerImpls) {
            try {
                T listener = (T) Class.forName(listenerImpl.trim(), true, JavaUtils.getClassLoader()).getConstructor(Configuration.class).newInstance(conf);
                listeners.add(listener);
            } catch (InvocationTargetException ie) {
                throw new MetaException("Failed to instantiate listener named: " + listenerImpl + ", reason: " + ie.getCause());
            } catch (Exception e) {
                throw new MetaException("Failed to instantiate listener named: " + listenerImpl + ", reason: " + e);
            }
        }
        return listeners;
    }

    public static Class<?> getClass(String rawStoreClassName) throws MetaException {
        try {
            return Class.forName(rawStoreClassName, true, JavaUtils.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new MetaException(rawStoreClassName + " class not found");
        }
    }

    public static <T> T newInstance(Class<T> theClass, Class<?>[] parameterTypes, Object[] initargs) {
        if (parameterTypes.length != initargs.length) {
            throw new IllegalArgumentException("Number of constructor parameter types doesn't match number of arguments");
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            if (!(clazz.isInstance(initargs[i]))) {
                throw new IllegalArgumentException("Object : " + initargs[i] + " is not an instance of " + clazz);
            }
        }
        try {
            Constructor<T> meth = theClass.getDeclaredConstructor(parameterTypes);
            meth.setAccessible(true);
            return meth.newInstance(initargs);
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate " + theClass.getName(), e);
        }
    }

    public static void validatePartitionNameCharacters(List<String> partVals, Pattern partitionValidationPattern) throws MetaException {
        String invalidPartitionVal = getPartitionValWithInvalidCharacter(partVals, partitionValidationPattern);
        if (invalidPartitionVal != null) {
            throw new MetaException("Partition value '" + invalidPartitionVal + "' contains a character " + "not matched by whitelist pattern '" + partitionValidationPattern.toString() + "'.  " + "(configure with " + HiveConf.ConfVars.METASTORE_PARTITION_NAME_WHITELIST_PATTERN.varname + ")");
        }
    }

    public static boolean partitionNameHasValidCharacters(List<String> partVals, Pattern partitionValidationPattern) {
        return getPartitionValWithInvalidCharacter(partVals, partitionValidationPattern) == null;
    }

    public static boolean compareFieldColumns(List<FieldSchema> schema1, List<FieldSchema> schema2) {
        if (schema1.size() != schema2.size()) {
            return false;
        }
        for (int i = 0; i < schema1.size(); i++) {
            FieldSchema f1 = schema1.get(i);
            FieldSchema f2 = schema2.get(i);
            if (f1.getName() == null) {
                if (f2.getName() != null) {
                    return false;
                }
            } else if (!f1.getName().equals(f2.getName())) {
                return false;
            }
            if (f1.getType() == null) {
                if (f2.getType() != null) {
                    return false;
                }
            } else if (!f1.getType().equals(f2.getType())) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, String> getMetaStoreSaslProperties(HiveConf conf) {
        return ShimLoader.getHadoopThriftAuthBridge().getHadoopSaslProperties(conf);
    }

    private static String getPartitionValWithInvalidCharacter(List<String> partVals, Pattern partitionValidationPattern) {
        if (partitionValidationPattern == null) {
            return null;
        }
        for (String partVal : partVals) {
            if (!partitionValidationPattern.matcher(partVal).matches()) {
                return partVal;
            }
        }
        return null;
    }

    public static ProtectMode getProtectMode(Partition partition) {
        return getProtectMode(partition.getParameters());
    }

    public static ProtectMode getProtectMode(Table table) {
        return getProtectMode(table.getParameters());
    }

    private static ProtectMode getProtectMode(Map<String, String> parameters) {
        if (parameters == null) {
            return null;
        }
        if (!parameters.containsKey(ProtectMode.PARAMETER_NAME)) {
            return new ProtectMode();
        } else {
            return ProtectMode.getProtectModeFromString(parameters.get(ProtectMode.PARAMETER_NAME));
        }
    }

    public static boolean canDropPartition(Table table, Partition partition) {
        ProtectMode mode = getProtectMode(partition);
        ProtectMode parentMode = getProtectMode(table);
        return (!mode.noDrop && !mode.offline && !mode.readOnly && !parentMode.noDropCascade);
    }

    public static String ARCHIVING_LEVEL = "archiving_level";

    public static int getArchivingLevel(Partition part) throws MetaException {
        if (!isArchived(part)) {
            throw new MetaException("Getting level of unarchived partition");
        }
        String lv = part.getParameters().get(ARCHIVING_LEVEL);
        if (lv != null) {
            return Integer.parseInt(lv);
        } else {
            return part.getValues().size();
        }
    }

    public static String[] getQualifiedName(String defaultDbName, String tableName) {
        String[] names = tableName.split("\\.");
        if (names.length == 1) {
            return new String[] { defaultDbName, tableName };
        }
        return new String[] { names[0], names[1] };
    }

    private static final com.google.common.base.Function<String, String> transFormNullsToEmptyString = new com.google.common.base.Function<String, String>() {

        @Override
        public java.lang.String apply(@Nullable java.lang.String string) {
            if (string == null) {
                return "";
            } else {
                return string;
            }
        }
    };

    public static Map<String, String> trimMapNulls(Map<String, String> dnMap, boolean retrieveMapNullsAsEmptyStrings) {
        if (dnMap == null) {
            return null;
        }
        if (retrieveMapNullsAsEmptyStrings) {
            return Maps.newLinkedHashMap(Maps.transformValues(dnMap, transFormNullsToEmptyString));
        } else {
            return Maps.newLinkedHashMap(Maps.filterValues(dnMap, Predicates.notNull()));
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
}
