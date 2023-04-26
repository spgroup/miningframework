package edu.harvard.iq.dataverse.ingest.tabulardata.impl.plugins.dta;

import java.io.*;
import java.nio.*;
import java.util.logging.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import org.apache.commons.lang.*;
import org.apache.commons.codec.binary.Hex;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import edu.harvard.iq.dataverse.DataTable;
import edu.harvard.iq.dataverse.datavariable.DataVariable;
import edu.harvard.iq.dataverse.datavariable.VariableCategory;
import edu.harvard.iq.dataverse.ingest.plugin.spi.*;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataFileReader;
import edu.harvard.iq.dataverse.ingest.tabulardata.spi.TabularDataFileReaderSpi;
import edu.harvard.iq.dataverse.ingest.tabulardata.TabularDataIngest;

public class DTAFileReader extends TabularDataFileReader {

    private static final Logger logger = Logger.getLogger(DTAFileReader.class.getCanonicalName());

    private static Map<Integer, String> STATA_RELEASE_NUMBER = new HashMap<>();

    private static Map<String, Integer> release105type = new LinkedHashMap<>();

    private static Map<String, Integer> release111type = new LinkedHashMap<>();

    private static Map<Integer, Map<String, Integer>> CONSTATNT_TABLE = new LinkedHashMap<>();

    private static Map<String, Integer> release104constant = new LinkedHashMap<>();

    private static Map<String, Integer> release105constant = new LinkedHashMap<>();

    private static Map<String, Integer> release108constant = new LinkedHashMap<>();

    private static Map<String, Integer> release110constant = new LinkedHashMap<>();

    private static Map<String, Integer> release111constant = new LinkedHashMap<>();

    private static Map<String, Integer> release113constant = new LinkedHashMap<>();

    private static Map<String, Integer> release114constant = new LinkedHashMap<>();

    private static Map<String, Integer> release115constant = new LinkedHashMap<>();

    private static Map<Byte, Integer> byteLengthTable105 = new HashMap<>();

    private static Map<Byte, Integer> byteLengthTable111 = new HashMap<>();

    private static Map<Byte, String> variableTypeTable105 = new LinkedHashMap<>();

    private static Map<Byte, String> variableTypeTable111 = new LinkedHashMap<>();

    private static Map<String, Integer> variableTypeMap = new LinkedHashMap<>();

    private static final int[] LENGTH_HEADER = { 60, 109 };

    private static final int[] LENGTH_LABEL = { 32, 81 };

    private static final int[] LENGTH_NAME = { 9, 33 };

    private static final int[] LENGTH_FORMAT_FIELD = { 7, 12, 49 };

    private static final int[] LENGTH_EXPANSION_FIELD = { 0, 2, 4 };

    private static final int[] DBL_MV_PWR = { 333, 1023 };

