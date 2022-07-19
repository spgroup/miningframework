package com.fasterxml.jackson.databind.deser;

import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.*;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.SubTypeValidator;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;

public class BeanDeserializerFactory extends BasicDeserializerFactory implements java.io.Serializable {

    private static final long serialVersionUID = 1;

    private final static Class<?>[] INIT_CAUSE_PARAMS = new Class<?>[] { Throwable.class };

    static {
        Set<String> s = new HashSet<>();
        s.add("org.apache.commons.collections.functors.InvokerTransformer");
        s.add("org.apache.commons.collections.functors.InstantiateTransformer");
        s.add("org.apache.commons.collections4.functors.InvokerTransformer");
        s.add("org.apache.commons.collections4.functors.InstantiateTransformer");
        s.add("org.codehaus.groovy.runtime.ConvertedClosure");
        s.add("org.codehaus.groovy.runtime.MethodClosure");
        s.add("org.springframework.beans.factory.ObjectFactory");
        s.add("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
        s.add("org.apache.xalan.xsltc.trax.TemplatesImpl");
        s.add("com.sun.rowset.JdbcRowSetImpl");
        s.add("java.util.logging.FileHandler");
        s.add("java.rmi.server.UnicastRemoteObject");
        s.add("org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor");
        s.add("org.springframework.beans.factory.config.PropertyPathFactoryBean");
        s.add("com.mchange.v2.c3p0.JndiRefForwardingDataSource");
        s.add("com.mchange.v2.c3p0.WrapperConnectionPoolDataSource");
        s.add("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
        s.add("com.sun.org.apache.bcel.internal.util.ClassLoader");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet(s);
    }

    public final static BeanDeserializerFactory instance = new BeanDeserializerFactory(new DeserializerFactoryConfig());

    public BeanDeserializerFactory(DeserializerFactoryConfig config) {
        super(config);
    }

    @Override
    public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
        if (_factoryConfig == config) {
            return this;
        }
        ClassUtil.verifyMustOverride(BeanDeserializerFactory.class, this, "withConfig");
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
        if (type.isAbstract() && !type.isPrimitive() && !type.isEnumType()) {
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
        _validateSubType(ctxt, type, beanDesc);
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
            return new ErrorThrowingDeserializer(error);
        } catch (IllegalArgumentException e) {
            throw InvalidDefinitionException.from(ctxt.getParser(), e.getMessage(), beanDesc, null);
        }
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, beanDesc);
        builder.setValueInstantiator(valueInstantiator);
        addBeanProps(ctxt, beanDesc, builder);
        addObjectIdReader(ctxt, beanDesc, builder);
        addBackReferenceProperties(ctxt, beanDesc, builder);
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
        ValueInstantiator valueInstantiator;
        try {
            valueInstantiator = findValueInstantiator(ctxt, builderDesc);
        } catch (NoClassDefFoundError error) {
            return new ErrorThrowingDeserializer(error);
        } catch (IllegalArgumentException e) {
            throw InvalidDefinitionException.from(ctxt.getParser(), e.getMessage(), builderDesc, null);
        }
        final DeserializationConfig config = ctxt.getConfig();
        BeanDeserializerBuilder builder = constructBeanDeserializerBuilder(ctxt, builderDesc);
        builder.setValueInstantiator(valueInstantiator);
        addBeanProps(ctxt, builderDesc, builder);
        addObjectIdReader(ctxt, builderDesc, builder);
        addBackReferenceProperties(ctxt, builderDesc, builder);
        addInjectables(ctxt, builderDesc, builder);
        JsonPOJOBuilder.Value builderConfig = builderDesc.findPOJOBuilderConfig();
        final String buildMethodName = (builderConfig == null) ? JsonPOJOBuilder.DEFAULT_BUILD_METHOD : builderConfig.buildMethodName;
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
        return new BeanDeserializerBuilder(beanDesc, ctxt);
    }

