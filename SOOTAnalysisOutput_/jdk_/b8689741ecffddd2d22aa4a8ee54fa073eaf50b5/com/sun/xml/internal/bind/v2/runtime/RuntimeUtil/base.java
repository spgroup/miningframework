package com.sun.xml.internal.bind.v2.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RuntimeUtil {

    public static final class ToStringAdapter extends XmlAdapter<String, Object> {

        public Object unmarshal(String s) {
            throw new UnsupportedOperationException();
        }

        public String marshal(Object o) {
            if (o == null)
                return null;
            return o.toString();
        }
    }

    public static final Map<Class, Class> boxToPrimitive;

    public static final Map<Class, Class> primitiveToBox;

    static {
        Map<Class, Class> b = new HashMap<Class, Class>();
        b.put(Byte.TYPE, Byte.class);
        b.put(Short.TYPE, Short.class);
        b.put(Integer.TYPE, Integer.class);
        b.put(Long.TYPE, Long.class);
        b.put(Character.TYPE, Character.class);
        b.put(Boolean.TYPE, Boolean.class);
        b.put(Float.TYPE, Float.class);
        b.put(Double.TYPE, Double.class);
        b.put(Void.TYPE, Void.class);
        primitiveToBox = Collections.unmodifiableMap(b);
        Map<Class, Class> p = new HashMap<Class, Class>();
        for (Map.Entry<Class, Class> e : b.entrySet()) p.put(e.getValue(), e.getKey());
        boxToPrimitive = Collections.unmodifiableMap(p);
    }

    private static String getTypeName(Object o) {
        return o.getClass().getName();
    }
}
