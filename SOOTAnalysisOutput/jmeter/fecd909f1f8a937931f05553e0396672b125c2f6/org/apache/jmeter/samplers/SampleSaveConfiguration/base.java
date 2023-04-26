package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Properties;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;

public class SampleSaveConfiguration implements Cloneable, Serializable {

    static final long serialVersionUID = 1;

    public static final String XML = "xml";

    public static final String CSV = "csv";

    public static final String DATABASE = "db";

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    public static final String MILLISECONDS = "ms";

    public static final String NONE = "none";

    public static final String FIRST = "first";

    public static final String ALL = "all";

    public static final String ASSERTION_RESULTS_FAILURE_MESSAGE_PROP = "jmeter.save.saveservice.assertion_results_failure_message";

    public static final String ASSERTION_RESULTS_PROP = "jmeter.save.saveservice.assertion_results";

    public static final String DEFAULT_DELIMITER_PROP = "jmeter.save.saveservice.default_delimiter";

    public static final String OUTPUT_FORMAT_PROP = "jmeter.save.saveservice.output_format";

    public static final String PRINT_FIELD_NAMES_PROP = "jmeter.save.saveservice.print_field_names";

    public static final String SAVE_DATA_TYPE_PROP = "jmeter.save.saveservice.data_type";

    public static final String SAVE_LABEL_PROP = "jmeter.save.saveservice.label";

    public static final String SAVE_RESPONSE_CODE_PROP = "jmeter.save.saveservice.response_code";

    public static final String SAVE_RESPONSE_DATA_PROP = "jmeter.save.saveservice.response_data";

    public static final String SAVE_RESPONSE_DATA_ON_ERROR_PROP = "jmeter.save.saveservice.response_data.on_error";

    public static final String SAVE_RESPONSE_MESSAGE_PROP = "jmeter.save.saveservice.response_message";

    public static final String SAVE_SUCCESSFUL_PROP = "jmeter.save.saveservice.successful";

    public static final String SAVE_THREAD_NAME_PROP = "jmeter.save.saveservice.thread_name";

    public static final String SAVE_TIME_PROP = "jmeter.save.saveservice.time";

    public static final String TIME_STAMP_FORMAT_PROP = "jmeter.save.saveservice.timestamp_format";

    public final static String PRESERVE = "preserve";

    public final static String XML_SPACE = "xml:space";

    public static final String ASSERTION_RESULT_TAG_NAME = "assertionResult";

    public static final String BINARY = "binary";

    public static final String DATA_TYPE = "dataType";

    public static final String ERROR = "error";

    public static final String FAILURE = "failure";

    public static final String FAILURE_MESSAGE = "failureMessage";

    public static final String LABEL = "label";

    public static final String RESPONSE_CODE = "responseCode";

    public static final String RESPONSE_MESSAGE = "responseMessage";

    public static final String SAMPLE_RESULT_TAG_NAME = "sampleResult";

    public static final String SUCCESSFUL = "success";

    public static final String THREAD_NAME = "threadName";

    public static final String TIME = "time";

    public static final String TIME_STAMP = "timeStamp";

    private boolean time = _time, latency = _latency, timestamp = _timestamp, success = _success, label = _label, code = _code, message = _message, threadName = _threadName, dataType = _dataType, encoding = _encoding, assertions = _assertions, subresults = _subresults, responseData = _responseData, samplerData = _samplerData, xml = _xml, fieldNames = _fieldNames, responseHeaders = _responseHeaders, requestHeaders = _requestHeaders, responseDataOnError = _responseDataOnError;

    private boolean saveAssertionResultsFailureMessage = _saveAssertionResultsFailureMessage;

    private int assertionsResultsToSave = _assertionsResultsToSave;

    private String delimiter = _delimiter;

    private boolean printMilliseconds = _printMilliseconds;

    private SimpleDateFormat formatter = _formatter;

