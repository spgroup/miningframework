package com.fasterxml.jackson.databind.deser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.NoClass;
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
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;

public abstract class BasicDeserializerFactory extends DeserializerFactory {

    final protected static HashMap<ClassKey, JsonDeserializer<Object>> _simpleDeserializers = new HashMap<ClassKey, JsonDeserializer<Object>>();

    final protected static HashMap<JavaType, JsonDeserializer<Object>> _arrayDeserializers = PrimitiveArrayDeserializers.getAll();

    final protected static HashMap<JavaType, KeyDeserializer> _keyDeserializers = StdKeyDeserializers.constructAll();

    static {
        _add(_simpleDeserializers, Object.class, new UntypedObjectDeserializer());
        StdDeserializer<?> strDeser = new StringDeserializer();
        _add(_simpleDeserializers, String.class, strDeser);
        _add(_simpleDeserializers, CharSequence.class, strDeser);
        _add(_simpleDeserializers, NumberDeserializers.all());
        _add(_simpleDeserializers, DateDeserializers.all());
        _add(_simpleDeserializers, JdkDeserializers.all());
        _add(_simpleDeserializers, JacksonDeserializers.all());
    }

    private static void _add(Map<ClassKey, JsonDeserializer<Object>> desers, StdDeserializer<?>[] serializers) {
        for (StdDeserializer<?> ser : serializers) {
            _add(desers, ser.getValueClass(), ser);
        }
    }

