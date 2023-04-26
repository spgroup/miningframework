package org.hibernate.service.jdbc.connections.internal;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.util.beans.BeanInfoHelper;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistry;

public class ConnectionProviderInitiator implements ServiceInitiator<ConnectionProvider> {

    public static final ConnectionProviderInitiator INSTANCE = new ConnectionProviderInitiator();

    private static final Logger log = LoggerFactory.getLogger(ConnectionProviderInitiator.class);

    public static final String C3P0_CONFIG_PREFIX = "hibernate.c3p0";

    public static final String C3P0_PROVIDER_CLASS_NAME = "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider";

    public static final String PROXOOL_CONFIG_PREFIX = "hibernate.proxool";

    public static final String PROXOOL_PROVIDER_CLASS_NAME = "org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider";

    public static final String INJECTION_DATA = "hibernate.connection_provider.injection_data";

    private static final Map<String, String> LEGACY_CONNECTION_PROVIDER_MAPPING;

    static {
        LEGACY_CONNECTION_PROVIDER_MAPPING = new HashMap<String, String>(5);
        LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.DatasourceConnectionProvider", DatasourceConnectionProviderImpl.class.getName());
        LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.DriverManagerConnectionProvider", DriverManagerConnectionProviderImpl.class.getName());
        LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.UserSuppliedConnectionProvider", UserSuppliedConnectionProviderImpl.class.getName());
        LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.C3P0ConnectionProvider", C3P0_PROVIDER_CLASS_NAME);
        LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.ProxoolConnectionProvider", PROXOOL_PROVIDER_CLASS_NAME);
    }

    public Class<ConnectionProvider> getServiceInitiated() {
        return ConnectionProvider.class;
    }

    public ConnectionProvider initiateService(Map configurationValues, ServiceRegistry registry) {
        final ClassLoaderService classLoaderService = registry.getService(ClassLoaderService.class);
        ConnectionProvider connectionProvider = null;
        String providerClassName = (String) getConfiguredConnectionProviderName(configurationValues);
        if (providerClassName != null) {
            connectionProvider = instantiateExplicitConnectionProvider(providerClassName, classLoaderService);
        } else if (configurationValues.get(Environment.DATASOURCE) != null) {
            connectionProvider = new DatasourceConnectionProviderImpl();
        }
        if (connectionProvider == null) {
            if (c3p0ConfigDefined(configurationValues) && c3p0ProviderPresent(classLoaderService)) {
                connectionProvider = instantiateExplicitConnectionProvider(C3P0_PROVIDER_CLASS_NAME, classLoaderService);
            }
        }
        if (connectionProvider == null) {
            if (proxoolConfigDefined(configurationValues) && proxoolProviderPresent(classLoaderService)) {
                connectionProvider = instantiateExplicitConnectionProvider(PROXOOL_PROVIDER_CLASS_NAME, classLoaderService);
            }
        }
        if (connectionProvider == null) {
            if (configurationValues.get(Environment.URL) != null) {
                connectionProvider = new DriverManagerConnectionProviderImpl();
            }
        }
        if (connectionProvider == null) {
            log.warn("No appropriate connection provider encountered, assuming application will be supplying connections");
            connectionProvider = new UserSuppliedConnectionProviderImpl();
        }
        final Map injectionData = (Map) configurationValues.get(INJECTION_DATA);
        if (injectionData != null && injectionData.size() > 0) {
            final ConnectionProvider theConnectionProvider = connectionProvider;
            new BeanInfoHelper(connectionProvider.getClass()).applyToBeanInfo(connectionProvider, new BeanInfoHelper.BeanInfoDelegate() {

                public void processBeanInfo(BeanInfo beanInfo) throws Exception {
                    PropertyDescriptor[] descritors = beanInfo.getPropertyDescriptors();
                    for (int i = 0, size = descritors.length; i < size; i++) {
                        String propertyName = descritors[i].getName();
                        if (injectionData.containsKey(propertyName)) {
                            Method method = descritors[i].getWriteMethod();
                            method.invoke(theConnectionProvider, injectionData.get(propertyName));
                        }
                    }
                }
            });
        }
        return connectionProvider;
    }

