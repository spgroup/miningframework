package org.springframework.boot.context.properties.bind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.ConfigurationPropertyState;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.Assert;

public class Binder {

    private static final Set<Class<?>> NON_BEAN_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Object.class, Class.class)));

    private static final List<BeanBinder> BEAN_BINDERS;

    static {
        List<BeanBinder> binders = new ArrayList<>();
        binders.add(new JavaBeanBinder());
        BEAN_BINDERS = Collections.unmodifiableList(binders);
    }

    private final Iterable<ConfigurationPropertySource> sources;

    private final PlaceholdersResolver placeholdersResolver;

    private final ConversionService conversionService;

    private final Consumer<PropertyEditorRegistry> propertyEditorInitializer;

    public Binder(ConfigurationPropertySource... sources) {
        this(Arrays.asList(sources), null, null, null);
    }

    public Binder(Iterable<ConfigurationPropertySource> sources) {
        this(sources, null, null, null);
    }

    public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver) {
        this(sources, placeholdersResolver, null, null);
    }

    public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver, ConversionService conversionService) {
        this(sources, placeholdersResolver, conversionService, null);
    }

    public Binder(Iterable<ConfigurationPropertySource> sources, PlaceholdersResolver placeholdersResolver, ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        Assert.notNull(sources, "Sources must not be null");
        this.sources = sources;
        this.placeholdersResolver = (placeholdersResolver != null) ? placeholdersResolver : PlaceholdersResolver.NONE;
        this.conversionService = (conversionService != null) ? conversionService : ApplicationConversionService.getSharedInstance();
        this.propertyEditorInitializer = propertyEditorInitializer;
    }

    public <T> BindResult<T> bind(String name, Class<T> target) {
        return bind(name, Bindable.of(target));
    }

    public <T> BindResult<T> bind(String name, Bindable<T> target) {
        return bind(ConfigurationPropertyName.of(name), target, null);
    }

    public <T> BindResult<T> bind(ConfigurationPropertyName name, Bindable<T> target) {
        return bind(name, target, null);
    }

    public <T> BindResult<T> bind(String name, Bindable<T> target, BindHandler handler) {
        return bind(ConfigurationPropertyName.of(name), target, handler);
    }

    public <T> BindResult<T> bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler) {
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(target, "Target must not be null");
        handler = (handler != null) ? handler : BindHandler.DEFAULT;
        Context context = new Context();
        T bound = bind(name, target, handler, context, false);
        return BindResult.of(bound);
    }

    protected final <T> T bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context, boolean allowRecursiveBinding) {
        context.clearConfigurationProperty();
        try {
            target = handler.onStart(name, target, context);
            if (target == null) {
                return null;
            }
            Object bound = bindObject(name, target, handler, context, allowRecursiveBinding);
            return handleBindResult(name, target, handler, context, bound);
        } catch (Exception ex) {
            return handleBindError(name, target, handler, context, ex);
        }
    }

    private <T> T handleBindResult(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context, Object result) throws Exception {
        if (result != null) {
            result = handler.onSuccess(name, target, context, result);
            result = context.getConverter().convert(result, target);
        }
        handler.onFinish(name, target, context, result);
        return context.getConverter().convert(result, target);
    }

    private <T> T handleBindError(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context, Exception error) {
        try {
            Object result = handler.onFailure(name, target, context, error);
            return context.getConverter().convert(result, target);
        } catch (Exception ex) {
            if (ex instanceof BindException) {
                throw (BindException) ex;
            }
            throw new BindException(name, target, context.getConfigurationProperty(), ex);
        }
    }

    private <T> Object bindObject(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context, boolean allowRecursiveBinding) {
        ConfigurationProperty property = findProperty(name, context);
        if (property == null && containsNoDescendantOf(context.getSources(), name)) {
            return null;
        }
        AggregateBinder<?> aggregateBinder = getAggregateBinder(target, context);
        if (aggregateBinder != null) {
            return bindAggregate(name, target, handler, context, aggregateBinder);
        }
        if (property != null) {
            try {
                return bindProperty(target, context, property);
            } catch (ConverterNotFoundException ex) {
                Object bean = bindBean(name, target, handler, context, allowRecursiveBinding);
                if (bean != null) {
                    return bean;
                }
                throw ex;
            }
        }
        return bindBean(name, target, handler, context, allowRecursiveBinding);
    }

    private AggregateBinder<?> getAggregateBinder(Bindable<?> target, Context context) {
        Class<?> resolvedType = target.getType().resolve(Object.class);
        if (Map.class.isAssignableFrom(resolvedType)) {
            return new MapBinder(context);
        }
        if (Collection.class.isAssignableFrom(resolvedType)) {
            return new CollectionBinder(context);
        }
        if (target.getType().isArray()) {
            return new ArrayBinder(context);
        }
        return null;
    }

    private <T> Object bindAggregate(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler, Context context, AggregateBinder<?> aggregateBinder) {
        AggregateElementBinder elementBinder = (itemName, itemTarget, source) -> {
            boolean allowRecursiveBinding = aggregateBinder.isAllowRecursiveBinding(source);
            Supplier<?> supplier = () -> bind(itemName, itemTarget, handler, context, allowRecursiveBinding);
            return context.withSource(source, supplier);
        };
        return context.withIncreasedDepth(() -> aggregateBinder.bind(name, target, elementBinder));
    }

    private ConfigurationProperty findProperty(ConfigurationPropertyName name, Context context) {
        if (name.isEmpty()) {
            return null;
        }
        for (ConfigurationPropertySource source : context.getSources()) {
            ConfigurationProperty property = source.getConfigurationProperty(name);
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    private <T> Object bindProperty(Bindable<T> target, Context context, ConfigurationProperty property) {
        context.setConfigurationProperty(property);
        Object result = property.getValue();
        result = this.placeholdersResolver.resolvePlaceholders(result);
        result = context.getConverter().convert(result, target);
        return result;
    }

    private Object bindBean(ConfigurationPropertyName name, Bindable<?> target, BindHandler handler, Context context, boolean allowRecursiveBinding) {
        if (containsNoDescendantOf(context.getSources(), name) || isUnbindableBean(name, target, context)) {
            return null;
        }
        BeanPropertyBinder propertyBinder = (propertyName, propertyTarget) -> bind(name.append(propertyName), propertyTarget, handler, context, false);
        Class<?> type = target.getType().resolve(Object.class);
        if (!allowRecursiveBinding && context.hasBoundBean(type)) {
            return null;
        }
        return context.withBean(type, () -> {
            Stream<?> boundBeans = BEAN_BINDERS.stream().map((b) -> b.bind(name, target, context, propertyBinder));
            return boundBeans.filter(Objects::nonNull).findFirst().orElse(null);
        });
    }

    private boolean isUnbindableBean(ConfigurationPropertyName name, Bindable<?> target, Context context) {
        for (ConfigurationPropertySource source : context.getSources()) {
            if (source.containsDescendantOf(name) == ConfigurationPropertyState.PRESENT) {
                return false;
            }
        }
        Class<?> resolved = target.getType().resolve(Object.class);
        if (resolved.isPrimitive() || NON_BEAN_CLASSES.contains(resolved)) {
            return true;
        }
        return resolved.getName().startsWith("java.");
    }

    private boolean containsNoDescendantOf(Iterable<ConfigurationPropertySource> sources, ConfigurationPropertyName name) {
        for (ConfigurationPropertySource source : sources) {
            if (source.containsDescendantOf(name) != ConfigurationPropertyState.ABSENT) {
                return false;
            }
        }
        return true;
    }

    public static Binder get(Environment environment) {
        return new Binder(ConfigurationPropertySources.get(environment), new PropertySourcesPlaceholdersResolver(environment));
    }

    final class Context implements BindContext {

        private final BindConverter converter;

        private int depth;

        private final List<ConfigurationPropertySource> source = Arrays.asList((ConfigurationPropertySource) null);

        private int sourcePushCount;

        private final Deque<Class<?>> beans = new ArrayDeque<>();

        private ConfigurationProperty configurationProperty;

        Context() {
            this.converter = BindConverter.get(Binder.this.conversionService, Binder.this.propertyEditorInitializer);
        }

        private void increaseDepth() {
            this.depth++;
        }

        private void decreaseDepth() {
            this.depth--;
        }

        private <T> T withSource(ConfigurationPropertySource source, Supplier<T> supplier) {
            if (source == null) {
                return supplier.get();
            }
            this.source.set(0, source);
            this.sourcePushCount++;
            try {
                return supplier.get();
            } finally {
                this.sourcePushCount--;
            }
        }

        private <T> T withBean(Class<?> bean, Supplier<T> supplier) {
            this.beans.push(bean);
            try {
                return withIncreasedDepth(supplier);
            } finally {
                this.beans.pop();
            }
        }

        private boolean hasBoundBean(Class<?> bean) {
            return this.beans.contains(bean);
        }

        private <T> T withIncreasedDepth(Supplier<T> supplier) {
            increaseDepth();
            try {
                return supplier.get();
            } finally {
                decreaseDepth();
            }
        }

        private void setConfigurationProperty(ConfigurationProperty configurationProperty) {
            this.configurationProperty = configurationProperty;
        }

        private void clearConfigurationProperty() {
            this.configurationProperty = null;
        }

        public PlaceholdersResolver getPlaceholdersResolver() {
            return Binder.this.placeholdersResolver;
        }

        public BindConverter getConverter() {
            return this.converter;
        }

        @Override
        public Binder getBinder() {
            return Binder.this;
        }

        @Override
        public int getDepth() {
            return this.depth;
        }

        @Override
        public Iterable<ConfigurationPropertySource> getSources() {
            if (this.sourcePushCount > 0) {
                return this.source;
            }
            return Binder.this.sources;
        }

        @Override
        public ConfigurationProperty getConfigurationProperty() {
            return this.configurationProperty;
        }
    }
}
