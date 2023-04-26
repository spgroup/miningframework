package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ser.Serializers;

public class OptionalHandlerFactory implements java.io.Serializable {

    private static final long serialVersionUID = 1;

    private final static String PACKAGE_PREFIX_JAVAX_XML = "javax.xml.";

    private final static String SERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLSerializers";

    private final static String DESERIALIZERS_FOR_JAVAX_XML = "com.fasterxml.jackson.databind.ext.CoreXMLDeserializers";

    private final static String CLASS_NAME_DOM_NODE = "org.w3c.dom.Node";

    private final static String CLASS_NAME_DOM_DOCUMENT = "org.w3c.dom.Node";

    private final static String SERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMSerializer";

    private final static String DESERIALIZER_FOR_DOM_DOCUMENT = "com.fasterxml.jackson.databind.ext.DOMDeserializer$DocumentDeserializer";

    private final static String DESERIALIZER_FOR_DOM_NODE = "com.fasterxml.jackson.databind.ext.DOMDeserializer$NodeDeserializer";

    public final static OptionalHandlerFactory instance = new OptionalHandlerFactory();

    protected OptionalHandlerFactory() {
    }

    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> rawType = type.getRawClass();
        String className = rawType.getName();
        String factoryName;
        if (doesImplement(rawType, CLASS_NAME_DOM_NODE)) {
            return (JsonSerializer<?>) instantiate(SERIALIZER_FOR_DOM_NODE);
        }
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || hasSupertypeStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
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
        Class<?> rawType = type.getRawClass();
        String className = rawType.getName();
        String factoryName;
        if (className.startsWith(PACKAGE_PREFIX_JAVAX_XML) || hasSupertypeStartingWith(rawType, PACKAGE_PREFIX_JAVAX_XML)) {
            factoryName = DESERIALIZERS_FOR_JAVAX_XML;
        } else if (doesImplement(rawType, CLASS_NAME_DOM_DOCUMENT)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_DOCUMENT);
        } else if (doesImplement(rawType, CLASS_NAME_DOM_NODE)) {
            return (JsonDeserializer<?>) instantiate(DESERIALIZER_FOR_DOM_NODE);
        } else {
            return null;
        }
        Object ob = instantiate(factoryName);
        if (ob == null) {
            return null;
        }
        return ((Deserializers) ob).findBeanDeserializer(type, config, beanDesc);
    }

    private Object instantiate(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (LinkageError e) {
        } catch (Exception e) {
        }
        return null;
    }

    private boolean doesImplement(Class<?> actualType, String classNameToImplement) {
        for (Class<?> type = actualType; type != null; type = type.getSuperclass()) {
            if (type.getName().equals(classNameToImplement)) {
                return true;
            }
            if (hasInterface(type, classNameToImplement)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInterface(Class<?> type, String interfaceToImplement) {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (iface.getName().equals(interfaceToImplement)) {
                return true;
            }
        }
        for (Class<?> iface : interfaces) {
            if (hasInterface(iface, interfaceToImplement)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSupertypeStartingWith(Class<?> rawType, String prefix) {
        for (Class<?> supertype = rawType.getSuperclass(); supertype != null; supertype = supertype.getSuperclass()) {
            if (supertype.getName().startsWith(prefix)) {
                return true;
            }
        }
        for (Class<?> cls = rawType; cls != null; cls = cls.getSuperclass()) {
            if (hasInterfaceStartingWith(cls, prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInterfaceStartingWith(Class<?> type, String prefix) {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (iface.getName().startsWith(prefix)) {
                return true;
            }
        }
        for (Class<?> iface : interfaces) {
            if (hasInterfaceStartingWith(iface, prefix)) {
                return true;
            }
        }
        return false;
    }
}
