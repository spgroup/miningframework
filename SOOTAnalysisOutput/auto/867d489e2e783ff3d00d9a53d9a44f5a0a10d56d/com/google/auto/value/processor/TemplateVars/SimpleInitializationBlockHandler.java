package com.google.auto.value.processor;

import com.google.common.collect.ImmutableList;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.apache.velocity.runtime.resource.ResourceCacheImpl;

abstract class TemplateVars {

    abstract SimpleNode parsedTemplate();

    private static final RuntimeInstance velocityRuntimeInstance = new RuntimeInstance();

    static {
        velocityRuntimeInstance.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, "true");
        velocityRuntimeInstance.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, new NullLogChute());
        velocityRuntimeInstance.setProperty(RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS, ResourceCacheImpl.class.getName());
        velocityRuntimeInstance.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Thread currentThread = Thread.currentThread();
        ClassLoader oldContextLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(TemplateVars.class.getClassLoader());
            velocityRuntimeInstance.init();
        } finally {
            currentThread.setContextClassLoader(oldContextLoader);
        }
    }

    private final ImmutableList<Field> fields;

    TemplateVars() {
        if (getClass().getSuperclass() != TemplateVars.class) {
            throw new IllegalArgumentException("Class must extend TemplateVars directly");
        }
        ImmutableList.Builder<Field> fields = ImmutableList.builder();
        Field[] declaredFields = getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isSynthetic() || isStaticFinal(field)) {
                continue;
            }
            if (Modifier.isPrivate(field.getModifiers())) {
                throw new IllegalArgumentException("Field cannot be private: " + field);
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new IllegalArgumentException("Field cannot be static unless also final: " + field);
            }
            if (field.getType().isPrimitive()) {
                throw new IllegalArgumentException("Field cannot be primitive: " + field);
            }
            fields.add(field);
        }
        this.fields = fields.build();
    }

    String toText() {
        VelocityContext velocityContext = toVelocityContext();
        StringWriter writer = new StringWriter();
        SimpleNode parsedTemplate = parsedTemplate();
        boolean rendered = velocityRuntimeInstance.render(velocityContext, writer, parsedTemplate.getTemplateName(), parsedTemplate);
        if (!rendered) {
            throw new IllegalArgumentException("Template rendering failed");
        }
        return writer.toString();
    }

    private VelocityContext toVelocityContext() {
        VelocityContext velocityContext = new VelocityContext();
        for (Field field : fields) {
            Object value = fieldValue(field, this);
            if (value == null) {
                throw new IllegalArgumentException("Field cannot be null (was it set?): " + field);
            }
            Object old = velocityContext.put(field.getName(), value);
            if (old != null) {
                throw new IllegalArgumentException("Two fields called " + field.getName() + "?!");
            }
        }
        return velocityContext;
    }

    static SimpleNode parsedTemplateForResource(String resourceName) {
        InputStream in = AutoValueTemplateVars.class.getResourceAsStream(resourceName);
        if (in == null) {
            throw new IllegalArgumentException("Could not find resource: " + resourceName);
        }
        try {
            Reader reader = new InputStreamReader(in, "UTF-8");
            return velocityRuntimeInstance.parse(reader, resourceName);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    private static Object fieldValue(Field field, Object container) {
        try {
            return field.get(container);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }
}