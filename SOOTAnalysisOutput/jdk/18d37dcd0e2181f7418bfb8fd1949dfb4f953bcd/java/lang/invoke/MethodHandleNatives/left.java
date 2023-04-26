package java.lang.invoke;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;
import sun.misc.Cleaner;

class MethodHandleNatives {

    private MethodHandleNatives() {
    }

    static native void init(MemberName self, Object ref);

    static native void expand(MemberName self);

    static native MemberName resolve(MemberName self, Class<?> caller) throws LinkageError;

    static native int getMembers(Class<?> defc, String matchName, String matchSig, int matchFlags, Class<?> caller, int skip, MemberName[] results);

    static native long objectFieldOffset(MemberName self);

    static native long staticFieldOffset(MemberName self);

    static native Object staticFieldBase(MemberName self);

    static native Object getMemberVMInfo(MemberName self);

    static native void setCallSiteTargetNormal(CallSite site, MethodHandle target);

    static native void setCallSiteTargetVolatile(CallSite site, MethodHandle target);

    static class CallSiteContext implements Runnable {

        static CallSiteContext make(CallSite cs) {
            final CallSiteContext newContext = new CallSiteContext();
            Cleaner.create(cs, newContext);
            return newContext;
        }

        @Override
        public void run() {
            MethodHandleNatives.clearCallSiteContext(this);
        }
    }

    private static native void clearCallSiteContext(CallSiteContext context);

    private static native void registerNatives();

    static {
        registerNatives();
        MethodHandleImpl.initStatics();
    }

    static class Constants {

        Constants() {
        }

        static final int MN_IS_METHOD = 0x00010000, MN_IS_CONSTRUCTOR = 0x00020000, MN_IS_FIELD = 0x00040000, MN_IS_TYPE = 0x00080000, MN_CALLER_SENSITIVE = 0x00100000, MN_REFERENCE_KIND_SHIFT = 24, MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT, MN_SEARCH_SUPERCLASSES = 0x00100000, MN_SEARCH_INTERFACES = 0x00200000;

        static final byte REF_NONE = 0, REF_getField = 1, REF_getStatic = 2, REF_putField = 3, REF_putStatic = 4, REF_invokeVirtual = 5, REF_invokeStatic = 6, REF_invokeSpecial = 7, REF_newInvokeSpecial = 8, REF_invokeInterface = 9, REF_LIMIT = 10;
    }

    static boolean refKindIsValid(int refKind) {
        return (refKind > REF_NONE && refKind < REF_LIMIT);
    }

