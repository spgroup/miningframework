package com.fasterxml.jackson.databind.introspect;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.AttributePropertyWriter;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.util.*;

public class JacksonAnnotationIntrospector extends AnnotationIntrospector implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private final static Class<? extends Annotation>[] ANNOTATIONS_TO_INFER_SER = (Class<? extends Annotation>[]) new Class<?>[] { JsonSerialize.class, JsonView.class, JsonFormat.class, JsonTypeInfo.class, JsonRawValue.class, JsonUnwrapped.class, JsonBackReference.class, JsonManagedReference.class };

    @SuppressWarnings("unchecked")
    private final static Class<? extends Annotation>[] ANNOTATIONS_TO_INFER_DESER = (Class<? extends Annotation>[]) new Class<?>[] { JsonDeserialize.class, JsonView.class, JsonFormat.class, JsonTypeInfo.class, JsonUnwrapped.class, JsonBackReference.class, JsonManagedReference.class };

    private static final Java7Support _jdk7Helper;

    static {
        Java7Support x = null;
        try {
            x = Java7Support.class.newInstance();
        } catch (Throwable t) {
            java.util.logging.Logger.getLogger(JacksonAnnotationIntrospector.class.getName()).warning("Unable to load JDK7 annotation types; will have to skip");
        }
        _jdk7Helper = x;
    }

    protected transient LRUMap<Class<?>, Boolean> _annotationsInside = new LRUMap<Class<?>, Boolean>(48, 48);

    public JacksonAnnotationIntrospector() {
    }

    @Override
    public Version version() {
        return com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION;
    }

    protected Object readResolve() {
        if (_annotationsInside == null) {
            _annotationsInside = new LRUMap<Class<?>, Boolean>(48, 48);
        }
        return this;
    }

    @Override
    public boolean isAnnotationBundle(Annotation ann) {
        Class<?> type = ann.annotationType();
        Boolean b = _annotationsInside.get(type);
        if (b == null) {
            b = type.getAnnotation(JacksonAnnotationsInside.class) != null;
            _annotationsInside.putIfAbsent(type, b);
        }
        return b.booleanValue();
    }

    @Override
    public String findEnumValue(Enum<?> value) {
        try {
            Field f = value.getClass().getField(value.name());
            if (f != null) {
                JsonProperty prop = f.getAnnotation(JsonProperty.class);
                if (prop != null) {
                    String n = prop.value();
                    if (n != null && !n.isEmpty()) {
                        return n;
                    }
                }
            }
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        }
        return value.name();
    }

    @Override
    public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
        HashMap<String, String> expl = null;
        for (Field f : ClassUtil.getDeclaredFields(enumType)) {
            if (!f.isEnumConstant()) {
                continue;
            }
            JsonProperty prop = f.getAnnotation(JsonProperty.class);
            if (prop == null) {
                continue;
            }
            String n = prop.value();
            if (n.isEmpty()) {
                continue;
            }
            if (expl == null) {
                expl = new HashMap<String, String>();
            }
            expl.put(f.getName(), n);
        }
        if (expl != null) {
            for (int i = 0, end = enumValues.length; i < end; ++i) {
                String defName = enumValues[i].name();
                names[i] = expl.get(defName);
            }
        }
        return names;
    }

    @Override
    public PropertyName findRootName(AnnotatedClass ac) {
        JsonRootName ann = _findAnnotation(ac, JsonRootName.class);
        if (ann == null) {
            return null;
        }
        String ns = ann.namespace();
        if (ns != null && ns.length() == 0) {
            ns = null;
        }
        return PropertyName.construct(ann.value(), ns);
    }

    @Override
    @Deprecated
    public String[] findPropertiesToIgnore(Annotated ac) {
        JsonIgnoreProperties ignore = _findAnnotation(ac, JsonIgnoreProperties.class);
        return (ignore == null) ? null : ignore.value();
    }

    @Override
    public String[] findPropertiesToIgnore(Annotated ac, boolean forSerialization) {
        JsonIgnoreProperties ignore = _findAnnotation(ac, JsonIgnoreProperties.class);
        if (ignore == null) {
            return null;
        }
        if (forSerialization) {
            if (ignore.allowGetters()) {
                return null;
            }
        } else {
            if (ignore.allowSetters()) {
                return null;
            }
        }
        return ignore.value();
    }

    @Override
    public Boolean findIgnoreUnknownProperties(AnnotatedClass ac) {
        JsonIgnoreProperties ignore = _findAnnotation(ac, JsonIgnoreProperties.class);
        return (ignore == null) ? null : ignore.ignoreUnknown();
    }

    @Override
    public Boolean isIgnorableType(AnnotatedClass ac) {
        JsonIgnoreType ignore = _findAnnotation(ac, JsonIgnoreType.class);
        return (ignore == null) ? null : ignore.value();
    }

    @Override
    public Object findFilterId(Annotated a) {
        JsonFilter ann = _findAnnotation(a, JsonFilter.class);
        if (ann != null) {
            String id = ann.value();
            if (id.length() > 0) {
                return id;
            }
        }
        return null;
    }

    @Override
    public Object findNamingStrategy(AnnotatedClass ac) {
        JsonNaming ann = _findAnnotation(ac, JsonNaming.class);
        return (ann == null) ? null : ann.value();
    }

    @Override
    public String findClassDescription(AnnotatedClass ac) {
        JsonClassDescription ann = _findAnnotation(ac, JsonClassDescription.class);
        return (ann == null) ? null : ann.value();
    }

    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker) {
        JsonAutoDetect ann = _findAnnotation(ac, JsonAutoDetect.class);
        return (ann == null) ? checker : checker.with(ann);
    }

    @Override
    public String findImplicitPropertyName(AnnotatedMember param) {
        return null;
    }

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        return _isIgnorable(m);
    }

    @Override
    public Boolean hasRequiredMarker(AnnotatedMember m) {
        JsonProperty ann = _findAnnotation(m, JsonProperty.class);
        if (ann != null) {
            return ann.required();
        }
        return null;
    }

    @Override
    public JsonProperty.Access findPropertyAccess(Annotated m) {
        JsonProperty ann = _findAnnotation(m, JsonProperty.class);
        if (ann != null) {
            return ann.access();
        }
        return null;
    }

    @Override
    public String findPropertyDescription(Annotated ann) {
        JsonPropertyDescription desc = _findAnnotation(ann, JsonPropertyDescription.class);
        return (desc == null) ? null : desc.value();
    }

    @Override
    public Integer findPropertyIndex(Annotated ann) {
        JsonProperty prop = _findAnnotation(ann, JsonProperty.class);
        if (prop != null) {
            int ix = prop.index();
            if (ix != JsonProperty.INDEX_UNKNOWN) {
                return Integer.valueOf(ix);
            }
        }
        return null;
    }

    @Override
    public String findPropertyDefaultValue(Annotated ann) {
        JsonProperty prop = _findAnnotation(ann, JsonProperty.class);
        if (prop == null) {
            return null;
        }
        String str = prop.defaultValue();
        return str.isEmpty() ? null : str;
    }

    @Override
    public JsonFormat.Value findFormat(Annotated ann) {
        JsonFormat f = _findAnnotation(ann, JsonFormat.class);
        return (f == null) ? null : new JsonFormat.Value(f);
    }

    @Override
    public ReferenceProperty findReferenceType(AnnotatedMember member) {
        JsonManagedReference ref1 = _findAnnotation(member, JsonManagedReference.class);
        if (ref1 != null) {
            return AnnotationIntrospector.ReferenceProperty.managed(ref1.value());
        }
        JsonBackReference ref2 = _findAnnotation(member, JsonBackReference.class);
        if (ref2 != null) {
            return AnnotationIntrospector.ReferenceProperty.back(ref2.value());
        }
        return null;
    }

    @Override
    public NameTransformer findUnwrappingNameTransformer(AnnotatedMember member) {
        JsonUnwrapped ann = _findAnnotation(member, JsonUnwrapped.class);
        if (ann == null || !ann.enabled()) {
            return null;
        }
        String prefix = ann.prefix();
        String suffix = ann.suffix();
        return NameTransformer.simpleTransformer(prefix, suffix);
    }

    @Override
    public Object findInjectableValueId(AnnotatedMember m) {
        JacksonInject ann = _findAnnotation(m, JacksonInject.class);
        if (ann == null) {
            return null;
        }
        String id = ann.value();
        if (id.length() == 0) {
            if (!(m instanceof AnnotatedMethod)) {
                return m.getRawType().getName();
            }
            AnnotatedMethod am = (AnnotatedMethod) m;
            if (am.getParameterCount() == 0) {
                return m.getRawType().getName();
            }
            return am.getRawParameterType(0).getName();
        }
        return id;
    }

    @Override
    public Class<?>[] findViews(Annotated a) {
        JsonView ann = _findAnnotation(a, JsonView.class);
        return (ann == null) ? null : ann.value();
    }

    @Override
    public AnnotatedMethod resolveSetterConflict(MapperConfig<?> config, AnnotatedMethod setter1, AnnotatedMethod setter2) {
        Class<?> cls1 = setter1.getRawParameterType(0);
        Class<?> cls2 = setter2.getRawParameterType(0);
        if (cls1.isPrimitive()) {
            if (!cls2.isPrimitive()) {
                return setter1;
            }
        } else if (cls2.isPrimitive()) {
            return setter2;
        }
        if (cls1 == String.class) {
            if (cls2 != String.class) {
                return setter1;
            }
        } else if (cls2 == String.class) {
            return setter2;
        }
        return null;
    }

    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
        return _findTypeResolver(config, ac, baseType);
    }

    @Override
    public TypeResolverBuilder<?> findPropertyTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType baseType) {
        if (baseType.isContainerType())
            return null;
        return _findTypeResolver(config, am, baseType);
    }

    @Override
    public TypeResolverBuilder<?> findPropertyContentTypeResolver(MapperConfig<?> config, AnnotatedMember am, JavaType containerType) {
        if (containerType.getContentType() == null) {
            throw new IllegalArgumentException("Must call method with a container or reference type (got " + containerType + ")");
        }
        return _findTypeResolver(config, am, containerType);
    }

    @Override
    public List<NamedType> findSubtypes(Annotated a) {
        JsonSubTypes t = _findAnnotation(a, JsonSubTypes.class);
        if (t == null)
            return null;
        JsonSubTypes.Type[] types = t.value();
        ArrayList<NamedType> result = new ArrayList<NamedType>(types.length);
        for (JsonSubTypes.Type type : types) {
            result.add(new NamedType(type.value(), type.name()));
        }
        return result;
    }

    @Override
    public String findTypeName(AnnotatedClass ac) {
        JsonTypeName tn = _findAnnotation(ac, JsonTypeName.class);
        return (tn == null) ? null : tn.value();
    }

    @Override
    public Boolean isTypeId(AnnotatedMember member) {
        return _hasAnnotation(member, JsonTypeId.class);
    }

    @Override
    public ObjectIdInfo findObjectIdInfo(Annotated ann) {
        JsonIdentityInfo info = _findAnnotation(ann, JsonIdentityInfo.class);
        if (info == null || info.generator() == ObjectIdGenerators.None.class) {
            return null;
        }
        PropertyName name = PropertyName.construct(info.property());
        return new ObjectIdInfo(name, info.scope(), info.generator(), info.resolver());
    }

    @Override
    public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo objectIdInfo) {
        JsonIdentityReference ref = _findAnnotation(ann, JsonIdentityReference.class);
        if (ref != null) {
            objectIdInfo = objectIdInfo.withAlwaysAsId(ref.alwaysAsId());
        }
        return objectIdInfo;
    }

    @Override
    public Object findSerializer(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonSerializer> serClass = ann.using();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        JsonRawValue annRaw = _findAnnotation(a, JsonRawValue.class);
        if ((annRaw != null) && annRaw.value()) {
            Class<?> cls = a.getRawType();
            return new RawSerializer<Object>(cls);
        }
        return null;
    }

    @Override
    public Object findKeySerializer(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonSerializer> serClass = ann.keyUsing();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        return null;
    }

    @Override
    public Object findContentSerializer(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonSerializer> serClass = ann.contentUsing();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        return null;
    }

    @Override
    public Object findNullSerializer(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonSerializer> serClass = ann.nullsUsing();
            if (serClass != JsonSerializer.None.class) {
                return serClass;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public JsonInclude.Include findSerializationInclusion(Annotated a, JsonInclude.Include defValue) {
        JsonInclude inc = _findAnnotation(a, JsonInclude.class);
        if (inc != null) {
            JsonInclude.Include v = inc.value();
            if (v != JsonInclude.Include.USE_DEFAULTS) {
                return v;
            }
        }
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        if (ann != null) {
            JsonSerialize.Inclusion i2 = ann.include();
            switch(i2) {
                case ALWAYS:
                    return JsonInclude.Include.ALWAYS;
                case NON_NULL:
                    return JsonInclude.Include.NON_NULL;
                case NON_DEFAULT:
                    return JsonInclude.Include.NON_DEFAULT;
                case NON_EMPTY:
                    return JsonInclude.Include.NON_EMPTY;
                case DEFAULT_INCLUSION:
                    break;
            }
        }
        return defValue;
    }

    @Override
    @Deprecated
    public JsonInclude.Include findSerializationInclusionForContent(Annotated a, JsonInclude.Include defValue) {
        JsonInclude inc = _findAnnotation(a, JsonInclude.class);
        if (inc != null) {
            JsonInclude.Include incl = inc.content();
            if (incl != JsonInclude.Include.USE_DEFAULTS) {
                return incl;
            }
        }
        return defValue;
    }

    @Override
    @SuppressWarnings("deprecation")
    public JsonInclude.Value findPropertyInclusion(Annotated a) {
        JsonInclude inc = _findAnnotation(a, JsonInclude.class);
        JsonInclude.Include valueIncl = (inc == null) ? JsonInclude.Include.USE_DEFAULTS : inc.value();
        if (valueIncl == JsonInclude.Include.USE_DEFAULTS) {
            JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
            if (ann != null) {
                JsonSerialize.Inclusion i2 = ann.include();
                switch(i2) {
                    case ALWAYS:
                        valueIncl = JsonInclude.Include.ALWAYS;
                        break;
                    case NON_NULL:
                        valueIncl = JsonInclude.Include.NON_NULL;
                        break;
                    case NON_DEFAULT:
                        valueIncl = JsonInclude.Include.NON_DEFAULT;
                        break;
                    case NON_EMPTY:
                        valueIncl = JsonInclude.Include.NON_EMPTY;
                        break;
                    case DEFAULT_INCLUSION:
                    default:
                }
            }
        }
        JsonInclude.Include contentIncl = (inc == null) ? JsonInclude.Include.USE_DEFAULTS : inc.content();
        return JsonInclude.Value.construct(valueIncl, contentIncl);
    }

    @Override
    @Deprecated
    public Class<?> findSerializationType(Annotated am) {
        JsonSerialize ann = _findAnnotation(am, JsonSerialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.as());
    }

    @Override
    @Deprecated
    public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
        JsonSerialize ann = _findAnnotation(am, JsonSerialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.keyAs());
    }

    @Override
    @Deprecated
    public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
        JsonSerialize ann = _findAnnotation(am, JsonSerialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.contentAs());
    }

    @Override
    public JsonSerialize.Typing findSerializationTyping(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        return (ann == null) ? null : ann.typing();
    }

    @Override
    public Object findSerializationConverter(Annotated a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.converter(), Converter.None.class);
    }

    @Override
    public Object findSerializationContentConverter(AnnotatedMember a) {
        JsonSerialize ann = _findAnnotation(a, JsonSerialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.contentConverter(), Converter.None.class);
    }

    @Override
    public String[] findSerializationPropertyOrder(AnnotatedClass ac) {
        JsonPropertyOrder order = _findAnnotation(ac, JsonPropertyOrder.class);
        return (order == null) ? null : order.value();
    }

    @Override
    public Boolean findSerializationSortAlphabetically(Annotated ann) {
        return _findSortAlpha(ann);
    }

    private final Boolean _findSortAlpha(Annotated ann) {
        JsonPropertyOrder order = _findAnnotation(ann, JsonPropertyOrder.class);
        if ((order != null) && order.alphabetic()) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public void findAndAddVirtualProperties(MapperConfig<?> config, AnnotatedClass ac, List<BeanPropertyWriter> properties) {
        JsonAppend ann = _findAnnotation(ac, JsonAppend.class);
        if (ann == null) {
            return;
        }
        final boolean prepend = ann.prepend();
        JavaType propType = null;
        JsonAppend.Attr[] attrs = ann.attrs();
        for (int i = 0, len = attrs.length; i < len; ++i) {
            if (propType == null) {
                propType = config.constructType(Object.class);
            }
            BeanPropertyWriter bpw = _constructVirtualProperty(attrs[i], config, ac, propType);
            if (prepend) {
                properties.add(i, bpw);
            } else {
                properties.add(bpw);
            }
        }
        JsonAppend.Prop[] props = ann.props();
        for (int i = 0, len = props.length; i < len; ++i) {
            BeanPropertyWriter bpw = _constructVirtualProperty(props[i], config, ac);
            if (prepend) {
                properties.add(i, bpw);
            } else {
                properties.add(bpw);
            }
        }
    }

    protected BeanPropertyWriter _constructVirtualProperty(JsonAppend.Attr attr, MapperConfig<?> config, AnnotatedClass ac, JavaType type) {
        PropertyMetadata metadata = attr.required() ? PropertyMetadata.STD_REQUIRED : PropertyMetadata.STD_OPTIONAL;
        String attrName = attr.value();
        PropertyName propName = _propertyName(attr.propName(), attr.propNamespace());
        if (!propName.hasSimpleName()) {
            propName = PropertyName.construct(attrName);
        }
        AnnotatedMember member = new VirtualAnnotatedMember(ac, ac.getRawType(), attrName, type.getRawClass());
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(config, member, propName, metadata, attr.include());
        return AttributePropertyWriter.construct(attrName, propDef, ac.getAnnotations(), type);
    }

    protected BeanPropertyWriter _constructVirtualProperty(JsonAppend.Prop prop, MapperConfig<?> config, AnnotatedClass ac) {
        PropertyMetadata metadata = prop.required() ? PropertyMetadata.STD_REQUIRED : PropertyMetadata.STD_OPTIONAL;
        PropertyName propName = _propertyName(prop.name(), prop.namespace());
        JavaType type = config.constructType(prop.type());
        AnnotatedMember member = new VirtualAnnotatedMember(ac, ac.getRawType(), propName.getSimpleName(), type.getRawClass());
        SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(config, member, propName, metadata, prop.include());
        Class<?> implClass = prop.value();
        HandlerInstantiator hi = config.getHandlerInstantiator();
        VirtualBeanPropertyWriter bpw = (hi == null) ? null : hi.virtualPropertyWriterInstance(config, implClass);
        if (bpw == null) {
            bpw = (VirtualBeanPropertyWriter) ClassUtil.createInstance(implClass, config.canOverrideAccessModifiers());
        }
        return bpw.withConfig(config, ac, propDef, type);
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        JsonGetter jg = _findAnnotation(a, JsonGetter.class);
        if (jg != null) {
            return PropertyName.construct(jg.value());
        }
        JsonProperty pann = _findAnnotation(a, JsonProperty.class);
        if (pann != null) {
            return PropertyName.construct(pann.value());
        }
        PropertyName ctorName = _findConstructorName(a);
        if (ctorName != null) {
            return ctorName;
        }
        if (_hasOneOf(a, ANNOTATIONS_TO_INFER_SER)) {
            return PropertyName.USE_DEFAULT;
        }
        return null;
    }

    @Override
    public boolean hasAsValueAnnotation(AnnotatedMethod am) {
        JsonValue ann = _findAnnotation(am, JsonValue.class);
        return (ann != null && ann.value());
    }

    @Override
    public Object findDeserializer(Annotated a) {
        JsonDeserialize ann = _findAnnotation(a, JsonDeserialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonDeserializer> deserClass = ann.using();
            if (deserClass != JsonDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }

    @Override
    public Object findKeyDeserializer(Annotated a) {
        JsonDeserialize ann = _findAnnotation(a, JsonDeserialize.class);
        if (ann != null) {
            Class<? extends KeyDeserializer> deserClass = ann.keyUsing();
            if (deserClass != KeyDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }

    @Override
    public Object findContentDeserializer(Annotated a) {
        JsonDeserialize ann = _findAnnotation(a, JsonDeserialize.class);
        if (ann != null) {
            @SuppressWarnings("rawtypes")
            Class<? extends JsonDeserializer> deserClass = ann.contentUsing();
            if (deserClass != JsonDeserializer.None.class) {
                return deserClass;
            }
        }
        return null;
    }

    @Override
    public Object findDeserializationConverter(Annotated a) {
        JsonDeserialize ann = _findAnnotation(a, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.converter(), Converter.None.class);
    }

    @Override
    public Object findDeserializationContentConverter(AnnotatedMember a) {
        JsonDeserialize ann = _findAnnotation(a, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.contentConverter(), Converter.None.class);
    }

    @Override
    @Deprecated
    public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
        JsonDeserialize ann = _findAnnotation(am, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.contentAs());
    }

    @Deprecated
    @Override
    public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
        JsonDeserialize ann = _findAnnotation(am, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.as());
    }

    @Override
    @Deprecated
    public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType) {
        JsonDeserialize ann = _findAnnotation(am, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.keyAs());
    }

    @Override
    public Object findValueInstantiator(AnnotatedClass ac) {
        JsonValueInstantiator ann = _findAnnotation(ac, JsonValueInstantiator.class);
        return (ann == null) ? null : ann.value();
    }

    @Override
    public Class<?> findPOJOBuilder(AnnotatedClass ac) {
        JsonDeserialize ann = _findAnnotation(ac, JsonDeserialize.class);
        return (ann == null) ? null : _classIfExplicit(ann.builder());
    }

    @Override
    public JsonPOJOBuilder.Value findPOJOBuilderConfig(AnnotatedClass ac) {
        JsonPOJOBuilder ann = _findAnnotation(ac, JsonPOJOBuilder.class);
        return (ann == null) ? null : new JsonPOJOBuilder.Value(ann);
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a) {
        JsonSetter js = _findAnnotation(a, JsonSetter.class);
        if (js != null) {
            return PropertyName.construct(js.value());
        }
        JsonProperty pann = _findAnnotation(a, JsonProperty.class);
        if (pann != null) {
            return PropertyName.construct(pann.value());
        }
        PropertyName ctorName = _findConstructorName(a);
        if (ctorName != null) {
            return ctorName;
        }
        if (_hasOneOf(a, ANNOTATIONS_TO_INFER_DESER)) {
            return PropertyName.USE_DEFAULT;
        }
        return null;
    }

    @Override
    public boolean hasAnySetterAnnotation(AnnotatedMethod am) {
        return _hasAnnotation(am, JsonAnySetter.class);
    }

    @Override
    public boolean hasAnyGetterAnnotation(AnnotatedMethod am) {
        return _hasAnnotation(am, JsonAnyGetter.class);
    }

    @Override
    public boolean hasCreatorAnnotation(Annotated a) {
        JsonCreator ann = _findAnnotation(a, JsonCreator.class);
        if (ann != null) {
            return (ann.mode() != JsonCreator.Mode.DISABLED);
        }
        if (a instanceof AnnotatedConstructor) {
            if (_jdk7Helper != null) {
                Boolean b = _jdk7Helper.hasCreatorAnnotation(a);
                if (b != null) {
                    return b.booleanValue();
                }
            }
        }
        return false;
    }

    @Override
    public JsonCreator.Mode findCreatorBinding(Annotated a) {
        JsonCreator ann = _findAnnotation(a, JsonCreator.class);
        return (ann == null) ? null : ann.mode();
    }

    protected boolean _isIgnorable(Annotated a) {
        JsonIgnore ann = _findAnnotation(a, JsonIgnore.class);
        if (ann != null) {
            return ann.value();
        }
        if (_jdk7Helper != null) {
            Boolean b = _jdk7Helper.findTransient(a);
            if (b != null) {
                return b.booleanValue();
            }
        }
        return false;
    }

    protected Class<?> _classIfExplicit(Class<?> cls) {
        if (cls == null || ClassUtil.isBogusClass(cls)) {
            return null;
        }
        return cls;
    }

    protected Class<?> _classIfExplicit(Class<?> cls, Class<?> implicit) {
        cls = _classIfExplicit(cls);
        return (cls == null || cls == implicit) ? null : cls;
    }

    protected PropertyName _propertyName(String localName, String namespace) {
        if (localName.isEmpty()) {
            return PropertyName.USE_DEFAULT;
        }
        if (namespace == null || namespace.isEmpty()) {
            return PropertyName.construct(localName);
        }
        return PropertyName.construct(localName, namespace);
    }

    protected PropertyName _findConstructorName(Annotated a) {
        if (a instanceof AnnotatedParameter) {
            AnnotatedParameter p = (AnnotatedParameter) a;
            AnnotatedWithParams ctor = p.getOwner();
            if (ctor != null) {
                if (_jdk7Helper != null) {
                    PropertyName name = _jdk7Helper.findConstructorName(p);
                    if (name != null) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    protected TypeResolverBuilder<?> _findTypeResolver(MapperConfig<?> config, Annotated ann, JavaType baseType) {
        TypeResolverBuilder<?> b;
        JsonTypeInfo info = _findAnnotation(ann, JsonTypeInfo.class);
        JsonTypeResolver resAnn = _findAnnotation(ann, JsonTypeResolver.class);
        if (resAnn != null) {
            if (info == null) {
                return null;
            }
            b = config.typeResolverBuilderInstance(ann, resAnn.value());
        } else {
            if (info == null) {
                return null;
            }
            if (info.use() == JsonTypeInfo.Id.NONE) {
                return _constructNoTypeResolverBuilder();
            }
            b = _constructStdTypeResolverBuilder();
        }
        JsonTypeIdResolver idResInfo = _findAnnotation(ann, JsonTypeIdResolver.class);
        TypeIdResolver idRes = (idResInfo == null) ? null : config.typeIdResolverInstance(ann, idResInfo.value());
        if (idRes != null) {
            idRes.init(baseType);
        }
        b = b.init(info.use(), idRes);
        JsonTypeInfo.As inclusion = info.include();
        if (inclusion == JsonTypeInfo.As.EXTERNAL_PROPERTY && (ann instanceof AnnotatedClass)) {
            inclusion = JsonTypeInfo.As.PROPERTY;
        }
        b = b.inclusion(inclusion);
        b = b.typeProperty(info.property());
        Class<?> defaultImpl = info.defaultImpl();
        if (defaultImpl != JsonTypeInfo.None.class && !defaultImpl.isAnnotation()) {
            b = b.defaultImpl(defaultImpl);
        }
        b = b.typeIdVisibility(info.visible());
        return b;
    }

    protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
        return new StdTypeResolverBuilder();
    }

    protected StdTypeResolverBuilder _constructNoTypeResolverBuilder() {
        return StdTypeResolverBuilder.noTypeInfoBuilder();
    }

    private static class Java7Support {

        @SuppressWarnings("unused")
        private final Class<?> _bogus;

        @SuppressWarnings("unused")
        public Java7Support() {
            Class<?> cls = Transient.class;
            cls = ConstructorProperties.class;
            _bogus = cls;
        }

        public Boolean findTransient(Annotated a) {
            Transient t = a.getAnnotation(Transient.class);
            if (t != null) {
                return t.value();
            }
            return null;
        }

        public Boolean hasCreatorAnnotation(Annotated a) {
            ConstructorProperties props = a.getAnnotation(ConstructorProperties.class);
            if (props != null) {
                return Boolean.TRUE;
            }
            return null;
        }

        public PropertyName findConstructorName(AnnotatedParameter p) {
            AnnotatedWithParams ctor = p.getOwner();
            if (ctor != null) {
                ConstructorProperties props = ctor.getAnnotation(ConstructorProperties.class);
                if (props != null) {
                    String[] names = props.value();
                    int ix = p.getIndex();
                    if (ix < names.length) {
                        return PropertyName.construct(names[ix]);
                    }
                }
            }
            return null;
        }
    }
}