    private String getConfiguredConnectionProviderName(Map configurationValues) {
        String providerClassName = (String) configurationValues.get(Environment.CONNECTION_PROVIDER);
        if (LEGACY_CONNECTION_PROVIDER_MAPPING.containsKey(providerClassName)) {
            String actualProviderClassName = LEGACY_CONNECTION_PROVIDER_MAPPING.get(providerClassName);
            if (log.isWarnEnabled()) {
                StringBuffer buf = new StringBuffer().append(providerClassName).append(" has been deprecated in favor of ").append(actualProviderClassName).append("; that provider will be used instead.");
                log.warn(buf.toString());
            }
            providerClassName = actualProviderClassName;
        }
        return providerClassName;
    }

    private ConnectionProvider instantiateExplicitConnectionProvider(String providerClassName, ClassLoaderService classLoaderService) {
        try {
            log.info("Instantiating explicit connection provider: " + providerClassName);
            return (ConnectionProvider) classLoaderService.classForName(providerClassName).newInstance();
        } catch (Exception e) {
            throw new HibernateException("Could not instantiate connection provider [" + providerClassName + "]", e);
        }
    }

    private boolean c3p0ProviderPresent(ClassLoaderService classLoaderService) {
        try {
            classLoaderService.classForName(C3P0_PROVIDER_CLASS_NAME);
        } catch (Exception e) {
            log.warn("c3p0 properties were encountered, but the " + C3P0_PROVIDER_CLASS_NAME + " provider class was not found on the classpath; these properties are going to be ignored.");
            return false;
        }
        return true;
    }

    private static boolean c3p0ConfigDefined(Map configValues) {
        for (Object key : configValues.keySet()) {
            if (String.class.isInstance(key) && ((String) key).startsWith(C3P0_CONFIG_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private boolean proxoolProviderPresent(ClassLoaderService classLoaderService) {
        try {
            classLoaderService.classForName(PROXOOL_PROVIDER_CLASS_NAME);
        } catch (Exception e) {
            log.warn("proxool properties were encountered, but the " + PROXOOL_PROVIDER_CLASS_NAME + " provider class was not found on the classpath; these properties are going to be ignored.");
            return false;
        }
        return true;
    }

    private static boolean proxoolConfigDefined(Map configValues) {
        for (Object key : configValues.keySet()) {
            if (String.class.isInstance(key) && ((String) key).startsWith(PROXOOL_CONFIG_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    public static Properties getConnectionProperties(Map<?, ?> properties) {
        Properties result = new Properties();
        for (Map.Entry entry : properties.entrySet()) {
            if (!(String.class.isInstance(entry.getKey())) || !String.class.isInstance(entry.getValue())) {
                continue;
            }
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();
            if (key.startsWith(Environment.CONNECTION_PREFIX)) {
                if (SPECIAL_PROPERTIES.contains(key)) {
                    if (Environment.USER.equals(key)) {
                        result.setProperty("user", value);
                    }
                } else {
                    final String passThruKey = key.substring(Environment.CONNECTION_PREFIX.length() + 1);
                    result.setProperty(passThruKey, value);
                }
            }
        }
        return result;
    }

    private static final Set<String> SPECIAL_PROPERTIES;

    static {
        SPECIAL_PROPERTIES = new HashSet<String>();
        SPECIAL_PROPERTIES.add(Environment.DATASOURCE);
        SPECIAL_PROPERTIES.add(Environment.URL);
        SPECIAL_PROPERTIES.add(Environment.CONNECTION_PROVIDER);
        SPECIAL_PROPERTIES.add(Environment.POOL_SIZE);
        SPECIAL_PROPERTIES.add(Environment.ISOLATION);
        SPECIAL_PROPERTIES.add(Environment.DRIVER);
        SPECIAL_PROPERTIES.add(Environment.USER);
    }
}
