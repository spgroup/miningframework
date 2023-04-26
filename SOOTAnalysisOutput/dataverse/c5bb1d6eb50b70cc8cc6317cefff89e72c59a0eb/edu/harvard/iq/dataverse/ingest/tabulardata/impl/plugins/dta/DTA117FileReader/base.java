package edu.harvard.iq.dataverse.ingest.tabulardata.impl.plugins.dta;

import java.io.*;
import java.nio.*;
import java.util.logging.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import org.apache.commons.lang.*;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import edu.harvard.iq.dataverse.DataTable;
import edu.harvard.iq.dataverse.datavariable.DataVariable;
import edu.harvard.iq.dataverse.datavariable.SummaryStatistic;
import edu.harvard.iq.dataverse.datavariable.VariableCategory;
import edu.harvard.iq.dataverse.datavariable.VariableRange;
import edu.harvard.iq.dataverse.ingest.plugin.spi.*;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataFileReader;
import edu.harvard.iq.dataverse.ingest.tabulardata.spi.TabularDataFileReaderSpi;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataIngest;

public class DTA117FileReader extends TabularDataFileReader {

    private static final String TAG_DTA_117 = "stata_dta";

    private static final String TAG_HEADER = "header";

    private static final String TAG_HEADER_FILEFORMATID = "release";

    private static final String TAG_HEADER_BYTEORDER = "byteorder";

    private static final String TAG_HEADER_VARNUMBER = "K";

    private static final String TAG_HEADER_OBSNUMBER = "N";

    private static final String TAG_HEADER_FILELABEL = "label";

    private static final String TAG_HEADER_TIMESTAMP = "timestamp";

    private static final String TAG_MAP = "map";

    private static final String TAG_VARIABLE_TYPES = "variable_types";

    private static final String TAG_VARIABLE_NAMES = "varnames";

    private static final String TAG_SORT_ORDER = "sortlist";

    private static final String TAG_DISPLAY_FORMATS = "formats";

    private static final String TAG_VALUE_LABEL_FORMAT_NAMES = "value_label_names";

    private static final String TAG_VARIABLE_LABELS = "variable_labels";

    private static final String TAG_CHARACTERISTICS = "characteristics";

    private static final String TAG_CHARACTERISTICS_SUBSECTION = "ch";

    private static final String TAG_DATA = "data";

    private static final String TAG_STRLS = "strls";

    private static final String STRL_GSO_HEAD = "GSO";

    private static final String TAG_VALUE_LABELS = "value_labels";

    private static final String TAG_VALUE_LABELS_LBL_DEF = "lbl";

    private static Map<Integer, String> STATA_RELEASE_NUMBER = new HashMap<Integer, String>();

    private static Map<Integer, Map<String, Integer>> CONSTANT_TABLE = new LinkedHashMap<Integer, Map<String, Integer>>();

    private static Map<String, Integer> release117constant = new LinkedHashMap<String, Integer>();

    private static Map<String, Integer> byteLengthTable117 = new HashMap<String, Integer>();

    private static Map<Integer, String> variableTypeTable117 = new LinkedHashMap<Integer, String>();

    private static final int[] LENGTH_HEADER = { 60, 109 };

    private static final int[] LENGTH_LABEL = { 32, 81 };

    private static final int[] LENGTH_NAME = { 9, 33 };

    private static final int[] LENGTH_FORMAT_FIELD = { 7, 12, 49 };

    private static final int[] LENGTH_EXPANSION_FIELD = { 0, 2, 4 };

    private static final int[] DBL_MV_PWR = { 333, 1023 };

    private static final int DTA_MAGIC_NUMBER_LENGTH = 4;

    private static final int NVAR_FIELD_LENGTH = 2;

    private static final int NOBS_FIELD_LENGTH = 4;

    private static final int TIME_STAMP_LENGTH = 18;

    private static final int VAR_SORT_FIELD_LENGTH = 2;

    private static final int VALUE_LABEL_HEADER_PADDING_LENGTH = 3;

    private static int MISSING_VALUE_BIAS = 26;

    private byte BYTE_MISSING_VALUE = Byte.MAX_VALUE;

    private short INT_MISSIG_VALUE = Short.MAX_VALUE;

    private int LONG_MISSING_VALUE = Integer.MAX_VALUE;

    static {
        STATA_RELEASE_NUMBER.put(117, "v.13");
        release117constant.put("HEADER", LENGTH_HEADER[1]);
        release117constant.put("LABEL", LENGTH_LABEL[1]);
        release117constant.put("NAME", LENGTH_NAME[1]);
        release117constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release117constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release117constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTANT_TABLE.put(117, release117constant);
        byteLengthTable117.put("Byte", 1);
        byteLengthTable117.put("Integer", 2);
        byteLengthTable117.put("Long", 4);
        byteLengthTable117.put("Float", 4);
        byteLengthTable117.put("Double", 8);
        byteLengthTable117.put("STRL", 8);
        variableTypeTable117.put(65530, "Byte");
        variableTypeTable117.put(65529, "Integer");
        variableTypeTable117.put(65528, "Long");
        variableTypeTable117.put(65527, "Float");
        variableTypeTable117.put(65526, "Double");
    }

    private static String[] MIME_TYPE = { "application/x-stata", "application/x-stata-13" };

    private static String unfVersionNumber = "6";

    private static final List<Float> FLOAT_MISSING_VALUES = Arrays.asList(0x1.000p127f, 0x1.001p127f, 0x1.002p127f, 0x1.003p127f, 0x1.004p127f, 0x1.005p127f, 0x1.006p127f, 0x1.007p127f, 0x1.008p127f, 0x1.009p127f, 0x1.00ap127f, 0x1.00bp127f, 0x1.00cp127f, 0x1.00dp127f, 0x1.00ep127f, 0x1.00fp127f, 0x1.010p127f, 0x1.011p127f, 0x1.012p127f, 0x1.013p127f, 0x1.014p127f, 0x1.015p127f, 0x1.016p127f, 0x1.017p127f, 0x1.018p127f, 0x1.019p127f, 0x1.01ap127f);

    private Set<Float> FLOAT_MISSING_VALUE_SET = new HashSet<Float>(FLOAT_MISSING_VALUES);

    private static final List<Double> DOUBLE_MISSING_VALUE_LIST = Arrays.asList(0x1.000p1023, 0x1.001p1023, 0x1.002p1023, 0x1.003p1023, 0x1.004p1023, 0x1.005p1023, 0x1.006p1023, 0x1.007p1023, 0x1.008p1023, 0x1.009p1023, 0x1.00ap1023, 0x1.00bp1023, 0x1.00cp1023, 0x1.00dp1023, 0x1.00ep1023, 0x1.00fp1023, 0x1.010p1023, 0x1.011p1023, 0x1.012p1023, 0x1.013p1023, 0x1.014p1023, 0x1.015p1023, 0x1.016p1023, 0x1.017p1023, 0x1.018p1023, 0x1.019p1023, 0x1.01ap1023);

    private Set<Double> DOUBLE_MISSING_VALUE_SET = new HashSet<Double>(DOUBLE_MISSING_VALUE_LIST);

    private static SimpleDateFormat sdf_ymdhmsS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat sdf_hms = new SimpleDateFormat("HH:mm:ss");

    private static SimpleDateFormat sdf_yw = new SimpleDateFormat("yyyy-'W'ww");

    private static Calendar GCO_STATA = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    private static String[] DATE_TIME_FORMAT = { "%tc", "%td", "%tw", "%tq", "%tm", "%th", "%ty", "%d", "%w", "%q", "%m", "h", "%tb" };

