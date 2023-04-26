package jdk.dynalink.beans;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.Namespace;
import jdk.dynalink.Operation;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.beans.GuardedInvocationComponent.ValidationType;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;
import jdk.dynalink.linker.support.Lookup;
import jdk.dynalink.linker.support.TypeUtilities;

class BeanLinker extends AbstractJavaLinker implements TypeBasedGuardingDynamicLinker {

    BeanLinker(final Class<?> clazz) {
        super(clazz, Guards.getClassGuard(clazz), Guards.getInstanceOfGuard(clazz));
        if (clazz.isArray()) {
            setPropertyGetter("length", MethodHandles.arrayLength(clazz), ValidationType.EXACT_CLASS);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            setPropertyGetter("length", GET_COLLECTION_LENGTH, ValidationType.INSTANCE_OF);
        } else if (Map.class.isAssignableFrom(clazz)) {
            setPropertyGetter("length", GET_MAP_LENGTH, ValidationType.INSTANCE_OF);
        }
    }

    @Override
    public boolean canLinkType(final Class<?> type) {
        return type == clazz;
    }

    @Override
    FacetIntrospector createFacetIntrospector() {
        return new BeanIntrospector(clazz);
    }

    @Override
    protected GuardedInvocationComponent getGuardedInvocationComponent(final ComponentLinkRequest req) throws Exception {
        final GuardedInvocationComponent superGic = super.getGuardedInvocationComponent(req);
        if (superGic != null) {
            return superGic;
        }
        if (!req.namespaces.isEmpty()) {
            final Operation op = req.baseOperation;
            final Namespace ns = req.namespaces.get(0);
            if (ns == StandardNamespace.ELEMENT) {
                if (op == StandardOperation.GET) {
                    return getElementGetter(req.popNamespace());
                } else if (op == StandardOperation.SET) {
                    return getElementSetter(req.popNamespace());
                }
            }
        }
        return null;
    }

    @Override
    SingleDynamicMethod getConstructorMethod(final String signature) {
        return null;
    }

    private static final MethodHandle GET_LIST_ELEMENT = Lookup.PUBLIC.findVirtual(List.class, "get", MethodType.methodType(Object.class, int.class));

    private static final MethodHandle GET_MAP_ELEMENT = Lookup.PUBLIC.findVirtual(Map.class, "get", MethodType.methodType(Object.class, Object.class));

    private static final MethodHandle LIST_GUARD = Guards.getInstanceOfGuard(List.class);

    private static final MethodHandle MAP_GUARD = Guards.getInstanceOfGuard(Map.class);

    private static final MethodHandle NULL_GETTER_1;

    private static final MethodHandle NULL_GETTER_2;

    static {
        final MethodHandle constantNull = MethodHandles.constant(Object.class, null);
        NULL_GETTER_1 = dropObjectArguments(constantNull, 1);
        NULL_GETTER_2 = dropObjectArguments(constantNull, 2);
    }

    private static MethodHandle dropObjectArguments(final MethodHandle m, final int n) {
        return MethodHandles.dropArguments(m, 0, Collections.nCopies(n, Object.class));
    }

    private enum CollectionType {

        ARRAY, LIST, MAP
    }

    private GuardedInvocationComponent getElementGetter(final ComponentLinkRequest req) throws Exception {
        final CallSiteDescriptor callSiteDescriptor = req.getDescriptor();
        final Object name = req.name;
        final boolean isFixedKey = name != null;
        assertParameterCount(callSiteDescriptor, isFixedKey ? 1 : 2);
        final LinkerServices linkerServices = req.linkerServices;
        final MethodType callSiteType = callSiteDescriptor.getMethodType();
        final GuardedInvocationComponent nextComponent = getNextComponent(req);
        final GuardedInvocationComponentAndCollectionType gicact = guardedInvocationComponentAndCollectionType(callSiteType, linkerServices, MethodHandles::arrayElementGetter, GET_LIST_ELEMENT, GET_MAP_ELEMENT);
        if (gicact == null) {
            return nextComponent;
        }
        final Object typedName = getTypedName(name, gicact.collectionType == CollectionType.MAP, linkerServices);
        if (typedName == INVALID_NAME) {
            return nextComponent;
        }
        return guardComponentWithRangeCheck(gicact, linkerServices, callSiteDescriptor, nextComponent, new Binder(linkerServices, callSiteType, typedName), isFixedKey ? NULL_GETTER_1 : NULL_GETTER_2);
    }

