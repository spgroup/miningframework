package com.fasterxml.jackson.databind.deser;

import java.util.*;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.*;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;

public class BeanDeserializerFactory extends BasicDeserializerFactory implements java.io.Serializable {

    private static final long serialVersionUID = 1;

    private final static Class<?>[] INIT_CAUSE_PARAMS = new Class<?>[] { Throwable.class };

    private final static Class<?>[] NO_VIEWS = new Class<?>[0];

    protected final static Set<String> DEFAULT_NO_DESER_CLASS_NAMES;

    static {
        Set<String> s = new HashSet<String>();
        s.add("org.apache.commons.collections.functors.InvokerTransformer");
        s.add("org.apache.commons.collections.functors.InstantiateTransformer");
        s.add("org.apache.commons.collections4.functors.InvokerTransformer");
        s.add("org.apache.commons.collections4.functors.InstantiateTransformer");
        s.add("org.codehaus.groovy.runtime.ConvertedClosure");
        s.add("org.codehaus.groovy.runtime.MethodClosure");
        s.add("org.springframework.beans.factory.ObjectFactory");
        s.add("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet(s);
    }

    protected Set<String> _cfgIllegalClassNames = DEFAULT_NO_DESER_CLASS_NAMES;

    public final static BeanDeserializerFactory instance = new BeanDeserializerFactory(new DeserializerFactoryConfig());

    public BeanDeserializerFactory(DeserializerFactoryConfig config) {
        super(config);
    }

    @Override
    public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }
        if (getClass() != BeanDeserializerFactory.class) {
            throw new IllegalStateException("Subtype of BeanDeserializerFactory (" + getClass().getName() + ") has not properly overridden method 'withAdditionalDeserializers': can not instantiate subtype with " + "additional deserializer definitions");
        }
        return new BeanDeserializerFactory(config);
    }

    @Override
    public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        JsonDeserializer<Object> custom = _findCustomBeanDeserializer(type, config, beanDesc);
        if (custom != null) {
            return custom;
        }
        if (type.isThrowable()) {
            return buildThrowableDeserializer(ctxt, type, beanDesc);
        }
        if (type.isAbstract() && !type.isPrimitive()) {
            JavaType concreteType = materializeAbstractType(ctxt, type, beanDesc);
            if (concreteType != null) {
                beanDesc = config.introspect(concreteType);
                return buildBeanDeserializer(ctxt, concreteType, beanDesc);
            }
        }
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) findStdDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            return deser;
        }
        if (!isPotentialBeanType(type.getRawClass())) {
            return null;
        }
        checkIllegalTypes(ctxt, type, beanDesc);
        return buildBeanDeserializer(ctxt, type, beanDesc);
    }

    @Override
    public JsonDeserializer<Object> createBuilderBasedDeserializer(DeserializationContext ctxt, JavaType valueType, BeanDescription beanDesc, Class<?> builderClass) throws JsonMappingException {
        JavaType builderType = ctxt.constructType(builderClass);
        BeanDescription builderDesc = ctxt.getConfig().introspectForBuilder(builderType);
        return buildBuilderBasedDeserializer(ctxt, valueType, builderDesc);
    }

    protected JsonDeserializer<?> findStdDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt, type, beanDesc);
        if (deser != null) {
            if (_factoryConfig.hasDeserializerModifiers()) {
                for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                    deser = mod.modifyDeserializer(ctxt.getConfig(), beanDesc, deser);
                }
            }
        }
        return deser;
    }

    protected JavaType materializeAbstractType(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        for (AbstractTypeResolver r : _factoryConfig.abstractTypeResolvers()) {
            JavaType concrete = r.resolveAbstractType(ctxt.getConfig(), beanDesc);
            if (concrete != null) {
                return concrete;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public JsonDeserializer<Object> buildBeanDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        ValueInstantiator valueInstantiator;
        try {
            valueInstantiator = findValueInstantiator(ctxt, beanDesc);
        } catch (NoClassDefFoundError error) {
            return new NoClassDefFoundDeserializer<Object>(error);
        }
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, beanDesc);
        builder.setValueInstantiator(valueInstantiator);
        addBeanProps(ctxt, beanDesc, builder);
        addObjectIdReader(ctxt, beanDesc, builder);
        addReferenceProperties(ctxt, beanDesc, builder);
        addInjectables(ctxt, beanDesc, builder);
        final DeserializationConfig config = ctxt.getConfig();
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer;
        if (type.isAbstract() && !valueInstantiator.canInstantiate()) {
            deserializer = builder.buildAbstract();
        } else {
            deserializer = builder.build();
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    @SuppressWarnings("unchecked")
    protected JsonDeserializer<Object> buildBuilderBasedDeserializer(DeserializationContext ctxt, JavaType valueType, BeanDescription builderDesc) throws JsonMappingException {
        ValueInstantiator valueInstantiator = findValueInstantiator(ctxt, builderDesc);
        final DeserializationConfig config = ctxt.getConfig();
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, builderDesc);
        builder.setValueInstantiator(valueInstantiator);
        addBeanProps(ctxt, builderDesc, builder);
        addObjectIdReader(ctxt, builderDesc, builder);
        addReferenceProperties(ctxt, builderDesc, builder);
        addInjectables(ctxt, builderDesc, builder);
        JsonPOJOBuilder.Value builderConfig = builderDesc.findPOJOBuilderConfig();
        final String buildMethodName = (builderConfig == null) ? "build" : builderConfig.buildMethodName;
        AnnotatedMethod buildMethod = builderDesc.findMethod(buildMethodName, null);
        if (buildMethod != null) {
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(buildMethod.getMember(), config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        builder.setPOJOBuilder(buildMethod, builderConfig);
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, builderDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.buildBuilderBased(valueType, buildMethodName);
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, builderDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    protected void addObjectIdReader(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        ObjectIdInfo objectIdInfo = beanDesc.getObjectIdInfo();
        if (objectIdInfo == null) {
            return;
        }
        Class<?> implClass = objectIdInfo.getGeneratorType();
        JavaType idType;
        SettableBeanProperty idProp;
        ObjectIdGenerator<?> gen;
        ObjectIdResolver resolver = ctxt.objectIdResolverInstance(beanDesc.getClassInfo(), objectIdInfo);
        if (implClass == ObjectIdGenerators.PropertyGenerator.class) {
            PropertyName propName = objectIdInfo.getPropertyName();
            idProp = builder.findProperty(propName);
            if (idProp == null) {
                throw new IllegalArgumentException("Invalid Object Id definition for " + beanDesc.getBeanClass().getName() + ": can not find property with name '" + propName + "'");
            }
            idType = idProp.getType();
            gen = new PropertyBasedObjectIdGenerator(objectIdInfo.getScope());
        } else {
            JavaType type = ctxt.constructType(implClass);
            idType = ctxt.getTypeFactory().findTypeParameters(type, ObjectIdGenerator.class)[0];
            idProp = null;
            gen = ctxt.objectIdGeneratorInstance(beanDesc.getClassInfo(), objectIdInfo);
        }
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(idType);
        builder.setObjectIdReader(ObjectIdReader.construct(idType, objectIdInfo.getPropertyName(), gen, deser, idProp, resolver));
    }

    @SuppressWarnings("unchecked")
    public JsonDeserializer<Object> buildThrowableDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        final DeserializationConfig config = ctxt.getConfig();
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, beanDesc);
        builder.setValueInstantiator(findValueInstantiator(ctxt, beanDesc));
        addBeanProps(ctxt, beanDesc, builder);
        AnnotatedMethod am = beanDesc.findMethod("initCause", INIT_CAUSE_PARAMS);
        if (am != null) {
            SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(ctxt.getConfig(), am, new PropertyName("cause"));
            SettableBeanProperty prop = constructSettableProperty(ctxt, beanDesc, propDef, am.getParameterType(0));
            if (prop != null) {
                builder.addOrReplaceProperty(prop, true);
            }
        }
        builder.addIgnorable("localizedMessage");
        builder.addIgnorable("suppressed");
        builder.addIgnorable("message");
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                builder = mod.updateBuilder(config, beanDesc, builder);
            }
        }
        JsonDeserializer<?> deserializer = builder.build();
        if (deserializer instanceof BeanDeserializer) {
            deserializer = new ThrowableDeserializer((BeanDeserializer) deserializer);
        }
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                deserializer = mod.modifyDeserializer(config, beanDesc, deserializer);
            }
        }
        return (JsonDeserializer<Object>) deserializer;
    }

    protected BeanDeserializerBuilder constructBeanDeserializerBuilder(DeserializationContext ctxt, BeanDescription beanDesc) {
        return new BeanDeserializerBuilder(beanDesc, ctxt.getConfig());
    }

    protected void addBeanProps(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        final SettableBeanProperty[] creatorProps = builder.getValueInstantiator().getFromObjectArguments(ctxt.getConfig());
        final boolean isConcrete = !beanDesc.getType().isAbstract();
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        if (intr != null) {
            Boolean B = intr.findIgnoreUnknownProperties(beanDesc.getClassInfo());
            if (B != null) {
                builder.setIgnoreUnknownProperties(B.booleanValue());
            }
        }
        Set<String> ignored = ArrayBuilders.arrayToSet(intr.findPropertiesToIgnore(beanDesc.getClassInfo(), false));
        for (String propName : ignored) {
            builder.addIgnorable(propName);
        }
        AnnotatedMethod anySetter = beanDesc.findAnySetter();
        if (anySetter != null) {
            builder.setAnySetter(constructAnySetter(ctxt, beanDesc, anySetter));
        }
        if (anySetter == null) {
            Collection<String> ignored2 = beanDesc.getIgnoredPropertyNames();
            if (ignored2 != null) {
                for (String propName : ignored2) {
                    builder.addIgnorable(propName);
                }
            }
        }
        final boolean useGettersAsSetters = (ctxt.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS) && ctxt.isEnabled(MapperFeature.AUTO_DETECT_GETTERS));
        List<BeanPropertyDefinition> propDefs = filterBeanProps(ctxt, beanDesc, builder, beanDesc.findProperties(), ignored);
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                propDefs = mod.updateProperties(ctxt.getConfig(), beanDesc, propDefs);
            }
        }
        for (BeanPropertyDefinition propDef : propDefs) {
            SettableBeanProperty prop = null;
            if (propDef.hasSetter()) {
                JavaType propertyType = propDef.getSetter().getParameterType(0);
                prop = constructSettableProperty(ctxt, beanDesc, propDef, propertyType);
            } else if (propDef.hasField()) {
                JavaType propertyType = propDef.getField().getType();
                prop = constructSettableProperty(ctxt, beanDesc, propDef, propertyType);
            } else if (useGettersAsSetters && propDef.hasGetter()) {
                AnnotatedMethod getter = propDef.getGetter();
                Class<?> rawPropertyType = getter.getRawType();
                if (Collection.class.isAssignableFrom(rawPropertyType) || Map.class.isAssignableFrom(rawPropertyType)) {
                    prop = constructSetterlessProperty(ctxt, beanDesc, propDef);
                }
            }
            if (isConcrete && propDef.hasConstructorParameter()) {
                final String name = propDef.getName();
                CreatorProperty cprop = null;
                if (creatorProps != null) {
                    for (SettableBeanProperty cp : creatorProps) {
                        if (name.equals(cp.getName()) && (cp instanceof CreatorProperty)) {
                            cprop = (CreatorProperty) cp;
                            break;
                        }
                    }
                }
                if (cprop == null) {
                    throw ctxt.mappingException("Could not find creator property with name '%s' (in class %s)", name, beanDesc.getBeanClass().getName());
                }
                if (prop != null) {
                    cprop.setFallbackSetter(prop);
                }
                prop = cprop;
                builder.addCreatorProperty(cprop);
                continue;
            }
            if (prop != null) {
                Class<?>[] views = propDef.findViews();
                if (views == null) {
                    if (!ctxt.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION)) {
                        views = NO_VIEWS;
                    }
                }
                prop.setViews(views);
                builder.addProperty(prop);
            }
        }
    }

    protected List<BeanPropertyDefinition> filterBeanProps(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder, List<BeanPropertyDefinition> propDefsIn, Set<String> ignored) throws JsonMappingException {
        ArrayList<BeanPropertyDefinition> result = new ArrayList<BeanPropertyDefinition>(Math.max(4, propDefsIn.size()));
        HashMap<Class<?>, Boolean> ignoredTypes = new HashMap<Class<?>, Boolean>();
        for (BeanPropertyDefinition property : propDefsIn) {
            String name = property.getName();
            if (ignored.contains(name)) {
                continue;
            }
            if (!property.hasConstructorParameter()) {
                Class<?> rawPropertyType = null;
                if (property.hasSetter()) {
                    rawPropertyType = property.getSetter().getRawParameterType(0);
                } else if (property.hasField()) {
                    rawPropertyType = property.getField().getRawType();
                }
                if ((rawPropertyType != null) && (isIgnorableType(ctxt.getConfig(), beanDesc, rawPropertyType, ignoredTypes))) {
                    builder.addIgnorable(name);
                    continue;
                }
            }
            result.add(property);
        }
        return result;
    }

    protected void addReferenceProperties(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        Map<String, AnnotatedMember> refs = beanDesc.findBackReferenceProperties();
        if (refs != null) {
            for (Map.Entry<String, AnnotatedMember> en : refs.entrySet()) {
                String name = en.getKey();
                AnnotatedMember m = en.getValue();
                JavaType type;
                if (m instanceof AnnotatedMethod) {
                    type = ((AnnotatedMethod) m).getParameterType(0);
                } else {
                    type = m.getType();
                }
                SimpleBeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(ctxt.getConfig(), m);
                builder.addBackReferenceProperty(name, constructSettableProperty(ctxt, beanDesc, propDef, type));
            }
        }
    }

    protected void addInjectables(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        Map<Object, AnnotatedMember> raw = beanDesc.findInjectables();
        if (raw != null) {
            boolean fixAccess = ctxt.canOverrideAccessModifiers();
            boolean forceAccess = fixAccess && ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
            for (Map.Entry<Object, AnnotatedMember> entry : raw.entrySet()) {
                AnnotatedMember m = entry.getValue();
                if (fixAccess) {
                    m.fixAccess(forceAccess);
                }
                builder.addInjectable(PropertyName.construct(m.getName()), m.getType(), beanDesc.getClassAnnotations(), m, entry.getKey());
            }
        }
    }

    protected SettableAnyProperty constructAnySetter(DeserializationContext ctxt, BeanDescription beanDesc, AnnotatedMethod setter) throws JsonMappingException {
        if (ctxt.canOverrideAccessModifiers()) {
            setter.fixAccess(ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
        }
        JavaType type = setter.getParameterType(1);
        BeanProperty.Std property = new BeanProperty.Std(PropertyName.construct(setter.getName()), type, null, beanDesc.getClassAnnotations(), setter, PropertyMetadata.STD_OPTIONAL);
        type = resolveType(ctxt, beanDesc, type, setter);
        JsonDeserializer<Object> deser = findDeserializerFromAnnotation(ctxt, setter);
        type = modifyTypeByAnnotation(ctxt, setter, type);
        if (deser == null) {
            deser = type.getValueHandler();
        }
        TypeDeserializer typeDeser = type.getTypeHandler();
        return new SettableAnyProperty(property, setter, type, deser, typeDeser);
    }

    protected SettableBeanProperty constructSettableProperty(DeserializationContext ctxt, BeanDescription beanDesc, BeanPropertyDefinition propDef, JavaType propType0) throws JsonMappingException {
        AnnotatedMember mutator = propDef.getNonConstructorMutator();
        if (ctxt.canOverrideAccessModifiers()) {
            if ((mutator instanceof AnnotatedField) && "cause".equals(mutator.getName()) && Throwable.class.isAssignableFrom(propType0.getRawClass())) {
                ;
            } else {
                mutator.fixAccess(ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
        }
        BeanProperty.Std property = new BeanProperty.Std(propDef.getFullName(), propType0, propDef.getWrapperName(), beanDesc.getClassAnnotations(), mutator, propDef.getMetadata());
        JavaType type = resolveType(ctxt, beanDesc, propType0, mutator);
        if (type != propType0) {
            property = property.withType(type);
        }
        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(ctxt, mutator);
        type = modifyTypeByAnnotation(ctxt, mutator, type);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop;
        if (mutator instanceof AnnotatedMethod) {
            prop = new MethodProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), (AnnotatedMethod) mutator);
        } else {
            prop = new FieldProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), (AnnotatedField) mutator);
        }
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        AnnotationIntrospector.ReferenceProperty ref = propDef.findReferenceType();
        if (ref != null && ref.isManagedReference()) {
            prop.setManagedReferenceName(ref.getName());
        }
        ObjectIdInfo objectIdInfo = propDef.findObjectIdInfo();
        if (objectIdInfo != null) {
            prop.setObjectIdInfo(objectIdInfo);
        }
        return prop;
    }

    protected SettableBeanProperty constructSetterlessProperty(DeserializationContext ctxt, BeanDescription beanDesc, BeanPropertyDefinition propDef) throws JsonMappingException {
        final AnnotatedMethod getter = propDef.getGetter();
        if (ctxt.canOverrideAccessModifiers()) {
            getter.fixAccess(ctxt.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
        }
        JavaType type = getter.getType();
        JsonDeserializer<Object> propDeser = findDeserializerFromAnnotation(ctxt, getter);
        type = modifyTypeByAnnotation(ctxt, getter, type);
        type = resolveType(ctxt, beanDesc, type, getter);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SetterlessProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), getter);
        if (propDeser != null) {
            prop = prop.withValueDeserializer(propDeser);
        }
        return prop;
    }

    protected boolean isPotentialBeanType(Class<?> type) {
        String typeStr = ClassUtil.canBeABeanType(type);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
        }
        if (ClassUtil.isProxyType(type)) {
            throw new IllegalArgumentException("Can not deserialize Proxy class " + type.getName() + " as a Bean");
        }
        typeStr = ClassUtil.isLocalType(type, true);
        if (typeStr != null) {
            throw new IllegalArgumentException("Can not deserialize Class " + type.getName() + " (of type " + typeStr + ") as a Bean");
        }
        return true;
    }

    protected boolean isIgnorableType(DeserializationConfig config, BeanDescription beanDesc, Class<?> type, Map<Class<?>, Boolean> ignoredTypes) {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        BeanDescription desc = config.introspectClassAnnotations(type);
        status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
        return (status == null) ? false : status.booleanValue();
    }

    protected void checkIllegalTypes(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        String full = type.getRawClass().getName();
        if (_cfgIllegalClassNames.contains(full)) {
            throw JsonMappingException.from(ctxt, String.format("Illegal type (%s) to deserialize: prevented for security reasons", full));
        }
    }
}
