package org.flowable.common.engine.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flowable.common.engine.api.FlowableException;

public class FlowableVersions {

<<<<<<< MINE
    public static final String CURRENT_VERSION = "6.4.1.3";
=======
    public static final String CURRENT_VERSION = "6.4.1.1";
>>>>>>> YOURS

    public static final List<FlowableVersion> FLOWABLE_VERSIONS = new ArrayList<>();

    public static final String LAST_V5_VERSION = "5.99.0.0";

    public static final String LAST_V6_VERSION_BEFORE_SERVICES = "6.1.2.0";

    static {
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.7"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.8"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.9"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.10"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.11"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.12", Arrays.asList("5.12.1", "5.12T")));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.13"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.14"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.15"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.15.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16.2-SNAPSHOT"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16.2"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16.3.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.16.4.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.17.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.17.0.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.17.0.2"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.18.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.18.0.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.20.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.20.0.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.20.0.2"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.21.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.22.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.23.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("5.24.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion(LAST_V5_VERSION));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.2"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.3"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.4"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.0.5"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.0.1.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.1.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.1.1.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion(LAST_V6_VERSION_BEFORE_SERVICES));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.2.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.2.1.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.3.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.3.0.1"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.3.1.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.3.2.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.4.0.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion("6.4.1.0"));
        FLOWABLE_VERSIONS.add(new FlowableVersion(CURRENT_VERSION));
    }

    public static int findMatchingVersionIndex(FlowableVersion flowableVersion) {
        return findMatchingVersionIndex(flowableVersion.mainVersion);
    }

    public static int findMatchingVersionIndex(String dbVersion) {
        int index = 0;
        int matchingVersionIndex = -1;
        while (matchingVersionIndex < 0 && index < FlowableVersions.FLOWABLE_VERSIONS.size()) {
            if (FlowableVersions.FLOWABLE_VERSIONS.get(index).matches(dbVersion)) {
                matchingVersionIndex = index;
            } else {
                index++;
            }
        }
        return matchingVersionIndex;
    }

    public static FlowableVersion getPreviousVersion(String version) {
        int currentVersion = findMatchingVersionIndex(version);
        if (currentVersion > 0) {
            return FLOWABLE_VERSIONS.get(currentVersion - 1);
        }
        return null;
    }

    public static int getFlowableVersionIndexForDbVersion(String dbVersion) {
        int matchingVersionIndex;
        matchingVersionIndex = findMatchingVersionIndex(dbVersion);
        if (matchingVersionIndex < 0 && dbVersion != null && dbVersion.startsWith("5.")) {
            matchingVersionIndex = findMatchingVersionIndex(FlowableVersions.LAST_V5_VERSION);
        }
        if (matchingVersionIndex < 0) {
            throw new FlowableException("Could not update Flowable database schema: unknown version from database: '" + dbVersion + "'");
        }
        return matchingVersionIndex;
    }
}