    private static class GuardedInvocationComponentAndCollectionType {

        final GuardedInvocationComponent gic;

        final CollectionType collectionType;

        GuardedInvocationComponentAndCollectionType(final GuardedInvocationComponent gic, final CollectionType collectionType) {
            this.gic = gic;
            this.collectionType = collectionType;
        }
    }

    private GuardedInvocationComponentAndCollectionType guardedInvocationComponentAndCollectionType(final MethodType callSiteType, final LinkerServices linkerServices, final Function<Class<?>, MethodHandle> arrayMethod, final MethodHandle listMethod, final MethodHandle mapMethod) {
        final Class<?> declaredType = callSiteType.parameterType(0);
        if (declaredType.isArray()) {
            return new GuardedInvocationComponentAndCollectionType(createInternalFilteredGuardedInvocationComponent(arrayMethod.apply(declaredType), linkerServices), CollectionType.ARRAY);
        } else if (List.class.isAssignableFrom(declaredType)) {
            return new GuardedInvocationComponentAndCollectionType(createInternalFilteredGuardedInvocationComponent(listMethod, linkerServices), CollectionType.LIST);
        } else if (Map.class.isAssignableFrom(declaredType)) {
            return new GuardedInvocationComponentAndCollectionType(createInternalFilteredGuardedInvocationComponent(mapMethod, linkerServices), CollectionType.MAP);
        } else if (clazz.isArray()) {
            return new GuardedInvocationComponentAndCollectionType(getClassGuardedInvocationComponent(linkerServices.filterInternalObjects(arrayMethod.apply(clazz)), callSiteType), CollectionType.ARRAY);
        } else if (List.class.isAssignableFrom(clazz)) {
            return new GuardedInvocationComponentAndCollectionType(createInternalFilteredGuardedInvocationComponent(listMethod, Guards.asType(LIST_GUARD, callSiteType), List.class, ValidationType.INSTANCE_OF, linkerServices), CollectionType.LIST);
        } else if (Map.class.isAssignableFrom(clazz)) {
            return new GuardedInvocationComponentAndCollectionType(createInternalFilteredGuardedInvocationComponent(mapMethod, Guards.asType(MAP_GUARD, callSiteType), Map.class, ValidationType.INSTANCE_OF, linkerServices), CollectionType.MAP);
        }
        return null;
    }

    private static final Object INVALID_NAME = new Object();

    private static Object getTypedName(final Object name, final boolean isMap, final LinkerServices linkerServices) throws Exception {
        if (!isMap && name != null) {
            final Integer integer = convertKeyToInteger(name, linkerServices);
            if (integer == null || integer.intValue() < 0) {
                return INVALID_NAME;
            }
            return integer;
        }
        return name;
    }

    private static GuardedInvocationComponent guardComponentWithRangeCheck(final GuardedInvocationComponentAndCollectionType gicact, final LinkerServices linkerServices, final CallSiteDescriptor callSiteDescriptor, final GuardedInvocationComponent nextComponent, final Binder binder, final MethodHandle noOp) {
        final MethodType callSiteType = callSiteDescriptor.getMethodType();
        final MethodHandle checkGuard;
        switch(gicact.collectionType) {
            case LIST:
                checkGuard = convertArgToNumber(RANGE_CHECK_LIST, linkerServices, callSiteDescriptor);
                break;
            case MAP:
                checkGuard = linkerServices.filterInternalObjects(CONTAINS_MAP);
                break;
            case ARRAY:
                checkGuard = convertArgToNumber(RANGE_CHECK_ARRAY, linkerServices, callSiteDescriptor);
                break;
            default:
                throw new AssertionError();
        }
        final GuardedInvocationComponent finalNextComponent;
        if (nextComponent != null) {
            finalNextComponent = nextComponent;
        } else {
            finalNextComponent = createGuardedInvocationComponentAsType(noOp, callSiteType, linkerServices);
        }
        final GuardedInvocationComponent gic = gicact.gic;
        final GuardedInvocation gi = gic.getGuardedInvocation();
        final MethodPair matchedInvocations = matchReturnTypes(binder.bind(gi.getInvocation()), finalNextComponent.getGuardedInvocation().getInvocation());
        return finalNextComponent.compose(matchedInvocations.guardWithTest(binder.bindTest(checkGuard)), gi.getGuard(), gic.getValidatorClass(), gic.getValidationType());
    }

