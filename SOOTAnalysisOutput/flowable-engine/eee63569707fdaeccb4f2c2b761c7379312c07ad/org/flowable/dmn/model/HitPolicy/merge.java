package org.flowable.dmn.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum HitPolicy {

    UNIQUE("UNIQUE"),
    FIRST("FIRST"),
    PRIORITY("PRIORITY"),
    ANY("ANY"),
    UNORDERED("UNORDERED"),
    RULE_ORDER("RULE ORDER"),
    OUTPUT_ORDER("OUTPUT ORDER"),
    COLLECT("COLLECT");

    private static final Map<String, HitPolicy> lookup = new HashMap<>();

    static {
        for (HitPolicy hitPolicy : EnumSet.allOf(HitPolicy.class)) lookup.put(hitPolicy.getValue(), hitPolicy);
    }

    private static final Map<String, HitPolicy> lookup = new HashMap<>();

    static {
        for (HitPolicy hitPolicy : EnumSet.allOf(HitPolicy.class)) lookup.put(hitPolicy.getValue(), hitPolicy);
    }

    private final String value;

    HitPolicy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HitPolicy get(String value) {
        return lookup.get(value);
    }
}
