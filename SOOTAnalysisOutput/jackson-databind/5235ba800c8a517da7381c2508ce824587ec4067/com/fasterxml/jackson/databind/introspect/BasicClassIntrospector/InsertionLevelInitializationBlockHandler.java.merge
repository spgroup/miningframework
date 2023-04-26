package com.fasterxml.jackson.databind.introspect;

import java.util.Collection;
import java.util.Map;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.LRUMap;

public class BasicClassIntrospector extends ClassIntrospector implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    protected final static BasicBeanDescription STRING_DESC;

    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, null);
        STRING_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(String.class), ac);
    }

    protected final static BasicBeanDescription BOOLEAN_DESC;

    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Boolean.TYPE, null);
        BOOLEAN_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Boolean.TYPE), ac);
    }

    protected final static BasicBeanDescription INT_DESC;

    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Integer.TYPE, null);
        INT_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Integer.TYPE), ac);
    }

    protected final static BasicBeanDescription LONG_DESC;

    static {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Long.TYPE, null);
        LONG_DESC = BasicBeanDescription.forOtherUse(null, SimpleType.constructUnsafe(Long.TYPE), ac);
    }

    @Deprecated
    public final static BasicClassIntrospector instance = new BasicClassIntrospector();

    protected final LRUMap<JavaType, BasicBeanDescription> _cachedFCA;

    public BasicClassIntrospector() {
        _cachedFCA = new LRUMap<JavaType, BasicBeanDescription>(16, 64);
    }

    @Override
    public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = _findStdTypeDesc(type);
        if (desc == null) {
            desc = _findStdJdkCollectionDesc(cfg, type);
            if (desc == null) {
                desc = BasicBeanDescription.forSerialization(collectProperties(cfg, type, r, true, "set"));
            }
            _cachedFCA.putIfAbsent(type, desc);
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = _findStdTypeDesc(type);
        if (desc == null) {
            desc = _findStdJdkCollectionDesc(cfg, type);
            if (desc == null) {
                desc = BasicBeanDescription.forDeserialization(collectProperties(cfg, type, r, false, "set"));
            }
            _cachedFCA.putIfAbsent(type, desc);
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig cfg, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = BasicBeanDescription.forDeserialization(collectPropertiesWithBuilder(cfg, type, r, false));
        _cachedFCA.putIfAbsent(type, desc);
        return desc;
    }

    @Override
    public BasicBeanDescription forCreation(DeserializationConfig cfg, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = _findStdTypeDesc(type);
        if (desc == null) {
            desc = _findStdJdkCollectionDesc(cfg, type);
            if (desc == null) {
                desc = BasicBeanDescription.forDeserialization(collectProperties(cfg, type, r, false, "set"));
            }
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forClassAnnotations(MapperConfig<?> config, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = _findStdTypeDesc(type);
        if (desc == null) {
            desc = _cachedFCA.get(type);
            if (desc == null) {
                AnnotatedClass ac = AnnotatedClass.construct(type, config, r);
                desc = BasicBeanDescription.forOtherUse(config, type, ac);
                _cachedFCA.put(type, desc);
            }
        }
        return desc;
    }

    @Override
    public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> config, JavaType type, MixInResolver r) {
        BasicBeanDescription desc = _findStdTypeDesc(type);
        if (desc == null) {
            AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(type.getRawClass(), config, r);
            desc = BasicBeanDescription.forOtherUse(config, type, ac);
        }
        return desc;
    }

    protected POJOPropertiesCollector collectProperties(MapperConfig<?> config, JavaType type, MixInResolver r, boolean forSerialization, String mutatorPrefix) {
        AnnotatedClass ac = AnnotatedClass.construct(type, config, r);
        return constructPropertyCollector(config, ac, type, forSerialization, mutatorPrefix);
    }

    protected POJOPropertiesCollector collectPropertiesWithBuilder(MapperConfig<?> config, JavaType type, MixInResolver r, boolean forSerialization) {
        boolean useAnnotations = config.isAnnotationProcessingEnabled();
        AnnotationIntrospector ai = useAnnotations ? config.getAnnotationIntrospector() : null;
        AnnotatedClass ac = AnnotatedClass.construct(type, config, r);
        JsonPOJOBuilder.Value builderConfig = (ai == null) ? null : ai.findPOJOBuilderConfig(ac);
        String mutatorPrefix = (builderConfig == null) ? "with" : builderConfig.withPrefix;
        return constructPropertyCollector(config, ac, type, forSerialization, mutatorPrefix);
    }

    protected POJOPropertiesCollector constructPropertyCollector(MapperConfig<?> config, AnnotatedClass ac, JavaType type, boolean forSerialization, String mutatorPrefix) {
        return new POJOPropertiesCollector(config, forSerialization, type, ac, mutatorPrefix);
    }

    protected BasicBeanDescription _findStdTypeDesc(JavaType type) {
        Class<?> cls = type.getRawClass();
        if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {
                return BOOLEAN_DESC;
            }
            if (cls == Integer.TYPE) {
                return INT_DESC;
            }
            if (cls == Long.TYPE) {
                return LONG_DESC;
            }
        } else {
            if (cls == String.class) {
                return STRING_DESC;
            }
        }
        return null;
    }

    protected boolean _isStdJDKCollection(JavaType type) {
        if (!type.isContainerType() || type.isArrayType()) {
            return false;
        }
        Class<?> raw = type.getRawClass();
        String pkgName = ClassUtil.getPackageName(raw);
        if (pkgName != null) {
            if (pkgName.startsWith("java.lang") || pkgName.startsWith("java.util")) {
                if (Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected BasicBeanDescription _findStdJdkCollectionDesc(MapperConfig<?> cfg, JavaType type) {
        if (_isStdJDKCollection(type)) {
            AnnotatedClass ac = AnnotatedClass.construct(type, cfg);
            return BasicBeanDescription.forOtherUse(cfg, type, ac);
        }
        return null;
    }
}