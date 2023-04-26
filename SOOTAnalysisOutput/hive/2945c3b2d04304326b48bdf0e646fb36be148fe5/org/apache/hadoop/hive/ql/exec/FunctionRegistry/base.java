package org.apache.hadoop.hive.ql.exec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.hive.ql.exec.FunctionInfo.FunctionResource;
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
import org.apache.hadoop.hive.ql.udf.UDFCrc32;
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
import org.apache.hadoop.hive.ql.udf.UDFMd5;
import org.apache.hadoop.hive.ql.udf.UDFMinute;
import org.apache.hadoop.hive.ql.udf.UDFMonth;
import org.apache.hadoop.hive.ql.udf.UDFOPBitAnd;
import org.apache.hadoop.hive.ql.udf.UDFOPBitNot;
import org.apache.hadoop.hive.ql.udf.UDFOPBitOr;
import org.apache.hadoop.hive.ql.udf.UDFOPBitShiftLeft;
import org.apache.hadoop.hive.ql.udf.UDFOPBitShiftRight;
import org.apache.hadoop.hive.ql.udf.UDFOPBitShiftRightUnsigned;
import org.apache.hadoop.hive.ql.udf.UDFOPBitXor;
import org.apache.hadoop.hive.ql.udf.UDFOPLongDivide;
import org.apache.hadoop.hive.ql.udf.UDFPI;
import org.apache.hadoop.hive.ql.udf.UDFParseUrl;
import org.apache.hadoop.hive.ql.udf.UDFRadians;
import org.apache.hadoop.hive.ql.udf.UDFRand;
import org.apache.hadoop.hive.ql.udf.UDFRegExpExtract;
import org.apache.hadoop.hive.ql.udf.UDFRegExpReplace;
import org.apache.hadoop.hive.ql.udf.UDFRepeat;
import org.apache.hadoop.hive.ql.udf.UDFReverse;
import org.apache.hadoop.hive.ql.udf.UDFSecond;
import org.apache.hadoop.hive.ql.udf.UDFSha1;
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
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hive.common.util.AnnotationUtils;