    private static void _add(Map<ClassKey, JsonDeserializer<Object>> desers, Class<?> valueClass, StdDeserializer<?> stdDeser) {
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) stdDeser;
        desers.put(new ClassKey(valueClass), deser);
    }

    @SuppressWarnings("rawtypes")
    final static HashMap<String, Class<? extends Map>> _mapFallbacks = new HashMap<String, Class<? extends Map>>();

    static {
        _mapFallbacks.put(Map.class.getName(), LinkedHashMap.class);
        _mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
        _mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);
        _mapFallbacks.put("java.util.NavigableMap", TreeMap.class);
        try {
            Class<?> key = Class.forName("java.util.ConcurrentNavigableMap");
            Class<?> value = Class.forName("java.util.ConcurrentSkipListMap");
            @SuppressWarnings("unchecked")
            Class<? extends Map<?, ?>> mapValue = (Class<? extends Map<?, ?>>) value;
            _mapFallbacks.put(key.getName(), mapValue);
        } catch (ClassNotFoundException cnfe) {
        }
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

    protected OptionalHandlerFactory optionalHandlers = OptionalHandlerFactory.instance;

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
                    throw new JsonMappingException("Broken registered ValueInstantiators (of type " + insts.getClass().getName() + "): returned null ValueInstantiator");
                }
            }
        }
        return instantiator;
    }

    private ValueInstantiator _findStdValueInstantiator(DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        return JacksonDeserializers.findValueInstantiator(config, beanDesc);
    }

    protected ValueInstantiator _constructDefaultValueInstantiator(DeserializationContext ctxt, BeanDescription beanDesc) throws JsonMappingException {
        boolean fixAccess = ctxt.canOverrideAccessModifiers();
        CreatorCollector creators = new CreatorCollector(beanDesc, fixAccess);
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        final DeserializationConfig config = ctxt.getConfig();
        VisibilityChecker<?> vchecker = config.getDefaultVisibilityChecker();
        vchecker = intr.findAutoDetectVisibility(beanDesc.getClassInfo(), vchecker);
        _addDeserializerFactoryMethods(ctxt, beanDesc, vchecker, intr, creators);
        if (beanDesc.getType().isConcrete()) {
            _addDeserializerConstructors(ctxt, beanDesc, vchecker, intr, creators);
        }
        return creators.constructValueInstantiator(config);
    }

    public ValueInstantiator _valueInstantiatorInstance(DeserializationConfig config, Annotated annotated, Object instDef) throws JsonMappingException {
        if (instDef == null) {
            return null;
        }
        ValueInstantiator inst;
        if (instDef instanceof ValueInstantiator) {
            inst = (ValueInstantiator) instDef;
        } else {
            if (!(instDef instanceof Class)) {
                throw new IllegalStateException("AnnotationIntrospector returned key deserializer definition of type " + instDef.getClass().getName() + "; expected type KeyDeserializer or Class<KeyDeserializer> instead");
            }
            Class<?> instClass = (Class<?>) instDef;
            if (instClass == NoClass.class) {
                return null;
            }
            if (!ValueInstantiator.class.isAssignableFrom(instClass)) {
                throw new IllegalStateException("AnnotationIntrospector returned Class " + instClass.getName() + "; expected Class<ValueInstantiator>");
            }
            HandlerInstantiator hi = config.getHandlerInstantiator();
            if (hi != null) {
                inst = hi.valueInstantiatorInstance(config, annotated, instClass);
            } else {
                inst = (ValueInstantiator) ClassUtil.createInstance(instClass, config.canOverrideAccessModifiers());
            }
        }
        return inst;
    }

    protected void _addDeserializerConstructors(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators) throws JsonMappingException {
        AnnotatedConstructor defaultCtor = beanDesc.findDefaultConstructor();
        if (defaultCtor != null) {
            if (!creators.hasDefaultCreator() || intr.hasCreatorAnnotation(defaultCtor)) {
                creators.setDefaultCreator(defaultCtor);
            }
        }
        for (AnnotatedConstructor ctor : beanDesc.getConstructors()) {
            int argCount = ctor.getParameterCount();
            boolean isCreator = intr.hasCreatorAnnotation(ctor);
            boolean isVisible = vchecker.isCreatorVisible(ctor);
            if (argCount == 1) {
                _handleSingleArgumentConstructor(ctxt, beanDesc, vchecker, intr, creators, ctor, isCreator, isVisible);
                continue;
            }
            if (!isCreator && !isVisible) {
                continue;
            }
            AnnotatedParameter nonAnnotatedParam = null;
            int namedCount = 0;
            int injectCount = 0;
            CreatorProperty[] properties = new CreatorProperty[argCount];
            for (int i = 0; i < argCount; ++i) {
                AnnotatedParameter param = ctor.getParameter(i);
                PropertyName pn = (param == null) ? null : intr.findNameForDeserialization(param);
                String name = (pn == null) ? null : pn.getSimpleName();
                Object injectId = intr.findInjectableValueId(param);
                if (name != null && name.length() > 0) {
                    ++namedCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                } else if (injectId != null) {
                    ++injectCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                } else if (nonAnnotatedParam == null) {
                    nonAnnotatedParam = param;
                }
            }
            if (isCreator || namedCount > 0 || injectCount > 0) {
                if ((namedCount + injectCount) == argCount) {
                    creators.addPropertyCreator(ctor, properties);
                } else if ((namedCount == 0) && ((injectCount + 1) == argCount)) {
                    creators.addDelegatingCreator(ctor, properties);
                } else {
                    throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of constructor " + ctor + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
                }
            }
        }
    }

    protected boolean _handleSingleArgumentConstructor(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, AnnotatedConstructor ctor, boolean isCreator, boolean isVisible) throws JsonMappingException {
        AnnotatedParameter param = ctor.getParameter(0);
        PropertyName pn = (param == null) ? null : intr.findNameForDeserialization(param);
        String name = (pn == null) ? null : pn.getSimpleName();
        Object injectId = intr.findInjectableValueId(param);
        if ((injectId != null) || (name != null && name.length() > 0)) {
            CreatorProperty[] properties = new CreatorProperty[1];
            properties[0] = constructCreatorProperty(ctxt, beanDesc, name, 0, param, injectId);
            creators.addPropertyCreator(ctor, properties);
            return true;
        }
        Class<?> type = ctor.getRawParameterType(0);
        if (type == String.class) {
            if (isCreator || isVisible) {
                creators.addStringCreator(ctor);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || isVisible) {
                creators.addIntCreator(ctor);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || isVisible) {
                creators.addLongCreator(ctor);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || isVisible) {
                creators.addDoubleCreator(ctor);
            }
            return true;
        }
        if (isCreator) {
            creators.addDelegatingCreator(ctor, null);
            return true;
        }
        return false;
    }

    protected void _addDeserializerFactoryMethods(DeserializationContext ctxt, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            boolean isCreator = intr.hasCreatorAnnotation(factory);
            int argCount = factory.getParameterCount();
            if (argCount == 0) {
                if (isCreator) {
                    creators.setDefaultCreator(factory);
                }
                continue;
            }
            if (argCount == 1) {
                AnnotatedParameter param = factory.getParameter(0);
                PropertyName pn = (param == null) ? null : intr.findNameForDeserialization(param);
                String name = (pn == null) ? null : pn.getSimpleName();
                Object injectId = intr.findInjectableValueId(param);
                if ((injectId == null) && (name == null || name.length() == 0)) {
                    _handleSingleArgumentFactory(config, beanDesc, vchecker, intr, creators, factory, isCreator);
                    continue;
                }
            } else {
                if (!intr.hasCreatorAnnotation(factory)) {
                    continue;
                }
            }
            AnnotatedParameter nonAnnotatedParam = null;
            CreatorProperty[] properties = new CreatorProperty[argCount];
            int namedCount = 0;
            int injectCount = 0;
            for (int i = 0; i < argCount; ++i) {
                AnnotatedParameter param = factory.getParameter(i);
                PropertyName pn = (param == null) ? null : intr.findNameForDeserialization(param);
                String name = (pn == null) ? null : pn.getSimpleName();
                Object injectId = intr.findInjectableValueId(param);
                if (name != null && name.length() > 0) {
                    ++namedCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                } else if (injectId != null) {
                    ++injectCount;
                    properties[i] = constructCreatorProperty(ctxt, beanDesc, name, i, param, injectId);
                } else if (nonAnnotatedParam == null) {
                    nonAnnotatedParam = param;
                }
            }
            if (isCreator || namedCount > 0 || injectCount > 0) {
                if ((namedCount + injectCount) == argCount) {
                    creators.addPropertyCreator(factory, properties);
                } else if ((namedCount == 0) && ((injectCount + 1) == argCount)) {
                    creators.addDelegatingCreator(factory, properties);
                } else {
                    throw new IllegalArgumentException("Argument #" + nonAnnotatedParam.getIndex() + " of factory method " + factory + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
                }
            }
        }
    }

    protected boolean _handleSingleArgumentFactory(DeserializationConfig config, BeanDescription beanDesc, VisibilityChecker<?> vchecker, AnnotationIntrospector intr, CreatorCollector creators, AnnotatedMethod factory, boolean isCreator) throws JsonMappingException {
        Class<?> type = factory.getRawParameterType(0);
        if (type == String.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addStringCreator(factory);
            }
            return true;
        }
        if (type == int.class || type == Integer.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addIntCreator(factory);
            }
            return true;
        }
        if (type == long.class || type == Long.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addLongCreator(factory);
            }
            return true;
        }
        if (type == double.class || type == Double.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addDoubleCreator(factory);
            }
            return true;
        }
        if (type == boolean.class || type == Boolean.class) {
            if (isCreator || vchecker.isCreatorVisible(factory)) {
                creators.addBooleanCreator(factory);
            }
            return true;
        }
        if (intr.hasCreatorAnnotation(factory)) {
            creators.addDelegatingCreator(factory, null);
            return true;
        }
        return false;
    }

    protected CreatorProperty constructCreatorProperty(DeserializationContext ctxt, BeanDescription beanDesc, String name, int index, AnnotatedParameter param, Object injectableValueId) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        JavaType t0 = config.getTypeFactory().constructType(param.getParameterType(), beanDesc.bindingsForBeanType());
        BeanProperty.Std property = new BeanProperty.Std(name, t0, beanDesc.getClassAnnotations(), param);
        JavaType type = resolveType(ctxt, beanDesc, t0, param);
        if (type != t0) {
            property = property.withType(type);
        }
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(ctxt, param);
        type = modifyTypeByAnnotation(ctxt, param, type);
        TypeDeserializer typeDeser = (TypeDeserializer) type.getTypeHandler();
        if (typeDeser == null) {
            typeDeser = findTypeDeserializer(config, type);
        }
        CreatorProperty prop = new CreatorProperty(name, type, typeDeser, beanDesc.getClassAnnotations(), param, index, injectableValueId);
        if (deser != null) {
            prop = prop.withValueDeserializer(deser);
        }
        return prop;
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext ctxt, ArrayType type, final BeanDescription beanDesc) throws JsonMappingException {
        JavaType elemType = type.getContentType();
        JsonDeserializer<Object> contentDeser = elemType.getValueHandler();
        if (contentDeser == null) {
            JsonDeserializer<?> deser = _arrayDeserializers.get(elemType);
            if (deser != null) {
                JsonDeserializer<?> custom = _findCustomArrayDeserializer(type, ctxt.getConfig(), beanDesc, null, contentDeser);
                if (custom != null) {
                    return custom;
                }
                return deser;
            }
            if (elemType.isPrimitive()) {
                throw new IllegalArgumentException("Internal error: primitive type (" + type + ") passed, no array deserializer found");
            }
        }
        TypeDeserializer elemTypeDeser = elemType.getTypeHandler();
        if (elemTypeDeser == null) {
            elemTypeDeser = findTypeDeserializer(ctxt.getConfig(), elemType);
        }
        JsonDeserializer<?> custom = _findCustomArrayDeserializer(type, ctxt.getConfig(), beanDesc, elemTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        return new ObjectArrayDeserializer(type, contentDeser, elemTypeDeser);
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

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext ctxt, CollectionType type, BeanDescription beanDesc) throws JsonMappingException {
        JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(ctxt.getConfig(), contentType);
        }
        JsonDeserializer<?> custom = _findCustomCollectionDeserializer(type, ctxt.getConfig(), beanDesc, contentTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        Class<?> collectionClass = type.getRawClass();
        if (contentDeser == null) {
            if (EnumSet.class.isAssignableFrom(collectionClass)) {
                return new EnumSetDeserializer(contentType, null);
            }
        }
        if (type.isInterface() || type.isAbstract()) {
            @SuppressWarnings({ "rawtypes" })
            Class<? extends Collection> fallback = _collectionFallbacks.get(collectionClass.getName());
            if (fallback == null) {
                throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type " + type);
            }
            collectionClass = fallback;
            type = (CollectionType) ctxt.getConfig().constructSpecializedType(type, collectionClass);
            beanDesc = ctxt.getConfig().introspectForCreation(type);
        }
        ValueInstantiator inst = findValueInstantiator(ctxt, beanDesc);
        if (contentType.getRawClass() == String.class) {
            return new StringCollectionDeserializer(type, contentDeser, inst);
        }
        return new CollectionDeserializer(type, contentDeser, contentTypeDeser, inst);
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext ctxt, CollectionLikeType type, final BeanDescription beanDesc) throws JsonMappingException {
        JavaType contentType = type.getContentType();
        JsonDeserializer<Object> contentDeser = contentType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(ctxt.getConfig(), contentType);
        }
        return _findCustomCollectionLikeDeserializer(type, ctxt.getConfig(), beanDesc, contentTypeDeser, contentDeser);
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
        JsonDeserializer<?> custom = _findCustomMapDeserializer(type, config, beanDesc, keyDes, contentTypeDeser, contentDeser);
        if (custom != null) {
            return custom;
        }
        Class<?> mapClass = type.getRawClass();
        if (EnumMap.class.isAssignableFrom(mapClass)) {
            Class<?> kt = keyType.getRawClass();
            if (kt == null || !kt.isEnum()) {
                throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
            }
            return new EnumMapDeserializer(type, null, contentDeser);
        }
        if (type.isInterface() || type.isAbstract()) {
            @SuppressWarnings("rawtypes")
            Class<? extends Map> fallback = _mapFallbacks.get(mapClass.getName());
            if (fallback == null) {
                throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type " + type);
            }
            mapClass = fallback;
            type = (MapType) config.constructSpecializedType(type, mapClass);
            beanDesc = config.introspectForCreation(type);
        }
        ValueInstantiator inst = findValueInstantiator(ctxt, beanDesc);
        MapDeserializer md = new MapDeserializer(type, inst, keyDes, contentDeser, contentTypeDeser);
        md.setIgnorableProperties(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()));
        return md;
    }

    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext ctxt, MapLikeType type, final BeanDescription beanDesc) throws JsonMappingException {
        JavaType keyType = type.getKeyType();
        JavaType contentType = type.getContentType();
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> contentDeser = (JsonDeserializer<Object>) contentType.getValueHandler();
        KeyDeserializer keyDes = (KeyDeserializer) keyType.getValueHandler();
        TypeDeserializer contentTypeDeser = contentType.getTypeHandler();
        if (contentTypeDeser == null) {
            contentTypeDeser = findTypeDeserializer(ctxt.getConfig(), contentType);
        }
        return _findCustomMapLikeDeserializer(type, ctxt.getConfig(), beanDesc, keyDes, contentTypeDeser, contentDeser);
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

    @Override
    public JsonDeserializer<?> createEnumDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        Class<?> enumClass = type.getRawClass();
        JsonDeserializer<?> custom = _findCustomEnumDeserializer(enumClass, ctxt.getConfig(), beanDesc);
        if (custom != null) {
            return custom;
        }
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (ctxt.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    Class<?> returnType = factory.getRawReturnType();
                    if (returnType.isAssignableFrom(enumClass)) {
                        return EnumDeserializer.deserializerForCreator(ctxt.getConfig(), enumClass, factory);
                    }
                }
                throw new IllegalArgumentException("Unsuitable method (" + factory + ") decorated with @JsonCreator (for Enum type " + enumClass.getName() + ")");
            }
        }
        return new EnumDeserializer(constructEnumResolver(enumClass, ctxt.getConfig(), beanDesc.findJsonValueMethod()));
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

    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        for (Deserializers d : _factoryConfig.deserializers()) {
            JsonDeserializer<?> deser = d.findTreeNodeDeserializer(type, config, beanDesc);
            if (deser != null) {
                return deser;
            }
        }
        return null;
    }

    @Override
    public TypeDeserializer findTypeDeserializer(DeserializationConfig config, JavaType baseType) throws JsonMappingException {
        Class<?> cls = baseType.getRawClass();
        BeanDescription bean = config.introspectClassAnnotations(cls);
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
            subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
        }
        if ((b.getDefaultImpl() == null) && baseType.isAbstract()) {
            JavaType defaultType = mapAbstractType(config, baseType);
            if (defaultType != null && defaultType.getRawClass() != baseType.getRawClass()) {
                b = b.defaultImpl(defaultType.getRawClass());
            }
        }
        return b.buildTypeDeserializer(config, baseType, subtypes);
    }

    @Override
    public KeyDeserializer createKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        if (_factoryConfig.hasKeyDeserializers()) {
            BeanDescription beanDesc = config.introspectClassAnnotations(type.getRawClass());
            for (KeyDeserializers d : _factoryConfig.keyDeserializers()) {
                KeyDeserializer deser = d.findKeyDeserializer(type, config, beanDesc);
                if (deser != null) {
                    return deser;
                }
            }
        }
        Class<?> raw = type.getRawClass();
        if (raw == String.class || raw == Object.class) {
            return StdKeyDeserializers.constructStringKeyDeserializer(config, type);
        }
        KeyDeserializer kdes = _keyDeserializers.get(type);
        if (kdes != null) {
            return kdes;
        }
        if (type.isEnumType()) {
            return _createEnumKeyDeserializer(ctxt, type);
        }
        kdes = StdKeyDeserializers.findStringBasedKeyDeserializer(config, type);
        return kdes;
    }

    private KeyDeserializer _createEnumKeyDeserializer(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        BeanDescription beanDesc = config.introspect(type);
        JsonDeserializer<?> des = findDeserializerFromAnnotation(ctxt, beanDesc.getClassInfo());
        if (des != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, des);
        }
        Class<?> enumClass = type.getRawClass();
        JsonDeserializer<?> custom = _findCustomEnumDeserializer(enumClass, config, beanDesc);
        if (custom != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, type, des);
        }
        EnumResolver<?> enumRes = constructEnumResolver(enumClass, config, beanDesc.findJsonValueMethod());
        for (AnnotatedMethod factory : beanDesc.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(factory)) {
                int argCount = factory.getParameterCount();
                if (argCount == 1) {
                    Class<?> returnType = factory.getRawReturnType();
                    if (returnType.isAssignableFrom(enumClass)) {
                        if (factory.getGenericParameterType(0) != String.class) {
                            throw new IllegalArgumentException("Parameter #0 type for factory method (" + factory + ") not suitable, must be java.lang.String");
                        }
                        if (config.canOverrideAccessModifiers()) {
                            ClassUtil.checkAndFixAccess(factory.getMember());
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
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(annotated, config, ai, baseType);
        return b.buildTypeDeserializer(config, baseType, subtypes);
    }

    public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig config, JavaType containerType, AnnotatedMember propertyEntity) throws JsonMappingException {
        AnnotationIntrospector ai = config.getAnnotationIntrospector();
        TypeResolverBuilder<?> b = ai.findPropertyContentTypeResolver(config, propertyEntity, containerType);
        JavaType contentType = containerType.getContentType();
        if (b == null) {
            return findTypeDeserializer(config, contentType);
        }
        Collection<NamedType> subtypes = config.getSubtypeResolver().collectAndResolveSubtypes(propertyEntity, config, ai, contentType);
        return b.buildTypeDeserializer(config, contentType, subtypes);
    }

    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationContext ctxt, Annotated ann) throws JsonMappingException {
        Object deserDef = ctxt.getAnnotationIntrospector().findDeserializer(ann);
        if (deserDef == null) {
            return null;
        }
        return ctxt.deserializerInstance(ann, deserDef);
    }

    @SuppressWarnings({ "unchecked" })
    protected <T extends JavaType> T modifyTypeByAnnotation(DeserializationContext ctxt, Annotated a, T type) throws JsonMappingException {
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        Class<?> subclass = intr.findDeserializationType(a, type);
        if (subclass != null) {
            try {
                type = (T) type.narrowBy(subclass);
            } catch (IllegalArgumentException iae) {
                throw new JsonMappingException("Failed to narrow type " + type + " with concrete-type annotation (value " + subclass.getName() + "), method '" + a.getName() + "': " + iae.getMessage(), null, iae);
            }
        }
        if (type.isContainerType()) {
            Class<?> keyClass = intr.findDeserializationKeyType(a, type.getKeyType());
            if (keyClass != null) {
                if (!(type instanceof MapLikeType)) {
                    throw new JsonMappingException("Illegal key-type annotation: type " + type + " is not a Map(-like) type");
                }
                try {
                    type = (T) ((MapLikeType) type).narrowKey(keyClass);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow key type " + type + " with key-type annotation (" + keyClass.getName() + "): " + iae.getMessage(), null, iae);
                }
            }
            JavaType keyType = type.getKeyType();
            if (keyType != null && keyType.getValueHandler() == null) {
                Object kdDef = intr.findKeyDeserializer(a);
                KeyDeserializer kd = ctxt.keyDeserializerInstance(a, kdDef);
                if (kd != null) {
                    type = (T) ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType();
                }
            }
            Class<?> cc = intr.findDeserializationContentType(a, type.getContentType());
            if (cc != null) {
                try {
                    type = (T) type.narrowContentsBy(cc);
                } catch (IllegalArgumentException iae) {
                    throw new JsonMappingException("Failed to narrow content type " + type + " with content-type annotation (" + cc.getName() + "): " + iae.getMessage(), null, iae);
                }
            }
            JavaType contentType = type.getContentType();
            if (contentType.getValueHandler() == null) {
                Object cdDef = intr.findContentDeserializer(a);
                JsonDeserializer<?> cd = ctxt.deserializerInstance(a, cdDef);
                if (cd != null) {
                    type = (T) type.withContentValueHandler(cd);
                }
            }
        }
        return type;
    }

    protected JavaType resolveType(DeserializationContext ctxt, BeanDescription beanDesc, JavaType type, AnnotatedMember member) throws JsonMappingException {
        if (type.isContainerType()) {
            AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
            JavaType keyType = type.getKeyType();
            if (keyType != null) {
                Object kdDef = intr.findKeyDeserializer(member);
                KeyDeserializer kd = ctxt.keyDeserializerInstance(member, kdDef);
                if (kd != null) {
                    type = ((MapLikeType) type).withKeyValueHandler(kd);
                    keyType = type.getKeyType();
                }
            }
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

    protected EnumResolver<?> constructEnumResolver(Class<?> enumClass, DeserializationConfig config, AnnotatedMethod jsonValueMethod) {
        if (jsonValueMethod != null) {
            Method accessor = jsonValueMethod.getAnnotated();
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(accessor);
            }
            return EnumResolver.constructUnsafeUsingMethod(enumClass, accessor);
        }
        if (config.isEnabled(DeserializationFeature.READ_ENUMS_USING_TO_STRING)) {
            return EnumResolver.constructUnsafeUsingToString(enumClass);
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
