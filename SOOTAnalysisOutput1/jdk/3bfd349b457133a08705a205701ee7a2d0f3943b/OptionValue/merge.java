package org.graalvm.compiler.options;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

public class OptionValue<T> {

    public static OverrideScope override(OptionValue<?> option, Object value) {
        OverrideScope current = getOverrideScope();
        if (current == null) {
            if (!value.equals(option.getValue())) {
                return new SingleOverrideScope(option, value);
            }
            Map<OptionValue<?>, Object> overrides = Collections.emptyMap();
            return new MultipleOverridesScope(current, overrides);
        }
        return new MultipleOverridesScope(current, option, value);
    }

    public static OverrideScope override(Map<OptionValue<?>, Object> overrides) {
        OverrideScope current = getOverrideScope();
        if (current == null && overrides.size() == 1) {
            Entry<OptionValue<?>, Object> single = overrides.entrySet().iterator().next();
            OptionValue<?> option = single.getKey();
            Object overrideValue = single.getValue();
            return new SingleOverrideScope(option, overrideValue);
        }
        return new MultipleOverridesScope(current, overrides);
    }

    public static OverrideScope override(Object... overrides) {
        OverrideScope current = getOverrideScope();
        if (current == null && overrides.length == 2) {
            OptionValue<?> option = (OptionValue<?>) overrides[0];
            Object overrideValue = overrides[1];
            if (!overrideValue.equals(option.getValue())) {
                return new SingleOverrideScope(option, overrideValue);
            }
        }
        Map<OptionValue<?>, Object> map = Collections.emptyMap();
        for (int i = 0; i < overrides.length; i += 2) {
            OptionValue<?> option = (OptionValue<?>) overrides[i];
            Object overrideValue = overrides[i + 1];
            if (!overrideValue.equals(option.getValue())) {
                if (map.isEmpty()) {
                    map = new HashMap<>();
                }
                map.put(option, overrideValue);
            }
        }
        return new MultipleOverridesScope(current, map);
    }

    private static final ThreadLocal<OverrideScope> overrideScopeTL = new ThreadLocal<>();

    protected static OverrideScope getOverrideScope() {
        return overrideScopeTL.get();
    }

    protected static void setOverrideScope(OverrideScope overrideScope) {
        overrideScopeTL.set(overrideScope);
    }

    private T defaultValue;

    protected T value;

    private OptionDescriptor descriptor;

    private long reads;

    private OptionValue<?> next;

    private static OptionValue<?> head;

    public static final String PROFILE_OPTIONVALUE_PROPERTY_NAME = "graal.profileOptionValue";

    private static final boolean ProfileOptionValue = Boolean.getBoolean(PROFILE_OPTIONVALUE_PROPERTY_NAME);