    static boolean refKindIsField(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind <= REF_putStatic);
    }

    static boolean refKindIsGetter(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind <= REF_getStatic);
    }

    static boolean refKindIsSetter(byte refKind) {
        return refKindIsField(refKind) && !refKindIsGetter(refKind);
    }

    static boolean refKindIsMethod(byte refKind) {
        return !refKindIsField(refKind) && (refKind != REF_newInvokeSpecial);
    }

    static boolean refKindIsConstructor(byte refKind) {
        return (refKind == REF_newInvokeSpecial);
    }

    static boolean refKindHasReceiver(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind & 1) != 0;
    }

    static boolean refKindIsStatic(byte refKind) {
        return !refKindHasReceiver(refKind) && (refKind != REF_newInvokeSpecial);
    }

    static boolean refKindDoesDispatch(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind == REF_invokeVirtual || refKind == REF_invokeInterface);
    }

    static {
        final int HR_MASK = ((1 << REF_getField) | (1 << REF_putField) | (1 << REF_invokeVirtual) | (1 << REF_invokeSpecial) | (1 << REF_invokeInterface));
        for (byte refKind = REF_NONE + 1; refKind < REF_LIMIT; refKind++) {
            assert (refKindHasReceiver(refKind) == (((1 << refKind) & HR_MASK) != 0)) : refKind;
        }
    }

    static String refKindName(byte refKind) {
        assert (refKindIsValid(refKind));
        switch(refKind) {
            case REF_getField:
                return "getField";
            case REF_getStatic:
                return "getStatic";
            case REF_putField:
                return "putField";
            case REF_putStatic:
                return "putStatic";
            case REF_invokeVirtual:
                return "invokeVirtual";
            case REF_invokeStatic:
                return "invokeStatic";
            case REF_invokeSpecial:
                return "invokeSpecial";
            case REF_newInvokeSpecial:
                return "newInvokeSpecial";
            case REF_invokeInterface:
                return "invokeInterface";
            default:
                return "REF_???";
        }
    }

    private static native int getNamedCon(int which, Object[] name);

    static boolean verifyConstants() {
        Object[] box = { null };
        for (int i = 0; ; i++) {
            box[0] = null;
            int vmval = getNamedCon(i, box);
            if (box[0] == null)
                break;
            String name = (String) box[0];
            try {
                Field con = Constants.class.getDeclaredField(name);
                int jval = con.getInt(null);
                if (jval == vmval)
                    continue;
                String err = (name + ": JVM has " + vmval + " while Java has " + jval);
                if (name.equals("CONV_OP_LIMIT")) {
                    System.err.println("warning: " + err);
                    continue;
                }
                throw new InternalError(err);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                String err = (name + ": JVM has " + vmval + " which Java does not define");
                continue;
            }
        }
        return true;
    }

    static {
        assert (verifyConstants());
    }

    static MemberName linkCallSite(Object callerObj, Object bootstrapMethodObj, Object nameObj, Object typeObj, Object staticArguments, Object[] appendixResult) {
        MethodHandle bootstrapMethod = (MethodHandle) bootstrapMethodObj;
        Class<?> caller = (Class<?>) callerObj;
        String name = nameObj.toString().intern();
        MethodType type = (MethodType) typeObj;
        if (!TRACE_METHOD_LINKAGE)
            return linkCallSiteImpl(caller, bootstrapMethod, name, type, staticArguments, appendixResult);
        return linkCallSiteTracing(caller, bootstrapMethod, name, type, staticArguments, appendixResult);
    }

    static MemberName linkCallSiteImpl(Class<?> caller, MethodHandle bootstrapMethod, String name, MethodType type, Object staticArguments, Object[] appendixResult) {
        CallSite callSite = CallSite.makeSite(bootstrapMethod, name, type, staticArguments, caller);
        if (callSite instanceof ConstantCallSite) {
            appendixResult[0] = callSite.dynamicInvoker();
            return Invokers.linkToTargetMethod(type);
        } else {
            appendixResult[0] = callSite;
            return Invokers.linkToCallSiteMethod(type);
        }
    }

    static MemberName linkCallSiteTracing(Class<?> caller, MethodHandle bootstrapMethod, String name, MethodType type, Object staticArguments, Object[] appendixResult) {
        Object bsmReference = bootstrapMethod.internalMemberName();
        if (bsmReference == null)
            bsmReference = bootstrapMethod;
        Object staticArglist = (staticArguments instanceof Object[] ? java.util.Arrays.asList((Object[]) staticArguments) : staticArguments);
        System.out.println("linkCallSite " + caller.getName() + " " + bsmReference + " " + name + type + "/" + staticArglist);
        try {
            MemberName res = linkCallSiteImpl(caller, bootstrapMethod, name, type, staticArguments, appendixResult);
            System.out.println("linkCallSite => " + res + " + " + appendixResult[0]);
            return res;
        } catch (Throwable ex) {
            System.out.println("linkCallSite => throw " + ex);
            throw ex;
        }
    }

    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        return MethodType.makeImpl(rtype, ptypes, true);
    }

    static MemberName linkMethod(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        if (!TRACE_METHOD_LINKAGE)
            return linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
        return linkMethodTracing(callerClass, refKind, defc, name, type, appendixResult);
    }

    static MemberName linkMethodImpl(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        try {
            if (defc == MethodHandle.class && refKind == REF_invokeVirtual) {
                return Invokers.methodHandleInvokeLinkerMethod(name, fixMethodType(callerClass, type), appendixResult);
            }
        } catch (Throwable ex) {
            if (ex instanceof LinkageError)
                throw (LinkageError) ex;
            else
                throw new LinkageError(ex.getMessage(), ex);
        }
        throw new LinkageError("no such method " + defc.getName() + "." + name + type);
    }

    private static MethodType fixMethodType(Class<?> callerClass, Object type) {
        if (type instanceof MethodType)
            return (MethodType) type;
        else
            return MethodType.fromDescriptor((String) type, callerClass.getClassLoader());
    }

    static MemberName linkMethodTracing(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        System.out.println("linkMethod " + defc.getName() + "." + name + type + "/" + Integer.toHexString(refKind));
        try {
            MemberName res = linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
            System.out.println("linkMethod => " + res + " + " + appendixResult[0]);
            return res;
        } catch (Throwable ex) {
            System.out.println("linkMethod => throw " + ex);
            throw ex;
        }
    }

    static MethodHandle linkMethodHandleConstant(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type) {
        try {
            Lookup lookup = IMPL_LOOKUP.in(callerClass);
            assert (refKindIsValid(refKind));
            return lookup.linkMethodHandleConstant((byte) refKind, defc, name, type);
        } catch (IllegalAccessException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AbstractMethodError) {
                throw (AbstractMethodError) cause;
            } else {
                Error err = new IllegalAccessError(ex.getMessage());
                throw initCauseFrom(err, ex);
            }
        } catch (NoSuchMethodException ex) {
            Error err = new NoSuchMethodError(ex.getMessage());
            throw initCauseFrom(err, ex);
        } catch (NoSuchFieldException ex) {
            Error err = new NoSuchFieldError(ex.getMessage());
            throw initCauseFrom(err, ex);
        } catch (ReflectiveOperationException ex) {
            Error err = new IncompatibleClassChangeError();
            throw initCauseFrom(err, ex);
        }
    }

    private static Error initCauseFrom(Error err, Exception ex) {
        Throwable th = ex.getCause();
        if (err.getClass().isInstance(th))
            return (Error) th;
        err.initCause(th == null ? ex : th);
        return err;
    }

    static boolean isCallerSensitive(MemberName mem) {
        if (!mem.isInvocable())
            return false;
        return mem.isCallerSensitive() || canBeCalledVirtual(mem);
    }

    static boolean canBeCalledVirtual(MemberName mem) {
        assert (mem.isInvocable());
        Class<?> defc = mem.getDeclaringClass();
        switch(mem.getName()) {
            case "checkMemberAccess":
                return canBeCalledVirtual(mem, java.lang.SecurityManager.class);
            case "getContextClassLoader":
                return canBeCalledVirtual(mem, java.lang.Thread.class);
        }
        return false;
    }

    static boolean canBeCalledVirtual(MemberName symbolicRef, Class<?> definingClass) {
        Class<?> symbolicRefClass = symbolicRef.getDeclaringClass();
        if (symbolicRefClass == definingClass)
            return true;
        if (symbolicRef.isStatic() || symbolicRef.isPrivate())
            return false;
        return (definingClass.isAssignableFrom(symbolicRefClass) || symbolicRefClass.isInterface());
    }
}
