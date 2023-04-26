package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SubTypeValidator {

    protected final static String PREFIX_SPRING = "org.springframework.";

    protected final static String PREFIX_C3P0 = "com.mchange.v2.c3p0.";

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
        s.add("org.apache.tomcat.dbcp.dbcp2.BasicDataSource");
        s.add("com.sun.org.apache.bcel.internal.util.ClassLoader");
        s.add("org.hibernate.jmx.StatisticsService");
        s.add("org.apache.ibatis.datasource.jndi.JndiDataSourceFactory");
        s.add("org.apache.ibatis.parsing.XPathParser");
        s.add("jodd.db.connection.DataSourceConnectionProvider");
        s.add("oracle.jdbc.connector.OracleManagedConnectionFactory");
        s.add("oracle.jdbc.rowset.OracleJDBCRowSet");
        s.add("org.slf4j.ext.EventData");
        s.add("flex.messaging.util.concurrent.AsynchBeansWorkManagerExecutor");
        s.add("com.sun.deploy.security.ruleset.DRSHelper");
        s.add("org.apache.axis2.jaxws.spi.handler.HandlerResolverImpl");
        s.add("org.jboss.util.propertyeditor.DocumentEditor");
        s.add("org.apache.openjpa.ee.RegistryManagedRuntime");
        s.add("org.apache.openjpa.ee.JNDIManagedRuntime");
        s.add("org.apache.axis2.transport.jms.JMSOutTransportInfo");
        s.add("com.mysql.cj.jdbc.admin.MiniAdmin");
        s.add("ch.qos.logback.core.db.DriverManagerConnectionSource");
        DEFAULT_NO_DESER_CLASS_NAMES = Collections.unmodifiableSet(s);
    }

    protected Set<String> _cfgIllegalClassNames = DEFAULT_NO_DESER_CLASS_NAMES;

    private final static SubTypeValidator instance = new SubTypeValidator();

    protected SubTypeValidator() {
    }

    public static SubTypeValidator instance() {
        return instance;
    }

    public void validateSubType(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) throws JsonMappingException {
        final Class<?> raw = type.getRawClass();
        String full = raw.getName();
        main_check: do {
            if (_cfgIllegalClassNames.contains(full)) {
                break;
            }
            if (raw.isInterface()) {
                ;
            } else if (full.startsWith(PREFIX_SPRING)) {
                for (Class<?> cls = raw; (cls != null) && (cls != Object.class); cls = cls.getSuperclass()) {
                    String name = cls.getSimpleName();
                    if ("AbstractPointcutAdvisor".equals(name) || "AbstractApplicationContext".equals(name)) {
                        break main_check;
                    }
                }
            } else if (full.startsWith(PREFIX_C3P0)) {
                if (full.endsWith("DataSource")) {
                    break main_check;
                }
            }
            return;
        } while (false);
        ctxt.reportBadTypeDefinition(beanDesc, "Illegal type (%s) to deserialize: prevented for security reasons", full);
    }
}