package org.flowable.dmn.model;

public enum HitPolicy {

    UNIQUE("UNIQUE"),
    FIRST("FIRST"),
    PRIORITY("PRIORITY"),
    ANY("ANY"),
    UNORDERED("UNORDERED"),
    RULE_ORDER("RULE ORDER"),
    OUTPUT_ORDER("OUTPUT ORDER");

    private final String value;

    HitPolicy(String value) {
        this.value = value;
    }
}
