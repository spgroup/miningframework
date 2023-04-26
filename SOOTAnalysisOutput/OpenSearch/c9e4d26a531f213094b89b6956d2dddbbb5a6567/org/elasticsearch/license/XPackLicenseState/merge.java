package org.elasticsearch.license;

import org.apache.logging.log4j.LogManager;
import org.elasticsearch.Version;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.LoggerMessageFormat;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.license.License.OperationMode;
import org.elasticsearch.xpack.core.XPackField;
import org.elasticsearch.xpack.core.XPackSettings;
import org.elasticsearch.xpack.core.monitoring.MonitoringField;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

public class XPackLicenseState {

    static final Map<String, String[]> EXPIRATION_MESSAGES;

    static {
        Map<String, String[]> messages = new LinkedHashMap<>();
        messages.put(XPackField.SECURITY, new String[] { "Cluster health, cluster stats and indices stats operations are blocked", "All data operations (read and write) continue to work" });
        messages.put(XPackField.WATCHER, new String[] { "PUT / GET watch APIs are disabled, DELETE watch API continues to work", "Watches execute and write to the history", "The actions of the watches don't execute" });
        messages.put(XPackField.MONITORING, new String[] { "The agent will stop collecting cluster and indices metrics", "The agent will stop automatically cleaning indices older than [xpack.monitoring.history.duration]" });
        messages.put(XPackField.GRAPH, new String[] { "Graph explore APIs are disabled" });
        messages.put(XPackField.MACHINE_LEARNING, new String[] { "Machine learning APIs are disabled" });
        messages.put(XPackField.LOGSTASH, new String[] { "Logstash will continue to poll centrally-managed pipelines" });
        messages.put(XPackField.BEATS, new String[] { "Beats will continue to poll centrally-managed configuration" });
        messages.put(XPackField.DEPRECATION, new String[] { "Deprecation APIs are disabled" });
        messages.put(XPackField.UPGRADE, new String[] { "Upgrade API is disabled" });
        messages.put(XPackField.SQL, new String[] { "SQL support is disabled" });
        messages.put(XPackField.ROLLUP, new String[] { "Creating and Starting rollup jobs will no longer be allowed.", "Stopping/Deleting existing jobs, RollupCaps API and RollupSearch continue to function." });
        EXPIRATION_MESSAGES = Collections.unmodifiableMap(messages);
    }

    static final Map<String, BiFunction<OperationMode, OperationMode, String[]>> ACKNOWLEDGMENT_MESSAGES;

    static {
        Map<String, BiFunction<OperationMode, OperationMode, String[]>> messages = new LinkedHashMap<>();
        messages.put(XPackField.SECURITY, XPackLicenseState::securityAcknowledgementMessages);
        messages.put(XPackField.WATCHER, XPackLicenseState::watcherAcknowledgementMessages);
        messages.put(XPackField.MONITORING, XPackLicenseState::monitoringAcknowledgementMessages);
        messages.put(XPackField.GRAPH, XPackLicenseState::graphAcknowledgementMessages);
        messages.put(XPackField.MACHINE_LEARNING, XPackLicenseState::machineLearningAcknowledgementMessages);
        messages.put(XPackField.LOGSTASH, XPackLicenseState::logstashAcknowledgementMessages);
        messages.put(XPackField.BEATS, XPackLicenseState::beatsAcknowledgementMessages);
        messages.put(XPackField.SQL, XPackLicenseState::sqlAcknowledgementMessages);
        ACKNOWLEDGMENT_MESSAGES = Collections.unmodifiableMap(messages);
    }

