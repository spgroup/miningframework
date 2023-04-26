package org.hibernate.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.HibernateLogger;
import org.hibernate.Version;
import org.hibernate.bytecode.BytecodeProvider;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.util.ConfigHelper;
import org.jboss.logging.Logger;

public final class Environment {

    public static final String CONNECTION_PROVIDER = "hibernate.connection.provider_class";

    public static final String DRIVER = "hibernate.connection.driver_class";

    public static final String ISOLATION = "hibernate.connection.isolation";

    public static final String URL = "hibernate.connection.url";

    public static final String USER = "hibernate.connection.username";

    public static final String PASS = "hibernate.connection.password";

    public static final String AUTOCOMMIT = "hibernate.connection.autocommit";

    public static final String POOL_SIZE = "hibernate.connection.pool_size";

    public static final String DATASOURCE = "hibernate.connection.datasource";

    public static final String CONNECTION_PREFIX = "hibernate.connection";

    public static final String JNDI_CLASS = "hibernate.jndi.class";

    public static final String JNDI_URL = "hibernate.jndi.url";

    public static final String JNDI_PREFIX = "hibernate.jndi";

    public static final String SESSION_FACTORY_NAME = "hibernate.session_factory_name";

    public static final String DIALECT = "hibernate.dialect";

    public static final String DIALECT_RESOLVERS = "hibernate.dialect_resolvers";

    public static final String DEFAULT_SCHEMA = "hibernate.default_schema";

    public static final String DEFAULT_CATALOG = "hibernate.default_catalog";

    public static final String SHOW_SQL = "hibernate.show_sql";

    public static final String FORMAT_SQL = "hibernate.format_sql";

    public static final String USE_SQL_COMMENTS = "hibernate.use_sql_comments";

    public static final String MAX_FETCH_DEPTH = "hibernate.max_fetch_depth";

    public static final String DEFAULT_BATCH_FETCH_SIZE = "hibernate.default_batch_fetch_size";

    public static final String USE_STREAMS_FOR_BINARY = "hibernate.jdbc.use_streams_for_binary";

    public static final String USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";

    public static final String USE_GET_GENERATED_KEYS = "hibernate.jdbc.use_get_generated_keys";

    public static final String STATEMENT_FETCH_SIZE = "hibernate.jdbc.fetch_size";

    public static final String STATEMENT_BATCH_SIZE = "hibernate.jdbc.batch_size";

    public static final String BATCH_STRATEGY = "hibernate.jdbc.factory_class";

    public static final String BATCH_VERSIONED_DATA = "hibernate.jdbc.batch_versioned_data";

    public static final String OUTPUT_STYLESHEET = "hibernate.xml.output_stylesheet";

    public static final String C3P0_MAX_SIZE = "hibernate.c3p0.max_size";

    public static final String C3P0_MIN_SIZE = "hibernate.c3p0.min_size";

    public static final String C3P0_TIMEOUT = "hibernate.c3p0.timeout";

    public static final String C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";

    public static final String C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";

    public static final String C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";

    public static final String PROXOOL_PREFIX = "hibernate.proxool";

    public static final String PROXOOL_XML = "hibernate.proxool.xml";

    public static final String PROXOOL_PROPERTIES = "hibernate.proxool.properties";

    public static final String PROXOOL_EXISTING_POOL = "hibernate.proxool.existing_pool";

    public static final String PROXOOL_POOL_ALIAS = "hibernate.proxool.pool_alias";

    public static final String AUTO_CLOSE_SESSION = "hibernate.transaction.auto_close_session";

    public static final String FLUSH_BEFORE_COMPLETION = "hibernate.transaction.flush_before_completion";

    public static final String RELEASE_CONNECTIONS = "hibernate.connection.release_mode";

    public static final String CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";

    public static final String TRANSACTION_STRATEGY = "hibernate.transaction.factory_class";

    public static final String TRANSACTION_MANAGER_STRATEGY = "hibernate.transaction.manager_lookup_class";

    public static final String USER_TRANSACTION = "jta.UserTransaction";

    public static final String CACHE_PROVIDER = "hibernate.cache.provider_class";

    public static final String CACHE_REGION_FACTORY = "hibernate.cache.region.factory_class";

    public static final String CACHE_PROVIDER_CONFIG = "hibernate.cache.provider_configuration_file_resource_path";

    public static final String CACHE_NAMESPACE = "hibernate.cache.jndi";

    public static final String USE_QUERY_CACHE = "hibernate.cache.use_query_cache";

    public static final String QUERY_CACHE_FACTORY = "hibernate.cache.query_cache_factory";

    public static final String USE_SECOND_LEVEL_CACHE = "hibernate.cache.use_second_level_cache";

    public static final String USE_MINIMAL_PUTS = "hibernate.cache.use_minimal_puts";

    public static final String CACHE_REGION_PREFIX = "hibernate.cache.region_prefix";

    public static final String USE_STRUCTURED_CACHE = "hibernate.cache.use_structured_entries";

