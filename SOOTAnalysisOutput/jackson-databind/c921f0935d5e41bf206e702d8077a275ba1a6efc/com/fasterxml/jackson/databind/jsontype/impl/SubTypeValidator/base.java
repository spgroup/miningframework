package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SubTypeValidator {

    protected final static String PREFIX_STRING = "org.springframework.";

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
        s.add("org.apache.xalan.xsltc.trax.TemplatesImpl");
        s.add("com.sun.rowset.JdbcRowSetImpl");
        s.add("java.util.logging.FileHandler");
        s.add("java.rmi.server.UnicastRemoteObject");
        s.add("org.springframework.beans.factory.config.PropertyPathFactoryBean");
        s.add("com.mchange.v2.c3p0.JndiRefForwardingDataSource");
        s.add("com.mchange.v2.c3p0.WrapperConnectionPoolDataSource");
        s.add("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
        s.add("com.sun.org.apache.bcel.internal.util.ClassLoader");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet(s);
    }

    protected Set<String> _cfgIllegalClassNames = DEFAULT_NO_DESER_CLASS_NAMES;

    private final static SubTypeValidator instance = new SubTypeValidator();

    protected SubTypeValidator() {
    }

    public static SubTypeValidator instance() {
        return instance;
    }

    public void validateSubType(DeserializationContext ctxt, JavaType type) throws JsonMappingException {
        final Class<?> raw = type.getRawClass();
        String full = raw.getName();
        main_check: do {
            if (_cfgIllegalClassNames.contains(full)) {
                break;
            }
            if (full.startsWith(PREFIX_STRING)) {
                for (Class<?> cls = raw; cls != Object.class; cls = cls.getSuperclass()) {
                    String name = cls.getSimpleName();
                    if ("AbstractPointcutAdvisor".equals(name) || "AbstractApplicationContext".equals(name)) {
                        break main_check;
                    }
                }
            }
            return;
        } while (false);
        throw JsonMappingException.from(ctxt, String.format("Illegal type (%s) to deserialize: prevented for security reasons", full));
    }
}