    static {
        STATA_RELEASE_NUMBER.put(104, "rel_3");
        STATA_RELEASE_NUMBER.put(105, "rel_4or5");
        STATA_RELEASE_NUMBER.put(108, "rel_6");
        STATA_RELEASE_NUMBER.put(110, "rel_7first");
        STATA_RELEASE_NUMBER.put(111, "rel_7scnd");
        STATA_RELEASE_NUMBER.put(113, "rel_8_or_9");
        STATA_RELEASE_NUMBER.put(114, "rel_10");
        STATA_RELEASE_NUMBER.put(115, "rel_12");
        release105type.put("STRING", 127);
        release105type.put("BYTE", 98);
        release105type.put("INT", 105);
        release105type.put("LONG", 108);
        release105type.put("FLOAT", 102);
        release105type.put("DOUBLE0", 100);
        release111type.put("STRING", 0);
        release111type.put("BYTE", -5);
        release111type.put("INT", -4);
        release111type.put("LONG", -3);
        release111type.put("FLOAT", -2);
        release111type.put("DOUBLE", -1);
        release104constant.put("HEADER", LENGTH_HEADER[0]);
        release104constant.put("LABEL", LENGTH_LABEL[0]);
        release104constant.put("NAME", LENGTH_NAME[0]);
        release104constant.put("FORMAT", LENGTH_FORMAT_FIELD[0]);
        release104constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[0]);
        release104constant.put("DBL_MV_PWR", DBL_MV_PWR[0]);
        CONSTATNT_TABLE.put(104, release104constant);
        release105constant.put("HEADER", LENGTH_HEADER[0]);
        release105constant.put("LABEL", LENGTH_LABEL[0]);
        release105constant.put("NAME", LENGTH_NAME[0]);
        release105constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release105constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[1]);
        release105constant.put("DBL_MV_PWR", DBL_MV_PWR[0]);
        CONSTATNT_TABLE.put(105, release105constant);
        release108constant.put("HEADER", LENGTH_HEADER[1]);
        release108constant.put("LABEL", LENGTH_LABEL[1]);
        release108constant.put("NAME", LENGTH_NAME[0]);
        release108constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release108constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[1]);
        release108constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(108, release108constant);
        release110constant.put("HEADER", LENGTH_HEADER[1]);
        release110constant.put("LABEL", LENGTH_LABEL[1]);
        release110constant.put("NAME", LENGTH_NAME[1]);
        release110constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release110constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release110constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(110, release110constant);
        release111constant.put("HEADER", LENGTH_HEADER[1]);
        release111constant.put("LABEL", LENGTH_LABEL[1]);
        release111constant.put("NAME", LENGTH_NAME[1]);
        release111constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release111constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release111constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(111, release111constant);
        release113constant.put("HEADER", LENGTH_HEADER[1]);
        release113constant.put("LABEL", LENGTH_LABEL[1]);
        release113constant.put("NAME", LENGTH_NAME[1]);
        release113constant.put("FORMAT", LENGTH_FORMAT_FIELD[1]);
        release113constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release113constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(113, release113constant);
        release114constant.put("HEADER", LENGTH_HEADER[1]);
        release114constant.put("LABEL", LENGTH_LABEL[1]);
        release114constant.put("NAME", LENGTH_NAME[1]);
        release114constant.put("FORMAT", LENGTH_FORMAT_FIELD[2]);
        release114constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release114constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(114, release114constant);
        release115constant.put("HEADER", LENGTH_HEADER[1]);
        release115constant.put("LABEL", LENGTH_LABEL[1]);
        release115constant.put("NAME", LENGTH_NAME[1]);
        release115constant.put("FORMAT", LENGTH_FORMAT_FIELD[2]);
        release115constant.put("EXPANSION", LENGTH_EXPANSION_FIELD[2]);
        release115constant.put("DBL_MV_PWR", DBL_MV_PWR[1]);
        CONSTATNT_TABLE.put(115, release115constant);
        byteLengthTable105.put((byte) 98, 1);
        byteLengthTable105.put((byte) 105, 2);
        byteLengthTable105.put((byte) 108, 4);
        byteLengthTable105.put((byte) 102, 4);
        byteLengthTable105.put((byte) 100, 8);
        byteLengthTable111.put((byte) -5, 1);
        byteLengthTable111.put((byte) -4, 2);
        byteLengthTable111.put((byte) -3, 4);
        byteLengthTable111.put((byte) -2, 4);
        byteLengthTable111.put((byte) -1, 8);
        variableTypeTable105.put((byte) 98, "Byte");
        variableTypeTable105.put((byte) 105, "Integer");
        variableTypeTable105.put((byte) 108, "Long");
        variableTypeTable105.put((byte) 102, "Float");
        variableTypeTable105.put((byte) 100, "Double");
        variableTypeTable111.put((byte) -5, "Byte");
        variableTypeTable111.put((byte) -4, "Integer");
        variableTypeTable111.put((byte) -3, "Long");
        variableTypeTable111.put((byte) -2, "Float");
        variableTypeTable111.put((byte) -1, "Double");
        variableTypeMap.put("Byte", -5);
        variableTypeMap.put("Integer", -4);
        variableTypeMap.put("Long", -3);
        variableTypeMap.put("Float", -2);
        variableTypeMap.put("Double", -1);
        variableTypeMap.put("String", 0);
    }

    private static String[] MIME_TYPE = { "application/x-stata" };

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

    private static Map<String, String> DATE_TIME_FORMAT_TABLE = new LinkedHashMap<>();

    private static long MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000;

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

    private static Logger dbgLog = Logger.getLogger(DTAFileReader.class.getPackage().getName());

    private String[] valueLabelsLookupTable = null;

    private Map<Integer, Integer> StringLengthTable = new LinkedHashMap<>();

    private Map<String, Integer> typeOffsetTable;

    private Map<String, Integer> constantTable;

    private Map<Byte, Integer> byteLengthTable;

    private Map<Byte, String> variableTypeTable;

    private NumberFormat twoDigitFormatter = new DecimalFormat("00");

    private NumberFormat doubleNumberFormatter = new DecimalFormat();

    TabularDataIngest ingesteddata = new TabularDataIngest();

    private DataTable dataTable = new DataTable();

    private int releaseNumber;

    private int headerLength;

    private int dataLabelLength;

    private boolean isLittleEndian = false;

    private int bytes_per_row;

    private String[] variableTypes = null;

    private String[] dateVariableFormats = null;

    private int value_label_table_length;

    private static final String MissingValueForTabDelimitedFile = "";

    public DTAFileReader(TabularDataFileReaderSpi originator) {
        super(originator);
    }

    private void init() throws IOException {
        if (dbgLog.isLoggable(Level.INFO))
            dbgLog.info("release number=" + releaseNumber);
        if (releaseNumber < 111) {
            typeOffsetTable = release105type;
            variableTypeTable = variableTypeTable105;
            byteLengthTable = byteLengthTable105;
        } else {
            typeOffsetTable = release111type;
            variableTypeTable = variableTypeTable111;
            byteLengthTable = byteLengthTable111;
            BYTE_MISSING_VALUE -= MISSING_VALUE_BIAS;
            INT_MISSIG_VALUE -= MISSING_VALUE_BIAS;
            LONG_MISSING_VALUE -= MISSING_VALUE_BIAS;
        }
        if (releaseNumber <= 105) {
            value_label_table_length = 2;
        } else {
            value_label_table_length = 4;
        }
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("type-offset table to be used:\n" + typeOffsetTable);
        constantTable = CONSTATNT_TABLE.get(releaseNumber);
        headerLength = constantTable.get("HEADER") - DTA_MAGIC_NUMBER_LENGTH;
        dataLabelLength = headerLength - (NVAR_FIELD_LENGTH + NOBS_FIELD_LENGTH + TIME_STAMP_LENGTH);
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("data_label_length=" + dataLabelLength);
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("constant table to be used:\n" + constantTable);
        doubleNumberFormatter.setGroupingUsed(false);
        doubleNumberFormatter.setMaximumFractionDigits(340);
    }

    @Override
    public TabularDataIngest read(BufferedInputStream stream, File dataFile) throws IOException {
        dbgLog.info("***** DTAFileReader: read() start *****");
        if (dataFile != null) {
            throw new IOException("this plugin does not support external raw data files");
        }
        try {
            decodeHeader(stream);
            decodeDescriptors(stream);
            decodeVariableLabels(stream);
            if (releaseNumber != 104) {
                decodeExpansionFields(stream);
            }
            decodeData(stream);
            decodeValueLabels(stream);
            ingesteddata.setDataTable(dataTable);
        } catch (IllegalArgumentException iaex) {
            throw new IOException(iaex.getMessage());
        }
        dbgLog.info("***** DTAFileReader: read() end *****");
        return ingesteddata;
    }

    private void decodeHeader(BufferedInputStream stream) throws IOException {
        dbgLog.fine("***** decodeHeader(): start *****");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        dbgLog.fine("reading the header segument 1: 4 byte\n");
        byte[] magic_number = new byte[DTA_MAGIC_NUMBER_LENGTH];
        int nbytes = stream.read(magic_number, 0, DTA_MAGIC_NUMBER_LENGTH);
        if (nbytes == 0) {
            throw new IOException();
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("hex dump: 1st 4bytes =>" + new String(Hex.encodeHex(magic_number)) + "<-");
        }
        logger.info("magic_number[0]: " + magic_number[0]);
        logger.info("magic_number[1]: " + magic_number[1]);
        logger.info("magic_number[2]: " + magic_number[2]);
        if (magic_number[2] != 1) {
            dbgLog.fine("3rd byte is not 1: given file is not stata-dta type");
            throw new IllegalArgumentException("The file is not in a STATA format that we can read or support.");
        } else if ((magic_number[1] != 1) && (magic_number[1] != 2)) {
            dbgLog.fine("2nd byte is neither 0 nor 1: this file is not stata-dta type");
            throw new IllegalArgumentException("given file is not stata-dta type");
        } else if (!STATA_RELEASE_NUMBER.containsKey((int) magic_number[0])) {
            dbgLog.fine("1st byte (" + magic_number[0] + ") is not within the ingestable range [rel. 3-10]:" + "we cannot ingest this Stata file.");
            throw new IllegalArgumentException("given file is not stata-dta type");
        } else {
            releaseNumber = magic_number[0];
            init();
            dataTable.setOriginalFileFormat(MIME_TYPE[0]);
            dataTable.setOriginalFormatVersion(STATA_RELEASE_NUMBER.get(releaseNumber));
            dataTable.setUnf("UNF:6:FILEFILEFILEFILE");
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("this file is stata-dta type: " + STATA_RELEASE_NUMBER.get(releaseNumber) + " (that means Stata version " + releaseNumber + ")");
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("Endian(file)(Big: 1; Little:2)=" + magic_number[1]);
            }
            if (magic_number[1] == 2) {
                isLittleEndian = true;
                dbgLog.fine("Reversal of the bytes is necessary to decode " + "multi-byte fields");
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("Endian of this platform:" + ByteOrder.nativeOrder().toString());
            }
        }
        dbgLog.fine("reading the remaining header segument 2: 60 or 109-byte");
        byte[] header = new byte[headerLength];
        nbytes = stream.read(header, 0, headerLength);
        ByteBuffer bbnvar = ByteBuffer.wrap(header, 0, NVAR_FIELD_LENGTH);
        ByteBuffer dupnvar = bbnvar.duplicate();
        short short_nvar = dupnvar.getShort();
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("get original short view(nvar)=" + short_nvar);
        }
        if (isLittleEndian) {
            bbnvar.order(ByteOrder.LITTLE_ENDIAN);
        }
        short shrt_nvar = bbnvar.getShort();
        dataTable.setVarQuantity(new Long(shrt_nvar));
        int nvar = shrt_nvar;
        if (dbgLog.isLoggable(Level.INFO)) {
            dbgLog.info("number of variables(nvar)=" + nvar);
        }
        List<DataVariable> variableList = new ArrayList<>();
        for (int i = 0; i < nvar; i++) {
            DataVariable dv = new DataVariable(i, dataTable);
            variableList.add(dv);
        }
        dataTable.setDataVariables(variableList);
        variableTypes = new String[nvar];
        dateVariableFormats = new String[nvar];
        ByteBuffer nobs = ByteBuffer.wrap(header, NVAR_FIELD_LENGTH, NOBS_FIELD_LENGTH);
        ByteBuffer dupnobs = nobs.duplicate();
        int int_dupnobs = dupnobs.getInt();
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("raw nobs=" + int_dupnobs);
        }
        if (isLittleEndian) {
            nobs.order(ByteOrder.LITTLE_ENDIAN);
        }
        int int_nobs = nobs.getInt();
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("reversed nobs=" + int_nobs);
        }
        dataTable.setCaseQuantity(new Long(int_nobs));
        int dl_offset = NVAR_FIELD_LENGTH + NOBS_FIELD_LENGTH;
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("dl_offset=" + dl_offset);
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("data_label_length=" + dataLabelLength);
        }
        String data_label = new String(Arrays.copyOfRange(header, dl_offset, (dl_offset + dataLabelLength)), "ISO-8859-1");
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("data_label_length=" + data_label.length());
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("loation of the null character=" + data_label.indexOf(0));
        }
        String dataLabel = getNullStrippedString(data_label);
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("data_label_length=" + dataLabel.length());
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("data_label=[" + dataLabel + "]");
        }
        if (releaseNumber > 104) {
            int ts_offset = dl_offset + dataLabelLength;
            String time_stamp = new String(Arrays.copyOfRange(header, ts_offset, ts_offset + TIME_STAMP_LENGTH), "ISO-8859-1");
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("time_stamp_length=" + time_stamp.length());
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("loation of the null character=" + time_stamp.indexOf(0));
            }
            String timeStamp = getNullStrippedString(time_stamp);
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("timeStamp_length=" + timeStamp.length());
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("timeStamp=[" + timeStamp + "]");
            }
        }
    }

    private void decodeDescriptors(BufferedInputStream stream) throws IOException {
        dbgLog.fine("decodeDescriptors(): start");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int nvar = dataTable.getVarQuantity().intValue();
        decodeDescriptorVarTypeList(stream, nvar);
        decodeDescriptorVarNameList(stream, nvar);
        decodeDescriptorVarSortList(stream, nvar);
        decodeDescriptorVariableFormat(stream, nvar);
        decodeDescriptorValueLabel(stream, nvar);
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("decodeDescriptors(): end");
        }
    }

    private void decodeDescriptorVarTypeList(BufferedInputStream stream, int nvar) throws IOException {
        byte[] typeList = new byte[nvar];
        int nbytes = stream.read(typeList, 0, nvar);
        if (nbytes == 0) {
            throw new IOException("reading the descriptior: no byte was read");
        }
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("type_offset_table:\n" + typeOffsetTable);
        bytes_per_row = 0;
        for (int i = 0; i < typeList.length; i++) {
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine(i + "-th value=" + typeList[i]);
            DataVariable dataVariable = dataTable.getDataVariables().get(i);
            if (byteLengthTable.containsKey(typeList[i])) {
                bytes_per_row += byteLengthTable.get(typeList[i]);
                variableTypes[i] = variableTypeTable.get(typeList[i]);
                String typeLabel = variableTypes[i];
                if (typeLabel != null) {
                    dataVariable.setTypeNumeric();
                    if (typeLabel.equals("Byte") || typeLabel.equals("Integer") || typeLabel.equals("Long")) {
                        dataVariable.setIntervalDiscrete();
                    } else if (typeLabel.equals("Float") || typeLabel.equals("Double")) {
                        dataVariable.setIntervalContinuous();
                    } else {
                        throw new IOException("Unrecognized type label: " + typeLabel + " for Stata type value byte " + typeList[i] + ".");
                    }
                } else {
                    throw new IOException("No entry in the known types table for Stata type value byte " + typeList[i] + ".");
                }
            } else {
                if (releaseNumber < 111) {
                    int stringType = 256 + typeList[i];
                    if (stringType >= typeOffsetTable.get("STRING")) {
                        int string_var_length = stringType - typeOffsetTable.get("STRING");
                        if (dbgLog.isLoggable(Level.FINE))
                            dbgLog.fine("string_var_length=" + string_var_length);
                        bytes_per_row += string_var_length;
                        variableTypes[i] = "String";
                        dataVariable.setTypeCharacter();
                        dataVariable.setIntervalDiscrete();
                        StringLengthTable.put(i, string_var_length);
                    } else {
                        throw new IOException("unknown variable type was detected: reading errors?");
                    }
                } else if (releaseNumber >= 111) {
                    if (dbgLog.isLoggable(Level.FINE))
                        dbgLog.fine("DTA reader: typeList[" + i + "]=" + typeList[i]);
                    int stringType = ((typeList[i] > 0) && (typeList[i] <= 127)) ? typeList[i] : 256 + typeList[i];
                    if (stringType >= typeOffsetTable.get("STRING")) {
                        int string_var_length = stringType - typeOffsetTable.get("STRING");
                        if (dbgLog.isLoggable(Level.FINE))
                            dbgLog.fine("DTA reader: string_var_length=" + string_var_length);
                        bytes_per_row += string_var_length;
                        variableTypes[i] = "String";
                        dataVariable.setTypeCharacter();
                        dataVariable.setIntervalDiscrete();
                        StringLengthTable.put(i, string_var_length);
                    } else {
                        throw new IOException("unknown variable type was detected: reading errors?");
                    }
                } else {
                    throw new IOException("uknown release number ");
                }
            }
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine(i + "=th\t sum=" + bytes_per_row);
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("bytes_per_row(final)=" + bytes_per_row);
            dbgLog.fine("variableTypes:\n" + Arrays.deepToString(variableTypes));
            dbgLog.fine("StringLengthTable=" + StringLengthTable);
        }
    }

    private void decodeDescriptorVarNameList(BufferedInputStream stream, int nvar) throws IOException {
        int length_var_name = constantTable.get("NAME");
        int length_var_name_list = length_var_name * nvar;
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("length_var_name_list=" + length_var_name_list);
        byte[] variableNameBytes = new byte[length_var_name_list];
        int nbytes = stream.read(variableNameBytes, 0, length_var_name_list);
        if (nbytes == 0) {
            throw new IOException("reading the var name list: no var name was read");
        }
        int offset_start = 0;
        int offset_end = 0;
        for (DataVariable dataVariable : dataTable.getDataVariables()) {
            offset_end += length_var_name;
            String vari = new String(Arrays.copyOfRange(variableNameBytes, offset_start, offset_end), "ISO-8859-1");
            String varName = getNullStrippedString(vari);
            dataVariable.setName(varName);
            dbgLog.fine("next name=[" + varName + "]");
            offset_start = offset_end;
        }
    }

    private void decodeDescriptorVarSortList(BufferedInputStream stream, int nvar) throws IOException {
        int length_var_sort_list = VAR_SORT_FIELD_LENGTH * (nvar + 1);
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("length_var_sort_list=" + length_var_sort_list);
        byte[] varSortList = new byte[length_var_sort_list];
        short[] variableSortList = new short[nvar + 1];
        int nbytes = stream.read(varSortList, 0, length_var_sort_list);
        if (nbytes == 0) {
            throw new IOException("reading error: the varSortList");
        }
        int offset_start = 0;
        for (int i = 0; i <= nvar; i++) {
            ByteBuffer bb_varSortList = ByteBuffer.wrap(varSortList, offset_start, VAR_SORT_FIELD_LENGTH);
            if (isLittleEndian) {
                bb_varSortList.order(ByteOrder.LITTLE_ENDIAN);
            }
            variableSortList[i] = bb_varSortList.getShort();
            offset_start += VAR_SORT_FIELD_LENGTH;
        }
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("variableSortList=" + Arrays.toString(variableSortList));
    }

    private void decodeDescriptorVariableFormat(BufferedInputStream stream, int nvar) throws IOException {
        int length_var_format = constantTable.get("FORMAT");
        int length_var_format_list = length_var_format * nvar;
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("length_var_format_list=" + length_var_format_list);
        byte[] variableFormatList = new byte[length_var_format_list];
        int nbytes = stream.read(variableFormatList, 0, length_var_format_list);
        if (nbytes == 0) {
            throw new IOException("reading var formats: no format was read");
        }
        int offset_start = 0;
        int offset_end = 0;
        for (int i = 0; i < nvar; i++) {
            offset_end += length_var_format;
            String vari = new String(Arrays.copyOfRange(variableFormatList, offset_start, offset_end), "ISO-8859-1");
            String variableFormat = getNullStrippedString(vari);
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine(i + "-th format=[" + variableFormat + "]");
            String variableFormatKey = null;
            if (variableFormat.startsWith("%t")) {
                variableFormatKey = variableFormat.substring(0, 3);
            } else {
                variableFormatKey = variableFormat.substring(0, 2);
            }
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine(i + " th variableFormatKey=" + variableFormatKey);
            if (DATE_TIME_FORMAT_TABLE.containsKey(variableFormatKey)) {
                DataVariable dataVariable = dataTable.getDataVariables().get(i);
                dateVariableFormats[i] = variableFormat;
                dataVariable.setFormatCategory(DATE_TIME_FORMAT_TABLE.get(variableFormatKey));
                if (dbgLog.isLoggable(Level.FINE))
                    dbgLog.fine(i + "th var: category=" + DATE_TIME_FORMAT_TABLE.get(variableFormatKey));
                dataVariable.setTypeCharacter();
                dataVariable.setIntervalDiscrete();
            }
            offset_start = offset_end;
        }
    }

    private void decodeDescriptorValueLabel(BufferedInputStream stream, int nvar) throws IOException {
        valueLabelsLookupTable = new String[nvar];
        int length_label_name = constantTable.get("NAME");
        int length_label_name_list = length_label_name * nvar;
        dbgLog.fine("length_label_name=" + length_label_name_list);
        byte[] labelNameList = new byte[length_label_name_list];
        String[] labelNames = new String[nvar];
        int nbytes = stream.read(labelNameList, 0, length_label_name_list);
        if (nbytes == 0) {
            throw new IOException("reading value-label list:: no var name was read");
        }
        int offset_start = 0;
        int offset_end = 0;
        for (int i = 0; i < nvar; i++) {
            offset_end += length_label_name;
            String vari = new String(Arrays.copyOfRange(labelNameList, offset_start, offset_end), "ISO-8859-1");
            labelNames[i] = getNullStrippedString(vari);
            dbgLog.fine(i + "-th label=[" + labelNames[i] + "]");
            offset_start = offset_end;
        }
        dbgLog.fine("labelNames=\n" + StringUtils.join(labelNames, ",\n") + "\n");
        for (int i = 0; i < nvar; i++) {
            if ((labelNames[i] != null) && (!labelNames[i].isEmpty())) {
                valueLabelsLookupTable[i] = labelNames[i];
            }
        }
    }

    private void decodeVariableLabels(BufferedInputStream stream) throws IOException {
        dbgLog.fine("decodeVariableLabels(): start");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int nvar = dataTable.getVarQuantity().intValue();
        int length_var_label = constantTable.get("LABEL");
        int length_var_label_list = length_var_label * nvar;
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("length_label_name=" + length_var_label_list);
        }
        byte[] variableLabelBytes = new byte[length_var_label_list];
        int nbytes = stream.read(variableLabelBytes, 0, length_var_label_list);
        if (nbytes == 0) {
            throw new IOException("reading variable label list: no label was read");
        }
        int offset_start = 0;
        int offset_end = 0;
        for (int i = 0; i < nvar; i++) {
            offset_end += length_var_label;
            String vari = new String(Arrays.copyOfRange(variableLabelBytes, offset_start, offset_end), "ISO-8859-1");
            String variableLabelParsed = getNullStrippedString(vari);
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine(i + "-th label=[" + variableLabelParsed + "]");
            }
            offset_start = offset_end;
            dataTable.getDataVariables().get(i).setLabel(variableLabelParsed);
        }
        dbgLog.fine("decodeVariableLabels(): end");
    }

    private void decodeExpansionFields(BufferedInputStream stream) throws IOException {
        dbgLog.fine("***** decodeExpansionFields(): start *****");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int int_type_expansion_field = constantTable.get("EXPANSION");
        if (dbgLog.isLoggable(Level.FINE))
            dbgLog.fine("int_type_expansion_field=" + int_type_expansion_field);
        while (true) {
            byte[] firstByte = new byte[1];
            byte[] lengthBytes = new byte[int_type_expansion_field];
            int nbyte = stream.read(firstByte, 0, 1);
            dbgLog.fine("read 1st byte");
            int nbytes = stream.read(lengthBytes, 0, int_type_expansion_field);
            dbgLog.fine("read next integer");
            ByteBuffer bb_field_length = ByteBuffer.wrap(lengthBytes);
            if (isLittleEndian) {
                bb_field_length.order(ByteOrder.LITTLE_ENDIAN);
                dbgLog.fine("byte reversed");
            }
            int field_length;
            if (int_type_expansion_field == 2) {
                field_length = bb_field_length.getShort();
            } else {
                field_length = bb_field_length.getInt();
            }
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine("field_length=" + field_length);
            if (dbgLog.isLoggable(Level.FINE))
                dbgLog.fine("firstByte[0]=" + firstByte[0]);
            if ((field_length + firstByte[0]) == 0) {
                break;
            } else {
                byte[] stringField = new byte[field_length];
                nbyte = stream.read(stringField, 0, field_length);
            }
        }
        dbgLog.fine("decodeExpansionFields(): end");
    }

    private void decodeValueLabels(BufferedInputStream stream) throws IOException {
        dbgLog.fine("decodeValueLabels(): start");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        if (stream.available() != 0) {
            if (releaseNumber <= 105) {
                parseValueLabelsRelease105(stream);
            } else {
                parseValueLabelsReleasel108(stream);
            }
        } else {
            dbgLog.fine("no value-label table: end of file");
        }
        dbgLog.fine("decodeValueLabels(): end");
    }

    void parseValueLabelsRelease105(BufferedInputStream stream) throws IOException {
        dbgLog.fine("parseValueLabelsRelease105(): start");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int nvar = dataTable.getVarQuantity().intValue();
        int length_label_name = constantTable.get("NAME") + 1;
        int length_value_label_header = value_label_table_length + length_label_name;
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("value_label_table_length=" + value_label_table_length);
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("length_value_label_header=" + length_value_label_header);
        }
        int length_lable_name_field = 8;
        Map<String, Map<String, String>> tempValueLabelTable = new LinkedHashMap<>();
        for (int i = 0; i < nvar; i++) {
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("\n\n" + i + "th value-label table header");
            }
            byte[] valueLabelHeader = new byte[length_value_label_header];
            int nbytes = stream.read(valueLabelHeader, 0, length_value_label_header);
            if (nbytes == 0) {
                throw new IOException("reading value label header: no datum");
            }
            ByteBuffer bb_value_label_pairs = ByteBuffer.wrap(valueLabelHeader, 0, value_label_table_length);
            if (isLittleEndian) {
                bb_value_label_pairs.order(ByteOrder.LITTLE_ENDIAN);
            }
            int no_value_label_pairs = bb_value_label_pairs.getShort();
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("no_value_label_pairs=" + no_value_label_pairs);
            }
            String rawLabelName = new String(Arrays.copyOfRange(valueLabelHeader, value_label_table_length, (value_label_table_length + length_label_name)), "ISO-8859-1");
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("rawLabelName(length)=" + rawLabelName.length());
            }
            String labelName = rawLabelName.substring(0, rawLabelName.indexOf(0));
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("label name = " + labelName + "\n");
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine(i + "-th value-label table");
            }
            int length_value_label_table = (value_label_table_length + length_lable_name_field) * no_value_label_pairs;
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("length_value_label_table=" + length_value_label_table);
            }
            byte[] valueLabelTable_i = new byte[length_value_label_table];
            int noBytes = stream.read(valueLabelTable_i, 0, length_value_label_table);
            if (noBytes == 0) {
                throw new IOException("reading value label table: no datum");
            }
            short[] valueList = new short[no_value_label_pairs];
            int offset_value = 0;
            for (int k = 0; k < no_value_label_pairs; k++) {
                ByteBuffer bb_value_list = ByteBuffer.wrap(valueLabelTable_i, offset_value, value_label_table_length);
                if (isLittleEndian) {
                    bb_value_list.order(ByteOrder.LITTLE_ENDIAN);
                }
                valueList[k] = bb_value_list.getShort();
                offset_value += value_label_table_length;
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("value_list=" + Arrays.toString(valueList) + "\n");
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("current offset_value=" + offset_value);
            }
            int offset_start = offset_value;
            int offset_end = offset_value + length_lable_name_field;
            String[] labelList = new String[no_value_label_pairs];
            for (int l = 0; l < no_value_label_pairs; l++) {
                String string_l = new String(Arrays.copyOfRange(valueLabelTable_i, offset_start, offset_end), "ISO-8859-1");
                int null_position = string_l.indexOf(0);
                if (null_position != -1) {
                    labelList[l] = string_l.substring(0, null_position);
                } else {
                    labelList[l] = string_l;
                }
                offset_start = offset_end;
                offset_end += length_lable_name_field;
            }
            tempValueLabelTable.put(labelName, new LinkedHashMap<>());
            for (int j = 0; j < no_value_label_pairs; j++) {
                if (dbgLog.isLoggable(Level.FINE)) {
                    dbgLog.fine(j + "-th pair:" + valueList[j] + "[" + labelList[j] + "]");
                }
                tempValueLabelTable.get(labelName).put(Integer.toString(valueList[j]), labelList[j]);
            }
            if (stream.available() == 0) {
                if (dbgLog.isLoggable(Level.FINE)) {
                    dbgLog.fine("reached the end of file at " + i + "th value-label Table.");
                }
                break;
            }
        }
        for (int i = 0; i < nvar; i++) {
            dataTable.getDataVariables().get(i).setLabled(true);
            if (valueLabelsLookupTable[i] != null) {
                if (tempValueLabelTable.get(valueLabelsLookupTable[i]) != null) {
                    for (String value : tempValueLabelTable.get(valueLabelsLookupTable[i]).keySet()) {
                        VariableCategory cat = new VariableCategory();
                        cat.setValue(value);
                        cat.setLabel(tempValueLabelTable.get(valueLabelsLookupTable[i]).get(value));
                        cat.setDataVariable(dataTable.getDataVariables().get(i));
                        dataTable.getDataVariables().get(i).getCategories().add(cat);
                    }
                }
            }
        }
        dbgLog.fine("parseValueLabelsRelease105(): end");
    }

    private void parseValueLabelsReleasel108(BufferedInputStream stream) throws IOException {
        dbgLog.fine("parseValueLabelsRelease108(): start");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int nvar = dataTable.getVarQuantity().intValue();
        int length_label_name = constantTable.get("NAME");
        int length_value_label_header = value_label_table_length + length_label_name + VALUE_LABEL_HEADER_PADDING_LENGTH;
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("value_label_table_length=" + value_label_table_length);
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("length_value_label_header=" + length_value_label_header);
        }
        Map<String, Map<String, String>> tempValueLabelTable = new LinkedHashMap<>();
        for (int i = 0; i < nvar; i++) {
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("\n\n" + i + "th value-label table header");
            }
            byte[] valueLabelHeader = new byte[length_value_label_header];
            int nbytes = stream.read(valueLabelHeader, 0, length_value_label_header);
            if (nbytes == 0) {
                throw new IOException("reading value label header: no datum");
            }
            ByteBuffer bb_value_label_header = ByteBuffer.wrap(valueLabelHeader, 0, value_label_table_length);
            if (isLittleEndian) {
                bb_value_label_header.order(ByteOrder.LITTLE_ENDIAN);
            }
            int length_value_label_table = bb_value_label_header.getInt();
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("length of this value-label table=" + length_value_label_table);
            }
            String rawLabelName = new String(Arrays.copyOfRange(valueLabelHeader, value_label_table_length, (value_label_table_length + length_label_name)), "ISO-8859-1");
            String labelName = getNullStrippedString(rawLabelName);
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("label name = " + labelName + "\n");
            }
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine(i + "-th value-label table");
            }
            byte[] valueLabelTable_i = new byte[length_value_label_table];
            int noBytes = stream.read(valueLabelTable_i, 0, length_value_label_table);
            if (noBytes == 0) {
                throw new IOException("reading value label table: no datum");
            }
            int valueLabelTable_offset = 0;
            ByteBuffer bb_value_label_pairs = ByteBuffer.wrap(valueLabelTable_i, valueLabelTable_offset, value_label_table_length);
            if (isLittleEndian) {
                bb_value_label_pairs.order(ByteOrder.LITTLE_ENDIAN);
            }
            int no_value_label_pairs = bb_value_label_pairs.getInt();
            valueLabelTable_offset += value_label_table_length;
            if (dbgLog.isLoggable(Level.FINE)) {
                dbgLog.fine("no_value_label_pairs=" + no_value_label_pairs);
            }
            ByteBuffer bb_length_label_segment = ByteBuffer.wrap(valueLabelTable_i, valueLabelTable_offset, value_label_table_length);
            if (isLittleEndian) {
                bb_length_label_segment.order(ByteOrder.LITTLE_ENDIAN);
            }
            int length_label_segment = bb_length_label_segment.getInt();
            valueLabelTable_offset += value_label_table_length;
            int[] label_offsets = new int[no_value_label_pairs];
            int byte_offset = valueLabelTable_offset;
            for (int j = 0; j < no_value_label_pairs; j++) {
                ByteBuffer bb_label_offset = ByteBuffer.wrap(valueLabelTable_i, byte_offset, value_label_table_length);
                if (isLittleEndian) {
                    bb_label_offset.order(ByteOrder.LITTLE_ENDIAN);
                    dbgLog.fine("label offset: byte reversed");
                }
                label_offsets[j] = bb_label_offset.getInt();
                dbgLog.fine("label offset [" + j + "]: " + label_offsets[j]);
                byte_offset += value_label_table_length;
            }
            dbgLog.fine("value array");
            int[] valueList = new int[no_value_label_pairs];
            int offset_value = byte_offset;
            for (int k = 0; k < no_value_label_pairs; k++) {
                ByteBuffer bb_value_list = ByteBuffer.wrap(valueLabelTable_i, offset_value, value_label_table_length);
                if (isLittleEndian) {
                    bb_value_list.order(ByteOrder.LITTLE_ENDIAN);
                }
                valueList[k] = bb_value_list.getInt();
                offset_value += value_label_table_length;
            }
            String label_segment = new String(Arrays.copyOfRange(valueLabelTable_i, offset_value, (length_label_segment + offset_value)), "ISO-8859-1");
            String[] labelList = new String[no_value_label_pairs];
            for (int l = 0; l < no_value_label_pairs; l++) {
                String lblString = null;
                int lblOffset = label_offsets[l];
                lblString = label_segment.substring(lblOffset);
                int nullIndx = lblString.indexOf('\000');
                if (nullIndx > -1) {
                    lblString = lblString.substring(0, nullIndx);
                }
                labelList[l] = lblString;
            }
            tempValueLabelTable.put(labelName, new LinkedHashMap<>());
            for (int l = 0; l < no_value_label_pairs; l++) {
                if (dbgLog.isLoggable(Level.FINE)) {
                    dbgLog.fine(l + "-th pair:" + valueList[l] + "[" + labelList[l] + "]");
                }
                tempValueLabelTable.get(labelName).put(Integer.toString(valueList[l]), labelList[l]);
            }
            if (stream.available() == 0) {
                dbgLog.fine("reached the end of the file at " + i + "th value-label Table");
                break;
            }
        }
        for (int i = 0; i < nvar; i++) {
            if (valueLabelsLookupTable[i] != null) {
                if (tempValueLabelTable.get(valueLabelsLookupTable[i]) != null) {
                    for (String value : tempValueLabelTable.get(valueLabelsLookupTable[i]).keySet()) {
                        VariableCategory cat = new VariableCategory();
                        cat.setValue(value);
                        cat.setLabel(tempValueLabelTable.get(valueLabelsLookupTable[i]).get(value));
                        cat.setDataVariable(dataTable.getDataVariables().get(i));
                        dataTable.getDataVariables().get(i).getCategories().add(cat);
                    }
                }
            }
        }
        dbgLog.fine("parseValueLabelsRelease108(): end");
    }

    private void decodeData(BufferedInputStream stream) throws IOException {
        dbgLog.fine("\n***** decodeData(): start *****");
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        int nvar = dataTable.getVarQuantity().intValue();
        int nobs = dataTable.getCaseQuantity().intValue();
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("data dimensions[observations x variables] = (" + nobs + "x" + nvar + ")");
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("bytes per row=" + bytes_per_row + " bytes");
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("variableTypes=" + Arrays.deepToString(variableTypes));
        }
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("StringLengthTable=" + StringLengthTable);
        }
        FileOutputStream fileOutTab = null;
        PrintWriter pwout = null;
        File tabDelimitedDataFile = File.createTempFile("tempTabfile.", ".tab");
        ingesteddata.setTabDelimitedFile(tabDelimitedDataFile);
        fileOutTab = new FileOutputStream(tabDelimitedDataFile);
        pwout = new PrintWriter(new OutputStreamWriter(fileOutTab, "utf8"), true);
        for (int i = 0; i < nobs; i++) {
            byte[] dataRowBytes = new byte[bytes_per_row];
            Object[] dataRow = new Object[nvar];
            int nbytes = stream.read(dataRowBytes, 0, bytes_per_row);
            if (nbytes == 0) {
                String errorMessage = "reading data: no data were read at(" + i + "th row)";
                throw new IOException(errorMessage);
            }
            int byte_offset = 0;
            for (int columnCounter = 0; columnCounter < variableTypes.length; columnCounter++) {
                Integer varType = variableTypeMap.get(variableTypes[columnCounter]);
                boolean isDateTimeDatum = false;
                String formatCategory = dataTable.getDataVariables().get(columnCounter).getFormatCategory();
                if (formatCategory != null && (formatCategory.equals("time") || formatCategory.equals("date"))) {
                    isDateTimeDatum = true;
                }
                String variableFormat = dateVariableFormats[columnCounter];
                switch(varType != null ? varType : 256) {
                    case -5:
                        byte byte_datum = dataRowBytes[byte_offset];
                        if (dbgLog.isLoggable(Level.FINER)) {
                            dbgLog.finer(i + "-th row " + columnCounter + "=th column byte =" + byte_datum);
                        }
                        if (byte_datum >= BYTE_MISSING_VALUE) {
                            if (dbgLog.isLoggable(Level.FINER)) {
                                dbgLog.finer(i + "-th row " + columnCounter + "=th column byte MV=" + byte_datum);
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            dataRow[columnCounter] = byte_datum;
                        }
                        byte_offset++;
                        break;
                    case -4:
                        ByteBuffer int_buffer = ByteBuffer.wrap(dataRowBytes, byte_offset, 2);
                        if (isLittleEndian) {
                            int_buffer.order(ByteOrder.LITTLE_ENDIAN);
                        }
                        short short_datum = int_buffer.getShort();
                        if (dbgLog.isLoggable(Level.FINER)) {
                            dbgLog.finer(i + "-th row " + columnCounter + "=th column stata int =" + short_datum);
                        }
                        if (short_datum >= INT_MISSIG_VALUE) {
                            if (dbgLog.isLoggable(Level.FINER)) {
                                dbgLog.finer(i + "-th row " + columnCounter + "=th column stata long missing value=" + short_datum);
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            if (isDateTimeDatum) {
                                DecodedDateTime ddt = decodeDateTimeData("short", variableFormat, Short.toString(short_datum));
                                if (dbgLog.isLoggable(Level.FINER)) {
                                    dbgLog.finer(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                                }
                                dataRow[columnCounter] = ddt.decodedDateTime;
                                dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                            } else {
                                dataRow[columnCounter] = short_datum;
                            }
                        }
                        byte_offset += 2;
                        break;
                    case -3:
                        ByteBuffer long_buffer = ByteBuffer.wrap(dataRowBytes, byte_offset, 4);
                        if (isLittleEndian) {
                            long_buffer.order(ByteOrder.LITTLE_ENDIAN);
                        }
                        int int_datum = long_buffer.getInt();
                        if (dbgLog.isLoggable(Level.FINE)) {
                        }
                        if (int_datum >= LONG_MISSING_VALUE) {
                            if (dbgLog.isLoggable(Level.FINE)) {
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            if (isDateTimeDatum) {
                                DecodedDateTime ddt = decodeDateTimeData("int", variableFormat, Integer.toString(int_datum));
                                if (dbgLog.isLoggable(Level.FINER)) {
                                    dbgLog.finer(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                                }
                                dataRow[columnCounter] = ddt.decodedDateTime;
                                dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                            } else {
                                dataRow[columnCounter] = int_datum;
                            }
                        }
                        byte_offset += 4;
                        break;
                    case -2:
                        ByteBuffer float_buffer = ByteBuffer.wrap(dataRowBytes, byte_offset, 4);
                        if (isLittleEndian) {
                            float_buffer.order(ByteOrder.LITTLE_ENDIAN);
                        }
                        float float_datum = float_buffer.getFloat();
                        if (dbgLog.isLoggable(Level.FINER)) {
                            dbgLog.finer(i + "-th row " + columnCounter + "=th column float =" + float_datum);
                        }
                        if (FLOAT_MISSING_VALUE_SET.contains(float_datum)) {
                            if (dbgLog.isLoggable(Level.FINER)) {
                                dbgLog.finer(i + "-th row " + columnCounter + "=th column float missing value=" + float_datum);
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            if (isDateTimeDatum) {
                                DecodedDateTime ddt = decodeDateTimeData("float", variableFormat, doubleNumberFormatter.format(float_datum));
                                if (dbgLog.isLoggable(Level.FINER)) {
                                    dbgLog.finer(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                                }
                                dataRow[columnCounter] = ddt.decodedDateTime;
                                dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                            } else {
                                dataRow[columnCounter] = float_datum;
                                dataTable.getDataVariables().get(columnCounter).setFormat("float");
                            }
                        }
                        byte_offset += 4;
                        break;
                    case -1:
                        ByteBuffer double_buffer = ByteBuffer.wrap(dataRowBytes, byte_offset, 8);
                        if (isLittleEndian) {
                            double_buffer.order(ByteOrder.LITTLE_ENDIAN);
                        }
                        double double_datum = double_buffer.getDouble();
                        if (DOUBLE_MISSING_VALUE_SET.contains(double_datum)) {
                            if (dbgLog.isLoggable(Level.FINER)) {
                                dbgLog.finer(i + "-th row " + columnCounter + "=th column double missing value=" + double_datum);
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            if (isDateTimeDatum) {
                                DecodedDateTime ddt = decodeDateTimeData("double", variableFormat, doubleNumberFormatter.format(double_datum));
                                if (dbgLog.isLoggable(Level.FINER)) {
                                    dbgLog.finer(i + "-th row , decodedDateTime " + ddt.decodedDateTime + ", format=" + ddt.format);
                                }
                                dataRow[columnCounter] = ddt.decodedDateTime;
                                dataTable.getDataVariables().get(columnCounter).setFormat(ddt.format);
                            } else {
                                dataRow[columnCounter] = doubleNumberFormatter.format(double_datum);
                            }
                        }
                        byte_offset += 8;
                        break;
                    case 0:
                        int strVarLength = StringLengthTable.get(columnCounter);
                        String raw_datum = new String(Arrays.copyOfRange(dataRowBytes, byte_offset, (byte_offset + strVarLength)), "ISO-8859-1");
                        String string_datum = getNullStrippedString(raw_datum);
                        if (dbgLog.isLoggable(Level.FINER)) {
                            dbgLog.finer(i + "-th row " + columnCounter + "=th column string =" + string_datum);
                        }
                        if (string_datum.isEmpty()) {
                            if (dbgLog.isLoggable(Level.FINER)) {
                                dbgLog.finer(i + "-th row " + columnCounter + "=th column string missing value=" + string_datum);
                            }
                            dataRow[columnCounter] = MissingValueForTabDelimitedFile;
                        } else {
                            String escapedString = string_datum.replace("\\", "\\\\");
                            escapedString = escapedString.replaceAll("\"", Matcher.quoteReplacement("\\\""));
                            escapedString = escapedString.replaceAll("\t", Matcher.quoteReplacement("\\t"));
                            escapedString = escapedString.replaceAll("\n", Matcher.quoteReplacement("\\n"));
                            escapedString = escapedString.replaceAll("\r", Matcher.quoteReplacement("\\r"));
                            dataRow[columnCounter] = "\"" + escapedString + "\"";
                        }
                        byte_offset += strVarLength;
                        break;
                    default:
                        dbgLog.fine("unknown variable type found");
                        String errorMessage = "unknow variable Type found at data section";
                        throw new InvalidObjectException(errorMessage);
                }
            }
            pwout.println(StringUtils.join(dataRow, "\t"));
            if (dbgLog.isLoggable(Level.FINE)) {
            }
        }
        pwout.close();
        if (dbgLog.isLoggable(Level.FINE)) {
            dbgLog.fine("variableTypes:\n" + Arrays.deepToString(variableTypes));
        }
        dbgLog.fine("DTA Ingest: decodeData(): end.");
    }

    private class DecodedDateTime {

        String format;

        String decodedDateTime;
    }

    private DecodedDateTime decodeDateTimeData(String storageType, String FormatType, String rawDatum) throws IOException {
        if (dbgLog.isLoggable(Level.FINER))
            dbgLog.finer("(storageType, FormatType, rawDatum)=(" + storageType + ", " + FormatType + ", " + rawDatum + ")");
        long milliSeconds;
        String decodedDateTime = null;
        String format = null;
        if (FormatType.matches("^%tc.*")) {
            milliSeconds = Math.round(new Double(rawDatum)) + STATA_BIAS_TO_EPOCH;
            decodedDateTime = sdf_ymdhmsS.format(new Date(milliSeconds));
            format = sdf_ymdhmsS.toPattern();
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("tc: result=" + decodedDateTime + ", format = " + format);
        } else if (FormatType.matches("^%t?d.*")) {
            milliSeconds = Long.parseLong(rawDatum) * MILLISECONDS_PER_DAY + STATA_BIAS_TO_EPOCH;
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("milliSeconds=" + milliSeconds);
            decodedDateTime = sdf_ymd.format(new Date(milliSeconds));
            format = sdf_ymd.toPattern();
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("td:" + decodedDateTime + ", format = " + format);
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
            String month = null;
            if (left == 12L) {
                left = 0L;
            }
            Long monthdata = (left + 1);
            month = "-" + twoDigitFormatter.format(monthdata) + "-01";
            long year = 1960L + years;
            String monthYear = Long.toString(year) + month;
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("rawDatum=" + rawDatum + ": monthYear=" + monthYear);
            decodedDateTime = monthYear;
            format = "yyyy-MM-dd";
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("tm:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?q.*")) {
            long quaterYears = Math.round(new Double(rawDatum));
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
            String quaterYear = Long.toString(year) + quater;
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("rawDatum=" + rawDatum + ": quaterYear=" + quaterYear);
            decodedDateTime = quaterYear;
            format = "yyyy-MM-dd";
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("tq:" + decodedDateTime + ", format:" + format);
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
            String halfYear = Long.toString(year) + half;
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("rawDatum=" + rawDatum + ": halfYear=" + halfYear);
            decodedDateTime = halfYear;
            format = "yyyy-MM-dd";
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("th:" + decodedDateTime + ", format:" + format);
        } else if (FormatType.matches("^%t?y.*")) {
            decodedDateTime = rawDatum;
            format = "yyyy";
            if (dbgLog.isLoggable(Level.FINER))
                dbgLog.finer("th:" + decodedDateTime);
        } else {
            decodedDateTime = rawDatum;
            format = null;
        }
        DecodedDateTime retValue = new DecodedDateTime();
        retValue.decodedDateTime = decodedDateTime;
        retValue.format = format;
        return retValue;
    }
}