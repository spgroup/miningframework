package javax.management.modelmbean;

import static com.sun.jmx.defaults.JmxProperties.MODELMBEAN_LOGGER;
import static com.sun.jmx.mbeanserver.Util.cast;
import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Util;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import sun.reflect.misc.ReflectUtil;

@SuppressWarnings("serial")
public class DescriptorSupport implements javax.management.Descriptor {

    private static final long oldSerialVersionUID = 8071560848919417985L;

    private static final long newSerialVersionUID = -6292969195866300415L;

    private static final ObjectStreamField[] oldSerialPersistentFields = { new ObjectStreamField("descriptor", HashMap.class), new ObjectStreamField("currClass", String.class) };

    private static final ObjectStreamField[] newSerialPersistentFields = { new ObjectStreamField("descriptor", HashMap.class) };

    private static final long serialVersionUID;

    private static final ObjectStreamField[] serialPersistentFields;

    private static final String serialForm;

    static {
        String form = null;
        boolean compat = false;
        try {
            GetPropertyAction act = new GetPropertyAction("jmx.serial.form");
            form = AccessController.doPrivileged(act);
            compat = "1.0".equals(form);
        } catch (Exception e) {
        }
        serialForm = form;
        if (compat) {
            serialPersistentFields = oldSerialPersistentFields;
            serialVersionUID = oldSerialVersionUID;
        } else {
            serialPersistentFields = newSerialPersistentFields;
            serialVersionUID = newSerialVersionUID;
        }
    }

    private transient SortedMap<String, Object> descriptorMap;

    private static final String currClass = "DescriptorSupport";

