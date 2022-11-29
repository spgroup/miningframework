package org.graalvm.options;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class OptionType<T> {

    private final String name;

    private final Function<String, T> stringConverter;

    private final Consumer<T> validator;

    private final T defaultValue;

    public OptionType(String name, T defaultValue, Function<String, T> stringConverter, Consumer<T> validator) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(defaultValue);
        Objects.requireNonNull(stringConverter);
        Objects.requireNonNull(validator);
        this.name = name;
        this.stringConverter = stringConverter;
        this.defaultValue = defaultValue;
        this.validator = validator;
    }

    public OptionType(String name, T defaultValue, Function<String, T> stringConverter) {
        this(name, defaultValue, stringConverter, new Consumer<T>() {

            public void accept(T t) {
            }
        });
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public T convert(String value) {
        T v = stringConverter.apply(value);
        validate(v);
        return v;
    }

    public void validate(T value) {
        validator.accept(value);
    }

    @Override
    public String toString() {
        return "OptionType[name=" + name + ", defaultValue=" + defaultValue + "]";
    }

    private static Map<Class<?>, OptionType<?>> DEFAULTTYPES = new HashMap<>();

    static {
        DEFAULTTYPES.put(Boolean.class, new OptionType<>("Boolean", false, new Function<String, Boolean>() {

            public Boolean apply(String t) {
                if ("true".equals(t)) {
                    return Boolean.TRUE;
                } else if ("false".equals(t)) {
                    return Boolean.FALSE;
                } else {
                    throw new IllegalArgumentException(String.format("Invalid boolean option value '%s'. The value of the option must be '%s' or '%s'.", t, "true", "false"));
                }
            }
        }));
        DEFAULTTYPES.put(Byte.class, new OptionType<>("Byte", (byte) 0, new Function<String, Byte>() {

            public Byte apply(String t) {
                try {
                    return Byte.parseByte(t);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }));
        DEFAULTTYPES.put(Integer.class, new OptionType<>("Integer", 0, new Function<String, Integer>() {

            public Integer apply(String t) {
                try {
                    return Integer.parseInt(t);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }));
        DEFAULTTYPES.put(Long.class, new OptionType<>("Long", 0L, new Function<String, Long>() {

            public Long apply(String t) {
                try {
                    return Long.parseLong(t);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }));
        DEFAULTTYPES.put(Float.class, new OptionType<>("Float", 0.0f, new Function<String, Float>() {

            public Float apply(String t) {
                try {
                    return Float.parseFloat(t);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }));
        DEFAULTTYPES.put(Double.class, new OptionType<>("Double", 0.0d, new Function<String, Double>() {

            public Double apply(String t) {
                try {
                    return Double.parseDouble(t);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }));
        DEFAULTTYPES.put(String.class, new OptionType<>("String", "0", new Function<String, String>() {

            public String apply(String t) {
                return t;
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public static <T> OptionType<T> defaultType(Object value) {
        return (OptionType<T>) DEFAULTTYPES.get(value.getClass());
    }
}