    private static String[] DATE_TIME_CATEGORY = { "time", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date" };

    private static Map<String, String> DATE_TIME_FORMAT_TABLE = new LinkedHashMap<String, String>();

    private static long SECONDS_PER_YEAR = 24 * 60 * 60 * 1000L;

    private static long STATA_BIAS_TO_EPOCH;

    static {
        sdf_ymdhmsS.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf_ymd.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf_hms.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf_yw.setTimeZone(TimeZone.getTimeZone("GMT"));
        GCO_STATA.set(1, 1960);
        GCO_STATA.set(2, 0);
        GCO_STATA.set(5, 1);
        GCO_STATA.set(9, 0);
        GCO_STATA.set(10, 0);
        GCO_STATA.set(12, 0);
        GCO_STATA.set(13, 0);
        GCO_STATA.set(14, 0);
        STATA_BIAS_TO_EPOCH = GCO_STATA.getTimeInMillis();
        for (int i = 0; i < DATE_TIME_FORMAT.length; i++) {
            DATE_TIME_FORMAT_TABLE.put(DATE_TIME_FORMAT[i], DATE_TIME_CATEGORY[i]);
        }
    }

    private static Logger logger = Logger.getLogger(DTAFileReader.class.getPackage().getName());

    private DataTable dataTable = new DataTable();

    private DTADataMap dtaMap = null;

    private String[] valueLabelsLookupTable = null;

    private Map<String, Integer> constantTable;

    private Map<String, Integer> byteLengthTable;

    private Map<Integer, String> variableTypeTable;

    private Map<String, String> cachedGSOs;

    private NumberFormat twoDigitFormatter = new DecimalFormat("00");

    private NumberFormat doubleNumberFormatter = new DecimalFormat();

    TabularDataIngest ingesteddata = new TabularDataIngest();

    private int releaseNumber = 117;

    private int headerLength;

    private int dataLabelLength;

    private boolean hasSTRLs = false;

    private String[] variableTypes = null;

    private String[] dateVariableFormats = null;

    private static final String MissingValueForTabDelimitedFile = "";

    public DTA117FileReader(TabularDataFileReaderSpi originator) {
        super(originator);
    }

    private void init() throws IOException {
        logger.fine("release number=" + releaseNumber);
        variableTypeTable = variableTypeTable117;
        byteLengthTable = byteLengthTable117;
        BYTE_MISSING_VALUE -= MISSING_VALUE_BIAS;
        INT_MISSIG_VALUE -= MISSING_VALUE_BIAS;
        LONG_MISSING_VALUE -= MISSING_VALUE_BIAS;
        constantTable = CONSTANT_TABLE.get(releaseNumber);
        headerLength = constantTable.get("HEADER") - DTA_MAGIC_NUMBER_LENGTH;
        dataLabelLength = headerLength - (NVAR_FIELD_LENGTH + NOBS_FIELD_LENGTH + TIME_STAMP_LENGTH);
        logger.fine("data_label_length=" + dataLabelLength);
        logger.fine("constant table to be used:\n" + constantTable);
        doubleNumberFormatter.setGroupingUsed(false);
        doubleNumberFormatter.setMaximumFractionDigits(340);
    }

    public TabularDataIngest read(BufferedInputStream stream, File dataFile) throws IOException {
        logger.info("DTA117FileReader: read() start");
        if (dataFile != null) {
            throw new IOException("this plugin does not support external raw data files");
        }
        DataReader dataReader = null;
        try {
            init();
            dataReader = new DataReader(stream);
            dataReader.readOpeningTag(TAG_DTA_117);
            readHeader(dataReader);
            readMap(dataReader);
            readVariableTypes(dataReader);
            readVariableNames(dataReader);
            readSortOrder(dataReader);
            readDisplayFormats(dataReader);
            readValueLabelFormatNames(dataReader);
            readVariableLabels(dataReader);
            readCharacteristics(dataReader);
            readData(dataReader);
            readSTRLs(dataReader);
            readValueLabels(dataReader);
            dataReader.readClosingTag(TAG_DTA_117);
            ingesteddata.setDataTable(dataTable);
        } catch (IllegalArgumentException iaex) {
            throw new IOException(iaex.getMessage());
        }
        logger.info("DTA117FileReader: read() end.");
        return ingesteddata;
    }

    private void readHeader(DataReader dataReader) throws IOException {
        logger.fine("readHeader(): start");
        if (dataReader == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        logger.fine("reading the version header.");
        dataReader.readOpeningTag(TAG_HEADER);
        String dtaVersionTag = dataReader.readPrimitiveStringSection(TAG_HEADER_FILEFORMATID, 3);
        if (!"117".equals(dtaVersionTag)) {
            throw new IOException("Unexpected version tag found: " + dtaVersionTag + "; expected value: 117.");
        }
        String byteOrderTag = dataReader.readPrimitiveStringSection(TAG_HEADER_BYTEORDER);
        logger.fine("byte order: " + byteOrderTag);
        if ("LSF".equals(byteOrderTag)) {
            dataReader.setLSF(true);
        } else if ("MSF".equals(byteOrderTag)) {
            dataReader.setLSF(false);
        }
        int varNumber = dataReader.readIntegerSection(TAG_HEADER_VARNUMBER, 2);
        logger.fine("number of variables: " + varNumber);
        int obsNumber = dataReader.readIntegerSection(TAG_HEADER_OBSNUMBER, 4);
        logger.fine("number of observations: " + obsNumber);
        dataTable.setVarQuantity(new Long(varNumber));
        dataTable.setCaseQuantity(new Long(obsNumber));
        dataTable.setOriginalFileFormat(MIME_TYPE[0]);
        dataTable.setOriginalFormatVersion("STATA 13");
        dataTable.setUnf("UNF:pending");
        String datasetLabel = dataReader.readDefinedStringSection(TAG_HEADER_FILELABEL, 80);
        logger.fine("dataset label: " + datasetLabel);
        String datasetTimeStamp = dataReader.readDefinedStringSection(TAG_HEADER_TIMESTAMP, 17);
        logger.fine("dataset time stamp: " + datasetTimeStamp);
        if (datasetTimeStamp == null || (datasetTimeStamp.length() > 0 && datasetTimeStamp.length() < 17)) {
            throw new IOException("unexpected/invalid length of the time stamp in the DTA117 header.");
        } else {
        }
        dataReader.readClosingTag("header");
        logger.fine("readHeader(): end");
    }

    private void readMap(DataReader reader) throws IOException {
        logger.fine("Map section; at offset " + reader.getByteOffset());
        reader.readOpeningTag(TAG_MAP);
        dtaMap = new DTADataMap();
        long dta_offset_stata_data = reader.readLongInteger();
        logger.fine("dta_offset_stata_data: " + dta_offset_stata_data);
        dtaMap.setOffset_head(dta_offset_stata_data);
        long dta_offset_map = reader.readLongInteger();
        logger.fine("dta_offset_map: " + dta_offset_map);
        dtaMap.setOffset_map(dta_offset_map);
        long dta_offset_variable_types = reader.readLongInteger();
        logger.fine("dta_offset_variable_types: " + dta_offset_variable_types);
        dtaMap.setOffset_types(dta_offset_variable_types);
        long dta_offset_varnames = reader.readLongInteger();
        logger.fine("dta_offset_varnames: " + dta_offset_varnames);
        dtaMap.setOffset_varnames(dta_offset_varnames);
        long dta_offset_sortlist = reader.readLongInteger();
        logger.fine("dta_offset_sortlist: " + dta_offset_sortlist);
        dtaMap.setOffset_srtlist(dta_offset_sortlist);
        long dta_offset_formats = reader.readLongInteger();
        logger.fine("dta_offset_formats: " + dta_offset_formats);
        dtaMap.setOffset_fmts(dta_offset_formats);
        long dta_offset_value_label_names = reader.readLongInteger();
        logger.fine("dta_offset_value_label_names: " + dta_offset_value_label_names);
        dtaMap.setOffset_vlblnames(dta_offset_value_label_names);
        long dta_offset_variable_labels = reader.readLongInteger();
        logger.fine("dta_offset_variable_labels: " + dta_offset_variable_labels);
        dtaMap.setOffset_varlabs(dta_offset_variable_labels);
        long dta_offset_characteristics = reader.readLongInteger();
        logger.fine("dta_offset_characteristics: " + dta_offset_characteristics);
        dtaMap.setOffset_characteristics(dta_offset_characteristics);
        long dta_offset_data = reader.readLongInteger();
        logger.fine("dta_offset_data: " + dta_offset_data);
        dtaMap.setOffset_data(dta_offset_data);
        long dta_offset_strls = reader.readLongInteger();
        logger.fine("dta_offset_strls: " + dta_offset_strls);
        dtaMap.setOffset_strls(dta_offset_strls);
        long dta_offset_value_labels = reader.readLongInteger();
        logger.fine("dta_offset_value_labels: " + dta_offset_value_labels);
        dtaMap.setOffset_vallabs(dta_offset_value_labels);
        long dta_offset_data_close = reader.readLongInteger();
        logger.fine("dta_offset_data_close: " + dta_offset_data_close);
        dtaMap.setOffset_data_close(dta_offset_data_close);
        long dta_offset_eof = reader.readLongInteger();
        logger.fine("dta_offset_eof: " + dta_offset_eof);
        dtaMap.setOffset_eof(dta_offset_eof);
        reader.readClosingTag(TAG_MAP);
    }

    private void readVariableTypes(DataReader reader) throws IOException {
        logger.fine("Type section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_types());
        reader.readOpeningTag(TAG_VARIABLE_TYPES);
        List<DataVariable> variableList = new ArrayList<DataVariable>();
        variableTypes = new String[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            int type = reader.readShortInteger();
            logger.fine("variable " + i + ": type=" + type);
            DataVariable dv = new DataVariable();
            dv.setInvalidRanges(new ArrayList<VariableRange>());
            dv.setSummaryStatistics(new ArrayList<SummaryStatistic>());
            dv.setCategories(new ArrayList<VariableCategory>());
            dv.setUnf("UNF:pending");
            dv.setFileOrder(i);
            dv.setDataTable(dataTable);
            variableTypes[i] = configureVariableType(dv, type);
            variableList.add(dv);
        }
        reader.readClosingTag(TAG_VARIABLE_TYPES);
        dataTable.setDataVariables(variableList);
    }

    private String configureVariableType(DataVariable dv, int type) throws IOException {
        String typeLabel = null;
        if (variableTypeTable.containsKey(type)) {
            typeLabel = variableTypeTable.get(type);
            dv.setTypeNumeric();
            if (typeLabel.equals("Byte") || typeLabel.equals("Integer") || typeLabel.equals("Long")) {
                dv.setIntervalDiscrete();
            } else if (typeLabel.equals("Float") || typeLabel.equals("Double")) {
                dv.setIntervalContinuous();
            } else {
                throw new IOException("Unrecognized type label: " + typeLabel + " for Stata type value (short) " + type + ".");
            }
        } else {
            if (type == 32768) {
                typeLabel = "STRL";
                hasSTRLs = true;
            } else if (type > 0 && type < 2046) {
                typeLabel = "STR" + type;
            } else {
                throw new IOException("unknown variable type value encountered: " + type);
            }
            dv.setTypeCharacter();
            dv.setIntervalDiscrete();
        }
        return typeLabel;
    }

    private void readVariableNames(DataReader reader) throws IOException {
        logger.fine("Variable names section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_varnames());
        reader.readOpeningTag(TAG_VARIABLE_NAMES);
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            String variableName = reader.readString(33);
            logger.fine("variable " + i + ": name=" + variableName);
            if ((variableName != null) && (!variableName.equals(""))) {
                dataTable.getDataVariables().get(i).setName(variableName);
            } else {
            }
        }
        reader.readClosingTag(TAG_VARIABLE_NAMES);
    }

    private void readSortOrder(DataReader reader) throws IOException {
        logger.fine("Sort Order section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_srtlist());
        reader.readOpeningTag(TAG_SORT_ORDER);
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            int order = reader.readShortInteger();
            logger.fine("variable " + i + ": sort order=" + order);
        }
        int terminatingShort = reader.readShortInteger();
        reader.readClosingTag(TAG_SORT_ORDER);
    }

    private void readDisplayFormats(DataReader reader) throws IOException {
        logger.fine("Formats section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_fmts());
        reader.readOpeningTag(TAG_DISPLAY_FORMATS);
        dateVariableFormats = new String[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            String variableFormat = reader.readString(49);
            logger.fine("variable " + i + ": displayFormat=" + variableFormat);
            String variableFormatKey = null;
            if (variableFormat.startsWith("%t")) {
                variableFormatKey = variableFormat.substring(0, 3);
            } else {
                variableFormatKey = variableFormat.substring(0, 2);
            }
            logger.fine(i + " th variableFormatKey=" + variableFormatKey);
            if (DATE_TIME_FORMAT_TABLE.containsKey(variableFormatKey)) {
                dateVariableFormats[i] = variableFormat;
                dataTable.getDataVariables().get(i).setFormatCategory(DATE_TIME_FORMAT_TABLE.get(variableFormatKey));
                logger.fine(i + "th var: category=" + DATE_TIME_FORMAT_TABLE.get(variableFormatKey));
                dataTable.getDataVariables().get(i).setTypeCharacter();
                dataTable.getDataVariables().get(i).setIntervalDiscrete();
            }
        }
        reader.readClosingTag(TAG_DISPLAY_FORMATS);
    }

    private void readValueLabelFormatNames(DataReader reader) throws IOException {
        logger.fine("Category valuable section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_vlblnames());
        reader.readOpeningTag(TAG_VALUE_LABEL_FORMAT_NAMES);
        valueLabelsLookupTable = new String[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            String valueLabelFormat = reader.readString(33);
            logger.fine("variable " + i + ": value label format=" + valueLabelFormat);
            if ((valueLabelFormat != null) && (!valueLabelFormat.equals(""))) {
                valueLabelsLookupTable[i] = valueLabelFormat;
            }
        }
        reader.readClosingTag(TAG_VALUE_LABEL_FORMAT_NAMES);
    }

    private void readVariableLabels(DataReader reader) throws IOException {
        logger.fine("Variable labels section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_varlabs());
        reader.readOpeningTag(TAG_VARIABLE_LABELS);
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            String variableLabel = reader.readString(81);
            logger.fine("variable " + i + ": label=" + variableLabel);
            if ((variableLabel != null) && (!variableLabel.equals(""))) {
                dataTable.getDataVariables().get(i).setLabel(variableLabel);
            }
        }
        reader.readClosingTag(TAG_VARIABLE_LABELS);
    }

    private void readCharacteristics(DataReader reader) throws IOException {
        logger.fine("Characteristics section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_characteristics());
        reader.readOpeningTag(TAG_CHARACTERISTICS);
        reader.skipDefinedSections(TAG_CHARACTERISTICS_SUBSECTION);
        reader.readClosingTag(TAG_CHARACTERISTICS);
    }

    private void readData(DataReader reader) throws IOException {
        logger.fine("Data section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_data());
        logger.fine("readData(): start");
        reader.readOpeningTag(TAG_DATA);
        int nvar = dataTable.getVarQuantity().intValue();
        int nobs = dataTable.getCaseQuantity().intValue();
        int[] variableByteLengths = getVariableByteLengths(variableTypes);
        int bytes_per_row = calculateBytesPerRow(variableByteLengths);
        logger.fine("data dimensions[observations x variables] = (" + nobs + "x" + nvar + ")");
        logger.fine("bytes per row=" + bytes_per_row + " bytes");
        logger.fine("variableTypes=" + Arrays.deepToString(variableTypes));
        FileOutputStream fileOutTab = null;
        PrintWriter pwout = null;
        File tabDelimitedDataFile = File.createTempFile("tempTabfile.", ".tab");
        ingesteddata.setTabDelimitedFile(tabDelimitedDataFile);
        fileOutTab = new FileOutputStream(tabDelimitedDataFile);
        pwout = new PrintWriter(new OutputStreamWriter(fileOutTab, "utf8"), true);
        logger.fine("Beginning to read data stream.");
        for (int i = 0; i < nobs; i++) {
            Object[] dataRow = new Object[nvar];
            int byte_offset = 0;
            for (int columnCounter = 0; columnCounter < nvar; columnCounter++) {
                String varType = variableTypes[columnCounter];
                boolean isDateTimeDatum = false;
                String formatCategory = dataTable.getDataVariables().get(columnCounter).getFormatCategory();
                if (formatCategory != null && (formatCategory.equals("time") || formatCategory.equals("date"))) {
                    isDateTimeDatum = true;
                }
                String variableFormat = dateVariableFormats[columnCounter];
                if (varType == null || varType.equals("")) {
                    throw new IOException("Undefined variable type encountered in readData()");
                }
                if (varType.equals("Byte")) {
                    byte byte_datum = reader.readSignedByte();
                    logger.fine(i + "-th row " + columnCounter + "=th column byte =" + byte_datum);
                    if (byte_datum >= BYTE_MISSING_VALUE) {
                        logger.fine(i + "-th row " + columnCounter + "=th column byte MV=" + byte_datum);
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        dataRow[columnCounter] = byte_datum;
                        logger.fine(i + "-th row " + columnCounter + "-th column byte value=" + byte_datum);
                    }
                    byte_offset++;
                } else if (varType.equals("Integer")) {
                    short short_datum = (short) reader.readShortSignedInteger();
                    logger.fine(i + "-th row " + columnCounter + "=th column stata int =" + short_datum);
                    if (short_datum >= INT_MISSIG_VALUE) {
                        logger.fine(i + "-th row " + columnCounter + "=th column stata long missing value=" + short_datum);
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        if (isDateTimeDatum) {
                            DecodedDateTime ddt = decodeDateTimeData("short", variableFormat, Short.toString(short_datum));
                            logger.fine(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                            dataRow[columnCounter] = ddt.decodedDateTime;
                            dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                        } else {
                            dataRow[columnCounter] = short_datum;
                            logger.fine(i + "-th row " + columnCounter + "-th column \"integer\" value=" + short_datum);
                        }
                    }
                    byte_offset += 2;
                } else if (varType.equals("Long")) {
                    int int_datum = reader.readSignedInteger();
                    if (int_datum >= LONG_MISSING_VALUE) {
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        if (isDateTimeDatum) {
                            DecodedDateTime ddt = decodeDateTimeData("int", variableFormat, Integer.toString(int_datum));
                            logger.fine(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                            dataRow[columnCounter] = ddt.decodedDateTime;
                            dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                        } else {
                            dataRow[columnCounter] = int_datum;
                            logger.fine(i + "-th row " + columnCounter + "-th column \"long\" value=" + int_datum);
                        }
                    }
                    byte_offset += 4;
                } else if (varType.equals("Float")) {
                    float float_datum = reader.readFloat();
                    logger.fine(i + "-th row " + columnCounter + "=th column float =" + float_datum);
                    if (FLOAT_MISSING_VALUE_SET.contains(float_datum)) {
                        logger.fine(i + "-th row " + columnCounter + "=th column float missing value=" + float_datum);
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        if (isDateTimeDatum) {
                            DecodedDateTime ddt = decodeDateTimeData("float", variableFormat, doubleNumberFormatter.format(float_datum));
                            logger.fine(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                            dataRow[columnCounter] = ddt.decodedDateTime;
                            dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                        } else {
                            dataRow[columnCounter] = float_datum;
                            logger.fine(i + "-th row " + columnCounter + "=th column float value:" + float_datum);
                            dataTable.getDataVariables().get(columnCounter).setFormat("float");
                        }
                    }
                    byte_offset += 4;
                } else if (varType.equals("Double")) {
                    double double_datum = reader.readDouble();
                    if (DOUBLE_MISSING_VALUE_SET.contains(double_datum)) {
                        logger.finer(i + "-th row " + columnCounter + "=th column double missing value=" + double_datum);
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        if (isDateTimeDatum) {
                            DecodedDateTime ddt = decodeDateTimeData("double", variableFormat, doubleNumberFormatter.format(double_datum));
                            logger.finer(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                            dataRow[columnCounter] = ddt.decodedDateTime;
                            dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                        } else {
                            logger.fine(i + "-th row " + columnCounter + "=th column double value:" + double_datum);
                            dataRow[columnCounter] = double_datum;
                        }
                    }
                    byte_offset += 8;
                } else if (varType.matches("^STR[1-9][0-9]*")) {
                    int strVarLength = variableByteLengths[columnCounter];
                    logger.fine(i + "-th row " + columnCounter + "=th column is a string (" + strVarLength + " bytes)");
                    String string_datum = reader.readString(strVarLength);
                    if (string_datum.length() < 64) {
                        logger.fine(i + "-th row " + columnCounter + "=th column string =" + string_datum);
                    } else {
                        logger.fine(i + "-th row " + columnCounter + "=th column string =" + string_datum.substring(0, 64) + "... (truncated)");
                    }
                    if (string_datum.equals("")) {
                        logger.fine(i + "-th row " + columnCounter + "=th column string missing value=" + string_datum);
                        dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                    } else {
                        dataRow[columnCounter] = escapeCharacterString(string_datum);
                    }
                    byte_offset += strVarLength;
                } else if (varType.equals("STRL")) {
                    logger.fine("STRL encountered.");
                    if (cachedGSOs == null) {
                        cachedGSOs = new LinkedHashMap<>();
                    }
                    long v = 0;
                    long o = 0;
                    String voPair = null;
                    v = reader.readInteger();
                    byte_offset += 4;
                    o = reader.readInteger();
                    byte_offset += 4;
                    voPair = v + "," + o;
                    dataRow[columnCounter] = voPair;
                    if (!(v == columnCounter + 1 && o == i + 1)) {
                        if (!cachedGSOs.containsKey(voPair)) {
                            cachedGSOs.put(voPair, "");
                        }
                    }
                } else {
                    logger.warning("unknown variable type found: " + varType);
                    String errorMessage = "unknown variable type encounted when reading data section: " + varType;
                    throw new IOException(errorMessage);
                }
            }
            if (byte_offset != bytes_per_row) {
                throw new IOException("Unexpected number of bytes read for data row " + i + "; " + bytes_per_row + " expected, " + byte_offset + " read.");
            }
            pwout.println(StringUtils.join(dataRow, "\t"));
            logger.fine("finished reading " + i + "-th row");
        }
        pwout.close();
        reader.readClosingTag(TAG_DATA);
        logger.fine("DTA117 Ingest: readData(): end.");
    }

    private void readSTRLs(DataReader reader) throws IOException {
        logger.fine("STRLs section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_strls());
        if (hasSTRLs) {
            reader.readOpeningTag(TAG_STRLS);
            File intermediateTabFile = ingesteddata.getTabDelimitedFile();
            FileInputStream fileInTab = new FileInputStream(intermediateTabFile);
            Scanner scanner = new Scanner(fileInTab);
            scanner.useDelimiter("\\n");
            File finalTabFile = File.createTempFile("finalTabfile.", ".tab");
            FileOutputStream fileOutTab = new FileOutputStream(finalTabFile);
            PrintWriter pwout = new PrintWriter(new OutputStreamWriter(fileOutTab, "utf8"), true);
            logger.fine("Setting the tab-delimited file to " + finalTabFile.getName());
            ingesteddata.setTabDelimitedFile(finalTabFile);
            int nvar = dataTable.getVarQuantity().intValue();
            int nobs = dataTable.getCaseQuantity().intValue();
            String[] line;
            for (int obsindex = 0; obsindex < nobs; obsindex++) {
                if (scanner.hasNext()) {
                    line = (scanner.next()).split("\t", -1);
                    for (int varindex = 0; varindex < nvar; varindex++) {
                        if ("STRL".equals(variableTypes[varindex])) {
                            String voPair = line[varindex];
                            long v;
                            long o;
                            if (voPair == null) {
                                throw new IOException("Failed to read an intermediate v,o Pair for variable " + varindex + ", observation " + obsindex);
                            }
                            if ("0,0".equals(voPair)) {
                                line[varindex] = "\"\"";
                            } else {
                                String[] voTokens = voPair.split(",", 2);
                                try {
                                    v = new Long(voTokens[0]).longValue();
                                    o = new Long(voTokens[1]).longValue();
                                } catch (NumberFormatException nfex) {
                                    throw new IOException("Illegal v,o value: " + voPair + " for variable " + varindex + ", observation " + obsindex);
                                }
                                if (v == varindex + 1 && o == obsindex + 1) {
                                    line[varindex] = readGSO(reader, v, o);
                                    if (line[varindex] == null) {
                                        throw new IOException("Failed to read GSO value for " + voPair);
                                    }
                                } else {
                                    if (cachedGSOs.get(voPair) != null && !cachedGSOs.get(voPair).equals("")) {
                                        line[varindex] = cachedGSOs.get(voPair);
                                    } else {
                                        throw new IOException("GSO string unavailable for v,o value " + voPair);
                                    }
                                }
                            }
                        }
                    }
                    pwout.println(StringUtils.join(line, "\t"));
                }
            }
            scanner.close();
            pwout.close();
            reader.readClosingTag(TAG_STRLS);
        } else {
            reader.readPrimitiveSection(TAG_STRLS);
        }
    }

    private String readGSO(DataReader reader, long v, long o) throws IOException {
        if (!reader.checkTag(STRL_GSO_HEAD)) {
            return null;
        }
        reader.readBytes(STRL_GSO_HEAD.length());
        long vStored = reader.readInteger();
        long oStored = reader.readInteger();
        String voPair = v + "," + o;
        if (vStored != v || oStored != o) {
            throw new IOException("GSO reading mismatch: expected v,o pair: " + voPair + ", found: " + vStored + "," + oStored);
        }
        short type = reader.readByte();
        boolean binary = false;
        if (type == 129) {
            logger.fine("STRL TYPE: binary");
            binary = true;
        } else if (type == 130) {
            logger.fine("STRL TYPE: ascii");
        } else {
            logger.warning("WARNING: unknown STRL type: " + type);
        }
        long length = reader.readInteger();
        logger.fine("Advertised length of the STRL: " + length);
        byte[] contents = reader.readBytes((int) length);
        String gsoString = null;
        if (binary) {
            gsoString = new String(contents, "utf8");
        } else {
            gsoString = new String(contents, 0, (int) length - 1, "US-ASCII");
        }
        logger.fine("GSO " + v + "," + o + ": " + gsoString);
        String escapedGsoString = escapeCharacterString(gsoString);
        if (cachedGSOs.containsKey(voPair)) {
            if (!"".equals(cachedGSOs.get(voPair))) {
                throw new IOException("Multiple GSO definitions for v,o " + voPair);
            }
            cachedGSOs.put(voPair, escapedGsoString);
        }
        return escapedGsoString;
    }

    private void readValueLabels(DataReader reader) throws IOException {
        logger.fine("Value Labels section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_vallabs());
        logger.fine("readValueLabels(): start.");
        reader.readOpeningTag(TAG_VALUE_LABELS);
        while (reader.checkTag("<" + TAG_VALUE_LABELS_LBL_DEF + ">")) {
            reader.readOpeningTag(TAG_VALUE_LABELS_LBL_DEF);
            long label_table_length = reader.readInteger();
            String label_table_name = reader.readString(33);
            reader.readBytes(3);
            long value_category_offset = 0;
            int number_of_categories = (int) reader.readInteger();
            long text_length = reader.readInteger();
            value_category_offset = 8;
            long[] value_label_offsets = new long[number_of_categories];
            long[] category_values = new long[number_of_categories];
            String[] category_value_labels = new String[number_of_categories];
            for (int i = 0; i < number_of_categories; i++) {
                value_label_offsets[i] = reader.readInteger();
                value_category_offset += 4;
            }
            for (int i = 0; i < number_of_categories; i++) {
                category_values[i] = reader.readInteger();
                value_category_offset += 4;
            }
            int total_label_bytes = 0;
            long label_offset = 0;
            long label_end = 0;
            int label_length = 0;
            boolean firstCategoryNonZeroOffsetMode = false;
            long firstCategoryLabelEnd = 0;
            for (int i = 0; i < number_of_categories; i++) {
                label_offset = value_label_offsets[i];
                label_end = i < number_of_categories - 1 ? value_label_offsets[i + 1] : text_length;
                if (number_of_categories == 2) {
                    if (i == 0 && label_offset != 0) {
                        logger.warning("The first label offset should always be zero!");
                        long nonZeroOffset = label_offset;
                        label_offset = 0;
                        label_end = nonZeroOffset;
                        firstCategoryNonZeroOffsetMode = true;
                        firstCategoryLabelEnd = label_end;
                    }
                    if (i == 1 && firstCategoryNonZeroOffsetMode) {
                        label_offset = firstCategoryLabelEnd;
                        label_end = text_length;
                    }
                }
                label_length = (int) (label_end - label_offset);
                category_value_labels[i] = reader.readString(label_length);
                total_label_bytes += label_length;
            }
            value_category_offset += total_label_bytes;
            if (total_label_bytes != text_length) {
                throw new IOException("<read mismatch in readLabels()>");
            }
            if (value_category_offset != label_table_length) {
                throw new IOException("<read mismatch in readLabels() 2>");
            }
            reader.readClosingTag(TAG_VALUE_LABELS_LBL_DEF);
            for (int i = 0; i < dataTable.getVarQuantity(); i++) {
                if (label_table_name.equals(valueLabelsLookupTable[i])) {
                    logger.fine("cross-linking value label table for " + label_table_name);
                    for (int j = 0; j < number_of_categories; j++) {
                        VariableCategory cat = new VariableCategory();
                        long cat_value = category_values[j];
                        String cat_label = category_value_labels[j];
                        cat.setValue("" + cat_value);
                        cat.setLabel(cat_label);
                        cat.setDataVariable(dataTable.getDataVariables().get(i));
                        dataTable.getDataVariables().get(i).getCategories().add(cat);
                    }
                }
            }
        }
        reader.readClosingTag(TAG_VALUE_LABELS);
        logger.fine("readValueLabels(): end.");
    }

    private int calculateBytesPerRow(int[] variableByteLengths) throws IOException {
        if (variableByteLengths == null || variableByteLengths.length != dataTable.getVarQuantity()) {
            throw new IOException("<internal variable byte offsets table not properly configured>");
        }
        int bytes_per_row = 0;
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            if (variableByteLengths[i] < 1) {
                throw new IOException("<bad variable byte offset: " + variableByteLengths[i] + ">");
            }
            bytes_per_row += variableByteLengths[i];
        }
        return bytes_per_row;
    }

    private int[] getVariableByteLengths(String[] variableTypes) throws IOException {
        if (variableTypes == null || variableTypes.length != dataTable.getVarQuantity()) {
            throw new IOException("<internal variable types not properly configured>");
        }
        int[] variableByteLengths = new int[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            variableByteLengths[i] = getVariableByteLength(variableTypes[i]);
        }
        return variableByteLengths;
    }

    private int getVariableByteLength(String variableType) throws IOException {
        int byte_length = 0;
        if (variableType == null || variableType.equals("")) {
            throw new IOException("<empty variable type in attempted byte length lookup.>");
        }
        if (byteLengthTable.containsKey(variableType)) {
            return byteLengthTable.get(variableType);
        }
        if (variableType.matches("^STR[1-9][0-9]*")) {
            String stringLengthToken = variableType.substring(3);
            Integer stringLength = null;
            try {
                stringLength = new Integer(stringLengthToken);
            } catch (NumberFormatException nfe) {
                stringLength = null;
            }
            if (stringLength == null || stringLength.intValue() < 1 || stringLength.intValue() > 2045) {
                throw new IOException("Invalid STRF encountered: " + variableType);
            }
            return stringLength.intValue();
        }
        throw new IOException("Unknown/invalid variable type: " + variableType);
    }

    private class DecodedDateTime {

        String format;

        String decodedDateTime;
    }

    private DecodedDateTime decodeDateTimeData(String storageType, String FormatType, String rawDatum) throws IOException {
        logger.fine("(storageType, FormatType, rawDatum)=(" + storageType + ", " + FormatType + ", " + rawDatum + ")");
        long milliSeconds;
        String decodedDateTime = null;
        String format = null;
        if (FormatType.matches("^%tc.*")) {
            milliSeconds = Long.parseLong(rawDatum) + STATA_BIAS_TO_EPOCH;
            decodedDateTime = sdf_ymdhmsS.format(new Date(milliSeconds));
            format = sdf_ymdhmsS.toPattern();
            logger.fine("tc: result=" + decodedDateTime + ", format = " + format);
        } else if (FormatType.matches("^%t?d.*")) {
            milliSeconds = Long.parseLong(rawDatum) * SECONDS_PER_YEAR + STATA_BIAS_TO_EPOCH;
            logger.fine("milliSeconds=" + milliSeconds);
            decodedDateTime = sdf_ymd.format(new Date(milliSeconds));
            format = sdf_ymd.toPattern();
            logger.fine("td:" + decodedDateTime + ", format = " + format);
        } else if (FormatType.matches("^%t?w.*")) {
            long weekYears = Long.parseLong(rawDatum);
            long left = Math.abs(weekYears) % 52L;
            long years;
            if (weekYears < 0L) {
                left = 52L - left;
                if (left == 52L) {
                    left = 0L;
                }
                years = (Math.abs(weekYears) - 1) / 52L + 1L;
                years *= -1L;
            } else {
                years = weekYears / 52L;
            }
            String yearString = Long.valueOf(1960L + years).toString();
            String dayInYearString = new DecimalFormat("000").format((left * 7) + 1).toString();
            String yearDayInYearString = yearString + "-" + dayInYearString;
            Date tempDate = null;
            try {
                tempDate = new SimpleDateFormat("yyyy-DDD").parse(yearDayInYearString);
            } catch (ParseException ex) {
                throw new IOException(ex);
            }
            decodedDateTime = sdf_ymd.format(tempDate.getTime());
            format = sdf_ymd.toPattern();
        } else if (FormatType.matches("^%t?m.*")) {
            long monthYears = Long.parseLong(rawDatum);
            long left = Math.abs(monthYears) % 12L;
            long years;
            if (monthYears < 0L) {
                left = 12L - left;
                years = (Math.abs(monthYears) - 1) / 12L + 1L;
                years *= -1L;
            } else {
                years = monthYears / 12L;
            }
            String month = null;
            if (left == 12L) {
                left = 0L;
            }
            Long monthdata = (left + 1);
            month = "-" + twoDigitFormatter.format(monthdata).toString() + "-01";
            long year = 1960L + years;
            String monthYear = Long.valueOf(year).toString() + month;
            logger.fine("rawDatum=" + rawDatum + ": monthYear=" + monthYear);
            decodedDateTime = monthYear;
            format = "yyyy-MM-dd";
            logger.fine("tm:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?q.*")) {
            long quaterYears = Long.parseLong(rawDatum);
            long left = Math.abs(quaterYears) % 4L;
            long years;
            if (quaterYears < 0L) {
                left = 4L - left;
                years = (Math.abs(quaterYears) - 1) / 4L + 1L;
                years *= -1L;
            } else {
                years = quaterYears / 4L;
            }
            String quater = null;
            if ((left == 0L) || (left == 4L)) {
                quater = "-01-01";
            } else if (left == 1L) {
                quater = "-04-01";
            } else if (left == 2L) {
                quater = "-07-01";
            } else if (left == 3L) {
                quater = "-11-01";
            }
            long year = 1960L + years;
            String quaterYear = Long.valueOf(year).toString() + quater;
            logger.fine("rawDatum=" + rawDatum + ": quaterYear=" + quaterYear);
            decodedDateTime = quaterYear;
            format = "yyyy-MM-dd";
            logger.fine("tq:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?h.*")) {
            long halvesYears = Long.parseLong(rawDatum);
            long left = Math.abs(halvesYears) % 2L;
            long years;
            if (halvesYears < 0L) {
                years = (Math.abs(halvesYears) - 1) / 2L + 1L;
                years *= -1L;
            } else {
                years = halvesYears / 2L;
            }
            String half = null;
            if (left != 0L) {
                half = "-07-01";
            } else {
                half = "-01-01";
            }
            long year = 1960L + years;
            String halfYear = Long.valueOf(year).toString() + half;
            logger.fine("rawDatum=" + rawDatum + ": halfYear=" + halfYear);
            decodedDateTime = halfYear;
            format = "yyyy-MM-dd";
            logger.fine("th:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?y.*")) {
            decodedDateTime = rawDatum;
            format = "yyyy";
            logger.fine("th:" + decodedDateTime);
        } else {
            decodedDateTime = rawDatum;
            format = null;
        }
        DecodedDateTime retValue = new DecodedDateTime();
        retValue.decodedDateTime = decodedDateTime;
        retValue.format = format;
        return retValue;
    }

    private class DataReader {

        private BufferedInputStream stream;

        private int DEFAULT_BUFFER_SIZE = 8192;

        private byte[] byte_buffer;

        private int buffer_size;

        private long byte_offset;

        private int buffer_byte_offset;

        Boolean LSF = null;

        public DataReader(BufferedInputStream stream) throws IOException {
            this(stream, 0);
        }

        public DataReader(BufferedInputStream stream, int size) throws IOException {
            if (buffer_size > 0) {
                this.DEFAULT_BUFFER_SIZE = size;
            }
            this.stream = stream;
            byte_buffer = new byte[DEFAULT_BUFFER_SIZE];
            byte_offset = 0;
            buffer_byte_offset = 0;
            bufferMoreBytes();
        }

        public BufferedInputStream getStream() {
            return stream;
        }

        public void setStream(BufferedInputStream stream) {
            this.stream = stream;
        }

        public void setLSF(boolean lsf) {
            LSF = lsf;
        }

        public Boolean isLSF() {
            return LSF;
        }

        public long getByteOffset() {
            return byte_offset + buffer_byte_offset;
        }

        public void setByteOffset(long byte_offset) {
            this.byte_offset = byte_offset;
        }

        public byte[] readBytes(int n) throws IOException {
            if (n <= 0) {
                throw new IOException("DataReader.readBytes called to read zero or negative number of bytes.");
            }
            byte[] bytes = new byte[n];
            if (this.buffer_size - buffer_byte_offset >= n) {
                System.arraycopy(byte_buffer, buffer_byte_offset, bytes, 0, n);
                buffer_byte_offset += n;
            } else {
                int bytes_read = 0;
                if (this.buffer_size - buffer_byte_offset > 0) {
                    logger.fine("reading the remaining " + (this.buffer_size - buffer_byte_offset) + " bytes from the buffer");
                    System.arraycopy(byte_buffer, buffer_byte_offset, bytes, 0, this.buffer_size - buffer_byte_offset);
                    bytes_read = this.buffer_size - buffer_byte_offset;
                }
                int morebytes = bufferMoreBytes();
                logger.fine("buffered " + morebytes + " bytes");
                while (n - bytes_read > this.buffer_size) {
                    logger.fine("copying a full buffer-worth of bytes into the return array");
                    System.arraycopy(byte_buffer, buffer_byte_offset, bytes, bytes_read, this.buffer_size);
                    bytes_read += this.buffer_size;
                    morebytes = bufferMoreBytes();
                    logger.fine("buffered " + morebytes + " bytes");
                }
                logger.fine("copying the remaining " + (n - bytes_read) + " bytes.");
                System.arraycopy(byte_buffer, 0, bytes, bytes_read, n - bytes_read);
                buffer_byte_offset = n - bytes_read;
            }
            return bytes;
        }

        private int bufferMoreBytes() throws IOException {
            int actual_bytes_read = stream.read(byte_buffer, 0, DEFAULT_BUFFER_SIZE);
            this.buffer_size = actual_bytes_read;
            byte_offset += buffer_byte_offset;
            buffer_byte_offset = 0;
            return actual_bytes_read;
        }

        private byte readSignedByte() throws IOException {
            byte ret;
            if (buffer_byte_offset > this.buffer_size) {
                throw new IOException("TD - buffer overflow");
            }
            if (buffer_byte_offset < this.buffer_size) {
                ret = byte_buffer[buffer_byte_offset];
                buffer_byte_offset++;
            } else {
                if (bufferMoreBytes() < 1) {
                    throw new IOException("reached the end of data stream prematurely.");
                }
                ret = byte_buffer[0];
                buffer_byte_offset = 1;
            }
            return ret;
        }

        private short readByte() throws IOException {
            short ret = readSignedByte();
            if (ret < 0) {
                ret += 256;
            }
            return ret;
        }

        public long readInteger() throws IOException {
            return readInteger(4);
        }

        public int readSignedInteger() throws IOException {
            return readSignedInteger(4);
        }

        public int readShortInteger() throws IOException {
            return readInteger(2);
        }

        public short readShortSignedInteger() throws IOException {
            return (short) readSignedInteger(2);
        }

        public long readLongInteger() throws IOException {
            byte[] raw_bytes = readBytes(8);
            return bytesToLong(raw_bytes);
        }

        private int readInteger(int n) throws IOException {
            byte[] raw_bytes = readBytes(n);
            return (int) bytesToInt(raw_bytes);
        }

        private int readSignedInteger(int n) throws IOException {
            byte[] raw_bytes = readBytes(n);
            return bytesToSignedInt(raw_bytes);
        }

        public double readDouble() throws IOException {
            if (LSF == null) {
                throw new IOException("Byte order not determined for reading numeric values.");
            }
            ByteBuffer double_buffer = ByteBuffer.wrap(readBytes(8));
            if (LSF) {
                double_buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            double ret = double_buffer.getDouble();
            return ret;
        }

        public float readFloat() throws IOException {
            ByteBuffer float_buffer = ByteBuffer.wrap(readBytes(4));
            float_buffer.order(ByteOrder.LITTLE_ENDIAN);
            float ret = float_buffer.getFloat();
            return ret;
        }

        private long bytesToInt(byte[] raw_bytes) throws IOException {
            if (LSF == null) {
                throw new IOException("Byte order not determined for reading numeric values.");
            }
            int n = raw_bytes.length;
            if (n != 2 && n != 4) {
                throw new IOException("Unsupported number of bytes in an integer: " + n);
            }
            long ret = 0;
            short unsigned_byte_value = 0;
            for (int i = 0; i < n; i++) {
                if (LSF) {
                    unsigned_byte_value = raw_bytes[i];
                } else {
                    unsigned_byte_value = raw_bytes[n - i - 1];
                }
                if (unsigned_byte_value < 0) {
                    unsigned_byte_value += 256;
                }
                ret += unsigned_byte_value * (1 << (8 * i));
            }
            return ret;
        }

        private int bytesToSignedInt(byte[] raw_bytes) throws IOException {
            if (LSF == null) {
                throw new IOException("Byte order not determined for reading numeric values.");
            }
            int n = raw_bytes.length;
            ByteBuffer byte_buffer = ByteBuffer.wrap(raw_bytes);
            if (LSF) {
                byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            int int_value;
            if (n == 2) {
                int_value = byte_buffer.getShort();
            } else if (n == 4) {
                int_value = byte_buffer.getInt();
            } else {
                throw new IOException("Unsupported number of bytes for signed integer: " + n);
            }
            return int_value;
        }

        private long bytesToLong(byte[] raw_bytes) throws IOException {
            if (raw_bytes.length != 8) {
                throw new IOException("Wrong number of bytes in bytesToLong().");
            }
            if (LSF == null) {
                throw new IOException("Byte order not determined for reading numeric values.");
            }
            long ret = 0;
            ByteBuffer byte_buffer = ByteBuffer.wrap(raw_bytes);
            if (LSF) {
                byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            ret = byte_buffer.getLong();
            return ret;
        }

        public String readString(int n) throws IOException {
            String ret = new String(readBytes(n), "US-ASCII");
            if (ret != null && ret.indexOf(0) > -1) {
                return ret.substring(0, ret.indexOf(0));
            }
            return ret;
        }

        public byte[] readPrimitiveSection(String tag) throws IOException {
            readOpeningTag(tag);
            byte[] ret = readPrimitiveSectionBytes();
            readClosingTag(tag);
            return ret;
        }

        public byte[] readPrimitiveSection(String tag, int length) throws IOException {
            readOpeningTag(tag);
            byte[] ret = readBytes(length);
            readClosingTag(tag);
            return ret;
        }

        public String readPrimitiveStringSection(String tag) throws IOException {
            return new String(readPrimitiveSection(tag), "US-ASCII");
        }

        public String readPrimitiveStringSection(String tag, int length) throws IOException {
            return new String(readPrimitiveSection(tag, length), "US-ASCII");
        }

        public String readDefinedStringSection(String tag, int limit) throws IOException {
            readOpeningTag(tag);
            short number = readByte();
            if (number < 0 || number > limit) {
                throw new IOException("<more than limit characters in the section \"tag\">");
            }
            String ret = null;
            if (number > 0) {
                ret = new String(readBytes(number), "US-ASCII");
            }
            readClosingTag(tag);
            return ret;
        }

        public int readIntegerSection(String tag, int n) throws IOException {
            readOpeningTag(tag);
            int number = readInteger(n);
            readClosingTag(tag);
            return number;
        }

        public void skipDefinedSections(String tag) throws IOException {
            logger.fine("entering at offset " + buffer_byte_offset);
            while (checkTag("<" + tag + ">")) {
                logger.fine("tag " + tag + " encountered at offset " + buffer_byte_offset);
                readOpeningTag(tag);
                long number = readInteger(4);
                logger.fine(number + " bytes in this section;");
                if (number < 0) {
                    throw new IOException("<negative number of bytes in skipDefinedSection(\"tag\")?>");
                }
                byte[] skipped_bytes = readBytes((int) number);
                readClosingTag(tag);
                logger.fine("read closing tag </" + tag + ">;");
            }
            logger.fine("exiting at offset " + buffer_byte_offset);
        }

        private boolean checkTag(String tag) throws IOException {
            if (tag == null || tag.equals("")) {
                throw new IOException("opening tag must be a non-empty string.");
            }
            int n = tag.length();
            if (this.buffer_size - buffer_byte_offset >= n) {
                return (tag).equals(new String(Arrays.copyOfRange(byte_buffer, buffer_byte_offset, buffer_byte_offset + n), "US-ASCII"));
            } else {
                throw new IOException("Checking section tags across byte buffers not yet implemented.");
            }
        }

        public void readOpeningTag(String tag) throws IOException {
            if (tag == null || tag.equals("")) {
                throw new IOException("opening tag must be a non-empty string.");
            }
            byte[] openTag = readBytes(tag.length() + 2);
            String openTagString = new String(openTag, "US-ASCII");
            if (openTagString == null || !openTagString.equals("<" + tag + ">")) {
                throw new IOException("Could not read opening tag <" + tag + ">");
            }
        }

        public void readClosingTag(String tag) throws IOException {
            if (tag == null || tag.equals("")) {
                throw new IOException("closing tag must be a non-empty string.");
            }
            byte[] closeTag = readBytes(tag.length() + 3);
            String closeTagString = new String(closeTag, "US-ASCII");
            if (closeTagString == null || !closeTagString.equals("</" + tag + ">")) {
                throw new IOException("Could not read closing tag </" + tag + ">");
            }
        }

        private byte[] readPrimitiveSectionBytes() throws IOException {
            byte[] cached_bytes = null;
            if (buffer_byte_offset > this.buffer_size) {
                throw new IOException("Buffer overflow in DataReader.");
            }
            if (buffer_byte_offset == this.buffer_size) {
                bufferMoreBytes();
            }
            int cached_offset = buffer_byte_offset;
            while (byte_buffer[buffer_byte_offset] != '<') {
                buffer_byte_offset++;
                if (buffer_byte_offset == this.buffer_size) {
                    logger.fine("reached the end of buffer in readPrimitiveSectionBytes; offset " + buffer_byte_offset);
                    cached_bytes = mergeCachedBytes(cached_bytes, cached_offset);
                    bufferMoreBytes();
                    cached_offset = 0;
                }
            }
            return mergeCachedBytes(cached_bytes, cached_offset);
        }

        private byte[] mergeCachedBytes(byte[] cached_bytes, int cached_offset) throws IOException {
            byte[] ret_bytes;
            if (cached_bytes == null) {
                if (buffer_byte_offset - cached_offset < 0) {
                    throw new IOException("read error in save local buffer 1; TODO: better exception message");
                }
                if (buffer_byte_offset - cached_offset == 0) {
                    return null;
                }
                ret_bytes = new byte[buffer_byte_offset - cached_offset];
                System.arraycopy(byte_buffer, cached_offset, ret_bytes, 0, buffer_byte_offset - cached_offset);
            } else {
                if (cached_offset != 0) {
                    throw new IOException("read error in save local buffer 2; TODO: better exception message");
                }
                ret_bytes = new byte[cached_bytes.length + buffer_byte_offset];
                System.arraycopy(cached_bytes, 0, ret_bytes, 0, cached_bytes.length);
                if (buffer_byte_offset > 0) {
                    System.arraycopy(byte_buffer, 0, ret_bytes, cached_bytes.length, buffer_byte_offset);
                }
            }
            return ret_bytes;
        }
    }

    private class DTADataMap {

        private long dta_offset_stata_data = 0;

        private long dta_offset_map = 0;

        private long dta_offset_variable_types = 0;

        private long dta_offset_varnames = 0;

        private long dta_offset_sortlist = 0;

        private long dta_offset_formats = 0;

        private long dta_offset_value_label_names = 0;

        private long dta_offset_variable_labels = 0;

        private long dta_offset_characteristics = 0;

        private long dta_offset_data = 0;

        private long dta_offset_strls = 0;

        private long dta_offset_value_labels = 0;

        private long dta_offset_data_close = 0;

        private long dta_offset_eof = 0;

        public long getOffset_head() {
            return dta_offset_stata_data;
        }

        public long getOffset_map() {
            return dta_offset_map;
        }

        public long getOffset_types() {
            return dta_offset_variable_types;
        }

        public long getOffset_varnames() {
            return dta_offset_varnames;
        }

        public long getOffset_srtlist() {
            return dta_offset_sortlist;
        }

        public long getOffset_fmts() {
            return dta_offset_formats;
        }

        public long getOffset_vlblnames() {
            return dta_offset_value_label_names;
        }

        public long getOffset_varlabs() {
            return dta_offset_variable_labels;
        }

        public long getOffset_characteristics() {
            return dta_offset_characteristics;
        }

        public long getOffset_data() {
            return dta_offset_data;
        }

        public long getOffset_strls() {
            return dta_offset_strls;
        }

        public long getOffset_vallabs() {
            return dta_offset_value_labels;
        }

        public long getOffset_data_close() {
            return dta_offset_data_close;
        }

        public long getOffset_eof() {
            return dta_offset_eof;
        }

        public void setOffset_head(long dta_offset_stata_data) {
            this.dta_offset_stata_data = dta_offset_stata_data;
        }

        public void setOffset_map(long dta_offset_map) {
            this.dta_offset_map = dta_offset_map;
        }

        public void setOffset_types(long dta_offset_variable_types) {
            this.dta_offset_variable_types = dta_offset_variable_types;
        }

        public void setOffset_varnames(long dta_offset_varnames) {
            this.dta_offset_varnames = dta_offset_varnames;
        }

        public void setOffset_srtlist(long dta_offset_sortlist) {
            this.dta_offset_sortlist = dta_offset_sortlist;
        }

        public void setOffset_fmts(long dta_offset_formats) {
            this.dta_offset_formats = dta_offset_formats;
        }

        public void setOffset_vlblnames(long dta_offset_value_label_names) {
            this.dta_offset_value_label_names = dta_offset_value_label_names;
        }

        public void setOffset_varlabs(long dta_offset_variable_labels) {
            this.dta_offset_variable_labels = dta_offset_variable_labels;
        }

        public void setOffset_characteristics(long dta_offset_characteristics) {
            this.dta_offset_characteristics = dta_offset_characteristics;
        }

        public void setOffset_data(long dta_offset_data) {
            this.dta_offset_data = dta_offset_data;
        }

        public void setOffset_strls(long dta_offset_strls) {
            this.dta_offset_strls = dta_offset_strls;
        }

        public void setOffset_vallabs(long dta_offset_value_labels) {
            this.dta_offset_value_labels = dta_offset_value_labels;
        }

        public void setOffset_data_close(long dta_offset_data_close) {
            this.dta_offset_data_close = dta_offset_data_close;
        }

        public void setOffset_eof(long dta_offset_eof) {
            this.dta_offset_eof = dta_offset_eof;
        }
    }
}