    public static final String GENERATE_STATISTICS = "hibernate.generate_statistics";

    public static final String USE_IDENTIFIER_ROLLBACK = "hibernate.use_identifier_rollback";

    public static final String USE_REFLECTION_OPTIMIZER = "hibernate.bytecode.use_reflection_optimizer";

    public static final String QUERY_TRANSLATOR = "hibernate.query.factory_class";

    public static final String QUERY_SUBSTITUTIONS = "hibernate.query.substitutions";

    public static final String QUERY_STARTUP_CHECKING = "hibernate.query.startup_check";

    public static final String HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";

    public static final String HBM2DDL_IMPORT_FILES = "hibernate.hbm2ddl.import_files";

    public static final String SQL_EXCEPTION_CONVERTER = "hibernate.jdbc.sql_exception_converter";

    public static final String WRAP_RESULT_SETS = "hibernate.jdbc.wrap_result_sets";

    public static final String ORDER_UPDATES = "hibernate.order_updates";

    public static final String ORDER_INSERTS = "hibernate.order_inserts";

    public static final String DEFAULT_ENTITY_MODE = "hibernate.default_entity_mode";

    public static final String JACC_CONTEXTID = "hibernate.jacc_context_id";

    public static final String GLOBALLY_QUOTED_IDENTIFIERS = "hibernate.globally_quoted_identifiers";

    public static final String CHECK_NULLABILITY = "hibernate.check_nullability";

    public static final String BYTECODE_PROVIDER = "hibernate.bytecode.provider";

    public static final String JPAQL_STRICT_COMPLIANCE = "hibernate.query.jpaql_strict_compliance";

    public static final String PREFER_POOLED_VALUES_LO = "hibernate.id.optimizer.pooled.prefer_lo";

    public static final String QUERY_PLAN_CACHE_MAX_STRONG_REFERENCES = "hibernate.query.plan_cache_max_strong_references";

    public static final String QUERY_PLAN_CACHE_MAX_SOFT_REFERENCES = "hibernate.query.plan_cache_max_soft_references";

    public static final String NON_CONTEXTUAL_LOB_CREATION = "hibernate.jdbc.lob.non_contextual_creation";

    private static final BytecodeProvider BYTECODE_PROVIDER_INSTANCE;

    private static final boolean ENABLE_BINARY_STREAMS;

    private static final boolean ENABLE_REFLECTION_OPTIMIZER;

    private static final boolean JVM_SUPPORTS_LINKED_HASH_COLLECTIONS;

    private static final boolean JVM_HAS_TIMESTAMP_BUG;

    private static final boolean JVM_HAS_JDK14_TIMESTAMP;

    private static final boolean JVM_SUPPORTS_GET_GENERATED_KEYS;

    private static final Properties GLOBAL_PROPERTIES;

    private static final HashMap ISOLATION_LEVELS = new HashMap();

    private static final Map OBSOLETE_PROPERTIES = new HashMap();

    private static final Map RENAMED_PROPERTIES = new HashMap();

    private static final HibernateLogger LOG = Logger.getMessageLogger(HibernateLogger.class, Environment.class.getName());

    public static void verifyProperties(Properties props) {
        Iterator iter = props.keySet().iterator();
        Map propertiesToAdd = new HashMap();
        while (iter.hasNext()) {
            final Object propertyName = iter.next();
            Object newPropertyName = OBSOLETE_PROPERTIES.get(propertyName);
            if (newPropertyName != null)
                LOG.unsupportedProperty(propertyName, newPropertyName);
            newPropertyName = RENAMED_PROPERTIES.get(propertyName);
            if (newPropertyName != null) {
                LOG.renamedProperty(propertyName, newPropertyName);
                if (!props.containsKey(newPropertyName)) {
                    propertiesToAdd.put(newPropertyName, props.get(propertyName));
                }
            }
        }
        props.putAll(propertiesToAdd);
    }

