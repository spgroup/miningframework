package com.sun.tools.javac.code;

import java.util.EnumSet;
import java.util.Set;
import static com.sun.tools.javac.code.TargetType.TargetAttribute.*;

public enum TargetType {

    TYPECAST(0x00, IsLocal),
    TYPECAST_GENERIC_OR_ARRAY(0x01, HasLocation, IsLocal),
    INSTANCEOF(0x02, IsLocal),
    INSTANCEOF_GENERIC_OR_ARRAY(0x03, HasLocation, IsLocal),
    NEW(0x04, IsLocal),
    NEW_GENERIC_OR_ARRAY(0x05, HasLocation, IsLocal),
    METHOD_RECEIVER(0x06),
    LOCAL_VARIABLE(0x08, IsLocal),
    LOCAL_VARIABLE_GENERIC_OR_ARRAY(0x09, HasLocation, IsLocal),
    METHOD_RETURN_GENERIC_OR_ARRAY(0x0B, HasLocation),
    METHOD_PARAMETER_GENERIC_OR_ARRAY(0x0D, HasLocation),
    FIELD_GENERIC_OR_ARRAY(0x0F, HasLocation),
    CLASS_TYPE_PARAMETER_BOUND(0x10, HasBound, HasParameter),
    CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY(0x11, HasBound, HasLocation, HasParameter),
    METHOD_TYPE_PARAMETER_BOUND(0x12, HasBound, HasParameter),
    METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY(0x13, HasBound, HasLocation, HasParameter),
    CLASS_EXTENDS(0x14),
    CLASS_EXTENDS_GENERIC_OR_ARRAY(0x15, HasLocation),
    THROWS(0x16),
    NEW_TYPE_ARGUMENT(0x18, IsLocal),
    NEW_TYPE_ARGUMENT_GENERIC_OR_ARRAY(0x19, HasLocation, IsLocal),
    METHOD_TYPE_ARGUMENT(0x1A, IsLocal),
    METHOD_TYPE_ARGUMENT_GENERIC_OR_ARRAY(0x1B, HasLocation, IsLocal),
    WILDCARD_BOUND(0x1C, HasBound),
    WILDCARD_BOUND_GENERIC_OR_ARRAY(0x1D, HasBound, HasLocation),
    CLASS_LITERAL(0x1E, IsLocal),
    CLASS_LITERAL_GENERIC_OR_ARRAY(0x1F, HasLocation, IsLocal),
    METHOD_TYPE_PARAMETER(0x20, HasParameter),
    CLASS_TYPE_PARAMETER(0x22, HasParameter),
    UNKNOWN(-1);

    static final int MAXIMUM_TARGET_TYPE_VALUE = 0x22;

    private final int targetTypeValue;

    private final Set<TargetAttribute> flags;

    TargetType(int targetTypeValue, TargetAttribute... attributes) {
        if (targetTypeValue < Byte.MIN_VALUE || targetTypeValue > Byte.MAX_VALUE)
            throw new AssertionError("attribute type value needs to be a byte: " + targetTypeValue);
        this.targetTypeValue = (byte) targetTypeValue;
        flags = EnumSet.noneOf(TargetAttribute.class);
        for (TargetAttribute attr : attributes) flags.add(attr);
    }

    public boolean hasLocation() {
        return flags.contains(HasLocation);
    }

    public TargetType getGenericComplement() {
        if (hasLocation())
            return this;
        else
            return fromTargetTypeValue(targetTypeValue() + 1);
    }

    public boolean hasParameter() {
        return flags.contains(HasParameter);
    }

    public boolean hasBound() {
        return flags.contains(HasBound);
    }

    public boolean isLocal() {
        return flags.contains(IsLocal);
    }

    public int targetTypeValue() {
        return this.targetTypeValue;
    }

    private static final TargetType[] targets;

    static {
        targets = new TargetType[MAXIMUM_TARGET_TYPE_VALUE + 1];
        TargetType[] alltargets = values();
        for (TargetType target : alltargets) {
            if (target.targetTypeValue >= 0)
                targets[target.targetTypeValue] = target;
        }
        for (int i = 0; i <= MAXIMUM_TARGET_TYPE_VALUE; ++i) {
            if (targets[i] == null)
                targets[i] = UNKNOWN;
        }
    }

    public static boolean isValidTargetTypeValue(int tag) {
        if (((byte) tag) == ((byte) UNKNOWN.targetTypeValue))
            return true;
        return (tag >= 0 && tag < targets.length);
    }

    public static TargetType fromTargetTypeValue(int tag) {
        if (((byte) tag) == ((byte) UNKNOWN.targetTypeValue))
            return UNKNOWN;
        if (tag < 0 || tag >= targets.length)
            throw new IllegalArgumentException("Unknown TargetType: " + tag);
        return targets[tag];
    }

    static enum TargetAttribute {

        HasLocation, HasParameter, HasBound, IsLocal
    }
}
