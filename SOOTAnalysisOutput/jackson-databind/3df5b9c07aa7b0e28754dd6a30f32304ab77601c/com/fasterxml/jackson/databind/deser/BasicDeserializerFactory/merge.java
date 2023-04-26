package com.fasterxml.jackson.databind.deser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.ext.OptionalHandlerFactory;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

@SuppressWarnings("serial")
public abstract class BasicDeserializerFactory extends DeserializerFactory implements java.io.Serializable {

    private final static Class<?> CLASS_OBJECT = Object.class;

    private final static Class<?> CLASS_STRING = String.class;

    private final static Class<?> CLASS_CHAR_BUFFER = CharSequence.class;

    private final static Class<?> CLASS_ITERABLE = Iterable.class;

    private final static Class<?> CLASS_MAP_ENTRY = Map.Entry.class;

    protected final static PropertyName UNWRAPPED_CREATOR_PARAM_NAME = new PropertyName("@JsonUnwrapped");

    @SuppressWarnings("rawtypes")
    final static HashMap<String, Class<? extends Map>> _mapFallbacks = new HashMap<String, Class<? extends Map>>();

    static {
        _mapFallbacks.put(Map.class.getName(), LinkedHashMap.class);
        _mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
        _mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);
        _mapFallbacks.put(java.util.NavigableMap.class.getName(), TreeMap.class);
        _mapFallbacks.put(java.util.concurrent.ConcurrentNavigableMap.class.getName(), java.util.concurrent.ConcurrentSkipListMap.class);
    }

    @SuppressWarnings("rawtypes")
    final static HashMap<String, Class<? extends Collection>> _collectionFallbacks = new HashMap<String, Class<? extends Collection>>();

    static {
        _collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
        _collectionFallbacks.put(List.class.getName(), ArrayList.class);
        _collectionFallbacks.put(Set.class.getName(), HashSet.class);
        _collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
        _collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
        _collectionFallbacks.put("java.util.Deque", LinkedList.class);
        _collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
    }

    protected final DeserializerFactoryConfig _factoryConfig;

    protected BasicDeserializerFactory(DeserializerFactoryConfig config) {
        _factoryConfig = config;
    }

    public DeserializerFactoryConfig getFactoryConfig() {
        return _factoryConfig;
    }

    protected abstract DeserializerFactory withConfig(DeserializerFactoryConfig config);

    @Override
    public final DeserializerFactory withAdditionalDeserializers(Deserializers additional) {
        return withConfig(_factoryConfig.withAdditionalDeserializers(additional));
    }

    @Override
    public final DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers additional) {
        return withConfig(_factoryConfig.withAdditionalKeyDeserializers(additional));
    }

    @Override
    public final DeserializerFactory withDeserializerModifier(BeanDeserializerModifier modifier) {
        return withConfig(_factoryConfig.withDeserializerModifier(modifier));
    }

    @Override
    public final DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver resolver) {
        return withConfig(_factoryConfig.withAbstractTypeResolver(resolver));
    }

    @Override
    public final DeserializerFactory withValueInstantiators(ValueInstantiators instantiators) {
        return withConfig(_factoryConfig.withValueInstantiators(instantiators));
    }

    @Override
    public JavaType mapAbstractType(DeserializationConfig config, JavaType type) throws JsonMappingException {
        while (true) {
            JavaType next = _mapAbstractType2(config, type);
            if (next == null) {
                return type;
            }
            Class<?> prevCls = type.getRawClass();
            Class<?> nextCls = next.getRawClass();
            if ((prevCls == nextCls) || !prevCls.isAssignableFrom(nextCls)) {
                throw new IllegalArgumentException("Invalid abstract type resolution from " + type + " to " + next + ": latter is not a subtype of former");
            }
            type = next;
        }
    }

    private JavaType _mapAbstractType2(DeserializationConfig config, JavaType type) throws JsonMappingException {
        Class<?> currClass = type.getRawClass();
        if (_factoryConfig.hasAbstractTypeResolvers()) {
            for (AbstractTypeResolver resolver : _factoryConfig.abstractTypeResolvers()) {
                JavaType concrete = resolver.findTypeMapping(config, type);
                if (concrete != null && concrete.getRawClass() != currClass) {
                    return concrete;
                }
            }
        }
        return null;
    }

    @Override
    public ValueInstantiator findValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        ValueInstantiator instantiator = null;
        AnnotatedClass ac = beanDesc.getClassInfo();
        Object instDef = ctxt.getAnnotationIntrospector().findValueInstantiator(ac);
        if (instDef != null) {
            instantiator = _valueInstantiatorInstance(config, ac, instDef);
        }
        if (instantiator == null) {
            instantiator = _findStdValueInstantiator(config, beanDesc);
            if (instantiator == null) {
                instantiator = _constructDefaultValueInstantiator(ctxt, beanDesc);
            }
        }
        if (_factoryConfig.hasValueInstantiators()) {
            for (ValueInstantiators insts : _factoryConfig.valueInstantiators()) {
                instantiator = insts.findValueInstantiator(config, beanDesc, instantiator);
                if (instantiator == null) {
                    throw JsonMappingException.from(ctxt.getParser(), "Broken registered ValueInstantiators (of type " + insts.getClass().getName() + "): returned null ValueInstantiator");
                }
            }
        }
        if (instantiator.getIncompleteParameter() != null) {
            final AnnotatedParameter nonAnnotatedParam = instantiator.getIncompleteParameter();
            final AnnotatedWithParams ctor = nonAnnotatedParam.getOwner();
            throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of constructor " + ctor + " has no property name annotation; must have name when multiple-parameter constructor annotated as Creator");
        }
        return instantiator;
    }

    private ValueInstantiator _findStdValueInstantiator(DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        if (beanDesc.getBeanClass() == JsonLocation.class) {
            return new JsonLocationInstantiator();
        }
        return null;
    }

    protected ValueInstantiator _constructDefaultValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        CreatorCollector creators = new CreatorCollector(beanDesc, ctxt.getConfig());
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        final DeserializationConfig config = ctxt.getConfig();
        VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
        vchecker = intr.findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorDefs = _findCreatorsFromProperties(ctxt, beanDesc);
        _addDeserializerFactoryMethods(ctxt, beanDesc, vchecker, intr, creators, creatorDefs);
        if (beanDesc.getType().isConcrete()) {
            _addDeserializerConstructors(ctxt, beanDesc, vchecker, intr, creators, creatorDefs);
        }
        return creators.constructValueInstantiator(config);
    }

    protected Map<AnnotatedWithParams, BeanPropertyDefinition[]> _findCreatorsFromProperties(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> result = Collections.emptyMap();
        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            Iterator<AnnotatedParameter> it = propDef.getConstructorParameters();
            while (it.hasNext()) {
                AnnotatedParameter param = it.next();
                AnnotatedWithParams owner = param.getOwner();
                BeanPropertyDefinition[] defs = result.get(owner);
                final int index = param.getIndex();
                if (defs == null) {
                    if (result.isEmpty()) {
                        result = new LinkedHashMap<AnnotatedWithParams, BeanPropertyDefinition[]>();
                    }
                    defs = new BeanPropertyDefinition[owner.getParameterCount()];
                    result.put(owner, defs);
                } else {
                    if (defs[index] != null) {
                        throw new IllegalStateException("Conflict: parameter #" + index + " of " + owner + " bound to more than one property; " + defs[index] + " vs " + propDef);
                    }
                }
                defs[index] = propDef;
            }
        }
        return result;
    }

    public ValueInstantiator _valueInstantiatorInstance(DeserializationConfig config, Annotated annotated, Object instDef) throws JsonMappingException {
        if (instDef == null) {
            return null;
        }
        ValueInstantiator inst;
        if (instDef instanceof ValueInstantiator) {
            return (ValueInstantiator) instDef;
        }
        if (!(instDef instanceof Class)) {
            throw new IllegalStateException("AnnotationIntrospector returned key deserializer definition of type " + instDef.getClass().getName() + "; expected type KeyDeserializer or Class<KeyDeserializer> instead");
        }
        Class<?> instClass = (Class<?>) instDef;
        if (ClassUtil.isBogusClass(instClass)) {
            return null;
        }
        if (!ValueInstantiator.class.isAssignableFrom(instClass)) {
            throw new IllegalStateException("AnnotationIntrospector returned Class " + instClass.getName() + "; expected Class<ValueInstantiator>");
        }
        HandlerInstantiator hi = config.getHandlerInstantiator();
        if (hi != null) {
            inst = hi.valueInstantiatorInstance(config, annotated, instClass);
            if (inst != null) {
                return inst;
            }
        }
        return (ValueInstantiator) ClassUtil.createInstance(instClass, config.canOverrideAccessModifiers());
    }

    protected void _addDeserializerConstructors(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams) throws JsonMappingException {
        AnnotatedConstructor defaultCtor = beanDesc.findDefaultConstructor();
        if (defaultCtor != null) {
            if (!creators.hasDefaultCreator() || intr.hasCreatorAnnotation(defaultCtor)) {
                creators.setDefaultCreator(defaultCtor);
            }
        }
        List<AnnotatedConstructor> implicitCtors = null;
        for (AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            final boolean isCreator = intr.hasCreatorAnnotation(ctor);
            BeanPropertyDefinition[] propDefs = creatorParams.get(ctor);
            final int argCount = ctor.getParameterCount();
            if (argCount == 1) {
                BeanPropertyDefinition argDef = (propDefs == null) ? null : propDefs[0];
                boolean useProps = _checkIfCreatorPropertyBased(intr, ctor, argDef);
                if (useProps) {
                    SettableBeanProperty[] properties = new SettableBeanProperty[1];
                    PropertyName name = (argDef == null) ? null : argDef.getFullName();
                    AnnotatedParameter arg = ctor.getParameter(0);
                    properties[0] = constructCreatorProperty(ctxt, beanDesc, name, 0, arg, intr.findInjectableValueId(arg));
                    creators.addPropertyCreator(ctor, isCreator, properties);
                } else {
                    _handleSingleArgumentConstructor(ctxt, beanDesc, vchecker, intr, creators, ctor, isCreator, vchecker.isCreatorVisible(ctor));
                    if (argDef != null) {
                        ((POJOPropertyBuilder) argDef).removeConstructors();
                    }
                }
                continue;
            }
            AnnotatedParameter nonAnnotatedParam = null;
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            int explicitNameCount = 0;
            int implicitWithCreatorCount = 0;
            int injectCount = 0;
            for (int i = 0; i < argCount; ++i) {
                final AnnotatedParameter param = ctor.getParameter(i);
                BeanPropertyDefinition propDef = (propDefs == null) ? null : propDefs[i];
                Object injectId = intr.findInjectableValueId(param);
                final PropertyName name = (propDef == null) ? null : propDef.getFullName();
                if (propDef != null && propDef.isExplicitlyNamed()) {
                    ++explicitNameCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                    continue;
                }
                if (injectId != null) {
                    ++injectCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                    continue;
                }
                NameTransformer unwrapper = intr.findUnwrappingNameTransformer(param);
                if (unwrapper != null) {
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, UNWRAPPED_CREATOR_PARAM_NAME, i, param, null);
                    ++explicitNameCount;
                    continue;
                }
                if (isCreator && (name != null && !name.isEmpty())) {
                    ++implicitWithCreatorCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                    continue;
                }
                if (nonAnnotatedParam == null) {
                    nonAnnotatedParam = param;
                }
            }
            final int namedCount = explicitNameCount + implicitWithCreatorCount;
            if (isCreator || (explicitNameCount > 0) || (injectCount > 0)) {
                if ((namedCount + injectCount) == argCount) {
                    creators.addPropertyCreator(ctor, isCreator, properties);
                    continue;
                }
                if ((explicitNameCount == 0) && ((injectCount + 1) == argCount)) {
                    creators.addDelegatingCreator(ctor, isCreator, properties);
                    continue;
                }
                PropertyName impl = _findImplicitParamName(nonAnnotatedParam, intr);
                if (impl == null || impl.isEmpty()) {
                    int ix = nonAnnotatedParam.getIndex();
                    if ((ix == 0) && ClassUtil.isNonStaticInnerClass(ctor.getDeclaringClass())) {
                        throw new IllegalArgumentException("Non-static inner classes like " + ctor.getDeclaringClass().getName() + " can not use @JsonCreator for constructors");
                    }
                    throw new IllegalArgumentException("Argument #" + ix + " of constructor " + ctor + " has no property name annotation; must have name when multiple-parameter constructor annotated as Creator");
                }
            }
            if (!creators.hasDefaultCreator()) {
                if (implicitCtors == null) {
                    implicitCtors = new LinkedList<AnnotatedConstructor>();
                }
                implicitCtors.add(ctor);
            }
        }
        if ((implicitCtors != null) && !creators.hasDelegatingCreator() && !creators.hasPropertyBasedCreator()) {
            _checkImplicitlyNamedConstructors(ctxt, beanDesc, vchecker, intr, creators, implicitCtors);
        }
    }

    protected void _checkImplicitlyNamedConstructors(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, List<AnnotatedConstructor> implicitCtors) throws JsonMappingException {
        AnnotatedConstructor found = null;
        SettableBeanProperty[] foundProps = null;
        main_loop: for (AnnotatedConstructor ctor : implicitCtors) {
            if (!vchecker.isCreatorVisible(ctor)) {
                continue;
            }
            final int argCount = ctor.getParameterCount();
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                final AnnotatedParameter param = ctor.getParameter(i);
                final PropertyName name = _findParamName(param, intr);
                if (name == null || name.isEmpty()) {
                    continue main_loop;
                }
                properties[i] = constructCreatorProperty(ctxt, beanDesc, name, param.getIndex(), param, null);
            }
            if (found != null) {
                found = null;
                break;
            }
            found = ctor;
            foundProps = properties;
        }
        if (found != null) {
            creators.addPropertyCreator(found, false, foundProps);
            BasicBeanDescription bbd = (BasicBeanDescription) beanDesc;
            for (SettableBeanProperty prop : foundProps) {
                PropertyName pn = prop.getFullName();
                if (!bbd.hasProperty(pn)) {
                    BeanPropertyDefinition newDef = SimpleBeanPropertyDefinition.construct(ctxt.getConfig(), prop.getMember(), pn);
                    bbd.addProperty(newDef);
                }
            }
        }
    }

    protected boolean _checkIfCreatorPropertyBased(AnnotationIntrospector intr, AnnotatedWithParams creator, BeanPropertyDefinition propDef) {
        JsonCreator.Mode mode = intr.findCreatorBinding(creator);
        if (mode == JsonCreator.Mode.PROPERTIES) {
            return true;
        }
        if (mode == JsonCreator.Mode.DELEGATING) {
            return false;
        }
        if (((propDef != null) && propDef.isExplicitlyNamed()) || (intr.findInjectableValueId(creator.getParameter(0)) != null)) {
            return true;
        }
        if (propDef != null) {
            String implName = propDef.getName();
            if (implName != null && !implName.isEmpty()) {
                if (propDef.couldSerialize()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean _handleSingleArgumentConstructor(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, AnnotatedConstructor ctor, boolean isCreator, boolean isVisible) throws JsonMappingException {
        Class<?> type = ctor.getRawParameterType(0);
        if (type == String.class || type == CharSequence.class) {
            if (isCreator || isVisible) {
                creators.addStringCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || isVisible) {
                creators.addIntCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || isVisible) {
                creators.addLongCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || isVisible) {
                creators.addDoubleCreator(ctor, isCreator);
            }
            return true;
        }
        if (type == boolean.class || type == Boolean.class) {
            if (isCreator || isVisible) {
                creators.addBooleanCreator(ctor, isCreator);
            }
            return true;
        }
        if (isCreator) {
            creators.addDelegatingCreator(ctor, isCreator, null);
            return true;
        }
        return false;
    }

    protected void _addDeserializerFactoryMethods(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            final boolean isCreator = intr.hasCreatorAnnotation(factory);
            final int argCount = factory.getParameterCount();
            if (argCount == 0) {
                if (isCreator) {
                    creators.setDefaultCreator(factory);
                }
                continue;
            }
            final BeanPropertyDefinition[] propDefs = creatorParams.get(factory);
            if (argCount == 1) {
                BeanPropertyDefinition argDef = (propDefs == null) ? null : propDefs[0];
                boolean useProps = _checkIfCreatorPropertyBased(intr, factory, argDef);
                if (!useProps) {
                    _handleSingleArgumentFactory(config, beanDesc, vchecker, intr, creators, factory, isCreator);
                    continue;
                }
            } else {
                if (!isCreator) {
                    continue;
                }
            }
            AnnotatedParameter nonAnnotatedParam = null;
            SettableBeanProperty[] properties = new SettableBeanProperty[argCount];
            int implicitNameCount = 0;
            int explicitNameCount = 0;
            int injectCount = 0;
            for (int i = 0; i < argCount; ++i) {
                final AnnotatedParameter param = factory.getParameter(i);
                BeanPropertyDefinition propDef = (propDefs == null) ? null : propDefs[i];
                Object injectId = intr.findInjectableValueId(param);
                final PropertyName name = (propDef == null) ? null : propDef.getFullName();
                if (propDef != null && propDef.isExplicitlyNamed()) {
                    ++explicitNameCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                    continue;
                }
                if (injectId != null) {
                    ++injectCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                    continue;
                }
                NameTransformer unwrapper = intr.findUnwrappingNameTransformer(param);
                if (unwrapper != null) {
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, UNWRAPPED_CREATOR_PARAM_NAME, i, param, null);
                    ++implicitNameCount;
                    continue;
                }
                if (isCreator) {
                    if (name != null && !name.isEmpty()) {
                        ++implicitNameCount;
                        properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                        continue;
                    }
                }
                if (nonAnnotatedParam == null) {
                    nonAnnotatedParam = param;
                }
            }
            final int namedCount = explicitNameCount + implicitNameCount;
            if (isCreator || explicitNameCount > 0 || injectCount > 0) {
                if ((namedCount + injectCount) == argCount) {
                    creators.addPropertyCreator(factory, isCreator, properties);
                } else if ((explicitNameCount == 0) && ((injectCount + 1) == argCount)) {
                    creators.addDelegatingCreator(factory, isCreator, properties);
                } else {
                    throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of factory method " + factory + " has no property name annotation; must have name when multiple-parameter constructor annotated as Creator");
                }
            }
        }
    }

    protected boolean _handleSingleArgumentFactory(DeserializationConfig config, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, AnnotatedMethod factory, boolean isCreator) throws JsonMappingException {
        Class<?> type = factory.getRawParameterType(0);
        if (type == String.class || type == CharSequence.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addStringCreator(factory, isCreator);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addIntCreator(factory, isCreator);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addLongCreator(factory, isCreator);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addDoubleCreator(factory, isCreator);
            }
            return true;
        }
        if (type == boolean.class || type == Boolean.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addBooleanCreator(factory, isCreator);
            }
            return true;
        }
        if (isCreator) {
            creators.addDelegatingCreator(factory, isCreator, null);
            return true;
        }
        return false;
    }

    protected SettableBeanProperty constructCreatorProperty(DeserializationContext ctxt, BeanDescription beanDesc, PropertyName name, int index, AnnotatedParameter param, Object injectableValueId) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        final AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        PropertyMetadata metadata;
        {
            if (intr == null) {
                metadata = PropertyMetadata.STD_REQUIRED_OR_OPTIONAL;
            } else {
                Boolean b = intr.hasRequiredMarker(param);
                boolean req = (b != null && b.booleanValue());
                String desc = intr.findPropertyDescription(param);
                Integer idx = intr.findPropertyIndex(param);
                String def = intr.findPropertyDefaultValue(param);
                metadata = PropertyMetadata.construct(req, desc, idx, def);
            }
        }
        JavaType t0 = beanDesc.resolveType(param.getParameterType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, intr.findWrapperName(param), beanDesc.getClassAnnotations(), param, metadata);
        JavaType type = resolveType(ctxt, beanDesc, t0, param);
        if (type != t0) {
            property = property.withType(type);
        }
        JsonDeserializer<?> deser = findDeserializerFromAnnotation(ctxt, param);
        type = modifyTypeByAnnotation(ctxt, param, type);
        TypeDeserializer typeDeser = (TypeDeserializer) type.getTypeHandler();
        if (typeDeser == null) {
            typeDeser = findTypeDeserializer(config, type);
        }
        SettableBeanProperty prop = new CreatorProperty(name, type, property.getWrapperName(), typeDeser, beanDesc.getClassAnnotations(), param, index, injectableValueId, metadata);
        if (deser != null) {
            deser = ctxt.handlePrimaryContextualization(deser, prop, type);
            prop = prop.withValueDeserializer(deser);
        }
        return prop;
    }

    protected PropertyName _findParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
        if (param != null && intr != null) {
            PropertyName name = intr.findNameForDeserialization(param);
            if (name != null) {
                return name;
            }
            String str = intr.findImplicitPropertyName(param);
            if (str != null && !str.isEmpty()) {
                return PropertyName.construct(str);
            }
        }
        return null;
    }

    protected PropertyName _findImplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
        String str = intr.findImplicitPropertyName(param);
        if (str != null && !str.isEmpty()) {
            return PropertyName.construct(str);
        }
        return null;
    }

    @Deprecated
    protected PropertyName _findExplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
        if (param != null && intr != null) {
            return intr.findNameForDeserialization(param);
        }
        return null;
    }

    @Deprecated
    protected boolean _hasExplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
        if (param != null && intr != null) {
            PropertyName n = intr.findNameForDeserialization(param);
            return (n != null) && n.hasSimpleName();
        }
        return false;
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext ctxt, ArrayType type, final BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        JavaType elemType = type.getContentType();
        JsonDeserializer<Object> contentDeser = elemType.getValueHandler();
        TypeDeserializer elemTypeDeser = elemType.getTypeHandler();
        if (elemTypeDeser == null) {
            elemTypeDeser = findTypeDeserializer(config, elemType);
        }
        JsonDeserializer<?> deser = _findCustomArrayDeserializer(type, config, beanDesc, elemTypeDeser, contentDeser);
        if (deser == null) {
            if (contentDeser == null) {
                Class<?> raw = elemType.getRawClass();
                if (elemType.isPrimitive()) {
                    return PrimitiveArrayDeserializers.forType(raw);
                } else if (raw == String.class) {
                    return StringArrayDeserializer.instance;
                }
            }
            deser = new ObjectArrayDeserializer(type, contentDeser, elemTypeDeser);
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deser = mod.modifyArrayDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext ctxt, CollectionType type, BeanDescription beanDesc) throws JsonMappingException {
        JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        final DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType);
        }
        JsonDeserializer<?> deser = _findCustomCollectionDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser);
        if (deser == null) {
            Class<?> collectionClass = type.getRawClass();
            if (contentDeser == null) {
                if (EnumSet.class.isAssignableFrom(collectionClass)) {
                    deser = new EnumSetDeserializer(contentType, null);
                }
            }
        }
        if (deser == null) {
            if (type.isInterface() || type.isAbstract()) {
                CollectionType implType = _mapAbstractCollectionType(type, config);
                if (implType == null) {
                    if (type.getTypeHandler() == null) {
                        throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type " + type);
                    }
                    deser = AbstractDeserializer.constructForNonPOJO(beanDesc);
                } else {
                    type = implType;
                    beanDesc = config.introspectForCreation(type);
                }
            }
            if (deser == null) {
                ValueInstantiator inst = findValueInstantiator(ctxt, beanDesc);
                if (!inst.canCreateUsingDefault()) {
                    if (type.getRawClass() == ArrayBlockingQueue.class) {
                        return new ArrayBlockingQueueDeserializer(type, contentDeser, contentTypeDeser, inst);
                    }
                }
                if (contentType.getRawClass() == String.class) {
                    deser = new StringCollectionDeserializer(type, contentDeser, inst);
                } else {
                    deser = new CollectionDeserializer(type, contentDeser, contentTypeDeser, inst);
                }
            }
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deser = mod.modifyCollectionDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    protected CollectionType _mapAbstractCollectionType(JavaType type, DeserializationConfig config) {
        Class<?> collectionClass = type.getRawClass();
        collectionClass = _collectionFallbacks.get(collectionClass.getName());
        if (collectionClass == null) {
            return null;
        }
        return (CollectionType) config.constructSpecializedType(type, collectionClass);
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext ctxt, CollectionLikeType type, final BeanDescription beanDesc) throws JsonMappingException {
        JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        final DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType);
        }
        JsonDeserializer<?> deser = _findCustomCollectionLikeDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser);
        if (deser != null) {
            if (_factoryConfig.hasDeserializerModifiers()) {
                for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                    deser = mod.modifyCollectionLikeDeserializer(config, type, beanDesc, deser);
                }
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type, BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeser = (JsonDeserializer<Object>) contentType.getValueHandler();
        KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType);
        }
        JsonDeserializer<?> deser = _findCustomMapDeserializer(type, config, beanDesc, keyDes, contentTypeDeser, contentDeser);
        if (deser == null) {
            Class<?> mapClass = type.getRawClass();
            if (EnumMap.class.isAssignableFrom(mapClass)) {
                Class<?> kt = keyType.getRawClass();
                if (kt == null || !kt.isEnum()) {
                    throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
                }
                deser = new EnumMapDeserializer(type, null, contentDeser, contentTypeDeser);
            }
            if (deser == null) {
                if (type.isInterface() || type.isAbstract()) {
                    @SuppressWarnings("rawtypes")
                    Class<? extends Map> fallback = _mapFallbacks.get(mapClass.getName());
                    if (fallback != null) {
                        mapClass = fallback;
                        type = (MapType) config.constructSpecializedType(type, mapClass);
                        beanDesc = config.introspectForCreation(type);
                    } else {
                        if (type.getTypeHandler() == null) {
                            throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type " + type);
                        }
                        deser = AbstractDeserializer.constructForNonPOJO(beanDesc);
                    }
                }
                if (deser == null) {
                    ValueInstantiator inst = findValueInstantiator(ctxt, beanDesc);
                    MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
                    AnnotationIntrospector ai = config.getAnnotationIntrospector();
                    md.setIgnorableProperties(ai.findPropertiesToIgnore(beanDesc.getClassInfo(), false));
                    deser = md;
                }
            }
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deser = mod.modifyMapDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext ctxt, MapLikeType type, final BeanDescription beanDesc) throws JsonMappingException {
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        final DeserializationConfig config = ctxt.getConfig();
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeser = (JsonDeserializer<Object>) contentType.getValueHandler();
        KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType);
        }
        JsonDeserializer<?> deser = _findCustomMapLikeDeserializer(type, config, beanDesc, keyDes, contentTypeDeser, contentDeser);
        if (deser != null) {
            if (_factoryConfig.hasDeserializerModifiers()) {
                for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                    deser = mod.modifyMapLikeDeserializer(config, type, beanDesc, deser);
                }
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createEnumDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        final Class<?> enumClass = type.getRawClass();
        JsonDeserializer<?> deser = _findCustomEnumDeserializer(enumClass, config, beanDesc);
        if (deser == null) {
            for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
                if (ctxt.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                    int argCount = factory.getParameterCount();
                    if (argCount == 1) {
                        Class<?> returnType = factory.getRawReturnType();
                        if (returnType.isAssignableFrom(enumClass)) {
                            deser = EnumDeserializer.deserializerForCreator(config, enumClass, factory);
                            break;
                        }
                    }
                    throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
                }
            }
            if (deser == null) {
                deser = new EnumDeserializer(constructEnumResolver(enumClass, config, beanDesc.findJsonValueMethod()));
            }
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deser = mod.modifyEnumDeserializer(config, type, beanDesc, deser);
            }
        }
        return deser;
    }

    @Override
    public JsonDeserializer<?> createTreeDeserializer(DeserializationConfig config, JavaType nodeType, BeanDescription beanDesc) throws JsonMappingException {
        @SuppressWarnings("unchecked")
        Class<? extends JsonNode> nodeClass = (Class<? extends JsonNode>) nodeType.getRawClass();
        JsonDeserializer<?> custom = _findCustomTreeNodeDeserializer(nodeClass, config, beanDesc);
        if (custom != null) {
            return custom;
        }
        return JsonNodeDeserializer.getDeserializer(nodeClass);
    }

    @Override
    public JsonDeserializer<?> createReferenceDeserializer(DeserializationContext ctxt, ReferenceType type, BeanDescription beanDesc) throws JsonMappingException {
        JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        final DeserializationConfig config = ctxt.getConfig();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(config, contentType);
        }
        JsonDeserializer<?> deser = _findCustomReferenceDeserializer(type, config, beanDesc, contentTypeDeser, contentDeser);
        if (deser == null) {
            if (AtomicReference.class.isAssignableFrom(type.getRawClass())) {
                return new AtomicReferenceDeserializer(contentType, contentTypeDeser, contentDeser);
            }
        }
        if (deser != null) {
            if (_factoryConfig.hasDeserializerModifiers()) {
                for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                    deser = mod.modifyReferenceDeserializer(config, type, beanDesc, deser);
                }
            }
        }
        return deser;
    }

    @Override
    public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType) throws JsonMappingException {
        BeanDescription bean = config.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = config.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, ac);
        }
        if ((b.getDefaultImpl() == null) && baseType.isAbstract()) {
            JavaType defaultType = mapAbstractType(config, baseType);
            if (defaultType != null && defaultType.getRawClass() != baseType.getRawClass()) {
                b = b.defaultImpl(defaultType.getRawClass());
            }
        }
        return b.buildTypeDeserializer(config, baseType, subtypes);
    }

    protected JsonDeserializer<?> findOptionalStdDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        return OptionalHandlerFactory.instance.findDeserializer(type, ctxt.getConfig(), beanDesc);
    }

    @Override
    public KeyDeserializer createKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        KeyDeserializer deser = null;
        if (_factoryConfig.hasKeyDeserializers()) {
            BeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
            for (KeyDeserializers d : _factoryConfig.keyDeserializers()) {
                deser = d.findKeyDeserializer(type, config, beanDesc);
                if (deser != null) {
                    break;
                }
            }
        }
        if (deser == null) {
            if (type.isEnumType()) {
                return _createEnumKeyDeserializer(ctxt, type);
            }
            deser = StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
        }
        if (deser != null) {
            if (_factoryConfig.hasDeserializerModifiers()) {
                for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                    deser = mod.modifyKeyDeserializer(config, type, deser);
                }
            }
        }
        return deser;
    }

    private KeyDeserializer _createEnumKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        Class<?> enumClass = type.getRawClass();
        BeanDescription beanDesc = config.introspect(type);
        KeyDeserializer des = findKeyDeserializerFromAnnotation(ctxt, beanDesc.getClassInfo());
        if (des != null) {
            return des;
        } else {
            JsonDeserializer<?> custom = _findCustomEnumDeserializer(enumClass, config, beanDesc);
            if (custom != null) {
                return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, custom);
            }
            JsonDeserializer<?> valueDesForKey = findDeserializerFromAnnotation(ctxt, beanDesc.getClassInfo());
            if (valueDesForKey != null) {
                return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, valueDesForKey);
            }
        }
        EnumResolver enumRes = constructEnumResolver(enumClass, config, beanDesc.findJsonValueMethod());
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (ai.hasCreatorAnnotation(factory)) {
                int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    Class<?> returnType = factory.getRawReturnType();
                    if (returnType.isAssignableFrom(enumClass)) {
                        if (factory.getRawParameterType(0) != String.class) {
                            throw new IllegalArgumentException("Parameter #0 type for factory method (" + factory + ") not suitable, must be java.lang.String");
                        }
                        if (config.canOverrideAccessModifiers()) {
                            ClassUtil.checkAndFixAccess(factory.getMember(), ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
                        }
                        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
            }
        }
        return StdKeyDeserializers.constructEnumKeyDeserializer(enumRes);
    }

    public TypeDeserializer findPropertyTypeDeserializer(DeserializationConfig config, JavaType baseType, AnnotatedMember annotated) throws JsonMappingException {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyTypeResolver(config, annotated, baseType);
        if (b == null) {
            return findTypeDeserializer(config, baseType);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, annotated, baseType);
        return b.buildTypeDeserializer(config, baseType, subtypes);
    }

    public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig config, JavaType containerType, AnnotatedMember propertyEntity) throws JsonMappingException {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);
        JavaType contentType = containerType.getContentType();
        if (b == null) {
            return findTypeDeserializer(config, contentType);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypesByTypeId(config, propertyEntity, contentType);
        return b.buildTypeDeserializer(config, contentType, subtypes);
    }

    public JsonDeserializer<?> findDefaultDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        Class<?> rawType = type.getRawClass();
        if (rawType == CLASS_OBJECT) {
            DeserializationConfig config = ctxt.getConfig();
            JavaType lt, mt;
            if (_factoryConfig.hasAbstractTypeResolvers()) {
                lt = _findRemappedType(config, List.class);
                mt = _findRemappedType(config, Map.class);
            } else {
                lt = mt = null;
            }
            return new UntypedObjectDeserializer(lt, mt);
        }
        if (rawType == CLASS_STRING || rawType == CLASS_CHAR_BUFFER) {
            return StringDeserializer.instance;
        }
        if (rawType == CLASS_ITERABLE) {
            TypeFactory tf = ctxt.getTypeFactory();
            JavaType[] tps = tf.findTypeParameters(type, CLASS_ITERABLE);
            JavaType elemType = (tps == null || tps.length != 1) ? TypeFactory.unknownType() : tps[0];
            CollectionType ct = tf.constructCollectionType(Collection.class, elemType);
            return createCollectionDeserializer(ctxt, ct, beanDesc);
        }
        if (rawType == CLASS_MAP_ENTRY) {
            JavaType kt = type.containedType(0);
            if (kt == null) {
                kt = TypeFactory.unknownType();
            }
            JavaType vt = type.containedType(1);
            if (vt == null) {
                vt = TypeFactory.unknownType();
            }
            TypeDeserializer vts = (TypeDeserializer) vt.getTypeHandler();
            if (vts == null) {
                vts = findTypeDeserializer(ctxt.getConfig(), vt);
            }
            JsonDeserializer<Object> valueDeser = vt.getValueHandler();
            KeyDeserializer keyDes = (KeyDeserializer) kt.getValueHandler();
            return new MapEntryDeserializer(type, keyDes, valueDeser, vts);
        }
        String clsName = rawType.getName();
        if (rawType.isPrimitive() || clsName.startsWith("java.")) {
            JsonDeserializer<?> deser = NumberDeserializers.find(rawType, clsName);
            if (deser == null) {
                deser = DateDeserializers.find(rawType, clsName);
            }
            if (deser != null) {
                return deser;
            }
        }
        if (rawType == TokenBuffer.class) {
            return new TokenBufferDeserializer();
        }
        JsonDeserializer<?> deser = findOptionalStdDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            return deser;
        }
        return JdkDeserializers.find(rawType, clsName);
    }

    protected JavaType _findRemappedType(DeserializationConfig config, Class<?> rawType) throws JsonMappingException {
        JavaType type = mapAbstractType(config, config.constructType(rawType));
        return (type == null || type.hasRawClass(rawType)) ? null : type;
    }

    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findTreeNodeDeserializer(type, config, beanDesc);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomReferenceDeserializer(ReferenceType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findReferenceDeserializer(type, config, beanDesc, contentTypeDeserializer, contentDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected JsonDeserializer<Object> _findCustomBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findBeanDeserializer(type, config, beanDesc);
            if (deser != null) {
                return (JsonDeserializer<Object>) deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findArrayDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findCollectionLikeDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findEnumDeserializer(type, config, beanDesc);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomMapDeserializer(MapType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType type, DeserializationConfig config, BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findMapLikeDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer, elementDeserializer);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef = ctxt.getAnnotationIntrospector().findDeserializer(ann);
        if (deserDef == null) {
            return null;
        }
        return ctxt.deserializerInstance(ann, deserDef);
    }

    protected KeyDeserializer findKeyDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef = ctxt.getAnnotationIntrospector().findKeyDeserializer(ann);
        if (deserDef == null) {
            return null;
        }
        return ctxt.keyDeserializerInstance(ann, deserDef);
    }

    @SuppressWarnings({ "unchecked" })
    protected <T extends JavaType> T modifyTypeByAnnotation(DeserializationContext ctxt, Annotated a, T type) throws JsonMappingException {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            return type;
        }
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            if (keyType != null && keyType.getValueHandler() == null) {
                Object kdDef = intr.findKeyDeserializer(a);
                KeyDeserializer kd = ctxt.keyDeserializerInstance(a, kdDef);
                if (kd != null) {
                    type = (T) ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType();
                }
            }
        }
        JavaType contentType = type.getContentType();
        if (contentType != null) {
            if (contentType.getValueHandler() == null) {
                Object cdDef = intr.findContentDeserializer(a);
                JsonDeserializer<?> cd = ctxt.deserializerInstance(a, cdDef);
                if (cd != null) {
                    type = (T) type.withContentValueHandler(cd);
                }
            }
        }
        type = (T) intr.refineDeserializationType(ctxt.getConfig(), a, type);
        return type;
    }

    protected JavaType resolveType(DeserializationContext ctxt, BeanDescription beanDesc, JavaType type, AnnotatedMember member) throws JsonMappingException {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr == null) {
            return type;
        }
        if (type.isMapLikeType()) {
            JavaType keyType = type.getKeyType();
            if (keyType != null) {
                Object kdDef = intr.findKeyDeserializer(member);
                KeyDeserializer kd = ctxt.keyDeserializerInstance(member, kdDef);
                if (kd != null) {
                    type = ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType();
                }
            }
        }
        if (type.getContentType() != null) {
            Object cdDef = intr.findContentDeserializer(member);
            JsonDeserializer<?> cd = ctxt.deserializerInstance(member, cdDef);
            if (cd != null) {
                type = type.withContentValueHandler(cd);
            }
            if (member instanceof AnnotatedMember) {
                TypeDeserializer contentTypeDeser = findPropertyContentTypeDeserializer(ctxt.getConfig(), type, (AnnotatedMember) member);
                if (contentTypeDeser != null) {
                    type = type.withContentTypeHandler(contentTypeDeser);
                }
            }
        }
        TypeDeserializer valueTypeDeser;
        if (member instanceof AnnotatedMember) {
            valueTypeDeser = findPropertyTypeDeserializer(ctxt.getConfig(), type, (AnnotatedMember) member);
        } else {
            valueTypeDeser = findTypeDeserializer(ctxt.getConfig(), type);
        }
        if (valueTypeDeser != null) {
            type = type.withTypeHandler(valueTypeDeser);
        }
        return type;
    }

    protected EnumResolver constructEnumResolver(Class<?> enumClass, DeserializationConfig config, AnnotatedMethod jsonValueMethod) {
        if (jsonValueMethod != null) {
            Method accessor = jsonValueMethod.getAnnotated();
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(accessor, config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            return EnumResolver.constructUnsafeUsingMethod(enumClass, accessor, config.getAnnotationIntrospector());
        }
        return EnumResolver.constructUnsafe(enumClass, config.getAnnotationIntrospector());
    }

    protected AnnotatedMethod _findJsonValueFor(DeserializationConfig config, JavaType enumType) {
        if (enumType == null) {
            return null;
        }
        BeanDescription beanDesc = config.introspect(enumType);
        return beanDesc.findJsonValueMethod();
    }
}
