package edu.harvard.iq.dataverse.ingest.tabulardata.impl.plugins.dta;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;
import edu.harvard.iq.dataverse.DataTable;
import edu.harvard.iq.dataverse.datavariable.DataVariable;
import edu.harvard.iq.dataverse.datavariable.VariableCategory;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataFileReader;
import edu.harvard.iq.dataverse.ingest.tabulardata.spi.TabularDataFileReaderSpi;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataIngest;
import org.apache.commons.lang3.StringUtils;

public class NewDTAFileReader extends TabularDataFileReader {

    private static final String TAG_DTA = "stata_dta";

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

    private static Map<String, Integer> releaseconstant = new LinkedHashMap<String, Integer>();

    private static Map<String, Integer> byteLengthTable = new HashMap<String, Integer>();

    private static Map<Integer, String> variableTypeTable = new LinkedHashMap<Integer, String>();

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
        releaseconstant.put("HEADER", LENGTH_HEADER[1]);
        releaseconstant.put("LABEL", LENGTH_LABEL[1]);
        releaseconstant.put("NAME", LENGTH_NAME[1]);
        releaseconstant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        releaseconstant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        releaseconstant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        byteLengthTable.put("Byte", 1);
        byteLengthTable.put("Integer", 2);
        byteLengthTable.put("Long", 4);
        byteLengthTable.put("Float", 4);
        byteLengthTable.put("Double", 8);
        byteLengthTable.put("STRL", 8);
        variableTypeTable.put(65530, "Byte");
        variableTypeTable.put(65529, "Integer");
        variableTypeTable.put(65528, "Long");
        variableTypeTable.put(65527, "Float");
        variableTypeTable.put(65526, "Double");
    }

    private static String unfVersionNumber = "6";

    private static final List<Float> FLOAT_MISSING_VALUES = Arrays.asList(0x1.000p127f, 0x1.001p127f, 0x1.002p127f, 0x1.003p127f, 0x1.004p127f, 0x1.005p127f, 0x1.006p127f, 0x1.007p127f, 0x1.008p127f, 0x1.009p127f, 0x1.00ap127f, 0x1.00bp127f, 0x1.00cp127f, 0x1.00dp127f, 0x1.00ep127f, 0x1.00fp127f, 0x1.010p127f, 0x1.011p127f, 0x1.012p127f, 0x1.013p127f, 0x1.014p127f, 0x1.015p127f, 0x1.016p127f, 0x1.017p127f, 0x1.018p127f, 0x1.019p127f, 0x1.01ap127f);

    private Set<Float> FLOAT_MISSING_VALUE_SET = new HashSet<>(FLOAT_MISSING_VALUES);

    private static final List<Double> DOUBLE_MISSING_VALUE_LIST = Arrays.asList(0x1.000p1023, 0x1.001p1023, 0x1.002p1023, 0x1.003p1023, 0x1.004p1023, 0x1.005p1023, 0x1.006p1023, 0x1.007p1023, 0x1.008p1023, 0x1.009p1023, 0x1.00ap1023, 0x1.00bp1023, 0x1.00cp1023, 0x1.00dp1023, 0x1.00ep1023, 0x1.00fp1023, 0x1.010p1023, 0x1.011p1023, 0x1.012p1023, 0x1.013p1023, 0x1.014p1023, 0x1.015p1023, 0x1.016p1023, 0x1.017p1023, 0x1.018p1023, 0x1.019p1023, 0x1.01ap1023);

    private Set<Double> DOUBLE_MISSING_VALUE_SET = new HashSet<>(DOUBLE_MISSING_VALUE_LIST);

    private static SimpleDateFormat sdf_ymdhmsS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat sdf_hms = new SimpleDateFormat("HH:mm:ss");

    private static SimpleDateFormat sdf_yw = new SimpleDateFormat("yyyy-'W'ww");

    private static Calendar GCO_STATA = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    private static String[] DATE_TIME_FORMAT = { "%tc", "%td", "%tw", "%tq", "%tm", "%th", "%ty", "%d", "%w", "%q", "%m", "h", "%tb" };

    private static String[] DATE_TIME_CATEGORY = { "time", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date", "date" };

    private static Map<String, String> DATE_TIME_FORMAT_TABLE = new LinkedHashMap<String, String>();

    private static long MILLISECCONDS_PER_DAY = 24 * 60 * 60 * 1000L;

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

    private Map<String, String> cachedGSOs;

    private NumberFormat twoDigitFormatter = new DecimalFormat("00");

    private NumberFormat doubleNumberFormatter = new DecimalFormat();

    TabularDataIngest ingesteddata = new TabularDataIngest();

    private int DTAVersion;

    private int headerLength;

    private int dataLabelLength;

    private boolean hasSTRLs = false;

    private String[] variableTypes = null;

    private String[] dateVariableFormats = null;

    private static final String MissingValueForTabDelimitedFile = "";

    private String[] MIME_TYPE = { "application/x-stata", "application/x-stata-13", "application/x-stata-14", "application/x-stata-15" };

    public NewDTAFileReader(TabularDataFileReaderSpi originator, int DTAVersion) {
        super(originator);
        this.DTAVersion = DTAVersion;
        STATA_RELEASE_NUMBER.put(DTAVersion, "v." + (DTAVersion - 104));
        CONSTANT_TABLE.put(DTAVersion, releaseconstant);
    }

    private void init() throws IOException {
        logger.fine("release number=" + DTAVersion);
        BYTE_MISSING_VALUE -= MISSING_VALUE_BIAS;
        INT_MISSIG_VALUE -= MISSING_VALUE_BIAS;
        LONG_MISSING_VALUE -= MISSING_VALUE_BIAS;
        constantTable = CONSTANT_TABLE.get(DTAVersion);
        headerLength = constantTable.get("HEADER") - DTA_MAGIC_NUMBER_LENGTH;
        dataLabelLength = headerLength - (NVAR_FIELD_LENGTH + NOBS_FIELD_LENGTH + TIME_STAMP_LENGTH);
        logger.fine("data_label_length=" + dataLabelLength);
        logger.fine("constant table to be used:\n" + constantTable);
        doubleNumberFormatter.setGroupingUsed(false);
        doubleNumberFormatter.setMaximumFractionDigits(340);
    }

    @Override
    public TabularDataIngest read(BufferedInputStream stream, File dataFile) throws IOException {
        logger.fine("NewDTAFileReader: read() start");
        if (dataFile != null) {
            throw new IOException("this plugin does not support external raw data files");
        }
        DataReader dataReader;
        init();
        dataReader = new DataReader(stream);
        dataReader.readOpeningTag(TAG_DTA);
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
        dataReader.readClosingTag(TAG_DTA);
        ingesteddata.setDataTable(dataTable);
        logger.fine("NewDTAFileReader: read() end.");
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
        if (!("117".equals(dtaVersionTag) || "118".equals(dtaVersionTag) || "119".equals(dtaVersionTag))) {
            throw new IOException("Unexpected version tag found: " + dtaVersionTag + "; expected value: 117-119.");
        }
        String byteOrderTag = dataReader.readPrimitiveStringSection(TAG_HEADER_BYTEORDER);
        logger.fine("byte order: " + byteOrderTag);
        dataReader.setLSF("LSF".equals(byteOrderTag));
        long varNumber = dataReader.readIntegerSection(TAG_HEADER_VARNUMBER, DTAVersion == 119 ? 4 : 2);
        logger.fine("number of variables: " + varNumber);
        long obsNumber = dataReader.readIntegerSection(TAG_HEADER_OBSNUMBER, DTAVersion == 117 ? 4 : 8);
        logger.fine("number of observations: " + obsNumber);
        dataTable.setVarQuantity(varNumber);
        dataTable.setCaseQuantity(obsNumber);
        dataTable.setOriginalFileFormat(MIME_TYPE[0]);
        dataTable.setOriginalFormatVersion("STATA " + (DTAVersion - 104));
        dataTable.setUnf("UNF:pending");
        String datasetLabel;
        if (DTAVersion == 117) {
            datasetLabel = dataReader.readDefinedStringSection(TAG_HEADER_FILELABEL, 80);
        } else {
            datasetLabel = dataReader.readLabelSection(TAG_HEADER_FILELABEL, 320);
        }
        logger.fine("Stata \"dataset\" label: " + datasetLabel);
        String datasetTimeStamp = dataReader.readDefinedStringSection(TAG_HEADER_TIMESTAMP, 17);
        logger.fine("dataset time stamp: " + datasetTimeStamp);
        if (datasetTimeStamp == null || (datasetTimeStamp.length() > 0 && datasetTimeStamp.length() < 17)) {
            throw new IOException("unexpected/invalid length of the time stamp in the NewDTA header.");
        } else {
        }
        dataReader.readClosingTag("header");
        logger.fine("readHeader(): end");
    }

    private void readMap(DataReader reader) throws IOException {
        logger.fine("Map section; at offset " + reader.getByteOffset());
        reader.readOpeningTag(TAG_MAP);
        dtaMap = new DTADataMap();
        long dta_offset_stata_data = reader.readULong();
        logger.fine("dta_offset_stata_data: " + dta_offset_stata_data);
        dtaMap.setOffset_head(dta_offset_stata_data);
        long dta_offset_map = reader.readULong();
        logger.fine("dta_offset_map: " + dta_offset_map);
        dtaMap.setOffset_map(dta_offset_map);
        long dta_offset_variable_types = reader.readULong();
        logger.fine("dta_offset_variable_types: " + dta_offset_variable_types);
        dtaMap.setOffset_types(dta_offset_variable_types);
        long dta_offset_varnames = reader.readULong();
        logger.fine("dta_offset_varnames: " + dta_offset_varnames);
        dtaMap.setOffset_varnames(dta_offset_varnames);
        long dta_offset_sortlist = reader.readULong();
        logger.fine("dta_offset_sortlist: " + dta_offset_sortlist);
        dtaMap.setOffset_srtlist(dta_offset_sortlist);
        long dta_offset_formats = reader.readULong();
        logger.fine("dta_offset_formats: " + dta_offset_formats);
        dtaMap.setOffset_fmts(dta_offset_formats);
        long dta_offset_value_label_names = reader.readULong();
        logger.fine("dta_offset_value_label_names: " + dta_offset_value_label_names);
        dtaMap.setOffset_vlblnames(dta_offset_value_label_names);
        long dta_offset_variable_labels = reader.readULong();
        logger.fine("dta_offset_variable_labels: " + dta_offset_variable_labels);
        dtaMap.setOffset_varlabs(dta_offset_variable_labels);
        long dta_offset_characteristics = reader.readULong();
        logger.fine("dta_offset_characteristics: " + dta_offset_characteristics);
        dtaMap.setOffset_characteristics(dta_offset_characteristics);
        long dta_offset_data = reader.readULong();
        logger.fine("dta_offset_data: " + dta_offset_data);
        dtaMap.setOffset_data(dta_offset_data);
        long dta_offset_strls = reader.readULong();
        logger.fine("dta_offset_strls: " + dta_offset_strls);
        dtaMap.setOffset_strls(dta_offset_strls);
        long dta_offset_value_labels = reader.readULong();
        logger.fine("dta_offset_value_labels: " + dta_offset_value_labels);
        dtaMap.setOffset_vallabs(dta_offset_value_labels);
        long dta_offset_data_close = reader.readULong();
        logger.fine("dta_offset_data_close: " + dta_offset_data_close);
        dtaMap.setOffset_data_close(dta_offset_data_close);
        long dta_offset_eof = reader.readULong();
        logger.fine("dta_offset_eof: " + dta_offset_eof);
        dtaMap.setOffset_eof(dta_offset_eof);
        reader.readClosingTag(TAG_MAP);
    }

    private void readVariableTypes(DataReader reader) throws IOException {
        logger.fine("Type section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_types());
        reader.readOpeningTag(TAG_VARIABLE_TYPES);
        List<DataVariable> variableList = new ArrayList<>();
        variableTypes = new String[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            int type = reader.readUShort();
            logger.fine("variable " + i + ": type=" + type);
            DataVariable dv = new DataVariable(i, dataTable);
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
            switch(typeLabel) {
                case "Byte":
                case "Integer":
                case "Long":
                    dv.setIntervalDiscrete();
                    break;
                case "Float":
                case "Double":
                    dv.setIntervalContinuous();
                    break;
                default:
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
            String variableName = reader.readString(DTAVersion == 117 ? 33 : 129);
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
            long order = reader.readULong(DTAVersion == 119 ? 4 : 2);
            logger.fine("variable " + i + ": sort order=" + order);
        }
        long terminatingShort = reader.readULong(DTAVersion == 119 ? 4 : 2);
        reader.readClosingTag(TAG_SORT_ORDER);
    }

    private void readDisplayFormats(DataReader reader) throws IOException {
        logger.fine("Formats section; at offset " + reader.getByteOffset() + "; dta map offset: " + dtaMap.getOffset_fmts());
        reader.readOpeningTag(TAG_DISPLAY_FORMATS);
        dateVariableFormats = new String[dataTable.getVarQuantity().intValue()];
        for (int i = 0; i < dataTable.getVarQuantity(); i++) {
            String variableFormat = reader.readString(DTAVersion == 117 ? 49 : 57);
            logger.fine("variable " + i + ": displayFormat=" + variableFormat);
            String variableFormatKey;
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
            String valueLabelFormat = reader.readString(DTAVersion == 117 ? 33 : 129);
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
            String variableLabel = reader.readString(DTAVersion == 117 ? 81 : 321);
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
        File tabDelimitedDataFile = File.createTempFile("tempTabfile.", ".tab");
        ingesteddata.setTabDelimitedFile(tabDelimitedDataFile);
        FileOutputStream fileOutTab = new FileOutputStream(tabDelimitedDataFile);
        PrintWriter pwout = new PrintWriter(new OutputStreamWriter(fileOutTab, "utf8"), true);
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
                    byte byte_datum = reader.readByte();
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
                    short short_datum = (short) reader.readShort();
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
                    int int_datum = reader.readInt();
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
                    long v;
                    long o;
                    if (DTAVersion == 117) {
                        v = reader.readUInt();
                        byte_offset += 4;
                        o = reader.readUInt();
                        byte_offset += 4;
                    } else {
                        v = reader.readUShort();
                        byte_offset += 2;
                        o = reader.readULong(6);
                        byte_offset += 6;
                    }
                    String voPair = v + "," + o;
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
        logger.fine("NewDTA Ingest: readData(): end.");
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
                                    v = new Long(voTokens[0]);
                                    o = new Long(voTokens[1]);
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
        long vStored = reader.readUInt();
        long oStored = reader.readULong(DTAVersion == 117 ? 4 : 8);
        String voPair = v + "," + o;
        if (vStored != v || oStored != o) {
            throw new IOException("GSO reading mismatch: expected v,o pair: " + voPair + ", found: " + vStored + "," + oStored);
        }
        short type = reader.readUByte();
        boolean binary = false;
        if (type == 129) {
            logger.fine("STRL TYPE: binary");
            binary = true;
        } else if (type == 130) {
            logger.fine("STRL TYPE: ascii");
        } else {
            logger.warning("WARNING: unknown STRL type: " + type);
        }
        long length = reader.readUInt();
        logger.fine("Advertised length of the STRL: " + length);
        byte[] contents = reader.readBytes((int) length);
        String gsoString;
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
            long label_table_length = reader.readUInt();
            String label_table_name = reader.readString(DTAVersion == 117 ? 33 : 129);
            reader.readBytes(3);
            int number_of_categories = (int) reader.readUInt();
            long text_length = reader.readUInt();
            long value_category_offset = 8;
            long[] value_label_offsets = new long[number_of_categories];
            long[] value_label_offsets_sorted = null;
            long[] category_values = new long[number_of_categories];
            String[] category_value_labels = new String[number_of_categories];
            boolean alreadySorted = true;
            for (int i = 0; i < number_of_categories; i++) {
                value_label_offsets[i] = reader.readUInt();
                logger.fine("offset " + i + ": " + value_label_offsets[i]);
                value_category_offset += 4;
                if (i > 0 && value_label_offsets[i] < value_label_offsets[i - 1]) {
                    alreadySorted = false;
                }
            }
            if (!alreadySorted) {
                value_label_offsets_sorted = Arrays.copyOf(value_label_offsets, number_of_categories);
                Arrays.sort(value_label_offsets_sorted);
            }
            for (int i = 0; i < number_of_categories; i++) {
                category_values[i] = reader.readInt();
                value_category_offset += 4;
            }
            int total_label_bytes = 0;
            long label_offset;
            long label_end;
            int label_length;
            byte[] labelBytes = null;
            if ((int) text_length != 0) {
                labelBytes = reader.readBytes((int) text_length);
            }
            for (int i = 0; i < number_of_categories; i++) {
                label_offset = value_label_offsets[i];
                if (value_label_offsets_sorted == null) {
                    label_end = i < number_of_categories - 1 ? value_label_offsets[i + 1] : text_length;
                } else {
                    int sortedPos = Arrays.binarySearch(value_label_offsets_sorted, label_offset);
                    label_end = sortedPos < number_of_categories - 1 ? value_label_offsets_sorted[sortedPos + 1] : text_length;
                }
                label_length = (int) (label_end - label_offset);
                category_value_labels[i] = new String(Arrays.copyOfRange(labelBytes, (int) label_offset, (int) label_end - 1), "US-ASCII");
                total_label_bytes += label_length;
            }
            value_category_offset += total_label_bytes;
            logger.fine("text_length: " + text_length);
            logger.fine("total_label_bytes: " + total_label_bytes);
            if (total_label_bytes != text_length) {
                throw new IOException("<read mismatch in readLabels()>");
            }
            if (value_category_offset != label_table_length) {
                throw new IOException("<read mismatch in readLabels() 2>");
            }
            reader.readClosingTag(TAG_VALUE_LABELS_LBL_DEF);
            List<DataVariable> dataVariables = dataTable.getDataVariables();
            for (int i = 0; i < dataVariables.size(); i++) {
                DataVariable dataVariable = dataVariables.get(i);
                if (label_table_name.equals(valueLabelsLookupTable[i])) {
                    logger.fine("cross-linking value label table for " + label_table_name);
                    for (int j = 0; j < number_of_categories; j++) {
                        VariableCategory cat = new VariableCategory();
                        long cat_value = category_values[j];
                        String cat_label = category_value_labels[j];
                        cat.setValue("" + cat_value);
                        cat.setLabel(cat_label);
                        cat.setDataVariable(dataVariable);
                        dataVariable.getCategories().add(cat);
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
            Integer stringLength;
            try {
                stringLength = new Integer(stringLengthToken);
            } catch (NumberFormatException nfe) {
                stringLength = null;
            }
            if (stringLength == null || stringLength < 1 || stringLength > 2045) {
                throw new IOException("Invalid STRF encountered: " + variableType);
            }
            return stringLength;
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
        String decodedDateTime;
        String format;
        if (FormatType.matches("^%tc.*")) {
            milliSeconds = Math.round(new Double(rawDatum)) + STATA_BIAS_TO_EPOCH;
            decodedDateTime = sdf_ymdhmsS.format(new Date(milliSeconds));
            format = sdf_ymdhmsS.toPattern();
            logger.fine("tc: result=" + decodedDateTime + ", format = " + format);
        } else if (FormatType.matches("^%t?d.*")) {
            milliSeconds = Math.round(new Double(rawDatum)) * MILLISECCONDS_PER_DAY + STATA_BIAS_TO_EPOCH;
            logger.fine("milliSeconds=" + milliSeconds);
            decodedDateTime = sdf_ymd.format(new Date(milliSeconds));
            format = sdf_ymd.toPattern();
            logger.fine("td:" + decodedDateTime + ", format = " + format);
        } else if (FormatType.matches("^%t?w.*")) {
            long weekYears = Math.round(new Double(rawDatum));
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
            String yearString = Long.toString(1960L + years);
            String dayInYearString = new DecimalFormat("000").format((left * 7) + 1);
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
            long monthYears = Math.round(new Double(rawDatum));
            long left = Math.abs(monthYears) % 12L;
            long years;
            if (monthYears < 0L) {
                left = 12L - left;
                years = (Math.abs(monthYears) - 1) / 12L + 1L;
                years *= -1L;
            } else {
                years = monthYears / 12L;
            }
            if (left == 12L) {
                left = 0L;
            }
            Long monthdata = (left + 1);
            String month = "-" + twoDigitFormatter.format(monthdata) + "-01";
            long year = 1960L + years;
            String monthYear = year + month;
            logger.fine("rawDatum=" + rawDatum + ": monthYear=" + monthYear);
            decodedDateTime = monthYear;
            format = "yyyy-MM-dd";
            logger.fine("tm:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?q.*")) {
            long quarterYears = Math.round(new Double(rawDatum));
            long left = Math.abs(quarterYears) % 4L;
            long years;
            if (quarterYears < 0L) {
                left = 4L - left;
                years = (Math.abs(quarterYears) - 1) / 4L + 1L;
                years *= -1L;
            } else {
                years = quarterYears / 4L;
            }
            String quarter = null;
            if ((left == 0L) || (left == 4L)) {
                quarter = "-01-01";
            } else if (left == 1L) {
                quarter = "-04-01";
            } else if (left == 2L) {
                quarter = "-07-01";
            } else if (left == 3L) {
                quarter = "-11-01";
            }
            long year = 1960L + years;
            String quarterYear = Long.toString(year) + quarter;
            logger.fine("rawDatum=" + rawDatum + ": quarterYear=" + quarterYear);
            decodedDateTime = quarterYear;
            format = "yyyy-MM-dd";
            logger.fine("tq:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?h.*")) {
            long halvesYears = Math.round(new Double(rawDatum));
            long left = Math.abs(halvesYears) % 2L;
            long years;
            if (halvesYears < 0L) {
                years = (Math.abs(halvesYears) - 1) / 2L + 1L;
                years *= -1L;
            } else {
                years = halvesYears / 2L;
            }
            String half;
            if (left != 0L) {
                half = "-07-01";
            } else {
                half = "-01-01";
            }
            long year = 1960L + years;
            String halfYear = Long.toString(year) + half;
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