package sun.management;

import java.lang.management.MemoryUsage;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.io.InvalidObjectException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.management.*;
import javax.management.openmbean.*;
import static javax.management.openmbean.SimpleType.*;
import com.sun.management.VMOption;

@SuppressWarnings("unchecked")
public abstract class MappedMXBeanType {

    private static final WeakHashMap<Type, MappedMXBeanType> convertedTypes = new WeakHashMap<>();

    boolean isBasicType = false;

    OpenType<?> openType = inProgress;

    Class<?> mappedTypeClass;

    static synchronized MappedMXBeanType newMappedType(Type javaType) throws OpenDataException {
        MappedMXBeanType mt = null;
        if (javaType instanceof Class) {
            final Class<?> c = (Class<?>) javaType;
            if (c.isEnum()) {
                mt = new EnumMXBeanType(c);
            } else if (c.isArray()) {
                mt = new ArrayMXBeanType(c);
            } else {
                mt = new CompositeDataMXBeanType(c);
            }
        } else if (javaType instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) javaType;
            final Type rawType = pt.getRawType();
            if (rawType instanceof Class) {
                final Class<?> rc = (Class<?>) rawType;
                if (rc == List.class) {
                    mt = new ListMXBeanType(pt);
                } else if (rc == Map.class) {
                    mt = new MapMXBeanType(pt);
                }
            }
        } else if (javaType instanceof GenericArrayType) {
            final GenericArrayType t = (GenericArrayType) javaType;
            mt = new GenericArrayMXBeanType(t);
        }
        if (mt == null) {
            throw new OpenDataException(javaType + " is not a supported MXBean type.");
        }
        convertedTypes.put(javaType, mt);
        return mt;
    }

    static synchronized MappedMXBeanType newBasicType(Class<?> c, OpenType<?> ot) throws OpenDataException {
        MappedMXBeanType mt = new BasicMXBeanType(c, ot);
        convertedTypes.put(c, mt);
        return mt;
    }

    static synchronized MappedMXBeanType getMappedType(Type t) throws OpenDataException {
        MappedMXBeanType mt = convertedTypes.get(t);
        if (mt == null) {
            mt = newMappedType(t);
        }
        if (mt.getOpenType() instanceof InProgress) {
            throw new OpenDataException("Recursive data structure");
        }
        return mt;
    }

    public static synchronized OpenType<?> toOpenType(Type t) throws OpenDataException {
        MappedMXBeanType mt = getMappedType(t);
        return mt.getOpenType();
    }

    public static Object toJavaTypeData(Object openData, Type t) throws OpenDataException, InvalidObjectException {
        if (openData == null) {
            return null;
        }
        MappedMXBeanType mt = getMappedType(t);
        return mt.toJavaTypeData(openData);
    }

    public static Object toOpenTypeData(Object data, Type t) throws OpenDataException {
        if (data == null) {
            return null;
        }
        MappedMXBeanType mt = getMappedType(t);
        return mt.toOpenTypeData(data);
    }

    OpenType<?> getOpenType() {
        return openType;
    }

    boolean isBasicType() {
        return isBasicType;
    }

    String getTypeName() {
        return getMappedTypeClass().getName();
    }

    Class<?> getMappedTypeClass() {
        return mappedTypeClass;
    }

    abstract Type getJavaType();

    abstract String getName();

    abstract Object toOpenTypeData(Object javaTypeData) throws OpenDataException;

    abstract Object toJavaTypeData(Object openTypeData) throws OpenDataException, InvalidObjectException;

    static class BasicMXBeanType extends MappedMXBeanType {

        final Class<?> basicType;

        BasicMXBeanType(Class<?> c, OpenType<?> openType) {
            this.basicType = c;
            this.openType = openType;
            this.mappedTypeClass = c;
            this.isBasicType = true;
        }

        Type getJavaType() {
            return basicType;
        }

        String getName() {
            return basicType.getName();
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            return data;
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            return data;
        }
    }

    static class EnumMXBeanType extends MappedMXBeanType {

        final Class enumClass;

        EnumMXBeanType(Class<?> c) {
            this.enumClass = c;
            this.openType = STRING;
            this.mappedTypeClass = String.class;
        }

        Type getJavaType() {
            return enumClass;
        }

        String getName() {
            return enumClass.getName();
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            return ((Enum) data).name();
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            try {
                return Enum.valueOf(enumClass, (String) data);
            } catch (IllegalArgumentException e) {
                final InvalidObjectException ioe = new InvalidObjectException("Enum constant named " + (String) data + " is missing");
                ioe.initCause(e);
                throw ioe;
            }
        }
    }

    static class ArrayMXBeanType extends MappedMXBeanType {

        final Class<?> arrayClass;

        protected MappedMXBeanType componentType;

        protected MappedMXBeanType baseElementType;

        ArrayMXBeanType(Class<?> c) throws OpenDataException {
            this.arrayClass = c;
            this.componentType = getMappedType(c.getComponentType());
            StringBuilder className = new StringBuilder();
            Class<?> et = c;
            int dim;
            for (dim = 0; et.isArray(); dim++) {
                className.append('[');
                et = et.getComponentType();
            }
            baseElementType = getMappedType(et);
            if (et.isPrimitive()) {
                className = new StringBuilder(c.getName());
            } else {
                className.append("L" + baseElementType.getTypeName() + ";");
            }
            try {
                mappedTypeClass = Class.forName(className.toString());
            } catch (ClassNotFoundException e) {
                final OpenDataException ode = new OpenDataException("Cannot obtain array class");
                ode.initCause(e);
                throw ode;
            }
            openType = new ArrayType<>(dim, baseElementType.getOpenType());
        }

        protected ArrayMXBeanType() {
            arrayClass = null;
        }

        Type getJavaType() {
            return arrayClass;
        }

        String getName() {
            return arrayClass.getName();
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            if (baseElementType.isBasicType()) {
                return data;
            }
            final Object[] array = (Object[]) data;
            final Object[] openArray = (Object[]) Array.newInstance(componentType.getMappedTypeClass(), array.length);
            int i = 0;
            for (Object o : array) {
                if (o == null) {
                    openArray[i] = null;
                } else {
                    openArray[i] = componentType.toOpenTypeData(o);
                }
                i++;
            }
            return openArray;
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            if (baseElementType.isBasicType()) {
                return data;
            }
            final Object[] openArray = (Object[]) data;
            final Object[] array = (Object[]) Array.newInstance((Class) componentType.getJavaType(), openArray.length);
            int i = 0;
            for (Object o : openArray) {
                if (o == null) {
                    array[i] = null;
                } else {
                    array[i] = componentType.toJavaTypeData(o);
                }
                i++;
            }
            return array;
        }
    }

    static class GenericArrayMXBeanType extends ArrayMXBeanType {

        final GenericArrayType gtype;

        GenericArrayMXBeanType(GenericArrayType gat) throws OpenDataException {
            this.gtype = gat;
            this.componentType = getMappedType(gat.getGenericComponentType());
            StringBuilder className = new StringBuilder();
            Type elementType = gat;
            int dim;
            for (dim = 0; elementType instanceof GenericArrayType; dim++) {
                className.append('[');
                GenericArrayType et = (GenericArrayType) elementType;
                elementType = et.getGenericComponentType();
            }
            baseElementType = getMappedType(elementType);
            if (elementType instanceof Class && ((Class) elementType).isPrimitive()) {
                className = new StringBuilder(gat.toString());
            } else {
                className.append("L" + baseElementType.getTypeName() + ";");
            }
            try {
                mappedTypeClass = Class.forName(className.toString());
            } catch (ClassNotFoundException e) {
                final OpenDataException ode = new OpenDataException("Cannot obtain array class");
                ode.initCause(e);
                throw ode;
            }
            openType = new ArrayType<>(dim, baseElementType.getOpenType());
        }

        Type getJavaType() {
            return gtype;
        }

        String getName() {
            return gtype.toString();
        }
    }

    static class ListMXBeanType extends MappedMXBeanType {

        final ParameterizedType javaType;

        final MappedMXBeanType paramType;

        final String typeName;

        ListMXBeanType(ParameterizedType pt) throws OpenDataException {
            this.javaType = pt;
            final Type[] argTypes = pt.getActualTypeArguments();
            assert (argTypes.length == 1);
            if (!(argTypes[0] instanceof Class)) {
                throw new OpenDataException("Element Type for " + pt + " not supported");
            }
            final Class<?> et = (Class<?>) argTypes[0];
            if (et.isArray()) {
                throw new OpenDataException("Element Type for " + pt + " not supported");
            }
            paramType = getMappedType(et);
            typeName = "List<" + paramType.getName() + ">";
            try {
                mappedTypeClass = Class.forName("[L" + paramType.getTypeName() + ";");
            } catch (ClassNotFoundException e) {
                final OpenDataException ode = new OpenDataException("Array class not found");
                ode.initCause(e);
                throw ode;
            }
            openType = new ArrayType<>(1, paramType.getOpenType());
        }

        Type getJavaType() {
            return javaType;
        }

        String getName() {
            return typeName;
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            final List<Object> list = (List<Object>) data;
            final Object[] openArray = (Object[]) Array.newInstance(paramType.getMappedTypeClass(), list.size());
            int i = 0;
            for (Object o : list) {
                openArray[i++] = paramType.toOpenTypeData(o);
            }
            return openArray;
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            final Object[] openArray = (Object[]) data;
            List<Object> result = new ArrayList<>(openArray.length);
            for (Object o : openArray) {
                result.add(paramType.toJavaTypeData(o));
            }
            return result;
        }
    }

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private static final String[] mapIndexNames = { KEY };

    private static final String[] mapItemNames = { KEY, VALUE };

    static class MapMXBeanType extends MappedMXBeanType {

        final ParameterizedType javaType;

        final MappedMXBeanType keyType;

        final MappedMXBeanType valueType;

        final String typeName;

        MapMXBeanType(ParameterizedType pt) throws OpenDataException {
            this.javaType = pt;
            final Type[] argTypes = pt.getActualTypeArguments();
            assert (argTypes.length == 2);
            this.keyType = getMappedType(argTypes[0]);
            this.valueType = getMappedType(argTypes[1]);
            typeName = "Map<" + keyType.getName() + "," + valueType.getName() + ">";
            final OpenType<?>[] mapItemTypes = new OpenType<?>[] { keyType.getOpenType(), valueType.getOpenType() };
            final CompositeType rowType = new CompositeType(typeName, typeName, mapItemNames, mapItemNames, mapItemTypes);
            openType = new TabularType(typeName, typeName, rowType, mapIndexNames);
            mappedTypeClass = javax.management.openmbean.TabularData.class;
        }

        Type getJavaType() {
            return javaType;
        }

        String getName() {
            return typeName;
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            final Map<Object, Object> map = (Map<Object, Object>) data;
            final TabularType tabularType = (TabularType) openType;
            final TabularData table = new TabularDataSupport(tabularType);
            final CompositeType rowType = tabularType.getRowType();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                final Object key = keyType.toOpenTypeData(entry.getKey());
                final Object value = valueType.toOpenTypeData(entry.getValue());
                final CompositeData row = new CompositeDataSupport(rowType, mapItemNames, new Object[] { key, value });
                table.put(row);
            }
            return table;
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            final TabularData td = (TabularData) data;
            Map<Object, Object> result = new HashMap<>();
            for (CompositeData row : (Collection<CompositeData>) td.values()) {
                Object key = keyType.toJavaTypeData(row.get(KEY));
                Object value = valueType.toJavaTypeData(row.get(VALUE));
                result.put(key, value);
            }
            return result;
        }
    }

    private static final Class<?> COMPOSITE_DATA_CLASS = javax.management.openmbean.CompositeData.class;

    static class CompositeDataMXBeanType extends MappedMXBeanType {

        final Class<?> javaClass;

        final boolean isCompositeData;

        Method fromMethod = null;

        CompositeDataMXBeanType(Class<?> c) throws OpenDataException {
            this.javaClass = c;
            this.mappedTypeClass = COMPOSITE_DATA_CLASS;
            try {
                fromMethod = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>() {

                    public Method run() throws NoSuchMethodException {
                        return javaClass.getMethod("from", COMPOSITE_DATA_CLASS);
                    }
                });
            } catch (PrivilegedActionException e) {
            }
            if (COMPOSITE_DATA_CLASS.isAssignableFrom(c)) {
                this.isCompositeData = true;
                this.openType = null;
            } else {
                this.isCompositeData = false;
                final Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

                    public Method[] run() {
                        return javaClass.getMethods();
                    }
                });
                final List<String> names = new ArrayList<>();
                final List<OpenType<?>> types = new ArrayList<>();
                for (int i = 0; i < methods.length; i++) {
                    final Method method = methods[i];
                    final String name = method.getName();
                    final Type type = method.getGenericReturnType();
                    final String rest;
                    if (name.startsWith("get")) {
                        rest = name.substring(3);
                    } else if (name.startsWith("is") && type instanceof Class && ((Class) type) == boolean.class) {
                        rest = name.substring(2);
                    } else {
                        continue;
                    }
                    if (rest.equals("") || method.getParameterTypes().length > 0 || type == void.class || rest.equals("Class")) {
                        continue;
                    }
                    names.add(decapitalize(rest));
                    types.add(toOpenType(type));
                }
                final String[] nameArray = names.toArray(new String[0]);
                openType = new CompositeType(c.getName(), c.getName(), nameArray, nameArray, types.toArray(new OpenType<?>[0]));
            }
        }

        Type getJavaType() {
            return javaClass;
        }

        String getName() {
            return javaClass.getName();
        }

        Object toOpenTypeData(Object data) throws OpenDataException {
            if (data instanceof MemoryUsage) {
                return MemoryUsageCompositeData.toCompositeData((MemoryUsage) data);
            }
            if (data instanceof ThreadInfo) {
                return ThreadInfoCompositeData.toCompositeData((ThreadInfo) data);
            }
            if (data instanceof LockInfo) {
                if (data instanceof java.lang.management.MonitorInfo) {
                    return MonitorInfoCompositeData.toCompositeData((MonitorInfo) data);
                }
                return LockDataConverter.toLockInfoCompositeData((LockInfo) data);
            }
            if (data instanceof MemoryNotificationInfo) {
                return MemoryNotifInfoCompositeData.toCompositeData((MemoryNotificationInfo) data);
            }
            if (data instanceof VMOption) {
                return VMOptionCompositeData.toCompositeData((VMOption) data);
            }
            if (isCompositeData) {
                CompositeData cd = (CompositeData) data;
                CompositeType ct = cd.getCompositeType();
                String[] itemNames = ct.keySet().toArray(new String[0]);
                Object[] itemValues = cd.getAll(itemNames);
                return new CompositeDataSupport(ct, itemNames, itemValues);
            }
            throw new OpenDataException(javaClass.getName() + " is not supported for platform MXBeans");
        }

        Object toJavaTypeData(Object data) throws OpenDataException, InvalidObjectException {
            if (fromMethod == null) {
                throw new AssertionError("Does not support data conversion");
            }
            try {
                return fromMethod.invoke(null, data);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                final OpenDataException ode = new OpenDataException("Failed to invoke " + fromMethod.getName() + " to convert CompositeData " + " to " + javaClass.getName());
                ode.initCause(e);
                throw ode;
            }
        }
    }

    private static class InProgress extends OpenType {

        private static final String description = "Marker to detect recursive type use -- internal use only!";

        InProgress() throws OpenDataException {
            super("java.lang.String", "java.lang.String", description);
        }

        public String toString() {
            return description;
        }

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object o) {
            return false;
        }

        public boolean isValue(Object o) {
            return false;
        }

        private static final long serialVersionUID = -3413063475064374490L;
    }

    private static final OpenType<?> inProgress;

    static {
        OpenType<?> t;
        try {
            t = new InProgress();
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
        inProgress = t;
    }

    private static final OpenType[] simpleTypes = { BIGDECIMAL, BIGINTEGER, BOOLEAN, BYTE, CHARACTER, DATE, DOUBLE, FLOAT, INTEGER, LONG, OBJECTNAME, SHORT, STRING, VOID };

    static {
        try {
            for (int i = 0; i < simpleTypes.length; i++) {
                final OpenType<?> t = simpleTypes[i];
                Class<?> c;
                try {
                    c = Class.forName(t.getClassName(), false, MappedMXBeanType.class.getClassLoader());
                    MappedMXBeanType.newBasicType(c, t);
                } catch (ClassNotFoundException e) {
                    throw new AssertionError(e);
                } catch (OpenDataException e) {
                    throw new AssertionError(e);
                }
                if (c.getName().startsWith("java.lang.")) {
                    try {
                        final Field typeField = c.getField("TYPE");
                        final Class<?> primitiveType = (Class<?>) typeField.get(null);
                        MappedMXBeanType.newBasicType(primitiveType, t);
                    } catch (NoSuchFieldException e) {
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }

    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
