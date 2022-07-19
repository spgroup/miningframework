package com.sun.xml.internal.ws.config.management.policy;

import com.sun.xml.internal.ws.policy.PolicyConstants;
import com.sun.xml.internal.ws.policy.spi.PrefixMapper;
import java.util.HashMap;
import java.util.Map;

public class ManagementPrefixMapper implements PrefixMapper {

    private static final Map<String, String> prefixMap = new HashMap<String, String>();

    static {
        prefixMap.put(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "sunman");
    }

    public Map<String, String> getPrefixMap() {
        return prefixMap;
    }
}
