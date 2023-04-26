package io.quarkus.restclient.config;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.smallrye.config.SmallRyeConfig;

@ConfigGroup
public class RestClientConfig {

    public static final RestClientConfig EMPTY;

    static {
        EMPTY = new RestClientConfig();
        EMPTY.url = Optional.empty();
        EMPTY.uri = Optional.empty();
        EMPTY.scope = Optional.empty();
        EMPTY.providers = Optional.empty();
        EMPTY.connectTimeout = Optional.empty();
        EMPTY.readTimeout = Optional.empty();
        EMPTY.followRedirects = Optional.empty();
        EMPTY.proxyAddress = Optional.empty();
        EMPTY.proxyUser = Optional.empty();
        EMPTY.proxyPassword = Optional.empty();
        EMPTY.nonProxyHosts = Optional.empty();
        EMPTY.queryParamStyle = Optional.empty();
        EMPTY.trustStore = Optional.empty();
        EMPTY.trustStorePassword = Optional.empty();
        EMPTY.trustStoreType = Optional.empty();
        EMPTY.keyStore = Optional.empty();
        EMPTY.keyStorePassword = Optional.empty();
        EMPTY.keyStoreType = Optional.empty();
        EMPTY.hostnameVerifier = Optional.empty();
        EMPTY.connectionTTL = Optional.empty();
        EMPTY.connectionPoolSize = Optional.empty();
        EMPTY.maxRedirects = Optional.empty();
        EMPTY.headers = Collections.emptyMap();
        EMPTY.shared = Optional.empty();
        EMPTY.name = Optional.empty();
    }

    @ConfigItem
    public Optional<String> url;

    @ConfigItem
    public Optional<String> uri;

    @ConfigItem
    public Optional<String> scope;

    @ConfigItem
    public Optional<String> providers;

    @ConfigItem
    public Optional<Long> connectTimeout;

    @ConfigItem
    public Optional<Long> readTimeout;

    @ConfigItem
    public Optional<Boolean> followRedirects;

    @ConfigItem
    public Optional<String> proxyAddress;

    @ConfigItem
    public Optional<String> proxyUser;

    @ConfigItem
    public Optional<String> proxyPassword;

    @ConfigItem
    public Optional<String> nonProxyHosts;

    @ConfigItem
    public Optional<QueryParamStyle> queryParamStyle;

    @ConfigItem
    public Optional<String> trustStore;

    @ConfigItem
    public Optional<String> trustStorePassword;

    @ConfigItem
    public Optional<String> trustStoreType;

    @ConfigItem
    public Optional<String> keyStore;

    @ConfigItem
    public Optional<String> keyStorePassword;

    @ConfigItem
    public Optional<String> keyStoreType;

    @ConfigItem
    public Optional<String> hostnameVerifier;

    @ConfigItem
    public Optional<Integer> connectionTTL;

    @ConfigItem
    public Optional<Integer> connectionPoolSize;

    @ConfigItem
    public Optional<Integer> maxRedirects;

    @ConfigItem
    public Map<String, String> headers;

    @ConfigItem
    public Optional<Boolean> shared;

    @ConfigItem
    public Optional<String> name;

    public static RestClientConfig load(String configKey) {
        final RestClientConfig instance = new RestClientConfig();
        instance.url = getConfigValue(configKey, "url", String.class);
        instance.uri = getConfigValue(configKey, "uri", String.class);
        instance.scope = getConfigValue(configKey, "scope", String.class);
        instance.providers = getConfigValue(configKey, "providers", String.class);
        instance.connectTimeout = getConfigValue(configKey, "connect-timeout", Long.class);
        instance.readTimeout = getConfigValue(configKey, "read-timeout", Long.class);
        instance.followRedirects = getConfigValue(configKey, "follow-redirects", Boolean.class);
        instance.proxyAddress = getConfigValue(configKey, "proxy-address", String.class);
        instance.proxyUser = getConfigValue(configKey, "proxy-user", String.class);
        instance.proxyPassword = getConfigValue(configKey, "proxy-password", String.class);
        instance.nonProxyHosts = getConfigValue(configKey, "non-proxy-hosts", String.class);
        instance.queryParamStyle = getConfigValue(configKey, "query-param-style", QueryParamStyle.class);
        instance.trustStore = getConfigValue(configKey, "trust-store", String.class);
        instance.trustStorePassword = getConfigValue(configKey, "trust-store-password", String.class);
        instance.trustStoreType = getConfigValue(configKey, "trust-store-type", String.class);
        instance.keyStore = getConfigValue(configKey, "key-store", String.class);
        instance.keyStorePassword = getConfigValue(configKey, "key-store-password", String.class);
        instance.keyStoreType = getConfigValue(configKey, "key-store-type", String.class);
        instance.hostnameVerifier = getConfigValue(configKey, "hostname-verifier", String.class);
        instance.connectionTTL = getConfigValue(configKey, "connection-ttl", Integer.class);
        instance.connectionPoolSize = getConfigValue(configKey, "connection-pool-size", Integer.class);
        instance.maxRedirects = getConfigValue(configKey, "max-redirects", Integer.class);
        instance.headers = getConfigValues(configKey, "headers", String.class, String.class);
        instance.shared = getConfigValue(configKey, "shared", Boolean.class);
        instance.name = getConfigValue(configKey, "name", String.class);
        return instance;
    }