    private static String[] securityAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
                switch(currentMode) {
                    case TRIAL:
                    case STANDARD:
                    case GOLD:
                    case PLATINUM:
                        return new String[] { "The following X-Pack security functionality will be disabled: authentication, authorization, " + "ip filtering, and auditing. Please restart your node after applying the license.", "Field and document level access control will be disabled.", "Custom realms will be ignored." };
                }
                break;
            case GOLD:
                switch(currentMode) {
                    case BASIC:
                    case STANDARD:
                    case TRIAL:
                    case PLATINUM:
                        return new String[] { "Field and document level access control will be disabled.", "Custom realms will be ignored." };
                }
                break;
            case STANDARD:
                switch(currentMode) {
                    case BASIC:
                    case GOLD:
                    case PLATINUM:
                    case TRIAL:
                        return new String[] { "Authentication will be limited to the native realms.", "IP filtering and auditing will be disabled.", "Field and document level access control will be disabled.", "Custom realms will be ignored." };
                }
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] watcherAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
                switch(currentMode) {
                    case TRIAL:
                    case STANDARD:
                    case GOLD:
                    case PLATINUM:
                        return new String[] { "Watcher will be disabled" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] monitoringAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
                switch(currentMode) {
                    case TRIAL:
                    case STANDARD:
                    case GOLD:
                    case PLATINUM:
                        return new String[] { LoggerMessageFormat.format("Multi-cluster support is disabled for clusters with [{}] license. If you are\n" + "running multiple clusters, users won't be able to access the clusters with\n" + "[{}] licenses from within a single X-Pack Kibana instance. You will have to deploy a\n" + "separate and dedicated X-pack Kibana instance for each [{}] cluster you wish to monitor.", newMode, newMode, newMode), LoggerMessageFormat.format("Automatic index cleanup is locked to {} days for clusters with [{}] license.", MonitoringField.HISTORY_DURATION.getDefault(Settings.EMPTY).days(), newMode) };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] graphAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
            case STANDARD:
            case GOLD:
                switch(currentMode) {
                    case TRIAL:
                    case PLATINUM:
                        return new String[] { "Graph will be disabled" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] machineLearningAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
            case STANDARD:
            case GOLD:
                switch(currentMode) {
                    case TRIAL:
                    case PLATINUM:
                        return new String[] { "Machine learning will be disabled" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] logstashAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
                if (isBasic(currentMode) == false) {
                    return new String[] { "Logstash will no longer poll for centrally-managed pipelines" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] beatsAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
                if (isBasic(currentMode) == false) {
                    return new String[] { "Beats will no longer be able to use centrally-managed configuration" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static String[] sqlAcknowledgementMessages(OperationMode currentMode, OperationMode newMode) {
        switch(newMode) {
            case BASIC:
            case STANDARD:
            case GOLD:
                switch(currentMode) {
                    case TRIAL:
                    case PLATINUM:
                        return new String[] { "JDBC and ODBC support will be disabled, but you can continue to use SQL CLI and REST endpoint" };
                }
                break;
        }
        return Strings.EMPTY_ARRAY;
    }

    private static boolean isBasic(OperationMode mode) {
        return mode == OperationMode.BASIC;
    }

    private static class Status {

        final OperationMode mode;

        final boolean active;

        Status(OperationMode mode, boolean active) {
            this.mode = mode;
            this.active = active;
        }
    }

    private final List<Runnable> listeners;

    private final boolean isSecurityEnabled;

    private final boolean isSecurityExplicitlyEnabled;

    private Status status = new Status(OperationMode.TRIAL, true);

    private boolean isSecurityEnabledByTrialVersion;

    public XPackLicenseState(Settings settings) {
        this.listeners = new CopyOnWriteArrayList<>();
        this.isSecurityEnabled = XPackSettings.SECURITY_ENABLED.get(settings);
        this.isSecurityExplicitlyEnabled = isSecurityEnabled && (settings.hasValue(XPackSettings.SECURITY_ENABLED.getKey()) || XPackSettings.TRANSPORT_SSL_ENABLED.get(settings));
        this.isSecurityEnabledByTrialVersion = false;
    }

    private XPackLicenseState(XPackLicenseState xPackLicenseState) {
        this.listeners = xPackLicenseState.listeners;
        this.isSecurityEnabled = xPackLicenseState.isSecurityEnabled;
        this.isSecurityExplicitlyEnabled = xPackLicenseState.isSecurityExplicitlyEnabled;
        this.status = xPackLicenseState.status;
        this.isSecurityEnabledByTrialVersion = xPackLicenseState.isSecurityEnabledByTrialVersion;
    }

    void update(OperationMode mode, boolean active, @Nullable Version mostRecentTrialVersion) {
        synchronized (this) {
            status = new Status(mode, active);
            if (isSecurityEnabled == true && isSecurityExplicitlyEnabled == false && mode == OperationMode.TRIAL && isSecurityEnabledByTrialVersion == false) {
                if (mostRecentTrialVersion == null || mostRecentTrialVersion.before(Version.V_6_3_0)) {
                    LogManager.getLogger(getClass()).info("Automatically enabling security for older trial license ({})", mostRecentTrialVersion == null ? "[pre 6.1.0]" : mostRecentTrialVersion.toString());
                    isSecurityEnabledByTrialVersion = true;
                }
            }
        }
        listeners.forEach(Runnable::run);
    }

    public void addListener(Runnable runnable) {
        listeners.add(Objects.requireNonNull(runnable));
    }

    public void removeListener(Runnable runnable) {
        listeners.remove(runnable);
    }

    public synchronized OperationMode getOperationMode() {
        return status.mode;
    }

    public synchronized boolean isActive() {
        return status.active;
    }

    public synchronized boolean isAuthAllowed() {
        OperationMode mode = status.mode;
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (mode == OperationMode.STANDARD || mode == OperationMode.GOLD || mode == OperationMode.PLATINUM || mode == OperationMode.TRIAL);
    }

    public synchronized boolean isIpFilteringAllowed() {
        OperationMode mode = status.mode;
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (mode == OperationMode.GOLD || mode == OperationMode.PLATINUM || mode == OperationMode.TRIAL);
    }

    public synchronized boolean isAuditingAllowed() {
        OperationMode mode = status.mode;
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (mode == OperationMode.GOLD || mode == OperationMode.PLATINUM || mode == OperationMode.TRIAL);
    }

    public synchronized boolean isStatsAndHealthAllowed() {
        return status.active;
    }

    public synchronized boolean isDocumentAndFieldLevelSecurityAllowed() {
        OperationMode mode = status.mode;
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (mode == OperationMode.TRIAL || mode == OperationMode.PLATINUM);
    }

    public enum AllowedRealmType {

        NONE, NATIVE, DEFAULT, ALL
    }

    public synchronized AllowedRealmType allowedRealmType() {
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(status.mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        if (isSecurityCurrentlyEnabled) {
            switch(status.mode) {
                case PLATINUM:
                case TRIAL:
                    return AllowedRealmType.ALL;
                case GOLD:
                    return AllowedRealmType.DEFAULT;
                case STANDARD:
                    return AllowedRealmType.NATIVE;
                default:
                    return AllowedRealmType.NONE;
            }
        } else {
            return AllowedRealmType.NONE;
        }
    }

    public synchronized boolean isCustomRoleProvidersAllowed() {
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(status.mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (status.mode == OperationMode.PLATINUM || status.mode == OperationMode.TRIAL) && status.active;
    }

    public synchronized boolean isAuthorizationRealmAllowed() {
        final boolean isSecurityCurrentlyEnabled = isSecurityEnabled(status.mode, isSecurityExplicitlyEnabled, isSecurityEnabledByTrialVersion, isSecurityEnabled);
        return isSecurityCurrentlyEnabled && (status.mode == OperationMode.PLATINUM || status.mode == OperationMode.TRIAL) && status.active;
    }

    public synchronized boolean isWatcherAllowed() {
        Status localStatus = status;
        if (localStatus.active == false) {
            return false;
        }
        switch(localStatus.mode) {
            case TRIAL:
            case GOLD:
            case PLATINUM:
            case STANDARD:
                return true;
            default:
                return false;
        }
    }

    public synchronized boolean isMonitoringAllowed() {
        return status.active;
    }

    public synchronized boolean isMonitoringClusterAlertsAllowed() {
        return isWatcherAllowed();
    }

    public synchronized boolean isUpdateRetentionAllowed() {
        final OperationMode mode = status.mode;
        return mode != OperationMode.BASIC && mode != OperationMode.MISSING;
    }

    public synchronized boolean isGraphAllowed() {
        Status localStatus = status;
        OperationMode operationMode = localStatus.mode;
        boolean licensed = operationMode == OperationMode.TRIAL || operationMode == OperationMode.PLATINUM;
        return licensed && localStatus.active;
    }

    public synchronized boolean isMachineLearningAllowed() {
        final Status currentStatus = status;
        return currentStatus.active && isMachineLearningAllowedForOperationMode(currentStatus.mode);
    }

    public static boolean isMachineLearningAllowedForOperationMode(final OperationMode operationMode) {
        return isPlatinumOrTrialOperationMode(operationMode);
    }

    public synchronized boolean isRollupAllowed() {
        return status.active;
    }

    public synchronized boolean isLogstashAllowed() {
        Status localStatus = status;
        return localStatus.active && (isBasic(localStatus.mode) == false);
    }

    public synchronized boolean isBeatsAllowed() {
        Status localStatus = status;
        return localStatus.active && (isBasic(localStatus.mode) == false);
    }

    public synchronized boolean isDeprecationAllowed() {
        return status.active;
    }

    public synchronized boolean isUpgradeAllowed() {
        return status.active;
    }

    public boolean isIndexLifecycleAllowed() {
        Status localStatus = status;
        return localStatus.active;
    }

    public synchronized boolean isSqlAllowed() {
        return status.active;
    }

    public synchronized boolean isJdbcAllowed() {
        Status localStatus = status;
        OperationMode operationMode = localStatus.mode;
        boolean licensed = operationMode == OperationMode.TRIAL || operationMode == OperationMode.PLATINUM;
        return licensed && localStatus.active;
    }

    public synchronized boolean isOdbcAllowed() {
        Status localStatus = status;
        OperationMode operationMode = localStatus.mode;
        boolean licensed = operationMode == OperationMode.TRIAL || operationMode == OperationMode.PLATINUM;
        return licensed && localStatus.active;
    }

    public synchronized boolean isTrialLicense() {
        return status.mode == OperationMode.TRIAL;
    }

    public synchronized boolean isSecurityAvailable() {
        OperationMode mode = status.mode;
        return mode == OperationMode.GOLD || mode == OperationMode.PLATINUM || mode == OperationMode.STANDARD || mode == OperationMode.TRIAL;
    }

    public synchronized boolean isSecurityDisabledByTrialLicense() {
        return status.mode == OperationMode.TRIAL && isSecurityEnabled && isSecurityExplicitlyEnabled == false && isSecurityEnabledByTrialVersion == false;
    }

    private static boolean isSecurityEnabled(final OperationMode mode, final boolean isSecurityExplicitlyEnabled, final boolean isSecurityEnabledByTrialVersion, final boolean isSecurityEnabled) {
        return mode == OperationMode.TRIAL ? (isSecurityExplicitlyEnabled || isSecurityEnabledByTrialVersion) : isSecurityEnabled;
    }

    public synchronized boolean isCcrAllowed() {
        final Status currentStatus = status;
        return currentStatus.active && isCcrAllowedForOperationMode(currentStatus.mode);
    }

    public static boolean isCcrAllowedForOperationMode(final OperationMode operationMode) {
        return isPlatinumOrTrialOperationMode(operationMode);
    }

    public static boolean isPlatinumOrTrialOperationMode(final OperationMode operationMode) {
        return operationMode == OperationMode.PLATINUM || operationMode == OperationMode.TRIAL;
    }

    public synchronized XPackLicenseState copyCurrentLicenseState() {
        return new XPackLicenseState(this);
    }
}