    public DescriptorSupport() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "DescriptorSupport()", "Constructor");
        }
        init(null);
    }

    public DescriptorSupport(int initNumFields) throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(initNumFields = " + initNumFields + ")", "Constructor");
        }
        if (initNumFields <= 0) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(initNumFields)", "Illegal arguments: initNumFields <= 0");
            }
            final String msg = "Descriptor field limit invalid: " + initNumFields;
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        init(null);
    }

    public DescriptorSupport(DescriptorSupport inDescr) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(Descriptor)", "Constructor");
        }
        if (inDescr == null)
            init(null);
        else
            init(inDescr.descriptorMap);
    }

    public DescriptorSupport(String inStr) throws MBeanException, RuntimeOperationsException, XMLParseException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String = '" + inStr + "')", "Constructor");
        }
        if (inStr == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String = null)", "Illegal arguments");
            }
            final String msg = "String in parameter is null";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        final String lowerInStr = inStr.toLowerCase();
        if (!lowerInStr.startsWith("<descriptor>") || !lowerInStr.endsWith("</descriptor>")) {
            throw new XMLParseException("No <descriptor>, </descriptor> pair");
        }
        init(null);
        StringTokenizer st = new StringTokenizer(inStr, "<> \t\n\r\f");
        boolean inFld = false;
        boolean inDesc = false;
        String fieldName = null;
        String fieldValue = null;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equalsIgnoreCase("FIELD")) {
                inFld = true;
            } else if (tok.equalsIgnoreCase("/FIELD")) {
                if ((fieldName != null) && (fieldValue != null)) {
                    fieldName = fieldName.substring(fieldName.indexOf('"') + 1, fieldName.lastIndexOf('"'));
                    final Object fieldValueObject = parseQuotedFieldValue(fieldValue);
                    setField(fieldName, fieldValueObject);
                }
                fieldName = null;
                fieldValue = null;
                inFld = false;
            } else if (tok.equalsIgnoreCase("DESCRIPTOR")) {
                inDesc = true;
            } else if (tok.equalsIgnoreCase("/DESCRIPTOR")) {
                inDesc = false;
                fieldName = null;
                fieldValue = null;
                inFld = false;
            } else if (inFld && inDesc) {
                int eq_separator = tok.indexOf("=");
                if (eq_separator > 0) {
                    String kwPart = tok.substring(0, eq_separator);
                    String valPart = tok.substring(eq_separator + 1);
                    if (kwPart.equalsIgnoreCase("NAME"))
                        fieldName = valPart;
                    else if (kwPart.equalsIgnoreCase("VALUE"))
                        fieldValue = valPart;
                    else {
                        final String msg = "Expected `name' or `value', got `" + tok + "'";
                        throw new XMLParseException(msg);
                    }
                } else {
                    final String msg = "Expected `keyword=value', got `" + tok + "'";
                    throw new XMLParseException(msg);
                }
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(XMLString)", "Exit");
        }
    }

    public DescriptorSupport(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(fieldNames,fieldObjects)", "Constructor");
        }
        if ((fieldNames == null) || (fieldValues == null) || (fieldNames.length != fieldValues.length)) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(fieldNames,fieldObjects)", "Illegal arguments");
            }
            final String msg = "Null or invalid fieldNames or fieldValues";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        init(null);
        for (int i = 0; i < fieldNames.length; i++) {
            setField(fieldNames[i], fieldValues[i]);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(fieldNames,fieldObjects)", "Exit");
        }
    }

    public DescriptorSupport(String... fields) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String... fields)", "Constructor");
        }
        init(null);
        if ((fields == null) || (fields.length == 0))
            return;
        init(null);
        for (int i = 0; i < fields.length; i++) {
            if ((fields[i] == null) || (fields[i].equals(""))) {
                continue;
            }
            int eq_separator = fields[i].indexOf("=");
            if (eq_separator < 0) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                    MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String... fields)", "Illegal arguments: field does not have " + "'=' as a name and value separator");
                }
                final String msg = "Field in invalid format: no equals sign";
                final RuntimeException iae = new IllegalArgumentException(msg);
                throw new RuntimeOperationsException(iae, msg);
            }
            String fieldName = fields[i].substring(0, eq_separator);
            String fieldValue = null;
            if (eq_separator < fields[i].length()) {
                fieldValue = fields[i].substring(eq_separator + 1);
            }
            if (fieldName.equals("")) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                    MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String... fields)", "Illegal arguments: fieldName is empty");
                }
                final String msg = "Field in invalid format: no fieldName";
                final RuntimeException iae = new IllegalArgumentException(msg);
                throw new RuntimeOperationsException(iae, msg);
            }
            setField(fieldName, fieldValue);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "Descriptor(String... fields)", "Exit");
        }
    }

    private void init(Map<String, ?> initMap) {
        descriptorMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        if (initMap != null)
            descriptorMap.putAll(initMap);
    }

    public synchronized Object getFieldValue(String fieldName) throws RuntimeOperationsException {
        if ((fieldName == null) || (fieldName.equals(""))) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldValue(String fieldName)", "Illegal arguments: null field name");
            }
            final String msg = "Fieldname requested is null";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        Object retValue = descriptorMap.get(fieldName);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldValue(String fieldName = " + fieldName + ")", "Returns '" + retValue + "'");
        }
        return (retValue);
    }

    public synchronized void setField(String fieldName, Object fieldValue) throws RuntimeOperationsException {
        if ((fieldName == null) || (fieldName.equals(""))) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setField(fieldName,fieldValue)", "Illegal arguments: null or empty field name");
            }
            final String msg = "Field name to be set is null or empty";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        if (!validateField(fieldName, fieldValue)) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setField(fieldName,fieldValue)", "Illegal arguments");
            }
            final String msg = "Field value invalid: " + fieldName + "=" + fieldValue;
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setField(fieldName,fieldValue)", "Entry: setting '" + fieldName + "' to '" + fieldValue + "'");
        }
        descriptorMap.put(fieldName, fieldValue);
    }

    public synchronized String[] getFields() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFields()", "Entry");
        }
        int numberOfEntries = descriptorMap.size();
        String[] responseFields = new String[numberOfEntries];
        Set<Map.Entry<String, Object>> returnedSet = descriptorMap.entrySet();
        int i = 0;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFields()", "Returning " + numberOfEntries + " fields");
        }
        for (Iterator<Map.Entry<String, Object>> iter = returnedSet.iterator(); iter.hasNext(); i++) {
            Map.Entry<String, Object> currElement = iter.next();
            if (currElement == null) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                    MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFields()", "Element is null");
                }
            } else {
                Object currValue = currElement.getValue();
                if (currValue == null) {
                    responseFields[i] = currElement.getKey() + "=";
                } else {
                    if (currValue instanceof java.lang.String) {
                        responseFields[i] = currElement.getKey() + "=" + currValue.toString();
                    } else {
                        responseFields[i] = currElement.getKey() + "=(" + currValue.toString() + ")";
                    }
                }
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFields()", "Exit");
        }
        return responseFields;
    }

    public synchronized String[] getFieldNames() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldNames()", "Entry");
        }
        int numberOfEntries = descriptorMap.size();
        String[] responseFields = new String[numberOfEntries];
        Set<Map.Entry<String, Object>> returnedSet = descriptorMap.entrySet();
        int i = 0;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldNames()", "Returning " + numberOfEntries + " fields");
        }
        for (Iterator<Map.Entry<String, Object>> iter = returnedSet.iterator(); iter.hasNext(); i++) {
            Map.Entry<String, Object> currElement = iter.next();
            if ((currElement == null) || (currElement.getKey() == null)) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                    MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldNames()", "Field is null");
                }
            } else {
                responseFields[i] = currElement.getKey().toString();
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldNames()", "Exit");
        }
        return responseFields;
    }

    public synchronized Object[] getFieldValues(String... fieldNames) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldValues(String... fieldNames)", "Entry");
        }
        final int numberOfEntries = (fieldNames == null) ? descriptorMap.size() : fieldNames.length;
        final Object[] responseFields = new Object[numberOfEntries];
        int i = 0;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldValues(String... fieldNames)", "Returning " + numberOfEntries + " fields");
        }
        if (fieldNames == null) {
            for (Object value : descriptorMap.values()) responseFields[i++] = value;
        } else {
            for (i = 0; i < fieldNames.length; i++) {
                if ((fieldNames[i] == null) || (fieldNames[i].equals(""))) {
                    responseFields[i] = null;
                } else {
                    responseFields[i] = getFieldValue(fieldNames[i]);
                }
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "getFieldValues(String... fieldNames)", "Exit");
        }
        return responseFields;
    }

    public synchronized void setFields(String[] fieldNames, Object[] fieldValues) throws RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setFields(fieldNames,fieldValues)", "Entry");
        }
        if ((fieldNames == null) || (fieldValues == null) || (fieldNames.length != fieldValues.length)) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setFields(fieldNames,fieldValues)", "Illegal arguments");
            }
            final String msg = "fieldNames and fieldValues are null or invalid";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, msg);
        }
        for (int i = 0; i < fieldNames.length; i++) {
            if ((fieldNames[i] == null) || (fieldNames[i].equals(""))) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                    MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setFields(fieldNames,fieldValues)", "Null field name encountered at element " + i);
                }
                final String msg = "fieldNames is null or invalid";
                final RuntimeException iae = new IllegalArgumentException(msg);
                throw new RuntimeOperationsException(iae, msg);
            }
            setField(fieldNames[i], fieldValues[i]);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "setFields(fieldNames,fieldValues)", "Exit");
        }
    }

    @Override
    public synchronized Object clone() throws RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "clone()", "Entry");
        }
        return (new DescriptorSupport(this));
    }

    public synchronized void removeField(String fieldName) {
        if ((fieldName == null) || (fieldName.equals(""))) {
            return;
        }
        descriptorMap.remove(fieldName);
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Descriptor))
            return false;
        if (o instanceof ImmutableDescriptor)
            return o.equals(this);
        return new ImmutableDescriptor(descriptorMap).equals(o);
    }

    @Override
    public synchronized int hashCode() {
        final int size = descriptorMap.size();
        return Util.hashCode(descriptorMap.keySet().toArray(new String[size]), descriptorMap.values().toArray(new Object[size]));
    }

    public synchronized boolean isValid() throws RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "isValid()", "Entry");
        }
        Set<Map.Entry<String, Object>> returnedSet = descriptorMap.entrySet();
        if (returnedSet == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "isValid()", "Returns false (null set)");
            }
            return false;
        }
        String thisName = (String) (this.getFieldValue("name"));
        String thisDescType = (String) (getFieldValue("descriptorType"));
        if ((thisName == null) || (thisDescType == null) || (thisName.equals("")) || (thisDescType.equals(""))) {
            return false;
        }
        for (Map.Entry<String, Object> currElement : returnedSet) {
            if (currElement != null) {
                if (currElement.getValue() != null) {
                    if (validateField((currElement.getKey()).toString(), (currElement.getValue()).toString())) {
                        continue;
                    } else {
                        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "isValid()", "Field " + currElement.getKey() + "=" + currElement.getValue() + " is not valid");
                        }
                        return false;
                    }
                }
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "isValid()", "Returns true");
        }
        return true;
    }

    private boolean validateField(String fldName, Object fldValue) {
        if ((fldName == null) || (fldName.equals("")))
            return false;
        String SfldValue = "";
        boolean isAString = false;
        if ((fldValue != null) && (fldValue instanceof java.lang.String)) {
            SfldValue = (String) fldValue;
            isAString = true;
        }
        boolean nameOrDescriptorType = (fldName.equalsIgnoreCase("Name") || fldName.equalsIgnoreCase("DescriptorType"));
        if (nameOrDescriptorType || fldName.equalsIgnoreCase("SetMethod") || fldName.equalsIgnoreCase("GetMethod") || fldName.equalsIgnoreCase("Role") || fldName.equalsIgnoreCase("Class")) {
            if (fldValue == null || !isAString)
                return false;
            if (nameOrDescriptorType && SfldValue.equals(""))
                return false;
            return true;
        } else if (fldName.equalsIgnoreCase("visibility")) {
            long v;
            if ((fldValue != null) && (isAString)) {
                v = toNumeric(SfldValue);
            } else if (fldValue instanceof java.lang.Integer) {
                v = ((Integer) fldValue).intValue();
            } else
                return false;
            if (v >= 1 && v <= 4)
                return true;
            else
                return false;
        } else if (fldName.equalsIgnoreCase("severity")) {
            long v;
            if ((fldValue != null) && (isAString)) {
                v = toNumeric(SfldValue);
            } else if (fldValue instanceof java.lang.Integer) {
                v = ((Integer) fldValue).intValue();
            } else
                return false;
            return (v >= 0 && v <= 6);
        } else if (fldName.equalsIgnoreCase("PersistPolicy")) {
            return (((fldValue != null) && (isAString)) && (SfldValue.equalsIgnoreCase("OnUpdate") || SfldValue.equalsIgnoreCase("OnTimer") || SfldValue.equalsIgnoreCase("NoMoreOftenThan") || SfldValue.equalsIgnoreCase("Always") || SfldValue.equalsIgnoreCase("Never") || SfldValue.equalsIgnoreCase("OnUnregister")));
        } else if (fldName.equalsIgnoreCase("PersistPeriod") || fldName.equalsIgnoreCase("CurrencyTimeLimit") || fldName.equalsIgnoreCase("LastUpdatedTimeStamp") || fldName.equalsIgnoreCase("LastReturnedTimeStamp")) {
            long v;
            if ((fldValue != null) && (isAString)) {
                v = toNumeric(SfldValue);
            } else if (fldValue instanceof java.lang.Number) {
                v = ((Number) fldValue).longValue();
            } else
                return false;
            return (v >= -1);
        } else if (fldName.equalsIgnoreCase("log")) {
            return ((fldValue instanceof java.lang.Boolean) || (isAString && (SfldValue.equalsIgnoreCase("T") || SfldValue.equalsIgnoreCase("true") || SfldValue.equalsIgnoreCase("F") || SfldValue.equalsIgnoreCase("false"))));
        }
        return true;
    }

    public synchronized String toXMLString() {
        final StringBuilder buf = new StringBuilder("<Descriptor>");
        Set<Map.Entry<String, Object>> returnedSet = descriptorMap.entrySet();
        for (Map.Entry<String, Object> currElement : returnedSet) {
            final String name = currElement.getKey();
            Object value = currElement.getValue();
            String valueString = null;
            if (value instanceof String) {
                final String svalue = (String) value;
                if (!svalue.startsWith("(") || !svalue.endsWith(")"))
                    valueString = quote(svalue);
            }
            if (valueString == null)
                valueString = makeFieldValue(value);
            buf.append("<field name=\"").append(name).append("\" value=\"").append(valueString).append("\"></field>");
        }
        buf.append("</Descriptor>");
        return buf.toString();
    }

    private static final String[] entities = { " &#32;", "\"&quot;", "<&lt;", ">&gt;", "&&amp;", "\r&#13;", "\t&#9;", "\n&#10;", "\f&#12;" };

    private static final Map<String, Character> entityToCharMap = new HashMap<String, Character>();

    private static final String[] charToEntityMap;

    static {
        char maxChar = 0;
        for (int i = 0; i < entities.length; i++) {
            final char c = entities[i].charAt(0);
            if (c > maxChar)
                maxChar = c;
        }
        charToEntityMap = new String[maxChar + 1];
        for (int i = 0; i < entities.length; i++) {
            final char c = entities[i].charAt(0);
            final String entity = entities[i].substring(1);
            charToEntityMap[c] = entity;
            entityToCharMap.put(entity, c);
        }
    }

    private static boolean isMagic(char c) {
        return (c < charToEntityMap.length && charToEntityMap[c] != null);
    }

    private static String quote(String s) {
        boolean found = false;
        for (int i = 0; i < s.length(); i++) {
            if (isMagic(s.charAt(i))) {
                found = true;
                break;
            }
        }
        if (!found)
            return s;
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isMagic(c))
                buf.append(charToEntityMap[c]);
            else
                buf.append(c);
        }
        return buf.toString();
    }

    private static String unquote(String s) throws XMLParseException {
        if (!s.startsWith("\"") || !s.endsWith("\""))
            throw new XMLParseException("Value must be quoted: <" + s + ">");
        final StringBuilder buf = new StringBuilder();
        final int len = s.length() - 1;
        for (int i = 1; i < len; i++) {
            final char c = s.charAt(i);
            final int semi;
            final Character quoted;
            if (c == '&' && (semi = s.indexOf(';', i + 1)) >= 0 && ((quoted = entityToCharMap.get(s.substring(i, semi + 1))) != null)) {
                buf.append(quoted);
                i = semi;
            } else
                buf.append(c);
        }
        return buf.toString();
    }

    private static String makeFieldValue(Object value) {
        if (value == null)
            return "(null)";
        Class<?> valueClass = value.getClass();
        try {
            valueClass.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            final String msg = "Class " + valueClass + " does not have a public " + "constructor with a single string arg";
            final RuntimeException iae = new IllegalArgumentException(msg);
            throw new RuntimeOperationsException(iae, "Cannot make XML descriptor");
        } catch (SecurityException e) {
        }
        final String quotedValueString = quote(value.toString());
        return "(" + valueClass.getName() + "/" + quotedValueString + ")";
    }

    private static Object parseQuotedFieldValue(String s) throws XMLParseException {
        s = unquote(s);
        if (s.equalsIgnoreCase("(null)"))
            return null;
        if (!s.startsWith("(") || !s.endsWith(")"))
            return s;
        final int slash = s.indexOf('/');
        if (slash < 0) {
            return s.substring(1, s.length() - 1);
        }
        final String className = s.substring(1, slash);
        final Constructor<?> constr;
        try {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader == null) {
                ReflectUtil.checkPackageAccess(className);
            }
            final Class<?> c = Class.forName(className, false, contextClassLoader);
            constr = c.getConstructor(new Class<?>[] { String.class });
        } catch (Exception e) {
            throw new XMLParseException(e, "Cannot parse value: <" + s + ">");
        }
        final String arg = s.substring(slash + 1, s.length() - 1);
        try {
            return constr.newInstance(new Object[] { arg });
        } catch (Exception e) {
            final String msg = "Cannot construct instance of " + className + " with arg: <" + s + ">";
            throw new XMLParseException(e, msg);
        }
    }

    @Override
    public synchronized String toString() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "toString()", "Entry");
        }
        String respStr = "";
        String[] fields = getFields();
        if ((fields == null) || (fields.length == 0)) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
                MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "toString()", "Empty Descriptor");
            }
            return respStr;
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "toString()", "Printing " + fields.length + " fields");
        }
        for (int i = 0; i < fields.length; i++) {
            if (i == (fields.length - 1)) {
                respStr = respStr.concat(fields[i]);
            } else {
                respStr = respStr.concat(fields[i] + ", ");
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINEST)) {
            MODELMBEAN_LOGGER.logp(Level.FINEST, DescriptorSupport.class.getName(), "toString()", "Exit returning " + respStr);
        }
        return respStr;
    }

    private long toNumeric(String inStr) {
        try {
            return java.lang.Long.parseLong(inStr);
        } catch (Exception e) {
            return -2;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        Map<String, Object> descriptor = cast(fields.get("descriptor", null));
        init(null);
        if (descriptor != null) {
            descriptorMap.putAll(descriptor);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        boolean compat = "1.0".equals(serialForm);
        if (compat)
            fields.put("currClass", currClass);
        SortedMap<String, Object> startMap = descriptorMap;
        if (startMap.containsKey("targetObject")) {
            startMap = new TreeMap<String, Object>(descriptorMap);
            startMap.remove("targetObject");
        }
        final HashMap<String, Object> descriptor;
        if (compat || "1.2.0".equals(serialForm) || "1.2.1".equals(serialForm)) {
            descriptor = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : startMap.entrySet()) descriptor.put(entry.getKey().toLowerCase(), entry.getValue());
        } else
            descriptor = new HashMap<String, Object>(startMap);
        fields.put("descriptor", descriptor);
        out.writeFields();
    }
}
