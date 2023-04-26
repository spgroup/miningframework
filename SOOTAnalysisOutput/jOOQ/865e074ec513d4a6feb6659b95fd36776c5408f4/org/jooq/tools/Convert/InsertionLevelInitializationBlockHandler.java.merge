package org.jooq.tools;

import static org.jooq.types.Unsigned.ubyte;
import static org.jooq.types.Unsigned.uint;
import static org.jooq.types.Unsigned.ulong;
import static org.jooq.types.Unsigned.ushort;
import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.jooq.Converter;
import org.jooq.EnumType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.exception.DataTypeException;
import org.jooq.tools.jdbc.MockArray;
import org.jooq.types.UByte;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.jooq.types.UShort;

public final class Convert {

    public static final Set<String> TRUE_VALUES;

    public static final Set<String> FALSE_VALUES;

    private static final Pattern UUID_PATTERN = Pattern.compile("(\\p{XDigit}{8})-?(\\p{XDigit}{4})-?(\\p{XDigit}{4})-?(\\p{XDigit}{4})-?(\\p{XDigit}{12})");

    static {
        Set<String> trueValues = new HashSet<String>();
        Set<String> falseValues = new HashSet<String>();
        trueValues.add("1");
        trueValues.add("1.0");
        trueValues.add("y");
        trueValues.add("Y");
        trueValues.add("yes");
        trueValues.add("YES");
        trueValues.add("true");
        trueValues.add("TRUE");
        trueValues.add("t");
        trueValues.add("T");
        trueValues.add("on");
        trueValues.add("ON");
        trueValues.add("enabled");
        trueValues.add("ENABLED");
        trueValues.add("t");
        falseValues.add("0");
        falseValues.add("0.0");
        falseValues.add("n");
        falseValues.add("N");
        falseValues.add("no");
        falseValues.add("NO");
        falseValues.add("false");
        falseValues.add("FALSE");
        falseValues.add("f");
        falseValues.add("F");
        falseValues.add("off");
        falseValues.add("OFF");
        falseValues.add("disabled");
        falseValues.add("DISABLED");
        falseValues.add("f");
        TRUE_VALUES = Collections.unmodifiableSet(trueValues);
        FALSE_VALUES = Collections.unmodifiableSet(falseValues);
    }