    private static final boolean _time, _timestamp, _success, _label, _code, _message, _threadName, _xml, _responseData, _dataType, _encoding, _assertions, _latency, _subresults, _samplerData, _fieldNames, _responseHeaders, _requestHeaders;

    private static final boolean _responseDataOnError;

    private static final boolean _saveAssertionResultsFailureMessage;

    private static final String _timeStampFormat;

    private static int _assertionsResultsToSave;

    public static final int SAVE_NO_ASSERTIONS = 0;

    public static final int SAVE_FIRST_ASSERTION = SAVE_NO_ASSERTIONS + 1;

    public static final int SAVE_ALL_ASSERTIONS = SAVE_FIRST_ASSERTION + 1;

    private static final boolean _printMilliseconds;

    private static final SimpleDateFormat _formatter;

    private static final String _delimiter;

    private static final String DEFAULT_DELIMITER = ",";

    static {
        _subresults = true;
        _assertions = true;
        _latency = true;
        _samplerData = false;
        _responseHeaders = false;
        _requestHeaders = false;
        _encoding = false;
        Properties props = JMeterUtils.getJMeterProperties();
        _delimiter = props.getProperty(DEFAULT_DELIMITER_PROP, DEFAULT_DELIMITER);
        _fieldNames = TRUE.equalsIgnoreCase(props.getProperty(PRINT_FIELD_NAMES_PROP, FALSE));
        _dataType = TRUE.equalsIgnoreCase(props.getProperty(SAVE_DATA_TYPE_PROP, TRUE));
        _label = TRUE.equalsIgnoreCase(props.getProperty(SAVE_LABEL_PROP, TRUE));
        _code = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_CODE_PROP, TRUE));
        _responseData = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_PROP, FALSE));
        _responseDataOnError = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_DATA_ON_ERROR_PROP, FALSE));
        _message = TRUE.equalsIgnoreCase(props.getProperty(SAVE_RESPONSE_MESSAGE_PROP, TRUE));
        _success = TRUE.equalsIgnoreCase(props.getProperty(SAVE_SUCCESSFUL_PROP, TRUE));
        _threadName = TRUE.equalsIgnoreCase(props.getProperty(SAVE_THREAD_NAME_PROP, TRUE));
        _time = TRUE.equalsIgnoreCase(props.getProperty(SAVE_TIME_PROP, TRUE));
        _timeStampFormat = props.getProperty(TIME_STAMP_FORMAT_PROP, MILLISECONDS);
        _printMilliseconds = MILLISECONDS.equalsIgnoreCase(_timeStampFormat);
        if (!_printMilliseconds && !NONE.equalsIgnoreCase(_timeStampFormat) && (_timeStampFormat != null)) {
            _formatter = new SimpleDateFormat(_timeStampFormat);
        } else {
            _formatter = null;
        }
        _timestamp = !_timeStampFormat.equalsIgnoreCase(NONE);
        _saveAssertionResultsFailureMessage = TRUE.equalsIgnoreCase(props.getProperty(ASSERTION_RESULTS_FAILURE_MESSAGE_PROP, FALSE));
        String whichAssertionResults = props.getProperty(ASSERTION_RESULTS_PROP, NONE);
        if (NONE.equals(whichAssertionResults)) {
            _assertionsResultsToSave = SAVE_NO_ASSERTIONS;
        } else if (FIRST.equals(whichAssertionResults)) {
            _assertionsResultsToSave = SAVE_FIRST_ASSERTION;
        } else if (ALL.equals(whichAssertionResults)) {
            _assertionsResultsToSave = SAVE_ALL_ASSERTIONS;
        }
        String howToSave = props.getProperty(OUTPUT_FORMAT_PROP, XML);
        if (XML.equals(howToSave)) {
            _xml = true;
        } else {
            _xml = false;
        }
    }

    private static final SampleSaveConfiguration _static = new SampleSaveConfiguration();

    public static SampleSaveConfiguration staticConfig() {
        return _static;
    }

    public SampleSaveConfiguration() {
    }

    public Object clone() {
        SampleSaveConfiguration s = new SampleSaveConfiguration();
        s.time = time;
        s.latency = latency;
        s.timestamp = timestamp;
        s.success = success;
        s.label = label;
        s.code = code;
        s.message = message;
        s.threadName = threadName;
        s.dataType = dataType;
        s.encoding = encoding;
        s.assertions = assertions;
        s.subresults = subresults;
        s.responseData = responseData;
        s.samplerData = samplerData;
        s.xml = xml;
        s.fieldNames = fieldNames;
        s.responseHeaders = responseHeaders;
        s.requestHeaders = requestHeaders;
        s.formatter = formatter;
        s.assertionsResultsToSave = assertionsResultsToSave;
        s.saveAssertionResultsFailureMessage = saveAssertionResultsFailureMessage;
        s.delimiter = delimiter;
        s.printMilliseconds = printMilliseconds;
        s.responseDataOnError = responseDataOnError;
        return s;
    }

    public boolean saveResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(boolean r) {
        responseHeaders = r;
    }

    public boolean saveRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(boolean r) {
        requestHeaders = r;
    }

    public boolean saveAssertions() {
        return assertions;
    }

    public void setAssertions(boolean assertions) {
        this.assertions = assertions;
    }

    public boolean saveCode() {
        return code;
    }

    public void setCode(boolean code) {
        this.code = code;
    }

    public boolean saveDataType() {
        return dataType;
    }

    public void setDataType(boolean dataType) {
        this.dataType = dataType;
    }

    public boolean saveEncoding() {
        return encoding;
    }

    public void setEncoding(boolean encoding) {
        this.encoding = encoding;
    }

    public boolean saveLabel() {
        return label;
    }

    public void setLabel(boolean label) {
        this.label = label;
    }

    public boolean saveLatency() {
        return latency;
    }

    public void setLatency(boolean latency) {
        this.latency = latency;
    }

    public boolean saveMessage() {
        return message;
    }

    public void setMessage(boolean message) {
        this.message = message;
    }

    public boolean saveResponseData(SampleResult res) {
        return responseData || TestPlan.getFunctionalMode() || (responseDataOnError && !res.isSuccessful());
    }

    public void setResponseData(boolean responseData) {
        this.responseData = responseData;
    }

    public boolean saveSamplerData(SampleResult res) {
        return samplerData || TestPlan.getFunctionalMode() || (responseDataOnError && !res.isSuccessful());
    }

    public void setSamplerData(boolean samplerData) {
        this.samplerData = samplerData;
    }

    public boolean saveSubresults() {
        return subresults;
    }

    public void setSubresults(boolean subresults) {
        this.subresults = subresults;
    }

    public boolean saveSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean saveThreadName() {
        return threadName;
    }

    public void setThreadName(boolean threadName) {
        this.threadName = threadName;
    }

    public boolean saveTime() {
        return time;
    }

    public void setTime(boolean time) {
        this.time = time;
    }

    public boolean saveTimestamp() {
        return timestamp;
    }

    public void setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
    }

    public boolean saveAsXml() {
        return xml;
    }

    public void setAsXml(boolean xml) {
        this.xml = xml;
    }

    public boolean saveFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(boolean printFieldNames) {
        this.fieldNames = printFieldNames;
    }

    public boolean printMilliseconds() {
        return printMilliseconds;
    }

    public SimpleDateFormat formatter() {
        return formatter;
    }

    public boolean saveAssertionResultsFailureMessage() {
        return saveAssertionResultsFailureMessage;
    }

    public void setAssertionResultsFailureMessage(boolean b) {
        saveAssertionResultsFailureMessage = b;
    }

    public int assertionsResultsToSave() {
        return assertionsResultsToSave;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