    static {
        LOG.version(Version.getVersionString());
        RENAMED_PROPERTIES.put("hibernate.cglib.use_reflection_optimizer", USE_REFLECTION_OPTIMIZER);
        ISOLATION_LEVELS.put(new Integer(Connection.TRANSACTION_NONE), "NONE");
        ISOLATION_LEVELS.put(new Integer(Connection.TRANSACTION_READ_UNCOMMITTED), "READ_UNCOMMITTED");
        ISOLATION_LEVELS.put(new Integer(Connection.TRANSACTION_READ_COMMITTED), "READ_COMMITTED");
        ISOLATION_LEVELS.put(new Integer(Connection.TRANSACTION_REPEATABLE_READ), "REPEATABLE_READ");
        ISOLATION_LEVELS.put(new Integer(Connection.TRANSACTION_SERIALIZABLE), "SERIALIZABLE");
        GLOBAL_PROPERTIES = new Properties();
        GLOBAL_PROPERTIES.setProperty(USE_REFLECTION_OPTIMIZER, Boolean.FALSE.toString());
        try {
            InputStream stream = ConfigHelper.getResourceAsStream("/hibernate.properties");
            try {
                GLOBAL_PROPERTIES.load(stream);
                LOG.propertiesLoaded(ConfigurationHelper.maskOut(GLOBAL_PROPERTIES, PASS));
            } catch (Exception e) {
                LOG.unableToloadProperties();
            } finally {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    LOG.unableToCloseStreamError(ioe);
                }
            }
        } catch (HibernateException he) {
            LOG.propertiesNotFound();
        }
        try {
            GLOBAL_PROPERTIES.putAll(System.getProperties());
        } catch (SecurityException se) {
            LOG.unableToCopySystemProperties();
        }
        verifyProperties(GLOBAL_PROPERTIES);
        ENABLE_BINARY_STREAMS = ConfigurationHelper.getBoolean(USE_STREAMS_FOR_BINARY, GLOBAL_PROPERTIES);
        ENABLE_REFLECTION_OPTIMIZER = ConfigurationHelper.getBoolean(USE_REFLECTION_OPTIMIZER, GLOBAL_PROPERTIES);
        if (ENABLE_BINARY_STREAMS)
            LOG.usingStreams();
        if (ENABLE_REFLECTION_OPTIMIZER)
            LOG.usingReflectionOptimizer();
        BYTECODE_PROVIDER_INSTANCE = buildBytecodeProvider(GLOBAL_PROPERTIES);
        boolean getGeneratedKeysSupport;
        try {
            Statement.class.getMethod("getGeneratedKeys", (Class[]) null);
            getGeneratedKeysSupport = true;
        } catch (NoSuchMethodException nsme) {
            getGeneratedKeysSupport = false;
        }
        JVM_SUPPORTS_GET_GENERATED_KEYS = getGeneratedKeysSupport;
        if (!JVM_SUPPORTS_GET_GENERATED_KEYS)
            LOG.generatedKeysNotSupported();
        boolean linkedHashSupport;
        try {
            Class.forName("java.util.LinkedHashSet");
            linkedHashSupport = true;
        } catch (ClassNotFoundException cnfe) {
            linkedHashSupport = false;
        }
        JVM_SUPPORTS_LINKED_HASH_COLLECTIONS = linkedHashSupport;
        if (!JVM_SUPPORTS_LINKED_HASH_COLLECTIONS)
            LOG.linkedMapsAndSetsNotSupported();
        long x = 123456789;
        JVM_HAS_TIMESTAMP_BUG = new Timestamp(x).getTime() != x;
        if (JVM_HAS_TIMESTAMP_BUG)
            LOG.usingTimestampWorkaround();
        Timestamp t = new Timestamp(0);
        t.setNanos(5 * 1000000);
        JVM_HAS_JDK14_TIMESTAMP = t.getTime() == 5;
        if (JVM_HAS_JDK14_TIMESTAMP)
            LOG.usingJdk14TimestampHandling();
        else
            LOG.usingPreJdk14TimestampHandling();
    }

    public static BytecodeProvider getBytecodeProvider() {
        return BYTECODE_PROVIDER_INSTANCE;
    }

    public static boolean jvmHasTimestampBug() {
        return JVM_HAS_TIMESTAMP_BUG;
    }

    @Deprecated
    public static boolean jvmHasJDK14Timestamp() {
        return JVM_HAS_JDK14_TIMESTAMP;
    }

    @Deprecated
    public static boolean jvmSupportsLinkedHashCollections() {
        return JVM_SUPPORTS_LINKED_HASH_COLLECTIONS;
    }

    @Deprecated
    public static boolean jvmSupportsGetGeneratedKeys() {
        return JVM_SUPPORTS_GET_GENERATED_KEYS;
    }

    public static boolean useStreamsForBinary() {
        return ENABLE_BINARY_STREAMS;
    }

    public static boolean useReflectionOptimizer() {
        return ENABLE_REFLECTION_OPTIMIZER;
    }

    private Environment() {
        throw new UnsupportedOperationException();
    }

    public static Properties getProperties() {
        Properties copy = new Properties();
        copy.putAll(GLOBAL_PROPERTIES);
        return copy;
    }

    public static String isolationLevelToString(int isolation) {
        return (String) ISOLATION_LEVELS.get(new Integer(isolation));
    }

    public static BytecodeProvider buildBytecodeProvider(Properties properties) {
        String provider = ConfigurationHelper.getString(BYTECODE_PROVIDER, properties, "javassist");
        LOG.bytecodeProvider(provider);
        return buildBytecodeProvider(provider);
    }

    private static BytecodeProvider buildBytecodeProvider(String providerName) {
        if ("javassist".equals(providerName)) {
            return new org.hibernate.bytecode.javassist.BytecodeProviderImpl();
        } else if ("cglib".equals(providerName)) {
            return new org.hibernate.bytecode.cglib.BytecodeProviderImpl();
        }
        LOG.unknownBytecodeProvider(providerName);
        return new org.hibernate.bytecode.javassist.BytecodeProviderImpl();
    }
}
