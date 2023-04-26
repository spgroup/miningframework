package jdk.nashorn.internal.runtime.linker;

import jdk.nashorn.internal.lookup.MethodHandleFunctionality;
import jdk.nashorn.internal.lookup.MethodHandleFactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.internal.dynalink.CallSiteDescriptor;
import jdk.internal.dynalink.linker.GuardedInvocation;
import jdk.internal.dynalink.linker.LinkRequest;
import jdk.internal.dynalink.linker.LinkerServices;
import jdk.internal.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.internal.dynalink.support.CallSiteDescriptorFactory;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.api.scripting.JSObject;

final class JSObjectLinker implements TypeBasedGuardingDynamicLinker {

    @Override
    public boolean canLinkType(final Class<?> type) {
        return canLinkTypeStatic(type);
    }

    static boolean canLinkTypeStatic(final Class<?> type) {
        return JSObject.class.isAssignableFrom(type);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(final LinkRequest request, final LinkerServices linkerServices) throws Exception {
        final LinkRequest requestWithoutContext = request.withoutRuntimeContext();
        final Object self = requestWithoutContext.getReceiver();
        final CallSiteDescriptor desc = requestWithoutContext.getCallSiteDescriptor();
        if (desc.getNameTokenCount() < 2 || !"dyn".equals(desc.getNameToken(CallSiteDescriptor.SCHEME))) {
            return null;
        }
        final GuardedInvocation inv;
        if (self instanceof JSObject) {
            inv = lookup(desc);
        } else {
            throw new AssertionError();
        }
        return Bootstrap.asType(inv, linkerServices, desc);
    }

    private static GuardedInvocation lookup(final CallSiteDescriptor desc) {
        final String operator = CallSiteDescriptorFactory.tokenizeOperators(desc).get(0);
        final int c = desc.getNameTokenCount();
        switch(operator) {
            case "getProp":
            case "getElem":
            case "getMethod":
                return c > 2 ? findGetMethod(desc) : findGetIndexMethod();
            case "setProp":
            case "setElem":
                return c > 2 ? findSetMethod(desc) : findSetIndexMethod();
            case "call":
                return findCallMethod(desc, operator);
            case "callMethod":
                return findCallMethodMethod(desc, operator);
            case "new":
                return findNewMethod(desc);
            default:
                return null;
        }
    }

    private static GuardedInvocation findGetMethod(final CallSiteDescriptor desc) {
        final MethodHandle getter = MH.insertArguments(JSOBJECT_GETMEMBER, 1, desc.getNameToken(2));
        return new GuardedInvocation(getter, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findGetIndexMethod() {
        return new GuardedInvocation(JSOBJECTLINKER_GET, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findSetMethod(final CallSiteDescriptor desc) {
        final MethodHandle getter = MH.insertArguments(JSOBJECT_SETMEMBER, 1, desc.getNameToken(2));
        return new GuardedInvocation(getter, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findSetIndexMethod() {
        return new GuardedInvocation(JSOBJECTLINKER_PUT, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findCallMethodMethod(final CallSiteDescriptor desc, final String operator) {
        final String methodName = desc.getNameToken(2);
        MethodHandle func = MH.insertArguments(JSOBJECT_CALLMEMBER, 1, methodName);
        func = MH.asCollector(func, Object[].class, desc.getMethodType().parameterCount() - 1);
        return new GuardedInvocation(func, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findCallMethod(final CallSiteDescriptor desc, final String operator) {
        final MethodHandle func = MH.asCollector(JSOBJECT_CALL, Object[].class, desc.getMethodType().parameterCount() - 2);
        return new GuardedInvocation(func, null, IS_JSOBJECT_GUARD);
    }

    private static GuardedInvocation findNewMethod(final CallSiteDescriptor desc) {
        final MethodHandle func = MH.asCollector(JSOBJECT_NEW, Object[].class, desc.getMethodType().parameterCount() - 1);
        return new GuardedInvocation(func, null, IS_JSOBJECT_GUARD);
    }

    @SuppressWarnings("unused")
    private static boolean isJSObject(final Object self) {
        return self instanceof JSObject;
    }

    @SuppressWarnings("unused")
    private static Object get(final Object jsobj, final Object key) {
        if (key instanceof Integer) {
            return ((JSObject) jsobj).getSlot((Integer) key);
        } else if (key instanceof Number) {
            final int index = getIndex((Number) key);
            if (index > -1) {
                return ((JSObject) jsobj).getSlot(index);
            }
        } else if (key instanceof String) {
            return ((JSObject) jsobj).getMember((String) key);
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static void put(final Object jsobj, final Object key, final Object value) {
        if (key instanceof Integer) {
            ((JSObject) jsobj).setSlot((Integer) key, value);
        } else if (key instanceof Number) {
            ((JSObject) jsobj).setSlot(getIndex((Number) key), value);
        } else if (key instanceof String) {
            ((JSObject) jsobj).setMember((String) key, value);
        }
    }

    private static int getIndex(final Number n) {
        final double value = n.doubleValue();
        return JSType.isRepresentableAsInt(value) ? (int) value : -1;
    }

    private static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();

    private static final MethodHandle IS_JSOBJECT_GUARD = findOwnMH("isJSObject", boolean.class, Object.class);

    private static final MethodHandle JSOBJECTLINKER_GET = findOwnMH("get", Object.class, Object.class, Object.class);

    private static final MethodHandle JSOBJECTLINKER_PUT = findOwnMH("put", Void.TYPE, Object.class, Object.class, Object.class);

    private static final MethodHandle JSOBJECT_GETMEMBER = findJSObjectMH("getMember", Object.class, String.class);

    private static final MethodHandle JSOBJECT_SETMEMBER = findJSObjectMH("setMember", Void.TYPE, String.class, Object.class);

    private static final MethodHandle JSOBJECT_CALLMEMBER = findJSObjectMH("callMember", Object.class, String.class, Object[].class);

    private static final MethodHandle JSOBJECT_CALL = findJSObjectMH("call", Object.class, Object.class, Object[].class);

    private static final MethodHandle JSOBJECT_NEW = findJSObjectMH("newObject", Object.class, Object[].class);

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        final Class<?> own = JSObjectLinker.class;
        final MethodType mt = MH.type(rtype, types);
        try {
            return MH.findStatic(MethodHandles.lookup(), own, name, mt);
        } catch (final MethodHandleFactory.LookupException e) {
            return MH.findVirtual(MethodHandles.lookup(), own, name, mt);
        }
    }

    private static MethodHandle findJSObjectMH(final String name, final Class<?> rtype, final Class<?>... types) {
        final Class<?> own = JSObject.class;
        final MethodType mt = MH.type(rtype, types);
        try {
            return MH.findVirtual(MethodHandles.publicLookup(), own, name, mt);
        } catch (final MethodHandleFactory.LookupException e) {
            return MH.findVirtual(MethodHandles.lookup(), own, name, mt);
        }
    }
}