    private static GuardedInvocationComponent createInternalFilteredGuardedInvocationComponent(final MethodHandle invocation, final LinkerServices linkerServices) {
        return new GuardedInvocationComponent(linkerServices.filterInternalObjects(invocation));
    }

    private static GuardedInvocationComponent createGuardedInvocationComponentAsType(final MethodHandle invocation, final MethodType fromType, final LinkerServices linkerServices) {
        return new GuardedInvocationComponent(linkerServices.asType(invocation, fromType));
    }

    private static GuardedInvocationComponent createInternalFilteredGuardedInvocationComponent(final MethodHandle invocation, final MethodHandle guard, final Class<?> validatorClass, final ValidationType validationType, final LinkerServices linkerServices) {
        return new GuardedInvocationComponent(linkerServices.filterInternalObjects(invocation), guard, validatorClass, validationType);
    }

    private static Integer convertKeyToInteger(final Object fixedKey, final LinkerServices linkerServices) throws Exception {
        if (fixedKey instanceof Integer) {
            return (Integer) fixedKey;
        }
        final Number n;
        if (fixedKey instanceof Number) {
            n = (Number) fixedKey;
        } else {
            final Class<?> keyClass = fixedKey.getClass();
            if (linkerServices.canConvert(keyClass, Number.class)) {
                final Object val;
                try {
                    val = linkerServices.getTypeConverter(keyClass, Number.class).invoke(fixedKey);
                } catch (Exception | Error e) {
                    throw e;
                } catch (final Throwable t) {
                    throw new RuntimeException(t);
                }
                if (!(val instanceof Number)) {
                    return null;
                }
                n = (Number) val;
            } else if (fixedKey instanceof String) {
                try {
                    return Integer.valueOf((String) fixedKey);
                } catch (final NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        if (n instanceof Integer) {
            return (Integer) n;
        }
        final int intIndex = n.intValue();
        final double doubleValue = n.doubleValue();
        if (intIndex != doubleValue && !Double.isInfinite(doubleValue)) {
            return null;
        }
        return intIndex;
    }

    private static MethodHandle convertArgToNumber(final MethodHandle mh, final LinkerServices ls, final CallSiteDescriptor desc) {
        final Class<?> sourceType = desc.getMethodType().parameterType(1);
        if (TypeUtilities.isMethodInvocationConvertible(sourceType, Number.class)) {
            return mh;
        } else if (ls.canConvert(sourceType, Number.class)) {
            final MethodHandle converter = ls.getTypeConverter(sourceType, Number.class);
            return MethodHandles.filterArguments(mh, 1, converter.asType(converter.type().changeReturnType(mh.type().parameterType(1))));
        }
        return mh;
    }

    private static class Binder {

        private final LinkerServices linkerServices;

        private final MethodType methodType;

        private final Object fixedKey;

        Binder(final LinkerServices linkerServices, final MethodType methodType, final Object fixedKey) {
            this.linkerServices = linkerServices;
            this.methodType = fixedKey == null ? methodType : methodType.insertParameterTypes(1, fixedKey.getClass());
            this.fixedKey = fixedKey;
        }

        MethodHandle bind(final MethodHandle handle) {
            return bindToFixedKey(linkerServices.asTypeLosslessReturn(handle, methodType));
        }

        MethodHandle bindTest(final MethodHandle handle) {
            return bindToFixedKey(Guards.asType(handle, methodType));
        }

        private MethodHandle bindToFixedKey(final MethodHandle handle) {
            return fixedKey == null ? handle : MethodHandles.insertArguments(handle, 1, fixedKey);
        }
    }

    private static final MethodHandle RANGE_CHECK_ARRAY = findRangeCheck(Object.class);

    private static final MethodHandle RANGE_CHECK_LIST = findRangeCheck(List.class);

    private static final MethodHandle CONTAINS_MAP = Lookup.PUBLIC.findVirtual(Map.class, "containsKey", MethodType.methodType(boolean.class, Object.class));

    private static MethodHandle findRangeCheck(final Class<?> collectionType) {
        return Lookup.findOwnStatic(MethodHandles.lookup(), "rangeCheck", boolean.class, collectionType, Object.class);
    }

    @SuppressWarnings("unused")
    private static boolean rangeCheck(final Object array, final Object index) {
        if (!(index instanceof Number)) {
            return false;
        }
        final Number n = (Number) index;
        final int intIndex = n.intValue();
        if (intIndex != n.doubleValue()) {
            return false;
        }
        return 0 <= intIndex && intIndex < Array.getLength(array);
    }

    @SuppressWarnings("unused")
    private static boolean rangeCheck(final List<?> list, final Object index) {
        if (!(index instanceof Number)) {
            return false;
        }
        final Number n = (Number) index;
        final int intIndex = n.intValue();
        if (intIndex != n.doubleValue()) {
            return false;
        }
        return 0 <= intIndex && intIndex < list.size();
    }

    @SuppressWarnings("unused")
    private static void noOpSetter() {
    }

    private static final MethodHandle SET_LIST_ELEMENT = Lookup.PUBLIC.findVirtual(List.class, "set", MethodType.methodType(Object.class, int.class, Object.class));

    private static final MethodHandle PUT_MAP_ELEMENT = Lookup.PUBLIC.findVirtual(Map.class, "put", MethodType.methodType(Object.class, Object.class, Object.class));

    private static final MethodHandle NO_OP_SETTER_2;

    private static final MethodHandle NO_OP_SETTER_3;

    static {
        final MethodHandle noOpSetter = Lookup.findOwnStatic(MethodHandles.lookup(), "noOpSetter", void.class);
        NO_OP_SETTER_2 = dropObjectArguments(noOpSetter, 2);
        NO_OP_SETTER_3 = dropObjectArguments(noOpSetter, 3);
    }

    private GuardedInvocationComponent getElementSetter(final ComponentLinkRequest req) throws Exception {
        final CallSiteDescriptor callSiteDescriptor = req.getDescriptor();
        final Object name = req.name;
        final boolean isFixedKey = name != null;
        assertParameterCount(callSiteDescriptor, isFixedKey ? 2 : 3);
        final LinkerServices linkerServices = req.linkerServices;
        final MethodType callSiteType = callSiteDescriptor.getMethodType();
        final GuardedInvocationComponentAndCollectionType gicact = guardedInvocationComponentAndCollectionType(callSiteType, linkerServices, MethodHandles::arrayElementSetter, SET_LIST_ELEMENT, PUT_MAP_ELEMENT);
        if (gicact == null) {
            return getNextComponent(req);
        }
        final boolean isMap = gicact.collectionType == CollectionType.MAP;
        final GuardedInvocationComponent nextComponent = isMap ? null : getNextComponent(req);
        final Object typedName = getTypedName(name, isMap, linkerServices);
        if (typedName == INVALID_NAME) {
            return nextComponent;
        }
        final GuardedInvocationComponent gic = gicact.gic;
        final GuardedInvocation gi = gic.getGuardedInvocation();
        final Binder binder = new Binder(linkerServices, callSiteType, typedName);
        final MethodHandle invocation = gi.getInvocation();
        if (isMap) {
            return gic.replaceInvocation(binder.bind(invocation));
        }
        return guardComponentWithRangeCheck(gicact, linkerServices, callSiteDescriptor, nextComponent, binder, isFixedKey ? NO_OP_SETTER_2 : NO_OP_SETTER_3);
    }

    private static final MethodHandle GET_COLLECTION_LENGTH = Lookup.PUBLIC.findVirtual(Collection.class, "size", MethodType.methodType(int.class));

    private static final MethodHandle GET_MAP_LENGTH = Lookup.PUBLIC.findVirtual(Map.class, "size", MethodType.methodType(int.class));

    private static void assertParameterCount(final CallSiteDescriptor descriptor, final int paramCount) {
        if (descriptor.getMethodType().parameterCount() != paramCount) {
            throw new BootstrapMethodError(descriptor.getOperation() + " must have exactly " + paramCount + " parameters.");
        }
    }
}