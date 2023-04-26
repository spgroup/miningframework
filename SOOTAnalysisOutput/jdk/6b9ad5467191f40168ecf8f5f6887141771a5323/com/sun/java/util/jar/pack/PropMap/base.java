package com.sun.java.util.jar.pack;

import java.util.*;
import java.util.jar.*;
import java.util.jar.Pack200;
import java.util.zip.*;
import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

class PropMap extends TreeMap {

    ArrayList _listeners = new ArrayList(1);

    void addListener(PropertyChangeListener listener) {
        _listeners.add(listener);
    }

    void removeListener(PropertyChangeListener listener) {
        _listeners.remove(listener);
    }

    void addListeners(ArrayList listeners) {
        _listeners.addAll(listeners);
    }

    void removeListeners(ArrayList listeners) {
        _listeners.removeAll(listeners);
    }

    public Object put(Object key, Object value) {
        Object oldValue = super.put(key, value);
        if (value != oldValue && _listeners.size() > 0) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, (String) key, oldValue, value);
            for (Iterator i = _listeners.iterator(); i.hasNext(); ) {
                PropertyChangeListener listener = (PropertyChangeListener) i.next();
                listener.propertyChange(event);
            }
        }
        return oldValue;
    }

    private static Map defaultProps;

    static {
        Properties props = new Properties();
        props.put(Utils.DEBUG_DISABLE_NATIVE, String.valueOf(Boolean.getBoolean(Utils.DEBUG_DISABLE_NATIVE)));
        props.put(Utils.DEBUG_VERBOSE, String.valueOf(Integer.getInteger(Utils.DEBUG_VERBOSE, 0)));
        props.put(Utils.PACK_DEFAULT_TIMEZONE, String.valueOf(Boolean.getBoolean(Utils.PACK_DEFAULT_TIMEZONE)));
        props.put(Pack200.Packer.SEGMENT_LIMIT, "" + (1 * 1000 * 1000));
        props.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.TRUE);
        props.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.KEEP);
        props.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.KEEP);
        props.put(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.PASS);
        props.put(Pack200.Packer.EFFORT, "5");
        try {
            String propFile = "intrinsic.properties";
            InputStream propStr = PackerImpl.class.getResourceAsStream(propFile);
            props.load(new BufferedInputStream(propStr));
            propStr.close();
            for (Iterator i = props.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry e = (Map.Entry) i.next();
                String key = (String) e.getKey();
                String val = (String) e.getValue();
                if (key.startsWith("attribute.")) {
                    e.setValue(Attribute.normalizeLayoutString(val));
                }
            }
        } catch (IOException ee) {
            throw new RuntimeException(ee);
        }
        defaultProps = (new HashMap(props));
    }

    PropMap() {
        putAll(defaultProps);
    }

    SortedMap prefixMap(String prefix) {
        int len = prefix.length();
        if (len == 0)
            return this;
        char nextch = (char) (prefix.charAt(len - 1) + 1);
        String limit = prefix.substring(0, len - 1) + nextch;
        return subMap(prefix, limit);
    }

    String getProperty(String s) {
        return (String) get(s);
    }

    String getProperty(String s, String defaultVal) {
        String val = getProperty(s);
        if (val == null)
            return defaultVal;
        return val;
    }

    String setProperty(String s, String val) {
        return (String) put(s, val);
    }

    List getProperties(String prefix) {
        Collection values = prefixMap(prefix).values();
        ArrayList res = new ArrayList(values.size());
        res.addAll(values);
        while (res.remove(null)) ;
        return res;
    }

    private boolean toBoolean(String val) {
        return Boolean.valueOf(val).booleanValue();
    }

    boolean getBoolean(String s) {
        return toBoolean(getProperty(s));
    }

    boolean setBoolean(String s, boolean val) {
        return toBoolean(setProperty(s, String.valueOf(val)));
    }

    int toInteger(String val) {
        if (val == null)
            return 0;
        if (Pack200.Packer.TRUE.equals(val))
            return 1;
        if (Pack200.Packer.FALSE.equals(val))
            return 0;
        return Integer.parseInt(val);
    }

    int getInteger(String s) {
        return toInteger(getProperty(s));
    }

    int setInteger(String s, int val) {
        return toInteger(setProperty(s, String.valueOf(val)));
    }

    long toLong(String val) {
        try {
            return val == null ? 0 : Long.parseLong(val);
        } catch (java.lang.NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid value");
        }
    }

    long getLong(String s) {
        return toLong(getProperty(s));
    }

    long setLong(String s, long val) {
        return toLong(setProperty(s, String.valueOf(val)));
    }

    int getTime(String s) {
        String sval = getProperty(s, "0");
        if (Utils.NOW.equals(sval)) {
            return (int) ((System.currentTimeMillis() + 500) / 1000);
        }
        long lval = toLong(sval);
        final long recentSecondCount = 1000000000;
        if (lval < recentSecondCount * 10 && !"0".equals(sval))
            Utils.log.warning("Supplied modtime appears to be seconds rather than milliseconds: " + sval);
        return (int) ((lval + 500) / 1000);
    }

    void list(PrintStream out) {
        PrintWriter outw = new PrintWriter(out);
        list(outw);
        outw.flush();
    }

    void list(PrintWriter out) {
        out.println("#" + Utils.PACK_ZIP_ARCHIVE_MARKER_COMMENT + "[");
        Set defaults = defaultProps.entrySet();
        for (Iterator i = entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (defaults.contains(e))
                continue;
            out.println("  " + e.getKey() + " = " + e.getValue());
        }
        out.println("#]");
    }
}
