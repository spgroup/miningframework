package org.graalvm.compiler.core.common.type;

import org.graalvm.compiler.debug.GraalError;
import jdk.vm.ci.code.CodeUtil;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.Signature;

public class StampFactory {

    static final class NodeIntrinsicStamp extends ObjectStamp {

        protected static final Stamp SINGLETON = new NodeIntrinsicStamp();

        private NodeIntrinsicStamp() {
            super(null, false, false, false);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }

    private static final Stamp[] stampCache = new Stamp[JavaKind.values().length];

    private static final Stamp[] emptyStampCache = new Stamp[JavaKind.values().length];

    private static final Stamp objectStamp = new ObjectStamp(null, false, false, false);

    private static final Stamp objectNonNullStamp = new ObjectStamp(null, false, true, false);

    private static final Stamp objectAlwaysNullStamp = new ObjectStamp(null, false, false, true);

    private static final Stamp positiveInt = forInteger(JavaKind.Int, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

    private static final Stamp booleanTrue = forInteger(JavaKind.Boolean, -1, -1, 1, 1);

    private static final Stamp booleanFalse = forInteger(JavaKind.Boolean, 0, 0, 0, 0);

    private static final Stamp rawPointer = new RawPointerStamp();

    private static void setCache(JavaKind kind, Stamp stamp) {
        stampCache[kind.ordinal()] = stamp;
    }

    private static void setIntCache(JavaKind kind) {
        int bits = kind.getStackKind().getBitCount();
        long mask;
        if (kind.isUnsigned()) {
            mask = CodeUtil.mask(kind.getBitCount());
        } else {
            mask = CodeUtil.mask(bits);
        }
        setCache(kind, new IntegerStamp(bits, kind.getMinValue(), kind.getMaxValue(), 0, mask));
    }

    private static void setFloatCache(JavaKind kind) {
        setCache(kind, new FloatStamp(kind.getBitCount()));
    }

    static {
        setIntCache(JavaKind.Boolean);
        setIntCache(JavaKind.Byte);
        setIntCache(JavaKind.Short);
        setIntCache(JavaKind.Char);
        setIntCache(JavaKind.Int);
        setIntCache(JavaKind.Long);
        setFloatCache(JavaKind.Float);
        setFloatCache(JavaKind.Double);
        setCache(JavaKind.Object, objectStamp);
        setCache(JavaKind.Void, VoidStamp.getInstance());
        setCache(JavaKind.Illegal, IllegalStamp.getInstance());
        for (JavaKind k : JavaKind.values()) {
            if (stampCache[k.ordinal()] != null) {
                emptyStampCache[k.ordinal()] = stampCache[k.ordinal()].empty();
            }
        }
    }

    public static Stamp tautology() {
        return booleanTrue;
    }

    public static Stamp contradiction() {
        return booleanFalse;
    }

    public static Stamp forKind(JavaKind kind) {
        assert stampCache[kind.ordinal()] != null : "unexpected forKind(" + kind + ")";
        return stampCache[kind.ordinal()];
    }

    public static Stamp forVoid() {
        return VoidStamp.getInstance();
    }

    public static Stamp forNodeIntrinsic() {
        return NodeIntrinsicStamp.SINGLETON;
    }

    public static Stamp intValue() {
        return forKind(JavaKind.Int);
    }

    public static Stamp positiveInt() {
        return positiveInt;
    }

    public static Stamp empty(JavaKind kind) {
        return emptyStampCache[kind.ordinal()];
    }

    public static IntegerStamp forInteger(JavaKind kind, long lowerBound, long upperBound, long downMask, long upMask) {
        return new IntegerStamp(kind.getBitCount(), lowerBound, upperBound, downMask, upMask);
    }

    public static IntegerStamp forInteger(JavaKind kind, long lowerBound, long upperBound) {
        return forInteger(kind.getBitCount(), lowerBound, upperBound);
    }

    public static IntegerStamp forIntegerWithMask(int bits, long newLowerBound, long newUpperBound, IntegerStamp maskStamp) {
        IntegerStamp limit = StampFactory.forInteger(bits, newLowerBound, newUpperBound);
        return new IntegerStamp(bits, newLowerBound, newUpperBound, limit.downMask() | maskStamp.downMask(), limit.upMask() & maskStamp.upMask());
    }

    public static IntegerStamp forIntegerWithMask(int bits, long newLowerBound, long newUpperBound, long newDownMask, long newUpMask) {
        IntegerStamp limit = StampFactory.forInteger(bits, newLowerBound, newUpperBound);
        return new IntegerStamp(bits, newLowerBound, newUpperBound, limit.downMask() | newDownMask, limit.upMask() & newUpMask);
    }

    public static IntegerStamp forInteger(int bits) {
        return new IntegerStamp(bits, CodeUtil.minValue(bits), CodeUtil.maxValue(bits), 0, CodeUtil.mask(bits));
    }

    public static IntegerStamp forInteger(int bits, long lowerBound, long upperBound) {
        long defaultMask = CodeUtil.mask(bits);
        if (lowerBound == upperBound) {
            return new IntegerStamp(bits, lowerBound, lowerBound, lowerBound & defaultMask, lowerBound & defaultMask);
        }
        final long downMask;
        final long upMask;
        if (lowerBound >= 0) {
            int upperBoundLeadingZeros = Long.numberOfLeadingZeros(upperBound);
            long differentBits = lowerBound ^ upperBound;
            int sameBitCount = Long.numberOfLeadingZeros(differentBits << upperBoundLeadingZeros);
            upMask = upperBound | -1L >>> (upperBoundLeadingZeros + sameBitCount);
            downMask = upperBound & ~(-1L >>> (upperBoundLeadingZeros + sameBitCount));
        } else {
            if (upperBound >= 0) {
                upMask = defaultMask;
                downMask = 0;
            } else {
                int lowerBoundLeadingOnes = Long.numberOfLeadingZeros(~lowerBound);
                long differentBits = lowerBound ^ upperBound;
                int sameBitCount = Long.numberOfLeadingZeros(differentBits << lowerBoundLeadingOnes);
                upMask = lowerBound | -1L >>> (lowerBoundLeadingOnes + sameBitCount) | ~(-1L >>> lowerBoundLeadingOnes);
                downMask = lowerBound & ~(-1L >>> (lowerBoundLeadingOnes + sameBitCount)) | ~(-1L >>> lowerBoundLeadingOnes);
            }
        }
        return new IntegerStamp(bits, lowerBound, upperBound, downMask & defaultMask, upMask & defaultMask);
    }

    public static FloatStamp forFloat(JavaKind kind, double lowerBound, double upperBound, boolean nonNaN) {
        assert kind.isNumericFloat();
        return new FloatStamp(kind.getBitCount(), lowerBound, upperBound, nonNaN);
    }

    public static Stamp forConstant(JavaConstant value) {
        JavaKind kind = value.getJavaKind();
        switch(kind) {
            case Boolean:
            case Byte:
            case Char:
            case Short:
            case Int:
            case Long:
                long mask = value.asLong() & CodeUtil.mask(kind.getBitCount());
                return forInteger(kind.getStackKind(), value.asLong(), value.asLong(), mask, mask);
            case Float:
                return forFloat(kind, value.asFloat(), value.asFloat(), !Float.isNaN(value.asFloat()));
            case Double:
                return forFloat(kind, value.asDouble(), value.asDouble(), !Double.isNaN(value.asDouble()));
            case Illegal:
                return forKind(JavaKind.Illegal);
            case Object:
                if (value.isNull()) {
                    return alwaysNull();
                } else {
                    return objectNonNull();
                }
            default:
                throw new GraalError("unexpected kind: %s", kind);
        }
    }

    public static Stamp forConstant(JavaConstant value, MetaAccessProvider metaAccess) {
        if (value.getJavaKind() == JavaKind.Object) {
            ResolvedJavaType type = value.isNull() ? null : metaAccess.lookupJavaType(value);
            return new ObjectStamp(type, value.isNonNull(), value.isNonNull(), value.isNull());
        } else {
            return forConstant(value);
        }
    }

    public static Stamp object() {
        return objectStamp;
    }

    public static Stamp objectNonNull() {
        return objectNonNullStamp;
    }

    public static Stamp alwaysNull() {
        return objectAlwaysNullStamp;
    }

    public static ObjectStamp object(TypeReference type) {
        return object(type, false);
    }

    public static ObjectStamp objectNonNull(TypeReference type) {
        return object(type, true);
    }

    public static ObjectStamp object(TypeReference type, boolean nonNull) {
        if (type == null) {
            return new ObjectStamp(null, false, nonNull, false);
        } else {
            return new ObjectStamp(type.getType(), type.isExact(), nonNull, false);
        }
    }

    public static Stamp[] createParameterStamps(Assumptions assumptions, ResolvedJavaMethod method) {
        Signature sig = method.getSignature();
        Stamp[] result = new Stamp[sig.getParameterCount(!method.isStatic())];
        int index = 0;
        if (!method.isStatic()) {
            result[index++] = StampFactory.objectNonNull(TypeReference.create(assumptions, method.getDeclaringClass()));
        }
        int max = sig.getParameterCount(false);
        ResolvedJavaType accessingClass = method.getDeclaringClass();
        for (int i = 0; i < max; i++) {
            JavaType type = sig.getParameterType(i, accessingClass);
            JavaKind kind = type.getJavaKind();
            Stamp stamp;
            if (kind == JavaKind.Object && type instanceof ResolvedJavaType) {
                stamp = StampFactory.object(TypeReference.create(assumptions, (ResolvedJavaType) type));
            } else {
                stamp = StampFactory.forKind(kind);
            }
            result[index++] = stamp;
        }
        return result;
    }

    public static Stamp pointer() {
        return rawPointer;
    }

    public static StampPair forDeclaredType(Assumptions assumptions, JavaType returnType, boolean nonNull) {
        if (returnType.getJavaKind() == JavaKind.Object && returnType instanceof ResolvedJavaType) {
            ResolvedJavaType resolvedJavaType = (ResolvedJavaType) returnType;
            TypeReference reference = TypeReference.create(assumptions, resolvedJavaType);
            if (resolvedJavaType.isInterface()) {
                ResolvedJavaType implementor = resolvedJavaType.getSingleImplementor();
                if (implementor != null && !resolvedJavaType.equals(implementor)) {
                    TypeReference uncheckedType = TypeReference.createTrusted(assumptions, implementor);
                    return StampPair.create(StampFactory.object(reference, nonNull), StampFactory.object(uncheckedType, nonNull));
                }
            }
            return StampPair.createSingle(StampFactory.object(reference, nonNull));
        } else {
            return StampPair.createSingle(StampFactory.forKind(returnType.getJavaKind()));
        }
    }
}