public final class FunctionRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionRegistry.class);

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

    private static final String MATCH_PATH_TABLE_FUNCTION = "matchpath";

    public static final Set<String> HIVE_OPERATORS = new HashSet<String>();

    static {
        HIVE_OPERATORS.addAll(Arrays.asList("+", "-", "*", "/", "%", "div", "&", "|", "^", "~", "and", "or", "not", "!", "=", "==", "<=>", "!=", "<>", "<", "<=", ">", ">=", "index"));
    }

    private static final Registry system = new Registry(true);

    static {
        system.registerGenericUDF("concat", GenericUDFConcat.class);
        system.registerUDF("substr", UDFSubstr.class, false);
        system.registerUDF("substring", UDFSubstr.class, false);
        system.registerGenericUDF("substring_index", GenericUDFSubstringIndex.class);
        system.registerUDF("space", UDFSpace.class, false);
        system.registerUDF("repeat", UDFRepeat.class, false);
        system.registerUDF("ascii", UDFAscii.class, false);
        system.registerGenericUDF("lpad", GenericUDFLpad.class);
        system.registerGenericUDF("rpad", GenericUDFRpad.class);
        system.registerGenericUDF("levenshtein", GenericUDFLevenshtein.class);
        system.registerGenericUDF("soundex", GenericUDFSoundex.class);
        system.registerGenericUDF("size", GenericUDFSize.class);
        system.registerGenericUDF("round", GenericUDFRound.class);
        system.registerGenericUDF("bround", GenericUDFBRound.class);
        system.registerGenericUDF("floor", GenericUDFFloor.class);
        system.registerUDF("sqrt", UDFSqrt.class, false);
        system.registerGenericUDF("cbrt", GenericUDFCbrt.class);
        system.registerGenericUDF("ceil", GenericUDFCeil.class);
        system.registerGenericUDF("ceiling", GenericUDFCeil.class);
        system.registerUDF("rand", UDFRand.class, false);
        system.registerGenericUDF("abs", GenericUDFAbs.class);
        system.registerGenericUDF("pmod", GenericUDFPosMod.class);
        system.registerUDF("ln", UDFLn.class, false);
        system.registerUDF("log2", UDFLog2.class, false);
        system.registerUDF("sin", UDFSin.class, false);
        system.registerUDF("asin", UDFAsin.class, false);
        system.registerUDF("cos", UDFCos.class, false);
        system.registerUDF("acos", UDFAcos.class, false);
        system.registerUDF("log10", UDFLog10.class, false);
        system.registerUDF("log", UDFLog.class, false);
        system.registerUDF("exp", UDFExp.class, false);
        system.registerGenericUDF("power", GenericUDFPower.class);
        system.registerGenericUDF("pow", GenericUDFPower.class);
        system.registerUDF("sign", UDFSign.class, false);
        system.registerUDF("pi", UDFPI.class, false);
        system.registerUDF("degrees", UDFDegrees.class, false);
        system.registerUDF("radians", UDFRadians.class, false);
        system.registerUDF("atan", UDFAtan.class, false);
        system.registerUDF("tan", UDFTan.class, false);
        system.registerUDF("e", UDFE.class, false);
        system.registerGenericUDF("factorial", GenericUDFFactorial.class);
        system.registerUDF("crc32", UDFCrc32.class, false);
        system.registerUDF("conv", UDFConv.class, false);
        system.registerUDF("bin", UDFBin.class, false);
        system.registerUDF("hex", UDFHex.class, false);
        system.registerUDF("unhex", UDFUnhex.class, false);
        system.registerUDF("base64", UDFBase64.class, false);
        system.registerUDF("unbase64", UDFUnbase64.class, false);
        system.registerGenericUDF("sha2", GenericUDFSha2.class);
        system.registerUDF("md5", UDFMd5.class, false);
        system.registerUDF("sha1", UDFSha1.class, false);
        system.registerUDF("sha", UDFSha1.class, false);
        system.registerGenericUDF("aes_encrypt", GenericUDFAesEncrypt.class);
        system.registerGenericUDF("aes_decrypt", GenericUDFAesDecrypt.class);
        system.registerGenericUDF("encode", GenericUDFEncode.class);
        system.registerGenericUDF("decode", GenericUDFDecode.class);
        system.registerGenericUDF("upper", GenericUDFUpper.class);
        system.registerGenericUDF("lower", GenericUDFLower.class);
        system.registerGenericUDF("ucase", GenericUDFUpper.class);
        system.registerGenericUDF("lcase", GenericUDFLower.class);
        system.registerGenericUDF("trim", GenericUDFTrim.class);
        system.registerGenericUDF("ltrim", GenericUDFLTrim.class);
        system.registerGenericUDF("rtrim", GenericUDFRTrim.class);
        system.registerUDF("length", UDFLength.class, false);
        system.registerUDF("reverse", UDFReverse.class, false);
        system.registerGenericUDF("field", GenericUDFField.class);
        system.registerUDF("find_in_set", UDFFindInSet.class, false);
        system.registerGenericUDF("initcap", GenericUDFInitCap.class);
        system.registerUDF("like", UDFLike.class, true);
        system.registerGenericUDF("rlike", GenericUDFRegExp.class);
        system.registerGenericUDF("regexp", GenericUDFRegExp.class);
        system.registerUDF("regexp_replace", UDFRegExpReplace.class, false);
        system.registerUDF("regexp_extract", UDFRegExpExtract.class, false);
        system.registerUDF("parse_url", UDFParseUrl.class, false);
        system.registerGenericUDF("nvl", GenericUDFNvl.class);
        system.registerGenericUDF("split", GenericUDFSplit.class);
        system.registerGenericUDF("str_to_map", GenericUDFStringToMap.class);
        system.registerGenericUDF("translate", GenericUDFTranslate.class);
        system.registerGenericUDF(UNARY_PLUS_FUNC_NAME, GenericUDFOPPositive.class);
        system.registerGenericUDF(UNARY_MINUS_FUNC_NAME, GenericUDFOPNegative.class);
        system.registerUDF("day", UDFDayOfMonth.class, false);
        system.registerUDF("dayofmonth", UDFDayOfMonth.class, false);
        system.registerUDF("month", UDFMonth.class, false);
        system.registerGenericUDF("quarter", GenericUDFQuarter.class);
        system.registerUDF("year", UDFYear.class, false);
        system.registerUDF("hour", UDFHour.class, false);
        system.registerUDF("minute", UDFMinute.class, false);
        system.registerUDF("second", UDFSecond.class, false);
        system.registerUDF("from_unixtime", UDFFromUnixTime.class, false);
        system.registerGenericUDF("to_date", GenericUDFDate.class);
        system.registerUDF("weekofyear", UDFWeekOfYear.class, false);
        system.registerGenericUDF("last_day", GenericUDFLastDay.class);
        system.registerGenericUDF("next_day", GenericUDFNextDay.class);
        system.registerGenericUDF("trunc", GenericUDFTrunc.class);
        system.registerGenericUDF("date_format", GenericUDFDateFormat.class);
        system.registerGenericUDF("date_add", GenericUDFDateAdd.class);
        system.registerGenericUDF("date_sub", GenericUDFDateSub.class);
        system.registerGenericUDF("datediff", GenericUDFDateDiff.class);
        system.registerGenericUDF("add_months", GenericUDFAddMonths.class);
        system.registerGenericUDF("months_between", GenericUDFMonthsBetween.class);
        system.registerUDF("get_json_object", UDFJson.class, false);
        system.registerUDF("xpath_string", UDFXPathString.class, false);
        system.registerUDF("xpath_boolean", UDFXPathBoolean.class, false);
        system.registerUDF("xpath_number", UDFXPathDouble.class, false);
        system.registerUDF("xpath_double", UDFXPathDouble.class, false);
        system.registerUDF("xpath_float", UDFXPathFloat.class, false);
        system.registerUDF("xpath_long", UDFXPathLong.class, false);
        system.registerUDF("xpath_int", UDFXPathInteger.class, false);
        system.registerUDF("xpath_short", UDFXPathShort.class, false);
        system.registerGenericUDF("xpath", GenericUDFXPath.class);
        system.registerGenericUDF("+", GenericUDFOPPlus.class);
        system.registerGenericUDF("-", GenericUDFOPMinus.class);
        system.registerGenericUDF("*", GenericUDFOPMultiply.class);
        system.registerGenericUDF("/", GenericUDFOPDivide.class);
        system.registerGenericUDF("%", GenericUDFOPMod.class);
        system.registerUDF("div", UDFOPLongDivide.class, true);
        system.registerUDF("&", UDFOPBitAnd.class, true);
        system.registerUDF("|", UDFOPBitOr.class, true);
        system.registerUDF("^", UDFOPBitXor.class, true);
        system.registerUDF("~", UDFOPBitNot.class, true);
        system.registerUDF("shiftleft", UDFOPBitShiftLeft.class, true);
        system.registerUDF("shiftright", UDFOPBitShiftRight.class, true);
        system.registerUDF("shiftrightunsigned", UDFOPBitShiftRightUnsigned.class, true);
        system.registerGenericUDF("current_database", UDFCurrentDB.class);
        system.registerGenericUDF("current_date", GenericUDFCurrentDate.class);
        system.registerGenericUDF("current_timestamp", GenericUDFCurrentTimestamp.class);
        system.registerGenericUDF("current_user", GenericUDFCurrentUser.class);
        system.registerGenericUDF("isnull", GenericUDFOPNull.class);
        system.registerGenericUDF("isnotnull", GenericUDFOPNotNull.class);
        system.registerGenericUDF("if", GenericUDFIf.class);
        system.registerGenericUDF("in", GenericUDFIn.class);
        system.registerGenericUDF("and", GenericUDFOPAnd.class);
        system.registerGenericUDF("or", GenericUDFOPOr.class);
        system.registerGenericUDF("=", GenericUDFOPEqual.class);
        system.registerGenericUDF("==", GenericUDFOPEqual.class);
        system.registerGenericUDF("<=>", GenericUDFOPEqualNS.class);
        system.registerGenericUDF("!=", GenericUDFOPNotEqual.class);
        system.registerGenericUDF("<>", GenericUDFOPNotEqual.class);
        system.registerGenericUDF("<", GenericUDFOPLessThan.class);
        system.registerGenericUDF("<=", GenericUDFOPEqualOrLessThan.class);
        system.registerGenericUDF(">", GenericUDFOPGreaterThan.class);
        system.registerGenericUDF(">=", GenericUDFOPEqualOrGreaterThan.class);
        system.registerGenericUDF("not", GenericUDFOPNot.class);
        system.registerGenericUDF("!", GenericUDFOPNot.class);
        system.registerGenericUDF("between", GenericUDFBetween.class);
        system.registerGenericUDF("ewah_bitmap_and", GenericUDFEWAHBitmapAnd.class);
        system.registerGenericUDF("ewah_bitmap_or", GenericUDFEWAHBitmapOr.class);
        system.registerGenericUDF("ewah_bitmap_empty", GenericUDFEWAHBitmapEmpty.class);
        system.registerUDF(serdeConstants.BOOLEAN_TYPE_NAME, UDFToBoolean.class, false, UDFToBoolean.class.getSimpleName());
        system.registerUDF(serdeConstants.TINYINT_TYPE_NAME, UDFToByte.class, false, UDFToByte.class.getSimpleName());
        system.registerUDF(serdeConstants.SMALLINT_TYPE_NAME, UDFToShort.class, false, UDFToShort.class.getSimpleName());
        system.registerUDF(serdeConstants.INT_TYPE_NAME, UDFToInteger.class, false, UDFToInteger.class.getSimpleName());
        system.registerUDF(serdeConstants.BIGINT_TYPE_NAME, UDFToLong.class, false, UDFToLong.class.getSimpleName());
        system.registerUDF(serdeConstants.FLOAT_TYPE_NAME, UDFToFloat.class, false, UDFToFloat.class.getSimpleName());
        system.registerUDF(serdeConstants.DOUBLE_TYPE_NAME, UDFToDouble.class, false, UDFToDouble.class.getSimpleName());
        system.registerUDF(serdeConstants.STRING_TYPE_NAME, UDFToString.class, false, UDFToString.class.getSimpleName());
        system.registerGenericUDF(serdeConstants.DATE_TYPE_NAME, GenericUDFToDate.class);
        system.registerGenericUDF(serdeConstants.TIMESTAMP_TYPE_NAME, GenericUDFTimestamp.class);
        system.registerGenericUDF(serdeConstants.INTERVAL_YEAR_MONTH_TYPE_NAME, GenericUDFToIntervalYearMonth.class);
        system.registerGenericUDF(serdeConstants.INTERVAL_DAY_TIME_TYPE_NAME, GenericUDFToIntervalDayTime.class);
        system.registerGenericUDF(serdeConstants.BINARY_TYPE_NAME, GenericUDFToBinary.class);
        system.registerGenericUDF(serdeConstants.DECIMAL_TYPE_NAME, GenericUDFToDecimal.class);
        system.registerGenericUDF(serdeConstants.VARCHAR_TYPE_NAME, GenericUDFToVarchar.class);
        system.registerGenericUDF(serdeConstants.CHAR_TYPE_NAME, GenericUDFToChar.class);
        system.registerGenericUDAF("max", new GenericUDAFMax());
        system.registerGenericUDAF("min", new GenericUDAFMin());
        system.registerGenericUDAF("sum", new GenericUDAFSum());
        system.registerGenericUDAF("$SUM0", new GenericUDAFSumEmptyIsZero());
        system.registerGenericUDAF("count", new GenericUDAFCount());
        system.registerGenericUDAF("avg", new GenericUDAFAverage());
        system.registerGenericUDAF("std", new GenericUDAFStd());
        system.registerGenericUDAF("stddev", new GenericUDAFStd());
        system.registerGenericUDAF("stddev_pop", new GenericUDAFStd());
        system.registerGenericUDAF("stddev_samp", new GenericUDAFStdSample());
        system.registerGenericUDAF("variance", new GenericUDAFVariance());
        system.registerGenericUDAF("var_pop", new GenericUDAFVariance());
        system.registerGenericUDAF("var_samp", new GenericUDAFVarianceSample());
        system.registerGenericUDAF("covar_pop", new GenericUDAFCovariance());
        system.registerGenericUDAF("covar_samp", new GenericUDAFCovarianceSample());
        system.registerGenericUDAF("corr", new GenericUDAFCorrelation());
        system.registerGenericUDAF("histogram_numeric", new GenericUDAFHistogramNumeric());
        system.registerGenericUDAF("percentile_approx", new GenericUDAFPercentileApprox());
        system.registerGenericUDAF("collect_set", new GenericUDAFCollectSet());
        system.registerGenericUDAF("collect_list", new GenericUDAFCollectList());
        system.registerGenericUDAF("ngrams", new GenericUDAFnGrams());
        system.registerGenericUDAF("context_ngrams", new GenericUDAFContextNGrams());
        system.registerGenericUDAF("ewah_bitmap", new GenericUDAFEWAHBitmap());
        system.registerGenericUDAF("compute_stats", new GenericUDAFComputeStats());
        system.registerUDAF("percentile", UDAFPercentile.class);
        system.registerGenericUDF("reflect", GenericUDFReflect.class);
        system.registerGenericUDF("reflect2", GenericUDFReflect2.class);
        system.registerGenericUDF("java_method", GenericUDFReflect.class);
        system.registerGenericUDF("array", GenericUDFArray.class);
        system.registerGenericUDF("assert_true", GenericUDFAssertTrue.class);
        system.registerGenericUDF("map", GenericUDFMap.class);
        system.registerGenericUDF("struct", GenericUDFStruct.class);
        system.registerGenericUDF("named_struct", GenericUDFNamedStruct.class);
        system.registerGenericUDF("create_union", GenericUDFUnion.class);
        system.registerGenericUDF("case", GenericUDFCase.class);
        system.registerGenericUDF("when", GenericUDFWhen.class);
        system.registerGenericUDF("hash", GenericUDFHash.class);
        system.registerGenericUDF("coalesce", GenericUDFCoalesce.class);
        system.registerGenericUDF("index", GenericUDFIndex.class);
        system.registerGenericUDF("in_file", GenericUDFInFile.class);
        system.registerGenericUDF("instr", GenericUDFInstr.class);
        system.registerGenericUDF("locate", GenericUDFLocate.class);
        system.registerGenericUDF("elt", GenericUDFElt.class);
        system.registerGenericUDF("concat_ws", GenericUDFConcatWS.class);
        system.registerGenericUDF("sort_array", GenericUDFSortArray.class);
        system.registerGenericUDF("array_contains", GenericUDFArrayContains.class);
        system.registerGenericUDF("sentences", GenericUDFSentences.class);
        system.registerGenericUDF("map_keys", GenericUDFMapKeys.class);
        system.registerGenericUDF("map_values", GenericUDFMapValues.class);
        system.registerGenericUDF("format_number", GenericUDFFormatNumber.class);
        system.registerGenericUDF("printf", GenericUDFPrintf.class);
        system.registerGenericUDF("greatest", GenericUDFGreatest.class);
        system.registerGenericUDF("least", GenericUDFLeast.class);
        system.registerGenericUDF("from_utc_timestamp", GenericUDFFromUtcTimestamp.class);
        system.registerGenericUDF("to_utc_timestamp", GenericUDFToUtcTimestamp.class);
        system.registerGenericUDF("unix_timestamp", GenericUDFUnixTimeStamp.class);
        system.registerGenericUDF("to_unix_timestamp", GenericUDFToUnixTimeStamp.class);
        system.registerGenericUDTF("explode", GenericUDTFExplode.class);
        system.registerGenericUDTF("inline", GenericUDTFInline.class);
        system.registerGenericUDTF("json_tuple", GenericUDTFJSONTuple.class);
        system.registerGenericUDTF("parse_url_tuple", GenericUDTFParseUrlTuple.class);
        system.registerGenericUDTF("posexplode", GenericUDTFPosExplode.class);
        system.registerGenericUDTF("stack", GenericUDTFStack.class);
        system.registerGenericUDF(LEAD_FUNC_NAME, GenericUDFLead.class);
        system.registerGenericUDF(LAG_FUNC_NAME, GenericUDFLag.class);
        system.registerGenericUDAF("row_number", new GenericUDAFRowNumber());
        system.registerGenericUDAF("rank", new GenericUDAFRank());
        system.registerGenericUDAF("dense_rank", new GenericUDAFDenseRank());
        system.registerGenericUDAF("percent_rank", new GenericUDAFPercentRank());
        system.registerGenericUDAF("cume_dist", new GenericUDAFCumeDist());
        system.registerGenericUDAF("ntile", new GenericUDAFNTile());
        system.registerGenericUDAF("first_value", new GenericUDAFFirstValue());
        system.registerGenericUDAF("last_value", new GenericUDAFLastValue());
        system.registerWindowFunction(LEAD_FUNC_NAME, new GenericUDAFLead());
        system.registerWindowFunction(LAG_FUNC_NAME, new GenericUDAFLag());
        system.registerTableFunction(NOOP_TABLE_FUNCTION, NoopResolver.class);
        system.registerTableFunction(NOOP_MAP_TABLE_FUNCTION, NoopWithMapResolver.class);
        system.registerTableFunction(NOOP_STREAMING_TABLE_FUNCTION, NoopStreamingResolver.class);
        system.registerTableFunction(NOOP_STREAMING_MAP_TABLE_FUNCTION, NoopWithMapStreamingResolver.class);
        system.registerTableFunction(WINDOWING_TABLE_FUNCTION, WindowingTableFunctionResolver.class);
        system.registerTableFunction(MATCH_PATH_TABLE_FUNCTION, MatchPathResolver.class);
        system.registerHiddenBuiltIn(GenericUDFOPDTIMinus.class);
        system.registerHiddenBuiltIn(GenericUDFOPDTIPlus.class);
        system.registerHiddenBuiltIn(GenericUDFOPNumericMinus.class);
        system.registerHiddenBuiltIn(GenericUDFOPNumericPlus.class);
    }

    public static String getNormalizedFunctionName(String fn) throws SemanticException {
        fn = fn.toLowerCase();
        return (FunctionUtils.isQualifiedFunctionName(fn) || getFunctionInfo(fn) != null) ? fn : FunctionUtils.qualifyFunctionName(fn, SessionState.get().getCurrentDatabase().toLowerCase());
    }

    public static FunctionInfo getFunctionInfo(String functionName) throws SemanticException {
        FunctionInfo info = getTemporaryFunctionInfo(functionName);
        return info != null ? info : system.getFunctionInfo(functionName);
    }

    public static FunctionInfo getTemporaryFunctionInfo(String functionName) throws SemanticException {
        Registry registry = SessionState.getRegistry();
        return registry == null ? null : registry.getFunctionInfo(functionName);
    }

    public static WindowFunctionInfo getWindowFunctionInfo(String functionName) throws SemanticException {
        Registry registry = SessionState.getRegistry();
        WindowFunctionInfo info = registry == null ? null : registry.getWindowFunctionInfo(functionName);
        return info != null ? info : system.getWindowFunctionInfo(functionName);
    }

    public static Set<String> getFunctionNames() {
        Set<String> names = new TreeSet<String>();
        names.addAll(system.getCurrentFunctionNames());
        if (SessionState.getRegistry() != null) {
            names.addAll(SessionState.getRegistry().getCurrentFunctionNames());
        }
        return names;
    }

    public static Set<String> getFunctionNames(String funcPatternStr) {
        Set<String> funcNames = new TreeSet<String>();
        funcNames.addAll(system.getFunctionNames(funcPatternStr));
        if (SessionState.getRegistry() != null) {
            funcNames.addAll(SessionState.getRegistry().getFunctionNames(funcPatternStr));
        }
        return funcNames;
    }

    public static Set<String> getFunctionNamesByLikePattern(String funcPatternStr) {
        Set<String> funcNames = new TreeSet<String>();
        Set<String> allFuncs = getFunctionNames();
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

    public static Set<String> getFunctionSynonyms(String funcName) throws SemanticException {
        FunctionInfo funcInfo = getFunctionInfo(funcName);
        if (null == funcInfo) {
            return Collections.emptySet();
        }
        Set<String> synonyms = new LinkedHashSet<String>();
        system.getFunctionSynonyms(funcName, funcInfo, synonyms);
        if (SessionState.getRegistry() != null) {
            SessionState.getRegistry().getFunctionSynonyms(funcName, funcInfo, synonyms);
        }
        return synonyms;
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
        if (TypeInfoUtils.implicitConvertible(a, b)) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcB);
        }
        if (TypeInfoUtils.implicitConvertible(b, a)) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, pcA);
        }
        for (PrimitiveCategory t : TypeInfoUtils.numericTypeList) {
            if (TypeInfoUtils.implicitConvertible(pcA, t) && TypeInfoUtils.implicitConvertible(pcB, t)) {
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
        for (PrimitiveCategory t : TypeInfoUtils.numericTypeList) {
            if (TypeInfoUtils.implicitConvertible(pcA, t) && TypeInfoUtils.implicitConvertible(pcB, t)) {
                return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, t);
            }
        }
        return null;
    }

    public static PrimitiveCategory getPrimitiveCommonCategory(TypeInfo a, TypeInfo b) {
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
        if (pgA == PrimitiveGrouping.DATE_GROUP && pgB == PrimitiveGrouping.STRING_GROUP) {
            return PrimitiveCategory.STRING;
        }
        if (pgB == PrimitiveGrouping.DATE_GROUP && pgA == PrimitiveGrouping.STRING_GROUP) {
            return PrimitiveCategory.STRING;
        }
        Integer ai = TypeInfoUtils.numericTypes.get(pcA);
        Integer bi = TypeInfoUtils.numericTypes.get(pcB);
        if (ai == null || bi == null) {
            return null;
        }
        return (ai > bi) ? pcA : pcB;
    }

    public static TypeInfo getCommonClass(TypeInfo a, TypeInfo b) {
        if (a.equals(b)) {
            return a;
        }
        PrimitiveCategory commonCat = getPrimitiveCommonCategory(a, b);
        if (commonCat != null) {
            return getTypeInfoForPrimitiveCategory((PrimitiveTypeInfo) a, (PrimitiveTypeInfo) b, commonCat);
        }
        if (a.getCategory() == Category.STRUCT && b.getCategory() == Category.STRUCT) {
            return getCommonClassForStruct((StructTypeInfo) a, (StructTypeInfo) b);
        }
        return null;
    }

    public static TypeInfo getCommonClassForStruct(StructTypeInfo a, StructTypeInfo b) {
        if (a == b || a.equals(b)) {
            return a;
        }
        List<String> names = new ArrayList<String>();
        List<TypeInfo> typeInfos = new ArrayList<TypeInfo>();
        Iterator<String> namesIterator = a.getAllStructFieldNames().iterator();
        Iterator<String> otherNamesIterator = b.getAllStructFieldNames().iterator();
        while (namesIterator.hasNext() && otherNamesIterator.hasNext()) {
            String name = namesIterator.next();
            if (!name.equalsIgnoreCase(otherNamesIterator.next())) {
                return null;
            }
            names.add(name);
        }
        if (namesIterator.hasNext() || otherNamesIterator.hasNext()) {
            return null;
        }
        ArrayList<TypeInfo> fromTypes = a.getAllStructFieldTypeInfos();
        ArrayList<TypeInfo> toTypes = b.getAllStructFieldTypeInfos();
        for (int i = 0; i < fromTypes.size(); i++) {
            TypeInfo commonType = getCommonClass(fromTypes.get(i), toTypes.get(i));
            if (commonType == null) {
                return null;
            }
            typeInfos.add(commonType);
        }
        return TypeInfoFactory.getStructTypeInfo(names, typeInfos);
    }

    @SuppressWarnings("deprecation")
    public static GenericUDAFEvaluator getGenericUDAFEvaluator(String name, List<ObjectInspector> argumentOIs, boolean isDistinct, boolean isAllColumns) throws SemanticException {
        GenericUDAFResolver udafResolver = getGenericUDAFResolver(name);
        if (udafResolver == null) {
            return null;
        }
        ObjectInspector[] args = new ObjectInspector[argumentOIs.size()];
        for (int ii = 0; ii < argumentOIs.size(); ++ii) {
            args[ii] = argumentOIs.get(ii);
        }
        GenericUDAFParameterInfo paramInfo = new SimpleGenericUDAFParameterInfo(args, isDistinct, isAllColumns);
        GenericUDAFEvaluator udafEvaluator;
        if (udafResolver instanceof GenericUDAFResolver2) {
            udafEvaluator = ((GenericUDAFResolver2) udafResolver).getEvaluator(paramInfo);
        } else {
            udafEvaluator = udafResolver.getEvaluator(paramInfo.getParameters());
        }
        return udafEvaluator;
    }

    public static GenericUDAFEvaluator getGenericWindowingEvaluator(String name, List<ObjectInspector> argumentOIs, boolean isDistinct, boolean isAllColumns) throws SemanticException {
        Registry registry = SessionState.getRegistry();
        GenericUDAFEvaluator evaluator = registry == null ? null : registry.getGenericWindowingEvaluator(name, argumentOIs, isDistinct, isAllColumns);
        return evaluator != null ? evaluator : system.getGenericWindowingEvaluator(name, argumentOIs, isDistinct, isAllColumns);
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
        if (!exact && TypeInfoUtils.implicitConvertible(argumentPassed, argumentAccepted)) {
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
                    if (acceptedIsPrimitive && TypeInfoUtils.numericTypes.containsKey(acceptedPrimCat)) {
                        int typeValue = TypeInfoUtils.numericTypes.get(acceptedPrimCat);
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
        try {
            return FunctionRegistry.getFunctionInfo("index").getGenericUDF();
        } catch (SemanticException e) {
            throw new RuntimeException("hive operator -- never be thrown", e);
        }
    }

    public static GenericUDF getGenericUDFForAnd() {
        try {
            return FunctionRegistry.getFunctionInfo("and").getGenericUDF();
        } catch (SemanticException e) {
            throw new RuntimeException("hive operator -- never be thrown", e);
        }
    }

    public static GenericUDF cloneGenericUDF(GenericUDF genericUDF) {
        if (null == genericUDF) {
            return null;
        }
        GenericUDF clonedUDF;
        if (genericUDF instanceof GenericUDFBridge) {
            GenericUDFBridge bridge = (GenericUDFBridge) genericUDF;
            clonedUDF = new GenericUDFBridge(bridge.getUdfName(), bridge.isOperator(), bridge.getUdfClassName());
        } else if (genericUDF instanceof GenericUDFMacro) {
            GenericUDFMacro bridge = (GenericUDFMacro) genericUDF;
            clonedUDF = new GenericUDFMacro(bridge.getMacroName(), bridge.getBody().clone(), bridge.getColNames(), bridge.getColTypes());
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
        return GenericUDFOPPositive.class == getGenericUDFClassFromExprDesc(desc);
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

    public static FunctionInfo registerTemporaryUDF(String functionName, Class<?> udfClass, FunctionResource... resources) {
        return SessionState.getRegistryForWrite().registerFunction(functionName, udfClass, resources);
    }

    public static void unregisterTemporaryUDF(String functionName) throws HiveException {
        if (SessionState.getRegistry() != null) {
            SessionState.getRegistry().unregisterFunction(functionName);
        }
    }

    public static void registerTemporaryMacro(String macroName, ExprNodeDesc body, List<String> colNames, List<TypeInfo> colTypes) {
        SessionState.getRegistryForWrite().registerMacro(macroName, body, colNames, colTypes);
    }

    public static FunctionInfo registerPermanentFunction(String functionName, String className, boolean registerToSession, FunctionResource[] resources) {
        return system.registerPermanentFunction(functionName, className, registerToSession, resources);
    }

    public static boolean isPermanentFunction(ExprNodeGenericFuncDesc fnExpr) {
        GenericUDF udf = fnExpr.getGenericUDF();
        if (udf == null)
            return false;
        Class<?> clazz = udf.getClass();
        if (udf instanceof GenericUDFBridge) {
            clazz = ((GenericUDFBridge) udf).getUdfClass();
        }
        if (clazz != null) {
            return system.isPermanentFunc(clazz);
        }
        return false;
    }

    public static void unregisterPermanentFunction(String functionName) throws HiveException {
        system.unregisterFunction(functionName);
        unregisterTemporaryUDF(functionName);
    }

    public static void unregisterPermanentFunctions(String dbName) throws HiveException {
        system.unregisterFunctions(dbName);
    }

    private FunctionRegistry() {
    }

    public static boolean impliesOrder(String functionName) throws SemanticException {
        FunctionInfo info = getFunctionInfo(functionName);
        if (info != null && info.isGenericUDF()) {
            UDFType type = AnnotationUtils.getAnnotation(info.getGenericUDF().getClass(), UDFType.class);
            if (type != null) {
                return type.impliesOrder();
            }
        }
        WindowFunctionInfo windowInfo = getWindowFunctionInfo(functionName);
        if (windowInfo != null) {
            return windowInfo.isImpliesOrder();
        }
        return false;
    }

    public static boolean pivotResult(String functionName) throws SemanticException {
        WindowFunctionInfo windowInfo = getWindowFunctionInfo(functionName);
        if (windowInfo != null) {
            return windowInfo.isPivotResult();
        }
        return false;
    }

    public static boolean isTableFunction(String functionName) throws SemanticException {
        FunctionInfo tFInfo = getFunctionInfo(functionName);
        return tFInfo != null && !tFInfo.isInternalTableFunction() && tFInfo.isTableFunction();
    }

    public static TableFunctionResolver getTableFunctionResolver(String functionName) throws SemanticException {
        FunctionInfo tfInfo = getFunctionInfo(functionName);
        if (tfInfo != null && tfInfo.isTableFunction()) {
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

    public static boolean isBuiltInFuncExpr(ExprNodeGenericFuncDesc fnExpr) {
        GenericUDF udf = fnExpr.getGenericUDF();
        if (udf == null)
            return false;
        Class clazz = udf.getClass();
        if (udf instanceof GenericUDFBridge) {
            clazz = ((GenericUDFBridge) udf).getUdfClass();
        }
        if (clazz != null) {
            return system.isBuiltInFunc(clazz);
        }
        return false;
    }

    public static boolean isBuiltInFuncClass(Class<?> clazz) {
        return system.isBuiltInFunc(clazz);
    }

    public static void setupPermissionsForBuiltinUDFs(String whiteListStr, String blackListStr) {
        system.setupPermissionsForUDFs(whiteListStr, blackListStr);
    }
}