    private static void addToHistogram(OptionValue<?> option) {
        if (ProfileOptionValue) {
            synchronized (OptionValue.class) {
                option.next = head;
                head = option;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public OptionValue(T value) {
        this.defaultValue = value;
        this.value = (T) DEFAULT;
        addToHistogram(this);
    }

    private static final Object DEFAULT = "DEFAULT";

    private static final Object UNINITIALIZED = "UNINITIALIZED";

    @SuppressWarnings("unchecked")
    protected OptionValue() {
        this.defaultValue = (T) UNINITIALIZED;
        this.value = (T) DEFAULT;
        addToHistogram(this);
    }

    protected T defaultValue() {
        throw new InternalError("Option without a default value value must override defaultValue()");
    }

    public void setDescriptor(OptionDescriptor descriptor) {
        assert this.descriptor == null : "Overwriting existing descriptor";
        this.descriptor = descriptor;
    }

    public OptionDescriptor getDescriptor() {
        return descriptor;
    }

    static class Lazy {

        static void init() {
            ServiceLoader<OptionDescriptors> loader = ServiceLoader.load(OptionDescriptors.class, OptionDescriptors.class.getClassLoader());
            for (OptionDescriptors opts : loader) {
                for (OptionDescriptor desc : opts) {
                    desc.getName();
                }
            }
        }
    }

    public String getName() {
        if (descriptor == null) {
            Lazy.init();
        }
        return descriptor == null ? super.toString() : descriptor.getName();
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }

    public T getDefaultValue() {
        if (defaultValue == UNINITIALIZED) {
            defaultValue = defaultValue();
        }
        return defaultValue;
    }

    public boolean hasBeenSet() {
        if (!(this instanceof StableOptionValue)) {
            getValue();
            OverrideScope overrideScope = getOverrideScope();
            if (overrideScope != null) {
                T override = overrideScope.getOverride(this);
                if (override != null) {
                    return true;
                }
            }
        }
        return value != DEFAULT;
    }

    public T getValue() {
        if (ProfileOptionValue) {
            reads++;
        }
        if (!(this instanceof StableOptionValue)) {
            OverrideScope overrideScope = getOverrideScope();
            if (overrideScope != null) {
                T override = overrideScope.getOverride(this);
                if (override != null) {
                    return override;
                }
            }
        }
        if (value != DEFAULT) {
            return value;
        } else {
            return getDefaultValue();
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<T> getValues(Collection<T> c) {
        Collection<T> values = c == null ? new ArrayList<>() : c;
        if (!(this instanceof StableOptionValue)) {
            OverrideScope overrideScope = getOverrideScope();
            if (overrideScope != null) {
                overrideScope.getOverrides(this, (Collection<Object>) values);
            }
        }
        if (value != DEFAULT) {
            values.add(value);
        } else {
            values.add(getDefaultValue());
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object v) {
        this.value = (T) v;
    }

    public abstract static class OverrideScope implements AutoCloseable {

        private Map<DerivedOptionValue<?>, Object> derivedCache = null;

        public <T> T getDerived(DerivedOptionValue<T> key) {
            if (derivedCache == null) {
                derivedCache = new HashMap<>();
            }
            @SuppressWarnings("unchecked")
            T ret = (T) derivedCache.get(key);
            if (ret == null) {
                ret = key.createValue();
                derivedCache.put(key, ret);
            }
            return ret;
        }

        abstract void addToInherited(Map<OptionValue<?>, Object> inherited);

        abstract <T> T getOverride(OptionValue<T> option);

        abstract void getOverrides(OptionValue<?> option, Collection<Object> c);

        @Override
        public abstract void close();
    }

    static class SingleOverrideScope extends OverrideScope {

        private final OptionValue<?> option;

        private final Object value;

        SingleOverrideScope(OptionValue<?> option, Object value) {
            if (option instanceof StableOptionValue) {
                throw new IllegalArgumentException("Cannot override stable option " + option);
            }
            this.option = option;
            this.value = value;
            setOverrideScope(this);
        }

        @Override
        void addToInherited(Map<OptionValue<?>, Object> inherited) {
            inherited.put(option, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        <T> T getOverride(OptionValue<T> key) {
            if (key == this.option) {
                return (T) value;
            }
            return null;
        }

        @Override
        void getOverrides(OptionValue<?> key, Collection<Object> c) {
            if (key == this.option) {
                c.add(value);
            }
        }

        @Override
        public void close() {
            setOverrideScope(null);
        }
    }

    static class MultipleOverridesScope extends OverrideScope {

        final OverrideScope parent;

        final Map<OptionValue<?>, Object> overrides;

        MultipleOverridesScope(OverrideScope parent, OptionValue<?> option, Object value) {
            this.parent = parent;
            this.overrides = new HashMap<>();
            if (parent != null) {
                parent.addToInherited(overrides);
            }
            if (option instanceof StableOptionValue) {
                throw new IllegalArgumentException("Cannot override stable option " + option);
            }
            if (!value.equals(option.getValue())) {
                this.overrides.put(option, value);
            }
            if (!overrides.isEmpty()) {
                setOverrideScope(this);
            }
        }

        MultipleOverridesScope(OverrideScope parent, Map<OptionValue<?>, Object> overrides) {
            this.parent = parent;
            if (overrides.isEmpty() && parent == null) {
                this.overrides = Collections.emptyMap();
                return;
            }
            this.overrides = new HashMap<>();
            if (parent != null) {
                parent.addToInherited(this.overrides);
            }
            for (Map.Entry<OptionValue<?>, Object> e : overrides.entrySet()) {
                OptionValue<?> option = e.getKey();
                if (option instanceof StableOptionValue) {
                    throw new IllegalArgumentException("Cannot override stable option " + option);
                }
                this.overrides.put(option, e.getValue());
            }
            if (!this.overrides.isEmpty()) {
                setOverrideScope(this);
            }
        }

        @Override
        void addToInherited(Map<OptionValue<?>, Object> inherited) {
            if (parent != null) {
                parent.addToInherited(inherited);
            }
            inherited.putAll(overrides);
        }

        @SuppressWarnings("unchecked")
        @Override
        <T> T getOverride(OptionValue<T> option) {
            return (T) overrides.get(option);
        }

        @Override
        void getOverrides(OptionValue<?> option, Collection<Object> c) {
            Object v = overrides.get(option);
            if (v != null) {
                c.add(v);
            }
            if (parent != null) {
                parent.getOverrides(option, c);
            }
        }

        @Override
        public void close() {
            if (!overrides.isEmpty()) {
                setOverrideScope(parent);
            }
        }
    }

    static {
        if (ProfileOptionValue) {
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    ArrayList<OptionValue<?>> options = new ArrayList<>();
                    for (OptionValue<?> option = head; option != null; option = option.next) {
                        options.add(option);
                    }
                    Collections.sort(options, new Comparator<OptionValue<?>>() {

                        @Override
                        public int compare(OptionValue<?> o1, OptionValue<?> o2) {
                            if (o1.reads < o2.reads) {
                                return -1;
                            } else if (o1.reads > o2.reads) {
                                return 1;
                            } else {
                                return o1.getName().compareTo(o2.getName());
                            }
                        }
                    });
                    PrintStream out = System.out;
                    out.println("=== OptionValue reads histogram ===");
                    for (OptionValue<?> option : options) {
                        out.println(option.reads + "\t" + option);
                    }
                }
            });
        }
    }
}
