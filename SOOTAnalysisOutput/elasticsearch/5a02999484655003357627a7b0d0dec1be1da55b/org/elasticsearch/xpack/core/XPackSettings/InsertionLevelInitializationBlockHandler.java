package org.elasticsearch.xpack.core;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.xpack.core.security.SecurityField;
import org.elasticsearch.xpack.core.security.authc.support.Hasher;
import org.elasticsearch.xpack.core.ssl.SSLClientAuth;
import org.elasticsearch.xpack.core.ssl.SSLConfigurationSettings;
import org.elasticsearch.xpack.core.ssl.VerificationMode;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import static org.elasticsearch.xpack.core.security.SecurityField.USER_SETTING;

public class XPackSettings {

    private XPackSettings() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static final Setting<Boolean> ENRICH_ENABLED_SETTING = Setting.boolSetting("xpack.enrich.enabled", true, Property.NodeScope);

    public static final Setting<Boolean> CCR_ENABLED_SETTING = Setting.boolSetting("xpack.ccr.enabled", true, Property.NodeScope);

    public static final Setting<Boolean> DATA_FRAME_ENABLED = Setting.boolSetting("xpack.data_frame.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> SECURITY_ENABLED = Setting.boolSetting("xpack.security.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> MONITORING_ENABLED = Setting.boolSetting("xpack.monitoring.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> WATCHER_ENABLED = Setting.boolSetting("xpack.watcher.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> GRAPH_ENABLED = Setting.boolSetting("xpack.graph.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> MACHINE_LEARNING_ENABLED = Setting.boolSetting("xpack.ml.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> ROLLUP_ENABLED = Setting.boolSetting("xpack.rollup.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> AUDIT_ENABLED = Setting.boolSetting("xpack.security.audit.enabled", false, Setting.Property.NodeScope);

    public static final Setting<Boolean> DLS_FLS_ENABLED = Setting.boolSetting("xpack.security.dls_fls.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> LOGSTASH_ENABLED = Setting.boolSetting("xpack.logstash.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> BEATS_ENABLED = Setting.boolSetting("xpack.beats.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> INDEX_LIFECYCLE_ENABLED = Setting.boolSetting("xpack.ilm.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> TRANSPORT_SSL_ENABLED = Setting.boolSetting("xpack.security.transport.ssl.enabled", false, Property.NodeScope);

    public static final Setting<Boolean> HTTP_SSL_ENABLED = Setting.boolSetting("xpack.security.http.ssl.enabled", false, Setting.Property.NodeScope);

    public static final Setting<Boolean> RESERVED_REALM_ENABLED_SETTING = Setting.boolSetting("xpack.security.authc.reserved_realm.enabled", true, Setting.Property.NodeScope);

    public static final Setting<Boolean> TOKEN_SERVICE_ENABLED_SETTING = Setting.boolSetting("xpack.security.authc.token.enabled", XPackSettings.HTTP_SSL_ENABLED::getRaw, Setting.Property.NodeScope);

    public static final Setting<Boolean> API_KEY_SERVICE_ENABLED_SETTING = Setting.boolSetting("xpack.security.authc.api_key.enabled", XPackSettings.HTTP_SSL_ENABLED::getRaw, Setting.Property.NodeScope);

    public static final Setting<Boolean> FIPS_MODE_ENABLED = Setting.boolSetting("xpack.security.fips_mode.enabled", false, Property.NodeScope);

    public static final Setting<Boolean> SQL_ENABLED = Setting.boolSetting("xpack.sql.enabled", true, Setting.Property.NodeScope);

    public static final List<String> DEFAULT_CIPHERS = List.of("TLS_AES_256_GCM_SHA384", "TLS_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_GCM_SHA384", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_256_CBC_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA");

    public static final Setting<String> PASSWORD_HASHING_ALGORITHM = new Setting<>("xpack.security.authc.password_hashing.algorithm", "bcrypt", Function.identity(), v -> {
        if (Hasher.getAvailableAlgoStoredHash().contains(v.toLowerCase(Locale.ROOT)) == false) {
            throw new IllegalArgumentException("Invalid algorithm: " + v + ". Valid values for password hashing are " + Hasher.getAvailableAlgoStoredHash().toString());
        } else if (v.regionMatches(true, 0, "pbkdf2", 0, "pbkdf2".length())) {
            try {
                SecretKeyFactory.getInstance("PBKDF2withHMACSHA512");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Support for PBKDF2WithHMACSHA512 must be available in order to use any of the " + "PBKDF2 algorithms for the [xpack.security.authc.password_hashing.algorithm] setting.", e);
            }
        }
    }, Setting.Property.NodeScope);

    public static final List<String> DEFAULT_SUPPORTED_PROTOCOLS = List.of("TLSv1.3", "TLSv1.2", "TLSv1.1");

    public static final SSLClientAuth CLIENT_AUTH_DEFAULT = SSLClientAuth.REQUIRED;

    public static final SSLClientAuth HTTP_CLIENT_AUTH_DEFAULT = SSLClientAuth.NONE;

    public static final VerificationMode VERIFICATION_MODE_DEFAULT = VerificationMode.FULL;

    public static final String HTTP_SSL_PREFIX = SecurityField.setting("http.ssl.");

    private static final SSLConfigurationSettings HTTP_SSL = SSLConfigurationSettings.withPrefix(HTTP_SSL_PREFIX);

    public static final String TRANSPORT_SSL_PREFIX = SecurityField.setting("transport.ssl.");

    private static final SSLConfigurationSettings TRANSPORT_SSL = SSLConfigurationSettings.withPrefix(TRANSPORT_SSL_PREFIX);

    public static List<Setting<?>> getAllSettings() {
        ArrayList<Setting<?>> settings = new ArrayList<>();
        settings.addAll(HTTP_SSL.getAllSettings());
        settings.addAll(TRANSPORT_SSL.getAllSettings());
        settings.add(SECURITY_ENABLED);
        settings.add(MONITORING_ENABLED);
        settings.add(GRAPH_ENABLED);
        settings.add(MACHINE_LEARNING_ENABLED);
        settings.add(AUDIT_ENABLED);
        settings.add(WATCHER_ENABLED);
        settings.add(DLS_FLS_ENABLED);
        settings.add(LOGSTASH_ENABLED);
        settings.add(TRANSPORT_SSL_ENABLED);
        settings.add(HTTP_SSL_ENABLED);
        settings.add(RESERVED_REALM_ENABLED_SETTING);
        settings.add(TOKEN_SERVICE_ENABLED_SETTING);
        settings.add(API_KEY_SERVICE_ENABLED_SETTING);
        settings.add(SQL_ENABLED);
        settings.add(USER_SETTING);
        settings.add(ROLLUP_ENABLED);
        settings.add(PASSWORD_HASHING_ALGORITHM);
        settings.add(INDEX_LIFECYCLE_ENABLED);
        settings.add(DATA_FRAME_ENABLED);
        return Collections.unmodifiableList(settings);
    }
}