package org.springframework.boot.context.properties.bind;

import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.Assert;

final class BindConverter {

    private static final Set<Class<?>> EXCLUDED_EDITORS;

    static {
        Set<Class<?>> excluded = new HashSet<>();
        excluded.add(FileEditor.class);
        EXCLUDED_EDITORS = Collections.unmodifiableSet(excluded);
    }

    private static BindConverter sharedInstance;

    private final ConversionService conversionService;

    private BindConverter(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        List<ConversionService> conversionServices = getConversionServices(conversionService, propertyEditorInitializer);
        this.conversionService = new CompositeConversionService(conversionServices);
    }

    private List<ConversionService> getConversionServices(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        List<ConversionService> services = new ArrayList<>();
        services.add(new TypeConverterConversionService(propertyEditorInitializer));
        services.add(conversionService);
        if (!(conversionService instanceof ApplicationConversionService)) {
            services.add(ApplicationConversionService.getSharedInstance());
        }
        return services;
    }

    boolean canConvert(Object value, ResolvableType type, Annotation... annotations) {
        return this.conversionService.canConvert(TypeDescriptor.forObject(value), new ResolvableTypeDescriptor(type, annotations));
    }

    <T> T convert(Object result, Bindable<T> target) {
        return convert(result, target.getType(), target.getAnnotations());
    }

    @SuppressWarnings("unchecked")
    <T> T convert(Object value, ResolvableType type, Annotation... annotations) {
        if (value == null) {
            return null;
        }
        return (T) this.conversionService.convert(value, TypeDescriptor.forObject(value), new ResolvableTypeDescriptor(type, annotations));
    }

    static BindConverter get(ConversionService conversionService, Consumer<PropertyEditorRegistry> propertyEditorInitializer) {
        if (conversionService == ApplicationConversionService.getSharedInstance() && propertyEditorInitializer == null) {
            if (sharedInstance == null) {
                sharedInstance = new BindConverter(conversionService, propertyEditorInitializer);
            }
            return sharedInstance;
        }
        return new BindConverter(conversionService, propertyEditorInitializer);
    }

    private static class ResolvableTypeDescriptor extends TypeDescriptor {

        ResolvableTypeDescriptor(ResolvableType resolvableType, Annotation[] annotations) {
            super(resolvableType, null, annotations);
        }
    }

    static class CompositeConversionService implements ConversionService {

        private final List<ConversionService> delegates;

        CompositeConversionService(List<ConversionService> delegates) {
            this.delegates = delegates;
        }

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            Assert.notNull(targetType, "Target type to convert to cannot be null");
            return canConvert((sourceType != null) ? TypeDescriptor.valueOf(sourceType) : null, TypeDescriptor.valueOf(targetType));
        }

        @Override
        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (ConversionService service : this.delegates) {
                if (service.canConvert(sourceType, targetType)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object source, Class<T> targetType) {
            Assert.notNull(targetType, "Target type to convert to cannot be null");
            return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            ConversionException failure = null;
            for (ConversionService delegate : this.delegates) {
                try {
                    if (delegate.canConvert(sourceType, targetType)) {
                        return delegate.convert(source, sourceType, targetType);
                    }
                } catch (ConversionException ex) {
                    if (failure == null && ex instanceof ConversionFailedException) {
                        failure = ex;
                    }
                }
            }
            throw (failure != null) ? failure : new ConverterNotFoundException(sourceType, targetType);
        }
    }

    private static class TypeConverterConversionService extends GenericConversionService {

        TypeConverterConversionService(Consumer<PropertyEditorRegistry> initializer) {
            addConverter(new TypeConverterConverter(initializer));
            ApplicationConversionService.addDelimitedStringConverters(this);
        }

        @Override
        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (targetType.isArray() && targetType.getElementTypeDescriptor().isPrimitive()) {
                return false;
            }
            return super.canConvert(sourceType, targetType);
        }
    }

    private static class TypeConverterConverter implements ConditionalGenericConverter {

        private final Consumer<PropertyEditorRegistry> initializer;

        private final SimpleTypeConverter matchesOnlyTypeConverter;

        TypeConverterConverter(Consumer<PropertyEditorRegistry> initializer) {
            this.initializer = initializer;
            this.matchesOnlyTypeConverter = createTypeConverter();
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(String.class, Object.class));
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            Class<?> type = targetType.getType();
            if (type == null || type == Object.class || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                return false;
            }
            PropertyEditor editor = this.matchesOnlyTypeConverter.getDefaultEditor(type);
            if (editor == null) {
                editor = this.matchesOnlyTypeConverter.findCustomEditor(type, null);
            }
            if (editor == null && String.class != type) {
                editor = BeanUtils.findEditorByConvention(type);
            }
            return (editor != null && !EXCLUDED_EDITORS.contains(editor.getClass()));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return createTypeConverter().convertIfNecessary(source, targetType.getType());
        }

        private SimpleTypeConverter createTypeConverter() {
            SimpleTypeConverter typeConverter = new SimpleTypeConverter();
            if (this.initializer != null) {
                this.initializer.accept(typeConverter);
            }
            return typeConverter;
        }
    }
}