    public static RestClientConfig load(Class<?> interfaceClass) {
        final RestClientConfig instance = new RestClientConfig();
        instance.url = getConfigValue(interfaceClass, "url", String.class);
        instance.uri = getConfigValue(interfaceClass, "uri", String.class);
        instance.scope = getConfigValue(interfaceClass, "scope", String.class);
        instance.providers = getConfigValue(interfaceClass, "providers", String.class);
        instance.connectTimeout = getConfigValue(interfaceClass, "connect-timeout", Long.class);
        instance.readTimeout = getConfigValue(interfaceClass, "read-timeout", Long.class);
        instance.followRedirects = getConfigValue(interfaceClass, "follow-redirects", Boolean.class);
        instance.proxyAddress = getConfigValue(interfaceClass, "proxy-address", String.class);
        instance.proxyUser = getConfigValue(interfaceClass, "proxy-user", String.class);
        instance.proxyPassword = getConfigValue(interfaceClass, "proxy-password", String.class);
        instance.nonProxyHosts = getConfigValue(interfaceClass, "non-proxy-hosts", String.class);
        instance.queryParamStyle = getConfigValue(interfaceClass, "query-param-style", QueryParamStyle.class);
        instance.trustStore = getConfigValue(interfaceClass, "trust-store", String.class);
        instance.trustStorePassword = getConfigValue(interfaceClass, "trust-store-password", String.class);
        instance.trustStoreType = getConfigValue(interfaceClass, "trust-store-type", String.class);
        instance.keyStore = getConfigValue(interfaceClass, "key-store", String.class);
        instance.keyStorePassword = getConfigValue(interfaceClass, "key-store-password", String.class);
        instance.keyStoreType = getConfigValue(interfaceClass, "key-store-type", String.class);
        instance.hostnameVerifier = getConfigValue(interfaceClass, "hostname-verifier", String.class);
        instance.connectionTTL = getConfigValue(interfaceClass, "connection-ttl", Integer.class);
        instance.connectionPoolSize = getConfigValue(interfaceClass, "connection-pool-size", Integer.class);
        instance.maxRedirects = getConfigValue(interfaceClass, "max-redirects", Integer.class);
        instance.headers = getConfigValues(interfaceClass, "headers", String.class, String.class);
        instance.shared = getConfigValue(interfaceClass, "shared", Boolean.class);
        instance.name = getConfigValue(interfaceClass, "name", String.class);
        return instance;
    }

    private static <T> Optional<T> getConfigValue(String configKey, String fieldName, Class<T> type) {
        final Config config = ConfigProvider.getConfig();
        Optional<T> optional = config.getOptionalValue(composePropertyKey(configKey, fieldName), type);
        if (optional.isEmpty()) {
            optional = config.getOptionalValue(composePropertyKey('"' + configKey + '"', fieldName), type);
        }
        return optional;
    }

    private static <T> Optional<T> getConfigValue(Class<?> clientInterface, String fieldName, Class<T> type) {
        final Config config = ConfigProvider.getConfig();
        Optional<T> optional = config.getOptionalValue(composePropertyKey('"' + clientInterface.getName() + '"', fieldName), type);
        if (optional.isEmpty()) {
            optional = config.getOptionalValue(composePropertyKey(clientInterface.getSimpleName(), fieldName), type);
        }
        if (optional.isEmpty()) {
            optional = config.getOptionalValue(composePropertyKey('"' + clientInterface.getSimpleName() + '"', fieldName), type);
        }
        return optional;
    }

    private static <K, V> Map<K, V> getConfigValues(String configKey, String fieldName, Class<K> keyType, Class<V> valueType) {
        final SmallRyeConfig config = (SmallRyeConfig) ConfigProvider.getConfig();
        Optional<Map<K, V>> optional = config.getOptionalValues(composePropertyKey(configKey, fieldName), keyType, valueType);
        if (optional.isEmpty()) {
            optional = config.getOptionalValues(composePropertyKey('"' + configKey + '"', fieldName), keyType, valueType);
        }
        return optional.isPresent() ? optional.get() : Collections.emptyMap();
    }

    private static <K, V> Map<K, V> getConfigValues(Class<?> clientInterface, String fieldName, Class<K> keyType, Class<V> valueType) {
        final SmallRyeConfig config = (SmallRyeConfig) ConfigProvider.getConfig();
        Optional<Map<K, V>> optional = config.getOptionalValues(composePropertyKey('"' + clientInterface.getName() + '"', fieldName), keyType, valueType);
        if (optional.isEmpty()) {
            optional = config.getOptionalValues(composePropertyKey(clientInterface.getSimpleName(), fieldName), keyType, valueType);
        }
        if (optional.isEmpty()) {
            optional = config.getOptionalValues(composePropertyKey('"' + clientInterface.getSimpleName() + '"', fieldName), keyType, valueType);
        }
        return optional.isPresent() ? optional.get() : Collections.emptyMap();
    }

    private static String composePropertyKey(String key, String fieldName) {
        return Constants.QUARKUS_CONFIG_PREFIX + key + "." + fieldName;
    }
}
