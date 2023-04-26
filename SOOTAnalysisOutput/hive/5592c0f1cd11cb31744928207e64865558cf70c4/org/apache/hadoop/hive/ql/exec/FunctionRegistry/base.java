package org.apache.hadoop.hive.ql.exec;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Function;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.ql.exec.FunctionUtils.UDFClassType;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.plan.ExprNodeDesc;
import org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.hadoop.hive.ql.udf.SettableUDF;
import org.apache.hadoop.hive.ql.udf.UDAFPercentile;
import org.apache.hadoop.hive.ql.udf.UDFAcos;
import org.apache.hadoop.hive.ql.udf.UDFAscii;
import org.apache.hadoop.hive.ql.udf.UDFAsin;
import org.apache.hadoop.hive.ql.udf.UDFAtan;
import org.apache.hadoop.hive.ql.udf.UDFBase64;
import org.apache.hadoop.hive.ql.udf.UDFBin;
import org.apache.hadoop.hive.ql.udf.UDFConv;
import org.apache.hadoop.hive.ql.udf.UDFCos;
import org.apache.hadoop.hive.ql.udf.UDFDayOfMonth;
import org.apache.hadoop.hive.ql.udf.UDFDegrees;
import org.apache.hadoop.hive.ql.udf.UDFE;
import org.apache.hadoop.hive.ql.udf.UDFExp;
import org.apache.hadoop.hive.ql.udf.UDFFindInSet;
import org.apache.hadoop.hive.ql.udf.UDFFromUnixTime;
import org.apache.hadoop.hive.ql.udf.UDFHex;
import org.apache.hadoop.hive.ql.udf.UDFHour;
import org.apache.hadoop.hive.ql.udf.UDFJson;
import org.apache.hadoop.hive.ql.udf.UDFLength;
import org.apache.hadoop.hive.ql.udf.UDFLike;
import org.apache.hadoop.hive.ql.udf.UDFLn;
import org.apache.hadoop.hive.ql.udf.UDFLog;
import org.apache.hadoop.hive.ql.udf.UDFLog10;
import org.apache.hadoop.hive.ql.udf.UDFLog2;
import org.apache.hadoop.hive.ql.udf.UDFMinute;
import org.apache.hadoop.hive.ql.udf.UDFMonth;
import org.apache.hadoop.hive.ql.udf.UDFOPBitAnd;
import org.apache.hadoop.hive.ql.udf.UDFOPBitNot;
import org.apache.hadoop.hive.ql.udf.UDFOPBitOr;
import org.apache.hadoop.hive.ql.udf.UDFOPBitXor;
import org.apache.hadoop.hive.ql.udf.UDFOPLongDivide;
import org.apache.hadoop.hive.ql.udf.UDFPI;
import org.apache.hadoop.hive.ql.udf.UDFParseUrl;
import org.apache.hadoop.hive.ql.udf.UDFRadians;
import org.apache.hadoop.hive.ql.udf.UDFRand;
import org.apache.hadoop.hive.ql.udf.UDFRegExp;
import org.apache.hadoop.hive.ql.udf.UDFRegExpExtract;
import org.apache.hadoop.hive.ql.udf.UDFRegExpReplace;
import org.apache.hadoop.hive.ql.udf.UDFRepeat;
import org.apache.hadoop.hive.ql.udf.UDFReverse;
import org.apache.hadoop.hive.ql.udf.UDFSecond;
import org.apache.hadoop.hive.ql.udf.UDFSign;
import org.apache.hadoop.hive.ql.udf.UDFSin;
import org.apache.hadoop.hive.ql.udf.UDFSpace;
import org.apache.hadoop.hive.ql.udf.UDFSqrt;
import org.apache.hadoop.hive.ql.udf.UDFSubstr;
import org.apache.hadoop.hive.ql.udf.UDFTan;
import org.apache.hadoop.hive.ql.udf.UDFToBoolean;
import org.apache.hadoop.hive.ql.udf.UDFToByte;
import org.apache.hadoop.hive.ql.udf.UDFToDouble;
import org.apache.hadoop.hive.ql.udf.UDFToFloat;
import org.apache.hadoop.hive.ql.udf.UDFToInteger;
import org.apache.hadoop.hive.ql.udf.UDFToLong;
import org.apache.hadoop.hive.ql.udf.UDFToShort;
import org.apache.hadoop.hive.ql.udf.UDFToString;
import org.apache.hadoop.hive.ql.udf.UDFType;
import org.apache.hadoop.hive.ql.udf.UDFUnbase64;
import org.apache.hadoop.hive.ql.udf.UDFUnhex;
import org.apache.hadoop.hive.ql.udf.UDFWeekOfYear;
import org.apache.hadoop.hive.ql.udf.UDFYear;
import org.apache.hadoop.hive.ql.udf.generic.*;
import org.apache.hadoop.hive.ql.udf.ptf.MatchPath.MatchPathResolver;
import org.apache.hadoop.hive.ql.udf.ptf.Noop.NoopResolver;
import org.apache.hadoop.hive.ql.udf.ptf.NoopStreaming.NoopStreamingResolver;
import org.apache.hadoop.hive.ql.udf.ptf.NoopWithMap.NoopWithMapResolver;
import org.apache.hadoop.hive.ql.udf.ptf.NoopWithMapStreaming.NoopWithMapStreamingResolver;
import org.apache.hadoop.hive.ql.udf.ptf.TableFunctionResolver;
import org.apache.hadoop.hive.ql.udf.ptf.WindowingTableFunction.WindowingTableFunctionResolver;
import org.apache.hadoop.hive.ql.udf.xml.GenericUDFXPath;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathBoolean;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathDouble;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathFloat;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathInteger;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathLong;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathShort;
import org.apache.hadoop.hive.ql.udf.xml.UDFXPathString;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector.Category;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils.PrimitiveGrouping;
import org.apache.hadoop.hive.serde2.typeinfo.HiveDecimalUtils;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hive.common.util.AnnotationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public final class FunctionRegistry {

    private static Log LOG = LogFactory.getLog(FunctionRegistry.class);

    static Map<String, FunctionInfo> mFunctions = Collections.synchronizedMap(new LinkedHashMap<String, FunctionInfo>());

    static Set<Class<?>> nativeUdfs = Collections.synchronizedSet(new HashSet<Class<?>>());

    public static final String LEAD_FUNC_NAME = "lead";

    public static final String LAG_FUNC_NAME = "lag";

    public static final String LAST_VALUE_FUNC_NAME = "last_value";

    public static final String UNARY_PLUS_FUNC_NAME = "positive";

    public static final String UNARY_MINUS_FUNC_NAME = "negative";

    public static final String WINDOWING_TABLE_FUNCTION = "windowingtablefunction";

    private static final String NOOP_TABLE_FUNCTION = "noop";

    private static final String NOOP_MAP_TABLE_FUNCTION = "noopwithmap";

    private static final String NOOP_STREAMING_TABLE_FUNCTION = "noopstreaming";

    private static final String NOOP_STREAMING_MAP_TABLE_FUNCTION = "noopwithmapstreaming";

    static Map<String, WindowFunctionInfo> windowFunctions = Collections.synchronizedMap(new LinkedHashMap<String, WindowFunctionInfo>());

    static {
        registerGenericUDF("concat", GenericUDFConcat.class);
        registerUDF("substr", UDFSubstr.class, false);
        registerUDF("substring", UDFSubstr.class, false);
        registerUDF("space", UDFSpace.class, false);
        registerUDF("repeat", UDFRepeat.class, false);
        registerUDF("ascii", UDFAscii.class, false);
        registerGenericUDF("lpad", GenericUDFLpad.class);
        registerGenericUDF("rpad", GenericUDFRpad.class);
        registerGenericUDF("size", GenericUDFSize.class);
        registerGenericUDF("round", GenericUDFRound.class);
        registerGenericUDF("floor", GenericUDFFloor.class);
        registerUDF("sqrt", UDFSqrt.class, false);
        registerGenericUDF("ceil", GenericUDFCeil.class);
        registerGenericUDF("ceiling", GenericUDFCeil.class);
        registerUDF("rand", UDFRand.class, false);
        registerGenericUDF("abs", GenericUDFAbs.class);
        registerGenericUDF("pmod", GenericUDFPosMod.class);
        registerUDF("ln", UDFLn.class, false);
        registerUDF("log2", UDFLog2.class, false);
        registerUDF("sin", UDFSin.class, false);
        registerUDF("asin", UDFAsin.class, false);
        registerUDF("cos", UDFCos.class, false);
        registerUDF("acos", UDFAcos.class, false);
        registerUDF("log10", UDFLog10.class, false);
        registerUDF("log", UDFLog.class, false);
        registerUDF("exp", UDFExp.class, false);
        registerGenericUDF("power", GenericUDFPower.class);
        registerGenericUDF("pow", GenericUDFPower.class);
        registerUDF("sign", UDFSign.class, false);
        registerUDF("pi", UDFPI.class, false);
        registerUDF("degrees", UDFDegrees.class, false);
        registerUDF("radians", UDFRadians.class, false);
        registerUDF("atan", UDFAtan.class, false);
        registerUDF("tan", UDFTan.class, false);
        registerUDF("e", UDFE.class, false);
        registerUDF("conv", UDFConv.class, false);
        registerUDF("bin", UDFBin.class, false);
        registerUDF("hex", UDFHex.class, false);
        registerUDF("unhex", UDFUnhex.class, false);
        registerUDF("base64", UDFBase64.class, false);
        registerUDF("unbase64", UDFUnbase64.class, false);
        registerGenericUDF("encode", GenericUDFEncode.class);
        registerGenericUDF("decode", GenericUDFDecode.class);
        registerGenericUDF("upper", GenericUDFUpper.class);
        registerGenericUDF("lower", GenericUDFLower.class);
        registerGenericUDF("ucase", GenericUDFUpper.class);
        registerGenericUDF("lcase", GenericUDFLower.class);
        registerGenericUDF("trim", GenericUDFTrim.class);
        registerGenericUDF("ltrim", GenericUDFLTrim.class);
        registerGenericUDF("rtrim", GenericUDFRTrim.class);
        registerUDF("length", UDFLength.class, false);
        registerUDF("reverse", UDFReverse.class, false);
        registerGenericUDF("field", GenericUDFField.class);
        registerUDF("find_in_set", UDFFindInSet.class, false);
        registerGenericUDF("initcap", GenericUDFInitCap.class);
        registerUDF("like", UDFLike.class, true);
        registerUDF("rlike", UDFRegExp.class, true);
        registerUDF("regexp", UDFRegExp.class, true);
        registerUDF("regexp_replace", UDFRegExpReplace.class, false);
        registerUDF("regexp_extract", UDFRegExpExtract.class, false);
        registerUDF("parse_url", UDFParseUrl.class, false);
        registerGenericUDF("nvl", GenericUDFNvl.class);
        registerGenericUDF("split", GenericUDFSplit.class);
        registerGenericUDF("str_to_map", GenericUDFStringToMap.class);
        registerGenericUDF("translate", GenericUDFTranslate.class);
        registerGenericUDF(UNARY_PLUS_FUNC_NAME, GenericUDFOPPositive.class);
        registerGenericUDF(UNARY_MINUS_FUNC_NAME, GenericUDFOPNegative.class);
        registerUDF("day", UDFDayOfMonth.class, false);
        registerUDF("dayofmonth", UDFDayOfMonth.class, false);
        registerUDF("month", UDFMonth.class, false);
        registerUDF("year", UDFYear.class, false);
        registerUDF("hour", UDFHour.class, false);
        registerUDF("minute", UDFMinute.class, false);
        registerUDF("second", UDFSecond.class, false);
        registerUDF("from_unixtime", UDFFromUnixTime.class, false);
        registerGenericUDF("to_date", GenericUDFDate.class);
        registerUDF("weekofyear", UDFWeekOfYear.class, false);
        registerGenericUDF("last_day", GenericUDFLastDay.class);
        registerGenericUDF("date_add", GenericUDFDateAdd.class);
        registerGenericUDF("date_sub", GenericUDFDateSub.class);
        registerGenericUDF("datediff", GenericUDFDateDiff.class);
        registerGenericUDF("add_months", GenericUDFAddMonths.class);
        registerUDF("get_json_object", UDFJson.class, false);
        registerUDF("xpath_string", UDFXPathString.class, false);
        registerUDF("xpath_boolean", UDFXPathBoolean.class, false);
        registerUDF("xpath_number", UDFXPathDouble.class, false);
        registerUDF("xpath_double", UDFXPathDouble.class, false);
        registerUDF("xpath_float", UDFXPathFloat.class, false);
        registerUDF("xpath_long", UDFXPathLong.class, false);
        registerUDF("xpath_int", UDFXPathInteger.class, false);
        registerUDF("xpath_short", UDFXPathShort.class, false);
        registerGenericUDF("xpath", GenericUDFXPath.class);
        registerGenericUDF("+", GenericUDFOPPlus.class);
        registerGenericUDF("-", GenericUDFOPMinus.class);
        registerGenericUDF("*", GenericUDFOPMultiply.class);
        registerGenericUDF("/", GenericUDFOPDivide.class);
        registerGenericUDF("%", GenericUDFOPMod.class);
        registerUDF("div", UDFOPLongDivide.class, true);
        registerUDF("&", UDFOPBitAnd.class, true);
        registerUDF("|", UDFOPBitOr.class, true);
        registerUDF("^", UDFOPBitXor.class, true);
        registerUDF("~", UDFOPBitNot.class, true);
        registerGenericUDF("current_database", UDFCurrentDB.class);
        registerGenericUDF("current_date", GenericUDFCurrentDate.class);
        registerGenericUDF("current_timestamp", GenericUDFCurrentTimestamp.class);
        registerGenericUDF("current_user", GenericUDFCurrentUser.class);
        registerGenericUDF("isnull", GenericUDFOPNull.class);
        registerGenericUDF("isnotnull", GenericUDFOPNotNull.class);
        registerGenericUDF("if", GenericUDFIf.class);
        registerGenericUDF("in", GenericUDFIn.class);
        registerGenericUDF("and", GenericUDFOPAnd.class);
        registerGenericUDF("or", GenericUDFOPOr.class);
        registerGenericUDF("=", GenericUDFOPEqual.class);
        registerGenericUDF("==", GenericUDFOPEqual.class);
        registerGenericUDF("<=>", GenericUDFOPEqualNS.class);
        registerGenericUDF("!=", GenericUDFOPNotEqual.class);
        registerGenericUDF("<>", GenericUDFOPNotEqual.class);
        registerGenericUDF("<", GenericUDFOPLessThan.class);
        registerGenericUDF("<=", GenericUDFOPEqualOrLessThan.class);
        registerGenericUDF(">", GenericUDFOPGreaterThan.class);
        registerGenericUDF(">=", GenericUDFOPEqualOrGreaterThan.class);
        registerGenericUDF("not", GenericUDFOPNot.class);
        registerGenericUDF("!", GenericUDFOPNot.class);
        registerGenericUDF("between", GenericUDFBetween.class);
        registerGenericUDF("ewah_bitmap_and", GenericUDFEWAHBitmapAnd.class);
        registerGenericUDF("ewah_bitmap_or", GenericUDFEWAHBitmapOr.class);
        registerGenericUDF("ewah_bitmap_empty", GenericUDFEWAHBitmapEmpty.class);
        registerUDF(serdeConstants.BOOLEAN_TYPE_NAME, UDFToBoolean.class, false, UDFToBoolean.class.getSimpleName());
        registerUDF(serdeConstants.TINYINT_TYPE_NAME, UDFToByte.class, false, UDFToByte.class.getSimpleName());
        registerUDF(serdeConstants.SMALLINT_TYPE_NAME, UDFToShort.class, false, UDFToShort.class.getSimpleName());
        registerUDF(serdeConstants.INT_TYPE_NAME, UDFToInteger.class, false, UDFToInteger.class.getSimpleName());
        registerUDF(serdeConstants.BIGINT_TYPE_NAME, UDFToLong.class, false, UDFToLong.class.getSimpleName());
        registerUDF(serdeConstants.FLOAT_TYPE_NAME, UDFToFloat.class, false, UDFToFloat.class.getSimpleName());
        registerUDF(serdeConstants.DOUBLE_TYPE_NAME, UDFToDouble.class, false, UDFToDouble.class.getSimpleName());
        registerUDF(serdeConstants.STRING_TYPE_NAME, UDFToString.class, false, UDFToString.class.getSimpleName());
        registerGenericUDF(serdeConstants.DATE_TYPE_NAME, GenericUDFToDate.class);
        registerGenericUDF(serdeConstants.TIMESTAMP_TYPE_NAME, GenericUDFTimestamp.class);
        registerGenericUDF(serdeConstants.BINARY_TYPE_NAME, GenericUDFToBinary.class);
        registerGenericUDF(serdeConstants.DECIMAL_TYPE_NAME, GenericUDFToDecimal.class);
        registerGenericUDF(serdeConstants.VARCHAR_TYPE_NAME, GenericUDFToVarchar.class);
        registerGenericUDF(serdeConstants.CHAR_TYPE_NAME, GenericUDFToChar.class);
        registerGenericUDAF("max", new GenericUDAFMax());
        registerGenericUDAF("min", new GenericUDAFMin());
        registerGenericUDAF("sum", new GenericUDAFSum());
        registerGenericUDAF("count", new GenericUDAFCount());
        registerGenericUDAF("avg", new GenericUDAFAverage());
        registerGenericUDAF("std", new GenericUDAFStd());
        registerGenericUDAF("stddev", new GenericUDAFStd());
        registerGenericUDAF("stddev_pop", new GenericUDAFStd());
        registerGenericUDAF("stddev_samp", new GenericUDAFStdSample());
        registerGenericUDAF("variance", new GenericUDAFVariance());
        registerGenericUDAF("var_pop", new GenericUDAFVariance());
        registerGenericUDAF("var_samp", new GenericUDAFVarianceSample());
        registerGenericUDAF("covar_pop", new GenericUDAFCovariance());
        registerGenericUDAF("covar_samp", new GenericUDAFCovarianceSample());
        registerGenericUDAF("corr", new GenericUDAFCorrelation());
        registerGenericUDAF("histogram_numeric", new GenericUDAFHistogramNumeric());
        registerGenericUDAF("percentile_approx", new GenericUDAFPercentileApprox());
        registerGenericUDAF("collect_set", new GenericUDAFCollectSet());
        registerGenericUDAF("collect_list", new GenericUDAFCollectList());
        registerGenericUDAF("ngrams", new GenericUDAFnGrams());
        registerGenericUDAF("context_ngrams", new GenericUDAFContextNGrams());
        registerGenericUDAF("ewah_bitmap", new GenericUDAFEWAHBitmap());
        registerGenericUDAF("compute_stats", new GenericUDAFComputeStats());
        registerUDAF("percentile", UDAFPercentile.class);
        registerGenericUDF("reflect", GenericUDFReflect.class);
        registerGenericUDF("reflect2", GenericUDFReflect2.class);
        registerGenericUDF("java_method", GenericUDFReflect.class);
        registerGenericUDF("array", GenericUDFArray.class);
        registerGenericUDF("assert_true", GenericUDFAssertTrue.class);
        registerGenericUDF("map", GenericUDFMap.class);
        registerGenericUDF("struct", GenericUDFStruct.class);
        registerGenericUDF("named_struct", GenericUDFNamedStruct.class);
        registerGenericUDF("create_union", GenericUDFUnion.class);
        registerGenericUDF("case", GenericUDFCase.class);
        registerGenericUDF("when", GenericUDFWhen.class);
        registerGenericUDF("hash", GenericUDFHash.class);
        registerGenericUDF("coalesce", GenericUDFCoalesce.class);
        registerGenericUDF("index", GenericUDFIndex.class);
        registerGenericUDF("in_file", GenericUDFInFile.class);
        registerGenericUDF("instr", GenericUDFInstr.class);
        registerGenericUDF("locate", GenericUDFLocate.class);
        registerGenericUDF("elt", GenericUDFElt.class);
        registerGenericUDF("concat_ws", GenericUDFConcatWS.class);
        registerGenericUDF("sort_array", GenericUDFSortArray.class);
        registerGenericUDF("array_contains", GenericUDFArrayContains.class);
        registerGenericUDF("sentences", GenericUDFSentences.class);
        registerGenericUDF("map_keys", GenericUDFMapKeys.class);
        registerGenericUDF("map_values", GenericUDFMapValues.class);
        registerGenericUDF("format_number", GenericUDFFormatNumber.class);
        registerGenericUDF("printf", GenericUDFPrintf.class);
        registerGenericUDF("greatest", GenericUDFGreatest.class);
        registerGenericUDF("least", GenericUDFLeast.class);
        registerGenericUDF("from_utc_timestamp", GenericUDFFromUtcTimestamp.class);
        registerGenericUDF("to_utc_timestamp", GenericUDFToUtcTimestamp.class);
        registerGenericUDF("unix_timestamp", GenericUDFUnixTimeStamp.class);
        registerGenericUDF("to_unix_timestamp", GenericUDFToUnixTimeStamp.class);
        registerGenericUDTF("explode", GenericUDTFExplode.class);
        registerGenericUDTF("inline", GenericUDTFInline.class);
        registerGenericUDTF("json_tuple", GenericUDTFJSONTuple.class);
        registerGenericUDTF("parse_url_tuple", GenericUDTFParseUrlTuple.class);
        registerGenericUDTF("posexplode", GenericUDTFPosExplode.class);
        registerGenericUDTF("stack", GenericUDTFStack.class);
        registerGenericUDF(LEAD_FUNC_NAME, GenericUDFLead.class);
        registerGenericUDF(LAG_FUNC_NAME, GenericUDFLag.class);
        registerWindowFunction("row_number", new GenericUDAFRowNumber());
        registerWindowFunction("rank", new GenericUDAFRank());
        registerWindowFunction("dense_rank", new GenericUDAFDenseRank());
        registerWindowFunction("percent_rank", new GenericUDAFPercentRank());
        registerWindowFunction("cume_dist", new GenericUDAFCumeDist());
        registerWindowFunction("ntile", new GenericUDAFNTile());
        registerWindowFunction("first_value", new GenericUDAFFirstValue());
        registerWindowFunction("last_value", new GenericUDAFLastValue());
        registerWindowFunction(LEAD_FUNC_NAME, new GenericUDAFLead(), false);
        registerWindowFunction(LAG_FUNC_NAME, new GenericUDAFLag(), false);
        registerTableFunction(NOOP_TABLE_FUNCTION, NoopResolver.class);
        registerTableFunction(NOOP_MAP_TABLE_FUNCTION, NoopWithMapResolver.class);
        registerTableFunction(NOOP_STREAMING_TABLE_FUNCTION, NoopStreamingResolver.class);
        registerTableFunction(NOOP_STREAMING_MAP_TABLE_FUNCTION, NoopWithMapStreamingResolver.class);
        registerTableFunction(WINDOWING_TABLE_FUNCTION, WindowingTableFunctionResolver.class);
        registerTableFunction("matchpath", MatchPathResolver.class);
    }

    public static void registerTemporaryUDF(String functionName, Class<? extends UDF> UDFClass, boolean isOperator) {
        registerUDF(false, functionName, UDFClass, isOperator);
    }

    static void registerUDF(String functionName, Class<? extends UDF> UDFClass, boolean isOperator) {
        registerUDF(true, functionName, UDFClass, isOperator);
    }

    public static void registerUDF(boolean isNative, String functionName, Class<? extends UDF> UDFClass, boolean isOperator) {
        registerUDF(isNative, functionName, UDFClass, isOperator, functionName.toLowerCase());
    }

    public static void registerUDF(String functionName, Class<? extends UDF> UDFClass, boolean isOperator, String displayName) {
        registerUDF(true, functionName, UDFClass, isOperator, displayName);
    }

    public static void registerUDF(boolean isNative, String functionName, Class<? extends UDF> UDFClass, boolean isOperator, String displayName) {
        if (UDF.class.isAssignableFrom(UDFClass)) {
            FunctionInfo fI = new FunctionInfo(isNative, displayName, new GenericUDFBridge(displayName, isOperator, UDFClass.getName()));
            mFunctions.put(functionName.toLowerCase(), fI);
            registerNativeStatus(fI);
        } else {
            throw new RuntimeException("Registering UDF Class " + UDFClass + " which does not extend " + UDF.class);
        }
    }

    public static void registerTemporaryGenericUDF(String functionName, Class<? extends GenericUDF> genericUDFClass) {
        registerGenericUDF(false, functionName, genericUDFClass);
    }

    static void registerGenericUDF(String functionName, Class<? extends GenericUDF> genericUDFClass) {
        registerGenericUDF(true, functionName, genericUDFClass);
    }

    public static void registerGenericUDF(boolean isNative, String functionName, Class<? extends GenericUDF> genericUDFClass) {
        if (GenericUDF.class.isAssignableFrom(genericUDFClass)) {
            FunctionInfo fI = new FunctionInfo(isNative, functionName, ReflectionUtils.newInstance(genericUDFClass, null));
            mFunctions.put(functionName.toLowerCase(), fI);
            registerNativeStatus(fI);
        } else {
            throw new RuntimeException("Registering GenericUDF Class " + genericUDFClass + " which does not extend " + GenericUDF.class);
        }
    }

    public static void registerTemporaryGenericUDTF(String functionName, Class<? extends GenericUDTF> genericUDTFClass) {
        registerGenericUDTF(false, functionName, genericUDTFClass);
    }

    static void registerGenericUDTF(String functionName, Class<? extends GenericUDTF> genericUDTFClass) {
        registerGenericUDTF(true, functionName, genericUDTFClass);
    }

    public static void registerGenericUDTF(boolean isNative, String functionName, Class<? extends GenericUDTF> genericUDTFClass) {
        if (GenericUDTF.class.isAssignableFrom(genericUDTFClass)) {
            FunctionInfo fI = new FunctionInfo(isNative, functionName, ReflectionUtils.newInstance(genericUDTFClass, null));
            mFunctions.put(functionName.toLowerCase(), fI);
            registerNativeStatus(fI);
        } else {
            throw new RuntimeException("Registering GenericUDTF Class " + genericUDTFClass + " which does not extend " + GenericUDTF.class);
        }
    }

    private static FunctionInfo getFunctionInfoFromMetastore(String functionName) {
        FunctionInfo ret = null;
        try {
            String dbName;
            String fName;
            if (FunctionUtils.isQualifiedFunctionName(functionName)) {
                String[] parts = FunctionUtils.splitQualifiedFunctionName(functionName);
                dbName = parts[0];
                fName = parts[1];
            } else {
                dbName = SessionState.get().getCurrentDatabase().toLowerCase();
                fName = functionName;
            }
            HiveConf conf = SessionState.get().getConf();
            Function func = Hive.get(conf).getFunction(dbName, fName);
            if (func != null) {
                try {
                    FunctionTask.addFunctionResources(func.getResourceUris());
                } catch (Exception e) {
                    LOG.error("Unable to load resources for " + dbName + "." + fName + ":" + e.getMessage(), e);
                    return null;
                }
                Class<?> udfClass = Class.forName(func.getClassName(), true, Utilities.getSessionSpecifiedClassLoader());
                if (registerTemporaryFunction(functionName, udfClass)) {
                    ret = mFunctions.get(functionName);
                } else {
                    LOG.error(func.getClassName() + " is not a valid UDF class and was not registered.");
                }
            }
        } catch (HiveException e) {
            if (!((e.getCause() != null) && (e.getCause() instanceof MetaException)) && (e.getCause().getCause() != null) && (e.getCause().getCause() instanceof NoSuchObjectException)) {
                LOG.info("Unable to lookup UDF in metastore: " + e);
            }
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to load UDF class: " + e);
        }
        return ret;
    }

    private static <T extends CommonFunctionInfo> T getQualifiedFunctionInfo(Map<String, T> mFunctions, String functionName) {
        T functionInfo = mFunctions.get(functionName);
        if (functionInfo == null) {
            FunctionInfo fi = getFunctionInfoFromMetastore(functionName);
            if (fi != null) {
                functionInfo = mFunctions.get(functionName);
            }
        }
        if (functionInfo != null) {
            loadFunctionResourcesIfNecessary(functionName, functionInfo);
        }
        return functionInfo;
    }

    private static void checkFunctionClass(CommonFunctionInfo cfi) throws ClassNotFoundException {
        Class<?> udfClass = cfi.getFunctionClass();
        Class.forName(udfClass.getName(), true, Utilities.getSessionSpecifiedClassLoader());
    }

    private static void loadFunctionResourcesIfNecessary(String functionName, CommonFunctionInfo cfi) {
        try {
            checkFunctionClass(cfi);
        } catch (Exception e) {
            LOG.debug("Attempting to reload resources for " + functionName);
            try {
                String[] parts = FunctionUtils.getQualifiedFunctionNameParts(functionName);
                HiveConf conf = SessionState.get().getConf();
                Function func = Hive.get(conf).getFunction(parts[0], parts[1]);
                if (func != null) {
                    FunctionTask.addFunctionResources(func.getResourceUris());
                    checkFunctionClass(cfi);
                } else {
                    LOG.error("Unable to reload resources for " + functionName);
                    throw e;
                }
            } catch (Exception err) {
                throw new RuntimeException(err);
            }
        }
    }

    public static String getNormalizedFunctionName(String fn) {
        fn = fn.toLowerCase();
        return (FunctionUtils.isQualifiedFunctionName(fn) || mFunctions.get(fn) != null) ? fn : FunctionUtils.qualifyFunctionName(fn, SessionState.get().getCurrentDatabase().toLowerCase());
    }

    private static <T extends CommonFunctionInfo> T getFunctionInfo(Map<String, T> mFunctions, String functionName) {
        functionName = functionName.toLowerCase();
        T functionInfo = null;
        if (FunctionUtils.isQualifiedFunctionName(functionName)) {
            functionInfo = getQualifiedFunctionInfo(mFunctions, functionName);
        } else {
            functionInfo = mFunctions.get(functionName);
            if (functionInfo == null && !FunctionUtils.isQualifiedFunctionName(functionName)) {
                String qualifiedName = FunctionUtils.qualifyFunctionName(functionName, SessionState.get().getCurrentDatabase().toLowerCase());
                functionInfo = getQualifiedFunctionInfo(mFunctions, qualifiedName);
            }
        }
        return functionInfo;
    }

    public static FunctionInfo getFunctionInfo(String functionName) throws SemanticException {
        FunctionInfo functionInfo = getFunctionInfo(mFunctions, functionName);
        if (functionInfo != null && functionInfo.isBlockedFunction()) {
            throw new SemanticException("UDF " + functionName + " is not allowed");
        }
        return functionInfo;
    }

    public static Set<String> getFunctionNames() {
        return getFunctionNames(true);
    }

    private static Set<String> getFunctionNames(boolean searchMetastore) {
        Set<String> functionNames = mFunctions.keySet();
        if (searchMetastore) {
            functionNames = new HashSet<String>(functionNames);
            try {
                Hive db = getHive();
                List<String> dbNames = db.getAllDatabases();
                for (String dbName : dbNames) {
                    List<String> funcNames = db.getFunctions(dbName, "*");
                    for (String funcName : funcNames) {
                        functionNames.add(FunctionUtils.qualifyFunctionName(funcName, dbName));
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        return functionNames;
    }

    public static Hive getHive() throws HiveException {
        return Hive.get(SessionState.get().getConf());
    }

    public static Set<String> getFunctionNames(String funcPatternStr) {
        Set<String> funcNames = new TreeSet<String>();
        Pattern funcPattern = null;
        try {
            funcPattern = Pattern.compile(funcPatternStr);
        } catch (PatternSyntaxException e) {
            return funcNames;
        }
        for (String funcName : mFunctions.keySet()) {
            if (funcPattern.matcher(funcName).matches()) {
                funcNames.add(funcName);
            }
        }
        return funcNames;
    }

    public static Set<String> getFunctionNamesByLikePattern(String funcPatternStr) {
        Set<String> funcNames = new TreeSet<String>();
        Set<String> allFuncs = getFunctionNames(true);
        String[] subpatterns = funcPatternStr.trim().split("\\|");
        for (String subpattern : subpatterns) {
            subpattern = "(?i)" + subpattern.replaceAll("\\*", ".*");
            try {
                Pattern patternObj = Pattern.compile(subpattern);
                for (String funcName : allFuncs) {
                    if (patternObj.matcher(funcName).matches()) {
                        funcNames.add(funcName);
                    }
                }
            } catch (PatternSyntaxException e) {
                continue;
            }
        }
        return funcNames;
    }

    public static Set<String> getFunctionSynonyms(String funcName) {
        Set<String> synonyms = new LinkedHashSet<String>();
        FunctionInfo funcInfo;
        try {
            funcInfo = getFunctionInfo(funcName);
        } catch (SemanticException e) {
            LOG.warn("Failed to load " + funcName);
            funcInfo = null;
        }
        if (null == funcInfo) {
            return synonyms;
        }
        Class<?> funcClass = funcInfo.getFunctionClass();
        for (String name : mFunctions.keySet()) {
            if (name.equals(funcName)) {
                continue;
            }
            if (mFunctions.get(name).getFunctionClass().equals(funcClass)) {
                synonyms.add(name);
            }
        }
        return synonyms;
    }

    static EnumMap<PrimitiveCategory, Integer> numericTypes = new EnumMap<PrimitiveCategory, Integer>(PrimitiveCategory.class);

    static List<PrimitiveCategory> numericTypeList = new ArrayList<PrimitiveCategory>();

    static void registerNumericType(PrimitiveCategory primitiveCategory, int level) {
        numericTypeList.add(primitiveCategory);
        numericTypes.put(primitiveCategory, level);
    }

    static {
        registerNumericType(PrimitiveCategory.BYTE, 1);
        registerNumericType(PrimitiveCategory.SHORT, 2);
        registerNumericType(PrimitiveCategory.INT, 3);
        registerNumericType(PrimitiveCategory.LONG, 4);
        registerNumericType(PrimitiveCategory.FLOAT, 5);
        registerNumericType(PrimitiveCategory.DOUBLE, 6);
        registerNumericType(PrimitiveCategory.DECIMAL, 7);
        registerNumericType(PrimitiveCategory.STRING, 8);
    }

    public static boolean isNumericType(PrimitiveTypeInfo typeInfo) {
        switch(typeInfo.getPrimitiveCategory()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case DECIMAL:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case VARCHAR:
            case CHAR:
            case VOID:
                return true;
            default:
                return false;
        }
    }

    public static boolean isExactNumericType(PrimitiveTypeInfo typeInfo) {
        switch(typeInfo.getPrimitiveCategory()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case DECIMAL:
                return true;
            default:
                return false;
        }
    }

    static int getCommonLength(int aLen, int bLen) {
        int maxLength;
        if (aLen < 0 || bLen < 0) {
            maxLength = -1;
        } else {
            maxLength = Math.max(aLen, bLen);
        }
        return maxLength;
    }

    public static TypeInfo getTypeInfoForPrimitiveCategory(PrimitiveTypeInfo a, PrimitiveTypeInfo b, PrimitiveCategory typeCategory) {
        int maxLength;
        switch(typeCategory) {
            case CHAR:
                maxLength = getCommonLength(TypeInfoUtils.getCharacterLengthForType(a), TypeInfoUtils.getCharacterLengthForType(b));
                return TypeInfoFactory.getCharTypeInfo(maxLength);
            case VARCHAR:
                maxLength = getCommonLength(TypeInfoUtils.getCharacterLengthForType(a), TypeInfoUtils.getCharacterLengthForType(b));
                return TypeInfoFactory.getVarcharTypeInfo(maxLength);
            case DECIMAL:
                return HiveDecimalUtils.getDecimalTypeForPrimitiveCategories(a, b);
            default:
                return TypeInfoFactory.getPrimitiveTypeInfo(PrimitiveObjectInspectorUtils.getTypeEntryFromPrimitiveCategory(typeCategory).typeName);
        }
    }

    public static TypeInfo getCommonClassForUnionAll(TypeInfo a, TypeInfo b) {
        if (a.equals(b)) {
            return a;
        }
        if (a.getCategory() != Category.PRIMITIVE || b.getCategory() != Category.PRIMITIVE) {
            return null;
        }
        PrimitiveCategory pcA = ((PrimitiveTypeInfo) a).getPrimitiveCategory();
        PrimitiveCategory pcB = ((PrimitiveTypeInfo) b).getPrimitiveCategory();
        if (pcA == pcB) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcA);
        }
        PrimitiveGrouping pgA = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcA);
        PrimitiveGrouping pgB = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcB);
        if (pgA == PrimitiveGrouping.STRING_GROUP && pgB == PrimitiveGrouping.STRING_GROUP) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, PrimitiveCategory.STRING);
        }
        if (FunctionRegistry.implicitConvertible(a, b)) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcB);
        }
        if (FunctionRegistry.implicitConvertible(b, a)) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcA);
        }
        for (PrimitiveCategory t : numericTypeList) {
            if (FunctionRegistry.implicitConvertible(pcA, t) && FunctionRegistry.implicitConvertible(pcB, t)) {
                return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, t);
            }
        }
        return null;
    }

    public static TypeInfo getCommonClassForComparison(TypeInfo a, TypeInfo b) {
        if (a.equals(b)) {
            return a;
        }
        if (a.getCategory() != Category.PRIMITIVE || b.getCategory() != Category.PRIMITIVE) {
            return null;
        }
        PrimitiveCategory pcA = ((PrimitiveTypeInfo) a).getPrimitiveCategory();
        PrimitiveCategory pcB = ((PrimitiveTypeInfo) b).getPrimitiveCategory();
        if (pcA == pcB) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcA);
        }
        PrimitiveGrouping pgA = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcA);
        PrimitiveGrouping pgB = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcB);
        if (pgA == PrimitiveGrouping.STRING_GROUP && pgB == PrimitiveGrouping.STRING_GROUP) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, PrimitiveCategory.STRING);
        }
        if ((pgA == PrimitiveGrouping.NUMERIC_GROUP || pgB == PrimitiveGrouping.NUMERIC_GROUP) && (pcA == PrimitiveCategory.TIMESTAMP || pcB == PrimitiveCategory.TIMESTAMP)) {
            return TypeInfoFactory.doubleTypeInfo;
        }
        for (PrimitiveCategory t : numericTypeList) {
            if (FunctionRegistry.implicitConvertible(pcA, t) && FunctionRegistry.implicitConvertible(pcB, t)) {
                return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, t);
            }
        }
        return null;
    }

    public static PrimitiveCategory getCommonCategory(TypeInfo a, TypeInfo b) {
        if (a.getCategory() != Category.PRIMITIVE || b.getCategory() != Category.PRIMITIVE) {
            return null;
        }
        PrimitiveCategory pcA = ((PrimitiveTypeInfo) a).getPrimitiveCategory();
        PrimitiveCategory pcB = ((PrimitiveTypeInfo) b).getPrimitiveCategory();
        PrimitiveGrouping pgA = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcA);
        PrimitiveGrouping pgB = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(pcB);
        if (pgA == PrimitiveGrouping.STRING_GROUP && pgB == PrimitiveGrouping.STRING_GROUP) {
            return PrimitiveCategory.STRING;
        }
        Integer ai = numericTypes.get(pcA);
        Integer bi = numericTypes.get(pcB);
        if (ai == null || bi == null) {
            return null;
        }
        return (ai > bi) ? pcA : pcB;
    }

    public static TypeInfo getCommonClass(TypeInfo a, TypeInfo b) {
        if (a.equals(b)) {
            return a;
        }
        PrimitiveCategory commonCat = getCommonCategory(a, b);
        if (commonCat == null)
            return null;
        return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, commonCat);
    }

    public static boolean implicitConvertible(PrimitiveCategory from, PrimitiveCategory to) {
        if (from == to) {
            return true;
        }
        PrimitiveGrouping fromPg = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(from);
        PrimitiveGrouping toPg = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(to);
        if (fromPg == PrimitiveGrouping.STRING_GROUP && to == PrimitiveCategory.DOUBLE) {
            return true;
        }
        if (fromPg == PrimitiveGrouping.STRING_GROUP && to == PrimitiveCategory.DECIMAL) {
            return true;
        }
        if (from == PrimitiveCategory.VOID) {
            return true;
        }
        if (fromPg == PrimitiveGrouping.DATE_GROUP && toPg == PrimitiveGrouping.STRING_GROUP) {
            return true;
        }
        if (fromPg == PrimitiveGrouping.NUMERIC_GROUP && toPg == PrimitiveGrouping.STRING_GROUP) {
            return true;
        }
        if (fromPg == PrimitiveGrouping.STRING_GROUP && toPg == PrimitiveGrouping.STRING_GROUP) {
            return true;
        }
        Integer f = numericTypes.get(from);
        Integer t = numericTypes.get(to);
        if (f == null || t == null) {
            return false;
        }
        if (f.intValue() > t.intValue()) {
            return false;
        }
        return true;
    }

    public static boolean implicitConvertible(TypeInfo from, TypeInfo to) {
        if (from.equals(to)) {
            return true;
        }
        if (from.getCategory() == Category.PRIMITIVE && to.getCategory() == Category.PRIMITIVE) {
            return implicitConvertible(((PrimitiveTypeInfo) from).getPrimitiveCategory(), ((PrimitiveTypeInfo) to).getPrimitiveCategory());
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static GenericUDAFEvaluator getGenericUDAFEvaluator(String name, List<ObjectInspector> argumentOIs, boolean isDistinct, boolean isAllColumns) throws SemanticException {
        GenericUDAFResolver udafResolver = getGenericUDAFResolver(name);
        if (udafResolver == null) {
            return null;
        }
        GenericUDAFEvaluator udafEvaluator = null;
        ObjectInspector[] args = new ObjectInspector[argumentOIs.size()];
        for (int ii = 0; ii < argumentOIs.size(); ++ii) {
            args[ii] = argumentOIs.get(ii);
        }
        GenericUDAFParameterInfo paramInfo = new SimpleGenericUDAFParameterInfo(args, isDistinct, isAllColumns);
        if (udafResolver instanceof GenericUDAFResolver2) {
            udafEvaluator = ((GenericUDAFResolver2) udafResolver).getEvaluator(paramInfo);
        } else {
            udafEvaluator = udafResolver.getEvaluator(paramInfo.getParameters());
        }
        return udafEvaluator;
    }

    @SuppressWarnings("deprecation")
    public static GenericUDAFEvaluator getGenericWindowingEvaluator(String name, List<ObjectInspector> argumentOIs, boolean isDistinct, boolean isAllColumns) throws SemanticException {
        WindowFunctionInfo finfo = windowFunctions.get(name.toLowerCase());
        if (finfo == null) {
            return null;
        }
        if (!name.toLowerCase().equals(LEAD_FUNC_NAME) && !name.toLowerCase().equals(LAG_FUNC_NAME)) {
            return getGenericUDAFEvaluator(name, argumentOIs, isDistinct, isAllColumns);
        }
        ObjectInspector[] args = new ObjectInspector[argumentOIs.size()];
        GenericUDAFResolver udafResolver = finfo.getfInfo().getGenericUDAFResolver();
        GenericUDAFParameterInfo paramInfo = new SimpleGenericUDAFParameterInfo(argumentOIs.toArray(args), isDistinct, isAllColumns);
        return ((GenericUDAFResolver2) udafResolver).getEvaluator(paramInfo);
    }

    public static <T> Method getMethodInternal(Class<? extends T> udfClass, String methodName, boolean exact, List<TypeInfo> argumentClasses) throws UDFArgumentException {
        List<Method> mlist = new ArrayList<Method>();
        for (Method m : udfClass.getMethods()) {
            if (m.getName().equals(methodName)) {
                mlist.add(m);
            }
        }
        return getMethodInternal(udfClass, mlist, exact, argumentClasses);
    }

    public static void registerTemporaryGenericUDAF(String functionName, GenericUDAFResolver genericUDAFResolver) {
        registerGenericUDAF(false, functionName, genericUDAFResolver);
    }

    static void registerGenericUDAF(String functionName, GenericUDAFResolver genericUDAFResolver) {
        registerGenericUDAF(true, functionName, genericUDAFResolver);
    }

    public static void registerGenericUDAF(boolean isNative, String functionName, GenericUDAFResolver genericUDAFResolver) {
        FunctionInfo fi = new FunctionInfo(isNative, functionName.toLowerCase(), genericUDAFResolver);
        mFunctions.put(functionName.toLowerCase(), fi);
        addFunctionInfoToWindowFunctions(functionName, fi);
        registerNativeStatus(fi);
    }

    public static void registerTemporaryUDAF(String functionName, Class<? extends UDAF> udafClass) {
        registerUDAF(false, functionName, udafClass);
    }

    static void registerUDAF(String functionName, Class<? extends UDAF> udafClass) {
        registerUDAF(true, functionName, udafClass);
    }

    public static void registerUDAF(boolean isNative, String functionName, Class<? extends UDAF> udafClass) {
        FunctionInfo fi = new FunctionInfo(isNative, functionName.toLowerCase(), new GenericUDAFBridge(ReflectionUtils.newInstance(udafClass, null)));
        mFunctions.put(functionName.toLowerCase(), fi);
        addFunctionInfoToWindowFunctions(functionName, fi);
        registerNativeStatus(fi);
    }

    public static void unregisterTemporaryUDF(String functionName) throws HiveException {
        FunctionInfo fi = mFunctions.get(functionName.toLowerCase());
        if (fi != null) {
            if (!fi.isNative()) {
                mFunctions.remove(functionName.toLowerCase());
            } else {
                throw new HiveException("Function " + functionName + " is hive native, it can't be dropped");
            }
        }
    }

    public static GenericUDAFResolver getGenericUDAFResolver(String functionName) throws SemanticException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking up GenericUDAF: " + functionName);
        }
        FunctionInfo finfo = getFunctionInfo(functionName);
        if (finfo == null) {
            return null;
        }
        GenericUDAFResolver result = finfo.getGenericUDAFResolver();
        return result;
    }

    public static Object invoke(Method m, Object thisObject, Object... arguments) throws HiveException {
        Object o;
        try {
            o = m.invoke(thisObject, arguments);
        } catch (Exception e) {
            String thisObjectString = "" + thisObject + " of class " + (thisObject == null ? "null" : thisObject.getClass().getName());
            StringBuilder argumentString = new StringBuilder();
            if (arguments == null) {
                argumentString.append("null");
            } else {
                argumentString.append("{");
                for (int i = 0; i < arguments.length; i++) {
                    if (i > 0) {
                        argumentString.append(", ");
                    }
                    if (arguments[i] == null) {
                        argumentString.append("null");
                    } else {
                        argumentString.append("" + arguments[i] + ":" + arguments[i].getClass().getName());
                    }
                }
                argumentString.append("} of size " + arguments.length);
            }
            throw new HiveException("Unable to execute method " + m + " " + " on object " + thisObjectString + " with arguments " + argumentString.toString(), e);
        }
        return o;
    }

    public static int matchCost(TypeInfo argumentPassed, TypeInfo argumentAccepted, boolean exact) {
        if (argumentAccepted.equals(argumentPassed) || TypeInfoUtils.doPrimitiveCategoriesMatch(argumentPassed, argumentAccepted)) {
            return 0;
        }
        if (argumentPassed.equals(TypeInfoFactory.voidTypeInfo)) {
            return 0;
        }
        if (argumentPassed.getCategory().equals(Category.LIST) && argumentAccepted.getCategory().equals(Category.LIST)) {
            TypeInfo argumentPassedElement = ((ListTypeInfo) argumentPassed).getListElementTypeInfo();
            TypeInfo argumentAcceptedElement = ((ListTypeInfo) argumentAccepted).getListElementTypeInfo();
            return matchCost(argumentPassedElement, argumentAcceptedElement, exact);
        }
        if (argumentPassed.getCategory().equals(Category.MAP) && argumentAccepted.getCategory().equals(Category.MAP)) {
            TypeInfo argumentPassedKey = ((MapTypeInfo) argumentPassed).getMapKeyTypeInfo();
            TypeInfo argumentAcceptedKey = ((MapTypeInfo) argumentAccepted).getMapKeyTypeInfo();
            TypeInfo argumentPassedValue = ((MapTypeInfo) argumentPassed).getMapValueTypeInfo();
            TypeInfo argumentAcceptedValue = ((MapTypeInfo) argumentAccepted).getMapValueTypeInfo();
            int cost1 = matchCost(argumentPassedKey, argumentAcceptedKey, exact);
            int cost2 = matchCost(argumentPassedValue, argumentAcceptedValue, exact);
            if (cost1 < 0 || cost2 < 0) {
                return -1;
            }
            return Math.max(cost1, cost2);
        }
        if (argumentAccepted.equals(TypeInfoFactory.unknownTypeInfo)) {
            return 1;
        }
        if (!exact && implicitConvertible(argumentPassed, argumentAccepted)) {
            return 1;
        }
        return -1;
    }

    static void filterMethodsByTypeAffinity(List<Method> udfMethods, List<TypeInfo> argumentsPassed) {
        if (udfMethods.size() > 1) {
            int currentScore = 0;
            int bestMatchScore = 0;
            Method bestMatch = null;
            for (Method m : udfMethods) {
                currentScore = 0;
                List<TypeInfo> argumentsAccepted = TypeInfoUtils.getParameterTypeInfos(m, argumentsPassed.size());
                Iterator<TypeInfo> argsPassedIter = argumentsPassed.iterator();
                for (TypeInfo acceptedType : argumentsAccepted) {
                    TypeInfo passedType = argsPassedIter.next();
                    if (acceptedType.getCategory() == Category.PRIMITIVE && passedType.getCategory() == Category.PRIMITIVE) {
                        PrimitiveGrouping acceptedPg = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(((PrimitiveTypeInfo) acceptedType).getPrimitiveCategory());
                        PrimitiveGrouping passedPg = PrimitiveObjectInspectorUtils.getPrimitiveGrouping(((PrimitiveTypeInfo) passedType).getPrimitiveCategory());
                        if (acceptedPg == passedPg) {
                            ++currentScore;
                        }
                    }
                }
                if (currentScore > bestMatchScore) {
                    bestMatchScore = currentScore;
                    bestMatch = m;
                } else if (currentScore == bestMatchScore) {
                    bestMatch = null;
                }
            }
            if (bestMatch != null) {
                udfMethods.clear();
                udfMethods.add(bestMatch);
            }
        }
    }

    public static Method getMethodInternal(Class<?> udfClass, List<Method> mlist, boolean exact, List<TypeInfo> argumentsPassed) throws UDFArgumentException {
        List<Method> udfMethods = new ArrayList<Method>();
        int leastConversionCost = Integer.MAX_VALUE;
        for (Method m : mlist) {
            List<TypeInfo> argumentsAccepted = TypeInfoUtils.getParameterTypeInfos(m, argumentsPassed.size());
            if (argumentsAccepted == null) {
                continue;
            }
            boolean match = (argumentsAccepted.size() == argumentsPassed.size());
            int conversionCost = 0;
            for (int i = 0; i < argumentsPassed.size() && match; i++) {
                int cost = matchCost(argumentsPassed.get(i), argumentsAccepted.get(i), exact);
                if (cost == -1) {
                    match = false;
                } else {
                    conversionCost += cost;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Method " + (match ? "did" : "didn't") + " match: passed = " + argumentsPassed + " accepted = " + argumentsAccepted + " method = " + m);
            }
            if (match) {
                if (conversionCost < leastConversionCost) {
                    udfMethods.clear();
                    udfMethods.add(m);
                    leastConversionCost = conversionCost;
                    if (leastConversionCost == 0) {
                        break;
                    }
                } else if (conversionCost == leastConversionCost) {
                    udfMethods.add(m);
                } else {
                }
            }
        }
        if (udfMethods.size() == 0) {
            throw new NoMatchingMethodException(udfClass, argumentsPassed, mlist);
        }
        if (udfMethods.size() > 1) {
            filterMethodsByTypeAffinity(udfMethods, argumentsPassed);
        }
        if (udfMethods.size() > 1) {
            int lowestNumericType = Integer.MAX_VALUE;
            boolean multiple = true;
            Method candidate = null;
            List<TypeInfo> referenceArguments = null;
            for (Method m : udfMethods) {
                int maxNumericType = 0;
                List<TypeInfo> argumentsAccepted = TypeInfoUtils.getParameterTypeInfos(m, argumentsPassed.size());
                if (referenceArguments == null) {
                    referenceArguments = argumentsAccepted;
                }
                Iterator<TypeInfo> referenceIterator = referenceArguments.iterator();
                for (TypeInfo accepted : argumentsAccepted) {
                    TypeInfo reference = referenceIterator.next();
                    boolean acceptedIsPrimitive = false;
                    PrimitiveCategory acceptedPrimCat = PrimitiveCategory.UNKNOWN;
                    if (accepted.getCategory() == Category.PRIMITIVE) {
                        acceptedIsPrimitive = true;
                        acceptedPrimCat = ((PrimitiveTypeInfo) accepted).getPrimitiveCategory();
                    }
                    if (acceptedIsPrimitive && numericTypes.containsKey(acceptedPrimCat)) {
                        int typeValue = numericTypes.get(acceptedPrimCat);
                        maxNumericType = typeValue > maxNumericType ? typeValue : maxNumericType;
                    } else if (!accepted.equals(reference)) {
                        throw new AmbiguousMethodException(udfClass, argumentsPassed, mlist);
                    }
                }
                if (lowestNumericType > maxNumericType) {
                    multiple = false;
                    lowestNumericType = maxNumericType;
                    candidate = m;
                } else if (maxNumericType == lowestNumericType) {
                    multiple = true;
                }
            }
            if (!multiple) {
                return candidate;
            } else {
                throw new AmbiguousMethodException(udfClass, argumentsPassed, mlist);
            }
        }
        return udfMethods.get(0);
    }

    public static GenericUDF getGenericUDFForIndex() {
        return FunctionRegistry.getFunctionInfo(mFunctions, "index").getGenericUDF();
    }

    public static GenericUDF getGenericUDFForAnd() {
        return FunctionRegistry.getFunctionInfo(mFunctions, "and").getGenericUDF();
    }

    public static GenericUDF cloneGenericUDF(GenericUDF genericUDF) {
        if (null == genericUDF) {
            return null;
        }
        GenericUDF clonedUDF = null;
        if (genericUDF instanceof GenericUDFBridge) {
            GenericUDFBridge bridge = (GenericUDFBridge) genericUDF;
            clonedUDF = new GenericUDFBridge(bridge.getUdfName(), bridge.isOperator(), bridge.getUdfClassName());
        } else if (genericUDF instanceof GenericUDFMacro) {
            GenericUDFMacro bridge = (GenericUDFMacro) genericUDF;
            clonedUDF = new GenericUDFMacro(bridge.getMacroName(), bridge.getBody(), bridge.getColNames(), bridge.getColTypes());
        } else {
            clonedUDF = ReflectionUtils.newInstance(genericUDF.getClass(), null);
        }
        if (clonedUDF != null) {
            try {
                genericUDF.copyToNewInstance(clonedUDF);
            } catch (UDFArgumentException err) {
                throw new IllegalArgumentException(err);
            }
            if (genericUDF instanceof SettableUDF) {
                try {
                    TypeInfo typeInfo = ((SettableUDF) genericUDF).getTypeInfo();
                    if (typeInfo != null) {
                        ((SettableUDF) clonedUDF).setTypeInfo(typeInfo);
                    }
                } catch (UDFArgumentException err) {
                    LOG.error("Unable to add settable data to UDF " + genericUDF.getClass());
                    throw new IllegalArgumentException(err);
                }
            }
        }
        return clonedUDF;
    }

    public static GenericUDTF cloneGenericUDTF(GenericUDTF genericUDTF) {
        if (null == genericUDTF) {
            return null;
        }
        return ReflectionUtils.newInstance(genericUDTF.getClass(), null);
    }

    private static Class<? extends GenericUDF> getGenericUDFClassFromExprDesc(ExprNodeDesc desc) {
        if (!(desc instanceof ExprNodeGenericFuncDesc)) {
            return null;
        }
        ExprNodeGenericFuncDesc genericFuncDesc = (ExprNodeGenericFuncDesc) desc;
        return genericFuncDesc.getGenericUDF().getClass();
    }

    private static Class<? extends GenericUDF> getUDFClassFromExprDesc(ExprNodeDesc desc) {
        if (!(desc instanceof ExprNodeGenericFuncDesc)) {
            return null;
        }
        ExprNodeGenericFuncDesc genericFuncDesc = (ExprNodeGenericFuncDesc) desc;
        return genericFuncDesc.getGenericUDF().getClass();
    }

    public static boolean isDeterministic(GenericUDF genericUDF) {
        if (isStateful(genericUDF)) {
            return false;
        }
        UDFType genericUDFType = AnnotationUtils.getAnnotation(genericUDF.getClass(), UDFType.class);
        if (genericUDFType != null && genericUDFType.deterministic() == false) {
            return false;
        }
        if (genericUDF instanceof GenericUDFBridge) {
            GenericUDFBridge bridge = (GenericUDFBridge) (genericUDF);
            UDFType bridgeUDFType = AnnotationUtils.getAnnotation(bridge.getUdfClass(), UDFType.class);
            if (bridgeUDFType != null && bridgeUDFType.deterministic() == false) {
                return false;
            }
        }
        if (genericUDF instanceof GenericUDFMacro) {
            GenericUDFMacro macro = (GenericUDFMacro) (genericUDF);
            return macro.isDeterministic();
        }
        return true;
    }

    public static boolean isStateful(GenericUDF genericUDF) {
        UDFType genericUDFType = AnnotationUtils.getAnnotation(genericUDF.getClass(), UDFType.class);
        if (genericUDFType != null && genericUDFType.stateful()) {
            return true;
        }
        if (genericUDF instanceof GenericUDFBridge) {
            GenericUDFBridge bridge = (GenericUDFBridge) genericUDF;
            UDFType bridgeUDFType = AnnotationUtils.getAnnotation(bridge.getUdfClass(), UDFType.class);
            if (bridgeUDFType != null && bridgeUDFType.stateful()) {
                return true;
            }
        }
        if (genericUDF instanceof GenericUDFMacro) {
            GenericUDFMacro macro = (GenericUDFMacro) (genericUDF);
            return macro.isStateful();
        }
        return false;
    }

    public static boolean isOpAndOrNot(ExprNodeDesc desc) {
        Class<? extends GenericUDF> genericUdfClass = getGenericUDFClassFromExprDesc(desc);
        return GenericUDFOPAnd.class == genericUdfClass || GenericUDFOPOr.class == genericUdfClass || GenericUDFOPNot.class == genericUdfClass;
    }

    public static boolean isOpAnd(ExprNodeDesc desc) {
        return GenericUDFOPAnd.class == getGenericUDFClassFromExprDesc(desc);
    }

    public static boolean isOpOr(ExprNodeDesc desc) {
        return GenericUDFOPOr.class == getGenericUDFClassFromExprDesc(desc);
    }

    public static boolean isOpNot(ExprNodeDesc desc) {
        return GenericUDFOPNot.class == getGenericUDFClassFromExprDesc(desc);
    }

    public static boolean isOpPositive(ExprNodeDesc desc) {
        Class<? extends GenericUDF> udfClass = getUDFClassFromExprDesc(desc);
        return GenericUDFOPPositive.class == udfClass;
    }

    public static boolean isOpCast(ExprNodeDesc desc) {
        if (!(desc instanceof ExprNodeGenericFuncDesc)) {
            return false;
        }
        return isOpCast(((ExprNodeGenericFuncDesc) desc).getGenericUDF());
    }

    public static boolean isOpCast(GenericUDF genericUDF) {
        Class udfClass = (genericUDF instanceof GenericUDFBridge) ? ((GenericUDFBridge) genericUDF).getUdfClass() : genericUDF.getClass();
        return udfClass == UDFToBoolean.class || udfClass == UDFToByte.class || udfClass == UDFToDouble.class || udfClass == UDFToFloat.class || udfClass == UDFToInteger.class || udfClass == UDFToLong.class || udfClass == UDFToShort.class || udfClass == UDFToString.class || udfClass == GenericUDFToVarchar.class || udfClass == GenericUDFToChar.class || udfClass == GenericUDFTimestamp.class || udfClass == GenericUDFToBinary.class || udfClass == GenericUDFToDate.class || udfClass == GenericUDFToDecimal.class;
    }

    public static boolean isOpPreserveInputName(ExprNodeDesc desc) {
        return isOpCast(desc);
    }

    public static boolean registerTemporaryFunction(String functionName, Class<?> udfClass) {
        UDFClassType udfClassType = FunctionUtils.getUDFClassType(udfClass);
        switch(udfClassType) {
            case UDF:
                FunctionRegistry.registerTemporaryUDF(functionName, (Class<? extends UDF>) udfClass, false);
                break;
            case GENERIC_UDF:
                FunctionRegistry.registerTemporaryGenericUDF(functionName, (Class<? extends GenericUDF>) udfClass);
                break;
            case GENERIC_UDTF:
                FunctionRegistry.registerTemporaryGenericUDTF(functionName, (Class<? extends GenericUDTF>) udfClass);
                break;
            case UDAF:
                FunctionRegistry.registerTemporaryUDAF(functionName, (Class<? extends UDAF>) udfClass);
                break;
            case GENERIC_UDAF_RESOLVER:
                FunctionRegistry.registerTemporaryGenericUDAF(functionName, (GenericUDAFResolver) ReflectionUtils.newInstance(udfClass, null));
                break;
            case TABLE_FUNCTION_RESOLVER:
                FunctionRegistry.registerTableFunction(functionName, (Class<? extends TableFunctionResolver>) udfClass);
                break;
            default:
                return false;
        }
        return true;
    }

    public static void registerTemporaryMacro(String macroName, ExprNodeDesc body, List<String> colNames, List<TypeInfo> colTypes) {
        FunctionInfo fI = new FunctionInfo(false, macroName, new GenericUDFMacro(macroName, body, colNames, colTypes));
        mFunctions.put(macroName.toLowerCase(), fI);
        registerNativeStatus(fI);
    }

    public static void registerFunctionsFromPluginJar(URL jarLocation, ClassLoader classLoader) throws Exception {
        URL url = new URL("jar:" + jarLocation + "!/META-INF/class-info.xml");
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            Element root = doc.getDocumentElement();
            if (!root.getTagName().equals("ClassList")) {
                return;
            }
            NodeList children = root.getElementsByTagName("Class");
            for (int i = 0; i < children.getLength(); ++i) {
                Element child = (Element) children.item(i);
                String javaName = child.getAttribute("javaname");
                String sqlName = child.getAttribute("sqlname");
                Class<?> udfClass = Class.forName(javaName, true, classLoader);
                boolean registered = registerTemporaryFunction(sqlName, udfClass);
                if (!registered) {
                    throw new RuntimeException("Class " + udfClass + " is not a Hive function implementation");
                }
            }
        } finally {
            IOUtils.closeStream(inputStream);
        }
    }

    private FunctionRegistry() {
    }

    public static void registerWindowFunction(String name, GenericUDAFResolver wFn) {
        registerWindowFunction(name, wFn, true);
    }

    public static void registerWindowFunction(String name, GenericUDAFResolver wFn, boolean registerAsUDAF) {
        FunctionInfo fInfo = null;
        if (registerAsUDAF) {
            registerGenericUDAF(true, name, wFn);
        } else {
            name = name.toLowerCase();
            fInfo = new FunctionInfo(true, name, wFn);
            addFunctionInfoToWindowFunctions(name, fInfo);
        }
    }

    public static WindowFunctionInfo getWindowFunctionInfo(String functionName) {
        return getFunctionInfo(windowFunctions, functionName);
    }

    public static boolean impliesOrder(String functionName) throws SemanticException {
        FunctionInfo info = getFunctionInfo(functionName);
        if (info != null) {
            if (info.isGenericUDF()) {
                UDFType type = AnnotationUtils.getAnnotation(info.getGenericUDF().getClass(), UDFType.class);
                if (type != null) {
                    return type.impliesOrder();
                }
            }
        }
        WindowFunctionInfo windowInfo = getWindowFunctionInfo(functionName);
        if (windowInfo != null) {
            return windowInfo.isImpliesOrder();
        }
        return false;
    }

    static private void addFunctionInfoToWindowFunctions(String functionName, FunctionInfo functionInfo) {
        WindowFunctionInfo wInfo = new WindowFunctionInfo(functionInfo);
        windowFunctions.put(functionName.toLowerCase(), wInfo);
    }

    public static boolean isTableFunction(String name) throws SemanticException {
        FunctionInfo tFInfo = getFunctionInfo(name);
        return tFInfo != null && !tFInfo.isInternalTableFunction() && tFInfo.isTableFunction();
    }

    public static TableFunctionResolver getTableFunctionResolver(String name) throws SemanticException {
        FunctionInfo tfInfo = getFunctionInfo(name);
        if (tfInfo.isTableFunction()) {
            return (TableFunctionResolver) ReflectionUtils.newInstance(tfInfo.getFunctionClass(), null);
        }
        return null;
    }

    public static TableFunctionResolver getWindowingTableFunction() throws SemanticException {
        return getTableFunctionResolver(WINDOWING_TABLE_FUNCTION);
    }

    public static boolean isNoopFunction(String fnName) {
        fnName = fnName.toLowerCase();
        return fnName.equals(NOOP_MAP_TABLE_FUNCTION) || fnName.equals(NOOP_STREAMING_MAP_TABLE_FUNCTION) || fnName.equals(NOOP_TABLE_FUNCTION) || fnName.equals(NOOP_STREAMING_TABLE_FUNCTION);
    }

    public static void registerTableFunction(String name, Class<? extends TableFunctionResolver> tFnCls) {
        FunctionInfo tInfo = new FunctionInfo(name, tFnCls);
        mFunctions.put(name.toLowerCase(), tInfo);
        registerNativeStatus(tInfo);
    }

    public static boolean isRankingFunction(String name) throws SemanticException {
        FunctionInfo info = getFunctionInfo(name);
        if (info == null) {
            return false;
        }
        GenericUDAFResolver res = info.getGenericUDAFResolver();
        if (res == null) {
            return false;
        }
        WindowFunctionDescription desc = AnnotationUtils.getAnnotation(res.getClass(), WindowFunctionDescription.class);
        return (desc != null) && desc.rankingFunction();
    }

    public static boolean isNativeFuncExpr(ExprNodeGenericFuncDesc fnExpr) {
        Class<?> udfClass = getUDFClassFromExprDesc(fnExpr);
        if (udfClass == null) {
            udfClass = getGenericUDFClassFromExprDesc(fnExpr);
        }
        return nativeUdfs.contains(udfClass);
    }

    private static void registerNativeStatus(FunctionInfo fi) {
        if (!fi.isNative()) {
            return;
        }
        nativeUdfs.add(fi.getFunctionClass());
    }

    public static void setupPermissionsForBuiltinUDFs(String whiteListStr, String blackListStr) {
        List<String> whiteList = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(whiteListStr));
        List<String> blackList = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(blackListStr));
        for (Entry<String, FunctionInfo> funcEntry : mFunctions.entrySet()) {
            funcEntry.getValue().setBlockedFunction(isUdfBlocked(funcEntry.getKey(), whiteList, blackList));
        }
    }

    private static boolean isUdfBlocked(String functionName, List<String> whiteList, List<String> blackList) {
        boolean inWhiteList = false;
        boolean inBlackList = false;
        if (whiteList.isEmpty()) {
            inWhiteList = true;
        } else {
            for (String allowedFunction : whiteList) {
                if (functionName.equalsIgnoreCase(allowedFunction)) {
                    inWhiteList = true;
                    break;
                }
            }
        }
        for (String blockedFunction : blackList) {
            if (functionName.equalsIgnoreCase(blockedFunction)) {
                inBlackList = true;
                break;
            }
        }
        return !inWhiteList || inBlackList;
    }
}
