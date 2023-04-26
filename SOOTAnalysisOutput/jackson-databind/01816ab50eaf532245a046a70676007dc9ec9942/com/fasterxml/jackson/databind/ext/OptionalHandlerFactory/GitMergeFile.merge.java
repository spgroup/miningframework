package com.fasterxml.jackson.databind.ext;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.util.ClassUtil;

public class OptionalHandlerFactory implements java.io.Serializable {

    private static final long serialVersionUID = 1;

    private final static String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";

    private final static String SERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLSerializers";

    private final static String DESERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLDeserializers";

    private final static String SERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMSerializer";

    private final static String DESERIALIZER_FOR_DOM_DOCUMENT = "com.fasterxml.jackson.databind.ext.DOMDeserializer$DocumentDeserializer";

    private final static String DESERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMDeserializer$NodeDeserializer";

    private final static Class<?> CLASS_DOM_NODE;

    private final static Class<?> CLASS_DOM_DOCUMENT;

    static {
        Class<?> doc = null, node = null;
        try {
            node = org.w3c.dom.Node.class;
            doc = org.w3c.dom.Document.class;
        } catch (Throwable e) {
            Logger.getLogger(OptionalHandlerFactory.class.getName()).log(Level.INFO, "Could not load DOM `Node` and/or `Document` classes: no DOM support");
        }
        CLASS_DOM_NODE = node;
        CLASS_DOM_DOCUMENT = doc;
    }

    private static final Java7Handlers _jdk7Helper;

    static {
        Java7Handlers x = null;
        try {
            x = Java7Handlers.instance();
        } catch (Throwable t) {
        }
        _jdk7Helper = x;
    }

    public final static OptionalHandlerFactory instance = new OptionalHandlerFactory();

    protected OptionalHandlerFactory() {
    }

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        final Class<?> rawType = type.getRawClass();
        if ((CLASS_DOM_NODE != null) && CLASS_DOM_NODE.isAssignableFrom(rawType)) {
            return (JsonSerializer<?>) instantiate(SERIALIZER_FOR_DOM_NODE);
        }
        if (_jdk7Helper != null) {
            JsonSerializer<?> ser = _jdk7Helper.getSerializerForJavaNioFilePath(rawType);
            if (ser != null) {
                return ser;
            }
        }
        String className = rawType.getName();
        String factoryName;
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || hasSuperClassStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            factoryName = SERIALIZERS_FOR_JAVAX_XML;
        } else {
            return null;
        }
        Object ob = instantiate(factoryName);
        if (ob == null) {
            return null;
        }
        return ((Serializers) ob).findSerializer(config, type, beanDesc);
    }

    public JsonDeserializer<?> findDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
        final Class<?> rawType = type.getRawClass();
        if (_jdk7Helper != null) {
            JsonDeserializer<?> deser = _jdk7Helper.getDeserializerForJavaNioFilePath(rawType);
            if (deser != null) {
                return deser;
            }
        }
        if ((CLASS_DOM_NODE != null) && CLASS_DOM_NODE.isAssignableFrom(rawType)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_NODE);
        }
        if ((CLASS_DOM_DOCUMENT != null) && CLASS_DOM_DOCUMENT.isAssignableFrom(rawType)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_DOCUMENT);
        }
        String className = rawType.getName();
        String factoryName;
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || hasSuperClassStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            factoryName = DESERIALIZERS_FOR_JAVAX_XML;
        } else {
            return null;
        }
        Object ob = instantiate(factoryName);
        if (ob == null) {
            return null;
        }
        return ((Deserializers) ob).findBeanDeserializer(type, config, beanDesc);
    }

    public boolean hasDeserializerFor(Class<?> valueType) {
        if ((CLASS_DOM_NODE != null) && CLASS_DOM_NODE.isAssignableFrom(valueType)) {
            return true;
        }
        if ((CLASS_DOM_DOCUMENT != null) && CLASS_DOM_DOCUMENT.isAssignableFrom(valueType)) {
            return true;
        }
        String className = valueType.getName();
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || hasSuperClassStartingWith(valueType, PACKAGE_PREFIX_JAVAX_XML)) {
            return true;
        }
        return false;
    }

    private Object instantiate(String className) {
        try {
            return ClassUtil.createInstance(Class.forName(className), false);
        } catch (LinkageError e) {
        } catch (Exception e) {
        }
        return null;
    }

    private boolean hasSuperClassStartingWith(Class<?> rawType, String prefix) {
        for (Class<?> supertype = rawType.getSuperclass(); supertype != null; supertype = supertype.getSuperclass()) {
            if (supertype == Object.class) {
                return false;
            }
            if (supertype.getName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