    protected void addBeanProps(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        final boolean isConcrete = !beanDesc.getType().isAbstract();
        final SettableBeanProperty[] creatorProps = isConcrete ? builder.getValueInstantiator().getFromObjectArguments(ctxt.getConfig()) : null;
        final boolean hasCreatorProps = (creatorProps != null);
        JsonIgnoreProperties.Value ignorals = ctxt.getConfig().getDefaultPropertyIgnorals(beanDesc.getBeanClass(), beanDesc.getClassInfo());
        Set<String> ignored;
        if (ignorals != null) {
            boolean ignoreAny = ignorals.getIgnoreUnknown();
            builder.setIgnoreUnknownProperties(ignoreAny);
            ignored = ignorals.findIgnoredForDeserialization();
            for (String propName : ignored) {
                builder.addIgnorable(propName);
            }
        } else {
            ignored = Collections.emptySet();
        }
        AnnotatedMember anySetter = beanDesc.findAnySetterAccessor();
        if (anySetter != null) {
            builder.setAnySetter(constructAnySetter(ctxt, beanDesc, anySetter));
        } else {
            Collection<String> ignored2 = beanDesc.getIgnoredPropertyNames();
            if (ignored2 != null) {
                for (String propName : ignored2) {
                    builder.addIgnorable(propName);
                }
            }
        }
        final boolean useGettersAsSetters = ctxt.isEnabled(MapperFeature.USE_GETTERS_AS_SETTERS) && ctxt.isEnabled(MapperFeature.AUTO_DETECT_GETTERS);
        List<BeanPropertyDefinition> propDefs = filterBeanProps(ctxt, beanDesc, builder, beanDesc.findProperties(), ignored);
        if (_factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier mod : _factoryConfig.deserializerModifiers()) {
                propDefs = mod.updateProperties(ctxt.getConfig(), beanDesc, propDefs);
            }
        }
        for (BeanPropertyDefinition propDef : propDefs) {
            SettableBeanProperty prop = null;
            if (propDef.hasSetter()) {
                AnnotatedMethod setter = propDef.getSetter();
                JavaType propertyType = setter.getParameterType(0);
                prop = constructSettableProperty(ctxt, beanDesc, propDef, propertyType);
            } else if (propDef.hasField()) {
                AnnotatedField field = propDef.getField();
                JavaType propertyType = field.getType();
                prop = constructSettableProperty(ctxt, beanDesc, propDef, propertyType);
            } else {
                AnnotatedMethod getter = propDef.getGetter();
                if (getter != null) {
                    if (useGettersAsSetters && _isSetterlessType(getter.getRawType())) {
                    prop = constructSetterlessProperty(ctxt, beanDesc, propDef);
                    } else if (!propDef.hasConstructorParameter()) {
                        PropertyMetadata md = propDef.getMetadata();
                        if (md.getMergeInfo() != null) {
                            prop = constructSetterlessProperty(ctxt, beanDesc, propDef);
                        }
                    }
                }
            }
            if (hasCreatorProps && propDef.hasConstructorParameter()) {
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
                    List<String> n = new ArrayList<>();
                    for (SettableBeanProperty cp : creatorProps) {
                        n.add(cp.getName());
                    }
                    ctxt.reportBadPropertyDefinition(beanDesc, propDef, "Could not find creator property with name '%s' (known Creator properties: %s)", name, n);
                    continue;
                }
                if (prop != null) {
                    cprop.setFallbackSetter(prop);
                }
                Class<?>[] views = propDef.findViews();
                if (views == null) {
                    views = beanDesc.findDefaultViews();
                }
                cprop.setViews(views);
                builder.addCreatorProperty(cprop);
                continue;
            }
            if (prop != null) {
                Class<?>[] views = propDef.findViews();
                if (views == null) {
                    views = beanDesc.findDefaultViews();
                }
                prop.setViews(views);
                builder.addProperty(prop);
            }
        }
    }

    private boolean _isSetterlessType(Class<?> rawType) {
        return Collection.class.isAssignableFrom(rawType) || Map.class.isAssignableFrom(rawType);
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
                Class<?> rawPropertyType = property.getRawPrimaryType();
                if ((rawPropertyType != null) && isIgnorableType(ctxt.getConfig(), property, rawPropertyType, ignoredTypes)) {
                    builder.addIgnorable(name);
                    continue;
                }
            }
            result.add(property);
        }
        return result;
    }

    protected void addBackReferenceProperties(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        List<BeanPropertyDefinition> refProps = beanDesc.findBackReferences();
        if (refProps != null) {
            for (BeanPropertyDefinition refProp : refProps) {
                String refName = refProp.findReferenceName();
                builder.addBackReferenceProperty(refName, constructSettableProperty(ctxt, beanDesc, refProp, refProp.getPrimaryType()));
            }
        }
    }

    @Deprecated