    public static final Object[] convert(Object[] values, Field<?>[] fields) {
        if (values != null) {
            Object[] result = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Field<?>) {
                    result[i] = values[i];
                } else {
                    result[i] = convert(values[i], fields[i].getType());
                }
            }
            return result;
        } else {
            return null;
        }
    }

    public static final Object[] convert(Object[] values, Class<?>[] types) {
        if (values != null) {
            Object[] result = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Field<?>) {
                    result[i] = values[i];
                } else {
                    result[i] = convert(values[i], types[i]);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static final <U> U[] convertArray(Object[] from, Converter<?, U> converter) throws DataTypeException {
        if (from == null) {
            return null;
        } else {
            Object[] arrayOfT = convertArray(from, converter.fromType());
            Object[] arrayOfU = (Object[]) Array.newInstance(converter.toType(), from.length);
            for (int i = 0; i < arrayOfT.length; i++) {
                arrayOfU[i] = convert(arrayOfT[i], converter);
            }
            return (U[]) arrayOfU;
        }
    }

    @SuppressWarnings("unchecked")
    public static final Object[] convertArray(Object[] from, Class<?> toClass) throws DataTypeException {
        if (from == null) {
            return null;
        } else if (!toClass.isArray()) {
            return convertArray(from, Array.newInstance(toClass, 0).getClass());
        } else if (toClass == from.getClass()) {
            return from;
        } else {
            final Class<?> toComponentType = toClass.getComponentType();
            if (from.length == 0) {
                return Arrays.copyOf(from, from.length, (Class<? extends Object[]>) toClass);
            } else if (from[0] != null && from[0].getClass() == toComponentType) {
                return Arrays.copyOf(from, from.length, (Class<? extends Object[]>) toClass);
            } else {
                final Object[] result = (Object[]) Array.newInstance(toComponentType, from.length);
                for (int i = 0; i < from.length; i++) {
                    result[i] = convert(from[i], toComponentType);
                }
                return result;
            }
        }
    }

    public static final <U> U convert(Object from, Converter<?, U> converter) throws DataTypeException {
        return convert0(from, converter);
    }

    private static final <T, U> U convert0(Object from, Converter<T, U> converter) throws DataTypeException {
        ConvertAll<T> all = new ConvertAll<T>(converter.fromType());
        return converter.from(all.from(from));
    }

    public static final <T> T convert(Object from, Class<? extends T> toClass) throws DataTypeException {
        return convert(from, new ConvertAll<T>(toClass));
    }

    public static final <T> List<T> convert(Collection<?> collection, Class<? extends T> type) throws DataTypeException {
        return convert(collection, new ConvertAll<T>(type));
    }

    public static final <U> List<U> convert(Collection<?> collection, Converter<?, U> converter) throws DataTypeException {
        return convert0(collection, converter);
    }

    private static final <T, U> List<U> convert0(Collection<?> collection, Converter<T, U> converter) throws DataTypeException {
        ConvertAll<T> all = new ConvertAll<T>(converter.fromType());
        List<U> result = new ArrayList<U>(collection.size());
        for (Object o : collection) {
            result.add(convert(all.from(o), converter));
        }
        return result;
    }

    private Convert() {
    }

    private static class ConvertAll<U> implements Converter<Object, U> {

        private static final long serialVersionUID = 2508560107067092501L;

        private final Class<? extends U> toClass;

        ConvertAll(Class<? extends U> toClass) {
            this.toClass = toClass;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public U from(Object from) {
            if (from == null) {
                if (toClass.isPrimitive()) {
                    if (toClass == char.class) {
                        return (U) Character.valueOf((char) 0);
                    } else {
                        return convert(0, toClass);
                    }
                } else {
                    return null;
                }
            } else {
                final Class<?> fromClass = from.getClass();
                if (toClass == fromClass) {
                    return (U) from;
                } else if (toClass.isAssignableFrom(fromClass)) {
                    return (U) from;
                } else if (fromClass == byte[].class) {
                    return convert(Arrays.toString((byte[]) from), toClass);
                } else if (fromClass.isArray()) {
                    if (toClass == java.sql.Array.class) {
                        return (U) new MockArray(null, (Object[]) from, fromClass);
                    } else {
                        return (U) convertArray((Object[]) from, toClass);
                    }
                } else if (toClass == String.class) {
                    if (from instanceof EnumType) {
                        return (U) ((EnumType) from).getLiteral();
                    }
                    return (U) from.toString();
                } else if (toClass == Byte.class || toClass == byte.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Byte.valueOf(((Number) from).byteValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
                    }
                    try {
                        return (U) Byte.valueOf(new BigDecimal(from.toString().trim()).byteValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Short.class || toClass == short.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Short.valueOf(((Number) from).shortValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Short.valueOf((short) 1) : Short.valueOf((short) 0));
                    }
                    try {
                        return (U) Short.valueOf(new BigDecimal(from.toString().trim()).shortValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Integer.class || toClass == int.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Integer.valueOf(((Number) from).intValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Integer.valueOf(1) : Integer.valueOf(0));
                    }
                    try {
                        return (U) Integer.valueOf(new BigDecimal(from.toString().trim()).intValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Long.class || toClass == long.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Long.valueOf(((Number) from).longValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Long.valueOf(1L) : Long.valueOf(0L));
                    }
                    if (java.util.Date.class.isAssignableFrom(fromClass)) {
                        return (U) Long.valueOf(((java.util.Date) from).getTime());
                    }
                    try {
                        return (U) Long.valueOf(new BigDecimal(from.toString().trim()).longValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == UByte.class) {
                    try {
                        if (Number.class.isAssignableFrom(fromClass)) {
                            return (U) ubyte(((Number) from).shortValue());
                        }
                        if (fromClass == Boolean.class || fromClass == boolean.class) {
                            return (U) (((Boolean) from) ? ubyte(1) : ubyte(0));
                        }
                        return (U) ubyte(new BigDecimal(from.toString().trim()).shortValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == UShort.class) {
                    try {
                        if (Number.class.isAssignableFrom(fromClass)) {
                            return (U) ushort(((Number) from).intValue());
                        }
                        if (fromClass == Boolean.class || fromClass == boolean.class) {
                            return (U) (((Boolean) from) ? ushort(1) : ushort(0));
                        }
                        return (U) ushort(new BigDecimal(from.toString().trim()).intValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == UInteger.class) {
                    try {
                        if (Number.class.isAssignableFrom(fromClass)) {
                            return (U) uint(((Number) from).longValue());
                        }
                        if (fromClass == Boolean.class || fromClass == boolean.class) {
                            return (U) (((Boolean) from) ? uint(1) : uint(0));
                        }
                        return (U) uint(new BigDecimal(from.toString().trim()).longValue());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == ULong.class) {
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? ulong(1) : ulong(0));
                    }
                    if (java.util.Date.class.isAssignableFrom(fromClass)) {
                        return (U) ulong(((java.util.Date) from).getTime());
                    }
                    try {
                        return (U) ulong(new BigDecimal(from.toString().trim()).toBigInteger().toString());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Float.class || toClass == float.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Float.valueOf(((Number) from).floatValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Float.valueOf(1.0f) : Float.valueOf(0.0f));
                    }
                    try {
                        return (U) Float.valueOf(from.toString().trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Double.class || toClass == double.class) {
                    if (Number.class.isAssignableFrom(fromClass)) {
                        return (U) Double.valueOf(((Number) from).doubleValue());
                    }
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Double.valueOf(1.0) : Double.valueOf(0.0));
                    }
                    try {
                        return (U) Double.valueOf(from.toString().trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == BigDecimal.class) {
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? BigDecimal.ONE : BigDecimal.ZERO);
                    }
                    try {
                        return (U) new BigDecimal(from.toString().trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == BigInteger.class) {
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? BigInteger.ONE : BigInteger.ZERO);
                    }
                    try {
                        return (U) new BigDecimal(from.toString().trim()).toBigInteger();
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (toClass == Boolean.class || toClass == boolean.class) {
                    String s = from.toString().toLowerCase().trim();
                    if (TRUE_VALUES.contains(s)) {
                        return (U) Boolean.TRUE;
                    } else if (FALSE_VALUES.contains(s)) {
                        return (U) Boolean.FALSE;
                    } else {
                        return (U) (toClass == Boolean.class ? null : false);
                    }
                } else if (toClass == Character.class || toClass == char.class) {
                    if (fromClass == Boolean.class || fromClass == boolean.class) {
                        return (U) (((Boolean) from) ? Character.valueOf('1') : Character.valueOf('0'));
                    }
                    if (from.toString().length() < 1) {
                        return null;
                    }
                    return (U) Character.valueOf(from.toString().charAt(0));
                } else if ((fromClass == String.class) && toClass == URI.class) {
                    try {
                        return (U) new URI(from.toString());
                    } catch (URISyntaxException e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && toClass == URL.class) {
                    try {
                        return (U) new URI(from.toString()).toURL();
                    } catch (Exception e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && toClass == File.class) {
                    try {
                        return (U) new File(from.toString());
                    } catch (Exception e) {
                        return null;
                    }
                } else if (java.util.Date.class.isAssignableFrom(fromClass)) {
                    return toDate(((java.util.Date) from).getTime(), toClass);
                } else if ((fromClass == Long.class || fromClass == long.class) && java.util.Date.class.isAssignableFrom(toClass)) {
                    return toDate((Long) from, toClass);
                } else if ((fromClass == String.class) && toClass == java.sql.Date.class) {
                    try {
                        return (U) java.sql.Date.valueOf((String) from);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && toClass == java.sql.Time.class) {
                    try {
                        return (U) java.sql.Time.valueOf((String) from);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && toClass == java.sql.Timestamp.class) {
                    try {
                        return (U) java.sql.Timestamp.valueOf((String) from);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && java.lang.Enum.class.isAssignableFrom(toClass)) {
                    try {
                        return (U) java.lang.Enum.valueOf((Class) toClass, (String) from);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                } else if ((fromClass == String.class) && toClass == UUID.class) {
                    try {
                        return (U) parseUUID((String) from);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                } else if (Record.class.isAssignableFrom(fromClass)) {
                    Record record = (Record) from;
                    return record.into(toClass);
                }
            }
            throw fail(from, toClass);
        }

        @Override
        public Object to(U to) {
            return to;
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<U> toType() {
            return (Class<U>) toClass;
        }

        @SuppressWarnings("unchecked")
        private static <X> X toDate(long time, Class<X> toClass) {
            if (toClass == Date.class) {
                return (X) new Date(time);
            } else if (toClass == Time.class) {
                return (X) new Time(time);
            } else if (toClass == Timestamp.class) {
                return (X) new Timestamp(time);
            } else if (toClass == java.util.Date.class) {
                return (X) new java.util.Date(time);
            } else if (toClass == Calendar.class) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);
                return (X) calendar;
            }
            throw fail(time, toClass);
        }

        private static final UUID parseUUID(String string) {
            if (string == null) {
                return null;
            } else if (string.contains("-")) {
                return UUID.fromString(string);
            } else {
                return UUID.fromString(UUID_PATTERN.matcher(string).replaceAll("$1-$2-$3-$4-$5"));
            }
        }

        private static DataTypeException fail(Object from, Class<?> toClass) {
            return new DataTypeException("Cannot convert from " + from + " (" + from.getClass() + ") to " + toClass);
        }
    }
}