protected void addReferenceProperties(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        addBackReferenceProperties(ctxt, beanDesc, builder);
    }

    protected void addInjectables(DeserializationContext ctxt, BeanDescription beanDesc, BeanDeserializerBuilder builder) throws JsonMappingException {
        Map<Object, AnnotatedMember> raw = beanDesc.findInjectables();
        if (raw != null) {
            for (Map.Entry<Object, AnnotatedMember> entry : raw.entrySet()) {
                AnnotatedMember m = entry.getValue();
                builder.addInjectable(PropertyName.construct(m.getName()), m.getType(), beanDesc.getClassAnnotations(), m, entry.getKey());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected SettableAnyProperty constructAnySetter(DeserializationContext ctxt, BeanDescription beanDesc, AnnotatedMember mutator) throws JsonMappingException {
        BeanProperty prop;
        JavaType keyType;
        JavaType valueType;
        if (mutator instanceof AnnotatedMethod) {
            AnnotatedMethod am = (AnnotatedMethod) mutator;
            keyType = am.getParameterType(0);
            valueType = am.getParameterType(1);
            valueType = resolveMemberAndTypeAnnotations(ctxt, mutator, valueType);
            prop = new BeanProperty.Std(PropertyName.construct(mutator.getName()), valueType, null, mutator, PropertyMetadata.STD_OPTIONAL);
        } else if (mutator instanceof AnnotatedField) {
            AnnotatedField af = (AnnotatedField) mutator;
            JavaType mapType = af.getType();
            mapType = resolveMemberAndTypeAnnotations(ctxt, mutator, mapType);
            keyType = mapType.getKeyType();
            valueType = mapType.getContentType();
            prop = new BeanProperty.Std(PropertyName.construct(mutator.getName()), mapType, null, mutator, PropertyMetadata.STD_OPTIONAL);
        } else {
            return ctxt.reportBadDefinition(beanDesc.getType(), String.format("Unrecognized mutator type for any setter: %s", mutator.getClass()));
        }
        KeyDeserializer keyDeser = findKeyDeserializerFromAnnotation(ctxt, mutator);
        if (keyDeser == null) {
            keyDeser = keyType.getValueHandler();
        }
        if (keyDeser == null) {
            keyDeser = ctxt.findKeyDeserializer(keyType, prop);
        } else {
            if (keyDeser instanceof ContextualKeyDeserializer) {
                keyDeser = ((ContextualKeyDeserializer) keyDeser).createContextual(ctxt, prop);
            }
        }
        JsonDeserializer<Object> deser = findContentDeserializerFromAnnotation(ctxt, mutator);
        if (deser == null) {
            deser = valueType.getValueHandler();
        }
        if (deser != null) {
            deser = (JsonDeserializer<Object>) ctxt.handlePrimaryContextualization(deser, prop, valueType);
        }
        TypeDeserializer typeDeser = valueType.getTypeHandler();
        return new SettableAnyProperty(prop, mutator, valueType, keyDeser, deser, typeDeser);
    }

    protected SettableBeanProperty constructSettableProperty(DeserializationContext ctxt, BeanDescription beanDesc, BeanPropertyDefinition propDef, JavaType propType0) throws JsonMappingException {
        AnnotatedMember mutator = propDef.getNonConstructorMutator();
        if (mutator == null) {
            ctxt.reportBadPropertyDefinition(beanDesc, propDef, "No non-constructor mutator available");
        }
        JavaType type = resolveMemberAndTypeAnnotations(ctxt, mutator, propType0);
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop;
        if (mutator instanceof AnnotatedMethod) {
            prop = new MethodProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), (AnnotatedMethod) mutator);
        } else {
            prop = new FieldProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), (AnnotatedField) mutator);
        }
        JsonDeserializer<?> deser = findDeserializerFromAnnotation(ctxt, mutator);
        if (deser == null) {
            deser = type.getValueHandler();
        }
        if (deser != null) {
            deser = ctxt.handlePrimaryContextualization(deser, prop, type);
            prop = prop.withValueDeserializer(deser);
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
        JavaType type = resolveMemberAndTypeAnnotations(ctxt, getter, getter.getType());
        TypeDeserializer typeDeser = type.getTypeHandler();
        SettableBeanProperty prop = new SetterlessProperty(propDef, type, typeDeser, beanDesc.getClassAnnotations(), getter);
        JsonDeserializer<?> deser = findDeserializerFromAnnotation(ctxt, getter);
        if (deser == null) {
            deser = type.getValueHandler();
        }
        if (deser != null) {
            deser = ctxt.handlePrimaryContextualization(deser, prop, type);
            prop = prop.withValueDeserializer(deser);
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

    protected boolean isIgnorableType(DeserializationConfig config, BeanPropertyDefinition propDef, Class<?> type, Map<Class<?>, Boolean> ignoredTypes) {
        Boolean status = ignoredTypes.get(type);
        if (status != null) {
            return status.booleanValue();
        }
        if ((type == String.class) || type.isPrimitive()) {
            status = Boolean.FALSE;
        } else {
            status = config.getConfigOverride(type).getIsIgnoredType();
        if (status == null) {
            BeanDescription desc = config.introspectClassAnnotations(type);
            status = config.getAnnotationIntrospector().isIgnorableType(desc.getClassInfo());
            if (status == null) {
                status = Boolean.FALSE;
            }
        }
        }
        ignoredTypes.put(type, status);
        return status.booleanValue();
    }

    protected void _validateSubType(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        SubTypeValidator.instance().validateSubType(ctxt, type);
    }
}